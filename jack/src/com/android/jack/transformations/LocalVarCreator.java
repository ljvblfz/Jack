 /*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License
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

package com.android.jack.transformations;

import com.android.jack.ir.SourceInfo;
import com.android.jack.ir.ast.JAbstractMethodBody;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JType;
import com.android.jack.transformations.request.AddJLocalInMethodBody;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.NamingTools;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * An helper class to synthesize temporary local variables in {@link JMethod}s.
 */
@Transform(add = {JLocal.class})
public class LocalVarCreator {

  @Nonnull
  private final JMethodBody currentMethodBody;
  @Nonnull
  private final String tmpLocalVarPrefix;
  @Nonnegative
  private int tmpLocalVarSuffix = 0;

  /**
   * @param method The method the variable will be declared into.
   * @param prefix A name prefix for the created variable. It helps to determine which client
   *               of @{code LocalVarCreator} created it when debugging.
   */
  public LocalVarCreator(@Nonnull JMethod method, @Nonnull String prefix) {
    JAbstractMethodBody body = method.getBody();
    assert body != null;
    assert body instanceof JMethodBody;
    currentMethodBody = (JMethodBody) body;
    tmpLocalVarPrefix = prefix;
  }

  /**
   * This function creates a temporary local variable of type {@code type}, and appends the
   * insertion of its declaration statement in {@code transformationRequest}.
   */
  @Nonnull
  public JLocal createTempLocal(@Nonnull JType type, @Nonnull SourceInfo sourceInfo,
      @Nonnull TransformationRequest transformationRequest) {

    String name = NamingTools.getNonSourceConflictingName(tmpLocalVarPrefix + tmpLocalVarSuffix++);
    JLocal local = new JLocal(sourceInfo, name, type, JModifier.SYNTHETIC, currentMethodBody);

    transformationRequest.append(new AddJLocalInMethodBody(local, currentMethodBody));

    return local;
  }
}