#!/usr/bin/env bash
set -e

# actual static-analysis - broken to variants to have a more stable lint process
./gradlew --stacktrace lintDebug --continue
./gradlew --stacktrace lintCanary --continue
./gradlew --stacktrace lintRelease --continue

#see https://github.com/actions/cache/issues/133
[[ -n "${GITHUB_ACTIONS}" ]] && chmod -R a+rwx .
