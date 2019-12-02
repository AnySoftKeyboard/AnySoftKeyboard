#!/usr/bin/env bash
set -e

./gradlew ${EXTRA_GRADLE_ARGS} check
./gradlew ${EXTRA_GRADLE_ARGS} verifyReleaseResources
./gradlew ${EXTRA_GRADLE_ARGS} generateReleasePlayResources