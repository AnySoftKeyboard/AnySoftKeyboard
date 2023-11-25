#!/usr/bin/env bash
set -e

source scripts/ci/third-party-update/version_grep_regex.sh

TESTS=0
PASSED=0

function testRegex {
  local text="$1"
  local expected="$2"
  local actual
  actual=$(echo "$text" | grep -o "${GREP_VERSION_CLASSES}")
  TESTS=$((TESTS+1))
  if [[ "${actual}" == "${expected}" ]]; then
    echo "'${text}' passed."
    PASSED=$((PASSED+1))
  else
    echo "'${text}' failed! expected '${expected}' but was '${actual}'"
  fi
}

echo "Testing grep regex '${GREP_VERSION_CLASSES}':"
testRegex "1.2.3" "1.2.3"
testRegex "1.12" "1.12"
testRegex "11.2" "11.2"
testRegex "1.2.32" "1.2.32"
testRegex "1.22.2" "1.22.2"
testRegex "11.2.4" "11.2.4"
testRegex "13.12.2" "13.12.2"
testRegex "1.2-rc3" "1.2-rc3"
testRegex "1.2.1-rc3" "1.2.1-rc3"
testRegex "1.2.1-beta" "1.2.1-beta"
testRegex "1.2-beta" "1.2-beta"
testRegex " 1.2.32 " "1.2.32"
testRegex ":1.22.2" "1.22.2"
testRegex "11.2.4 " "11.2.4"
testRegex "v13.12.2" "13.12.2"

echo "${PASSED} tests passed out of ${TESTS}."
if [[ "$PASSED" != "$TESTS" ]]; then
  exit 1
fi
