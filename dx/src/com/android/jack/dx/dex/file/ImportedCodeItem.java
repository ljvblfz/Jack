/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.jack.dx.dex.file;

import com.android.jack.dx.io.Code;
import com.android.jack.dx.io.Code.CatchHandler;
import com.android.jack.dx.io.Code.Try;
import com.android.jack.dx.io.CodeReader;
import com.android.jack.dx.io.Opcodes;
import com.android.jack.dx.io.instructions.DecodedInstruction;
import com.android.jack.dx.io.instructions.ShortArrayCodeOutput;
import com.android.jack.dx.rop.cst.Constant;
import com.android.jack.dx.rop.cst.CstIndexMap;
import com.android.jack.dx.rop.cst.CstMethodRef;
import com.android.jack.dx.util.AnnotatedOutput;
import com.android.jack.dx.util.ByteArrayAnnotatedOutput;
import com.android.jack.dx.util.DexException;
import com.android.jack.dx.util.Hex;

import java.io.PrintWriter;

/**
 * Representation of all the parts needed to import methods from a {@code dex} file into another.
 */
public final class ImportedCodeItem extends OffsettedItem implements
    com.android.jack.dx.dex.file.Code {

  /** {@code null-ok;} the imported debug info or {@code null} if there is none; */
  ImportedDebugInfoItem debugInfoItem = null;

  /** {@code non-null;} method that this code implements */
  private final CstMethodRef ref;

  /** {@code non-null;} code representing the imported method */
  private final Code code;

  /**
   * {@code non-null;} map index values used into code that references {@link Constant} from one
   * dex file into index values compliant with another dex file.
   */
  private final CstIndexMap cstIndexMap;

  /** Array of remapped instructions */
  private DecodedInstruction[] remappedInstructions;

  /** Index used during instructions remapping.*/
  private int remappingIndex;

  /** Binary representation of catch handlers. */
  private byte[] encodedHandlers;

  /** Array containing the result of catch handler remapping. */
  private int[] remappedCatchHandlerOffsets;

  /**
   * Constructs an instance.
   * @param ref {@code non-null;} method that this code implements
   * @param code {@code non-null;} the underlying code
   * @param debugInfoItem {@code null-ok;} the imported debug information of method {@code ref}
   * @param cstIndexMap {@code non-null;} maps constant index of one dex file into another
   */
  public ImportedCodeItem(CstMethodRef ref, Code code, ImportedDebugInfoItem debugInfoItem,
      CstIndexMap cstIndexMap) {
    super(ALIGNMENT, -1);

    if (ref == null) {
      throw new NullPointerException("ref == null");
    }
    this.ref = ref;

    if (code == null) {
      throw new NullPointerException("code == null");
    }
    this.code = code;

    this.debugInfoItem = debugInfoItem;

    if (cstIndexMap == null) {
      throw new NullPointerException("cstIndexMap == null");
    }
    this.cstIndexMap = cstIndexMap;
  }

  /** {@inheritDoc} */
  @Override
  public ItemType itemType() {
    return ItemType.TYPE_CODE_ITEM;
  }

  /** {@inheritDoc} */
  @Override
  public void addContents(DexFile file) {
    if (debugInfoItem != null) {
      file.getByteData().add(debugInfoItem);
    }
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return "CodeItem{" + toHuman() + "}";
  }

  /** {@inheritDoc} */
  @Override
  public String toHuman() {
    return ref.toHuman();
  }

  /**
   * Gets the reference to the method this instance implements.
   * @return {@code non-null;} the method reference
   */
  public CstMethodRef getRef() {
    return ref;
  }

  /** {@inheritDoc} */
  @Override
  protected void place0(Section addedTo, int offset) {
    int triesLength = code.getTries().length;

    encodedHandlers =
        triesLength != 0 ? encodeAndRemapCatchHandler(addedTo.getFile()) : new byte[0];

    int catchesSize = triesLength * CatchStructs.TRY_ITEM_WRITE_SIZE + encodedHandlers.length;

    int insnsSize = code.getInstructions().length;
    if ((insnsSize & 1) != 0) {
      // Requires padding to align tries and handlers
      insnsSize++;
    }

    setWriteSize(HEADER_SIZE + (insnsSize * 2) + catchesSize);
  }

  /** {@inheritDoc} */
  @Override
  protected void writeTo0(DexFile file, AnnotatedOutput out) {
    boolean annotates = out.annotates();
    int regSz = getRegistersSize();
    int outsSz = getOutsSize();
    int insSz = getInsSize();
    int insnsSz = code.getInstructions().length;
    boolean needPadding = (insnsSz & 1) != 0;
    int debugOff = (debugInfoItem == null) ? 0 : debugInfoItem.getAbsoluteOffset();
    int triesSz = code.getTries().length;

    if (annotates) {
      out.annotate(0, offsetString() + ' ' + ref.toHuman());
      out.annotate(2, "  registers_size: " + Hex.u2(regSz));
      out.annotate(2, "  ins_size:       " + Hex.u2(insSz));
      out.annotate(2, "  outs_size:      " + Hex.u2(outsSz));
      out.annotate(2, "  tries_size:     " + Hex.u2(triesSz));
      out.annotate(4, "  debug_off:      " + Hex.u4(debugOff));
      out.annotate(4, "  insns_size:     " + Hex.u4(insnsSz));
    }

    out.writeShort(regSz);
    out.writeShort(insSz);
    out.writeShort(outsSz);
    out.writeShort(triesSz);
    out.writeInt(debugOff);
    out.writeInt(insnsSz);

    for (short inst : encodeAndRemapCode(file, code.getInstructions())) {
      out.writeShort(inst);
    }

    if (triesSz != 0) {
      if (needPadding) {
        if (annotates) {
          out.annotate(2, "  padding: 0");
        }
        out.writeShort(0);
      }

      for (Try atry : code.getTries()) {
        out.writeInt(atry.getStartAddress());
        out.writeShort(atry.getInstructionCount());
        out.writeShort(remappedCatchHandlerOffsets[atry.getCatchHandlerIndex()]);
      }

      out.write(encodedHandlers);
    }

    if (annotates) {
      /*
       * These are pointed at in the code header (above), but it's less distracting to expand on
       * them at the bottom of the code.
       */
      if (debugInfoItem != null) {
        out.annotate(0, "  debug info");
        debugInfoItem.annotateTo(file, out, "    ");
      }
    }
  }

  /**
   * Encode and remap catch handlers.
   * @param file {@link DexFile} which will contains remapped catch handlers.
   * @return Byte array representing encoded catch handlers.
   */
  private byte[] encodeAndRemapCatchHandler(DexFile file) {
    ByteArrayAnnotatedOutput out = new ByteArrayAnnotatedOutput();

    CatchHandler[] catchHandlers = code.getCatchHandlers();
    out.writeUleb128(catchHandlers.length);

    remappedCatchHandlerOffsets = new int[catchHandlers.length];
    int catchHandlerIdx = 0;

    for (CatchHandler catchHandler : catchHandlers) {
      remappedCatchHandlerOffsets[catchHandlerIdx++] = out.getCursor();

      int catchAllAddress = catchHandler.getCatchAllAddress();
      int[] typeIndexes = catchHandler.getTypeIndexes();
      int[] addresses = catchHandler.getAddresses();

      if (catchAllAddress != -1) {
        out.writeSleb128(-typeIndexes.length);
      } else {
        out.writeSleb128(typeIndexes.length);
      }

      for (int i = 0; i < typeIndexes.length; i++) {
        out.writeUleb128(cstIndexMap.getRemappedCstTypeIndex(file, typeIndexes[i]));
        out.writeUleb128(addresses[i]);
      }

      if (catchAllAddress != -1) {
        out.writeUleb128(catchAllAddress);
      }
    }

    return out.toByteArray();
  }

  /**
   * Encode and remap code.
   * @param file {@link DexFile} which will contains remapped code.
   * @param insts Instructions in binary form that must be remap.
   * @return Remapped instructions in binary form.
   */
  private short[] encodeAndRemapCode(DexFile file, short[] insts) {
    CodeReader codeReader = new CodeReader();
    DecodedInstruction[] decodedInstructions = DecodedInstruction.decodeAll(insts);
    remappedInstructions = new DecodedInstruction[decodedInstructions.length];
    remappingIndex = 0;

    codeReader.setFallbackVisitor(new GenericVisitor());
    codeReader.setStringVisitor(new StringRemapper(file));
    codeReader.setFieldVisitor(new FieldRemapper(file));
    codeReader.setTypeVisitor(new TypeRemapper(file));
    codeReader.setMethodVisitor(new MethodRemapper(file));
    codeReader.setDualConstantVisitor(new DualConstantRemapper(file));

    codeReader.visitAll(decodedInstructions);

    ShortArrayCodeOutput outputCode = new ShortArrayCodeOutput(insts.length);
    for (DecodedInstruction instruction : remappedInstructions) {
      if (instruction != null) {
        instruction.encode(outputCode);
      }
    }

    return outputCode.getArray();
  }

  /**
   * Get the in registers count.
   * @return the count
   */
  private int getInsSize() {
    return code.getInsSize();
  }

  /**
   * Get the out registers count.
   * @return the count
   */
  private int getOutsSize() {
    return code.getOutsSize();
  }

  /**
   * Get the total registers count.
   * @return the count
   */
  private int getRegistersSize() {
    return code.getRegistersSize();
  }

  /**
   * Does a human-friendly dump of this instance.
   * @param out {@code non-null;} where to dump
   * @param prefix {@code non-null;} per-line prefix to use
   * @param verbose whether to be verbose with the output
   */
  @Override
  public void debugPrint(PrintWriter out, String prefix, boolean verbose) {
    throw new AssertionError("Not yet supported");
  }

  public CstIndexMap getCstIndexMap() {
    return cstIndexMap;
  }

  private class GenericVisitor implements CodeReader.Visitor {

    @Override
    public void visit(DecodedInstruction[] all, DecodedInstruction decodedInst) {
      remappedInstructions[remappingIndex++] = decodedInst;
    }
  }

  /**
   * {@link com.android.jack.dx.io.CodeReader.Visitor} remapping instructions using string index.
   */
  private class StringRemapper implements CodeReader.Visitor {

    private final DexFile file;

    public StringRemapper(DexFile dex) {
      this.file = dex;
    }

    @Override
    public void visit(DecodedInstruction[] all, DecodedInstruction decodedInst) {
      int newIndex = cstIndexMap.getRemappedCstStringIndex(file, decodedInst.getFirstIndex());

      if (decodedInst.getOpcode() != Opcodes.CONST_STRING_JUMBO && (newIndex > 0xffff)) {
        throw new DexException(
            "Cannot remap new index " + newIndex + " into a non-jumbo instruction!");
      }

      remappedInstructions[remappingIndex++] = decodedInst.withIndex(newIndex);
    }
  }

  /**
   * {@link com.android.jack.dx.io.CodeReader.Visitor} remapping instructions using field index.
   */
  private class FieldRemapper implements CodeReader.Visitor {

    private final DexFile file;

    public FieldRemapper(DexFile dex) {
      this.file = dex;
    }

    @Override
    public void visit(DecodedInstruction[] all, DecodedInstruction decodedInst) {
      remappedInstructions[remappingIndex++] = decodedInst.withIndex(
          cstIndexMap.getRemappedCstFieldRefIndex(file, decodedInst.getFirstIndex()));
    }
  }

  /**
   * {@link com.android.jack.dx.io.CodeReader.Visitor} remapping instructions using type index.
   */
  private class TypeRemapper implements CodeReader.Visitor {

    private final DexFile file;

    public TypeRemapper(DexFile dex) {
      this.file = dex;
    }

    @Override
    public void visit(DecodedInstruction[] all, DecodedInstruction decodedInst) {
      remappedInstructions[remappingIndex++] = decodedInst
          .withIndex(cstIndexMap.getRemappedCstTypeIndex(file, decodedInst.getFirstIndex()));
    }
  }

  /**
   * {@link com.android.jack.dx.io.CodeReader.Visitor} remapping instructions using method index.
   */
  private class MethodRemapper implements CodeReader.Visitor {

    private final DexFile file;

    public MethodRemapper(DexFile dex) {
      this.file = dex;
    }

    @Override
    public void visit(DecodedInstruction[] all, DecodedInstruction decodedInst) {
      remappedInstructions[remappingIndex++] = decodedInst.withIndex(
          cstIndexMap.getRemappedCstBaseMethodRefIndex(file, decodedInst.getFirstIndex()));
    }
  }

  /**
   * {@link com.android.jack.dx.io.CodeReader.Visitor} remapping instructions using two constants.
   */
  private class DualConstantRemapper implements CodeReader.Visitor {

    private final DexFile file;

    public DualConstantRemapper(DexFile dex) {
      this.file = dex;
    }

    @Override
    public void visit(DecodedInstruction[] all, DecodedInstruction decodedInst) {
      remappedInstructions[remappingIndex++] = decodedInst.withIndex(
          cstIndexMap.getRemappedCstBaseMethodRefIndex(file, decodedInst.getFirstIndex()),
          cstIndexMap.getRemappedCstPrototypeRefIndex(file, decodedInst.getSecondIndex()));
    }
  }
}
