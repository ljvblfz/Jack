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

package com.android.sched.util.config.id;

import com.android.sched.util.codec.OutputStreamCodec;
import com.android.sched.util.config.category.Category;
import com.android.sched.util.config.expression.BooleanExpression;
import com.android.sched.util.file.OutputStreamFile;
import com.android.sched.util.file.StreamFileStatus;
import com.android.sched.util.log.LoggerFactory;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Specialized {@link PropertyId} that manages properties of type {@link OutputStreamFile}
 */
public class OutputStreamFilePropertyId extends PropertyId<OutputStreamFile> {
  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  @Nonnull
  public static OutputStreamFilePropertyId create(@Nonnull String name, @Nonnull String description,
      @Nonnull OutputStreamCodec codec) {
    return new OutputStreamFilePropertyId(name, description, codec);
  }

  protected OutputStreamFilePropertyId(
      @Nonnull String name, @Nonnull String description, @Nonnull OutputStreamCodec codec) {
    super(name, description, codec);
    withAutoCheck();
  }

  @Nonnull
  public OutputStreamFilePropertyId withAutoClose() {
    setShutdownHook(new ShutdownRunnable<OutputStreamFile>() {
      @Override
      public void run(OutputStreamFile file) {
        if (file.getStatus() == StreamFileStatus.OPEN) {
          try {
            file.getOutputStream().close();
          } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to close " + file.getLocation().getDescription()
                + " from property '" + getName() + "'", e);
          }
        }
      }
    });

    return this;
  }

  @Nonnull
  public OutputStreamFilePropertyId withAutoCheck() {
    setShutdownHook(new ShutdownRunnable<OutputStreamFile>() {
      @Override
      public void run(OutputStreamFile file) {
        if (file.getStatus() == StreamFileStatus.OPEN) {
          throw new AssertionError(file.getLocation().getDescription() + " from property '"
              + getName() + " is not closed");
        }
      }
    });

    return this;
  }

  @Nonnull
  public OutputStreamFilePropertyId withoutAutoAction() {
    removeShutdownHook();

    return this;
  }

  @Override
  @Nonnull
  public OutputStreamFilePropertyId addDefaultValue (@Nonnull String defaultValue) {
    super.addDefaultValue(defaultValue);

    return this;
  }

  @Override
  @Nonnull
  public OutputStreamFilePropertyId addDefaultValue (@Nonnull OutputStreamFile defaultValue) {
    super.addDefaultValue(defaultValue);

    return this;
  }

  @Override
  @Nonnull
  public OutputStreamFilePropertyId requiredIf(@Nonnull BooleanExpression expression) {
    super.requiredIf(expression);

    return this;
  }

  @Override
  @Nonnull
  public OutputStreamCodec getCodec() {
    return (OutputStreamCodec) super.getCodec();
  }

  @Override
  @Nonnull
  public OutputStreamFilePropertyId addCategory(@Nonnull Class<? extends Category> category) {
    super.addCategory(category);

    return this;
  }

  @Override
  @Nonnull
  public OutputStreamFilePropertyId addCategory(@Nonnull Category category) {
    super.addCategory(category);

    return this;
  }
}
