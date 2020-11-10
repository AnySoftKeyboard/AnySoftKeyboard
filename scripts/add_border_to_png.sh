#!/bin/bash

FILENAME=$1
COLOR=$2
SIZE="${3}x${3}"

function help() {
    echo "Missing arguments"
    echo "add_border_to_png.sh [path-to-png-image] [color] [border size]"
    echo "Examples:"
    echo "add_border_to_png.sh cancel.png blue 1"
    echo "add_border_to_png.sh cancel.png #aaa 2"
    exit 1
}
if [ -z "${FILENAME}" ] || [ -z "${COLOR}" ] || [ -z "${SIZE}" ]; then
    help
fi

convert ${FILENAME} -bordercolor none -border ${SIZE} -background ${COLOR} -alpha background -channel A -blur ${SIZE} -level 0,0% ${FILENAME}
