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

JACKTEST_INTERMEDIATE_DIR := $(jack.intermediate)/$(JACKTEST_MODULE)

JACKTEST_LIB_JAR := $(if $(strip $(JACKTEST_LIB_SRC)),$(JACKTEST_INTERMEDIATE_DIR)/lib/classes.jar,)
JACKTEST_LIB_DEX := $(if $(strip $(JACKTEST_LIB_SRC)),$(ANDROID_BUILD_TOP)/$(JACKTEST_INTERMEDIATE_DIR)/lib/classes.dex,)

JACKTEST_WITHJACK_JAR := $(JACKTEST_INTERMEDIATE_DIR)/withjack/classes.jar
JACKTEST_WITHJACK_DEX := $(ANDROID_BUILD_TOP)/$(JACKTEST_INTERMEDIATE_DIR)/withjack/classes.dex

JACKTEST_LINK_DEX := $(if $(strip $(JACKTEST_LINK_SRC)),$(ANDROID_BUILD_TOP)/$(JACKTEST_INTERMEDIATE_DIR)/link/classes.dex,)

JACKTEST_WITHDX_JAR := $(JACKTEST_INTERMEDIATE_DIR)/withdx/classes.jar
JACKTEST_WITHDX_DEX := $(ANDROID_BUILD_TOP)/$(JACKTEST_INTERMEDIATE_DIR)/withdx/classes.dex

ifeq ($(JACKTEST_ARGS)$(JACKTEST_DALVIK_FLAGS)$(JACKTEST_SOURCE_JAVA7),)
JACKREGRESSIONTEST_WITHJACK_SRC := $(JACKREGRESSIONTEST_WITHJACK_SRC) $(JACKTEST_WITHJACK_SRC)
JACKREGRESSIONTEST_LINK_SRC := $(JACKREGRESSIONTEST_LINK_SRC) $(JACKTEST_LINK_SRC)
JACKREGRESSIONTEST_JUNIT := $(JACKREGRESSIONTEST_JUNIT) $(JACKTEST_JUNIT)
JACKREGRESSIONTEST_WITHDX_SRC := $(JACKREGRESSIONTEST_WITHDX_SRC) $(JACKTEST_WITHDX_SRC)
JACKREGRESSIONTEST_LIB_SRC := $(JACKREGRESSIONTEST_LIB_SRC) $(JACKTEST_LIB_SRC)
JACKREGRESSIONTEST_TEST_MK := $(JACKREGRESSIONTEST_TEST_MK) $(PRIVATE_TEST_MK)
else
test-jack-regression: test-jack-$(JACKTEST_MODULE)
endif
ifneq ($(JACKTEST_MODULE),regression)
test-jack-one-by-one: test-jack-$(JACKTEST_MODULE)
endif

jack_test_lib_sources_list := $(JACKTEST_INTERMEDIATE_DIR)/lib/$(JACKTEST_MODULE)_lib_sources.list
$(jack_test_lib_sources_list): PRIVATE_JAVA_SOURCES := $(JACKTEST_LIB_SRC)
$(jack_test_lib_sources_list): PRIVATE_SOURCE_INTERMEDIATES_DIR :=
$(jack_test_lib_sources_list): $(JACKTEST_LIB_SRC)
	$(call create-source-list)

jack_test_with_jack_sources_list := $(JACKTEST_INTERMEDIATE_DIR)/withjack/$(JACKTEST_MODULE)_with_jack_sources.list
$(jack_test_with_jack_sources_list): PRIVATE_JAVA_SOURCES := $(JACKTEST_WITHJACK_SRC)
$(jack_test_with_jack_sources_list): PRIVATE_SOURCE_INTERMEDIATES_DIR :=
$(jack_test_with_jack_sources_list): $(JACKTEST_WITHJACK_SRC)
	$(call create-source-list)

jack_test_with_dx_sources_list := $(JACKTEST_INTERMEDIATE_DIR)/withdx/$(JACKTEST_MODULE)_with_dx_sources.list
$(jack_test_with_dx_sources_list): PRIVATE_JAVA_SOURCES := $(JACKTEST_WITHDX_SRC)
$(jack_test_with_dx_sources_list): PRIVATE_SOURCE_INTERMEDIATES_DIR :=
$(jack_test_with_dx_sources_list): $(JACKTEST_WITHDX_SRC)
	$(call create-source-list)

local_bootclasspath := \
	$(addprefix -bootclasspath ,$(call normalize-path-list,$(BOOTCLASSPATH_LIBS)))
local_bootclasspath_jack := \
	$(addprefix --bootclasspath ,$(call normalize-path-list,$(BOOTCLASSPATH_LIBS)))

$(JACKTEST_LIB_JAR): PRIVATE_JACK_DEBUG_FLAGS := -g
$(JACKTEST_LIB_JAR): PRIVATE_JAVA_SOURCES := $(JACKTEST_LIB_SRC)
$(JACKTEST_LIB_JAR): PRIVATE_STATIC_JAVA_LIBRARIES :=
$(JACKTEST_LIB_JAR): PRIVATE_ALL_JAVA_LIBRARIES :=
$(JACKTEST_LIB_JAR): PRIVATE_SOURCE_INTERMEDIATES_DIR :=
$(JACKTEST_LIB_JAR): PRIVATE_MODULE := jacktest_$(JACKTEST_MODULE)_lib
$(JACKTEST_LIB_JAR): PRIVATE_CLASS_INTERMEDIATES_DIR := $(JACKTEST_INTERMEDIATE_DIR)/lib/classes
$(JACKTEST_LIB_JAR): PRIVATE_BOOTCLASSPATH := $(local_bootclasspath)
$(JACKTEST_LIB_JAR): PRIVATE_SOURCES_LIST := $(jack_test_lib_sources_list)
$(JACKTEST_LIB_JAR): PRIVATE_JAVACFLAGS := -nowarn
$(JACKTEST_LIB_JAR): $(JACKTEST_LIB_SRC) $(BOOTCLASSPATH_LIBS) $(PRIVATE_TEST_MK) $(jack_test_lib_sources_list) $(JACK_JAR)
	$(transform-host-java-to-package-with-jack)

$(JACKTEST_LIB_DEX): PRIVATE_MODULE := jacktest_$(JACKTEST_MODULE)_lib
$(JACKTEST_LIB_DEX): PRIVATE_INTERMEDIATES_DIR := $(JACKTEST_INTERMEDIATE_DIR)/lib
$(JACKTEST_LIB_DEX): PRIVATE_DX_FLAGS :=
$(JACKTEST_LIB_DEX): $(JACKTEST_LIB_JAR) $(JACK_JAR) $(JILL_JAR)
	$(transform-classes.jar-to-dex)

$(JACKTEST_WITHJACK_JAR): PRIVATE_JACK_DEBUG_FLAGS := -g
$(JACKTEST_WITHJACK_JAR): PRIVATE_JAVA_SOURCES := $(JACKTEST_WITHJACK_SRC)
$(JACKTEST_WITHJACK_JAR): PRIVATE_STATIC_JAVA_LIBRARIES :=
$(JACKTEST_WITHJACK_JAR): PRIVATE_ALL_JAVA_LIBRARIES := $(JACKTEST_LIB_JAR)
$(JACKTEST_WITHJACK_JAR): PRIVATE_SOURCE_INTERMEDIATES_DIR :=
$(JACKTEST_WITHJACK_JAR): PRIVATE_MODULE := jacktest_$(JACKTEST_MODULE)_withjack
$(JACKTEST_WITHJACK_JAR): PRIVATE_CLASS_INTERMEDIATES_DIR := $(JACKTEST_INTERMEDIATE_DIR)/withjack/classes
$(JACKTEST_WITHJACK_JAR): PRIVATE_BOOTCLASSPATH := $(local_bootclasspath)
$(JACKTEST_WITHJACK_JAR): PRIVATE_SOURCES_LIST := $(jack_test_with_jack_sources_list)
$(JACKTEST_WITHJACK_JAR): PRIVATE_JAVACFLAGS := -nowarn
$(JACKTEST_WITHJACK_JAR): PRIVATE_JACK_FLAGS := $(JACKTEST_SOURCE_JAVA7)
$(JACKTEST_WITHJACK_JAR): COMMON_JAVAC := $(JAVA_COMPILER)
$(JACKTEST_WITHJACK_JAR): $(JACKTEST_WITHJACK_SRC) $(BOOTCLASSPATH_LIBS) $(JACKTEST_LIB_JAR) $(PRIVATE_TEST_MK) $(jack_test_with_jack_sources_list) $(JACK_JAR)
	$(transform-host-java-to-package-with-jack)

$(JACKTEST_LINK_DEX): PRIVATE_BOOTCLASSPATH := $(local_bootclasspath_jack)
$(JACKTEST_LINK_DEX): PRIVATE_CLASSPATH :=
$(JACKTEST_LINK_DEX): PRIVATE_JAVA_SOURCES := $(JACKTEST_LINK_SRC)
$(JACKTEST_LINK_DEX): PRIVATE_ARGS := $(JACKTEST_ARGS) $(JACKTEST_SOURCE_JAVA7)
$(JACKTEST_LINK_DEX): $(JACKTEST_LINK_SRC) $(PRIVATE_TEST_MK) $(BOOTCLASSPATH_LIBS) $(JACK_JAR)
	$(hide) mkdir -p $(dir $@)
	$(hide) $(JACK) $(PRIVATE_ARGS) $(PRIVATE_BOOTCLASSPATH) $(addprefix -cp ,$(PRIVATE_CLASSPATH)) -o $(dir $@) --ecj -nowarn \
		 $(PRIVATE_JAVA_SOURCES)

$(JACKTEST_WITHJACK_DEX): PRIVATE_BOOTCLASSPATH := $(local_bootclasspath_jack)
$(JACKTEST_WITHJACK_DEX): PRIVATE_CLASSPATH := $(call normalize-path-list,$(JACKTEST_LIB_JAR))
$(JACKTEST_WITHJACK_DEX): PRIVATE_JAVA_SOURCES := $(JACKTEST_WITHJACK_SRC)
$(JACKTEST_WITHJACK_DEX): PRIVATE_ARGS := $(JACKTEST_ARGS) $(JACKTEST_SOURCE_JAVA7)
$(JACKTEST_WITHJACK_DEX): $(JACKTEST_WITHJACK_SRC) $(BOOTCLASSPATH_LIBS) $(PRIVATE_TEST_MK) $(JACKTEST_LIB_JAR) $(JACK_JAR)
	$(hide) mkdir -p $(dir $@)
	$(hide) $(JACK) $(PRIVATE_ARGS) -o $(dir $@) $(PRIVATE_BOOTCLASSPATH) $(addprefix -cp ,$(PRIVATE_CLASSPATH)) --ecj -nowarn \
		$(PRIVATE_JAVA_SOURCES)

$(JACKTEST_WITHDX_JAR): PRIVATE_JACK_DEBUG_FLAGS := -g
$(JACKTEST_WITHDX_JAR): PRIVATE_JAVA_SOURCES := $(JACKTEST_WITHDX_SRC)
$(JACKTEST_WITHDX_JAR): PRIVATE_STATIC_JAVA_LIBRARIES :=
$(JACKTEST_WITHDX_JAR): PRIVATE_ALL_JAVA_LIBRARIES := $(JACKTEST_LIB_JAR) $(JACKTEST_WITHJACK_JAR)
$(JACKTEST_WITHDX_JAR): PRIVATE_SOURCE_INTERMEDIATES_DIR :=
$(JACKTEST_WITHDX_JAR): PRIVATE_MODULE := jacktest_$(JACKTEST_MODULE)_withdx
$(JACKTEST_WITHDX_JAR): PRIVATE_CLASS_INTERMEDIATES_DIR := $(JACKTEST_INTERMEDIATE_DIR)/withdx/classes
$(JACKTEST_WITHDX_JAR): PRIVATE_BOOTCLASSPATH := $(local_bootclasspath)
$(JACKTEST_WITHDX_JAR): PRIVATE_SOURCES_LIST := $(jack_test_with_dx_sources_list)
$(JACKTEST_WITHDX_JAR): PRIVATE_JAVACFLAGS := -nowarn
$(JACKTEST_WITHDX_JAR): $(JACKTEST_WITHDX_SRC) $(BOOTCLASSPATH_LIBS) $(JACKTEST_LIB_JAR) $(JACKTEST_WITHJACK_JAR) $(PRIVATE_TEST_MK) $(jack_test_with_dx_sources_list) $(JACK_JAR)
	$(transform-host-java-to-package-with-jack)

$(JACKTEST_WITHDX_DEX): PRIVATE_MODULE := jacktest_$(JACKTEST_MODULE)_withdx
$(JACKTEST_WITHDX_DEX): PRIVATE_INTERMEDIATES_DIR := $(JACKTEST_INTERMEDIATE_DIR)/withdx
$(JACKTEST_WITHDX_DEX): PRIVATE_DX_FLAGS :=
$(JACKTEST_WITHDX_DEX): $(JACKTEST_WITHDX_JAR) | $(DX_ORIGIN)
	$(transform-classes.jar-to-dex-with-dx-origin)

# Make sure that this JACKTEST_MODULE is unique.
jackmodule_id := JACKMODULE.$(JACKTEST_MODULE)
ifdef $(jackmodule_id)
$(error $(LOCAL_PATH): $(jackmodule_id) already defined by $($(jackmodule_id)))
endif
$(jackmodule_id) := $(LOCAL_PATH)

.PHONY: test-jack-$(JACKTEST_MODULE)
test-jack-$(JACKTEST_MODULE): PRIVATE_CLASSPATH := $(JACKTEST_WITHDX_DEX) $(JACKTEST_LINK_DEX) $(JACKTEST_WITHJACK_DEX) $(JACKTEST_LIB_DEX) $(junit.dex)
test-jack-$(JACKTEST_MODULE): PRIVATE_JUNIT := $(JACKTEST_JUNIT)
test-jack-$(JACKTEST_MODULE): PRIVATE_DALVIK_FLAGS := $(JACKTEST_DALVIK_FLAGS)
test-jack-$(JACKTEST_MODULE): PRIVATE_MODULE := $(JACKTEST_MODULE)
test-jack-$(JACKTEST_MODULE): $(JACKTEST_WITHDX_DEX) $(JACKTEST_WITHJACK_DEX) $(JACKTEST_LIB_DEX) $(JACKTEST_LINK_DEX) $(BOOTCLASSPATH_DEX) | $(junit.dex)
	$(hide) mkdir -p /tmp/android-data/dalvik-cache
	$(hide) find /tmp/android-data/ -name "*$(subst /,@,$(PRIVATE_MODULE))*.dex" | xargs rm -f
ifneq ($(ART_ANDROID_BUILD_TOP),)
	ANDROID_BUILD_TOP=$(ART_ANDROID_BUILD_TOP) ANDROID_HOST_OUT=$(ART_ANDROID_BUILD_TOP)/out/host/linux-x86 $(ART_ANDROID_BUILD_TOP)/out/host/linux-x86/bin/art $(PRIVATE_DALVIK_FLAGS) -classpath $(call normalize-path-list,$(PRIVATE_CLASSPATH)) org.junit.runner.JUnitCore $(PRIVATE_JUNIT) && echo $@ PASSED || (echo $@ FAILED with ART; exit 42)
else
	$(hide) dalvik $(PRIVATE_DALVIK_FLAGS) -Xint:fast -classpath $(call normalize-path-list,$(PRIVATE_CLASSPATH)) org.junit.runner.JUnitCore $(PRIVATE_JUNIT) \
          || (echo $@ FAILED with fast interpreter; exit 42)
	$(hide) dalvik $(PRIVATE_DALVIK_FLAGS) -classpath $(call normalize-path-list,$(PRIVATE_CLASSPATH)) org.junit.runner.JUnitCore $(PRIVATE_JUNIT) \
          && echo $@ PASSED || (echo $@ FAILED with JIT; exit 42)
endif
