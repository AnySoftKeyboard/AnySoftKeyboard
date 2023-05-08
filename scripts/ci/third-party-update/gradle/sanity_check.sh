#!/usr/bin/env bash
set -e

./gradlew :api:assembleDebug :api:lintDebug
./gradlew :api:assembleDebug :api:lintDebug
