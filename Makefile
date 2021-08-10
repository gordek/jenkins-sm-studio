DIR := ${PWD}
DOCKER_DIR := ${DIR}/docker
ENVIRONMENTS_DIR := ${DIR}/environments
AWS_PROFILE := default

FULL_BASE_IMAGE_NAME ?= jupyter/base-notebook:python-3.7.6
AWS_ACCOUNT_ID ?= .
AWS_REGION ?= us-west-2
REPO ?= custom-racer-manual
IMAGE_TAG := latest
DOCKER_FILE_PATH ?= .
APP_IMAGE_CONFIG_PATH ?= .

AWS_ACCOUNT_ID ?= $(shell aws sts get-caller-identity --query "Account")

IMAGE_NAME := $(shell echo "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${REPO}")
FULL_IMAGE_NAME = ${IMAGE_NAME}:${IMAGE_TAG}

ROLE_ARN := $(shell aws --region ${AWS_REGION} iam list-roles --output text --query "Roles[?starts_with(RoleName, 'AmazonSageMaker-ExecutionRole')].Arn")
SM_DOMAIN_ID := $(shell aws --region ${AWS_REGION} sagemaker list-domains --output text --query "Domains[].DomainId" )


login:
	aws --region ${AWS_REGION} ecr get-login-password --profile ${AWS_PROFILE} | docker login --username AWS --password-stdin ${IMAGE_NAME}
# create-ecr-repo:
# 	aws ecr create-repository --repository-name "${REPO}" --region ${AWS_REGION} \
# 	&& aws ecr set-repository-policy --region ${AWS_REGION} --repository-name $REPO --policy-text file://docker/ecr-policy.json
# create-ecr-repo-ifdoesnotexist:
# 	aws ecr describe-repositories --repository-names ${REPO} --region ${AWS_REGION} || aws ecr create-repository --repository-name ${REPO} --region ${AWS_REGION}
# build:
# 	docker build -t ${FULL_IMAGE_NAME} -t ${REPO}:${IMAGE_TAG} \
# 	--build-arg DOCKER_REPO_NAME=${REPO} \
# 	--build-arg BASE_IMAGE=${FULL_BASE_IMAGE_NAME} \
# 	.
# push:
# 	docker push ${FULL_IMAGE_NAME}
# create-image:
# 	aws --region ${AWS_REGION} sagemaker create-image --image-name ${REPO}-${KERNEL_NAME}-${IMAGE_TAG} --role-arn ${ROLE_ARN}
# update-image:
# 	aws --region ${AWS_REGION} sagemaker update-image --image-name ${REPO}-${KERNEL_NAME}-${IMAGE_TAG} --role-arn ${ROLE_ARN}
# create-image-version:
# 	aws --region ${AWS_REGION} sagemaker create-image-version --image-name custom-racer --base-image ${IMAGE_NAME}:${IMAGE_TAG} --profile ${AWS_PROFILE}
# create-app-image-config:
# 	aws --region ${AWS_REGION} sagemaker create-app-image-config --cli-input-json file://${APP_IMAGE_CONFIG_PATH}
# update-app-image-config:
# 	aws --region ${AWS_REGION} sagemaker update-app-image-config --cli-input-json file://${APP_IMAGE_CONFIG_PATH}
# describe-in-service-apps:
# 	aws sagemaker list-apps \
# 		--output text --query "Apps[?starts_with(Status,'InService')]|[?DomainId=='${SM_DOMAIN_ID}'][UserProfileName, AppType, AppName]" | \
# 		while read app; \
# 		do aws sagemaker describe-app \
# 			--domain-id ${SM_DOMAIN_ID} \
# 			--user-profile-name $$(echo $$app|awk '{print $$1}') \
# 			--app-type $$(echo $$app|awk '{print $$2}') \
# 			--app-name $$(echo $$app|awk '{print $$3}'); \
# 		done
# describe-domain:
# 	aws --region ${AWS_REGION} sagemaker describe-domain --domain-id ${SM_DOMAIN_ID} --output json >> ${APP_IMAGE_CONFIG_PATH}
# update-domain:
# 	cat ${APP_IMAGE_CONFIG_PATH} && \
# 	aws --region ${AWS_REGION} sagemaker update-domain --domain-id ${SM_DOMAIN_ID} --cli-input-json file://update_domain.json --profile ${AWS_PROFILE}

# build_n_push: login build push

# build_n_push_with_repo: login create-ecr-repo-ifdoesnotexist build push

# sagemaker_attach_image: create-image create-image-version create-app-config

# all: login build push create-image-version
