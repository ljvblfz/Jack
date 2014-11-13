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


import com.android.sched.util.HasDescription;
import com.android.sched.util.codec.CodecContext;
import com.android.sched.util.codec.ParsingException;
import com.android.sched.util.codec.StringCodec;
import com.android.sched.util.config.ConfigurationError;
import com.android.sched.util.config.category.Category;
import com.android.sched.util.config.category.DefaultCategory;
import com.android.sched.util.config.expression.BooleanExpression;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * An instance of this type identifies a particular configuration property.
 * @param <T> Type of the configuration property.
 */
public class PropertyId<T> extends KeyId<T, String> implements HasDescription {
  @Nonnull
  private final String description;

  @Nonnull
  private final StringCodec<T> codec;

  @Nonnull
  private final List<Value> defaultValues = new ArrayList<Value>(1);
  @CheckForNull
  private Value    defaultValue = null;
  private boolean  defaultValueAvailable = false;

  @Nonnull
  private Category category = DefaultCategory.get();

  @Nonnull
  public static <T> PropertyId<T> create(
      @Nonnull String name, @Nonnull String description, @Nonnull StringCodec<T> codec) {
    return new PropertyId<T>(name, description, codec);
  }

  protected PropertyId(@Nonnull String name, @Nonnull String description,
      @Nonnull StringCodec<T> codec) {
    super(name);

    this.description = description;
    this.codec = codec;
  }

  @Nonnull
  public PropertyId<T> addDefaultValue(@Nonnull String defaultValue) {
    defaultValues.add(new Value(defaultValue));

    return this;
  }

  @Nonnull
  public PropertyId<T> addDefaultValue(@Nonnull T defaultValue) {
    defaultValues.add(new Value(defaultValue));

    return this;
  }

  @CheckForNull
  public Value getDefaultValue(@Nonnull CodecContext context) {
    if (!defaultValueAvailable) {
      ParsingException lastException = null;

      for (Value value : getDefaultValues()) {
        try {
          value.check(context);
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
  public List<Value> getDefaultValues() {
    return defaultValues;
  }

  @Override
  @Nonnull
  public String getDescription() {
    return description;
  }

  @Nonnull
  public Category getCategory() {
    return category;
  }

  @Nonnull
  public PropertyId<T> withCategory(@Nonnull Category category) {
    this.category = category;
    return this;
  }

  @Override
  public boolean isPublic() {
    return category.isPublic();
  }

  @Nonnull
  public StringCodec<T> getCodec() {
    return codec;
  }

  @Override
  @Nonnull
  public PropertyId<T> requiredIf(@Nonnull BooleanExpression expression) {
    super.requiredIf(expression);

    return this;
  }

  /**
   * A {@link PropertyId} value.
   */
  public class Value {
    @Nonnull
    private IValue<T> value;

    public Value (@Nonnull T value) {
      this.value = new IValueObject<T>(value);
    }

    public Value (@Nonnull String value) {
      this.value = new IValueString(value);
    }

    public synchronized void check(@Nonnull CodecContext context) throws ParsingException {
      value = value.check(context);
    }

    @Nonnull
    public String getString() {
      return value.getString();
    }

    @Nonnull
    public synchronized T getObject(@Nonnull CodecContext context) {
      value = value.getValueObject(context);

      return ((IValueObject<T>) value).getObject();
    }


    @CheckForNull
    public synchronized T getObjectIfAny() {
      if (value instanceof IValueObject) {
        return ((IValueObject<T>) value).getObject();
      } else {
        return null;
      }
    }
  }

  //
  // Private IValue hierarchy
  //

  private static interface IValue<T> {
    @Nonnull
    public IValue<T> check(@Nonnull CodecContext context) throws ParsingException;

    @Nonnull
    public PropertyId<?>.IValueObject<T> getValueObject(@Nonnull CodecContext context);

    @Nonnull
    public String getString();
  }

  private class IValueString implements IValue<T> {
    @Nonnull
    private final String value;

    public IValueString (@Nonnull String value) {
      this.value = value;
    }

    @Override
    public String getString() {
      return value;
    }

    @Override
    @Nonnull
    public IValue<T> check(@Nonnull CodecContext context) throws ParsingException {
      T val = PropertyId.this.codec.checkString(context, value);

      if (val != null) {
        return new IValueObject<T>(val);
      } else {
        return new IValueCheckedString(value);
      }
    }

    @Override
    @Nonnull
    public PropertyId<T>.IValueObject<T> getValueObject(@Nonnull CodecContext context) {
      throw new AssertionError();
    }
  }

  private class IValueCheckedString implements IValue<T> {
    @Nonnull
    private final String value;

    private IValueCheckedString (@Nonnull String value) {
      this.value = value;
    }

    @Override
    public String getString() {
      return value;
    }

    @Override
    @Nonnull
    public IValue<T> check(@Nonnull CodecContext context) {
      return this;
    }

    @Override
    @Nonnull
    public PropertyId<?>.IValueObject<T> getValueObject(@Nonnull CodecContext context) {
      return new IValueObject<T>(PropertyId.this.codec.parseString(context, value));
    }
  }

  private class IValueObject<T> implements IValue<T> {
    @Nonnull
    private final T value;

    public IValueObject (@Nonnull T value) {
      this.value = value;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nonnull
    public String getString() {
      return ((PropertyId<T>) (PropertyId.this)).codec.formatValue(value);
    }

    @Override
    @Nonnull
    public IValue<T> check(@Nonnull CodecContext context) {
      return this;
    }

    @Override
    @Nonnull
    public PropertyId<?>.IValueObject<T> getValueObject(@Nonnull CodecContext context) {
      return this;
    }

    @Nonnull
    public T getObject() {
      return value;
    }
  }
}
