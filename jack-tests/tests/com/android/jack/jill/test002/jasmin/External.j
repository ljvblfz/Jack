; Copyright (C) 2016 The Android Open Source Project
;
; Licensed under the Apache License, Version 2.0 (the "License");
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;      http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.

.class public com/android/jack/jill/test002/jack/External
.super java/lang/Object

.method public <init>()V
  aload_0
  invokespecial java/lang/Object/<init>()V
  return
.end method

.method public static testDeadIinc(Z)V
        .limit locals 2
        .limit stack 1
  ; Generate a dead-store to a local declared in an if block
  iload_0
  ifeq if_label
  iconst_0
  istore_1
  iinc 1 4       ; dead store to local 1

if_label:
  return
.end method

.method public static testDeadIstore(Z)V
        .limit locals 2
        .limit stack 1
  ; Generate a dead-store to a local declared in an if block
  iload_0
  ifeq if_label
  iconst_0
  istore_1       ; dead store to local 1

if_label:
  return
.end method
