/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.ir.ast;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * {@link JMethodIdWithReturnType} describes a method by using a {@link JMethodId} and its return
 * type.
 */
public class JMethodIdWithReturnType {

  @Nonnull
  private JMethodId methodId;

  @Nonnull
  private final JType returnType;

  public JMethodIdWithReturnType(@Nonnull JMethodId methodId, @Nonnull JType returnType) {
    this.methodId = methodId;
    this.returnType = returnType;
  }

  @Nonnull
  public JMethodId getMethodId() {
    return methodId;
  }

  public void setMethodId(@Nonnull JMethodId newMthId) {
    methodId = newMthId;
  }

  @Nonnull
  public JType getReturnType() {
    return returnType;
  }

  @Nonnull
  public List<JType> getParameterTypes() {
    return methodId.getParamTypes();
  }

  @Nonnull
  public String getName() {
    return methodId.getName();
  }
}