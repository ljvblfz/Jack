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

package com.android.jack.ir.ast;

import com.android.jack.ir.SourceInfo;
import com.android.jack.ir.formatter.BinaryQualifiedNameFormatter;
import com.android.jack.ir.formatter.BinarySignatureFormatter;
import com.android.jack.ir.formatter.SourceFormatter;
import com.android.jack.ir.formatter.TypeFormatter;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/**
 * String representing the source name of a type.
 */
@Description("String representing the source name of a type")
public class JTypeStringLiteral extends JAbstractStringLiteral {

  private static final long serialVersionUID = 1L;

  @Nonnull
  private static final TypeFormatter binaryQnFormatter =
      BinaryQualifiedNameFormatter.getFormatter();
  @Nonnull
  private static final TypeFormatter binarySignatureFormatter =
      BinarySignatureFormatter.getFormatter();
  @Nonnull
  private static final TypeFormatter sourceQnFormatter = SourceFormatter.getFormatter();

  /**
   * kind of literal type representation.
   */
  public enum Kind {
    BINARY_SIGNATURE,   // means Ljava/lang/Object;
    SRC_SIGNATURE,      // means Ljava.lang.Object;
    BINARY_QN,          // means java/lang/Object
    SRC_QN,             // means java.lang.Object
    SIMPLE_NAME         // means d when type name is a/b/c/d or a/b/c/e$d or a.b.c.d or a.b.c$d
  }

  @Nonnull
  private final JType type;

  @Nonnull
  private final Kind kind;

  public JTypeStringLiteral(
      @Nonnull SourceInfo sourceInfo, @Nonnull Kind kind, @Nonnull JType type) {
    super(sourceInfo);
    assert kind != Kind.SIMPLE_NAME || type instanceof JClassOrInterface;
    this.kind = kind;
    this.type = type;
  }

  @Nonnull
  private static String getValue(@Nonnull JType type, @Nonnull Kind kind) {
    if (type instanceof JArrayType) {
      switch (kind) {
        case BINARY_SIGNATURE:
        case SRC_SIGNATURE:
          return "[" + getValue(((JArrayType) type).getElementType(), kind);
        case BINARY_QN:
        case SRC_QN:
        case SIMPLE_NAME:
          return getValue(((JArrayType) type).getElementType(), kind) + "[]";
        default: {
          throw new AssertionError();
        }
      }
    } else {
      switch (kind) {
        case BINARY_SIGNATURE:
          return binarySignatureFormatter.getName(type);
        case SRC_SIGNATURE:
          return binarySignatureFormatter.getName(type).replace('/', '.');
        case BINARY_QN:
          assert type instanceof JClassOrInterface;
          return binaryQnFormatter.getName(type);
        case SRC_QN:
          assert type instanceof JClassOrInterface;
          return sourceQnFormatter.getName(type);
        case SIMPLE_NAME:
          return getSimpleName(type);
        default: {
          throw new AssertionError();
        }
      }
    }
  }

  @Override
  @Nonnull
  public String getValue() {
    return getValue(type, kind);
  }

  @Nonnull
  public JType getReferencedType() {
    return type;
  }

  @Nonnull
  private static String getSimpleName(@Nonnull JType type) {
    String typeName = type.getName();
    int simpleNameBeginIndex = typeName.lastIndexOf("$");

    if (simpleNameBeginIndex == -1) {
      return typeName;
    } else {
      return typeName.substring(simpleNameBeginIndex + 1);
    }
  }

  @Override
  @Nonnull
  public JTypeStringLiteral clone() {
    return (JTypeStringLiteral) super.clone();
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }
}
