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

package com.android.jack.config.id;


import com.android.jack.config.id.JavaVersionPropertyId.JavaVersion;
import com.android.sched.util.codec.KeyValueCodec;
import com.android.sched.util.codec.KeyValueCodec.Entry;
import com.android.sched.util.config.ConfigChecker;
import com.android.sched.util.config.MissingPropertyException;
import com.android.sched.util.config.PropertyIdException;
import com.android.sched.util.config.expression.BooleanExpression;
import com.android.sched.util.config.id.PropertyId;

import javax.annotation.Nonnull;

/**
 * Specialized {@link PropertyId} that manages java version.
 */
public class JavaVersionPropertyId extends PropertyId<JavaVersion> {

  /**
   * Supported Java source version.
   */
  public enum JavaVersion {
    JAVA_3("1.3"),
    JAVA_4("1.4"),
    JAVA_5("1.5"),
    JAVA_6("1.6"),
    JAVA_7("1.7");

    @Nonnull
    private final String value;

    JavaVersion(@Nonnull String value) {
        this.value = value;
    }

    @Override
    @Nonnull
    public String toString() {
        return value;
    }
  }

  static {
    @SuppressWarnings("unchecked")
    Entry<JavaVersion>[] elements =
      new Entry[] {
        new Entry<JavaVersion>("1.3",  JavaVersion.JAVA_3),
        new Entry<JavaVersion>("1.4",  JavaVersion.JAVA_4),
        new Entry<JavaVersion>("1.5",  JavaVersion.JAVA_5),
        new Entry<JavaVersion>("5.0",  JavaVersion.JAVA_5),
        new Entry<JavaVersion>("5",    JavaVersion.JAVA_5),
        new Entry<JavaVersion>("1.6",  JavaVersion.JAVA_6),
        new Entry<JavaVersion>("6.0",  JavaVersion.JAVA_6),
        new Entry<JavaVersion>("6",    JavaVersion.JAVA_6),
        new Entry<JavaVersion>("1.7",  JavaVersion.JAVA_7),
        new Entry<JavaVersion>("7.0",  JavaVersion.JAVA_7),
        new Entry<JavaVersion>("7",    JavaVersion.JAVA_7)
    };

    parser = new KeyValueCodec<JavaVersion>(elements).ignoreCase();
  }

  @Nonnull
  private static KeyValueCodec<JavaVersion> parser;

  @Nonnull
  public static JavaVersionPropertyId create(
      @Nonnull String name, @Nonnull String description) {
    return new JavaVersionPropertyId(name, description);
  }

  protected JavaVersionPropertyId(@Nonnull String name, @Nonnull String description) {
    super(name, description, parser);
  }

  @Override
  @Nonnull
  public JavaVersionPropertyId addDefaultValue (@Nonnull String defaultValue) {
    super.addDefaultValue(defaultValue);

    return this;
  }

  @Override
  @Nonnull
  public JavaVersionPropertyId addDefaultValue (@Nonnull JavaVersion defaultValue) {
    super.addDefaultValue(defaultValue);

    return this;
  }

  @Override
  @Nonnull
  public JavaVersionPropertyId requiredIf(@Nonnull BooleanExpression expression) {
    super.requiredIf(expression);

    return this;
  }

  @Nonnull
  public BooleanExpression is(@Nonnull final JavaVersion enumValue) {
    return new BooleanExpression() {
      @Override
      public boolean eval(@Nonnull ConfigChecker checker)
          throws PropertyIdException, MissingPropertyException {
        if (!isRequired(checker)) {
          throw new MissingPropertyException(JavaVersionPropertyId.this);
        }

        return checker.parse(JavaVersionPropertyId.this) == enumValue;
      }

      @Override
      @Nonnull
      public String getDescription() {
        return format(formatPropertyName(JavaVersionPropertyId.this), true,
            JavaVersionPropertyId.this.getCodec().formatValue(enumValue));
      }

      @Override
      @Nonnull
      public String getCause(@Nonnull ConfigChecker checker) throws PropertyIdException {
        try {
          return format(formatPropertyName(checker, JavaVersionPropertyId.this),
              eval(checker), getCodec().formatValue(checker.parse(JavaVersionPropertyId.this)));
        } catch (MissingPropertyException e) {
          return e.getMessage();
        }
      }
    };
  }

  @Nonnull
  private String format(@Nonnull String left, boolean set, @Nonnull String right) {
    StringBuilder sb = new StringBuilder();

    sb.append(left);
    sb.append(" is ");
    if (!set) {
      sb.append("not ");
    }
    sb.append("set to '");
    sb.append(right);
    sb.append('\'');

    return sb.toString();
  }
}
