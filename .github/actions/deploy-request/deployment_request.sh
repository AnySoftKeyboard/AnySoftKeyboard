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

if [[ "${SHA}" == "HEAD" ]]; then
  echo "HEAD was specified as SHA. Taking from git:"
  git config --global --add safe.directory "${PWD}"
  SHA="$(git show-ref --head --hash "${REF}" | tail -n 1)"
  echo "HEAD SHA was found to be '${SHA}'."
fi

echo "Request deployment flow for sha ${SHA} on branch ${REF}. New deployment: ${NEW_DEPLOY}."
./gradlew --rerun-tasks :deployment:deploymentRequestProcess -PRequest.sha="${SHA}" -PRequest.ref="${REF}" -PRequest.new_deploy="${NEW_DEPLOY}" -PRequest.apiUsername="${API_USERNAME}" -PRequest.apiUserToken="${API_TOKEN}"

[[ -n "${GITHUB_ACTIONS}" ]] && chmod -R a+rwx .
