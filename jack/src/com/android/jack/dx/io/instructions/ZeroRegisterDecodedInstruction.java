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
 * A decoded Dalvik instruction which has no register arguments.
 */
public final class ZeroRegisterDecodedInstruction extends DecodedInstruction {

  /**
   * Constructs an instance.
   */
  public ZeroRegisterDecodedInstruction(InstructionCodec format, int opcode, int firstIndex,
      IndexType firstIndexType, int target, long literal) {
    this(format, opcode, firstIndex, firstIndexType, target, literal, /* secondIndex = */ 0,
        IndexType.NONE);
    assert !OpcodeInfo.hasDualConstants(opcode);
  }

  /**
   * Constructs an instance.
   */
  public ZeroRegisterDecodedInstruction(InstructionCodec format, int opcode, int firstIndex,
      IndexType firstIndexType, int target, long literal, int secondIndex,
      IndexType secondIndexType) {
    super(format, opcode, firstIndex, firstIndexType, target, literal, secondIndex,
        secondIndexType);
  }

  @Override
  /** @inheritDoc */
  public int getRegisterCount() {
    return 0;
  }

  @Override
  /** @inheritDoc */
  public DecodedInstruction withIndex(int newFirstIndex, int newSecondIndex) {
    return new ZeroRegisterDecodedInstruction(getFormat(), getOpcode(), newFirstIndex,
        getFirstIndexType(), getTarget(), getLiteral(), newSecondIndex, getSecondIndexType());
  }
}
