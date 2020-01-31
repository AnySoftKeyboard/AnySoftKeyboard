#!/usr/bin/env bash
set -e

export TEST_GROUPS_COUNT=${1}
export TEST_GROUP_INDEX=${2}
MODULE=${3}
EXTRA_ARGS=${4}

echo "Will run tests for module '${MODULE}' with extra args '${EXTRA_ARGS}' for group-index ${TEST_GROUP_INDEX} out of ${TEST_GROUPS_COUNT} groups:"

./scripts/download_robolectric_jars_to_machine.sh

./gradlew "${MODULE}testDebugUnitTest" "${MODULE}testDebugUnitTestCoverage" ${EXTRA_ARGS}

#see https://github.com/actions/cache/issues/133
[[ -n "${GITHUB_ACTIONS}" ]] && chmod -R a+rwx .
