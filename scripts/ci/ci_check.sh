#!/usr/bin/env bash
set -e

# Ensure we can perform git operations
git config --global --add safe.directory "$PWD"

git clean -f -d
git reset --hard HEAD

./gradlew generatePacksMarkDown || {
    echo "Some addons metadata is missing."
    exit 1
}

# ensures we can configure all tasks
./gradlew --stacktrace tasks

# actual static-analysis
./gradlew --stacktrace checkstyleMain --continue
