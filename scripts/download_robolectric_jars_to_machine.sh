#!/bin/bash
set -e

TARGET_FOLDER=".robolectric-android-all-jars/"
rm -rf "${TARGET_FOLDER}" || true
mkdir -p "${TARGET_FOLDER}"

DOWNLOAD_BASE_URL="https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented"
#artifacts taken from: https://github.com/robolectric/robolectric/blob/robolectric-4.6.1/robolectric/src/main/java/org/robolectric/plugins/DefaultSdkProvider.java
ARTIFACTS=( \
  "11-robolectric-6757853" \
  "10-robolectric-5803371" \
  "9-robolectric-4913185-2" \
  "8.1.0-robolectric-4611349" \
  "8.0.0_r4-robolectric-r1" \
  "7.1.0_r7-robolectric-r1" \
  "7.0.0_r1-robolectric-r1" \
  "6.0.1_r3-robolectric-r1" \
  "5.1.1_r9-robolectric-r2" \
  "5.0.2_r3-robolectric-r0" \
  "4.4_r1-robolectric-r2" \
  "4.3_r2-robolectric-r1" \
  "4.2.2_r1.2-robolectric-r1" \
  "4.1.2_r1-robolectric-r1" \
  )

for artifact in "${ARTIFACTS[@]}"
do
  echo "downloading ${artifact}..."
  jar_name="android-all-instrumented-${artifact}-i1.jar"
  wget --tries=5 --waitretry=5 --progress=dot:giga --output-document="${TARGET_FOLDER}/${jar_name}" "${DOWNLOAD_BASE_URL}/${artifact}-i1/${jar_name}"
done
