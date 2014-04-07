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

package com.android.jack.dx.ssa;

import com.android.jack.dx.rop.code.LocalItem;
import com.android.jack.dx.rop.code.RegisterSpec;
import com.android.jack.dx.rop.code.RegisterSpecList;
import com.android.jack.dx.rop.type.Type;
import com.android.jack.dx.rop.type.TypeBearer;

import java.util.BitSet;
import java.util.List;

/**
 * Resolves the result types of phi instructions. When phi instructions
 * are inserted, their result types are set to BT_VOID (which is a nonsensical
 * type for a register) but must be resolve to a real type before converting
 * out of SSA form.<p>
 *
 * The resolve is done as an iterative merge of each phi's operand types.
 * Phi operands may be themselves be the result of unresolved phis,
 * and the algorithm tries to find the most-fit type (for example, if every
 * operand is the same constant value or the same local variable info, we want
 * that to be reflected).<p>
 *
 * This algorithm assumes a dead-code remover has already removed all
 * circular-only phis that may have been inserted.
 */
public class PhiTypeResolver {

  SsaMethod ssaMeth;
  /** indexed by register; all registers still defined by unresolved phis */
  private final BitSet worklist;

  /**
   * Resolves all phi types in the method
   * @param ssaMeth method to process
   */
  public static void process(SsaMethod ssaMeth) {
    new PhiTypeResolver(ssaMeth).run();
  }

  private PhiTypeResolver(SsaMethod ssaMeth) {
    this.ssaMeth = ssaMeth;
    worklist = new BitSet(ssaMeth.getRegCount());
  }

  /**
   * Runs the phi-type resolver.
   */
  private void run() {

    int regCount = ssaMeth.getRegCount();

    for (int reg = 0; reg < regCount; reg++) {
      SsaInsn definsn = ssaMeth.getDefinitionForRegister(reg);

      if (definsn != null && (definsn.getResult().getBasicType() == Type.BT_VOID)) {
        worklist.set(reg);
      }
    }

    int reg;
    while (0 <= (reg = worklist.nextSetBit(0))) {
      worklist.clear(reg);

      /*
       * definitions on the worklist have a type of BT_VOID, which
       * must have originated from a PhiInsn.
       */
      PhiInsn definsn = (PhiInsn) ssaMeth.getDefinitionForRegister(reg);

      if (resolveResultType(definsn)) {
        /*
         * If the result type has changed, re-resolve all phis
         * that use this.
         */

List<SsaInsn> useList = ssaMeth.getUseListForRegister(reg);

        int sz = useList.size();
        for (int i = 0; i < sz; i++) {
          SsaInsn useInsn = useList.get(i);
          RegisterSpec resultReg = useInsn.getResult();
          if (resultReg != null && useInsn instanceof PhiInsn) {
            worklist.set(resultReg.getReg());
          }
        }
      }
    }
  }

  /**
   * Returns true if a and b are equal, whether
   * or not either of them are null.
   * @param a
   * @param b
   * @return true if equal
   */
  private static boolean equalsHandlesNulls(LocalItem a, LocalItem b) {
    return (a == b) || ((a != null) && a.equals(b));
  }

  /**
   * Resolves the result of a phi insn based on its operands. The "void"
   * type, which is a nonsensical type for a register, is used for
   * registers defined by as-of-yet-unresolved phi operations.
   *
   * @return true if the result type changed, false if no change
   */
  boolean resolveResultType(PhiInsn insn) {
    insn.updateSourcesToDefinitions(ssaMeth);

    RegisterSpecList sources = insn.getSources();

    // Start by finding the first non-void operand
    RegisterSpec first = null;
    int firstIndex = -1;

    int szSources = sources.size();
    for (int i = 0; i < szSources; i++) {
      RegisterSpec rs = sources.get(i);

      if (rs.getBasicType() != Type.BT_VOID) {
        first = rs;
        firstIndex = i;
      }
    }

    if (first == null) {
      // All operands are void -- we're not ready to resolve yet
      return false;
    }

    LocalItem firstLocal = first.getLocalItem();
    TypeBearer mergedType = first.getType();
    boolean sameLocals = true;
    for (int i = 0; i < szSources; i++) {
      if (i == firstIndex) {
        continue;
      }

      RegisterSpec rs = sources.get(i);

      // Just skip void (unresolved phi results) for now
      if (rs.getBasicType() == Type.BT_VOID) {
        continue;
      }

      sameLocals = sameLocals && equalsHandlesNulls(firstLocal, rs.getLocalItem());

      mergedType = mergeType(mergedType, rs.getType());
    }

    TypeBearer newResultType;

    if (mergedType != null) {
      newResultType = mergedType;
    } else {
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < szSources; i++) {
        sb.append(sources.get(i).toString());
        sb.append(' ');
      }

      throw new RuntimeException("Couldn't map types in phi insn:" + sb);
    }

    LocalItem newLocal = sameLocals ? firstLocal : null;

    RegisterSpec result = insn.getResult();

    if ((result.getTypeBearer() == newResultType)
        && equalsHandlesNulls(newLocal, result.getLocalItem())) {
      return false;
    }

    insn.changeResultType(newResultType, newLocal);

    return true;
  }

  /**
   * Merges two frame types.
   *
   * @param ft1 {@code non-null;} a frame type
   * @param ft2 {@code non-null;} another frame type
   * @return {@code non-null;} the result of merging the two types
   */
  private static TypeBearer mergeType(TypeBearer ft1, TypeBearer ft2) {
    if ((ft1 == null) || ft1.equals(ft2)) {
      return ft1;
    } else if (ft2 == null) {
      return null;
    } else {
      Type type1 = ft1.getType();
      Type type2 = ft2.getType();

      if (type1 == type2) {
        return type1;
      } else if (type1.isReference() && type2.isReference()) {
        if (type1 == Type.KNOWN_NULL) {
          /*
           * A known-null merges with any other reference type to
           * be that reference type.
           */
          return type2;
        } else if (type2 == Type.KNOWN_NULL) {
          /*
           * The same as above, but this time it's type2 that's
           * the known-null.
           */
          return type1;
        } else if (type1.isArray() && type2.isArray()) {
          TypeBearer componentUnion = mergeType(type1.getComponentType(), type2.getComponentType());
          if (componentUnion == null) {
            /*
             * At least one of the types is a primitive type,
             * so the merged result is just Object.
             */
            return Type.OBJECT;
          }
          return ((Type) componentUnion).getArrayType();
        } else {
          /*
           * All other unequal reference types get merged to be
           * Object in this phase. This is fine here, but it
           * won't be the right thing to do in the verifier.
           */
          return Type.OBJECT;
        }
      } else if (type1.isIntlike() && type2.isIntlike()) {
        /*
         * Merging two non-identical int-like types results in
         * the type int.
         */
        return Type.INT;
      } else {
        return null;
      }
    }
  }

}
