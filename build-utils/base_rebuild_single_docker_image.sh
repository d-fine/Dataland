#!/usr/bin/env bash
set -euxo pipefail
# This Script checks if a docker image for a given set of input files already exists in the registry.
# If not, it will be rebuilt and pushed
# usage:
# base_rebuild_single_docker_image.sh image_name dockerfile [additional_relevant_files...]
# e.g.: base_rebuild_single_docker_image.sh <image-name> <path to Dockerfile> <first file that is relevant> <second file that is relevant> ...
docker_image_name=$1
if [[ "$docker_image_name" == *"-"* ]];
then
  echo "ERROR: Docker image name '$docker_image_name' contains a '-' which is forbidden"
  exit 1
fi


# shift removes the first argument (it's the docker image name - hence not not a file)
# Now $@ contains only filenames where a change in any file content should trigger a rebuild
shift
dockerfile=$1
echo Rebuilding docker image. Parameters: "$@"

input_sha=$( \
  find "$0" "$@" -type f | awk '{print "\042" $1 "\042"}' | \
  grep -v '/node_modules/\|/dist/\|coverage\|/\.gradle/\|/\.git/\|/build/\|package-lock\.json\|\.log\|/local/\|/\.nyc_output/\|/cypress/\|/venv/' | \
  sort -u | \
  xargs shasum | awk '{print $1}' | \
  shasum | awk '{print $1}'
)

echo Input sha1 Hash: "$input_sha"

# Only execute the "build" command if the manifests are different.
full_image_reference="ghcr.io/d-fine/dataland/$docker_image_name:$input_sha"
echo "${docker_image_name^^}_VERSION=$input_sha" >> ./${BUILD_SCRIPT:-default}_github_env.log
echo "${docker_image_name^^}_VERSION=$input_sha" >> ${GITHUB_OUTPUT:-/dev/null}
sha1_manifest=$(docker manifest inspect "$full_image_reference" || echo "no sha1 manifest")
if [[ ! "$sha1_manifest" =~ "no sha1 manifest" ]] ; then
  echo "docker manifest found. No rebuild for $full_image_reference required"
  exit 0
fi

images_found=$(docker image ls --quiet "$full_image_reference" | wc -w)
if [[ "$images_found" == "1" ]] ; then
  echo "docker image already present locally. No rebuild for $full_image_reference required"
  exit 0
fi
DOCKER_SECRET=$(printf "githubUser=%s\ngithubToken=%s\n" "${GITHUB_USER:-}" "${GITHUB_TOKEN:-}")
export DOCKER_SECRET
docker_build_args=(     --build-arg PROXY_ENVIRONMENT="${PROXY_ENVIRONMENT:-}" \
                        --secret id=DOCKER_SECRET \
                        --build-arg DATALAND_PROXY_BASE_VERSION="${DATALAND_PROXY_BASE_VERSION:-}" \
                        --build-arg DATALAND_E2ETESTS_BASE_VERSION="${DATALAND_E2ETESTS_BASE_VERSION:-}" \
                        --build-arg DATALAND_BACKEND_BASE_VERSION="${DATALAND_BACKEND_BASE_VERSION:-}" \
                        --build-arg DATALAND_API_KEY_MANAGER_BASE_VERSION="${DATALAND_API_KEY_MANAGER_BASE_VERSION:-}" \
                        --build-arg DATALAND_DOCUMENT_MANAGER_BASE_VERSION="${DATALAND_DOCUMENT_MANAGER_BASE_VERSION:-}" \
                        --build-arg DATALAND_QA_SERVICE_BASE_VERSION="${DATALAND_QA_SERVICE_BASE_VERSION:-}" \
                        --build-arg DATALAND_AUTOMATED_QA_SERVICE_BASE_VERSION="${DATALAND_AUTOMATED_QA_SERVICE_BASE_VERSION:-}" \
                        --build-arg DATALAND_INTERNAL_STORAGE_BASE_VERSION="${DATALAND_INTERNAL_STORAGE_BASE_VERSION:-}" \
                        --build-arg DATALAND_BATCH_MANAGER_BASE_VERSION="${DATALAND_BATCH_MANAGER_BASE_VERSION:-}" \
                        --build-arg DATALAND_COMMUNITY_MANAGER_BASE_VERSION="${DATALAND_COMMUNITY_MANAGER_BASE_VERSION:-}" \
                        --build-arg DATALAND_EMAIL_SERVICE_BASE_VERSION="${DATALAND_EMAIL_SERVICE_BASE_VERSION:-}" \
                        --build-arg DATALAND_EXTERNAL_STORAGE_BASE_VERSION="${DATALAND_EXTERNAL_STORAGE_BASE_VERSION:-}" \
                        --build-arg DATALAND_DUMMY_EURODAT_CLIENT_BASE_VERSION="${DATALAND_DUMMY_EURODAT_CLIENT_BASE_VERSION:-}" \
                        --build-arg DATALAND_DUMMY_EURODAT_CLIENT_TEST_VERSION="${DATALAND_DUMMY_EURODAT_CLIENT_TEST_VERSION:-}" \
                        --build-arg DATALAND_GRADLE_BASE_VERSION="${DATALAND_GRADLE_BASE_VERSION:-}" \
                        --build-arg DOCUMENT_UPLOAD_MAX_FILE_SIZE_IN_MEGABYTES="${DOCUMENT_UPLOAD_MAX_FILE_SIZE_IN_MEGABYTES:-}" \
                        --build-arg DATA_REQUEST_UPLOAD_MAX_FILE_SIZE_IN_MEGABYTES="${DATA_REQUEST_UPLOAD_MAX_FILE_SIZE_IN_MEGABYTES:-}" \
                        --build-arg MAX_NUMBER_OF_DATA_REQUESTS_PER_DAY_FOR_ROLE_USER="${MAX_NUMBER_OF_DATA_REQUESTS_PER_DAY_FOR_ROLE_USER:-}" \
                        --build-arg MAX_NUMBER_OF_DAYS_SELECTABLE_FOR_API_KEY_VALIDITY="${MAX_NUMBER_OF_DAYS_SELECTABLE_FOR_API_KEY_VALIDITY:-}"
                  )

if [[ ${GITHUB_ACTIONS:-} == "true" ]]; then
  echo "Running in Github Actions - using gha-type cache and --push flag"
  docker_build_environment_parameters=(--push --cache-to "type=gha,scope=$GITHUB_REF_NAME-$docker_image_name" --cache-from "type=gha,scope=$GITHUB_REF_NAME-$docker_image_name")
else
  echo "Running outside Github Actions - using no cache and no --push flag"
  docker_build_environment_parameters=(--builder default --load)
fi
echo "rebuilding image $full_image_reference"
docker buildx build -f "$dockerfile" . -t "$full_image_reference" \
   "${docker_build_args[@]}" \
   "${docker_build_environment_parameters[@]}"
