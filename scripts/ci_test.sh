#!/usr/bin/env bash

./gradlew --no-daemon --stacktrace testDebugUnitTest :app:testDebugUnitTestCoverage -PDisableRibbon
