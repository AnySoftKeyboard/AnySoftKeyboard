#!/usr/bin/env bash
set -e

CODECOV_TOKEN="${1}"

wget --tries=5 --waitretry=5 --progress=dot:mega --output-document=codecov.sh https://codecov.io/bash
chmod +x codecov.sh

TARGET_FOLDER="${PWD}/build/jacoco"

ls -al "${TARGET_FOLDER}"

./scripts/retry.sh 5 ./codecov.sh -t "${CODECOV_TOKEN}" -X coveragepy -X xcode -X gcov -s "${TARGET_FOLDER}"
