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

package com.android.jack.util;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import com.android.jack.util.MethodNameCodec.MethodNameValue;
import com.android.sched.util.codec.CodecContext;
import com.android.sched.util.codec.ParsingException;
import com.android.sched.util.codec.StringCodec;
import com.android.sched.util.config.ConfigurationError;

import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to make a {@link MethodNameValue} object to/from {@link String}
 * representing a fully qualified method name
 */
public class MethodNameCodec implements StringCodec<MethodNameValue> {
  @Nonnull
  private final Splitter splitter = Splitter.on(PACKAGE_SEPARATOR);
  @Nonnull
  private static final String CLASS_METHOD_SEPARATOR = "#";
  @Nonnull
  private static final String PACKAGE_SEPARATOR = ".";
  @Nonnull
  private static final String USAGE = "a fully qualified java method name (e.g. "
      + Joiner.on(PACKAGE_SEPARATOR).join("java", "lang", "Object").toString()
      + CLASS_METHOD_SEPARATOR + "toString)";
  @Nonnull
  private static final String MESSAGE = "The value must be " + USAGE + " but ";

  @Override
  @Nonnull
  public String getUsage() {
    return USAGE;
  }

  @Override
  @Nonnull
  public String getVariableName() {
    return "method";
  }

  @Override
  @Nonnull
  public List<ValueDescription> getValueDescriptions() {
    return Collections.<ValueDescription> emptyList();
  }

  @Override
  @Nonnull
  public MethodNameValue parseString(@Nonnull CodecContext context, @Nonnull String string) {
    try {
      return checkString(context, string);
    } catch (ParsingException e) {
      throw new ConfigurationError(e);
    }
  }

  @Override
  @CheckForNull
  public MethodNameValue checkString(@Nonnull CodecContext context, @Nonnull String string)
      throws ParsingException {
    string = string.trim();

    int idx = string.indexOf(CLASS_METHOD_SEPARATOR);
    if (idx < 0) {
      throw new ParsingException(MESSAGE + "has no '" + CLASS_METHOD_SEPARATOR + "'");
    }

    String className = string.substring(0, idx);
    List<String> parts = splitter.splitToList(className);
    if (parts.isEmpty()) {
      throw new ParsingException(MESSAGE + "class name is empty");
    }

    // Package names
    String name;
    for (int i = 0; i < parts.size() - 1; i++) {
      name = parts.get(i);
      if (name.isEmpty()) {
        throw new ParsingException(MESSAGE + "package #" + (i + 1) + " name is empty");
      }
      if (!isJavaValidIdentifier(parts.get(i))) {
        throw new ParsingException(MESSAGE + "package #" + (i + 1) + " name '" + name
            + "' is not a valid Java identifier");
      }
    }

    // Class name
    name = parts.get(parts.size() - 1);
    if (name.isEmpty()) {
      throw new ParsingException(MESSAGE + "class name is empty");
    }
    if (!isJavaValidIdentifier(name)) {
      throw new ParsingException(
          MESSAGE + "class name '" + name + "' is not a valid Java identifier");
    }

    // Method name
    String methodName = string.substring(idx + 1, string.length());
    if (methodName.indexOf(CLASS_METHOD_SEPARATOR) >= 0) {
      throw new ParsingException(MESSAGE + "has several '" + CLASS_METHOD_SEPARATOR + "'");
    }
    if (methodName.isEmpty()) {
      throw new ParsingException(MESSAGE + "method name is empty");
    }
    if (!isJavaValidIdentifier(methodName)) {
      throw new ParsingException(
          MESSAGE + "method name '" + methodName + "' is not a valid Java identifier");
    }

    return new MethodNameValue(className, methodName);
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull MethodNameValue data) {
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull MethodNameValue name) {
    return name.getFullName();
  }

  private boolean isJavaValidIdentifier(@Nonnull String id) {
    assert id.length() > 0;

    char[] chars = id.toCharArray();
    if (!Character.isJavaIdentifierStart(chars[0])) {
      return false;
    }

    for (int i = 1; i < chars.length; i++) {
      if (!Character.isJavaIdentifierPart(chars[i])) {
        return false;
      }
    }

    return true;
  }

  /**
   * A fully qualified java method name (e.g. java.lang.Object#toString)
   */
  public static final class MethodNameValue {
    @Nonnull
    private final String className;
    @Nonnull
    private final String methodName;

    private MethodNameValue(@Nonnull String className, @Nonnull String methodName) {
      this.className = className;
      this.methodName = methodName;
    }

    @Nonnull
    public String getClassName() {
      return className;
    }

    @Nonnull
    public String getMethodName() {
      return methodName;
    }

    @Nonnull
    public String getFullName() {
      return className + '#' + methodName;
    }
  }
}
