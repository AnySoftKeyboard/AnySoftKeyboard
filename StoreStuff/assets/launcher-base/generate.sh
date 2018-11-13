#!/usr/bin/env bash

PROJECT_DIR=$1
BUILD_DIR=$2
DIMEN=$3
HEIGHT=$4
OFFSET=$5
ASSETS_DIR=$6

mkdir -p ${BUILD_DIR}/image_temp
mkdir -p ${PROJECT_DIR}/src/main/res/mipmap-${DIMEN}
convert ${PROJECT_DIR}/flag/flag.png \
    -adaptive-resize x${HEIGHT} \
    ${BUILD_DIR}/image_temp/flag-${DIMEN}.png
convert ${ASSETS_DIR}/${DIMEN}.png ${BUILD_DIR}/image_temp/flag-${DIMEN}.png \
    -gravity south -geometry +0+${OFFSET} -composite \
    ${PROJECT_DIR}/src/main/res/mipmap-${DIMEN}/ic_launcher.png
