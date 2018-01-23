#!/usr/bin/env bash

./gradlew --stacktrace lintDebug
./gradlew --stacktrace checkstyleMain
./gradlew --stacktrace checkDebug
#Not running findbugs in CI, since it takes too much memory and ends the process with exit-code 137
if [ "${CI}" == "" ]; then
    ./gradlew findbugsMain
fi
