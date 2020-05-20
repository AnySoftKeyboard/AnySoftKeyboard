#!/usr/bin/env bash
set -e
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
PUBLISH_JSON_URL="${1}"
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

echo "Downloading secret files..."
wget --tries=5 --waitretry=5 "${KEYSTORE_FILE_URL}" -q -O /tmp/anysoftkeyboard.keystore
stat /tmp/anysoftkeyboard.keystore
wget --tries=5 --waitretry=5 "${PUBLISH_JSON_URL}" -q -O /tmp/apk_upload_key.json
stat /tmp/apk_upload_key.json

DEPLOY_TASKS=( "--stacktrace" "-PwithAutoVersioning" ":generateFdroidYamls" "-DdeployChannel=${DEPLOY_CHANNEL}" "-DdeployFraction=${FRACTION}" )
if [[ "${DEPLOYMENT_TASK}" == "deploy" ]]; then
  case "${PROCESS_NAME}" in

    imeMaster)
      DEPLOY_TASKS+=( "ime:app:assembleCanary" "ime:app:publishCanary" )
      ;;

    imeProduction)
      DEPLOY_TASKS+=( "ime:app:assembleRelease" "ime:app:publishRelease" )
      ;;

    addOns*)
      DEPLOY_TASKS+=( "assembleRelease" "publishRelease" "-x" "ime:app:assembleRelease" "-x" "ime:app:publishRelease" )
      ;;

    *)
      echo "PROCESS_NAME '${PROCESS_NAME}' is unknown in task ${DEPLOYMENT_TASK}!"
      exit 1
      ;;

  esac
elif [[ "${DEPLOYMENT_TASK}" == "deploy:migration" ]]; then
  case "${PROCESS_NAME}" in

    ime*)
      DEPLOY_TASKS+=( "ime:app:promoteReleaseArtifact" )
      ;;

    addOns*)
      DEPLOY_TASKS+=( "promoteReleaseArtifact" "-x" "ime:app:promoteReleaseArtifact" )
      ;;

  esac
fi

echo "Counter is ${BUILD_COUNT_FOR_VERSION}, crash email: ${ANYSOFTKEYBOARD_CRASH_REPORT_EMAIL}, and tasks: ${DEPLOY_TASKS[*]}"

./gradlew "${DEPLOY_TASKS[@]}"

#Making sure no future deployments will happen on this branch.
if [[ "${FRACTION}" == "1.00" ]] && [[ "${DEPLOY_CHANNEL}" == "production" ]]; then
  echo "A succesfull full deploy to production has finished."
  MARKER_FILE="deployment/halt_deployment_marker"
  if [[ -f "${MARKER_FILE}" ]]; then
    echo "${MARKER_FILE} exits. No need to create another."
  else
    echo "Full deployment to production '${DEPLOYMENT_ENVIRONMENT}' was done succesfully" > "${MARKER_FILE}"
    git config --global user.email "ask@evendanan.net"
    git config --global user.name "Polyglot"
    git add "${MARKER_FILE})"
    git commit -m "Halting deploy to ${DEPLOYMENT_ENVIRONMENT}"
    #cleaning repo
    git clean -f -d && git reset --hard HEAD
  fi
fi

[[ -n "${GITHUB_ACTIONS}" ]] && chmod -R a+rwx .
