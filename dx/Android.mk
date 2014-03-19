# Copyright 2014 The Android Open Source Project
#
LOCAL_PATH := $(call my-dir)

subdirs := $(addprefix $(LOCAL_PATH)/,$(addsuffix /Android.mk, \
		src \
	))

include $(subdirs)
