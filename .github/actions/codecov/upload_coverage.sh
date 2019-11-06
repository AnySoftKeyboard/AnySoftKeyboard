#!/usr/bin/env bash

./scripts/retry.sh 5 curl  --fail https://codecov.io/bash -o codecov.sh
chmod +x codecov.sh
COV_FILES=$(find . -name "test*UnitTestCoverage.xml" | xargs -n 1 echo -n " -f ")
./scripts/retry.sh 5 ./codecov.sh -X gcov -X coveragepy -X xcode ${COV_FILES}
