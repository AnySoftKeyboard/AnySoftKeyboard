#!/usr/bin/env bash
set -e

DUPLICATE_ID=$(find . -type f -name "*dictionaries.xml" -o -name "*keyboards.xml" -exec grep -Eo "\\sid=\"([A-Za-z0-9\\-]+)\"" {} \; | sort | uniq -d)

if [[ -n "${DUPLICATE_ID}" ]]; then
  echo "Found duplicate add-ons ID:"
  echo "${DUPLICATE_ID}"
  exit 1
fi

./gradlew verifyGoogleJavaFormat || {
    echo "code is not formatted."
    echo "run './gradlew googleJavaFormat' to fix."
    exit 1
}
./gradlew --stacktrace lintDebug checkstyleMain --continue
#see https://github.com/actions/cache/issues/133
[[ -n "${GITHUB_ACTIONS}" ]] && chmod -R a+rwx .
