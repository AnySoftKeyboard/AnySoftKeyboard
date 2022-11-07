#!/usr/bin/env bash
set -e

export TEST_GROUPS_COUNT=${1}
export TEST_GROUP_INDEX=${2}
MODULE=${3}
EXTRA_ARGS=${4}

echo "Will run tests for module '${MODULE}' with extra args '${EXTRA_ARGS}' for group-index ${TEST_GROUP_INDEX} out of ${TEST_GROUPS_COUNT} groups:"

./scripts/download_robolectric_jars_to_machine.sh

#extra args needs to come before the coverage task so "--tests" will be passed to the test tasks
# we automatically re-try on gradle crash
./scripts/retry-on-SIGSEGV.sh 3 ./gradlew "${MODULE}testDebugUnitTest" ${EXTRA_ARGS} "${MODULE}testDebugUnitTestCoverage"
