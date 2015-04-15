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
# Build jack script
#

include $(CLEAR_VARS)

LOCAL_MODULE := jack
LOCAL_SRC_FILES := etc/jack
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_SUFFIX := $(HOST_EXECUTABLE_SUFFIX)
LOCAL_BUILT_MODULE_STEM := jack$(HOST_EXECUTABLE_SUFFIX)
LOCAL_IS_HOST_MODULE := true

include $(BUILD_PREBUILT)
jack_script := $(LOCAL_INSTALLED_MODULE)

#
# Build jack-admin script
#

include $(CLEAR_VARS)

LOCAL_MODULE := jack-admin
LOCAL_SRC_FILES := etc/jack-admin
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_SUFFIX := $(HOST_EXECUTABLE_SUFFIX)
LOCAL_BUILT_MODULE_STEM := jack-admin$(HOST_EXECUTABLE_SUFFIX)
LOCAL_IS_HOST_MODULE := true

include $(BUILD_PREBUILT)
jack_admin_script := $(LOCAL_INSTALLED_MODULE)

#
# Build Jack
#

# $(1): library name
# $(2): Non-empty if IS_HOST_MODULE
define java-lib-libs
$(foreach lib,$(1),$(call _java-lib-dir,$(lib),$(2))/$(if $(2),javalib,classes)$(COMMON_JAVA_PACKAGE_SUFFIX))
endef

include $(CLEAR_VARS)

LOCAL_MODULE := jack-no-server
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
  antlr-runtime-jack \
  jack-api

LOCAL_JAVA_LIBRARIES := \
  sched-build \
  allocation-jack \
  $(JACK_STATIC_JAVA_LIBRARIES)

include $(BUILD_HOST_JAVA_LIBRARY)
$(LOCAL_INSTALLED_MODULE) : $(jack_script) $(jack_admin_script)
INSTALLED_JACK_NOSERVER := $(LOCAL_INSTALLED_MODULE)
JACK_JAR_INTERMEDIATE:=$(LOCAL_BUILT_MODULE).intermediate.jar
$(JACK_JAR_INTERMEDIATE): $(LOCAL_BUILT_MODULE)
	java -jar $(call java-lib-libs,sched-build,true) $< $(call java-lib-libs,$(JACK_STATIC_JAVA_LIBRARIES),true) $@

JACK_CORE_STUBS_MINI := $(LOCAL_BUILT_MODULE).core-stub-mini.jack
JACK_CORE_STUBS_MINI_SRC := $(addprefix $(TOP_DIR)$(LOCAL_PATH)/,$(call all-java-files-under, ../core-stubs-mini/src))

JACK_DEFAULT_LIB := $(LOCAL_BUILT_MODULE).defaultlib.jack
JACK_DEFAULT_LIB_SRC :=$(addprefix $(TOP_DIR)$(LOCAL_PATH)/,$(call all-java-files-under, src/com/android/jack/annotations))
$(JACK_CORE_STUBS_MINI) $(JACK_DEFAULT_LIB): PRIVATE_JACK_VM_ARGS := $(DEFAULT_JACK_VM_ARGS)
ifneq ($(ANDROID_JACK_VM_ARGS),)
$(JACK_CORE_STUBS_MINI) $(JACK_DEFAULT_LIB): PRIVATE_JACK_VM_ARGS := $(ANDROID_JACK_VM_ARGS)
endif
$(JACK_CORE_STUBS_MINI) $(JACK_DEFAULT_LIB): PRIVATE_JACK_EXTRA_ARGS := $(DEFAULT_JACK_EXTRA_ARGS)
ifneq ($(ANDROID_JACK_EXTRA_ARGS),)
$(JACK_CORE_STUBS_MINI) $(JACK_DEFAULT_LIB): PRIVATE_JACK_EXTRA_ARGS := $(ANDROID_JACK_EXTRA_ARGS)
endif
$(JACK_CORE_STUBS_MINI) $(JACK_DEFAULT_LIB): PRIVATE_JACK_VM := $(DEFAULT_JACK_VM)
ifneq ($(strip $(ANDROID_JACK_VM)),)
$(JACK_CORE_STUBS_MINI) $(JACK_DEFAULT_LIB): PRIVATE_JACK_VM := $(ANDROID_JACK_VM)
endif

$(JACK_CORE_STUBS_MINI): $(JACK_CORE_STUBS_MINI_SRC) $(JACK_JAR_INTERMEDIATE)
	$(PRIVATE_JACK_VM) $(PRIVATE_JACK_VM_ARGS) -cp $(JACK_JAR_INTERMEDIATE) com.android.jack.Main $(PRIVATE_JACK_EXTRA_ARGS) \
		-D jack.classpath.default-libraries=false --output-jack $(JACK_CORE_STUBS_MINI) $(JACK_CORE_STUBS_MINI_SRC)

$(JACK_DEFAULT_LIB): $(JACK_DEFAULT_LIB_SRC) $(JACK_CORE_STUBS_MINI) $(JACK_JAR_INTERMEDIATE)
	$(PRIVATE_JACK_VM) $(PRIVATE_JACK_VM_ARGS) -cp $(JACK_JAR_INTERMEDIATE) com.android.jack.Main $(PRIVATE_JACK_EXTRA_ARGS) \
		--classpath $(JACK_CORE_STUBS_MINI) -D jack.classpath.default-libraries=false --output-jack $(JACK_DEFAULT_LIB) $(JACK_DEFAULT_LIB_SRC)

# overwrite install rule, using LOCAL_POST_INSTALL_CMD may cause the installed jar to be used before the post install command is completed
$(LOCAL_INSTALLED_MODULE): PRIVATE_JAR_MANIFEST := $(LOCAL_PATH)/$(LOCAL_JAR_MANIFEST)
$(LOCAL_INSTALLED_MODULE): $(JACK_JAR_INTERMEDIATE) $(JACK_DEFAULT_LIB)
	$(hide) rm -rf $<.tmp
	$(hide) mkdir -p $<.tmp/jack-default-lib
	$(hide) unzip -qd $<.tmp $<
	$(hide) unzip -qd $<.tmp/jack-default-lib $(JACK_DEFAULT_LIB)
	$(hide) jar -cfm $@ $(PRIVATE_JAR_MANIFEST) -C $<.tmp .

# Merge with sched lib support
$(LOCAL_BUILT_MODULE):  $(call java-lib-libs,sched-build,true)


include $(CLEAR_VARS)

LOCAL_MODULE := jack
LOCAL_MODULE_TAGS := optional
LOCAL_STATIC_JAVA_LIBRARIES := \
  jack-server
LOCAL_ADDITIONAL_DEPENDENCIES := $(jack_admin_script)
include $(BUILD_HOST_JAVA_LIBRARY)
$(LOCAL_INSTALLED_MODULE): $(LOCAL_BUILT_MODULE)
	$(hide) $(jack_admin_script) kill-server || echo
	$(hide) rm -rf $<.tmp
	$(hide) mkdir -p $<.tmp
	$(hide) unzip -qd $<.tmp $<
	$(hide) unzip -oqd $<.tmp $(INSTALLED_JACK_NOSERVER)
	$(hide) jar -cf $@ -C $<.tmp .


# Include this library in the build server's output directory
$(call dist-for-goals, dist_files, $(LOCAL_BUILT_MODULE):jack.jar)

#
# Build jack-annotations.jar
#

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(call all-java-files-under, src/com/android/jack/annotations)

LOCAL_MODULE := jack-annotations
LOCAL_MODULE_TAGS := optional

include $(BUILD_HOST_JAVA_LIBRARY)

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
	$(hide) cd $(PRIVATE_PATH) && $(PRIVATE_RUN_TESTS) com.android.jack.PreSubmitUnitTests

local_long_libs := $(call java-lib-files,core-libart bouncycastle core-junit ext framework guava services \
	libarity prebuilt-google-play-services-first-party telephony-common,)
.PHONY: test-jack-long
test-jack-long: PRIVATE_RUN_TESTS := ./run-jack-unit-tests
test-jack-long: PRIVATE_PATH := $(LOCAL_PATH)
test-jack-long: $(LIB_JACK_UNIT_TESTS) $(LOCAL_PATH)/run-jack-unit-tests $(local_long_libs) $(JACK_JAR)
	$(hide) cd $(PRIVATE_PATH) && $(PRIVATE_RUN_TESTS) com.android.jack.LongLastingUnitTests && echo "$@: PASSED"

.PHONY: test-jack-unit-all
test-jack-unit-all: PRIVATE_RUN_TESTS := ./run-jack-unit-tests
test-jack-unit-all: PRIVATE_PATH := $(LOCAL_PATH)
test-jack-unit-all: $(LIB_JACK_UNIT_TESTS) $(LOCAL_PATH)/run-jack-unit-tests $(local_unit_libs) $(local_long_libs) $(JACK_JAR)
	$(hide) cd $(PRIVATE_PATH) && $(PRIVATE_RUN_TESTS) com.android.jack.AllUnitTests

.PHONY: clean-jack-files
clean-jack-files: clean-dex-files
	$(hide) find $(OUT_DIR) -name "*.jack" | xargs rm -f
	$(hide) find $(OUT_DIR) -type d -name "jack" | xargs rm -rf
	@echo "All jack files have been removed."

.PHONY: clean-dex-files
clean-dex-files:
	$(hide) find $(OUT_DIR) -name "*.dex" | xargs rm -f
	$(hide) for i in `find $(OUT_DIR) -name "*.jar" -o -name "*.apk"` ; do ((unzip -l $$i 2> /dev/null | \
				grep -q "\.dex$$" && rm -f $$i) || continue ) ; done
	@echo "All dex files and archives containing dex files have been removed."
