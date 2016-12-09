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

package com.android.jack.dx.rop.cst;

import com.android.jack.dx.dex.file.ValueEncoder.ValueType;
import com.android.jack.dx.rop.type.Type;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Constants of type reference to a method handle.
 */
public final class CstMethodHandleRef extends TypedConstant {

  /**
   * Method handle kinds.
   */
  public enum MethodHandleKind {
    PUT_STATIC(0x00),
    GET_STATIC(0x01),
    PUT_INSTANCE(0x02),
    GET_INSTANCE(0x03),
    INVOKE_STATIC(0x04),
    INVOKE_INSTANCE(0x05),
    INVOKE_CONSTRUCTOR(0x06);

    private final int value;

    private MethodHandleKind(@Nonnegative int value) {
      this.value = value;
    }

    @Nonnegative
    public int getValue() {
      return value;
    }

    @Nonnull
    public static MethodHandleKind getKind(@Nonnegative int value) {
      MethodHandleKind kind;

      switch (value) {
        case 0x00:
          kind = PUT_STATIC;
          break;
        case 0x01:
          kind = GET_STATIC;
          break;
        case 0x02:
          kind = PUT_INSTANCE;
          break;
        case 0x03:
          kind = GET_INSTANCE;
          break;
        case 0x04:
          kind = INVOKE_STATIC;
          break;
        case 0x05:
          kind = INVOKE_INSTANCE;
          break;
        case 0x06:
          kind = INVOKE_CONSTRUCTOR;
          break;
        default:
          throw new AssertionError();
      }

      assert kind.getValue() == value;
      return kind;
    }
  }

  @Nonnull
  private final MethodHandleKind kind;

  @Nonnull
  private final CstMemberRef memberRef;

  public CstMethodHandleRef(@Nonnull MethodHandleKind kind, @Nonnull CstMemberRef memberRef) {
    this.kind = kind;
    this.memberRef = memberRef;
  }

  /** {@inheritDoc} */
  @Override
  public final boolean equals(@Nonnull Object other) {
    if (other == null || getClass() != other.getClass()) {
      return false;
    }

    CstMethodHandleRef otherCst = (CstMethodHandleRef) other;
    return kind.equals(otherCst.kind) && memberRef.equals(otherCst.memberRef);
  }

  /** {@inheritDoc} */
  @Override
  public final int hashCode() {
    return (kind.hashCode() * 31) ^ memberRef.hashCode();
  }

  /** {@inheritDoc} */
  @Override
  protected int compareTo0(@Nonnull Constant other) {
    assert other instanceof CstMethodHandleRef;

    int cmp = kind.compareTo(((CstMethodHandleRef) other).kind);

    if (cmp != 0) {
      return cmp;
    }

    return memberRef.compareTo(((CstMethodHandleRef) other).memberRef);
  }

  /** {@inheritDoc} */
  @Override
  public boolean isCategory2() {
    return false;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public Type getType() {
    return memberRef.getType();
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public String typeName() {
    return "method-handle";
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public String toString() {
    return typeName() + '{' + toHuman() + '}';
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public String toHuman() {
    return kind.name() + ", " + memberRef.toHuman();
  }

  @Nonnull
  public MethodHandleKind getKind() {
    return kind;
  }

  @Nonnull
  public CstMemberRef getMemberRef() {
    return memberRef;
  }

  @Override
  @Nonnull
  public ValueType getEncodedValueType() {
    return ValueType.VALUE_METHOD_HANDLE;
  }
}