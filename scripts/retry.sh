#!/usr/bin/env bash

#Taken from https://gist.github.com/sj26/88e1c6584397bb7c13bd11108a579746

retries=$1
shift

count=0
until "$@"; do
    exit=$?
    count=$(($count + 1))
    if [[ ${count} -lt ${retries} ]]; then
      echo "Retry ${count}/${retries} exited ${exit}, retrying in ${count} seconds..."
      sleep ${count}
    else
      echo "Retry ${count}/${retries} exited ${exit}, no more retries left."
      exit ${exit}
    fi
done
