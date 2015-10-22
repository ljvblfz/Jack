# Copyright (C) 2015 The Android Open Source Project
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

include $(BUILD_SYSTEM)/base_rules.mk

ifneq ($(ANDROID_JACK_VM_ARGS),)
jack_vm_args := $(ANDROID_JACK_VM_ARGS)
else
jack_vm_args := -Dfile.encoding=UTF-8 -XX:+TieredCompilation $(JAVA_TMPDIR_ARG)
endif
$(LOCAL_BUILT_MODULE): $(LOCAL_PATH)/$(LOCAL_SRC_FILES) $(HOST_OUT_JAVA_LIBRARIES)/jack-launcher.jar $(HOST_OUT_JAVA_LIBRARIES)/jack-server.jar $(jack_admin_script) $(JACK_JAR) | $(ACP)
	@echo "Build: $@"
ifneq ($(dist_goal),)
	$(hide) $(jack_admin_script) stop-server 2>&1 || (exit 0)
	$(hide) $(jack_admin_script) kill-server 2>&1 || (exit 0)
	$(hide) $(jack_admin_script) uninstall-server 2>&1 || (exit 0)
endif
	$(hide) $(jack_admin_script) install-server $(HOST_OUT_JAVA_LIBRARIES)/jack-launcher.jar $(HOST_OUT_JAVA_LIBRARIES)/jack-server.jar  2>&1 || (exit 0)
ifneq ($(dist_goal),)
	mkdir -p "$(DIST_DIR)/logs/jack/"
	$(hide) JACK_SERVER_VM_ARGUMENTS="$(jack_vm_args) -Dcom.android.jack.server.log.file=$(abspath $(DIST_DIR))/logs/jack/jack-server-%u-%g.log" $(jack_admin_script) start-server 2>&1 || exit 0
else
	$(hide) JACK_SERVER_VM_ARGUMENTS="$(jack_vm_args)" $(jack_admin_script) start-server 2>&1 || exit 0
endif
	$(hide) $(jack_admin_script) force-update server $(HOST_OUT_JAVA_LIBRARIES)/jack-server.jar
	$(hide) $(jack_admin_script) force-update jack $(JACK_JAR) $(patsubst $(PRIVATE_PATH)/jacks/jack-%.jar,%,$(JACK_JAR)) || exit 47
	$(copy-file-to-target)


include $(CLEAR_VARS)

LOCAL_MODULE := jack-server
LOCAL_MODULE_CLASS := JAVA_LIBRARIES
LOCAL_MODULE_TAGS := optional
LOCAL_JAVACFLAGS := -target 1.7 -source 1.7
LOCAL_SRC_FILES := $(call all-java-files-under,src)
LOCAL_STATIC_JAVA_LIBRARIES := \
  simple-jack \
  schedlib-norsc \
  allocation-jack \
  guava-jack \
  freemarker-jack \
  watchmaker-jack \
  maths-jack \
  antlr-runtime-jack \
  jack-api \
  jack-server-api
LOCAL_JAVA_LIBRARIES := \
  jsr305lib-jack

JACK_SERVER_JARJAR_RULES := $(LOCAL_PATH)/jarjar-rules.txt

LOCAL_JAVA_RESOURCE_DIRS  := rsc

JACK_SERVER_VERSION_FILE := $(call local-intermediates-dir,COMMON)/generated.version/jack-server-version.properties
LOCAL_JAVA_RESOURCE_FILES += $(JACK_SERVER_VERSION_FILE)

include $(BUILD_HOST_JAVA_LIBRARY)

$(JACK_SERVER_VERSION_FILE): $(TOP_DIR)$(LOCAL_PATH)/../version.properties | $(ACP)
	$(copy-file-to-target)

$(LOCAL_INSTALLED_MODULE): $(LOCAL_BUILT_MODULE)
	@echo JarJar and Install: $@
	$(hide) java -jar $(JARJAR) process $(JACK_SERVER_JARJAR_RULES) $< $@
