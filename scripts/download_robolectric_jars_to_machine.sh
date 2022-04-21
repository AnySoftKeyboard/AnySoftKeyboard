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
  wget --tries=5 --waitretry=5 --progress=dot:giga --output-document="${TARGET_FOLDER}/${jar_name}" "${DOWNLOAD_BASE_URL}/${artifact}-i${PREINSTRUMENTED_VERSION}/${jar_name}"
done
