LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := anysoftkey2_jni
LOCAL_LDFLAGS := -Wl,--build-id
LOCAL_LDLIBS := -llog
LOCAL_SRC_FILES := \
	src/main/jni/source/char_utils.cpp \
	src/main/jni/source/com_anysoftkeyboard_dictionaries_ResourceBinaryDictionary.cpp \
	src/main/jni/source/dictionary.cpp \

LOCAL_C_INCLUDES += src/main/jni/include/

include $(BUILD_SHARED_LIBRARY)
