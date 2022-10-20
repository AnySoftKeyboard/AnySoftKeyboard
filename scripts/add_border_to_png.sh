#!/bin/bash

FILENAME="$1"
COLOR="$2"
SIZE="${3}x${3}"

function help() {
    echo "Missing arguments"
    echo "add_border_to_png.sh [path-to-png-image] [color] [border size] [optional output-file]"
    echo "Examples:"
    echo "add_border_to_png.sh cancel.png blue 1"
    echo "add_border_to_png.sh cancel.png #aaa 2 output.png"
    exit 1
}
if [ -z "${FILENAME}" ] || [ -z "${COLOR}" ] || [ -z "${SIZE}" ]; then
  echo "FILENAME '${FILENAME}'"
  echo "COLOR '${COLOR}'"
  echo "SIZE '${SIZE}'"
  help
fi

OUTPUT_FILENAME=""${4:-${FILENAME}}""
convert ${FILENAME} -bordercolor none -border ${SIZE} -background ${COLOR} -alpha background -channel A -blur ${SIZE} -level 0,0% ${OUTPUT_FILENAME}
