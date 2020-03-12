#!/usr/bin/env bash
set -e
DEPLOYMET_ID="${1}"
shift
DEPLOYMENT_ENVIRONMENT="${1}"
shift
DEPLOYMENT_TASK="${1}"
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
API_USER="${1}"
shift
API_TOKEN="${1}"
shift

function deployProcessFromEnvironmentName() {
    #imeMaster_alpha_100
    [[ $1 =~ ([a-zA-Z]+)_.*_.* ]]
    echo "${BASH_REMATCH[1]}"
}

function deployChannelFromEnvironmentName() {
    #imeMaster_alpha_100
    [[ $1 =~ .*_([a-zA-Z]+)_.* ]]
    echo "${BASH_REMATCH[1]}"
}

function deployFractionFromEnvironmentName() {
    #imeMaster_alpha_100
    [[ $1 =~ .*_.*_([0-9]+) ]]
    local PERCENTAGE="${BASH_REMATCH[1]}"
    echo "$(echo "${PERCENTAGE}" | cut -c1-1).$(echo "${PERCENTAGE}" | cut -c2-3)"
}

PROCESS_NAME=$(deployProcessFromEnvironmentName "${DEPLOYMENT_ENVIRONMENT}")
DEPLOY_CHANNEL=$(deployChannelFromEnvironmentName "${DEPLOYMENT_ENVIRONMENT}")
FRACTION=$(deployFractionFromEnvironmentName "${DEPLOYMENT_ENVIRONMENT}")

echo "for ${DEPLOYMENT_ENVIRONMENT}: will deploy process ${PROCESS_NAME} to ${DEPLOY_CHANNEL} with ${FRACTION} fraction."
export BUILD_COUNT_FOR_VERSION=${GITHUB_RUN_NUMBER}

./gradlew --stacktrace :deployment:updateDeploymentState -PRequest.apiUsername="${API_USER}" -PRequest.apiUserToken="${API_TOKEN}" \
          -PrequestStatus.environment="${DEPLOYMENT_ENVIRONMENT}" -PrequestStatus.deployment_id="${DEPLOYMET_ID}" -PrequestStatus.deployment_state="in-progress"

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

DEPLOY_TASKS=( "-PwithAutoVersioning" ":generateFdroidYamls" "-DdeployChannel=${DEPLOY_CHANNEL}" "--user-fraction" "${FRACTION}" )
if [[ "${DEPLOYMENT_TASK}" == "deploy" ]]; then
  case "${PROCESS_NAME}" in

    imeMaster)
      DEPLOY_TASKS+=( "ime:app:assembleCanary" "ime:app:publishCanary" )
      ;;

    imeProduction)
      DEPLOY_TASKS+=( "ime:app:assembleRelease" "ime:app:publishRelease" )
      ;;

    addOns)
      DEPLOY_TASKS+=( "assembleRelease" "publishRelease" "-x" "ime:app:assembleRelease" "-x" "ime:app:publishRelease" )
      ;;

    *)
      echo "PROCESS_NAME '${PROCESS_NAME}' is unknown in task ${DEPLOYMENT_TASK}!"
      exit 1
      ;;

  esac
elif [[ "${DEPLOYMENT_TASK}" == "deploy:migration" ]]; then
  case "${PROCESS_NAME}" in

    imeMaster)
      DEPLOY_TASKS+=( "ime:app:promoteReleaseArtifact" )
      ;;

    imeProduction)
      DEPLOY_TASKS+=( "ime:app:promoteReleaseArtifact" )
      ;;

    addOns)
      DEPLOY_TASKS+=( "promoteReleaseArtifact" "-x" "ime:app:promoteReleaseArtifact" )
      ;;

  esac
fi

echo "Counter is ${BUILD_COUNT_FOR_VERSION}, crash email: ${ANYSOFTKEYBOARD_CRASH_REPORT_EMAIL}, and tasks: ${DEPLOY_TASKS[*]}"

./gradlew "${DEPLOY_TASKS[@]}"

./gradlew --stacktrace :deployment:updateDeploymentState -PRequest.apiUsername="${API_USER}" -PRequest.apiUserToken="${API_TOKEN}" \
          -PrequestStatus.environment="${DEPLOYMENT_ENVIRONMENT}" -PrequestStatus.deployment_id="${DEPLOYMET_ID}" -PrequestStatus.deployment_state="success"

## TODO: kill previous enabled environments

[[ -n "${GITHUB_ACTIONS}" ]] && chmod -R a+rwx .
