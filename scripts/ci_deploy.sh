#!/usr/bin/env bash

KEYSTORE_FILE_URL=$1
PUBLISH_CERT_FILE_URL=$2

if [ -z "${KEYSTORE_FILE_URL}" ]; then
    echo "Could not find secure env variable KEYSTORE_FILE_URL. Can not deploy."
    exit 1
fi

if [ -z "${PUBLISH_CERT_FILE_URL}" ]; then
    echo "Could not find secure env variable PUBLISH_CERT_FILE_URL. Can not deploy."
    exit 1
fi

#checking last two commits, to see if they include DEPLOY directive
REQUEST_TO_DEPLOY_RELEASE=$(git log -2 --pretty=%s | grep -e "^DEPLOY-RELEASE")
REQUEST_TO_DEPLOY_CANARY=$(git log -2 --pretty=%s | grep -e "^DEPLOY-CANARY")
BUILD_TYPE=""
if [ -n "${REQUEST_TO_DEPLOY_RELEASE}" ]; then
    echo "BUILD_TYPE method RELEASE"
    BUILD_TYPE="assembleRelease publishRelease"
elif [ -n "${REQUEST_TO_DEPLOY_CANARY}" ]; then
    echo "Deploy method CANARY"
    BUILD_TYPE="assembleCanary publishCanary"
else
    echo "Deploy was not requested for this commit"
    exit 0
fi

echo "Downloading signature files..."
wget ${KEYSTORE_FILE_URL} -q -O /tmp/anysoftkeyboard.keystore
wget ${PUBLISH_CERT_FILE_URL} -q -O /tmp/apk_upload_key.p12
./gradlew --no-daemon ${BUILD_TYPE}
