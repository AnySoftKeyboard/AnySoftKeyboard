#!/usr/bin/env bash

COMMIT_BRANCH=$1
KEYSTORE_FILE_URL=$2
PUBLISH_CERT_FILE_URL=$3

if [ -z "${COMMIT_BRANCH}" ]; then
    echo "[POST MERGE] You must supply branch name as first argument!"
    exit 1
fi

IS_POST_MERGE=0
if [ "${COMMIT_BRANCH}" == "master" ]; then
    echo "[POST MERGE] in 'master' branch"
    if [ -n "${KEYSTORE_FILE_URL}" ]; then
        echo "[POST MERGE] found secure env variables. This is a valid post-merge run."
        IS_POST_MERGE=1
    fi
fi

if [ ${IS_POST_MERGE} -eq 0 ]; then
    echo "[POST MERGE] this is not a post-merge build. Exiting."
    exit 0
fi

REQUEST_TO_DEPLOY_RELEASE=$(git log -1 --pretty=%s | grep -e "^DEPLOY-RELEASE")
REQUEST_TO_DEPLOY_DISABLE=$(git log -1 --pretty=%s | grep -e "^DEPLOY-DISABLE")
DEPLOY_METHOD=1
if [ -n "${REQUEST_TO_DEPLOY_RELEASE}" ]; then
    echo "[POST MERGE] Deploy method RELEASE"
    DEPLOY_METHOD=2
elif [ -n "${REQUEST_TO_DEPLOY_DISABLE}" ]; then
    echo "[POST MERGE] Deploy method DISABLE"
    DEPLOY_METHOD=0
fi

if [ ${DEPLOY_METHOD} -eq 0 ]; then
    echo "[POST MERGE] deploy was disable for this commit"
else
    echo "[POST MERGE] Downloading signature files..."
    wget ${KEYSTORE_FILE_URL} -q -O /tmp/anysoftkeyboard.keystore
    wget ${PUBLISH_CERT_FILE_URL} -q -O /tmp/apk_upload_key.p12
    if [ ${DEPLOY_METHOD} -eq 1 ]; then
        echo "[POST MERGE] Building and deploying CANARY (to beta channel)..."
        ./gradlew assembleCanary publishCanary
    elif [ ${DEPLOY_METHOD} -eq 2 ]; then
        echo "[POST MERGE] Building and deploying RELEASE (to beta channel)..."
        ./gradlew assembleRelease publishRelease
        echo "[POST MERGE] ProGuard mapping:"
        echo "[POST MERGE] *******START****"
        cat build/outputs/mapping/release/mapping.txt
        echo "[POST MERGE] *******END******"
    else
        echo "[POST MERGE] unknown DEPLOY_METHOD '${DEPLOY_METHOD}'! Not deploying."
    fi
fi