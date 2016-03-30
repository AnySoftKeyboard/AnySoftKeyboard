#!/usr/bin/env bash

COMMIT_BRANCH=$1

if [ -z ${COMMIT_BRANCH} ]; then
    echo "You must supply branch name as first argument!"
    exit 1
fi

IS_POST_MERGE=0
if [ "${COMMIT_BRANCH}" == 'master' ]; then
    if [ ! -z $KEYSTORE_FILE_URL ]; then
        IS_POST_MERGE=1
    fi
fi

if [ $IS_POST_MERGE -eq 1 ]; then
    echo "[POST MERGE] Downloading signature files"
    scripts/download_signing_files.sh
fi

echo "Doing commit checks..."
./gradlew clean testDebug checkDebug
./gradlew jacocoTestReport


if [ $IS_POST_MERGE -eq 1 ]; then
    echo "[POST MERGE] Building and deploying CANARY..."
    ./gradlew assembleCanary publishCanary
    echo "[POST MERGE] Building release and debug..."
    ./gradlew assembleDebug assembleRelease
fi