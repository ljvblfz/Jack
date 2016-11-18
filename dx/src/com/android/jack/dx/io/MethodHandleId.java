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

package com.android.jack.dx.io;

import com.android.jack.dx.rop.cst.CstMethodHandleRef.MethodHandleKind;
import com.android.jack.dx.util.Unsigned;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Method handle data contained in a dex file.
 */
public final class MethodHandleId implements Comparable<MethodHandleId> {
  @Nonnull
  private final DexBuffer buffer;
  @Nonnull
  private final MethodHandleKind kind;
  @Nonnegative
  private final int memberIdx;

  public MethodHandleId(@Nonnull DexBuffer buffer, @Nonnull MethodHandleKind kind,
      @Nonnegative int fieldOrMethodIdx) {
    this.buffer = buffer;
    this.kind = kind;
    this.memberIdx = fieldOrMethodIdx;
  }

  @Nonnull
  public MethodHandleKind getKind() {
    return kind;
  }

  @Nonnegative
  public int getMemberIndex() {
    return memberIdx;
  }

  @Override
  public int compareTo(@Nonnull MethodHandleId other) {
    if (kind.getValue() != other.getKind().getValue()) {
      return Unsigned.compare(kind.getValue(), other.getKind().getValue());
    }
    return Unsigned.compare(memberIdx, other.memberIdx);
  }

  public void writeTo(@Nonnull DexBuffer.Section out) {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  public final String toString() {
    if (buffer == null) {
      return kind.getValue() + " " + memberIdx;
    }

    switch (kind) {
      case PUT_INSTANCE:
      case PUT_STATIC:
      case GET_INSTANCE:
      case GET_STATIC: {
        return kind.getValue() + "." + buffer.fieldIds().get(memberIdx);
      }
      case INVOKE_CONSTRUCTOR:
      case INVOKE_INSTANCE:
      case INVOKE_STATIC: {
        return kind.getValue() + "." + buffer.methodIds().get(memberIdx);
      }
      default :
        throw new AssertionError();
    }
  }
}