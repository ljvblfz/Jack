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
import com.android.jack.dx.io.Opcodes;
import com.android.jack.dx.util.DexException;
import com.android.jack.dx.util.Hex;

import java.io.EOFException;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A decoded Dalvik instruction. This consists of a format codec, a
 * numeric opcode, an optional index type, and any additional
 * arguments of the instruction. The additional arguments (if any) are
 * represented as uninterpreted data.
 *
 * <p><b>Note:</b> The names of the arguments are <i>not</i> meant to
 * match the names given in the Dalvik instruction format
 * specification, specification which just names fields (somewhat)
 * arbitrarily alphabetically from A. In this class, non-register
 * fields are given descriptive names and register fields are
 * consistently named alphabetically.</p>
 */
public abstract class DecodedInstruction {
  /** non-null; instruction format / codec */
  @Nonnull
  private final InstructionCodec format;

  /** opcode number */
  @Nonnegative
  private final int opcode;

  /** first constant index argument */
  private final int firstIndex;

  /** first index type */
  @Nonnull
  private final IndexType firstIndexType;

  /** second constant index argument */
  private final int secondIndex;

  /** second index type */
  @Nonnull
  private final IndexType secondIndexType;

  /**
   * target address argument. This is an absolute address, not just a signed offset. <b>Note:</b>
   * The address is unsigned, even though it is stored in an {@code int}.
   */
  @Nonnegative
  private final int target;

  /**
   * literal value argument; also used for special verification error
   * constants (format 20bc) as well as should-be-zero values
   * (formats 10x, 20t, 30t, and 32x)
   */
  private final long literal;

  /**
   * Decodes an instruction from the given input source.
   */
  @Nonnull
  public static DecodedInstruction decode(@Nonnull CodeInput in) throws EOFException {
    int opcodeUnit = in.read();
    int opcode = Opcodes.extractOpcodeFromUnit(opcodeUnit);
    InstructionCodec format = OpcodeInfo.getFormat(opcode);

    return format.decode(opcodeUnit, in);
  }

  /**
   * Decodes an array of instructions. The result has non-null
   * elements at each offset that represents the start of an
   * instruction.
   */
  @Nonnull
  public static DecodedInstruction[] decodeAll(@Nonnull short[] encodedInstructions) {
    int size = encodedInstructions.length;
    DecodedInstruction[] decoded = new DecodedInstruction[size];
    ShortArrayCodeInput in = new ShortArrayCodeInput(encodedInstructions);

    try {
      while (in.hasMore()) {
        decoded[in.cursor()] = DecodedInstruction.decode(in);
      }
    } catch (EOFException ex) {
      throw new DexException(ex);
    }

    return decoded;
  }

  /**
   * Constructs an instance.
   */
  public DecodedInstruction(@Nonnull InstructionCodec format, @Nonnegative int opcode,
      int firstIndex, @Nonnull IndexType firstIndexType, @Nonnegative int target, long literal) {
    this(format, opcode, firstIndex, firstIndexType, target, literal, /* secondIndex = */ 0,
        IndexType.NONE);
  }

  /**
   * Constructs an instance.
   */
  public DecodedInstruction(@Nonnull InstructionCodec format, @Nonnegative int opcode,
      @Nonnegative int firstIndex, @Nonnull IndexType firstIndexType, @Nonnegative int target,
      long literal, @Nonnegative int secondIndex, @Nonnull IndexType secondIndexType) {
    assert format != null;
    assert Opcodes.isValidShape(opcode);

    this.format = format;
    this.opcode = opcode;
    this.firstIndex = firstIndex;
    this.firstIndexType = firstIndexType;
    this.target = target;
    this.literal = literal;
    this.secondIndex = secondIndex;
    this.secondIndexType = secondIndexType;
  }

  @Nonnull
  public final InstructionCodec getFormat() {
    return format;
  }

  @Nonnegative
  public final int getOpcode() {
    return opcode;
  }

  /**
   * Gets the opcode, as a code unit.
   */
  @Nonnegative
  public final short getOpcodeUnit() {
    return (short) opcode;
  }

  public final int getFirstIndex() {
    assert firstIndexType != IndexType.NONE;
    return firstIndex;
  }

  public final int getSecondIndex() {
    assert secondIndexType != IndexType.NONE;
    return secondIndex;
  }

  /**
   * Gets the first index, as a code unit.
   */
  public final short getFirstIndexUnit() {
    assert firstIndexType != IndexType.NONE;
    return (short) firstIndex;
  }

  /**
   * Gets the second index, as a code unit.
   */
  public final short getSecondIndexUnit() {
    assert secondIndexType != IndexType.NONE;
    return (short) secondIndex;
  }

  @Nonnull
  public final IndexType getFirstIndexType() {
    return firstIndexType;
  }

  @Nonnull
  public final IndexType getSecondIndexType() {
    return secondIndexType;
  }

  /**
   * Gets the raw target.
   */
  @Nonnegative
  public final int getTarget() {
    return target;
  }

  /**
   * Gets the target as a relative offset from the given address.
   */
  public final int getTarget(int baseAddress) {
    return target - baseAddress;
  }

  /**
   * Gets the target as a relative offset from the given base
   * address, as a code unit. This will throw if the value is out of
   * the range of a signed code unit.
   */
  public final short getTargetUnit(int baseAddress) {
    int relativeTarget = getTarget(baseAddress);

    if (relativeTarget != (short) relativeTarget) {
      throw new DexException("Target out of range: " + Hex.s4(relativeTarget));
    }

    return (short) relativeTarget;
  }

  /**
   * Gets the target as a relative offset from the given base
   * address, masked to be a byte in size. This will throw if the
   * value is out of the range of a signed byte.
   */
  public final int getTargetByte(int baseAddress) {
    int relativeTarget = getTarget(baseAddress);

    if (relativeTarget != (byte) relativeTarget) {
      throw new DexException("Target out of range: " + Hex.s4(relativeTarget));
    }

    return relativeTarget & 0xff;
  }

  public final long getLiteral() {
    return literal;
  }

  /**
   * Gets the literal value, masked to be an int in size. This will
   * throw if the value is out of the range of a signed int.
   */
  public final int getLiteralInt() {
    if (literal != (int) literal) {
      throw new DexException("Literal out of range: " + Hex.u8(literal));
    }

    return (int) literal;
  }

  /**
   * Gets the literal value, as a code unit. This will throw if the
   * value is out of the range of a signed code unit.
   */
  public final short getLiteralUnit() {
    if (literal != (short) literal) {
      throw new DexException("Literal out of range: " + Hex.u8(literal));
    }

    return (short) literal;
  }

  /**
   * Gets the literal value, masked to be a byte in size. This will
   * throw if the value is out of the range of a signed byte.
   */
  public final int getLiteralByte() {
    if (literal != (byte) literal) {
      throw new DexException("Literal out of range: " + Hex.u8(literal));
    }

    return (int) literal & 0xff;
  }

  /**
   * Gets the literal value, masked to be a nibble in size. This
   * will throw if the value is out of the range of a signed nibble.
   */
  public final int getLiteralNibble() {
    if ((literal < -8) || (literal > 7)) {
      throw new DexException("Literal out of range: " + Hex.u8(literal));
    }

    return (int) literal & 0xf;
  }

  @Nonnegative
  public abstract int getRegisterCount();

  @Nonnegative
  public int getA() {
    return 0;
  }

  @Nonnegative
  public int getB() {
    return 0;
  }

  @Nonnegative
  public int getC() {
    return 0;
  }

  @Nonnegative
  public int getD() {
    return 0;
  }

  @Nonnegative
  public int getE() {
    return 0;
  }

  /**
   * Gets the A register number, as a code unit. This will throw if the
   * value is out of the range of an unsigned code unit.
   */
  public final short getAUnit() {
    int a = getA();

    if ((a & ~0xffff) != 0) {
      throw new DexException("Register A out of range: " + Hex.u8(a));
    }

    return (short) a;
  }

  /**
   * Gets the B register number, as a code unit. This will throw if the
   * value is out of the range of an unsigned code unit.
   */
  public final short getBUnit() {
    int b = getB();

    if ((b & ~0xffff) != 0) {
      throw new DexException("Register B out of range: " + Hex.u8(b));
    }

    return (short) b;
  }

  /**
   * Encodes this instance to the given output.
   */
  public final void encode(@Nonnull CodeOutput out) {
    format.encode(this, out);
  }

  /**
   * Returns an instance just like this one, except with the index replaced
   * with the given one.
   */
  @Nonnull
  public DecodedInstruction withIndex(int newFirstIndex) {
    return withIndex(newFirstIndex, /* newSecondIndex = */ 0);
  }

  /**
   * Returns an instance just like this one, except with the indexes replaced with the given
   * indexes.
   */
  @Nonnull
  public abstract DecodedInstruction withIndex(int newFirstIndex, int newSecondIndex);
}
