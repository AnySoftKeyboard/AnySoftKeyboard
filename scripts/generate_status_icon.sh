#!/usr/bin/env bash

TEXT=$1
FILE_POSTFIX=$2
SIZE=$3
FONT_SIZE=$4
TARGET_FOLDER=$5

mkdir -p ${TARGET_FOLDER}
convert -background transparent -fill white \
          -size ${SIZE}x${SIZE} -pointsize ${FONT_SIZE} -gravity center \
          label:${TEXT} \
          ${TARGET_FOLDER}/ic_status_${FILE_POSTFIX}.png