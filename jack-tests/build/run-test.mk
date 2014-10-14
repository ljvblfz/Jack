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


ifneq ($(strip $(JACKTEST_LIB_SRC)),)
include $(CLEAR_VARS)
LOCAL_MODULE := jacktest_$(JACKTEST_MODULE)_lib
LOCAL_USE_JACK := true
LOCAL_SRC_FILES := $(JACKTEST_LIB_SRC)
LOCAL_MODULE_TAGS := optional
LOCAL_JAVACFLAGS := -nowarn
LOCAL_JAVA_LIBRARIES := junit4-hostdex-jack
include $(BUILD_HOST_DALVIK_JAVA_LIBRARY)
JACKTEST_LIB_DEX := $(LOCAL_BUILT_MODULE)
JACK_TEST_LIB_NAME := $(LOCAL_MODULE)
else
JACKTEST_LIB_DEX :=
JACK_TEST_LIB_NAME :=
endif

ifneq ($(strip $(JACKTEST_LINK_SRC)),)
include $(CLEAR_VARS)
LOCAL_MODULE := jacktest_$(JACKTEST_MODULE)_link
LOCAL_USE_JACK := true
LOCAL_SRC_FILES := $(JACKTEST_LINK_SRC)
LOCAL_MODULE_TAGS := optional
LOCAL_JAVACFLAGS := -nowarn
LOCAL_JAVA_LIBRARIES := junit4-hostdex-jack
include $(BUILD_HOST_DALVIK_JAVA_LIBRARY)
JACKTEST_LINK_DEX := $(LOCAL_BUILT_MODULE)
else
JACKTEST_LINK_DEX :=
endif

ifneq ($(strip $(JACKTEST_WITHJACK_SRC)),)
include $(CLEAR_VARS)
LOCAL_MODULE := jacktest_$(JACKTEST_MODULE)_withjack
LOCAL_USE_JACK := true
LOCAL_SRC_FILES := $(JACKTEST_WITHJACK_SRC)
LOCAL_MODULE_TAGS := optional
LOCAL_JAVACFLAGS := -nowarn
LOCAL_JACK_FLAGS := $(JACKTEST_ARGS) $(JACKTEST_SOURCE_JAVA7)
LOCAL_JAVA_LIBRARIES := junit4-hostdex-jack $(JACK_TEST_LIB_NAME)
include $(BUILD_HOST_DALVIK_JAVA_LIBRARY)
JACKTEST_WITHJACK_DEX := $(LOCAL_BUILT_MODULE)
JACK_TEST_JACK_NAME := $(LOCAL_MODULE)
else
JACKTEST_WITHJACK_DEX :=
JACK_TEST_JACK_NAME :=
endif

ifneq ($(strip $(JACKTEST_WITHDX_SRC)),)
include $(CLEAR_VARS)
LOCAL_MODULE := jacktest_$(JACKTEST_MODULE)_withdx
LOCAL_USE_JACK := false
LOCAL_SRC_FILES := $(JACKTEST_WITHDX_SRC)
LOCAL_MODULE_TAGS := optional
LOCAL_JAVACFLAGS := -nowarn
LOCAL_JAVA_LIBRARIES := junit4-hostdex-jack $(JACK_TEST_LIB_NAME) $(JACK_TEST_JACK_NAME)
include $(BUILD_HOST_DALVIK_JAVA_LIBRARY)
JACKTEST_WITHDX_DEX := $(LOCAL_BUILT_MODULE)
else
JACKTEST_WITHDX_DEX :=
endif


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
test-jack-$(JACKTEST_MODULE): $(JACKTEST_WITHDX_DEX) $(JACKTEST_WITHJACK_DEX) $(JACKTEST_LIB_DEX) $(JACKTEST_LINK_DEX) $(BOOTCLASSPATH_DEX) | $(junit.dex) $(HOST_OUT_EXECUTABLES)/art
	$(hide) mkdir -p /tmp/android-data/dalvik-cache
	$(hide) find /tmp/android-data/ -name "*$(subst /,@,$(PRIVATE_MODULE))*.dex" | xargs rm -f
ifneq ($(ART_ANDROID_BUILD_TOP),)
	ANDROID_BUILD_TOP=$(ART_ANDROID_BUILD_TOP) ANDROID_HOST_OUT=$(ART_ANDROID_BUILD_TOP)/out/host/linux-x86 $(ART_ANDROID_BUILD_TOP)/out/host/linux-x86/bin/art $(PRIVATE_DALVIK_FLAGS) -classpath $(call normalize-path-list,$(PRIVATE_CLASSPATH)) org.junit.runner.JUnitCore $(PRIVATE_JUNIT) && echo $@ PASSED || (echo $@ FAILED with ART; exit 42)
else
	$(hide) art $(PRIVATE_DALVIK_FLAGS) -classpath $(call normalize-path-list,$(PRIVATE_CLASSPATH)) org.junit.runner.JUnitCore $(PRIVATE_JUNIT) \
          || (echo $@ FAILED; exit 42)
endif
