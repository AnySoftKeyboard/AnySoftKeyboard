#!/usr/bin/env bash

TEXT=$1
SIZE=$2
FONT_SIZE=$3
TARGET_FOLDER=$4

mkdir -p ${TARGET_FOLDER}
convert -background transparent -fill white \
          -size ${SIZE}x${SIZE} -pointsize ${FONT_SIZE} -gravity center \
          label:${TEXT} \
          ${TARGET_FOLDER}/ic_status_${TEXT}.png