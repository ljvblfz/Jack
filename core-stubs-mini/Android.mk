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

LOCAL_PATH := $(call my-dir)

#
# core-stubs-mini library
#

include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE:= core-stubs-mini

include $(BUILD_JAVA_LIBRARY)
$(LOCAL_BUILT_MODULE):PRIVATE_DEST:=$(LOCAL_PATH)
$(LOCAL_BUILT_MODULE):PRIVATE_CLASSES_JAR:=$(full_classes_jar)
$(LOCAL_BUILT_MODULE): $(common_javalib.jar)
	$(copy-file-to-target)
	mkdir -p $(dir $(PRIVATE_DEST)/../jack/libs/)
	cp $(PRIVATE_CLASSES_JAR) $(PRIVATE_DEST)/../jack/libs/core-stubs-mini.jar
