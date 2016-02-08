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

package com.android.jack.tools.jacoco;

import org.jacoco.core.internal.analysis.MethodCoverageImpl;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Jack specialization of method coverage.
 */
public class JackMethodCoverage extends MethodCoverageImpl {
  private final int id;

  public JackMethodCoverage(
      @Nonnegative int id, @Nonnull String name, @Nonnull String desc, @Nonnull String signature) {
    super(name, desc, signature);
    this.id = id;
  }

  @Nonnegative
  public int getId() {
    return id;
  }
}
