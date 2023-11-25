#!/usr/bin/env bash
set -e

# Ensure we can perform git operations
git config --global --add safe.directory "$PWD"

DUPLICATE_ID=$(find . -type f -path '*/build/*' -prune -o -name "*dictionaries.xml" -o -name "*keyboards.xml" -exec grep -Eo "\\sid=\"([A-Za-z0-9\\-]+)\"" {} \; | sort | uniq -d)

if [[ -n "${DUPLICATE_ID}" ]]; then
  echo "Found duplicate add-ons ID:"
  echo "${DUPLICATE_ID}"
  exit 1
fi

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
