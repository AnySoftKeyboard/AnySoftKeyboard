#!/usr/bin/env bash

# Ensuring all compile tasks will run
./gradlew clean

TEMP_DIR="$(mktemp -d)"
LOG_FILE="${TEMP_DIR}/error-prone-build.log"
ERRORS_LIST_FILE="${TEMP_DIR}/error-prone-errors.txt"

./gradlew --no-build-cache compileDebugUnitTestJavaWithJavac compileDebugJavaWithJavac --continue 2>&1 | tee ${LOG_FILE}
cat ${LOG_FILE} | sed -rn 's/.+\.java:[0-9]+:\s\w+:\s\[(\w+)\]\s.+/\1/p' > ${ERRORS_LIST_FILE}
EP_ERRORS_ARRAY=( $(cat ${ERRORS_LIST_FILE}) )
EP_ERRORS_LIST=$(IFS=, ; echo "${EP_ERRORS_ARRAY[*]}")

echo ""
echo "******************"
echo ""
if [[ ! -z "$EP_ERRORS_LIST" ]]; then
  set -e
  echo "*** Trying to auto-patch Error-Prone issue: ${EP_ERRORS_LIST}."
  echo ""
  echo "******************"
  echo ""
  ./gradlew --no-build-cache compileDebugUnitTestJavaWithJavac compileDebugJavaWithJavac -PErrorProneAutoPatchList=${EP_ERRORS_LIST} --continue
else
  echo "* No issues were found with Error-Prone."
fi