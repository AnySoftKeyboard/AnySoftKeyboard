#!/usr/bin/env bash

GRADLE_ARGS=""
GRADLE_EXTRA_CHECKS="findbugsMain"
if [ "${CI}" == "true" ]; then
    GRADLE_ARGS="--no-daemon --stacktrace --max-workers=2"
    GRADLE_EXTRA_CHECKS=""
fi

./gradlew ${GRADLE_ARGS} lintDebug ${GRADLE_EXTRA_CHECKS}
./gradlew ${GRADLE_ARGS} checkstyleMain ${GRADLE_EXTRA_CHECKS}
./gradlew ${GRADLE_ARGS} checkDebug ${GRADLE_EXTRA_CHECKS}
