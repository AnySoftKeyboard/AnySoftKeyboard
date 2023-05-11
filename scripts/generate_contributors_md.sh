#!/bin/bash
set -e

TEMP_CONT_MD_FILE="$(mktemp)"

echo "# Contributors" > "$TEMP_CONT_MD_FILE"
echo "" >> "$TEMP_CONT_MD_FILE"
echo "Thank you for the fine contributors:" >> "$TEMP_CONT_MD_FILE"
echo "" >> "$TEMP_CONT_MD_FILE"
git config --global --add safe.directory "${PWD}"
./gradlew :generateContributorsFile -PRequest.apiUsername="$1" -PRequest.apiUserToken="$2" -PRequest.sha="$(git rev-parse HEAD)" -PRequest.maxContributors=200

cat build/generateContributorsFile_contributors.md >> "$TEMP_CONT_MD_FILE"

cp "$TEMP_CONT_MD_FILE" CONTRIBUTORS.md