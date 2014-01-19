
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := anysoftkey_jni

LOCAL_SRC_FILES := \
        com_anysoftkeyboard_dictionaries_BinaryDictionary.cpp \
        dictionary.cpp

include $(BUILD_STATIC_LIBRARY)
