#!/usr/bin/env bash
set -e

## Assuming the following environment variables
# BUILD_TYPE
# KEYSTORE_FILE_URL
# PUBLISH_CERT_FILE_URL

if [[ "${BUILD_TYPE}" == "canary" ]]; then
    echo "Deploy build-type CANARY from master."
    GRADLE_TASKS="-DdeployChannel=alpha assembleCanary publishCanary"
elif [[ "${BUILD_TYPE}" == "release" ]]; then
    echo "Deploy build-type RELEASE from 'release-branch'."
    cp app/src/main/play/release-notes/en-US/alpha.txt app/src/main/play/release-notes/en-US/beta.txt
    GRADLE_TASKS="-DdeployChannel=beta assembleRelease publishRelease"
elif [[ "${BUILD_TYPE}" == "dry-run-release" ]]; then
    echo "Dry Run Deploy build-type RELEASE."
    GRADLE_TASKS="-DdeployChannel=alpha assembleRelease"
else
    echo "Invalid build type '${BUILD_TYPE}'. Can not deploy."
    exit 1
fi

# from this point, we fail with error when stuff missing, since we want to deploy.

if [[ -z "${KEYSTORE_FILE_URL}" ]]; then
    echo "Could not find secure env variable KEYSTORE_FILE_URL. Can not deploy."
    exit 1
fi

if [[ -z "${PUBLISH_CERT_FILE_URL}" ]]; then
    echo "Could not find secure env variable PUBLISH_CERT_FILE_URL. Can not deploy."
    exit 1
fi

echo "Downloading signature files..."
wget ${KEYSTORE_FILE_URL} -q -O /tmp/anysoftkeyboard.keystore
wget ${PUBLISH_CERT_FILE_URL} -q -O /tmp/apk_upload_key.p12
./gradlew --stacktrace -PwithAutoVersioning ${GRADLE_TASKS} generateFdroidYamls

cat outputs/fdroid.yaml