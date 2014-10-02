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

# $(1): library name list
# $(2): Non-empty if IS_HOST_MODULE
define java-dex-files
$(foreach lib,$(1),$(call _java-lib-full-classes.dex,$(lib),$(2)))
endef

# $(1): library name
# $(2): Non-empty if IS_HOST_MODULE
define _java-lib-full-classes.dex
$(call _java-lib-dir,$(1),$(2))/classes.dex
endef

jack.intermediate := $(call intermediates-dir-for,JAVA_LIBRARIES,jack,1,COMMON)
BOOTCLASSPATH_MODULE := core-libart-hostdex junit4-hostdex-jack
BOOTCLASSPATH_LIBS := $(call java-lib-files,$(BOOTCLASSPATH_MODULE),true)
BOOTCLASSPATH_DEX := $(call java-dex-files,$(BOOTCLASSPATH_MODULE),true)
junit.dex = \
		$(call intermediates-dir-for,JAVA_LIBRARIES,junit4-hostdex-jack,1,COMMON)/javalib.jar

JACK_CLEAR_VARS := $(JACK_PATH)/build/clear_vars.mk
JACK_RUN_TEST := $(JACK_PATH)/build/run-test.mk

# $(1): mk declaring the test
define declare-test
$(eval PRIVATE_TEST_MK := $$(1)) \
$(eval include $$(1)) \
$(eval PRIVATE_TEST_MK :=)
endef

# $(1): name of the test module
define declare-test-with-name
$(eval include $(JACK_CLEAR_VARS)) \
$(eval JAVA_COMPILER := $(COMMON_JAVAC)) \
$(eval JACKTEST_MODULE := $$(1)) \
$(eval JACKTEST_MODULE_PATH := $(JACK_PATH)/tests/com/android/jack/$$(1)) \
$(eval include $(JACK_PATH)/build/test.mk)
endef

# $(1): name of the test module
define declare-java7-test-with-name
$(eval include $(JACK_CLEAR_VARS)) \
$(eval JACKTEST_SOURCE_JAVA7 := -D jack.java.source.version=1.7) \
$(eval JAVA_COMPILER := $(JAVA7_COMPILER)) \
$(eval JACKTEST_MODULE := $$(1)) \
$(eval JACKTEST_MODULE_PATH := $(JACK_PATH)/tests/com/android/jack/$$(1)) \
$(if $(JAVA7_COMPILER), $(eval include $(JACK_PATH)/build/test.mk), $(warning Ignoring test $(1), Missing Java 7 compiler, export Java 7 compiler into JAVA7_COMPILER))
endef

define transform-classes.jar-to-dex-with-dx-origin
@echo "target Dex: $(PRIVATE_MODULE)"
@mkdir -p $(dir $@)
$(hide) $(DX_ORIGIN) \
    --dex \
    --output=$@ \
    $(incremental_dex) \
    $(if $(NO_OPTIMIZE_DX), \
        --no-optimize) \
    $(if $(GENERATE_DEX_DEBUG), \
»	    --debug --verbose \
»	    --dump-to=$(@:.dex=.lst) \
»	    --dump-width=1000) \
    $(PRIVATE_DX_FLAGS) \
    $<
endef
