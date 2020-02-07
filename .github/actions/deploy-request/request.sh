#!/usr/bin/env bash
set -e

REF_TO_DEPLOY="${1}"
#we are using exact SHA to deploy, and not branc (which can move)
SHA_TO_DEPLOY="${2}"
API_USERNAME="${3}"
API_TOKEN="${4}"
OUTPUT="${5}"

rm -rf "${OUTPUT}" || true
mkdir -p "${OUTPUT}"

function deployment_request() {
  echo "making request to: ${1}"
  local JSON_TEXT
  JSON_TEXT=$( jq -n \
                    --arg jsonRef "${SHA_TO_DEPLOY}" \
                    --arg jsonDeployTarget "${1}" \
                    --arg jsonDescription "${2}" \
                    '{ ref: $jsonRef, task: "deploy", auto_merge: false, environment: $jsonDeployTarget, description: $jsonDescription, required_contexts: [ "push-ready" ] }' )

  local JSON_FILENAME="${OUTPUT}/deployment_request_${1}.json"
  echo "${JSON_TEXT}" > "${JSON_FILENAME}"
  cat "${JSON_FILENAME}"
  set +e
  curl --fail -u "${API_USERNAME}:${API_TOKEN}" -o "${OUTPUT}/deployment_response_${1}.json" -d "@${JSON_FILENAME}" https://api.github.com/repos/AnySoftKeyboard/AnySoftKeyboard/deployments
  local curl_exit_code=$?
  set -e
  echo "response with exit-code ${curl_exit_code}:"
  cat "${OUTPUT}/deployment_response_${1}.json"
  if [[ ${curl_exit_code} -ne 0 ]]; then
    exit ${curl_exit_code}
  fi
}

#some deploy logic
if [[ "${REF_TO_DEPLOY}" == "refs/heads/master" ]]; then
  deployment_request "app_alpha" "Deployment request by ${API_USERNAME}"
  deployment_request "addons_alpha" "Deployment request by ${API_USERNAME}"
elif [[ "${REF_TO_DEPLOY}" == "release-branch-v"* ]]; then
  deployment_request "app_beta" "Deployment request by ${API_USERNAME}"
fi
