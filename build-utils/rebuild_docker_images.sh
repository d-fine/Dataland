#!/bin/bash
set -euxo pipefail
# This Script checks if a docker image for a given set of input files already exists in the registry.
# If not, it will be rebuilt and pushed
# usage:
# rebuild_docker_images.sh image_name dockerfile [additional_relevant_files...]
# e.g.: rebuild_docker_images.sh <image-name> <path to Dockerfile> <first file that is relevant> <second file that is relevant> ...
DOCKER_IMAGE_NAME=$1

# shift removes the first argument which is not a file.
# Now $@ contains only filenames where a change in any file content should trigger a rebuild
shift
DOCKERFILE=$1
echo Rebuilding docker image. Parameters: "$@"
set +x # don't print out file contents
INPUT_FILES_CONTENT=$(cat "$@" build-utils/rebuild_docker_images.sh)
echo "$INPUT_FILES_CONTENT" > "${CI_PROJECT_DIR}"/logs/rebuild_"${DOCKER_IMAGE_NAME}"_file_content.log
INPUT_MD5=$(echo "$INPUT_FILES_CONTENT" | md5sum | awk '{print $1}')
echo Input MD5 Hash: "$INPUT_MD5"
echo slug commit ref name: "$CI_COMMIT_REF_SLUG"
set -x

# Only execute the "build" command if the manifests are different.
MD5_MANIFEST=$(docker manifest inspect "$CI_REGISTRY_IMAGE"/"$DOCKER_IMAGE_NAME":"$INPUT_MD5" || echo "no MD5 manifest")
COMMIT_REF_MANIFEST=$(docker manifest inspect "$CI_REGISTRY_IMAGE"/"$DOCKER_IMAGE_NAME":"$CI_COMMIT_REF_SLUG" || echo "no commit ref manifest")
if [ "$MD5_MANIFEST" == "$COMMIT_REF_MANIFEST" ]
then
  echo "Requirements already satisfied!"
else
  docker pull "$CI_REGISTRY_IMAGE"/"$DOCKER_IMAGE_NAME":"$INPUT_MD5" || \
  docker build -f "$DOCKERFILE" . -t "$CI_REGISTRY_IMAGE"/"$DOCKER_IMAGE_NAME":"$INPUT_MD5"
  docker push "$CI_REGISTRY_IMAGE"/"$DOCKER_IMAGE_NAME":"$INPUT_MD5"
  docker tag "$CI_REGISTRY_IMAGE"/"$DOCKER_IMAGE_NAME":"$INPUT_MD5" "$CI_REGISTRY_IMAGE"/"$DOCKER_IMAGE_NAME":"$CI_COMMIT_REF_SLUG"
  docker push "$CI_REGISTRY_IMAGE"/"$DOCKER_IMAGE_NAME":"$CI_COMMIT_REF_SLUG"
fi