#!/usr/bin/env bash
set -e

LATEST_VERSION="$1"

source scripts/ci/third-party-update/version_grep_regex.sh

sed "s/robolectricVersion[[:space:]]=[[:space:]]'${GREP_VERSION_CLASSES}'/robolectricVersion = '${LATEST_VERSION}'/g" \
  gradle/root_all_projects_ext.gradle > /tmp/output.file
cp /tmp/output.file gradle/root_all_projects_ext.gradle
cat gradle/root_all_projects_ext.gradle
curl --silent "https://raw.githubusercontent.com/robolectric/robolectric/robolectric-${LATEST_VERSION}/robolectric/src/main/java/org/robolectric/plugins/DefaultSdkProvider.java" \
  | grep -oP '\s+int\s+PREINSTRUMENTED_VERSION\s+=\s+\K\d+(?=;)' \
  > scripts/robolectric_jars_versions.txt
curl --silent "https://raw.githubusercontent.com/robolectric/robolectric/robolectric-${LATEST_VERSION}/robolectric/src/main/java/org/robolectric/plugins/DefaultSdkProvider.java" \
  | grep -o "[[:space:]]\\{1,\\}knownSdks.put([[:graph:]]\\{1,\\},[[:space:]]new[[:space:]]DefaultSdk([[:graph:]]\\{1,\\}[,[:space:]]*[\"][[:digit:]]\\{1,\\}[.[:digit:]]*[[:graph:]]*[\"][,[:space:]]*[\"][[:graph:]]\\{1,\\}[\"]" \
  | grep -oP '"\K[r_\d\.-]+(?=")' \
  >> scripts/robolectric_jars_versions.txt
