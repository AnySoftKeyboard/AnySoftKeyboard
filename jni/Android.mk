LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
	com_anysoftkeyboard_dictionaries_BinaryDictionary.cpp \
	dictionary.cpp

LOCAL_MODULE := anysoftkey_jni

include $(BUILD_SHARED_LIBRARY)
