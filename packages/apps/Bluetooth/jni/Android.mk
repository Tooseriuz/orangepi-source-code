LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
    com_android_bluetooth_btservice_AdapterService.cpp \
    com_android_bluetooth_hfp.cpp \
    com_android_bluetooth_a2dp.cpp \
    com_android_bluetooth_avrcp.cpp \
    com_android_bluetooth_hid.cpp \
    com_android_bluetooth_hdp.cpp \
    com_android_bluetooth_pan.cpp \
    com_android_bluetooth_gatt.cpp

LOCAL_C_INCLUDES += \
    $(JNI_H_INCLUDE) \

LOCAL_SHARED_LIBRARIES := \
    libandroid_runtime \
    libnativehelper \
    libcutils \
    libutils \
    liblog \
    libhardware

#LOCAL_CFLAGS += -O0 -g

# M: ALPS01293574: Dump FTrace
ifeq ($(HAVE_AEE_FEATURE), yes)
	LOCAL_CFLAGS += -DHAVE_AEE_FEATURE
	LOCAL_C_INCLUDES += $(TOP)/$(MTK_ROOT)/external/aee/binary/inc
	LOCAL_SHARED_LIBRARIES += libaed
endif

LOCAL_MODULE := libbluetooth_jni
LOCAL_MODULE_TAGS := optional

include $(BUILD_SHARED_LIBRARY)
