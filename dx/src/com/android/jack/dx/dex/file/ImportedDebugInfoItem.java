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

import static com.android.jack.dx.dex.file.DebugInfoConstants.DBG_ADVANCE_LINE;
import static com.android.jack.dx.dex.file.DebugInfoConstants.DBG_ADVANCE_PC;
import static com.android.jack.dx.dex.file.DebugInfoConstants.DBG_END_LOCAL;
import static com.android.jack.dx.dex.file.DebugInfoConstants.DBG_END_SEQUENCE;
import static com.android.jack.dx.dex.file.DebugInfoConstants.DBG_RESTART_LOCAL;
import static com.android.jack.dx.dex.file.DebugInfoConstants.DBG_SET_EPILOGUE_BEGIN;
import static com.android.jack.dx.dex.file.DebugInfoConstants.DBG_SET_FILE;
import static com.android.jack.dx.dex.file.DebugInfoConstants.DBG_SET_PROLOGUE_END;
import static com.android.jack.dx.dex.file.DebugInfoConstants.DBG_START_LOCAL;
import static com.android.jack.dx.dex.file.DebugInfoConstants.DBG_START_LOCAL_EXTENDED;

import com.android.jack.dx.io.DexBuffer;
import com.android.jack.dx.rop.cst.Constant;
import com.android.jack.dx.rop.cst.CstIndexMap;
import com.android.jack.dx.util.AnnotatedOutput;

import java.io.PrintWriter;

/**
 * TODO(jack team)
 */
public class ImportedDebugInfoItem extends OffsettedItem {

  /** the required alignment for instances of this class */
  private static final int ALIGNMENT = 1;

  private final DexBuffer dexBuffer;

  private final int debugInfoOffset;

  private final int debugInfoSize;

  /**
   * {@code non-null;} map index values used into debug information that references {@link Constant}
   * from one dex file into index values compliant with another dex file.
   */
  private CstIndexMap cstIndexMap;

  public ImportedDebugInfoItem(DexBuffer dexBuffer, int debugInfoOffset, int debugInfoSize,
      CstIndexMap cstIndexMap) {
    // We don't know the write size yet.
    super(ALIGNMENT, -1);

    assert dexBuffer != null;

    this.dexBuffer = dexBuffer;

    this.debugInfoOffset = debugInfoOffset;

    this.debugInfoSize = debugInfoSize;

    this.cstIndexMap = cstIndexMap;
  }

  /** {@inheritDoc} */
  @Override
  public ItemType itemType() {
    return ItemType.TYPE_DEBUG_INFO_ITEM;
  }

  /** {@inheritDoc} */
  @Override
  public void addContents(DexFile file) {
    // Nothing to do
  }


  /** {@inheritDoc} */
  @Override
  protected void place0(Section addedTo, int offset) {
    setWriteSize(debugInfoSize);
  }

  /** {@inheritDoc} */
  @Override
  public String toHuman() {
    throw new RuntimeException("unsupported");
  }

  /**
   * Writes annotations for the elements of this list, as zero-length. This is meant to be used for
   * dumping this instance directly after a code dump (with the real local list actually existing
   * elsewhere in the output).
   * @param file {@code non-null;} the file to use for referencing other sections
   * @param out {@code non-null;} where to annotate to
   * @param prefix {@code null-ok;} prefix to attach to each line of output
   */
  public void annotateTo(DexFile file, AnnotatedOutput out, String prefix) {
    throw new RuntimeException("unsupported");
  }

  /**
   * Does a human-friendly dump of this instance.
   * @param out {@code non-null;} where to dump
   * @param prefix {@code non-null;} prefix to attach to each line of output
   */
  public void debugPrint(PrintWriter out, String prefix) {
    throw new RuntimeException("unsupported");
  }

  /** {@inheritDoc} */
  @Override
  protected void writeTo0(DexFile file, AnnotatedOutput out) {
    encodeAndRemapDebugInfoItem(file, out);
  }

  private void encodeAndRemapDebugInfoItem(DexFile file, AnnotatedOutput out) {
    com.android.jack.dx.io.DexBuffer.Section in = dexBuffer.open(debugInfoOffset);

    int lineStart = in.readUleb128();
    out.writeUleb128(lineStart);

    int parametersSize = in.readUleb128();
    out.writeUleb128(parametersSize);

    for (int p = 0; p < parametersSize; p++) {
      int parameterName = in.readUleb128p1();
      out.writeUleb128(cstIndexMap.getRemappedCstStringIndex(file, parameterName) + 1);
    }

    int addrDiff; // uleb128 address delta.
    int lineDiff; // sleb128 line delta.
    int registerNum; // uleb128 register number.
    int nameIndex; // uleb128p1 string index. Needs indexMap adjustment.
    int typeIndex; // uleb128p1 type index. Needs indexMap adjustment.
    int sigIndex; // uleb128p1 string index. Needs indexMap adjustment.

    while (true) {
      int opcode = in.readByte();
      out.writeByte(opcode);

      switch (opcode) {
        case DBG_END_SEQUENCE:
          return;

        case DBG_ADVANCE_PC:
          addrDiff = in.readUleb128();
          out.writeUleb128(addrDiff);
          break;

        case DBG_ADVANCE_LINE:
          lineDiff = in.readSleb128();
          out.writeSleb128(lineDiff);
          break;

        case DBG_START_LOCAL:
        case DBG_START_LOCAL_EXTENDED:
          registerNum = in.readUleb128();
          out.writeUleb128(registerNum);
          nameIndex = in.readUleb128p1();
          out.writeUleb128(cstIndexMap.getRemappedCstStringIndex(file, nameIndex) + 1);
          typeIndex = in.readUleb128p1();
          out.writeUleb128(cstIndexMap.getRemappedCstTypeIndex(file, typeIndex) + 1);
          if (opcode == DBG_START_LOCAL_EXTENDED) {
            sigIndex = in.readUleb128p1();
            out.writeUleb128(cstIndexMap.getRemappedCstStringIndex(file, sigIndex) + 1);
          }
          break;

        case DBG_END_LOCAL:
        case DBG_RESTART_LOCAL:
          registerNum = in.readUleb128();
          out.writeUleb128(registerNum);
          break;

        case DBG_SET_FILE:
          nameIndex = in.readUleb128p1();
          out.writeUleb128(cstIndexMap.getRemappedCstStringIndex(file, nameIndex) + 1);
          break;

        case DBG_SET_PROLOGUE_END:
        case DBG_SET_EPILOGUE_BEGIN:
        default:
          break;
      }
    }
  }
}
