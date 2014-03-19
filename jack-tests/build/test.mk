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

private_path:= $(call my-dir)

JACKTEST_WITHJACK_SRC := $(call all-java-files-under, $(abspath $(JACKTEST_MODULE_PATH)/jack/))
JACKTEST_WITHJACK_FOLDER := $(abspath $(JACKTEST_MODULE_PATH)/jack/)
ifneq ($(wildcard $(JACKTEST_WITHJACK_FOLDER)),)
JACKTEST_WITHJACK_SRC := $(call all-java-files-under, $(JACKTEST_WITHJACK_FOLDER))
endif

JACKTEST_LINK_FOLDER := $(abspath $(JACKTEST_MODULE_PATH)/link/)
ifneq ($(wildcard $(JACKTEST_LINK_FOLDER)),)
JACKTEST_LINK_SRC := $(call all-java-files-under, $(JACKTEST_LINK_FOLDER))
endif

JACKTEST_WITHDX_SRC := $(call all-java-files-under, $(abspath $(JACKTEST_MODULE_PATH)/dx/))
JACKTEST_WITHDX_FOLDER := $(abspath $(JACKTEST_MODULE_PATH)/dx/)
ifneq ($(wildcard $(JACKTEST_WITHDX_FOLDER)),)
JACKTEST_WITHDX_SRC := $(call all-java-files-under, $(JACKTEST_WITHDX_FOLDER))
endif

JACKTEST_LIB_FOLDER := $(abspath $(JACKTEST_MODULE_PATH)/lib/)
ifneq ($(wildcard $(JACKTEST_LIB_FOLDER)),)
JACKTEST_LIB_SRC := $(call all-java-files-under, $(JACKTEST_LIB_FOLDER))
endif
JACKTEST_JUNIT := com.android.jack.$(subst /,.,$(JACKTEST_MODULE)).dx.Tests

# To run Jack tests also with Art, set ART_ANDROID_BUILD_TOP to the top of an android source tree
# which contains the already built Art that you want to test with
ART_ANDROID_BUILD_TOP :=

include $(JACK_RUN_TEST)
