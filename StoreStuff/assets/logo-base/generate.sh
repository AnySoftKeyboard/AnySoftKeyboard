#!/usr/bin/env bash

PROJECT_DIR=$1
BUILD_DIR=$2
ASSETS_DIR=$3

mkdir -p ${BUILD_DIR}/image_web_temp
mkdir -p ${PROJECT_DIR}/store_logo
convert ${PROJECT_DIR}/flag/flag.png \
    -adaptive-resize x128 \
    ${BUILD_DIR}/image_web_temp/flag.png
convert ${ASSETS_DIR}/web.png ${BUILD_DIR}/image_web_temp/flag.png \
    -gravity south -geometry +0+64 -composite \
    ${PROJECT_DIR}/store_logo/logo.png
