#
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

LOCAL_PATH := $(call my-dir)

#
# Build support for sched: annotation processor and jar merger
#

include $(CLEAR_VARS)

LOCAL_MODULE := sched-build
LOCAL_MODULE_TAGS := optional
LOCAL_JAVACFLAGS := -sourcepath $(LOCAL_PATH)/src
LOCAL_SRC_FILES := \
  src/com/android/sched/build/SchedAnnotationProcessor.java \
  src/com/android/sched/build/JarMerger.java
LOCAL_JAVA_RESOURCE_DIRS := rsc-ap
LOCAL_JAR_MANIFEST := etc/manifest.txt

LOCAL_JAVA_LIBRARIES := \
  jsr305lib-jack

include $(BUILD_HOST_JAVA_LIBRARY)

#
# Build sched lib
#

include $(CLEAR_VARS)

LOCAL_MODULE := schedlib

LOCAL_JAVA_RESOURCE_DIRS := rsc

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under,src)
LOCAL_JAVACFLAGS := -processor com.android.sched.build.SchedAnnotationProcessor

LOCAL_JAVA_LIBRARIES := \
  sched-build \
  guava-jack \
  jsr305lib-jack \
  allocation-jack \
  freemarker-jack \
  watchmaker-jack \
  maths-jack

include $(BUILD_HOST_JAVA_LIBRARY)

#
# Build sched lib tests
#

include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, tests)

LOCAL_MODULE := libschedtests

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := \
  schedlib \
  guava-jack \
  jsr305lib-jack \
  junit4-jack

include $(BUILD_HOST_JAVA_LIBRARY)
