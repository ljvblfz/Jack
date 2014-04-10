# Copyright (C) 2013 The Android Open Source Project
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

LOCAL_PATH:= $(call my-dir)

#
# java-allocation-instrumenter
#
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, src/main/java)

LOCAL_JAR_MANIFEST := etc/manifest.txt

LOCAL_MODULE := allocation-jack

LOCAL_MODULE_TAGS := optional

LOCAL_JARJAR_RULES := $(LOCAL_PATH)/jarjar-rules.txt

LOCAL_JAVA_LIBRARIES := \
  guava-jack

LOCAL_STATIC_JAVA_LIBRARIES := \
  guava-collect-jack

include $(BUILD_HOST_JAVA_LIBRARY)

$(LOCAL_BUILT_MODULE): PRIVATE_LOCAL_PATH := $(LOCAL_PATH)
$(LOCAL_BUILT_MODULE): $(full_classes_compiled_jar) | $(JARJAR)
	@echo JarJar: $@
	$(hide) java -jar $(JARJAR) process $(PRIVATE_JARJAR_RULES) $< $@
	$(hide)jar umf $(PRIVATE_LOCAL_PATH)/etc/manifest.txt $@

$(call dist-for-goals, dist_files, $(LOCAL_BUILT_MODULE):allocation-jack.jar)

