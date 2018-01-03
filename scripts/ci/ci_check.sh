#!/usr/bin/env bash

./gradlew --no-daemon --stacktrace --max-workers=3 lintDebug checkDebug checkstyleMain findbugsMain
