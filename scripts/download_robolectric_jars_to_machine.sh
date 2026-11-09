#!/bin/bash
set -e

TARGET_FOLDER=".robolectric-android-all-jars"
rm -rf "${TARGET_FOLDER}" || true
mkdir -p "${TARGET_FOLDER}"

DOWNLOAD_BASE_URL="https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented"
#artifacts taken from: https://github.com/robolectric/robolectric/blob/robolectric-VERSION/robolectric/src/main/java/org/robolectric/plugins/DefaultSdkProvider.java
mapfile -t VERSIONS_ARRAY < scripts/robolectric_jars_versions.txt
PREINSTRUMENTED_VERSION="${VERSIONS_ARRAY[0]}"
ARTIFACTS=()
for (( idx=1; idx<${#VERSIONS_ARRAY[@]} ; idx=idx+2 )) ; do
  ARTIFACT="${VERSIONS_ARRAY[${idx}]}-robolectric-${VERSIONS_ARRAY[${idx}+1]}"
  ARTIFACTS+=( "$ARTIFACT" )
done

for artifact in "${ARTIFACTS[@]}"
do
  echo "downloading ${artifact}..."
  jar_name="android-all-instrumented-${artifact}-i${PREINSTRUMENTED_VERSION}.jar"
  # Maven Central may rate-limit (HTTP 429), which wget does not retry by default.
  wget --tries=8 --waitretry=30 --retry-on-http-error=429,500,502,503,504 --progress=dot:giga --output-document="${TARGET_FOLDER}/${jar_name}" "${DOWNLOAD_BASE_URL}/${artifact}-i${PREINSTRUMENTED_VERSION}/${jar_name}"
done
