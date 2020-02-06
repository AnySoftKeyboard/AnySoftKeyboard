#!/usr/bin/env bash
set -e

OUTPUT=${1}
FILE_PATTERN=${2}

rm -rf "/tmp/${OUTPUT}" || true
mkdir -p "/tmp/${OUTPUT}"
rm -rf "${OUTPUT}" || true
mkdir -p "${OUTPUT}"

TEMP_TAR="/tmp/${OUTPUT}/archive.tar"
tar -cvf "${TEMP_TAR}" --files-from /dev/null
find . -path "${FILE_PATTERN}" -exec tar uvf "${TEMP_TAR}" {} \;

mv "${TEMP_TAR}" "${OUTPUT}/"
