#!/bin/bash

DEFAULT_API=23
BUILD_TOOLS="24.0.0"

function print_help_and_exit() {
    echo "./start_emulator.sh [options]"
    echo "Options:"
    echo "--api=[api level]   : starts an x86 emulator of a specific API level. Default value is ${DEFAULT_API}."
    echo "--tablet            : starts an x86 emulator with a Nexus 9 configutation. Default is Nexus 5."
    echo "--update            : Updates your local SDK. Default is not to update."
    echo "--headless          : Runs the emulator in a headless (CI) mode. Default is with UI (non-headless)."
    exit 0
}

API=$DEFAULT_API
IS_TABLET=0
UPDATE_SDK=0
HEADLESS=0

for i in "$@"
do
case $i in
    --api=*)
    API="${i#*=}"
    ;;
    --tablet)
    IS_TABLET=1
    ;;
    --help|-h)
    print_help_and_exit
    ;;
    --update_sdk)
    UPDATE_SDK=1
    ;;
    --headless)
    HEADLESS=1
    ;;
    *)
    # unknown option
    echo "unknown argument ${i}"
    print_help_and_exit
    ;;
esac
done

REQUIRED_ADD_ON="addon-google_apis-google-${API}"
REQUIRED_SYS_IMG="sys-img-x86-addon-google_apis-google-${API}"

TARGET_NAME="Google Inc.:Google APIs:${API}"
TARGET_ID_NUMNER_TEXT=$(android list targets | grep "${TARGET_NAME}" | egrep -o "id: [0-9]+")
if [ -z "${TARGET_ID_NUMNER_TEXT}" ]; then
    echo "Could not find System image for API ${API}. Forcing SDK update."
    UPDATE_SDK=1
fi

if [ ${UPDATE_SDK} -eq 1 ]; then
    echo "Fetching required SDK packages for emulator with API ${API} and is-tablet=${IS_TABLET}"

    android update sdk --all --no-ui --filter platform-tools,build-tools-${BUILD_TOOLS},extra-intel-Hardware_Accelerated_Execution_Manager,android-${DEFAULT_API},addon-google_apis-google-${DEFAULT_API},${REQUIRED_ADD_ON},extra-android-support,extra-android-m2repository,extra-google-m2repository,extra-google-google_play_services,${REQUIRED_SYS_IMG}
fi

TARGET_ID_NUMNER_TEXT=$(android list targets | grep "${TARGET_NAME}" | egrep -o "id: [0-9]+")
TARGET_ID_NUMNER=${TARGET_ID_NUMNER_TEXT:4}

if [ ${IS_TABLET} -eq 1 ]; then
    DEV_WIDTH="2048"
    DEV_HEIGHT="1536"
    DPI="320"
    ORIENTATION="landscape"
    AVD_NAME="Tablet_${API}"
else
    DEV_WIDTH="1080"
    DEV_HEIGHT="1900"
    ORIENTATION="portrait"
    DPI="420"
    AVD_NAME="Phone_${API}"
fi

#checking if we have this emulator already created
if [ -z "$(android list avd -c | grep ${AVD_NAME})" ]; then
    echo "Creating emulator with target-id ${TARGET_ID_NUMNER}, API ${API} and is-tablet=${IS_TABLET}"
    android create avd --name "${AVD_NAME}" --target ${TARGET_ID_NUMNER} -c 200M -s ${DEV_WIDTH}x${DEV_HEIGHT} --tag google_apis --abi x86
    #tweaking
    echo "hw.gpu.enabled=yes
hw.gpu.mode=auto
hw.keyboard=yes
hw.lcd.density=420
hw.mainKeys=no
hw.ramSize=1536
hw.sdCard=yes
hw.sensors.orientation=yes
hw.sensors.proximity=yes
hw.lcd.width=${DEV_WIDTH}
hw.lcd.height=${DEV_HEIGHT}
hw.trackBall=no
runtime.network.latency=none
runtime.network.speed=full
runtime.scalefactor=auto
vm.heapSize=512" >> "${HOME}/.android/avd/${AVD_NAME}.avd/config.ini"
fi

echo "Starting emulator with API ${API} and is-tablet=${IS_TABLET}..."
HEADLESS_ARGS=""
if [ ${HEADLESS} -eq 1 ]; then
    echo "Headless mode!"
    #-noaudio removed till emulator bug fixed (does not work on macOS)
    HEADLESS_ARGS="-no-skin -no-window"
fi
#>/dev/null 2>&1 &
emulator -avd ${AVD_NAME} ${HEADLESS_ARGS} 
