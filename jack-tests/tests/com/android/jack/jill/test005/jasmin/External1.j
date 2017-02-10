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

.class public com/android/jack/jill/test005/jack/External1
.super java/lang/Object

.method public <init>()V
    aload_0
    invokespecial java/lang/Object/<init>()V
    return
.end method

.method public getValueInt1()I
    .limit locals 1
    .limit stack 2
    iconst_1
    dup
    pop
    ireturn
.end method

.method public getValueInt2()I
    .limit locals 1
    .limit stack 2
    iconst_1
    dup
    iadd
    ireturn
.end method

.method public getValueInt3()I
    .limit locals 1
    .limit stack 2
    iconst_2
    iconst_3
    swap
    pop
    ireturn
.end method

.method public getValueIntMinus2()I
    .limit locals 1
    .limit stack 4
    iconst_1
    iconst_2
    dup2
    isub
    isub
    isub
    ireturn
.end method

.method public getValueInt4()I
    .limit locals 1
    .limit stack 4
    iconst_1
    iconst_2
    iconst_3
    dup_x1
    pop2
    iadd
    ireturn
.end method

.method public getValueInt5()I
    .limit locals 1
    .limit stack 5
    iconst_1
    iconst_2
    iconst_3
    dup2_x1
    pop2
    pop
    iadd
    ireturn
.end method

.method public getValueInt6()I
    .limit locals 1
    .limit stack 5
    lconst_0
    iconst_4
    dup_x2
    pop
    pop2
    iconst_2
    iadd
    ireturn
.end method

.method public getValueLong2()J
    .limit locals 1
    .limit stack 4
    lconst_1
    dup2
    ladd
    lreturn
.end method

.method public getValueLong1()J
    .limit locals 1
    .limit stack 4
    lconst_1
    dup2
    pop2
    lreturn
.end method

.method public getValueLong1Bis()J
    .limit locals 1
    .limit stack 5
    iconst_2
    lconst_1
    dup2_x1
    pop2
    pop
    lreturn
.end method

.method public getValueLong2()J
    .limit locals 1
    .limit stack 4
    lconst_0
    lconst_1
    dup2_x2
    pop2
    pop2
    lconst_1
    ladd
    lreturn
.end method