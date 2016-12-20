#!/usr/bin/env bash

if [[ -f ~/docker/${DOCKER_IMAGE_VERSION}.tar ]]; then
    echo "Have a cached image for ${DOCKER_IMAGE}. Loading..."
    docker load -i ~/docker/${DOCKER_IMAGE_VERSION}.tar
else
    echo "Could not find a cached image for ${DOCKER_IMAGE}. Pulling..."
    docker pull ${DOCKER_IMAGE}
    #deleting any previously cached files
    rm -rf ~/docker || true
    mkdir -p ~/docker
    docker save ${DOCKER_IMAGE} > ~/docker/${DOCKER_IMAGE_VERSION}.tar
fi
