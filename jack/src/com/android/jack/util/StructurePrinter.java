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

package com.android.jack.util;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.formatter.BinarySignatureFormatter;
import com.android.jack.ir.formatter.TypeAndMethodFormatter;

import java.io.PrintWriter;

import javax.annotation.Nonnull;

/**
 * Prints the structure of visited JNode(s) accepted by a filter.
 */
public class StructurePrinter extends JVisitor {

  @Nonnull
  private static final TypeAndMethodFormatter formatter = BinarySignatureFormatter.getFormatter();

  @Nonnull
  private final PrintWriter writer;

  public StructurePrinter(@Nonnull PrintWriter out) {
    this.writer = out;
  }

  @Override
  public boolean visit(@Nonnull JDefinedClassOrInterface type) {
    if (acceptFilter(type)) {
      writer.print(formatter.getName(type));
      writer.println(":");
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean visit(@Nonnull JField field) {
    if (acceptFilter(field)) {
      writer.print(formatter.getName(field.getType()));
      writer.print(" ");
      writer.println(field.getName());
    }
    return false;
  }

  @Override
  public boolean visit(@Nonnull JMethod method) {
    if (acceptFilter(method)) {
      writer.println(formatter.getName(method));
    }
    return false;
  }

  protected boolean acceptFilter(@Nonnull JDefinedClassOrInterface type) {
    return acceptFilter((JNode) type);
  }

  protected boolean acceptFilter(@Nonnull JField field) {
    return acceptFilter((JNode) field);
  }

  protected boolean acceptFilter(@Nonnull JMethod method) {
    return acceptFilter((JNode) method);
  }
  /**
   * @param node structure node, ie class interface or member.
   */
  protected boolean acceptFilter(@Nonnull JNode node) {
    return true;
  }
}
