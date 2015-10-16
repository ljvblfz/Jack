/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.shrob.shrink;

import com.android.jack.reporting.ReportableException;

import javax.annotation.Nonnull;

/**
 * An {@link Exception} that occurs during the mapping phase of the obfuscation.
 */
public class MappingContextException extends ReportableException {

  private static final long serialVersionUID = 1L;

  public MappingContextException(
      @Nonnull Throwable cause) {
    super(cause);
  }

  @Override
  @Nonnull
  public String getMessage() {
    return "Proguard mapping: " + getCause().getMessage();
  }

  @Override
  @Nonnull
  public ProblemLevel getDefaultProblemLevel() {
    return ProblemLevel.ERROR;
  }

}
