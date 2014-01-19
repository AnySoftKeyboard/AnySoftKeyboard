LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := anysoftkey2_jni

LOCAL_SRC_FILES := \
	com_anysoftkeyboard_dictionaries_ResourceBinaryDictionary.cpp \
	dictionary.cpp \
	char_utils.cpp

include $(BUILD_STATIC_LIBRARY)

