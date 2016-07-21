# Copyright (C) 2014 The Android Open Source Project
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

LOCAL_PATH := $(call my-dir)

# build dexlib-jack jar
# ============================================================

include $(CLEAR_VARS)

LOCAL_MODULE := dexlib-jack

LOCAL_MODULE_TAGS := optional

#LOCAL_MODULE_CLASS and LOCAL_IS_HOST_MODULE must be defined before calling $(local-intermediates-dir)
LOCAL_MODULE_CLASS := JAVA_LIBRARIES
LOCAL_IS_HOST_MODULE := true

intermediates := $(call local-intermediates-dir,COMMON)

LOCAL_SRC_FILES := \
	$(call all-java-files-under, src/main/java) \

#extract the current version from the pom file
DEXLIB_VERSION := $(shell cat $(LOCAL_PATH)/version)

#create a new dexlib.properties file using the correct version
$(intermediates)/resources/dexlib.properties:
	$(hide) mkdir -p $(dir $@)
	$(hide) echo "application.version=$(DEXLIB_VERSION)" > $@

LOCAL_JAVA_RESOURCE_FILES := $(intermediates)/resources/dexlib.properties

LOCAL_JAVA_LIBRARIES := \
	guava-jack \
	jsr305lib-jack

include $(BUILD_HOST_JAVA_LIBRARY)
