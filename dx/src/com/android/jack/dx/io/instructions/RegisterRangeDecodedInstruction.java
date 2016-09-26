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
 * A decoded Dalvik instruction which has register range arguments (an
 * "A" start register and a register count).
 */
public class RegisterRangeDecodedInstruction extends DecodedInstruction {
  /** register argument "A" */
  private final int a;

  /** register count */
  private final int registerCount;

  /**
   * Constructs an instance.
   */
  public RegisterRangeDecodedInstruction(InstructionCodec format,
      int opcode,
      int firstIndex,
      IndexType firstIndexType,
      int target,
      long literal,
      int a,
      int registerCount,
      int secondIndex,
      IndexType secondIndexType) {
    super(format, opcode, firstIndex, firstIndexType, target, literal, secondIndex,
        secondIndexType);

    this.a = a;
    this.registerCount = registerCount;
  }

  @Override
  /** @inheritDoc */
  public int getRegisterCount() {
    return registerCount;
  }

  @Override
  /** @inheritDoc */
  public int getA() {
    return a;
  }

  @Override
  /** @inheritDoc */
  public DecodedInstruction withIndex(int newFirstIndex, int newSecondIndex) {
    return new RegisterRangeDecodedInstruction(getFormat(),
        getOpcode(),
        newFirstIndex,
        getFirstIndexType(),
        getTarget(),
        getLiteral(),
        a,
        registerCount,
        newSecondIndex,
        getSecondIndexType());
  }
}
