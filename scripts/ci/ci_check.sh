#!/usr/bin/env bash
set -e

./gradlew verifyGoogleJavaFormat || {
    echo "code is not formatted."
    echo "run './gradlew googleJavaFormat' to fix."
    exit 1
}
./gradlew --stacktrace lintDebug checkstyleMain pmdMain pmdTest
./gradlew --stacktrace verifyReleaseResources
./gradlew --stacktrace generateReleasePlayResources
