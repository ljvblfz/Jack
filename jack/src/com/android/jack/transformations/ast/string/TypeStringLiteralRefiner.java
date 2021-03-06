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

import com.android.jack.Options;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JSession;
import com.android.jack.shrob.spec.FilterSpecification;
import com.android.jack.shrob.spec.Flags;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Access;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;
import com.android.sched.util.config.ThreadConfig;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Refine {@code JStringLiteral} in types into more specific string literals.
 */
@Description("Refine JStringLiteral in types into more specific string literals.")
@Use(StringLiteralRefinerVisitor.class)
// Uses StringLiteralRefinerVisitor which looks up types.
@Access(JSession.class)
@Transform(add = StringLiteralRefined.Type.class)
public class TypeStringLiteralRefiner implements RunnableSchedulable<JDefinedClassOrInterface> {

  @Nonnull
  private final List<FilterSpecification> adaptClassStrings =
      ThreadConfig.get(Options.FLAGS).getAdaptClassStrings();

  @Override
  public void run(@Nonnull JDefinedClassOrInterface type) {
    if (Flags.acceptClass(type, adaptClassStrings)) {
      TransformationRequest tr = new TransformationRequest(type);
      StringLiteralRefinerVisitor visitor = new StringLiteralRefinerVisitor(tr);
      visitor.accept(type.getAnnotations());
      tr.commit();
    }
  }

}
