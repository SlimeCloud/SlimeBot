#!/bin/bash

CONTAINER_NAME="slimetest"
IMAGE_NAME="slimetest"

if [ $(docker ps -q -f name=$CONTAINER_NAME) ]; then
    echo "Stopping container: $CONTAINER_NAME"
    docker stop $CONTAINER_NAME
    echo "Removing container: $CONTAINER_NAME"
    docker rm $CONTAINER_NAME
else
    echo "Container $CONTAINER_NAME is not running."
fi

if [ $(docker images -q $IMAGE_NAME) ]; then
    echo "Removing image: $IMAGE_NAME"
    docker rmi $IMAGE_NAME
else
    echo "Image $IMAGE_NAME does not exist."
fi

echo "Building image: $IMAGE_NAME"
docker build -t $IMAGE_NAME .

echo "Starting container: $CONTAINER_NAME"
docker run -d --name $CONTAINER_NAME $IMAGE_NAME