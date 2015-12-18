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

import com.android.sched.util.config.ConfigurationError;
import com.android.sched.util.config.ReflectDefaultCtorFactory;

import java.lang.reflect.Modifier;

import javax.annotation.Nonnull;

/**
 * An instance of this type identifies a specific value.
 *
 * @param <T> Type of the value.
 */
public class ObjectId<T> extends KeyId<T, T> {

  @Nonnull
  private final Class<? extends T> cls;

  public ObjectId(@Nonnull String name, @Nonnull final Class<? extends T> cls) {
    super(name);
    this.cls = cls;
  }

  public void checkInstantiability() throws ConfigurationError {
    if (cls.isInterface() || Modifier.isAbstract(cls.getModifiers())) {
      throw new ConfigurationError("Object id '" + getName()
          + "' is declared with uninstantiable type " + cls.getName());
    }
  }

  @Nonnull
  public T createObject() {
    return new ReflectDefaultCtorFactory<T>(cls, false).create();
  }
}
