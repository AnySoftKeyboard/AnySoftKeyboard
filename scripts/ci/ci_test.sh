#!/usr/bin/env bash

./gradlew --no-daemon --stacktrace testDebugUnitTest :app:testDebugUnitTestCoverage -DmaxTestForks=${TEST_FORKS}

