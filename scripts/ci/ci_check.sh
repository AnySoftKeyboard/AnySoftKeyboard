#!/usr/bin/env bash

GRADLE_ARGS=""
GRADLE_EXTRA_CHECKS="findbugsMain"
if [ "${CI}" == "true" ]; then
    GRADLE_ARGS="--no-daemon --stacktrace --max-workers=2"
    GRADLE_EXTRA_CHECKS=""
fi

./gradlew ${GRADLE_ARGS} lintDebug checkDebug checkstyleMain ${GRADLE_EXTRA_CHECKS}
