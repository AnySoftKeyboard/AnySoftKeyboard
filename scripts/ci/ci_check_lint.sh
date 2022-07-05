#!/usr/bin/env bash
set -e

# actual static-analysis - broken to variants to have a more stable lint process
# running on the app module because: https://developer.android.com/studio/releases/gradle-plugin#improved_lint_for_library_dependencies
./gradlew --stacktrace :ime:app:lintDebug --continue
./gradlew --stacktrace :ime:app:lintCanary --continue
./gradlew --stacktrace :ime:app:lintRelease --continue
./gradlew --stacktrace :ime:app:lintAllAddOns --continue
