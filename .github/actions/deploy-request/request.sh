#!/usr/bin/env bash
set -e

REF_TO_DEPLOY="${1}"
API_USERNAME="${2}"
API_TOKEN="${3}"
OUTPUT="${4}"

TEMP_FOLDER="/tmp/${OUTPUT}"
rm -rf "${TEMP_FOLDER}" || true
mkdir -p "${TEMP_FOLDER}"

function deployment_request() {
  echo "making request to: ${1}"
  local JSON_TEXT
  JSON_TEXT=$( jq -n \
                    --arg jsonRef "${REF_TO_DEPLOY}" \
                    --arg jsonDeployTarget "${1}" \
                    --arg jsonDescription "${2}" \
                    '{ ref: $jsonRef, task: "deploy", auto_merge: false, environment: $jsonDeployTarget, description: $jsonDescription, required_contexts=[ "push-ready" ] }' )

  local JSON_FILENAME="${TEMP_FOLDER}/deployment_request_${1}.json"
  echo "${JSON_TEXT}" > "${JSON_FILENAME}"

  curl -u "${API_USERNAME}:${API_TOKEN}" -o "${TEMP_FOLDER}/deployment_response_${1}.json" -d "@${JSON_FILENAME}" https://api.github.com/repos/AnySoftKeyboard/AnySoftKeyboard/deployments
}

#some deploy logic
if [[ "${REF_TO_DEPLOY}" == "refs/heads/master" ]]; then
  deployment_request "app_alpha" "Deployment request by ${API_USERNAME}"
  deployment_request "addons_alpha" "Deployment request by ${API_USERNAME}"
elif [[ "${REF_TO_DEPLOY}" == "release-branch-v"* ]]; then
  deployment_request "app_beta" "Deployment request by ${API_USERNAME}"
fi

rm -rf "${OUTPUT}" || true
mkdir -p "${OUTPUT}"

mv "${TEMP_FOLDER}" "${OUTPUT}"
