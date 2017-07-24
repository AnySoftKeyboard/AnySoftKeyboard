#!/bin/bash

if [ -z "${CROWDIN_API}" ]; then
    echo "Could not find crowdin API environment variable at CROWDIN_API."
    exit 1
fi

curl \
  -F "files[strings.xml]=@app/src/main/res/values/strings.xml" \
  https://api.crowdin.com/api/project/anysoftkeyboard/update-file?key=${CROWDIN_API}&update_option=update_as_unapproved
