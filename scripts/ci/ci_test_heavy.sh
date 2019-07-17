#!/usr/bin/env bash
set -e

./gradlew --stacktrace -DmaxTestForks=1 -DTEST_FORK_EVERY=1 -Dorg.gradle.jvmargs=-Xmx7000M :app:testDebugUnitTest --tests="*AllSdkTest*" :app:testDebugUnitTestCoverage
