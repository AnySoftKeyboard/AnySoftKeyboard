#!/usr/bin/env bash

./gradlew ${EXTRA_GRADLE_ARGS} clean
./gradlew ${EXTRA_GRADLE_ARGS} --no-build-cache bintrayUpload -PdryRun=false