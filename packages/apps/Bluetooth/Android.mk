ANDROID_BT_JB_MR1 := yes
ifeq ($(ANDROID_BT_JB_MR1),yes)
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := \
        $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := Bluetooth
LOCAL_CERTIFICATE := platform

LOCAL_JNI_SHARED_LIBRARIES := libbluetooth_jni
LOCAL_JAVA_LIBRARIES := javax.obex telephony-common mms-common mediatek-telephony-common
LOCAL_JAVA_LIBRARIES += mediatek-framework
LOCAL_STATIC_JAVA_LIBRARIES := com.android.vcard

#Path to aspect root folder
LOCAL_ASPECTS_DIR := aspect/Bluetooth/src

LOCAL_REQUIRED_MODULES := libbluetooth_jni bluetooth.default

LOCAL_PROGUARD_ENABLED := disabled

ifeq (yes,$(strip $(RUNYEE_CONFIG_OPPO_STYLE)))
  LOCAL_MANIFEST_FILE := myAndroidManifest/AndroidManifest.xml
endif

include $(BUILD_PACKAGE)

##################################################
#include $(CLEAR_VARS)

#LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := aspectj-runtime:../../../prebuilts/tools/common/aspectj/aspectjrt.jar

#include $(BUILD_MULTI_PREBUILT)

include $(call all-makefiles-under,$(LOCAL_PATH))

endif
