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

.class public com/android/jack/jill/test001/jack/External
.super java/lang/Object

.method public <init>()V
	aload_0
	invokespecial java/lang/Object/<init>()V
	return
.end method

.method public sub1(II)I
        .limit locals 3
        .limit stack 3
	iload_1
    aload_0
	iload_2
	swap
	pop
	isub
	ireturn
.end method

.method public sub2(II)I
        .limit locals 3
        .limit stack 3
	iload_1
	dup
	pop
	iload_2
	dup
	pop
	isub
	ireturn
.end method


.method public sub3(II)I
        .limit locals 3
        .limit stack 4
	iload_1
	iload_2
	aload_0
	dup_x1
	pop
	swap
	pop
	isub
	ireturn
.end method

.method public sub4(II)I
        .limit locals 3
        .limit stack 4
	iload_1
	iload_2
	aload_0
	dup_x2
	pop
	isub
	swap
	pop
	ireturn
.end method

.method public sub5(II)I
        .limit locals 3
        .limit stack 5
	iload_1
	dconst_0
	iload_2
	dup_x2
	pop
	pop2
	isub
	ireturn
.end method

.method public sub6(II)I
        .limit locals 3
        .limit stack 5
	iload_1
	iload_2
	aload_0
	dup2
	pop
	pop
	pop
	isub
	ireturn
.end method

.method public sub7(II)I
        .limit locals 3
        .limit stack 6
	iload_1
	iload_2
	dconst_1
	dup2
	pop2
	pop2
	isub
	ireturn
.end method

.method public sub8(II)I
        .limit locals 3
        .limit stack 5
	iload_1
	iload_2
	aload_0
	dup2_x1
	pop
	isub
	swap
	pop
	swap
	pop
	ireturn
.end method

.method public sub9(II)I
        .limit locals 3
        .limit stack 5
	iload_1
	dconst_1
	dup2_x1
	pop2
	iload_2
	isub
	ireturn
.end method

.method public sub10(II)I
        .limit locals 3
        .limit stack 6
	iload_1
	iload_1
	iload_2
	aload_0
	dup2_x2
	pop
	isub
	ireturn
.end method

.method public sub11(II)I
        .limit locals 3
        .limit stack 6
	iload_1
	iload_2
	dconst_1
	dup2_x2
	pop2
	isub
	ireturn
.end method

.method public sub12(II)I
        .limit locals 3
        .limit stack 6
    dconst_1
	iload_1
	iload_2
	dup2_x2
	isub
	ireturn
.end method

.method public sub13(II)I
        .limit locals 3
        .limit stack 8
	iload_1
	iload_2
	dconst_1
	dconst_0
	dup2_x2
	pop2
	pop2
	pop2
	isub
	ireturn
.end method