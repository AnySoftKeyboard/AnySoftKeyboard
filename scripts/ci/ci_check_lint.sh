#!/usr/bin/env bash
set -e

# actual static-analysis
#./gradlew --no-daemon --stacktrace lintDebug lintRelease lintCanary --continue

#see https://github.com/actions/cache/issues/133
[[ -n "${GITHUB_ACTIONS}" ]] && chmod -R a+rwx .
