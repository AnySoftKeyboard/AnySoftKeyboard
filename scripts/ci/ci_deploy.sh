#!/usr/bin/env bash
set -e

RELEASE_BUILD=$1
KEYSTORE_FILE_URL=$2
PUBLISH_CERT_FILE_URL=$3

if [[ "${RELEASE_BUILD}" == "false" ]]; then
    echo "Deploy build-type CANARY from master."
    GRADLE_TASKS="-DdeployChannel=alpha assembleCanary publishCanary"
elif [[ "${RELEASE_BUILD}" == "true" ]]; then
    echo "Deploy build-type RELEASE from 'release-branch'."
    cp app/src/main/play/release-notes/en-US/alpha.txt app/src/main/play/release-notes/en-US/beta.txt
    GRADLE_TASKS="-DdeployChannel=beta assembleRelease publishRelease"
elif [[ "${RELEASE_BUILD}" == "dry-run" ]]; then
    echo "Dry Run Deploy build-type RELEASE+CANARY."
    GRADLE_TASKS="-DdeployChannel=alpha assembleRelease assembleCanary"
else
    echo "Invalid build type '${RELEASE_BUILD}'. Can not deploy."
    exit 1
fi

if [[ "${RELEASE_BUILD}" != "dry-run" ]]; then
    echo "Downloading signature files..."

    if [[ -z "${KEYSTORE_FILE_URL}" ]]; then
        echo "Could not find secure env variable KEYSTORE_FILE_URL. Can not deploy."
        exit 1
    fi

    if [[ -z "${PUBLISH_CERT_FILE_URL}" ]]; then
        echo "Could not find secure env variable PUBLISH_CERT_FILE_URL. Can not deploy."
        exit 1
    fi

    wget ${KEYSTORE_FILE_URL} -q -O /tmp/anysoftkeyboard.keystore
    wget ${PUBLISH_CERT_FILE_URL} -q -O /tmp/apk_upload_key.p12
fi

./gradlew --stacktrace -PwithAutoVersioning ${GRADLE_TASKS} generateFdroidYamls
