#!/usr/bin/env bash
set -e

export TEST_GROUPS_COUNT=${1}
export TEST_GROUP_INDEX=${2}
MODULE=${3}
EXTRA_ARGS=${4}

echo "Will run tests for module '${MODULE}' with extra args '${EXTRA_ARGS}' for group-index ${TEST_GROUP_INDEX} out of ${TEST_GROUPS_COUNT} groups:"

./gradlew --stacktrace -Dorg.gradle.daemon=false ${MODULE}testDebugUnitTest ${MODULE}testDebugUnitTestCoverage ${EXTRA_ARGS}

#see https://github.com/actions/cache/issues/133
chmod -R a+rwx .
