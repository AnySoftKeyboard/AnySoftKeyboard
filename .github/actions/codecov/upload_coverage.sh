#!/usr/bin/env bash
set -e

CODECOV_TOKEN="${1}"

wget --tries=5 --waitretry=5 --progress=dot:mega --output-document=codecov.sh https://codecov.io/bash
chmod +x codecov.sh

TARGET_FOLDER="${2}"

ls -al "${TARGET_FOLDER}"

#some values are auto-detected from env variables
./codecov.sh -t "${CODECOV_TOKEN}" \
    -v \
    -n "job-name-${GITHUB_RUN_ID}" \
    -X coveragepy -X xcode -X gcov \
    -s "${TARGET_FOLDER}"
