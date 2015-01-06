# Get local path
LOCAL_PATH := $(call my-dir)
# Clear variables
include $(CLEAR_VARS)

# Module name. lib and .so is added by script.
LOCAL_MODULE := msm

MSM_INCLUDE_PATH := $(LOCAL_PATH)/../modules/MineSweeperMatrix/src
MSM_SRC_PATH := ../modules/MineSweeperMatrix/src

LOCAL_SRC_FILES := msm-jni.cpp \
				$(MSM_SRC_PATH)/field.cpp \
				$(MSM_SRC_PATH)/matrix.cpp
$(info LOCAL_SRC_FILES: $(LOCAL_SRC_FILES))

LOCAL_CFLAGS := -DJNIREF=1 -DBOOST_SIGNALS=0
$(info LOCAL_CFLAGS: $(LOCAL_CFLAGS))

LOCAL_C_INCLUDES := $(MSM_INCLUDE_PATH)
$(info LOCAL_C_INCLUDES: $(LOCAL_C_INCLUDES))

LOCAL_CPP_FEATURES := exceptions

LOCAL_LDLIBS    := -llog
include $(BUILD_SHARED_LIBRARY)
