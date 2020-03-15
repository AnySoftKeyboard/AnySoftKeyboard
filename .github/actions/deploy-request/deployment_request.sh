#!/usr/bin/env bash
set -e

SHA="${1}"
shift
REF="${1}"
shift
NEW_DEPLOY="${1}"
shift
API_USERNAME="${1}"
shift
API_TOKEN="${1}"
shift

echo "Request deployment flow for sha ${SHA} on branch ${REF}. New deployment: ${NEW_DEPLOY}."
./gradlew :deployment:deploymentRequestProcess -PRequest.sha="${SHA}" -PRequest.ref="${REF}" -PRequest.new_deploy="${NEW_DEPLOY}" -PRequest.apiUsername="${API_USERNAME}" -PRequest.apiUserToken="${API_TOKEN}"

[[ -n "${GITHUB_ACTIONS}" ]] && chmod -R a+rwx .
