#!/usr/bin/env bash

KEYSTORE_FILE_URL=$1
PUBLISH_CERT_FILE_URL=$2
USERNAME=$3
BRANCH=$4

BUILD_TYPE=""
if [ "${BRANCH}" == "master" ]; then
    echo "Deploy build-type CANARY from master."
    #adding INTERNET note to change-logs
    echo '* INTERNET permission for BETA builds. Required for crash tracking.' | cat - app/src/main/play/en-US/whatsnew > temp && mv temp app/src/main/play/en-US/whatsnew
    BUILD_TYPE="assembleCanary publishCanary"
elif [ "${BRANCH}" == "release-branch" ]; then
    echo "Deploy build-type RELEASE from 'release-branch'."
    BUILD_TYPE="assembleRelease publishRelease"
else
    echo "Invalid branch. Can not deploy."
    exit 0
fi

if [ "${USERNAME}" == "AnySoftKeyboard" ]; then
    echo "Repo owner is allowed for deploy."
else
    echo "Invalid repo owner. Can not deploy."
    exit 0
fi

# from this point, we fail with error when stuff missing, since we want to deploy.

if [ -z "${KEYSTORE_FILE_URL}" ]; then
    echo "Could not find secure env variable KEYSTORE_FILE_URL. Can not deploy."
    exit 1
fi

if [ -z "${PUBLISH_CERT_FILE_URL}" ]; then
    echo "Could not find secure env variable PUBLISH_CERT_FILE_URL. Can not deploy."
    exit 1
fi

echo "Downloading signature files..."
wget ${KEYSTORE_FILE_URL} -q -O /tmp/anysoftkeyboard.keystore
wget ${PUBLISH_CERT_FILE_URL} -q -O /tmp/apk_upload_key.p12
./gradlew --no-daemon --stacktrace ${BUILD_TYPE}
