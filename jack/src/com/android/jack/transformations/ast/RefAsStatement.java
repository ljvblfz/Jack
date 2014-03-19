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

package com.android.jack.transformations.ast;

import com.android.sched.item.Description;
import com.android.sched.item.Tag;

/**
 * The tag {code JVariableRefAsStatement} allows to know that the code may contain
 * {@link com.android.jack.ir.ast.JVariableRef JVariableRef} as
 * {@link com.android.jack.ir.ast.JArrayRef JArrayRef} as
 * {@link com.android.jack.ir.ast.JExpressionStatement JExpressionStatement}.
 */
@Description("Code may contains JVariableRef or JArrayRef as JExpressionStatement.")
public final class RefAsStatement implements Tag {
}