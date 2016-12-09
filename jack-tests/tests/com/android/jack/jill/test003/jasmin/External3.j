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

.class public com/android/jack/jill/test003/jack/External3
.super java/lang/Object
.implements com/android/jack/jill/test003/jack/GreaterThan

.method public <init>()V
    aload_0
    invokespecial java/lang/Object/<init>()V
    return
.end method

.method public greaterThan(DD)Z
    .limit locals 5
    .limit stack 4
    dload_1
    dload_3
    dcmpl
    iconst_1
    iconst_2
    iadd
    swap
    ifle if_label
    pop
    iconst_1
    ireturn
if_label:
    pop
    iconst_0
    ireturn
.end method