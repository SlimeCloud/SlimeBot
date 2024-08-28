#!/bin/bash

DOCKERFILE_DIR="$1"
CONTAINER_NAME="$2"
IMAGE_NAME="$3"

status=$(docker container inspect -f "{{.State.Status}}" "$CONTAINER_NAME" 2>/dev/null)

if [ $? -eq 0 ]; then
  if [ "$status" != "exited" ] && [ "$status" != "created" ]; then
    echo "Container running... Stopping $CONTAINER_NAME"
    docker stop "$CONTAINER_NAME"
  fi

  echo "Container exists... Removing $CONTAINER_NAME"
  docker rm "$CONTAINER_NAME"
fi

if [ "$(docker images -q "$IMAGE_NAME")" ]; then
    echo "Image exists... Removing $IMAGE_NAME"
    docker rmi "$IMAGE_NAME"
fi

echo "Building image $IMAGE_NAME"
docker build -t "$IMAGE_NAME" "$DOCKERFILE_DIR"

echo "Starting container: $CONTAINER_NAME"
docker run -d -v "$DOCKERFILE_DIR/run:/bot/run" --name "$CONTAINER_NAME" "$IMAGE_NAME" - v ""