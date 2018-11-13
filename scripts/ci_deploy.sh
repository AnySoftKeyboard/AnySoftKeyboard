#!/usr/bin/env bash

KEYSTORE_FILE_URL=$1
PUBLISH_CERT_FILE_URL=$2

if [[ -z "${KEYSTORE_FILE_URL}" ]]; then
    echo "Could not find secure env variable KEYSTORE_FILE_URL. Can not deploy."
    exit 1
fi

if [[ -z "${PUBLISH_CERT_FILE_URL}" ]]; then
    echo "Could not find secure env variable PUBLISH_CERT_FILE_URL. Can not deploy."
    exit 1
fi

echo "Downloading signature files..."
wget ${KEYSTORE_FILE_URL} -q -O /tmp/language_pack.keystore
wget ${PUBLISH_CERT_FILE_URL} -q -O /tmp/apk_upload_key.p12

echo "NOT DEPLOYING ATM!"
exit 0
./gradlew --no-daemon --stacktrace assembleRelease publishRelease
