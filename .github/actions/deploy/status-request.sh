#!/usr/bin/env bash
set -e

ID="${1}"
ENVIRONMENT="${2}"
STATE="${3}"
API_USERNAME="${4}"
API_TOKEN="${5}"

echo "making request to: ${ID} @ ${ENVIRONMENT} to state ${STATE}"
if [[ "${STATE}" == "success" ]]; then
  JSON_TEXT=$( jq -n \
                    --arg jsonEnvironment "${ENVIRONMENT}" \
                    --arg jsonState "${STATE}" \
                    '{ environment: $jsonEnvironment, state: $jsonState, auto_inactive: true }' )
else
  JSON_TEXT=$( jq -n \
                    --arg jsonEnvironment "${ENVIRONMENT}" \
                    --arg jsonState "${STATE}" \
                    '{ environment: $jsonEnvironment, state: $jsonState }' )
fi

JSON_FILENAME="${OUTPUT}/deployment_request.json"
echo "${JSON_TEXT}" > "${JSON_FILENAME}"
cat "${JSON_FILENAME}"
set +e
curl --fail \
  -u "${API_USERNAME}:${API_TOKEN}" \
  -o "${OUTPUT}/deployment_response.json" \
  -d "@${JSON_FILENAME}" \
  -H "Accept: application/vnd.github.flash-preview+json" \
  -H "Accept: application/vnd.github.ant-man-preview+json" \
  "https://api.github.com/repos/AnySoftKeyboard/AnySoftKeyboard/deployments/${ID}/statuses"
curl_exit_code=$?
set -e

echo "response with exit-code ${curl_exit_code}:"
cat "${OUTPUT}/deployment_response.json"
if [[ ${curl_exit_code} -ne 0 ]]; then
  exit ${curl_exit_code}
fi
