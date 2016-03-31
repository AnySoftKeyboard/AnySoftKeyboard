#!/usr/bin/env bash

COMMIT_BRANCH=$1

if [ -z ${COMMIT_BRANCH} ]; then
    echo "You must supply branch name as first argument!"
    exit 1
fi

IS_POST_MERGE=0
if [ "${COMMIT_BRANCH}" == 'master' ]; then
    if [ ! -z ${KEYSTORE_FILE_URL} ]; then
        IS_POST_MERGE=1
    fi
fi

REQUEST_TO_DEPLOY_RELEASE=$(git log -1 --pretty=%s | grep -e "^DEPLOY-RELEASE")
REQUEST_TO_DEPLOY_DISABLE=$(git log -1 --pretty=%s | grep -e "^DEPLOY-DISABLE")
DEPLOY_METHOD=1
if [ ! -z ${REQUEST_TO_DEPLOY_RELEASE} ]; then
    DEPLOY_METHOD=2
elif [ ! -z ${REQUEST_TO_DEPLOY_DISABLE} ]; then
    DEPLOY_METHOD=0
fi

if [ ${IS_POST_MERGE} -eq 1 ]; then
    echo "[POST MERGE] Downloading signature files"
    scripts/download_signing_files.sh
fi

echo "Doing commit checks..."
./gradlew clean testDebug checkDebug
./gradlew jacocoTestReport


if [ ${IS_POST_MERGE} -eq 1 ]; then
    if [ ${DEPLOY_METHOD} -eq 0 ]; then
        echo "[POST MERGE] deploy was disable for this commit"
    elif [ ${DEPLOY_METHOD} -eq 1 ]; then
        echo "[POST MERGE] Building and deploying CANARY (to beta channel)..."
        ./gradlew assembleCanary publishCanary
    elif [ ${DEPLOY_METHOD} -eq 1 ]; then
        echo "[POST MERGE] Building and deploying RELEASE (to beta channel)..."
        ./gradlew assembleRelease publishRelease
    else
        echo "[POST MERGE] unknown DEPLOY_METHOD '${DEPLOY_METHOD}'! Not deploying."
    fi
fi