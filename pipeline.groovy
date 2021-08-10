
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