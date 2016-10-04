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
import com.android.jack.dx.io.OpcodeInfo;

/**
 * A decoded Dalvik instruction which has one register argument.
 */
public final class OneRegisterDecodedInstruction extends DecodedInstruction {
  /** register argument "A" */
  private final int a;

  /**
   * Constructs an instance.
   */
  public OneRegisterDecodedInstruction(InstructionCodec format, int opcode, int firstIndex,
      IndexType firstIndexType, int target, long literal, int a) {
    this(format, opcode, firstIndex, firstIndexType, target, literal, a, /* secondIndex = */ 0,
        IndexType.NONE);
    assert !OpcodeInfo.hasDualConstants(opcode);
  }

  /**
   * Constructs an instance.
   */
  public OneRegisterDecodedInstruction(InstructionCodec format, int opcode, int firstIndex,
      IndexType firstIndexType, int target, long literal, int a, int secondIndex,
      IndexType secondIndexType) {
    super(format, opcode, firstIndex, firstIndexType, target, literal, secondIndex,
        secondIndexType);

    this.a = a;
  }

  @Override
  /** @inheritDoc */
  public int getRegisterCount() {
    return 1;
  }

  @Override
  /** @inheritDoc */
  public int getA() {
    return a;
  }

  @Override
  /** @inheritDoc */
  public DecodedInstruction withIndex(int newFirstIndex, int newSecondIndex) {
    return new OneRegisterDecodedInstruction(getFormat(), getOpcode(), newFirstIndex,
        getFirstIndexType(), getTarget(), getLiteral(), a, newSecondIndex, getSecondIndexType());
  }
}
