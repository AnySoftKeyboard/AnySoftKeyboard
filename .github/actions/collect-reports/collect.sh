#!/usr/bin/env bash
set -e

OUTPUT=${1}
PATTERN=${2}
mkdir ${OUTPUT} || true
find . -path ./${OUTPUT} -prune -o -name "${PATTERN}" -exec cp {} ${OUTPUT}/ \;
