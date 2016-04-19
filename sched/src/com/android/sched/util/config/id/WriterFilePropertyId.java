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

import com.android.sched.util.codec.WriterFileCodec;
import com.android.sched.util.config.category.Category;
import com.android.sched.util.config.expression.BooleanExpression;
import com.android.sched.util.file.OutputStreamFile;
import com.android.sched.util.file.StreamFileStatus;
import com.android.sched.util.file.WriterFile;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.stream.CustomPrintWriter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Specialized {@link PropertyId} that manages properties of type {@link OutputStreamFile}
 */
public class WriterFilePropertyId extends PropertyId<WriterFile> {
  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  @Nonnull
  public static WriterFilePropertyId create(@Nonnull String name, @Nonnull String description,
      @Nonnull WriterFileCodec codec) {
    return new WriterFilePropertyId(name, description, codec);
  }

  protected WriterFilePropertyId(
      @Nonnull String name, @Nonnull String description, @Nonnull WriterFileCodec codec) {
    super(name, description, codec);
    withAutoCheck();
  }

  @Nonnull
  public WriterFilePropertyId withAutoClose() {
    setShutdownHook(new ShutdownRunnable<WriterFile>() {
      @Override
      public void run(WriterFile file) {
        if (file.getStatus() != StreamFileStatus.NOT_USED) {
          CustomPrintWriter writer = file.getPrintWriter();
          try {
            writer.throwPendingException();
          } catch (IOException e) {
            logger.log(Level.SEVERE, "Pending exception writing "
                + file.getLocation().getDescription() + " from property '" + getName() + "'", e);
          }

          if (file.getStatus() == StreamFileStatus.OPEN) {
            try {
              writer.close();
              writer.throwPendingException();
            } catch (IOException e) {
              logger.log(Level.SEVERE, "Failed to close " + file.getLocation().getDescription()
                  + " from property '" + getName() + "'", e);
            }
          }
        }
      }
    });

    return this;
  }

  @Nonnull
  public WriterFilePropertyId withAutoCheck() {
    setShutdownHook(new ShutdownRunnable<WriterFile>() {
      @Override
      public void run(WriterFile file) {
        if (file.getStatus() != StreamFileStatus.NOT_USED) {
          CustomPrintWriter writer = file.getPrintWriter();

          try {
            writer.throwPendingException();
          } catch (IOException e) {
            String message = "Pending exception writing " + file.getLocation().getDescription()
                + " from property '" + getName() + "'";

            logger.log(Level.SEVERE, message, e);
            throw new AssertionError(message);
          }

          if (file.getStatus() == StreamFileStatus.OPEN) {
            throw new AssertionError(file.getLocation().getDescription() + " from property '"
                + getName() + "' is not closed");
          }
        }
      }
    });

    return this;
  }

  @Nonnull
  public WriterFilePropertyId withoutAutoAction() {
    removeShutdownHook();

    return this;
  }

  @Override
  @Nonnull
  public WriterFilePropertyId addDefaultValue (@Nonnull String defaultValue) {
    super.addDefaultValue(defaultValue);

    return this;
  }

  @Override
  @Nonnull
  public WriterFilePropertyId addDefaultValue (@Nonnull WriterFile defaultValue) {
    super.addDefaultValue(defaultValue);

    return this;
  }

  @Override
  @Nonnull
  public WriterFilePropertyId requiredIf(@Nonnull BooleanExpression expression) {
    super.requiredIf(expression);

    return this;
  }

  @Override
  @Nonnull
  public WriterFileCodec getCodec() {
    return (WriterFileCodec) super.getCodec();
  }

  @Override
  @Nonnull
  public WriterFilePropertyId addCategory(@Nonnull Class<? extends Category> category) {
    super.addCategory(category);

    return this;
  }

  @Override
  @Nonnull
  public WriterFilePropertyId addCategory(@Nonnull Category category) {
    super.addCategory(category);

    return this;
  }
}
