#!/usr/bin/env bash

./gradlew --no-daemon --stacktrace lintDebug checkDebug checkstyleMain findbugsMain
