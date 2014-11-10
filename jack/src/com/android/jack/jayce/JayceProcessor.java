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

package com.android.jack.jayce;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.annotation.Nonnull;

/**
 * Jayce processor.
 */
public class JayceProcessor {

  @Nonnull
  protected static Object instantiateConstructorWithParameters(@Nonnull String className,
      @Nonnull Class<?>[] parameterTypes, @Nonnull Object[] parameterInstances,
      @Nonnull String version)
      throws JayceVersionException {
    Object constructorInstance = null;
    try {
      Class<?> jayceReaderClass = Class.forName(className);
      Constructor<?> constructor = jayceReaderClass.getConstructor(parameterTypes);
      constructorInstance = constructor.newInstance(parameterInstances);
    } catch (SecurityException e) {
      throw new AssertionError("Security issue with Jayce stream");
    } catch (IllegalArgumentException e) {
      throw new AssertionError("Illegal argument for Jayce processor for version " + version);
    } catch (ClassNotFoundException e) {
      throw new JayceVersionException("Jayce version " + version + " not supported");
    } catch (NoSuchMethodException e) {
      throw new AssertionError("Jayce processing method not found for version " + version);
    } catch (InstantiationException e) {
      throw new AssertionError("Problem instantiating Jayce processor for version " + version);
    } catch (IllegalAccessException e) {
      throw new AssertionError("Problem accessing Jayce processor for version " + version);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e.getCause());
    }
    return constructorInstance;
  }
}
