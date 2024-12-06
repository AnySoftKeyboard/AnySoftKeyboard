#!/usr/bin/env bash
set -e

# Ensure we can perform git operations
git config --global --add safe.directory "$PWD"

pnpm build && node dist/checkers/index.js --root_dir "$PWD"

git clean -f -d
git reset --hard HEAD

./gradlew spotlessCheck || {
    echo "code is not formatted."
    echo "run './gradlew spotlessApply' to fix."
    exit 1
}

./gradlew generatePacksMarkDown || {
    echo "Some addons metadata is missing."
    exit 1
}

# ensures we can configure all tasks
./gradlew --stacktrace tasks

# actual static-analysis
./gradlew --stacktrace checkstyleMain --continue
