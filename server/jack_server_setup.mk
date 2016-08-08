#
# Copyright (C) 2016 The Android Open Source Project
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
LOCAL_PATH:= $(call my-dir)

ifneq ($(ANDROID_JACK_VM_ARGS),)
jack_vm_args := $(ANDROID_JACK_VM_ARGS)
else
jack_vm_args := -Dfile.encoding=UTF-8 -XX:+UseG1GC
endif

ifdef JACK_SERVER
ifneq ($(JACK_SERVER),true)
jack_server_disabled := true
endif
endif

.PHONY: setup-jack-server
setup-jack-server : PRIVATE_JACK_ADMIN := $(LOCAL_PATH)/jack-server/etc/jack-admin
setup-jack-server: $(JACK) $(LOCAL_PATH)/jack-server/etc/jack-admin $(HOST_OUT_JAVA_LIBRARIES)/jack-launcher.jar $(HOST_OUT_JAVA_LIBRARIES)/jack-server.jar $(HOST_OUT_JAVA_LIBRARIES)/jack.jar
ifndef jack_server_disabled
	@echo Ensure Jack server is installed and started
ifneq ($(dist_goal),)
	$(hide) $(PRIVATE_JACK_ADMIN) stop-server 2>&1 || (exit 0)
	$(hide) $(PRIVATE_JACK_ADMIN) kill-server 2>&1 || (exit 0)
	$(hide) $(PRIVATE_JACK_ADMIN) uninstall-server 2>&1 || (exit 0)
endif
	$(hide) $(PRIVATE_JACK_ADMIN) install-server $(HOST_OUT_JAVA_LIBRARIES)/jack-launcher.jar $(HOST_OUT_JAVA_LIBRARIES)/jack-server.jar 2>&1 || (exit 0)
ifneq ($(dist_goal),)
	$(hide) mkdir -p "$(DIST_DIR)/logs/jack/"
	$(hide) JACK_SERVER_VM_ARGUMENTS="$(jack_vm_args) -Dcom.android.jack.server.log.file=$(abspath $(DIST_DIR))/logs/jack/jack-server-%u-%g.log" $(PRIVATE_JACK_ADMIN) start-server 2>&1 || exit 0
else
	$(hide) JACK_SERVER_VM_ARGUMENTS="$(jack_vm_args)" $(PRIVATE_JACK_ADMIN) start-server 2>&1 || exit 0
endif
	$(hide) $(PRIVATE_JACK_ADMIN) force-update server $(HOST_OUT_JAVA_LIBRARIES)/jack-server.jar 2>&1 || exit 0
	$(hide) $(PRIVATE_JACK_ADMIN) update jack $(HOST_OUT_JAVA_LIBRARIES)/jack.jar || exit 47;
endif # jack_server_disabled

