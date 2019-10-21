#!/usr/bin/env bash
set -e

./gradlew --stacktrace -DmaxTestForks=1 --max-workers=3 testDebugUnitTest testDebugUnitTestCoverage -PexcludeTestClasses="**/*AllSdkTest*"
