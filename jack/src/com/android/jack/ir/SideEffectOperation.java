/*
 * Copyright (C) 2012 The Android Open Source Project
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

import com.android.jack.ir.ast.JPostfixDecOperation;
import com.android.jack.ir.ast.JPostfixIncOperation;
import com.android.jack.ir.ast.JPrefixDecOperation;
import com.android.jack.ir.ast.JPrefixIncOperation;
import com.android.sched.item.AbstractComponent;
import com.android.sched.item.ComposedOf;
import com.android.sched.item.Description;

/**
 * Represents binary or unary operation that have a side effect.
 */
@ComposedOf({CompoundAssignment.class, JPrefixIncOperation.class, JPrefixDecOperation.class,
  JPostfixDecOperation.class, JPostfixIncOperation.class})
@Description("Represents binary or unary operation that have a side effect.")
public class SideEffectOperation implements AbstractComponent {
}
