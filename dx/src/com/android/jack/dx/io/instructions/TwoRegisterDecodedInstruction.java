/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.jack.dx.io.instructions;

import com.android.jack.dx.io.IndexType;

/**
 * A decoded Dalvik instruction which has two register arguments.
 */
public final class TwoRegisterDecodedInstruction extends DecodedInstruction {
  /** register argument "A" */
  private final int a;

  /** register argument "B" */
  private final int b;

  /**
   * Constructs an instance.
   */
  public TwoRegisterDecodedInstruction(InstructionCodec format,
      int opcode,
      int index,
      IndexType indexType,
      int target,
      long literal,
      int a,
      int b) {
    super(format, opcode, index, indexType, target, literal);

    this.a = a;
    this.b = b;
  }

  @Override
  /** @inheritDoc */
  public int getRegisterCount() {
    return 2;
  }

  @Override
  /** @inheritDoc */
  public int getA() {
    return a;
  }

  @Override
  /** @inheritDoc */
  public int getB() {
    return b;
  }

  @Override
  /** @inheritDoc */
  public DecodedInstruction withIndex(int newFirstIndex, int newSecondIndex) {
    assert getSecondIndexType() == IndexType.NONE;
    return new TwoRegisterDecodedInstruction(getFormat(),
        getOpcode(),
        newFirstIndex,
        getFirstIndexType(),
        getTarget(),
        getLiteral(),
        a,
        b);
  }
}
