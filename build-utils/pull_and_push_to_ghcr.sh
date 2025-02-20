#!/usr/bin/env bash
set -euxo pipefail

for DOCKER_HUB_IMAGE in "$@"; do
  # Pull the image from Docker Hub
  docker pull "$DOCKER_HUB_IMAGE"

  # Extract image name for tagging
  IMAGE_NAME=$(echo "$DOCKER_HUB_IMAGE" | cut -d':' -f1)
  IMAGE_TAG=$(echo "$DOCKER_HUB_IMAGE" | cut -d':' -f2 | cut -d'@' -f1)
   GHCR_IMAGE="ghcr.io/d-fine/dataland/$IMAGE_NAME:$IMAGE_TAG

  # Tag the image for GHCR
  docker tag "$DOCKER_HUB_IMAGE" "$GHCR_IMAGE"

  # Push the image to GHCR
  docker push "$GHCR_IMAGE"

  echo "Successfully pulled $DOCKER_HUB_IMAGE and pushed to $GHCR_IMAGE"
done
