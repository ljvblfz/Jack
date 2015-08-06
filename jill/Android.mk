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

include $(CLEAR_VARS)

LOCAL_MODULE := jill
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := EXECUTABLES

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_JAVA_RESOURCE_DIRS  := rsc
LOCAL_JAR_MANIFEST := etc/manifest.txt

LOCAL_STATIC_JAVA_LIBRARIES := \
  jill-api \
  asm-all-4.1-jack \
  guava-jack \
  jsr305lib-jack \
  args4j-jack \
  schedlib

JILL_VERSION_FILE := $(call local-intermediates-dir,COMMON)/generated.version/jill-version.properties
LOCAL_JAVA_RESOURCE_FILES += $(JILL_VERSION_FILE)
LOCAL_ADDITIONAL_DEPENDENCIES += $(JILL_VERSION_FILE)

include $(BUILD_HOST_JAVA_LIBRARY)

$(JILL_VERSION_FILE): $(TOP_DIR)$(LOCAL_PATH)/../version.properties | $(ACP)
	$(copy-file-to-target)

# Include this library in the build server's output directory
$(call dist-for-goals, dist_files, $(LOCAL_BUILT_MODULE):jill.jar)

include $(CLEAR_VARS)

LOCAL_MODULE := jill-jarjar-asm
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := EXECUTABLES

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_JAVA_RESOURCE_DIRS  := rsc
LOCAL_JAR_MANIFEST := etc/manifest.txt

LOCAL_STATIC_JAVA_LIBRARIES := \
  jill-api \
  asm-all-4.1-jack \
  guava-jack \
  jsr305lib-jack \
  args4j-jack \
  schedlib

LOCAL_JARJAR_RULES := $(LOCAL_PATH)/jarjar-rules.txt
LOCAL_JAVA_RESOURCE_FILES += $(JILL_VERSION_FILE)
LOCAL_ADDITIONAL_DEPENDENCIES += $(JILL_VERSION_FILE)

include $(BUILD_HOST_JAVA_LIBRARY)

#
# Build Jill tests
#

include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, tests)

LOCAL_MODULE := libjillunittests

LOCAL_MODULE_TAGS := optional
LOCAL_JAVACFLAGS := -processor com.android.sched.build.SchedAnnotationProcessor

LOCAL_STATIC_JAVA_LIBRARIES := jill jill-api

LOCAL_JAVA_LIBRARIES := \
  libjackunittests \
  sched-build \
  schedlib

LOCAL_REQUIRED_MODULES:= \
  core \
  bouncycastle \
  ext \
  core-junit \
  framework \
  telephony-common \
  android.policy

include $(BUILD_HOST_JAVA_LIBRARY)

#
# Test targets
#

LIB_JILL_UNIT_TESTS := $(LOCAL_BUILT_MODULE)

local_unit_libs := $(call java-lib-files,core-libart-hostdex junit4-hostdex-jack,true)

.PHONY: test-jill
test-jill-unit: PRIVATE_RUN_TESTS := ./run-jill-unit-tests
test-jill-unit: PRIVATE_PATH := $(LOCAL_PATH)
test-jill-unit: $(LIB_JILL_UNIT_TESTS) $(LOCAL_PATH)/run-jill-unit-tests $(local_unit_libs) $(JACK_JAR) $(JILL_JAR)
	$(hide) cd $(PRIVATE_PATH) && $(PRIVATE_RUN_TESTS) com.android.jill.PreSubmitTests

local_long_libs := $(call java-lib-files,core bouncycastle core-junit ext framework guava services \
  libarity google-play-services-first-party telephony-common,)
.PHONY: test-jill-long
test-jill-long: PRIVATE_RUN_TESTS := ./run-jill-unit-tests
test-jill-long: PRIVATE_PATH := $(LOCAL_PATH)
test-jill-long: $(LIB_JILL_UNIT_TESTS) $(LOCAL_PATH)/run-jill-unit-tests $(local_long_libs) $(JACK_JAR) $(JILL_JAR)
	$(hide) cd $(PRIVATE_PATH) && $(PRIVATE_RUN_TESTS) com.android.jill.LongLastingTests

.PHONY: test-jill-unit-all
test-jill-unit-all: PRIVATE_RUN_TESTS := ./run-jill-unit-tests
test-jill-unit-all: PRIVATE_PATH := $(LOCAL_PATH)
test-jill-unit-all: $(LIB_JILL_UNIT_TESTS) $(LOCAL_PATH)/run-jill-unit-tests $(local_unit_libs) $(local_long_libs) $(JACK_JAR) $(JILL_JAR)
	$(hide) cd $(PRIVATE_PATH) && $(PRIVATE_RUN_TESTS) com.android.jill.AllTests

