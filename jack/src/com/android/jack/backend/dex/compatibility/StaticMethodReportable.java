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

package com.android.jack.backend.dex.compatibility;

import com.android.jack.Jack;
import com.android.jack.ir.HasSourceInfo;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.formatter.TypeAndMethodFormatter;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.reporting.Reportable;
import com.android.jack.util.AndroidApiLevel;

import javax.annotation.Nonnull;

/**
 * A {@link Reportable} indicating that a static method was found
 */
public class StaticMethodReportable implements Reportable, HasSourceInfo {

  @Nonnull
  private final JMethod method;

  public StaticMethodReportable(@Nonnull JMethod method) {
    this.method = method;
  }

  @Override
  @Nonnull
  public String getMessage() {
    TypeAndMethodFormatter formatter = Jack.getUserFriendlyFormatter();
    return "Static method "
        + formatter.getName(method)
        + " not supported in Android API level less than "
        + AndroidApiLevel.ReleasedLevel.N.getLevel();
  }

  @Override
  @Nonnull
  public ProblemLevel getDefaultProblemLevel() {
    return ProblemLevel.ERROR;
  }

  @Override
  public SourceInfo getSourceInfo() {
    return method.getSourceInfo();
  }

}

