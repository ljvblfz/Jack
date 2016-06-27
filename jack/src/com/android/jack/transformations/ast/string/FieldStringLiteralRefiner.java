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

package com.android.jack.transformations.ast.string;

import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JSession;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Access;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;

import javax.annotation.Nonnull;

/**
 * Refine {@code JStringLiteral} in fields into more specific string literals.
 */
@Description("Refine JStringLiteral in fields into more specific string literals.")
@Use(StringLiteralRefinerVisitor.class)
@Transform(add = StringLiteralRefined.Field.class)
// Uses StringLiteralRefinerVisitor which lookup types.
@Access(JSession.class)
public class FieldStringLiteralRefiner implements RunnableSchedulable<JField> {

  @Override
  public void run(@Nonnull JField field) {
    TransformationRequest tr = new TransformationRequest(field);
    StringLiteralRefinerVisitor visitor = new StringLiteralRefinerVisitor(tr);
    visitor.accept(field);
    tr.commit();
  }

}
