#!/bin/bash

FILENAME=$1

if [ -z "${FILENAME}" ]; then
    echo "First argument should be the PNG file to add border to."
    exit 1
fi

convert ${FILENAME} -bordercolor none -border 1x1 -background white -alpha background -channel A -blur 1x1 -level 0,0% ${FILENAME}