#!/usr/bin/env bash

PROJECT_DIR=$1
BUILD_DIR=$2
ASSETS_DIR=$3

mkdir -p ${BUILD_DIR}/image_web_temp
mkdir -p ${PROJECT_DIR}/store_logo

SOURCE_FLAG_FILE=${PROJECT_DIR}/flag/flag.png
if [[ ! -f ${SOURCE_FLAG_FILE} ]]; then
    SOURCE_FLAG_FILE=${PROJECT_DIR}/flag/flag.svg
fi
if [[ ! -f ${SOURCE_FLAG_FILE} ]]; then
    echo "Please provide a flag image (svg or png format) and store it at ${PROJECT_DIR}/flag/ as flag.png or flag.svg"
    exit 1
fi

convert ${SOURCE_FLAG_FILE} \
    -adaptive-resize x128 \
    ${BUILD_DIR}/image_web_temp/flag.png
convert ${ASSETS_DIR}/web.png ${BUILD_DIR}/image_web_temp/flag.png \
    -gravity south -geometry +0+64 -composite \
    ${PROJECT_DIR}/store_logo/logo.png
