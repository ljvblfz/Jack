/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.ir.formatter;

import com.android.jack.ir.ast.JArrayType;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JType;

import java.util.Iterator;

import javax.annotation.Nonnull;

/**
 * Provides formatted names as source identifier.
 * The format is for instance "package1_package2_Classname".
 */
public class IdentifierFormatter extends SourceFormatter {

  @Nonnull
  private static final IdentifierFormatter formatter = new IdentifierFormatter();

  private static final char separator = '_';

  private IdentifierFormatter() {
  }

  @Nonnull
  public static IdentifierFormatter getFormatter() {
    return formatter;
  }

  @Override
  protected char getPackageSeparator() {
    return separator;
  }

  @Override
  @Nonnull
  public String getName(@Nonnull JType type) {
    if (type instanceof JArrayType) {
      return getName(((JArrayType) type).getElementType()) + separator;
    }
    return super.getName(type);
  }

  @Override
  @Nonnull
  public String getName(@Nonnull JMethod method) {
    StringBuilder sb = new StringBuilder(getName(method.getType()));
    sb.append(separator);
    sb.append(method.getName().replace('<', separator).replace('>', separator));
    sb.append(separator);
    Iterator<JParameter> argumentIterator = method.getParams().iterator();
    while (argumentIterator.hasNext()) {
      JParameter argument = argumentIterator.next();
      sb.append(getName(argument.getType()));
      sb.append(separator);
      sb.append(argument.getName());
      if (argumentIterator.hasNext()) {
        sb.append(separator);
      }
    }
    sb.append(separator);
    return sb.toString();
  }

}
