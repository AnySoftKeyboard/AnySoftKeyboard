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

REQUEST_TO_DEPLOY_RELEASE=$(git log -2 --pretty=%s | grep -e "^DEPLOY-RELEASE")
BUILD_TYPE=""
if [ -n "${REQUEST_TO_DEPLOY_RELEASE}" ]; then
    echo "BUILD_TYPE method RELEASE"
    BUILD_TYPE="assembleRelease publishRelease -PDisableRibbon"
else
    echo "Deploy method CANARY"
    #adding INTERNET note to changelogs
    echo '* INTERNET permissions for BETA builds. Required for crash tracking.' | cat - app/src/main/play/en-US/whatsnew > temp && mv temp app/src/main/play/en-US/whatsnew
    BUILD_TYPE="assembleCanary publishCanary"
fi

echo "Downloading signature files..."
wget ${KEYSTORE_FILE_URL} -q -O /tmp/anysoftkeyboard.keystore
wget ${PUBLISH_CERT_FILE_URL} -q -O /tmp/apk_upload_key.p12
./gradlew --no-daemon --stacktrace ${BUILD_TYPE}
