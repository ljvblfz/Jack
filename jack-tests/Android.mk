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

private_jack_tests_mk := $(lastword $(MAKEFILE_LIST))

LOCAL_PATH:= $(call my-dir)

test-jack-incremental: $(JACK_JAR)
	$(hide) $(eval TEMPDIR := $(shell mktemp -d))
	$(hide) $(JACK) -D jack.jackfile.generate=true -D jack.jackfile.output.container=dir \
	-D jack.jackfile.output.dir=$(TEMPDIR)/jackIncrementalOutput -o \
	$(TEMPDIR)/coreDexFromJava.dex \
	--ecj -nowarn @$(ANDROID_BUILD_TOP)/out/target/common/obj/JAVA_LIBRARIES/core_intermediates/sources.list
	$(hide) $(JACK) \
	--output $(TEMPDIR)/coreDexFromJack.dex --import-jack $(TEMPDIR)/jackIncrementalOutput
	$(hide) dexdump -d $(TEMPDIR)/coreDexFromJava.dex | tail -n +3 &> $(TEMPDIR)/coreDexFromJava.dex.txt
	$(hide) dexdump -d $(TEMPDIR)/coreDexFromJack.dex | tail -n +3 &> $(TEMPDIR)/coreDexFromJack.dex.txt
	$(hide) diff --side-by-side --suppress-common-lines $(TEMPDIR)/coreDexFromJava.dex.txt \
	$(TEMPDIR)/coreDexFromJack.dex.txt
	$(hide) rm -rf $(TEMPDIR)

.PHONY: test-jack
test-jack: test-jack-unit test-jack-incremental

.PHONY: test-jack-all
test-jack-all: test-jack-unit-all test-jack-incremental test-jack-regression

#################
# tests executing the created dex
#################
JACK_PATH := $(LOCAL_PATH)
include $(LOCAL_PATH)/build/run-test-common.mk

# tests suites

# Annotation
$(call declare-test-with-name,annotation/test001)
$(call declare-test-with-name,annotation/test002)
$(call declare-test-with-name,annotation/test003)
# Annotation on package are not supported in dex format: http://code.google.com/p/android/issues/detail?id=16149
#$(call declare-test-with-name,annotation/test005)
$(call declare-test-with-name,annotation/test006)
$(call declare-test-with-name,annotation/test007)
$(call declare-test-with-name,annotation/test008)
$(call declare-test-with-name,annotation/test009)

# Arithmetic
$(call declare-test-with-name,arithmetic/test001)
$(call declare-test-with-name,arithmetic/test002)
$(call declare-test-with-name,arithmetic/test003)
$(call declare-test-with-name,arithmetic/test004)

# Array
$(call declare-test-with-name,array/test001)

# Assertions
# this test must be run with assertions enabled (for now, use dalvik)
#$(call declare-test-with-name,assertion/test001)
$(call declare-test-with-name,assertion/test002)
$(call declare-test-with-name,assertion/test003)

# Assign
$(call declare-test-with-name,assign)

# Box
$(call declare-test-with-name,box/test001)

# Bridge
$(call declare-test-with-name,bridge/test001)
$(call declare-test-with-name,bridge/test002)
$(call declare-test-with-name,bridge/test003)
$(call declare-test-with-name,bridge/test004)
$(call declare-test-with-name,bridge/test005)
$(call declare-test-with-name,bridge/test006)
$(call declare-test-with-name,bridge/test007)

# Cast
$(call declare-test-with-name,cast/explicit001)
$(call declare-test-with-name,cast/implicit001)
$(call declare-test-with-name,cast/implicit002)
$(call declare-test-with-name,cast/implicit003)
$(call declare-test-with-name,cast/implicit004)
$(call declare-test-with-name,cast/useless001)
$(call declare-test-with-name,cast/useless002)

# Comparisons
$(call declare-test-with-name,comparison/test001)

# Constant
$(call declare-test-with-name,constant/test001)
$(call declare-test-with-name,constant/test002)
$(call declare-test-with-name,constant/test003)
$(call declare-test-with-name,constant/test004)
$(call declare-test-with-name,constant/test005)
$(call declare-test-with-name,constant/test006)
$(call declare-test-with-name,constant/test007)
$(call declare-test-with-name,constant/clazz)

# Debug info
$(call declare-test,$(LOCAL_PATH)/tests/com/android/jack/debug/test001/test.mk)
$(call declare-test-with-name,debug/test002)
$(call declare-test-with-name,debug/test004)

# Dx compiler and optimizer tests
$(call declare-test-with-name,dx/compiler)
$(call declare-test-with-name,dx/optimizer)
$(call declare-test-with-name,dx/overlapping)

# Enum
$(call declare-test-with-name,enums/test001)
$(call declare-test-with-name,enums/test002)
$(call declare-test,$(LOCAL_PATH)/tests/com/android/jack/enums/test003/test.mk)

# Conditional
$(call declare-test-with-name,conditional/test001)
$(call declare-test-with-name,conditional/test002)
$(call declare-test-with-name,conditional/test003)
$(call declare-test-with-name,conditional/test004)
$(call declare-test-with-name,conditional/test005)
$(call declare-test-with-name,conditional/test006)
$(call declare-test-with-name,conditional/test007)

# External
$(call declare-test-with-name,external/test001)

# Field
$(call declare-test-with-name,field/static001)
$(call declare-test-with-name,field/static002)
$(call declare-test-with-name,field/static004)
$(call declare-test-with-name,field/static005)
$(call declare-test-with-name,field/instance001)
$(call declare-test-with-name,field/instance002)
$(call declare-test-with-name,field/instance003)
$(call declare-test-with-name,field/instance004)

# Finally
$(call declare-test-with-name,tryfinally/finallyblock)
$(call declare-test-with-name,tryfinally/finally002)
$(call declare-test-with-name,tryfinally/finally003)
$(call declare-test-with-name,tryfinally/finally004)

# Fibonacci
$(call declare-test-with-name,fibonacci)

# Flow
$(call declare-test-with-name,flow/loop)
$(call declare-test-with-name,flow/cfg001)

# If
$(call declare-test-with-name,ifstatement/simpleTest)
$(call declare-test-with-name,ifstatement/advancedTest)
$(call declare-test-with-name,ifstatement/cfgTest)
$(call declare-test-with-name,ifstatement/fastpath)

# Init
$(call declare-test-with-name,init/test002)

# Inner
$(call declare-test-with-name,inner/test001)
$(call declare-test-with-name,inner/test002)
$(call declare-test-with-name,inner/test003)
$(call declare-test-with-name,inner/test004)
$(call declare-test-with-name,inner/test005)
$(call declare-test-with-name,inner/test006)
$(call declare-test-with-name,inner/test007)
$(call declare-test-with-name,inner/test008)
$(call declare-test-with-name,inner/test009)
$(call declare-test-with-name,inner/test010)
$(call declare-test-with-name,inner/test011)
$(call declare-test-with-name,inner/test012)
$(call declare-test-with-name,inner/test013)
$(call declare-test-with-name,inner/test014)
$(call declare-test-with-name,inner/test015)
$(call declare-test-with-name,inner/test016)
$(call declare-test-with-name,inner/test017)
$(call declare-test-with-name,inner/test018)
$(call declare-test-with-name,inner/test019)
$(call declare-test-with-name,inner/test020)
$(call declare-test-with-name,inner/test021)
$(call declare-test-with-name,inner/test022)
$(call declare-test-with-name,inner/test023)
$(call declare-test-with-name,inner/test024)
$(call declare-test-with-name,inner/test026)

# Invoke
$(call declare-test-with-name,invoke/test001)
$(call declare-test-with-name,invoke/test002)
$(call declare-test-with-name,invoke/test003)
$(call declare-test-with-name,invoke/test004)
$(call declare-test-with-name,invoke/test005)
$(call declare-test-with-name,invoke/test006)

# Jarjar
$(call declare-test,$(LOCAL_PATH)/tests/com/android/jack/jarjar/test001/test.mk)
$(call declare-test,$(LOCAL_PATH)/tests/com/android/jack/jarjar/test003/test.mk)

# Newarray
$(call declare-test-with-name,newarray/test001)
$(call declare-test-with-name,newarray/test002)
$(call declare-test-with-name,newarray/test003)
$(call declare-test-with-name,newarray/test004)

# Not simplifier
$(call declare-test-with-name,optimizations/notsimplifier/test001)
$(call declare-test-with-name,optimizations/exprsimplifier/test001)

# Opcode
$(call declare-test,$(LOCAL_PATH)/tests/com/android/jack/opcodes/test-opcodes.mk)

# Order
$(call declare-test-with-name,order)

# Return
$(call declare-test-with-name,returnstatement/returnvoid)
$(call declare-test-with-name,returnstatement/returns)

# Shrob
#$(call declare-test,$(LOCAL_PATH)/tests/com/android/jack/shrob/test011/test.mk)
$(call declare-test,$(LOCAL_PATH)/tests/com/android/jack/shrob/test011/test2.mk)
$(call declare-test,$(LOCAL_PATH)/tests/com/android/jack/shrob/test016/test.mk)
$(call declare-test,$(LOCAL_PATH)/tests/com/android/jack/shrob/test025/test.mk)
$(call declare-test,$(LOCAL_PATH)/tests/com/android/jack/shrob/test030/test.mk)

# String
$(call declare-test-with-name,string/concat001)
$(call declare-test-with-name,string/concat002)
$(call declare-test-with-name,string/concat003)

# Switches
$(call declare-test-with-name,switchstatement/test001)
$(call declare-test-with-name,switchstatement/test002)
$(call declare-test-with-name,switchstatement/test003)
# TODO uncomment when bug 7439926 is corrected
#$(call declare-test-with-name,switchstatement/test004)
$(call declare-test-with-name,switchstatement/test005)
$(call declare-test-with-name,switchstatement/test006)
$(call declare-test-with-name,switchstatement/test007)
$(call declare-test-with-name,switchstatement/test008)

# Synchronize
$(call declare-test-with-name,synchronize/test001)

# Three Address
$(call declare-test-with-name,threeaddress)

# Throws
$(call declare-test-with-name,throwstatement/test001)

# Try/Catch
$(call declare-test-with-name,trycatch/test001)
$(call declare-test-with-name,trycatch/test002)
$(call declare-test-with-name,trycatch/test003)
$(call declare-test-with-name,trycatch/test005)

# Type
$(call declare-test-with-name,type/test001)
$(call declare-test-with-name,type/test002)

# Unary
$(call declare-test-with-name,unary/test001)
$(call declare-test-with-name,unary/test002)
$(call declare-test-with-name,unary/test003)
$(call declare-test-with-name,unary/test004)

# Verify
$(call declare-test-with-name,verify/test001)

#Java7
$(call declare-java7-test-with-name,java7/switches/test001)
$(call declare-java7-test-with-name,java7/switches/test002)
$(call declare-java7-test-with-name,java7/switches/test003)
$(call declare-java7-test-with-name,java7/exceptions/test001)
$(call declare-java7-test-with-name,java7/exceptions/test002)
$(call declare-java7-test-with-name,java7/exceptions/test003)
$(call declare-java7-test-with-name,java7/exceptions/test004)
$(call declare-java7-test-with-name,java7/exceptions/test005)
$(call declare-java7-test-with-name,java7/trywithresources/test001)
$(call declare-java7-test-with-name,java7/trywithresources/test002)
$(call declare-java7-test-with-name,java7/parser/literals/test001)
$(call declare-java7-test-with-name,java7/parser/literals/test002)

# define global regression test
include $(JACK_CLEAR_VARS)

JACKTEST_MODULE := regression
JACKTEST_WITHJACK_SRC := $(JACKREGRESSIONTEST_WITHJACK_SRC)
JACKTEST_WITHDX_SRC := $(JACKREGRESSIONTEST_WITHDX_SRC)
JACKTEST_LIB_SRC := $(JACKREGRESSIONTEST_LIB_SRC)
JACKTEST_LINK_SRC := $(JACKREGRESSIONTEST_LINK_SRC)
JACKTEST_JUNIT := $(JACKREGRESSIONTEST_JUNIT)
PRIVATE_TEST_MK := $(JACKREGRESSIONTEST_TEST_MK) $(private_jack_tests_mk)
JAVA_COMPILER := $(COMMON_JAVAC)

include $(JACK_RUN_TEST)
test-jack: test-jack-$(JACKTEST_MODULE)
	$(hide) echo test-jack: PASSED
ifeq ($(JAVA7_COMPILER),)
	$(hide) echo test-jack: Missing Java 7 compiler, Java 7 tests Skipped, export Java 7 compiler into JAVA7_COMPILER
endif
