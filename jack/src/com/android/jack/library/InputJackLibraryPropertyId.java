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


import com.android.sched.util.HasDescription;
import com.android.sched.util.config.ConfigChecker;
import com.android.sched.util.config.MissingPropertyException;
import com.android.sched.util.config.PropertyIdException;
import com.android.sched.util.config.category.Category;
import com.android.sched.util.config.expression.BooleanExpression;
import com.android.sched.util.config.expression.LongExpression;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.log.LoggerFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Specialized {@link PropertyId} that manages properties of type {@link InputJackLibrary}
 */
public class InputJackLibraryPropertyId extends PropertyId<InputJackLibrary> implements
    HasDescription {
  @Nonnull
  private static Logger logger = LoggerFactory.getLogger();

  @Nonnull
  public static InputJackLibraryPropertyId create(
      @Nonnull String name, @Nonnull String description) {
    return new InputJackLibraryPropertyId(name, description, new InputJackLibraryCodec());
  }

  protected InputJackLibraryPropertyId(@Nonnull String name, @Nonnull String description,
      @Nonnull InputJackLibraryCodec codec) {
    super(name, description, codec);
    setShutdownHook(new ShutdownRunnable<InputJackLibrary>() {
      @Override
      public void run(@Nonnull InputJackLibrary lib) {
        try {
          lib.close();
        } catch (LibraryIOException e) {
          logger.log(Level.FINE, "Cannot close input jack library "
              + lib.getLocation().getDescription());
        }
      }
    });
  }

  @Override
  @Nonnull
  public InputJackLibraryPropertyId addDefaultValue (@Nonnull String defaultValue) {
    super.addDefaultValue(defaultValue);

    return this;
  }

  @Override
  @Nonnull
  public InputJackLibraryPropertyId addDefaultValue (@Nonnull InputJackLibrary defaultValue) {
    super.addDefaultValue(defaultValue);

    return this;
  }

  @Override
  @Nonnull
  public InputJackLibraryPropertyId requiredIf(@Nonnull BooleanExpression expression) {
    super.requiredIf(expression);

    return this;
  }

  @Override
  @Nonnull
  public InputJackLibraryPropertyId addCategory(@Nonnull Class<? extends Category> category) {
    super.addCategory(category);
    return this;
  }

  @Override
  @Nonnull
  public InputJackLibraryPropertyId addCategory(@Nonnull Category category) {
    super.addCategory(category);
    return this;
  }

  @Nonnull
  public LongExpression getMajorVersion() {
    return new LongExpression() {
      @Override
      public long eval(@Nonnull ConfigChecker checker)
          throws PropertyIdException, MissingPropertyException {
        if (!isRequired(checker)) {
          throw new MissingPropertyException(InputJackLibraryPropertyId.this);
        }

        return checker.parse(InputJackLibraryPropertyId.this).getMajorVersion();
      }

      @Override
      @Nonnull
      public String getDescription() {
        return "major version of " + formatPropertyName(InputJackLibraryPropertyId.this);
      }

      @Override
      @Nonnull
      public String getCause(@Nonnull ConfigChecker checker) {
        return "major version of " + formatPropertyName(checker, InputJackLibraryPropertyId.this);
      }
    };
  }

  public LongExpression getMinorVersion() {
    return new LongExpression() {
      @Override
      public long eval(@Nonnull ConfigChecker checker) throws PropertyIdException,
          MissingPropertyException {
        if (!isRequired(checker)) {
          throw new MissingPropertyException(InputJackLibraryPropertyId.this);
        }

        return checker.parse(InputJackLibraryPropertyId.this).getMinorVersion();
      }

      @Override
      @Nonnull
      public String getDescription() {
        return "minor version of " + formatPropertyName(InputJackLibraryPropertyId.this);
      }

      @Override
      @Nonnull
      public String getCause(@Nonnull ConfigChecker checker) {
        return "minor version of " + formatPropertyName(checker, InputJackLibraryPropertyId.this);
      }
    };
  }

  public BooleanExpression containsFileType(@Nonnull final FileType type) {
    return new BooleanExpression() {
      @Override
      public boolean eval(@Nonnull ConfigChecker checker) throws PropertyIdException,
          MissingPropertyException {
        if (!isRequired(checker)) {
          throw new MissingPropertyException(InputJackLibraryPropertyId.this);
        }

        return checker.parse(InputJackLibraryPropertyId.this).containsFileType(type);
      }

      @Override
      @Nonnull
      public String getDescription() {
        return format(formatPropertyName(InputJackLibraryPropertyId.this), true, type);
      }

      @Override
      @Nonnull
      public String getCause(@Nonnull ConfigChecker checker) throws PropertyIdException {
        try {
          return format(formatPropertyName(InputJackLibraryPropertyId.this), eval(checker), type);
        } catch (MissingPropertyException e) {
          return e.getMessage();
        }
      }
    };
  }

  @Nonnull
  private String format(@Nonnull String left, boolean set, @Nonnull FileType type) {
    StringBuilder sb = new StringBuilder();

    sb.append(left);
    if (!set) {
      sb.append(" does not contain any ");
    } else {
      sb.append(" contains ");
    }
    sb.append(type.toString());

    return sb.toString();
  }
}
