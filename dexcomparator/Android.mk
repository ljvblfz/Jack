#
# Dex comparator
#

LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_MODULE := dexcomparator
LOCAL_MODULE_TAGS := optional

LOCAL_JAVA_LIBRARIES := \
    dx-jack \
    dexlib-jack \
    jsr305lib-jack \
    args4j-jack

include $(BUILD_HOST_JAVA_LIBRARY)


include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_MODULE := dexComparatorTool
LOCAL_JAR_MANIFEST := etc/manifest.txt
LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := \
    dx-jack \
    dexlib-jack \
    jsr305lib-jack \
    args4j-jack

include $(BUILD_HOST_JAVA_LIBRARY)
