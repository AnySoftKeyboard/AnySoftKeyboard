#!/usr/bin/env bash

PROJECT_DIR=$1
BUILD_DIR=$2
DIMEN=$3
HEIGHT=$4
OFFSET=$5
ASSETS_DIR=$6

mkdir -p ${BUILD_DIR}/image_temp
mkdir -p ${PROJECT_DIR}/src/main/res/mipmap-${DIMEN}

SOURCE_FLAG_FILE=${PROJECT_DIR}/flag/flag.png
if [[ ! -f ${SOURCE_FLAG_FILE} ]]; then
    SOURCE_FLAG_FILE=${PROJECT_DIR}/flag/flag.svg
fi
if [[ ! -f ${SOURCE_FLAG_FILE} ]]; then
    echo "Please provide a flag image (svg or png format) and store it at ${PROJECT_DIR}/flag/ as flag.png or flag.svg"
    exit 1
fi

convert ${SOURCE_FLAG_FILE} \
    -adaptive-resize x${HEIGHT} \
    ${BUILD_DIR}/image_temp/flag-${DIMEN}.png
convert ${ASSETS_DIR}/${DIMEN}.png ${BUILD_DIR}/image_temp/flag-${DIMEN}.png \
    -gravity south -geometry +0+${OFFSET} -composite \
    ${PROJECT_DIR}/src/main/res/mipmap-${DIMEN}/ic_launcher.png
