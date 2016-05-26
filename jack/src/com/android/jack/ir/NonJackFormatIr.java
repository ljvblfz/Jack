/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.jack.ir;

import com.android.jack.backend.dex.annotations.tag.ReflectAnnotations;
import com.android.jack.ir.ast.JConcatOperation;
import com.android.jack.ir.ast.JContinueStatement;
import com.android.jack.ir.ast.JDoStatement;
import com.android.jack.ir.ast.JExceptionRuntimeValue;
import com.android.jack.ir.ast.JFieldInitializer;
import com.android.jack.ir.ast.JForStatement;
import com.android.jack.ir.ast.JPostfixDecOperation;
import com.android.jack.ir.ast.JPostfixIncOperation;
import com.android.jack.ir.ast.JPrefixDecOperation;
import com.android.jack.ir.ast.JPrefixIncOperation;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JWhileStatement;
import com.android.jack.transformations.ast.removeinit.FieldInitMethod;
import com.android.jack.transformations.ast.removeinit.FieldInitMethodCall;
import com.android.jack.transformations.enums.EnumMappingMarker;
import com.android.jack.transformations.enums.SwitchEnumSupport;
import com.android.sched.item.AbstractComponent;
import com.android.sched.item.ComposedOf;
import com.android.sched.item.Description;
import com.android.sched.item.Name;

/**
 * Tag containing all JNodes, tags or markers that are forbidden in the Jack format.
 */
@Name("Non-Jack format IR")
@Description("All JNodes, tags or markers that are forbidden in the Jack format.")
@ComposedOf({CompoundAssignment.class,
    EnumMappingMarker.class,
    FieldInitMethod.class,
    FieldInitMethodCall.class,
    JConcatOperation.class,
    JContinueStatement.class,
    JDoStatement.class,
    JExceptionRuntimeValue.class,
    JFieldInitializer.class,
    JForStatement.class,
    JPostfixDecOperation.class,
    JPostfixIncOperation.class,
    JPrefixDecOperation.class,
    JPrefixIncOperation.class,
    JSwitchStatement.SwitchWithEnum.class,
    JSwitchStatement.SwitchWithString.class,
    JWhileStatement.class,
    ReflectAnnotations.class,
    SwitchEnumSupport.UsedEnumField.class})
public class NonJackFormatIr implements AbstractComponent {
}
