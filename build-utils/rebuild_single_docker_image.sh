#!/bin/bash
set -euxo pipefail
# This Script checks if a docker image for a given set of input files already exists in the registry.
# If not, it will be rebuilt and pushed
# usage:
# rebuild_single_docker_image.sh image_name dockerfile [additional_relevant_files...]
# e.g.: rebuild_single_docker_image.sh <image-name> <path to Dockerfile> <first file that is relevant> <second file that is relevant> ...
docker_image_name=$1
if [ "$docker_image_name" == "*-*" ];
then
  echo "ERROR: Docker image name contains a '-' which is forbidden"
  exit 1
fi


# shift removes the first argument which is not a file.
# Now $@ contains only filenames where a change in any file content should trigger a rebuild
shift
dockerfile=$1
echo Rebuilding docker image. Parameters: "$@"
input_sha1=$(tar --sort=name --owner=root:0 --group=root:0 --mtime='UTC 2019-01-01' -cvf - "$0" "$@" | sha1sum | awk '{print $1}')

echo Input sha1 Hash: "$input_sha1"

# Only execute the "build" command if the manifests are different.
full_image_reference="ghcr.io/d-fine/dataland/$docker_image_name:$input_sha1"
echo "${docker_image_name^^}_VERSION=$input_sha1" >> $GITHUB_ENV
sha1_manifest=$(docker manifest inspect "$full_image_reference" || echo "no sha1 manifest")
if [ "$sha1_manifest" == "no sha1 manifest" ] || [ "${FORCE_BUILD:-}" == "true" ] || [[ "${COMMIT_MESSAGE:-}" == *"FORCE_BUILD"* ]];
then
  echo "rebuilding image $full_image_reference"
  docker build -f "$dockerfile" . -t "$full_image_reference" --build-arg PROXY_ENVIRONMENT="${PROXY_ENVIRONMENT:-}" --build-arg GITHUB_TOKEN="${GITHUB_TOKEN:-}" --build-arg GITHUB_USER="${GITHUB_USER:-}" --build-arg DATALAND_PROXY_BASE_VERSION="${DATALAND_PROXY_BASE_VERSION:-}"
  docker push "$full_image_reference"
else
  echo "Requirements already satisfied!"
fi