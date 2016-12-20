#!/usr/bin/env bash

#cd $(dirname ${0})/..
./gradlew lint checkDebug
./gradlew testDebugUnitTest :app:testDebugUnitTestCoverage
