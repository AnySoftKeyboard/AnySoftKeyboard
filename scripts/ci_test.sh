#!/usr/bin/env bash

./gradlew --no-daemon lint checkDebug
./gradlew --no-daemon testDebugUnitTest :app:testDebugUnitTestCoverage
