# Copyright (C) 2012 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#
# asm
#
LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, src)
# Excludes
LOCAL_SRC_FILES := $(patsubst src/org/objectweb/asm/attrs%,,$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(patsubst src/org/objectweb/asm/commons%,,$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(patsubst src/org/objectweb/asm/optimizer%,,$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(patsubst src/org/objectweb/asm/tree%,,$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(patsubst src/org/objectweb/asm/util%,,$(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(patsubst src/org/objectweb/asm/xml%,,$(LOCAL_SRC_FILES))

LOCAL_MODULE := asm-4.1-jack

LOCAL_MODULE_TAGS := optional

include $(BUILD_HOST_JAVA_LIBRARY)

#
# asm-analysis
#
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, src/org/objectweb/asm/tree/analysis)

LOCAL_MODULE := asm-analysis-4.1-jack

LOCAL_MODULE_TAGS := optional

LOCAL_JAVA_LIBRARIES := asm-4.1-jack asm-tree-4.1-jack

include $(BUILD_HOST_JAVA_LIBRARY)

#
# asm-commons
#
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, src/org/objectweb/asm/commons)

LOCAL_MODULE := asm-commons-4.1-jack

LOCAL_MODULE_TAGS := optional

LOCAL_JAVA_LIBRARIES := asm-4.1-jack asm-tree-4.1-jack

include $(BUILD_HOST_JAVA_LIBRARY)

#
# asm-tree
#
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, src/org/objectweb/asm/tree)
# Excludes
LOCAL_SRC_FILES := $(patsubst src/org/objectweb/asm/tree/analysis%,, $(LOCAL_SRC_FILES))

LOCAL_MODULE := asm-tree-4.1-jack

LOCAL_MODULE_TAGS := optional

LOCAL_JAVA_LIBRARIES := asm-4.1-jack

include $(BUILD_HOST_JAVA_LIBRARY)

#
# asm-util
#
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, src/org/objectweb/asm/util)

LOCAL_MODULE := asm-util-4.1-jack

LOCAL_MODULE_TAGS := optional

LOCAL_JAVA_LIBRARIES := asm-4.1-jack asm-analysis-4.1-jack asm-tree-4.1-jack

include $(BUILD_HOST_JAVA_LIBRARY)

#
# asm-xml
#
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, src/org/objectweb/asm/xml)

LOCAL_MODULE := asm-xml-4.1-jack

LOCAL_MODULE_TAGS := optional

LOCAL_JAVA_LIBRARIES := asm-4.1-jack asm-util-4.1-jack

include $(BUILD_HOST_JAVA_LIBRARY)

#
# asm-all
#
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, src)
# Excludes
LOCAL_SRC_FILES := $(patsubst src/org/objectweb/asm/attrs%,, $(LOCAL_SRC_FILES))
LOCAL_SRC_FILES := $(patsubst src/org/objectweb/asm/optimizer%,, $(LOCAL_SRC_FILES))

LOCAL_MODULE := asm-all-4.1-jack

LOCAL_MODULE_TAGS := optional

include $(BUILD_HOST_JAVA_LIBRARY)


