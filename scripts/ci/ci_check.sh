#!/usr/bin/env bash
set -e

./gradlew verifyGoogleJavaFormat || {
    echo "code is not formatted."
    echo "run './gradlew googleJavaFormat' to fix."
    exit 1
}
./gradlew --stacktrace lintDebug checkstyleMain pmdMain pmdTest --continue
#see https://github.com/actions/cache/issues/133
[[ -n "${GITHUB_ACTIONS}" ]] && chmod -R a+rwx .
