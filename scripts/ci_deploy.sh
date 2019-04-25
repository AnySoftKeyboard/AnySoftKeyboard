#!/usr/bin/env bash
set -e

KEYSTORE_FILE_URL=$1
PUBLISH_CERT_FILE_URL=$2

GIT_MESSAGE=`git log -1`
if [[ "${GIT_MESSAGE}" == *"NO-DEPLOY"* ]]; then
    echo "Not deploying, since git message contains 'NO-DEPLOY'"
    exit 0
fi

if [[ -z "${KEYSTORE_FILE_URL}" ]]; then
    echo "Could not find secure env variable KEYSTORE_FILE_URL. Can not deploy."
    exit 1
fi

if [[ -z "${PUBLISH_CERT_FILE_URL}" ]]; then
    echo "Could not find secure env variable PUBLISH_CERT_FILE_URL. Can not deploy."
    exit 1
fi

wget ${KEYSTORE_FILE_URL} -q -O /tmp/add_on_pack.keystore
wget ${PUBLISH_CERT_FILE_URL} -q -O /tmp/apk_upload_key.p12

echo "**** Uploading to Play Store..."
./gradlew ${EXTRA_GRADLE_ARGS} clean
./gradlew ${EXTRA_GRADLE_ARGS} --no-build-cache assembleRelease publishRelease


