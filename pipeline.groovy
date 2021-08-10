
JUPYTER_IMAGE = 'jupyter/r-notebook:latest'
CERBERUS_SECRET_PATH = 'shared/sagemaker-cicd/awssecret'
AWS_ROLE = 'aiml-sagemaker-test.sagemaker-cicd'
AWS_REGION = 'us-west-2'
ECR_BASE_IMAGE_REPOSITORY = 'sagemaker-studio-p2'
ECR_GPU_IMAGES_REPOSITORY = 'sagemaker-studio-p2-gpu'

def calculateBaseImagesTag(String branch_name, String build) {
    return "latest"
}

def customImageTag(String branch_name, environment) {
    if (branch_name == DEFAULT_BRANCH)
        return "latest"
    else {
        String specialCharRegex = /[^a-zA-Z0-9]/
        return readFile("${WORKSPACE}/environments/${environment}/version.txt").trim().replaceAll(specialCharRegex, '-')
    }
}

def buildAndPush(Map args) {
    stdout = sh(
            returnStdout: true,
            script: "make build_n_push_with_repo " +
                    "FULL_BASE_IMAGE_NAME=${args.base_image_name} " +
                    "AWS_ACCOUNT_ID=${args.account} " +
                    "AWS_REGION=${args.region} " +
                    "REPO=${args.repo} " +
                    "IMAGE_TAG=${args.tag} " +
                    "DOCKER_FILE_PATH=${args.docker_path} "
    )
    return "${stdout}"
}

def ecrlogin(Map args) {
    stdout = sh(
            returnStdout: true,
            script: "make login"
    )
    return "${stdout}"
}


pipeline {
    agent any

    parameters {
        booleanParam(name: 'build_base', defaultValue: false, description: "build base images, only default branch")
        booleanParam(name: 'build_gpu', defaultValue: false, description: "build gpu images, only default branch")
        booleanParam(name: 'rebuild_all_envs', defaultValue: false, description: "Rebuild all environment images")
        string(name: 'build_env', defaultValue: "", description: "Name of environment folder to rebuild")
        string(name: 'BRANCH_NAME', defaultValue: "master", description: "Name of branch")
        booleanParam(name: 'release', defaultValue: false, description: "release to Sage Maker")
        booleanParam(name: 'STOP_ALL', defaultValue: false, description: "!!!! STOP ALL InService Apps when Release !!!!")
    }
    options { skipDefaultCheckout() }
    stages {
    stage('Describe') {
            steps {
                script {
                    envs = sh (returnStdout: true, script: "printenv")
                    print envs
                    currentBuild.description = "branch: $params.BRANCH_NAME\n"
                }
            }
        }
        stage('checkout') {
            steps {
                checkout([
                        $class: 'GitSCM',
                        branches: [[name: params.BRANCH_NAME]],
                        userRemoteConfigs: scm.userRemoteConfigs
                ])
            }
        }
    stage('Build and Push Base Image') {
            when {
                // expression { params.build_base == true && params.BRANCH_NAME == DEFAULT_BRANCH}
                expression { params.build_base == true && params.BRANCH_NAME == 'master'}
            }
            steps {
                script {
                    // withCerberus([sdbPath: CERBERUS_SECRET_PATH, sdbKeys:['aws-test-account':'AWS_ACCOUNT']]) {
                        // withAWS(role: "arn:aws:iam::${AWS_ACCOUNT}:role/${AWS_ROLE}", region: AWS_REGION) {
                            ecrlogin(
                                    account: AWS_ACCOUNT,
                                    region: AWS_REGION,
                                    repo: ECR_BASE_IMAGE_REPOSITORY,
                                    docker_path: "${WORKSPACE}/docker/sm-studio-base/Dockerfile"
                            )
                        // }
                    // }
                }
            }
        }
    }
}