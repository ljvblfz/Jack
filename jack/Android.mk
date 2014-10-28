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

LOCAL_PATH:= $(call my-dir)

#
# Build Jack
#

# $(1): library name
# $(2): Non-empty if IS_HOST_MODULE
define java-lib-libs
$(foreach lib,$(1),$(call _java-lib-dir,$(lib),$(2))/$(if $(2),javalib,classes)$(COMMON_JAVA_PACKAGE_SUFFIX))
endef

include $(CLEAR_VARS)

JACK_BASE_VERSION_NAME := 1.0
JACK_BASE_VERSION_CODE := 001

LOCAL_MODULE := jack
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := EXECUTABLES

proguard_intermediates := \
  $(call local-intermediates-dir,COMMON)/grammar/com/android/jack/shrob/proguard

preprocessor_intermediates := \
  $(call local-intermediates-dir,COMMON)/grammar/com/android/jack/preprocessor

GEN_PG := $(addprefix $(proguard_intermediates)/, \
  ProguardLexer.java \
  ProguardParser.java \
)

GEN_PP := $(addprefix $(preprocessor_intermediates)/, \
  PreProcessorLexer.java \
  PreProcessorParser.java \
  PreProcessor_Java.java \
)

ANTLR_JACK_JAR = $(call java-lib-libs,antlr-jack,true)
$(info ANTLR_JACK_JAR = $(ANTLR_JACK_JAR))
$(GEN_PG): $(ANTLR_JACK_JAR)
$(GEN_PG): PRIVATE_PATH := $(LOCAL_PATH)
$(GEN_PG): PRIVATE_CUSTOM_TOOL = java -jar $(ANTLR_JACK_JAR) -fo $(dir $@) $<
$(GEN_PG): $(LOCAL_PATH)/src/com/android/jack/shrob/proguard/Proguard.g
	$(transform-generated-source)

$(GEN_PP): $(ANTLR_JACK_JAR)
$(GEN_PP): PRIVATE_PATH := $(LOCAL_PATH)
$(GEN_PP): PRIVATE_CUSTOM_TOOL = java -jar $(ANTLR_JACK_JAR) -fo $(dir $@) $<
$(GEN_PP): $(LOCAL_PATH)/src/com/android/jack/preprocessor/PreProcessor.g
	$(transform-generated-source)

LOCAL_GENERATED_SOURCES += $(GEN_PG) $(GEN_PP)

LOCAL_SRC_FILES := $(filter-out %/ProguardLexer.java %/ProguardParser.java %/PreProcessorLexer.java %/PreProcessorParser.java %/PreProcessor_Java.java, \
  $(call all-java-files-under, src))

LOCAL_JAVA_RESOURCE_DIRS  := rsc
LOCAL_JAR_MANIFEST := etc/manifest.txt
LOCAL_JAVACFLAGS := -processor com.android.sched.build.SchedAnnotationProcessor

JACK_STATIC_JAVA_LIBRARIES := \
  ecj-jack \
  guava-jack \
  jsr305lib-jack \
  dx-jack \
  schedlib \
  freemarker-jack \
  watchmaker-jack \
  maths-jack \
  args4j-jack \
  antlr-runtime-jack

LOCAL_JAVA_LIBRARIES := \
  sched-build \
  allocation-jack \
  $(JACK_STATIC_JAVA_LIBRARIES)

ifneq "" "$(filter eng.%,$(BUILD_NUMBER))"
  JACK_VERSION_NAME_TAG := eng.$(USER)
else
  JACK_VERSION_NAME_TAG := $(BUILD_NUMBER)
endif

JACK_VERSION_NAME := "$(JACK_BASE_VERSION_NAME).$(JACK_BASE_VERSION_CODE).$(JACK_VERSION_NAME_TAG)"

intermediates := $(call local-intermediates-dir,COMMON)
$(intermediates)/rsc/jack.properties: $(LOCAL_PATH)/Android.mk
	$(hide) mkdir -p $(dir $@)
	$(hide) echo "jack.version=$(JACK_VERSION_NAME)" > $@

LOCAL_JAVA_RESOURCE_FILES := $(intermediates)/rsc/jack.properties

include $(BUILD_HOST_JAVA_LIBRARY)

# overwrite install rule, using LOCAL_POST_INSTALL_CMD may cause the installed jar to be used before the post install command is completed
$(LOCAL_INSTALLED_MODULE): $(LOCAL_BUILT_MODULE)
	java -jar $(call java-lib-libs,sched-build,true) $< $(call java-lib-libs,$(JACK_STATIC_JAVA_LIBRARIES),true) $@

# Merge with sched lib support
$(LOCAL_BUILT_MODULE):  $(call java-lib-libs,sched-build,true)

# Include this library in the build server's output directory
$(call dist-for-goals, dist_files, $(LOCAL_BUILT_MODULE):jack.jar)

#
# Build Jack tests
#

include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, tests)

LOCAL_MODULE := libjackunittests

LOCAL_MODULE_TAGS := optional
LOCAL_JAVACFLAGS := -processor com.android.sched.build.SchedAnnotationProcessor

TEST_STATIC_JAVA_LIBRARIES := \
  jack \
  junit4-jack \
  dexlib-jack \
  dx \
  dexcomparator

LOCAL_JAVA_LIBRARIES := \
  sched-build \
  $(TEST_STATIC_JAVA_LIBRARIES)

LOCAL_REQUIRED_MODULES:= \
  core-stubs-mini \
  core-libart \
  bouncycastle \
  ext \
  core-junit \
  framework \
  telephony-common \
  android.policy \
  hamcrest-core-jack

include $(BUILD_HOST_JAVA_LIBRARY)

# overwrite install rule, using LOCAL_POST_INSTALL_CMD may cause the installed jar to be used before the post install command is completed
$(LOCAL_INSTALLED_MODULE): $(LOCAL_BUILT_MODULE) $(call java-lib-libs,sched-build,true)
	java -jar $(call java-lib-libs,sched-build,true) $< $(JACK_JAR) $(call java-lib-libs,$(TEST_STATIC_JAVA_LIBRARIES),true) $@

#
# Test targets
#

LIB_JACK_UNIT_TESTS := $(LOCAL_INSTALLED_MODULE)

local_unit_libs := $(call java-lib-files,core-libart-hostdex junit4-hostdex-jack,true)
.PHONY: test-jack-unit
test-jack-unit: PRIVATE_RUN_TESTS := ./run-jack-unit-tests
test-jack-unit: PRIVATE_PATH := $(LOCAL_PATH)
test-jack-unit: $(LIB_JACK_UNIT_TESTS) $(LOCAL_PATH)/run-jack-unit-tests $(local_unit_libs) $(JACK_JAR)
	$(hide) cd $(PRIVATE_PATH) && $(PRIVATE_RUN_TESTS) com.android.jack.PreSubmitTests

local_long_libs := $(call java-lib-files,core-libart bouncycastle core-junit ext framework guava services \
	libarity prebuilt-google-play-services-first-party telephony-common,)
.PHONY: test-jack-long
test-jack-long: PRIVATE_RUN_TESTS := ./run-jack-unit-tests
test-jack-long: PRIVATE_PATH := $(LOCAL_PATH)
test-jack-long: $(LIB_JACK_UNIT_TESTS) $(LOCAL_PATH)/run-jack-unit-tests $(local_long_libs) $(JACK_JAR)
	$(hide) cd $(PRIVATE_PATH) && $(PRIVATE_RUN_TESTS) com.android.jack.LongLastingTests && echo "$@: PASSED"

.PHONY: test-jack-unit-all
test-jack-unit-all: PRIVATE_RUN_TESTS := ./run-jack-unit-tests
test-jack-unit-all: PRIVATE_PATH := $(LOCAL_PATH)
test-jack-unit-all: $(LIB_JACK_UNIT_TESTS) $(LOCAL_PATH)/run-jack-unit-tests $(local_unit_libs) $(local_long_libs) $(JACK_JAR)
	$(hide) cd $(PRIVATE_PATH) && $(PRIVATE_RUN_TESTS) com.android.jack.AllTests

.PHONY: clean-jack-files
clean-jack-files:
	$(hide) find $(OUT_DIR) -name "*.jayce" | xargs rm -f
	$(hide) for i in `find $(OUT_DIR) -name "*.jar"` ; do ((unzip -l $$i 2> /dev/null | \
				grep -q "\.jayce$$" && rm -f $$i) || continue ) ; done
	@echo "All jack files and archives containing jack files have been removed."

.PHONY: clean-dex-files
clean-dex-files:
	$(hide) find $(OUT_DIR) -name "*.dex" | xargs rm -f
	$(hide) for i in `find $(OUT_DIR) -name "*.jar" -o -name "*.apk"` ; do ((unzip -l $$i 2> /dev/null | \
				grep -q "\.dex$$" && rm -f $$i) || continue ) ; done
	@echo "All dex files and archives containing dex files have been removed."
