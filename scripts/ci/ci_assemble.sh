#!/usr/bin/env bash

set -e

./gradlew --max-workers=4 --stacktrace assembleDebug
