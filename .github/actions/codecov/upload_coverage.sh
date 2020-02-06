#!/usr/bin/env bash
set -e

CODECOV_TOKEN="${1}"

wget --tries=5 --waitretry=5 --progress=dot:mega --output-document=codecov.sh https://codecov.io/bash
chmod +x codecov.sh

readarray -d '' COV_FILES < <(find . -name "test*UnitTestCoverage.xml" -print0)

rm -rf /tmp/coverage || true
mkdir /tmp/coverage

for cov_file in "${COV_FILES[@]}"
do
  echo "file: ${cov_file}"
  cp "${cov_file}" "/tmp/coverage/${RANDOM}-${RANDOM}.xml"
done

./scripts/retry.sh 5 ./codecov.sh -t "${CODECOV_TOKEN}" -y .github/actions/codecov/codecov.yml -X search -X gcov -X coveragepy -X xcode -f '/tmp/coverage/*.xml'
