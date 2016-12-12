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

# This property file is created by ant build and will cause failure here. Deleting it here is not
# interfering with further ant build making it an acceptable workaround.
$(shell rm -f $(LOCAL_PATH)/rsc/jack-version.properties)

LOCAL_MODULE := jack
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := JAVA_LIBRARIES

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
  schedlib \
  freemarker-jack \
  watchmaker-jack \
  maths-jack \
  args4j-jack \
  antlr-runtime-jack \
  jack-api \
  jill

LOCAL_JAVA_LIBRARIES := \
  sched-build \
  allocation-jack \
  $(JACK_STATIC_JAVA_LIBRARIES)

JACK_VERSION_FILE := $(call local-intermediates-dir,COMMON)/generated.version/jack-version.properties
LOCAL_JAVA_RESOURCE_FILES += $(JACK_VERSION_FILE)

include $(BUILD_HOST_JAVA_LIBRARY)

$(JACK_VERSION_FILE): $(TOP_DIR)$(LOCAL_PATH)/../version.properties | $(ACP)
	$(copy-file-to-target)

JACK_JAR_INTERMEDIATE:=$(LOCAL_BUILT_MODULE).intermediate.jar

# Merge with sched lib support
$(JACK_JAR_INTERMEDIATE):  $(call java-lib-libs,sched-build,true)
$(JACK_JAR_INTERMEDIATE): $(LOCAL_BUILT_MODULE)
	java -jar $(call java-lib-libs,sched-build,true) $< $(call java-lib-libs,$(JACK_STATIC_JAVA_LIBRARIES),true) $@

JACK_CORE_STUBS_MINI := $(LOCAL_BUILT_MODULE).core-stub-mini.jack
JACK_CORE_STUBS_MINI_SRC := $(addprefix $(TOP_DIR)$(LOCAL_PATH)/,$(call all-java-files-under, ../core-stubs-mini/src))

JACK_DEFAULT_LIB := $(LOCAL_BUILT_MODULE).defaultlib.jack
JACK_DEFAULT_LIB_SRC :=$(addprefix $(TOP_DIR)$(LOCAL_PATH)/,$(call all-java-files-under, src/com/android/jack/annotations))
$(JACK_CORE_STUBS_MINI) $(JACK_DEFAULT_LIB): PRIVATE_JACK_EXTRA_ARGS := $(DEFAULT_JACK_EXTRA_ARGS)
ifneq ($(ANDROID_JACK_EXTRA_ARGS),)
$(JACK_CORE_STUBS_MINI) $(JACK_DEFAULT_LIB): PRIVATE_JACK_EXTRA_ARGS := $(ANDROID_JACK_EXTRA_ARGS)
endif

$(JACK_CORE_STUBS_MINI): $(JACK_CORE_STUBS_MINI_SRC) $(JACK_JAR_INTERMEDIATE)
	java -Dfile.encoding=UTF-8 -XX:+TieredCompilation $(JAVA_TMPDIR_ARG) -cp $(JACK_JAR_INTERMEDIATE) com.android.jack.Main $(PRIVATE_JACK_EXTRA_ARGS) \
		-D jack.classpath.default-libraries=false --output-jack $(JACK_CORE_STUBS_MINI) $(JACK_CORE_STUBS_MINI_SRC)

$(JACK_DEFAULT_LIB): $(JACK_DEFAULT_LIB_SRC) $(JACK_CORE_STUBS_MINI) $(JACK_JAR_INTERMEDIATE)
	java -Dfile.encoding=UTF-8 -XX:+TieredCompilation $(JAVA_TMPDIR_ARG) -cp $(JACK_JAR_INTERMEDIATE) com.android.jack.Main $(PRIVATE_JACK_EXTRA_ARGS) \
		--classpath $(JACK_CORE_STUBS_MINI) -D jack.classpath.default-libraries=false --output-jack $(JACK_DEFAULT_LIB) $(JACK_DEFAULT_LIB_SRC)

# overwrite install rule, using LOCAL_POST_INSTALL_CMD may cause the installed jar to be used before the post install command is completed
$(LOCAL_INSTALLED_MODULE): PRIVATE_JAR_MANIFEST := $(LOCAL_PATH)/$(LOCAL_JAR_MANIFEST)
$(LOCAL_INSTALLED_MODULE): $(JACK_JAR_INTERMEDIATE) $(JACK_DEFAULT_LIB)
	@echo "Install: $@"
	$(hide) rm -rf $<.tmp
	$(hide) mkdir -p $<.tmp/jack-default-lib
	$(hide) unzip -qd $<.tmp $<
	$(hide) unzip -qd $<.tmp/jack-default-lib $(JACK_DEFAULT_LIB)
	$(hide) jar -cfm $@ $(PRIVATE_JAR_MANIFEST) -C $<.tmp .

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

