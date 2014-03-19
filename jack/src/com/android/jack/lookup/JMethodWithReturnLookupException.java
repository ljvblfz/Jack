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

package com.android.jack.lookup;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JReferenceType;
import com.android.jack.ir.ast.JType;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Lookup exception thrown when searching for JMethod from its name, args and return types.
 */
public class JMethodWithReturnLookupException extends JMethodLookupException {

  private static final long serialVersionUID = 1L;
  @Nonnull
  private final String methodName;
  @Nonnull
  private final List<? extends JType> methodArgumentsTypes;
  @Nonnull
  private final JType methodReturnType;

  public JMethodWithReturnLookupException(
      @Nonnull JReferenceType declaringClass,
      @Nonnull String name,
      @Nonnull List<? extends JType> args,
      @Nonnull JType returnType) {
    super(declaringClass);
    this.methodName = name;
    this.methodArgumentsTypes = args;
    this.methodReturnType = returnType;
  }

  @Nonnull
  @Override
  protected String getMethodString() {
    return Jack.getUserFriendlyFormatter()
        .getName(methodName, methodArgumentsTypes, methodReturnType);
  }
}