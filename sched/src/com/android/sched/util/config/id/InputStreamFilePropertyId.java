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

import com.android.sched.util.codec.InputStreamCodec;
import com.android.sched.util.config.category.Category;
import com.android.sched.util.config.expression.BooleanExpression;
import com.android.sched.util.file.InputStreamFile;
import com.android.sched.util.file.StreamFileStatus;
import com.android.sched.util.log.LoggerFactory;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Specialized {@link PropertyId} that manages properties of type {@link InputStreamFile}
 */
public class InputStreamFilePropertyId extends PropertyId<InputStreamFile> {
  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  @Nonnull
  public static InputStreamFilePropertyId create(@Nonnull String name, @Nonnull String description,
      @Nonnull InputStreamCodec codec) {
    return new InputStreamFilePropertyId(name, description, codec);
  }

  protected InputStreamFilePropertyId(
      @Nonnull String name, @Nonnull String description, @Nonnull InputStreamCodec codec) {
    super(name, description, codec);
    withAutoCheck();
  }

  @Nonnull
  public InputStreamFilePropertyId withAutoClose() {
    setShutdownHook(new ShutdownRunnable<InputStreamFile>() {
      @Override
      public void run(InputStreamFile file) {
        if (file.getStatus() == StreamFileStatus.OPEN) {
          try {
            file.getInputStream().close();
          } catch (IOException e) {
            logger.log(Level.SEVERE,
                "Failed to close '" + file.getPath() + "' from property '" + getName() + "'", e);
          }
        }
      }
    });

    return this;
  }

  @Nonnull
  public InputStreamFilePropertyId withAutoCheck() {
    setShutdownHook(new ShutdownRunnable<InputStreamFile>() {
      @Override
      public void run(InputStreamFile file) {
        if (file.getStatus() == StreamFileStatus.OPEN) {
          throw new AssertionError(
              "File '" + file.getPath() + "' from property '" + getName() + "' is not closed");

        }
      }
    });

    return this;
  }

  @Nonnull
  public InputStreamFilePropertyId withoutAutoAction() {
    removeShutdownHook();

    return this;
  }

  @Override
  @Nonnull
  public InputStreamFilePropertyId addDefaultValue (@Nonnull String defaultValue) {
    super.addDefaultValue(defaultValue);

    return this;
  }

  @Override
  @Nonnull
  public InputStreamFilePropertyId addDefaultValue (@Nonnull InputStreamFile defaultValue) {
    super.addDefaultValue(defaultValue);

    return this;
  }

  @Override
  @Nonnull
  public InputStreamFilePropertyId requiredIf(@Nonnull BooleanExpression expression) {
    super.requiredIf(expression);

    return this;
  }

  @Override
  @Nonnull
  public InputStreamCodec getCodec() {
    return (InputStreamCodec) super.getCodec();
  }

  @Override
  @Nonnull
  public InputStreamFilePropertyId addCategory(@Nonnull Class<? extends Category> category) {
    super.addCategory(category);

    return this;
  }

  @Override
  @Nonnull
  public InputStreamFilePropertyId addCategory(@Nonnull Category category) {
    super.addCategory(category);

    return this;
  }
}
