#!/usr/bin/env bash

KEYSTORE_FILE_URL=$1
PUBLISH_CERT_FILE_URL=$2
USERNAME=$3
BUILD_TYPE=$4

if [ "${BUILD_TYPE}" == "canary" ]; then
    echo "Deploy build-type CANARY from master."
    BUILD_TYPE="-DdeployChannel=alpha assembleCanary publishCanary"
elif [ "${BUILD_TYPE}" == "release" ]; then
    echo "Deploy build-type RELEASE from 'release-branch'."
    BUILD_TYPE="-DdeployChannel=beta assembleRelease publishRelease"
else
    echo "Invalid build type. Can not deploy."
    exit 1
fi

if [ "${USERNAME}" == "AnySoftKeyboard" ]; then
    echo "Repo owner is allowed for deploy."
else
    echo "Invalid repo owner. Can not deploy."
    exit 1
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
./gradlew --stacktrace ${BUILD_TYPE}
