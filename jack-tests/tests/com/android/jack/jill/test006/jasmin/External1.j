; Copyright (C) 2017 The Android Open Source Project
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

.class public com/android/jack/jill/test006/jack/External1
.super java/lang/Object

.method public <init>()V
    aload_0
    invokespecial java/lang/Object/<init>()V
    return
.end method

.method private sum(II)I
    .limit locals 3
    .limit stack 2
    iload_1
    iload_2
    iadd
    ireturn
.end method

.method public test(I)I
    .limit locals 2
    .limit stack 3
    aload_0
    iconst_3
    iload_1
    lookupswitch
      1 : Label1
      2 : Label2
      default : LabelDefault
    Label1:
      iconst_1
      goto Next
    Label2:
      iconst_2
      goto Next
    LabelDefault:
      iconst_0
    Next:
    invokespecial com/android/jack/jill/test006/jack/External1/sum(II)I
    ireturn
.end method

.method public test2(I)I
    .limit locals 2
    .limit stack 3
    aload_0
    iconst_3
    iload_1
    tableswitch 0
      LabelDefault
      Label1
      Label2
      default : LabelDefault
    Label1:
      iconst_1
      goto Next
    Label2:
      iconst_2
      goto Next
    LabelDefault:
      iconst_0
    Next:
    invokespecial com/android/jack/jill/test006/jack/External1/sum(II)I
    ireturn
.end method
