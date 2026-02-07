#!/usr/bin/env bash
set -e

ROBOLECTRIC_VERSION="$1"

if [ -z "$ROBOLECTRIC_VERSION" ]; then
  echo "Usage: $0 <robolectric-version>"
  echo "Example: $0 4.14.1"
  exit 1
fi

echo "Updating robolectric_jars_versions.txt for Robolectric version ${ROBOLECTRIC_VERSION}..."

# Fetch the PREINSTRUMENTED_VERSION from Robolectric's source
curl --silent "https://raw.githubusercontent.com/robolectric/robolectric/robolectric-${ROBOLECTRIC_VERSION}/robolectric/src/main/java/org/robolectric/plugins/DefaultSdkProvider.java" \
  | perl -nle 'print $1 if /\s+int\s+PREINSTRUMENTED_VERSION\s+=\s+(\d+);/' \
  > scripts/robolectric_jars_versions.txt

# Fetch the Android SDK versions and their corresponding artifact revisions
curl --silent "https://raw.githubusercontent.com/robolectric/robolectric/robolectric-${ROBOLECTRIC_VERSION}/robolectric/src/main/java/org/robolectric/plugins/DefaultSdkProvider.java" \
  | grep -o "[[:space:]]\\{1,\\}knownSdks.put([[:graph:]]\\{1,\\},[[:space:]]new[[:space:]]DefaultSdk([[:graph:]]\\{1,\\}[,[:space:]]*[\"][[:digit:]]\\{1,\\}[.[:digit:]]*[[:graph:]]*[\"][,[:space:]]*[\"][[:graph:]]\\{1,\\}[\"]" \
  | perl -nle 'print $1 while /"([r_\d\.-]+)"/g' \
  >> scripts/robolectric_jars_versions.txt

echo "✓ Successfully updated scripts/robolectric_jars_versions.txt"
echo "Contents:"
cat scripts/robolectric_jars_versions.txt
