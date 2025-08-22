#!/usr/bin/env bash
set -e

DEPLOYMENT_ENVIRONMENT="${1}"
shift
PREVIOUS_DEPLOYMENT_ENVIRONMENT="${1}"
shift
DEPLOYMENT_TASK="${1}"
shift
export ANYSOFTKEYBOARD_CRASH_REPORT_EMAIL="${1}"
shift
SECRETS_REPO_FOLDER="${1}"
shift
export KEY_STORE_FILE_PASSWORD="${1}"
shift
export KEY_STORE_FILE_DEFAULT_ALIAS_PASSWORD="${1}"
shift

# Setup the android env for ASK
./scripts/ci/ci_setup.sh
mkdir -p outputs/apk || true
mkdir -p outputs/bundle || true
mkdir -p outputs/fdroid || true
mkdir -p ime/app/build/outputs/mapping || true

function deployProcessFromEnvironmentName() {
    #imeMain_alpha_100
    [[ $1 =~ ([a-zA-Z]+)_.*_.* ]]
    echo "${BASH_REMATCH[1]}"
}

function deployChannelFromEnvironmentName() {
    #imeMain_alpha_100
    [[ $1 =~ .*_([a-zA-Z]+)_.* ]]
    echo "${BASH_REMATCH[1]}"
}

function deployFractionFromEnvironmentName() {
    #imeMain_alpha_100
    [[ $1 =~ .*_.*_([0-9]+) ]]
    local PERCENTAGE="${BASH_REMATCH[1]}"
    echo "$(echo "${PERCENTAGE}" | cut -c1-1).$(echo "${PERCENTAGE}" | cut -c2-3)"
}

PROCESS_NAME=$(deployProcessFromEnvironmentName "${DEPLOYMENT_ENVIRONMENT}")
DEPLOY_CHANNEL=$(deployChannelFromEnvironmentName "${DEPLOYMENT_ENVIRONMENT}")
FRACTION=$(deployFractionFromEnvironmentName "${DEPLOYMENT_ENVIRONMENT}")

echo "for ${DEPLOYMENT_ENVIRONMENT}: will deploy process ${PROCESS_NAME} to ${DEPLOY_CHANNEL} with ${FRACTION} fraction."
export BUILD_COUNT_FOR_VERSION=${GITHUB_RUN_NUMBER}

echo "Copying secret files..."
cp "${SECRETS_REPO_FOLDER}/anysoftkeyboard.keystore" /tmp/anysoftkeyboard.keystore
cp "${SECRETS_REPO_FOLDER}/playstore-publisher-certs.json" /tmp/apk_upload_key.json

echo "Preparing change log files..."
for f in $(find . -name 'alpha.txt'); do
  cp $f "$(dirname $f)/beta.txt"
  cp $f "$(dirname $f)/production.txt"
done

echo "Generating store assets..."
./gradlew generateStoreLogoIcon

DEPLOY_ARGS=()
DEPLOY_TASKS=( "--rerun-tasks" "--continue" "--stacktrace" "-PwithAutoVersioning" "-PonlyPublishSupporting=${DEPLOY_CHANNEL}" )
if [[ "${FRACTION}" == "1.00" ]]; then
  DEPLOY_ARGS+=("--release-status" "completed")
else
  DEPLOY_ARGS+=("--release-status" "inProgress" "--user-fraction" "${FRACTION}")
fi

# we will call assemble, bundle and publish to ensure:
# 1) we have the APK file
# 2) we have the aab file
# 3) we have uploaded (published) the AAB file to Play Store

if [[ "${DEPLOYMENT_TASK}" == "deploy" ]]; then
  case "${PROCESS_NAME}" in

    imeMain)
      DEPLOY_TASKS+=( "ime:app:assembleCanary" "ime:app:bundleCanary" "ime:app:publishCanaryBundle" )
      DEPLOY_ARGS+=( "--track" "${DEPLOY_CHANNEL}" )
      ;;

    imeProduction)
      DEPLOY_ARGS+=( "--track" "${DEPLOY_CHANNEL}" )
      DEPLOY_TASKS+=( "ime:app:assembleRelease" "ime:app:bundleRelease" "ime:app:publishReleaseBundle" )
      ;;

    addOns*)
      DEPLOY_ARGS+=( "--track" "${DEPLOY_CHANNEL}" )
      DEPLOY_TASKS+=( "assembleRelease" "bundleRelease" "publishReleaseBundle" "-x" "ime:app:assembleRelease" "-x" "ime:app:bundleRelease" "-x" "ime:app:publishReleaseBundle" )
      ;;

    *)
      echo "PROCESS_NAME '${PROCESS_NAME}' is unknown in task ${DEPLOYMENT_TASK}!"
      exit 1
      ;;

  esac
elif [[ "${DEPLOYMENT_TASK}" == "deploy:migration" ]]; then
  PREVIOUS_DEPLOY_CHANNEL=$(deployChannelFromEnvironmentName "${PREVIOUS_DEPLOYMENT_ENVIRONMENT}")
  case "${PROCESS_NAME}" in

    ime*)
      DEPLOY_ARGS+=( "--from-track" "${PREVIOUS_DEPLOY_CHANNEL}" "--promote-track" "${DEPLOY_CHANNEL}" )
      DEPLOY_TASKS+=( "ime:app:promoteReleaseArtifact" )
      ;;

    addOns*)
      DEPLOY_ARGS+=( "--from-track" "${PREVIOUS_DEPLOY_CHANNEL}" "--promote-track" "${DEPLOY_CHANNEL}" )
      DEPLOY_TASKS+=( "promoteReleaseArtifact" "-x" "ime:app:promoteReleaseArtifact" )
      ;;

  esac
fi

echo "Counter is ${BUILD_COUNT_FOR_VERSION}, crash email: ${ANYSOFTKEYBOARD_CRASH_REPORT_EMAIL}, and tasks: ${DEPLOY_TASKS[*]}, and DEPLOY_ARGS: ${DEPLOY_ARGS[*]}"

./gradlew "${DEPLOY_TASKS[@]}" "${DEPLOY_ARGS[@]}" --continue



[[ -n "${GITHUB_ACTIONS}" ]] && chmod -R a+rwx .
