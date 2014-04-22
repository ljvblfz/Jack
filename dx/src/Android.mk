# Copyright 2006 The Android Open Source Project
#
LOCAL_PATH := $(call my-dir)

# dx java library
# ============================================================
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE:= dx-jack
LOCAL_STATIC_JAVA_LIBRARIES := jsr305lib-jack

include $(BUILD_HOST_JAVA_LIBRARY)

# the documentation
# ============================================================
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-subdir-java-files) $(call all-subdir-html-files)
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE:= dx-jack
LOCAL_DROIDDOC_OPTIONS := -hidden
LOCAL_MODULE_CLASS := JAVA_LIBRARIES
LOCAL_IS_HOST_MODULE := true

include $(BUILD_DROIDDOC)
