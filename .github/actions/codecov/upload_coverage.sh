curl https://codecov.io/bash -o codecov.sh
chmod +x codecov.sh
./codecov.sh -X gcov -X coveragepy -X xcode `find . -name "test*UnitTestCoverage.xml" | xargs -n 1 echo -n " -f "`
