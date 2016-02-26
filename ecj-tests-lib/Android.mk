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

LOCAL_PATH:= $(call my-dir)


#
# ecj-tests-lib
#

include $(CLEAR_VARS)

LOCAL_MODULE := ecj-tests-lib

LOCAL_STATIC_JAVA_LIBRARIES := \
	ecj-tests-lib-ecj \
	ecj-tests-lib-equinox-common \
	ecj-tests-lib-jdt-core \
	ecj-tests-lib-jdt-core-tests-compiler \
	ecj-tests-lib-test-performance

include $(BUILD_HOST_JAVA_LIBRARY)

#
# ecj-tests-lib-ecj
#

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_PREBUILT_JAVA_LIBRARIES := \
	ecj-tests-lib-ecj:ecj-4.6M2$(COMMON_JAVA_PACKAGE_SUFFIX)

include $(BUILD_HOST_PREBUILT)

#
# ecj-tests-lib-equinox-common
#

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_PREBUILT_JAVA_LIBRARIES := \
	ecj-tests-lib-equinox-common:org.eclipse.equinox.common_3.8.0.v20150911-2106$(COMMON_JAVA_PACKAGE_SUFFIX)

include $(BUILD_HOST_PREBUILT)

#
# ecj-tests-lib-jdt-core
#

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_PREBUILT_JAVA_LIBRARIES := \
	ecj-tests-lib-jdt-core:org.eclipse.jdt.core_3.12.0.v20150913-1717$(COMMON_JAVA_PACKAGE_SUFFIX)

include $(BUILD_HOST_PREBUILT)

#
# ecj-tests-lib-jdt-core-tests-compiler
#

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_PREBUILT_JAVA_LIBRARIES := \
	ecj-tests-lib-jdt-core-tests-compiler:org.eclipse.jdt.core.tests.compiler_3.12.0.v20150913-1717$(COMMON_JAVA_PACKAGE_SUFFIX)

include $(BUILD_HOST_PREBUILT)

#
# ecj-tests-lib-test-performance
#

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_PREBUILT_JAVA_LIBRARIES := \
	ecj-tests-lib-test-performance:org.eclipse.test.performance_3.11.0.v20150223-0658$(COMMON_JAVA_PACKAGE_SUFFIX)

include $(BUILD_HOST_PREBUILT)
