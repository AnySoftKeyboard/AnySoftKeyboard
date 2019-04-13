#!/usr/bin/env bash

./gradlew ${EXTRA_GRADLE_ARGS} check
./gradlew ${EXTRA_GRADLE_ARGS} verifyReleaseResources
./gradlew ${EXTRA_GRADLE_ARGS} generateReleasePlayResources