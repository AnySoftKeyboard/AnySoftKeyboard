#!/usr/bin/env bash
set -e

./gradlew --stacktrace testDebugUnitTest testDebugUnitTestCoverage -PexcludeTestClasses="**/*AllSdkTest*"
