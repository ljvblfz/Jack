/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.library;


import com.android.jack.Jack;
import com.android.jack.JackAbortException;
import com.android.jack.reporting.ReportableException;
import com.android.jack.reporting.Reporter.Severity;
import com.android.sched.util.HasDescription;
import com.android.sched.util.config.category.Category;
import com.android.sched.util.config.expression.BooleanExpression;
import com.android.sched.util.config.id.PropertyId;

import javax.annotation.Nonnull;

/**
 * Specialized {@link PropertyId} that manages properties of type {@link OutputJackLibrary}
 */
public class OutputJackLibraryPropertyId extends PropertyId<OutputJackLibrary> implements
    HasDescription {
  @Nonnull
  public static OutputJackLibraryPropertyId create(
      @Nonnull String name, @Nonnull String description) {
    return new OutputJackLibraryPropertyId(name, description, new OutputJackLibraryCodec());
  }

  private static class CannotCloseOutputLibException extends ReportableException {
    private static final long serialVersionUID = 1L;

    public CannotCloseOutputLibException(@Nonnull OutputJackLibrary lib,
        @Nonnull LibraryIOException e) {
      super("Cannot close output Jack library " + lib.getLocation(), e);
    }

    @Override
    @Nonnull
    public ProblemLevel getDefaultProblemLevel() {
      return ProblemLevel.ERROR;
    }
  }

  protected OutputJackLibraryPropertyId(@Nonnull String name, @Nonnull String description,
      @Nonnull OutputJackLibraryCodec codec) {
    super(name, description, codec);
    setShutdownHook(new ShutdownRunnable<OutputJackLibrary>() {
      @Override
      public void run(@Nonnull OutputJackLibrary lib) {
        try {
          lib.close();
        } catch (LibraryIOException e) {
          CannotCloseOutputLibException reportable = new CannotCloseOutputLibException(lib, e);
          Jack.getSession().getReporter().report(Severity.FATAL, reportable);
          throw new JackAbortException(reportable);
        }
      }});
  }

  @Override
  @Nonnull
  public OutputJackLibraryPropertyId addDefaultValue (@Nonnull String defaultValue) {
    super.addDefaultValue(defaultValue);

    return this;
  }

  @Override
  @Nonnull
  public OutputJackLibraryPropertyId addDefaultValue (@Nonnull OutputJackLibrary defaultValue) {
    super.addDefaultValue(defaultValue);

    return this;
  }

  @Override
  @Nonnull
  public OutputJackLibraryPropertyId requiredIf(@Nonnull BooleanExpression expression) {
    super.requiredIf(expression);

    return this;
  }

  @Override
  @Nonnull
  public OutputJackLibraryPropertyId addCategory(@Nonnull Class<? extends Category> category) {
    super.addCategory(category);

    return this;
  }
}
