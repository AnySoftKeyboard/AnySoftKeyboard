#!/usr/bin/env bash
set -e

DEPLOY_TARGET="${1}"
shift
export ANYSOFTKEYBOARD_CRASH_REPORT_EMAIL="${1}"
shift
KEYSTORE_FILE_URL="${1}"
shift
export KEY_STORE_FILE_PASSWORD="${1}"
shift
export KEY_STORE_FILE_DEFAULT_ALIAS_PASSWORD="${1}"
shift
PUBLISH_CERT_FILE_URL="${1}"
shift
export PUBLISH_APK_SERVICE_ACCOUNT_EMAIL="${1}"
shift

export BUILD_COUNT_FOR_VERSION=${GITHUB_RUN_NUMBER}

if [[ -z "${KEYSTORE_FILE_URL}" ]]; then
    echo "Using debug keystore for signing."
    mkdir -p /root/.android/ || true
    cp ./.github/actions/deploy/debug.keystore /root/.android/ || exit 1
fi

DEPLOY_TASKS=( "-PwithAutoVersioning" ":generateFdroidYamls" )
case "${DEPLOY_TARGET}" in
  dry-run)
    DEPLOY_TASKS+=( "-DdeployChannel=alpha" "assembleRelease" "assembleCanary" "verifyReleaseResources" "generateReleasePlayResources" "generateCanaryPlayResources" )
    ;;

  app_alpha)
    DEPLOY_TASKS+=( "-DdeployChannel=alpha" "ime:app:assembleCanary" "ime:app:publishCanary" )
    ;;

  app_beta)
    DEPLOY_TASKS+=( "-DdeployChannel=beta" "ime:app:assembleRelease" "ime:app:publishRelease")
    ;;

  addons_alpha)
    DEPLOY_TASKS+=( "-DdeployChannel=alpha" "assembleRelease" "publishRelease" "-x" "ime:app:assembleRelease" "-x" "ime:app:publishRelease" )
    ;;

  *)
    echo "deploy-target '${DEPLOY_TARGET}' is unkown!"
    exit 1
    ;;
esac

echo "Counter is ${BUILD_COUNT_FOR_VERSION}, DEPLOY_TARGET: ${DEPLOY_TARGET}, crash email: ${ANYSOFTKEYBOARD_CRASH_REPORT_EMAIL}, and tasks: ${DEPLOY_TASKS[*]}"

if [[ "${DEPLOY_TASKS[*]}" == *"publish"* ]]; then
    echo "Downloading signature files..."

    if [[ -z "${KEYSTORE_FILE_URL}" ]]; then
        echo "Could not find secure env variable KEYSTORE_FILE_URL. Can not deploy."
        exit 1
    fi

    if [[ -z "${PUBLISH_CERT_FILE_URL}" ]]; then
        echo "Could not find secure env variable PUBLISH_CERT_FILE_URL. Can not deploy."
        exit 1
    fi

    wget --tries=5 --waitretry=5 "${KEYSTORE_FILE_URL}" -q -O /tmp/anysoftkeyboard.keystore
    stat /tmp/anysoftkeyboard.keystore
    wget --tries=5 --waitretry=5 "${PUBLISH_CERT_FILE_URL}" -q -O /tmp/apk_upload_key.p12
    stat /tmp/apk_upload_key.p12
fi

# shellcheck disable=SC2086
./gradlew "${DEPLOY_TASKS[@]}"

[[ -n "${GITHUB_ACTIONS}" ]] && chmod -R a+rwx .
