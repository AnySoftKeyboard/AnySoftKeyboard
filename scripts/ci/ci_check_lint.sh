#!/usr/bin/env bash
set -e

# actual static-analysis
#Re-enable with AGP7
#./gradlew --stacktrace lintDebug lintRelease lintCanary --continue

#see https://github.com/actions/cache/issues/133
[[ -n "${GITHUB_ACTIONS}" ]] && chmod -R a+rwx .
