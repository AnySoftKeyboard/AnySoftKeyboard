#!/usr/bin/env bash

GRADLE_ARGS=""
if [ "${CI}" == "true" ]; then
    GRADLE_ARGS="--no-daemon --stacktrace --max-workers=2 -DmaxTestForks=${TEST_FORKS}"
fi

./gradlew ${GRADLE_ARGS} testDebugUnitTest testDebugUnitTestCoverage
