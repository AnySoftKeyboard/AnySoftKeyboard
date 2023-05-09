#!/usr/bin/env bash
set -e

LATEST_VERSION="$1"

echo "Bumping Gradle to version '${LATEST_VERSION}'."
./gradlew wrapper --gradle-version="${LATEST_VERSION}"
./gradlew wrapper --gradle-version="${LATEST_VERSION}"
