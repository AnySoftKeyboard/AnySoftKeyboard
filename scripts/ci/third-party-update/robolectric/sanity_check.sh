#!/usr/bin/env bash
set -e

./scripts/download_robolectric_jars_to_machine.sh
./gradlew :api:testDebugUnitTest :ime:nextword:testDebugUnitTest
