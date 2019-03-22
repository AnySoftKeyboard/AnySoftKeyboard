#!/usr/bin/env bash

./gradlew --stacktrace lintDebug checkstyleMain pmdMain pmdTest
./gradlew --stacktrace verifyReleaseResources
./gradlew --stacktrace generateReleasePlayResources
