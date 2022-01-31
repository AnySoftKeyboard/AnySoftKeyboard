#!/usr/bin/env bash

./gradlew assembleDebug 2>&1 | tee build/error-prone-build.log
cat build/error-prone-build.log | sed -rn 's/.+\.java:[0-9]+:\s\w+:\s\[(\w+)\]\s.+/\1/p' > build/error-prone-errors.txt
EP_ERRORS_ARRAY=( $(cat build/error-prone-errors.txt) )
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
  ./gradlew assembleDebug -PErrorProneAutoPatchList=${EP_ERRORS_LIST}
  ./gradlew googleJavaFormat
else
  echo "* No issues were found with Error-Prone."
fi