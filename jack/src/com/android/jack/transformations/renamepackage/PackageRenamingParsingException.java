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

package com.android.jack.transformations.renamepackage;

import com.android.jack.reporting.ReportableException;
import com.android.sched.util.location.FileLocation;

import javax.annotation.Nonnull;

/**
 * A {@link ReportableException} that occurs during the package renaming phase.
 */
public class PackageRenamingParsingException extends ReportableException {

  private static final long serialVersionUID = 1L;

  @Nonnull
  private final FileLocation ruleFileLocation;

  public PackageRenamingParsingException(@Nonnull FileLocation ruleFileLocation,
      @Nonnull Throwable cause) {
    super(cause);
    this.ruleFileLocation = ruleFileLocation;
  }

  @Override
  @Nonnull
  public String getMessage() {
    return "Package renaming with " + ruleFileLocation.getDescription() + ": "
        + getCause().getMessage();
  }

  @Override
  @Nonnull
  public ProblemLevel getDefaultProblemLevel() {
    return ProblemLevel.ERROR;
  }

}
