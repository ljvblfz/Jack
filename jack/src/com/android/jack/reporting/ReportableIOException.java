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

package com.android.jack.reporting;

import com.android.sched.util.file.SchedIOException;
import com.android.sched.util.location.Location;

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * A generic {@link Reportable} for {@link IOException}.
 */
public class ReportableIOException extends ReportableException {
  private static final long serialVersionUID = 1L;
  @Nonnull
  private final Location location;
  @Nonnull
  private final String module;

  public ReportableIOException(@Nonnull String module, @Nonnull SchedIOException cause) {
    super(cause);
    this.location = cause.getLocation();
    this.module = module;
  }

  @Override
  public String getMessage() {
    String message = getCause().getMessage();
    if (message == null) {
      message = "unknown error";
    }

    return module + ": " + getCause().getMessage();
  }

  @Override
  @Nonnull
  public ProblemLevel getDefaultProblemLevel() {
    return ProblemLevel.ERROR;
  }
}