#!/bin/bash
set -euxo pipefail
# This Script checks if a docker image for a given set of input files already exists in the registry.
# If not, it will be rebuilt and pushed
# usage:
# rebuild_docker_images.sh image_name dockerfile [additional_relevant_files...]
# e.g.: rebuild_docker_images.sh <image-name> <path to Dockerfile> <first file that is relevant> <second file that is relevant> ...
#TODO stick to naming convention - local variables lower case
# TODO: Change the cat for the md5 hash to: tar all input files, and the md5 the tar file
# TODO: Store md5 hash as ci parameter/variable
docker_image_name=$1

# shift removes the first argument which is not a file.
# Now $@ contains only filenames where a change in any file content should trigger a rebuild
shift
dockerfile=$1
echo Rebuilding docker image. Parameters: "$@"
set +x # don't print out file contents
input_files_content=$(cat "$@" build-utils/rebuild_docker_images.sh)
echo "$input_files_content" > "${CI_PROJECT_DIR}"/logs/rebuild_"${docker_image_name}"_file_content.log
input_md5=$(echo "$input_files_content" | md5sum | awk '{print $1}')
echo Input MD5 Hash: "$input_md5"
echo slug commit ref name: "$CI_COMMIT_REF_SLUG"
set -x

# Only execute the "build" command if the manifests are different.
md5_manifest=$(docker manifest inspect "$CI_REGISTRY_IMAGE"/"$docker_image_name":"$input_md5" || echo "no MD5 manifest")
commit_ref_manifest=$(docker manifest inspect "$CI_REGISTRY_IMAGE"/"$docker_image_name":"$CI_COMMIT_REF_SLUG" || echo "no commit ref manifest")
if [ "$md5_manifest" == "$commit_ref_manifest" ]

then
  echo "Requirements already satisfied!"
else
  docker pull "$CI_REGISTRY_IMAGE"/"$docker_image_name":"$input_md5" || \
  docker build -f "$dockerfile" . -t "$CI_REGISTRY_IMAGE"/"$docker_image_name":"$input_md5"
  docker push "$CI_REGISTRY_IMAGE"/"$docker_image_name":"$input_md5"
  docker tag "$CI_REGISTRY_IMAGE"/"$docker_image_name":"$input_md5" "$CI_REGISTRY_IMAGE"/"$docker_image_name":"$CI_COMMIT_REF_SLUG"
  docker push "$CI_REGISTRY_IMAGE"/"$docker_image_name":"$CI_COMMIT_REF_SLUG"
fi