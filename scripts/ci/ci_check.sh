#!/usr/bin/env bash
set -e

./gradlew verifyGoogleJavaFormat
./gradlew --stacktrace lintDebug checkstyleMain pmdMain pmdTest
./gradlew --stacktrace verifyReleaseResources
./gradlew --stacktrace generateReleasePlayResources
