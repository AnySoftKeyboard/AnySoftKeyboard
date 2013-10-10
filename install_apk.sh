#!/bin/bash
# This script will install a specific build type on all connected devices.
if [$1 == '']; then
  echo "Please specify build type to install (release, debug or beta) as an argument. For example, to install release apk type '$0 release'"
  exit
fi
 
for SERIAL in $(adb devices | grep -v List | cut -f 1);
do 
  APK="build/apk/AnySoftKeyboard-$1.apk"
  echo "Installing $APK to device $SERIAL..."
  `adb -s $SERIAL install -r $APK`
done
