#!/usr/bin/env bash

PROJECT_DIR=$1
BUILD_DIR=$2
ASSETS_DIR=$3

mkdir -p ${BUILD_DIR}/image_web_temp

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
    ${BUILD_DIR}/image_web_temp/flag_final.png

mkdir -p ${PROJECT_DIR}/src/main/play/listings/en-US/graphics/icon
mv ${BUILD_DIR}/image_web_temp/flag_final.png ${PROJECT_DIR}/src/main/play/listings/en-US/graphics/icon/pack_store_icon.png

convert ${SOURCE_FLAG_FILE} \
    -adaptive-resize x256 \
    ${BUILD_DIR}/image_web_temp/flag.png
convert ${ASSETS_DIR}/feature_graphics.png ${BUILD_DIR}/image_web_temp/flag.png \
    -gravity southeast -geometry +16+16 -composite \
    ${BUILD_DIR}/image_web_temp/pack_store_feature_graphics.png

mkdir -p ${PROJECT_DIR}/src/main/play/listings/en-US/graphics/feature-graphic
mv ${BUILD_DIR}/image_web_temp/pack_store_feature_graphics.png ${PROJECT_DIR}/src/main/play/listings/en-US/graphics/feature-graphic/pack_store_feature_graphics.png

