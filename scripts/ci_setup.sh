#!/usr/bin/env bash

#accepting licenses - creating a folder to store the license CRC
mkdir -p ${ANDROID_HOME}/licenses || true
rm ${ANDROID_HOME}/licenses/* || true
#this value was taken from my local machine, after I accepted it locally.
echo -e "8933bad161af4178b1185d1a37fbf41ea5269c55\nd56f5187479451eabf01fb78af6dfcb131a6481e\c" > ${ANDROID_HOME}/licenses/android-sdk-license
echo -e "79120722343a6f314e0719f863036c702b0e6b2a\n84831b9409646a918e30573bab4c9c91346d8abd\c" > ${ANDROID_HOME}/licenses/android-sdk-preview-license
echo -e "d975f751698a77b662f1254ddbeed3901e976f5a\c" > ${ANDROID_HOME}/licenses/intel-android-extra-license
echo -e "8403addf88ab4874007e1c1e80a0025bf2550a37\c" > ${ANDROID_HOME}/licenses/intel-android-sysimage-license


#setting up SDK paths
rm local.properties || true
echo -e "sdk.dir=${ANDROID_HOME}" > local.properties

#workaround for plugin error https://code.google.com/p/android/issues/detail?id=212309
./gradlew ${EXTRA_GRADLE_ARGS} dependencies || true
