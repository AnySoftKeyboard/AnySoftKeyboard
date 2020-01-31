LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := anysoftkey_jni
LOCAL_LDFLAGS := -Wl,--build-id
LOCAL_SRC_FILES := \
	src/main/jni/source/com_anysoftkeyboard_dictionaries_BinaryDictionary.cpp \
	src/main/jni/source/dictionary.cpp \

LOCAL_C_INCLUDES += src/main/jni/include/

include $(BUILD_SHARED_LIBRARY)
