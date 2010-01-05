LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_C_INCLUDES += $(LOCAL_PATH)/src

LOCAL_SRC_FILES := \
	com_menny_android_anysoftkeyboard_Dictionary_BinaryDictionary.cpp \
	dictionary.cpp

LOCAL_MODULE := nativeime

include $(BUILD_SHARED_LIBRARY)