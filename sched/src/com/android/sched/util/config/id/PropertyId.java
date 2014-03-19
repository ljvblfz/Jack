/*
 * Copyright (C) 2013 The Android Open Source Project
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


import com.android.sched.util.codec.CodecContext;
import com.android.sched.util.codec.ParsingException;
import com.android.sched.util.codec.StringCodec;
import com.android.sched.util.config.ConfigurationError;
import com.android.sched.util.config.expression.BooleanExpression;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * An instance of this type identifies a particular configuration property.
 * @param <T> Type of the configuration property.
 */
public class PropertyId<T> extends KeyId<T, String> {

  @Nonnull
  private final String description;

  @Nonnull
  private final StringCodec<T> parser;

  @CheckForNull
  private String  defaultValue = null;
  private boolean defaultValueAvailable = false;

  private boolean isPublic = true;

  public static <T> PropertyId<T> create(
      @Nonnull String name, @Nonnull String description, @Nonnull StringCodec<T> parser) {
    return new PropertyId<T>(name, description, parser);
  }

  protected PropertyId(
      @Nonnull String name, @Nonnull String description, @Nonnull StringCodec<T> parser) {
    super(name);

    this.description = description;
    this.parser = parser;
  }

  @Override
  @Nonnull
  public PropertyId<T> addDefaultValue(@Nonnull String defaultValue) {
    super.addDefaultValue(defaultValue);

    return this;
  }

  @CheckForNull
  public String getDefaultValue(@Nonnull CodecContext context) {
    if (!defaultValueAvailable) {
      ParsingException lastException = null;

      for (String value : getDefaultValues()) {
        try {
          parser.checkString(context, value);
          defaultValue = value;
          break;
        } catch (ParsingException e) {
          lastException = e;
        }
      }

      if (defaultValue == null && lastException != null) {
        throw new ConfigurationError(lastException);
      }

      defaultValueAvailable = true;
    }

    return defaultValue;
  }

  @Nonnull
  public String getDescription() {
    return description;
  }

  @Override
  public boolean isPublic() {
    return isPublic;
  }

  public PropertyId<T> makePrivate() {
    isPublic = false;
    return this;
  }

  @Nonnull
  public StringCodec<T> getCodec() {
    return parser;
  }

  @Override
  @Nonnull
  public PropertyId<T> requiredIf(@Nonnull BooleanExpression expression) {
    super.requiredIf(expression);

    return this;
  }

}
