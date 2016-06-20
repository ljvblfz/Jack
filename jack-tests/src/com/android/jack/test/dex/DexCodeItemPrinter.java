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

package com.android.jack.test.dex;

import org.jf.dexlib.Code.FiveRegisterInstruction;
import org.jf.dexlib.Code.Format.Format;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.InstructionWithReference;
import org.jf.dexlib.Code.LiteralInstruction;
import org.jf.dexlib.Code.MultiOffsetInstruction;
import org.jf.dexlib.Code.OffsetInstruction;
import org.jf.dexlib.Code.SingleRegisterInstruction;
import org.jf.dexlib.Code.ThreeRegisterInstruction;
import org.jf.dexlib.Code.TwoRegisterInstruction;
import org.jf.dexlib.CodeItem;
import org.jf.dexlib.Item;
import org.jf.dexlib.MethodIdItem;

import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;

/** Formats Dex code item in a format used in tests */
public final class DexCodeItemPrinter {
  @Nonnull
  public static String print(@Nonnull MethodIdItem method, @Nonnull CodeItem code) {
    return new DexCodeItemPrinter(method, code).print();
  }

  @Nonnull
  private final org.jf.dexlib.MethodIdItem method;

  @Nonnull
  private final org.jf.dexlib.CodeItem code;

  @Nonnull
  private final StringBuilder builder = new StringBuilder();

  private DexCodeItemPrinter(@Nonnull MethodIdItem method, @Nonnull CodeItem code) {
    this.method = method;
    this.code = code;
  }

  private DexCodeItemPrinter write(@Nonnull String s) {
    builder.append(s);
    return this;
  }

  private DexCodeItemPrinter write(@Nonnull Item ref) {
    return write(ref.getConciseIdentity());
  }

  private DexCodeItemPrinter write(long i) {
    builder.append(i);
    return this;
  }

  private DexCodeItemPrinter nl() {
    return write("\n");
  }

  private String var(int v) {
    return "v" + v;
  }

  private DexCodeItemPrinter registers(@Nonnull Instruction instr) {
    if (instr instanceof FiveRegisterInstruction) {
      return write(var(((FiveRegisterInstruction) instr).getRegisterA()))
          .write(", ").write(var(((FiveRegisterInstruction) instr).getRegisterD()))
          .write(", ").write(var(((FiveRegisterInstruction) instr).getRegisterE()))
          .write(", ").write(var(((FiveRegisterInstruction) instr).getRegisterF()))
          .write(", ").write(var(((FiveRegisterInstruction) instr).getRegisterG()));
    }
    if (instr instanceof ThreeRegisterInstruction) {
      return write(var(((ThreeRegisterInstruction) instr).getRegisterA()))
          .write(", ").write(var(((ThreeRegisterInstruction) instr).getRegisterB()))
          .write(", ").write(var(((ThreeRegisterInstruction) instr).getRegisterC()));
    }
    if (instr instanceof TwoRegisterInstruction) {
      return write(var(((TwoRegisterInstruction) instr).getRegisterA()))
          .write(", ").write(var(((TwoRegisterInstruction) instr).getRegisterB()));
    }
    return write(var(((SingleRegisterInstruction) instr).getRegisterA()));
  }

  private DexCodeItemPrinter literal(@Nonnull Instruction instr) {
    return write(((LiteralInstruction) instr).getLiteral());
  }

  private DexCodeItemPrinter reference(@Nonnull Instruction instr) {
    return write(((InstructionWithReference) instr).getReferencedItem());
  }

  private DexCodeItemPrinter address(
      @Nonnull Instruction instr, int addr,
      @Nonnull TreeMap<Integer, String> references) {
    addr += ((OffsetInstruction) instr).getTargetAddressOffset();
    String label = references.get(addr);
    assert label != null;
    return write(label);
  }

  private void dump(
      @Nonnull Instruction instr, int address,
      @Nonnull TreeMap<Integer, String> references) {
    write(instr.opcode.name);

    Format format = instr.getFormat();
    switch (format) {
      case Format10x:
        break;

      case Format10t:
      case Format20t:
      case Format30t:
        write(" ").address(instr, address, references);
        break;

      case Format21t:
      case Format31t:
        write(" ").registers(instr).write(", ").address(instr, address, references);
        break;

      case Format11x:
      case Format12x:
      case Format23x:
        write(" ").registers(instr);
        break;

      case Format11n:
      case Format21h:
      case Format21s:
      case Format22b:
      case Format22s:
      case Format31i:
        write(" ").registers(instr).write(", ").literal(instr);
        break;

      case Format21c:
      case Format22c:
      case Format31c:
      case Format35c:
        write(" ").registers(instr).write(", ").reference(instr);
        break;

      default:
        write(" // Details are not implemented yet for this format: " + format);
        break;
    }

    nl();
  }

  private void dump(@Nonnull Instruction[] instructions) {
    String indent = "    | ";

    TreeMap<Integer, String> references = getReferencedAddresses(instructions);

    int address = 0;
    for (Instruction instr : instructions) {
      // Indentation with optional label
      String label = references.get(address);
      write(label == null ? indent : (label + "-> "));
      dump(instr, address, references);
      address += instr.getSize(address);
    }
  }

  @Nonnull
  private TreeMap<Integer, String> getReferencedAddresses(@Nonnull Instruction[] instructions) {
    int address = 0;
    TreeMap<Integer, String> references = new TreeMap<Integer, String>();
    for (Instruction instr : instructions) {
      if (instr instanceof OffsetInstruction) {
        references.put(address + ((OffsetInstruction) instr).getTargetAddressOffset(), null);
      } else if (instr instanceof MultiOffsetInstruction) {
        for (int target : ((MultiOffsetInstruction) instr).getTargets()) {
          references.put(target, null);
        }
      }
      address += instr.getSize(address);
    }

    int index = 0;
    for (Map.Entry<Integer, String> entry : references.entrySet()) {
      entry.setValue((index < 10 ? "#0" : "#") + (index++));
    }
    return references;
  }

  private void dump(@Nonnull CodeItem.TryItem[] tries) {
    write(" | !!!! NOT IMPLEMENTED YET ").nl();
  }

  private void dump(@Nonnull CodeItem.EncodedCatchHandler[] catches) {
    write(" | !!!! NOT IMPLEMENTED YET ").nl();
  }

  @Nonnull
  private String print() {
    write("method: ").write(method.getMethodString()).nl();

    write("registers: ").write(code.getRegisterCount())
        .write(", in/out: ").write(code.getInWords())
        .write("/").write(code.getOutWords()).nl();

    Instruction[] instructions = code.getInstructions();
    if (instructions != null) {
      write("instructions: ").write(instructions.length).nl();
      dump(instructions);
    }

    CodeItem.TryItem[] tries = code.getTries();
    if (tries != null) {
      write("tries: ").write(tries.length).nl();
      dump(tries);
    }

    CodeItem.EncodedCatchHandler[] handlers = code.getHandlers();
    if (handlers != null) {
      write("catches: ").write(handlers.length).nl();
      dump(handlers);
    }

    return builder.toString();
  }
}
