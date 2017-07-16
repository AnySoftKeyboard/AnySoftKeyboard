#!/bin/bash

if [ -z "${CIRCLE_CI_API}" ]; then
    echo "Could not find CIRCLE_CI_API environment variable"
    exit 1
fi

curl "https://circleci.com/api/v1.1/project/github/AnySoftKeyboard/AnySoftKeyboard?circle-token=${CIRCLE_CI_API}" -o /tmp/recentbuilds.txt
echo "Recent builds:"
head -n 60 /tmp/recentbuilds.txt

read -p "Continue with triggerring release build? " -n 1 -r
echo    # (optional) move to a new line
if [[ $REPLY =~ ^[Yy]$ ]]
then
    echo "!!!!!!!!!!!!!!RELEASING!!!!!!!!!!"
    curl \
         -o /tmp/buildtrigger.txt \
        --header "Content-Type: application/json" \
        --data '{"build_parameters": {"ASK_RELEASE_VARIANT": "TRUE"}}' \
        --request POST \
        "https://circleci.com/api/v1.1/project/github/AnySoftKeyboard/AnySoftKeyboard/tree/master?circle-token=$CIRCLE_CI_API"
    echo "**Result:"
    cat /tmp/buildtrigger.txt
    echo "**End of result."
else
    echo "Okay, no release."
fi