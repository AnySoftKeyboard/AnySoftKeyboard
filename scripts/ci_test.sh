#!/usr/bin/env bash

./gradlew lint checkDebug
./gradlew testDebugUnitTest :app:testDebugUnitTestCoverage
