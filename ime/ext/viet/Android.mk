LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := anysoftkey_telex
LOCAL_LDFLAGS := -Wl,--build-id
LOCAL_LDLIBS := -llog
LOCAL_SRC_FILES := \
	src/main/jni/source/telex.c \

include $(BUILD_SHARED_LIBRARY)
