#!/usr/bin/env bash

retries=$1
shift
IFS=',' read -r -a retry_exit_codes_array <<< "$1"
retry_exit_codes=" ${retry_exit_codes_array[*]} "
shift

echo "Will retry '$*' for ${retries} when exit-codes are '${retry_exit_codes}':"
set +e
count=0
EXIT_CODE=0
pwd
"$@"
EXIT_CODE=$?
echo "EXIT_CODE is ${EXIT_CODE}"
while [[ "${retry_exit_codes}" =~ .*" ${EXIT_CODE} ".* ]]; do
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
  fi
done

exit ${EXIT_CODE}