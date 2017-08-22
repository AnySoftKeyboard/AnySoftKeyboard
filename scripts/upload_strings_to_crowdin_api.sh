#!/bin/bash

if [ -z "${CROWDIN_API}" ]; then
    echo "Could not find crowdin API environment variable at CROWDIN_API."
    exit 1
fi

UPDATE_OPTION=""
if [ "$1" == "keep" ]; then
    echo "Updating string and keeping previous translations..."
    UPDATE_OPTION="&update_option=update_as_unapproved"
elif [ "$1" == "remove" ]; then
    echo "Updating string and removing previous translations..."
    UPDATE_OPTION=""
else
  echo "Please specify update option ('keep', 'remove') for translations"
  exit 1
fi

curl \
  -F "files[strings.xml]=@app/src/main/res/values/strings.xml" \
  https://api.crowdin.com/api/project/anysoftkeyboard/update-file?key=${CROWDIN_API}${UPDATE_OPTION}
