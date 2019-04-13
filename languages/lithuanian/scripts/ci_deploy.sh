#!/usr/bin/env bash

KEYSTORE_FILE_URL=$1
PUBLISH_CERT_FILE_URL=$2
USERNAME=$3
BRANCH=$4
#note: PULL_REQUEST_URL may be empty, so make sure it is last
PULL_REQUEST_URL=$5

if [ "${USERNAME}" == "AnySoftKeyboard" ]; then
    echo "Repo owner is allowed for deploy."
else
    echo "Invalid repo owner '${USERNAME}'. Can not deploy."
    exit 0
fi

if [ -z "${KEYSTORE_FILE_URL}" ]; then
    echo "Could not find secure env variable KEYSTORE_FILE_URL. Can not deploy."
    exit 1
fi

if [ -z "${PUBLISH_CERT_FILE_URL}" ]; then
    echo "Could not find secure env variable PUBLISH_CERT_FILE_URL. Can not deploy."
    exit 1
fi

if [ -z "${PULL_REQUEST_URL}" ]; then
    echo "This is not a pull request. We should deploy."
else
    echo "This is a pull request. We should not deploy."
    exit 0
fi

#master SHOULD BE REPLACED WITH THE LANGUAGE BRANCH
if [ "${BRANCH}" == "master" ]; then
    echo "Building in the language branch. We should deploy."
else
    echo "Building in '${BRANCH}'. We should not deploy."
    exit 0
fi

echo "Downloading signature files..."
wget ${KEYSTORE_FILE_URL} -q -O /tmp/language_pack.keystore
wget ${PUBLISH_CERT_FILE_URL} -q -O /tmp/apk_upload_key.p12
./gradlew --no-daemon --stacktrace assembleRelease publishRelease
