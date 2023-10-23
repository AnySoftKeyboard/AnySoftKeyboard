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
  -F "files[strings.xml]=@ime/app/src/main/res/values/strings.xml" \
  -F "files[remote_strings.xml]=@ime/remote/src/main/res/values/strings.xml" \
  -F "files[addons_strings.xml]=@ime/addons/src/main/res/values/strings.xml" \
  -F "files[chewbacca_strings.xml]=@ime/chewbacca/src/main/res/values/strings.xml" \
  -F "files[release_info_strings.xml]=@ime/releaseinfo/src/main/res/values/strings.xml" \
  -F "files[permissions_strings.xml]=@ime/permissions/src/main/res/values/strings.xml" \
  -F "files[pixel_strings.xml]=@ime/pixel/src/main/res/values/strings.xml" \
  "https://api.crowdin.com/api/project/anysoftkeyboard/update-file?key=${CROWDIN_API}${UPDATE_OPTION}"
