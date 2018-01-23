#!/usr/bin/env bash

GRADLE_ARGS=""
if [ "${CI}" == "true" ]; then
    GRADLE_ARGS="-DmaxTestForks=${TEST_FORKS}"
fi

./gradlew --stacktrace ${GRADLE_ARGS} testDebugUnitTest testDebugUnitTestCoverage
