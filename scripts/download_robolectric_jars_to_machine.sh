#!/bin/bash
set -e

TARGET_FOLDER=".robolectric-android-all-jars"
rm -rf "${TARGET_FOLDER}" || true
mkdir -p "${TARGET_FOLDER}"

DOWNLOAD_BASE_URL="https://maven-central.storage-download.googleapis.com/maven2/org/robolectric/android-all-instrumented"
FALLBACK_DOWNLOAD_BASE_URL="https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented"
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
  artifact_path="${artifact}-i${PREINSTRUMENTED_VERSION}/${jar_name}"
  # Prefer Google's Maven Central mirror to avoid Maven Central HTTP 429 rate limits; fall back to repo1.maven.org if needed.
  if ! wget --tries=4 --waitretry=15 --retry-on-http-error=429,500,502,503,504 --progress=dot:giga --output-document="${TARGET_FOLDER}/${jar_name}" "${DOWNLOAD_BASE_URL}/${artifact_path}"; then
    rm -f "${TARGET_FOLDER}/${jar_name}"
    echo "mirror download failed for ${jar_name}, falling back to repo1.maven.org..."
    wget --tries=8 --waitretry=30 --retry-on-http-error=429,500,502,503,504 --progress=dot:giga --output-document="${TARGET_FOLDER}/${jar_name}" "${FALLBACK_DOWNLOAD_BASE_URL}/${artifact_path}"
  fi
done
