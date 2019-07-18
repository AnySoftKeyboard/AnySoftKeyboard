#!/usr/bin/env bash
set -e

./gradlew --stacktrace verifyGoogleJavaFormat
./gradlew --stacktrace lintDebug checkstyleMain pmdMain pmdTest
./gradlew --stacktrace verifyReleaseResources
./gradlew --stacktrace generateReleasePlayResources
