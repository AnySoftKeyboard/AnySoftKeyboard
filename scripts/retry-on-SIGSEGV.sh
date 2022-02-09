#!/usr/bin/env bash

retries=$1
shift

echo "Will retry '$*' for ${retries} times:"

function needsRetry() {
  if [[ -d "build/build-logging" ]]; then
    local contents
    contents=$(cat "$(ls -t build/build-logging/*.log | head -1)")

    [[ "$contents" =~ .*"finished with non-zero exit value 134".* ]] && echo "RETRY"
  else
    echo "RETRY-NO-LOGGING-FOLDER"
  fi
}

set +e
count=0
"$@"
EXIT_CODE=$?
while needsRetry; do
  count=$((count + 1))
  if [[ ${count} -lt ${retries} ]]; then
    echo "************** Retry ${count}/${retries} exited ${EXIT_CODE}, retrying in ${count} seconds..."
    ./gradlew --stop
    sleep ${count}
    "$@"
    EXIT_CODE=$?
    echo "EXIT_CODE is ${EXIT_CODE}"
  else
    echo "Retry ${count}/${retries} exited ${EXIT_CODE}, no more retries left."
    exit ${EXIT_CODE}
  fi
done

exit ${EXIT_CODE}
