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

import com.android.sched.util.codec.ReaderFileCodec;
import com.android.sched.util.config.category.Category;
import com.android.sched.util.config.expression.BooleanExpression;
import com.android.sched.util.file.OutputStreamFile;
import com.android.sched.util.file.ReaderFile;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.stream.ExtendedBufferedReader;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Specialized {@link PropertyId} that manages properties of type {@link OutputStreamFile}
 */
public class ReaderFilePropertyId extends PropertyId<ReaderFile> {
  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  @Nonnull
  public static ReaderFilePropertyId create(@Nonnull String name, @Nonnull String description,
      @Nonnull ReaderFileCodec codec) {
    return new ReaderFilePropertyId(name, description, codec);
  }

  protected ReaderFilePropertyId(
      @Nonnull String name, @Nonnull String description, @Nonnull ReaderFileCodec codec) {
    super(name, description, codec);
    withAutoCheck();
  }

  @Nonnull
  public ReaderFilePropertyId withAutoClose() {
    setShutdownHook(new ShutdownRunnable<ReaderFile>() {
      @Override
      public void run(ReaderFile file) {
        if (file.hasUsedReader()) {
          ExtendedBufferedReader reader = file.getBufferedReader();

          try {
            reader.close();
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
  public ReaderFilePropertyId withAutoCheck() {
    setShutdownHook(new ShutdownRunnable<ReaderFile>() {
      @Override
      public void run(ReaderFile file) {
        if (file.hasUsedReader()) {
          ExtendedBufferedReader reader = file.getBufferedReader();

          if (!reader.isClosed()) {
            throw new AssertionError(
                "File '" + file.getPath() + "' from property '" + getName() + "' is not closed");
          }
        }
      }
    });

    return this;
  }

  @Nonnull
  public ReaderFilePropertyId withoutAutoAction() {
    removeShutdownHook();

    return this;
  }

  @Override
  @Nonnull
  public ReaderFilePropertyId addDefaultValue (@Nonnull String defaultValue) {
    super.addDefaultValue(defaultValue);

    return this;
  }

  @Override
  @Nonnull
  public ReaderFilePropertyId addDefaultValue (@Nonnull ReaderFile defaultValue) {
    super.addDefaultValue(defaultValue);

    return this;
  }

  @Override
  @Nonnull
  public ReaderFilePropertyId requiredIf(@Nonnull BooleanExpression expression) {
    super.requiredIf(expression);

    return this;
  }

  @Override
  @Nonnull
  public ReaderFileCodec getCodec() {
    return (ReaderFileCodec) super.getCodec();
  }

  @Override
  @Nonnull
  public ReaderFilePropertyId addCategory(@Nonnull Class<? extends Category> category) {
    super.addCategory(category);

    return this;
  }

  @Override
  @Nonnull
  public ReaderFilePropertyId addCategory(@Nonnull Category category) {
    super.addCategory(category);

    return this;
  }
}
