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

package com.android.sched.util.config.id;


import com.android.sched.util.codec.ConvertCodec;
import com.android.sched.util.codec.KeyValueCodec;
import com.android.sched.util.codec.MessageDigestCodec;
import com.android.sched.util.config.MessageDigestFactory;
import com.android.sched.util.config.category.Category;
import com.android.sched.util.config.expression.BooleanExpression;

import java.security.MessageDigest;
import java.security.Provider.Service;

import javax.annotation.Nonnull;

/**
 * Specialized {@link PropertyId} that manages properties of type {@link MessageDigest}
 */
public class MessageDigestPropertyId extends PropertyId<MessageDigestFactory> {
  @Nonnull
  private static KeyValueCodec<Boolean> parser;

  @Nonnull
  public static MessageDigestPropertyId create(@Nonnull String name, @Nonnull String description) {
    return new MessageDigestPropertyId(name, description);
  }

  protected MessageDigestPropertyId(@Nonnull String name, @Nonnull String description) {
    super(name, description, new ConvertCodec<Service, MessageDigestFactory>(
        new MessageDigestCodec()) {
      @Override
      @Nonnull
      protected Service revert(@Nonnull MessageDigestFactory dst) {
        return dst.getService();
      }

      @Override
      @Nonnull
      protected MessageDigestFactory convert(@Nonnull Service service) {
        return new MessageDigestFactory(service);
      }
    });
  }

  @Override
  @Nonnull
  public MessageDigestPropertyId addDefaultValue (@Nonnull String defaultValue) {
    super.addDefaultValue(defaultValue);

    return this;
  }

  @Nonnull
  public MessageDigestPropertyId addDefaultValue (@Nonnull Service service) {
    super.addDefaultValue(new MessageDigestFactory(service));

    return this;
  }

  @Override
  @Nonnull
  public MessageDigestPropertyId requiredIf(@Nonnull BooleanExpression expression) {
    super.requiredIf(expression);

    return this;
  }

  @Override
  @Nonnull
  public MessageDigestPropertyId addCategory(@Nonnull Class<? extends Category> category) {
    super.addCategory(category);

    return this;
  }
}
