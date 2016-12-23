#!/usr/bin/env bash

./gradlew --no-daemon --stacktrace lint checkDebug
./gradlew --no-daemon --stacktrace testDebugUnitTest :app:testDebugUnitTestCoverage
