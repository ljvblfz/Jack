/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.backend.dex.rop;

import com.android.jack.dx.rop.code.LocalItem;
import com.android.jack.dx.rop.code.RegisterSpec;
import com.android.jack.dx.rop.cst.CstString;
import com.android.jack.dx.rop.type.Type;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JThis;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.ir.ast.marker.GenericSignature;
import com.android.jack.ir.ast.marker.ThisRefTypeInfo;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

class RopRegisterManager {

  private int nextFreeReg = 0;

  /**
   * Keep a list of temporary register for each type.
   */
  private final Map<JType, List<RegisterSpec>> typeToTmpRegister =
      new Hashtable<JType, List<RegisterSpec>>();

  /**
   * Keep position of the next free register into {@code typeToTmpRegister}.
   */
  private final Map<JType, Integer> typeToNextPosFreeRegister = new Hashtable<JType, Integer>();

  /**
   * Keep mapping between variables of IR and dex registers.
   */
  @Nonnull
  private final Map<JVariable, RegisterSpec> variableToRegister =
      new Hashtable<JVariable, RegisterSpec>();

  @CheckForNull
  private RegisterSpec returnReg = null;
  @CheckForNull
  private RegisterSpec thisReg = null;

  private final boolean emitSyntheticDebugInfo;

  private final boolean emitDebugInfo;

  public RopRegisterManager(boolean emitDebugInfo, boolean emitSyntheticDebugInfo) {
    this.emitDebugInfo = emitDebugInfo;
    this.emitSyntheticDebugInfo = emitSyntheticDebugInfo;
  }

  /**
   * Create a {@link RegisterSpec} representing the variable {@code this}.
   * @param jThis The {@link JThis} we want the {@link RegisterSpec} for.
   * @return The built {@link RegisterSpec}.
   */
  @Nonnull
  RegisterSpec createThisReg(@Nonnull JThis jThis) {
    assert thisReg == null : "This register was already created.";
    JDefinedClassOrInterface type = (JDefinedClassOrInterface) jThis.getType();

    Type dexRegType = RopHelper.convertTypeToDx(type);
    String name = jThis.getName();
    if (emitDebugInfo && name != null) {
      CstString cstSignature = null;
      ThisRefTypeInfo thisMarker = type.getMarker(ThisRefTypeInfo.class);
      if (thisMarker != null && !thisMarker.getGenericSignature().isEmpty()) {
        cstSignature = new CstString(thisMarker.getGenericSignature());
      }
      LocalItem localItem =
          LocalItem.make(new CstString(name), RopHelper.getCstType(type), cstSignature);
      thisReg = RegisterSpec.make(nextFreeReg, dexRegType, localItem);
    } else {
      thisReg = RegisterSpec.make(nextFreeReg, dexRegType);
    }
    nextFreeReg += dexRegType.getCategory();

    assert thisReg != null;
    return (thisReg);
  }

  /**
   * Create a {@code RegisterSpec} from a {@code JType}.
   *
   * @param type The {@code JType} we want the {@code RegisterSpec} of.
   * @return The built {@code RegisterSpec}.
   */
  @Nonnull
  RegisterSpec createRegisterSpec(@Nonnull JType type) {

    Type dexRegType = RopHelper.convertTypeToDx(type);
    RegisterSpec reg = RegisterSpec.make(nextFreeReg, dexRegType);
    nextFreeReg += dexRegType.getCategory();

    return (reg);
  }

  /**
   * Create a {@link RegisterSpec} from a {@link JVariable}.
   *
   * @param var The {@link JVariable} we want the {@link RegisterSpec} for.
   * @return The built {@link RegisterSpec}.
   */
  @Nonnull
  RegisterSpec createRegisterSpec(@Nonnull JVariable var) {
    assert var instanceof JLocal || var instanceof JParameter;
    assert !variableToRegister.containsKey(var);

    JType type = var.getType();
    Type dexRegType = RopHelper.convertTypeToDx(type);
    RegisterSpec reg;
    if (emitDebugInfo && var.getName() != null && (emitSyntheticDebugInfo || !isSynthetic(var))) {
      CstString cstSignature = null;
      GenericSignature infoMarker = var.getMarker(GenericSignature.class);
      if (infoMarker != null) {
        cstSignature = new CstString(infoMarker.getGenericSignature());
      }
      LocalItem localItem =
          LocalItem.make(new CstString(var.getName()), RopHelper.getCstType(type), cstSignature);
      reg = RegisterSpec.make(nextFreeReg, dexRegType, localItem);
    } else {
      reg = RegisterSpec.make(nextFreeReg, dexRegType);
    }
    nextFreeReg += dexRegType.getCategory();

    variableToRegister.put(var, reg);

    return (reg);
  }

  private boolean isSynthetic(@Nonnull JVariable var) {
    return var.isSynthetic()
        || var.getName().startsWith("-p_") || var.getName().startsWith("-l_")
        || var.getName().startsWith("-s_") || var.getName().startsWith("-e_");
  }

  /**
   * Get a {@code RegisterSpec} from a {@code JVariableRef}.
   *
   * @param varRef The {@code JVariableRef} we want the {@code RegisterSpec} of.
   * @return The previously built {@code RegisterSpec}.
   */
  @Nonnull
  RegisterSpec getRegisterSpec(@Nonnull JVariableRef varRef) {
    if (varRef instanceof JThisRef) {
      assert thisReg != null : "This register was not created.";
      return (thisReg);
    }

    JVariable var = varRef.getTarget();
    assert variableToRegister.containsKey(var);

    RegisterSpec register = variableToRegister.get(var);
    assert RopHelper.areTypeCompatible(RopHelper.convertTypeToDx(varRef.getType()),
        register.getType());

    return register;
  }

  /**
   * Get the {@code RegisterSpec} with the type {@code type} to return value from method.
   *
   * @param returnType The return type of the method.
   * @return The {@code RegisterSpec} used to return result.
   */
  @Nonnull
  RegisterSpec getReturnReg(@Nonnull JType returnType) {
    RegisterSpec localReturnReg = returnReg;
    assert localReturnReg != null : "Return reg must be firstly created.";
    assert RopHelper.areTypeCompatible(
        RopHelper.convertTypeToDx(returnType), localReturnReg.getType());
    return (localReturnReg);
  }

  /**
   * Create a {@code RegisterSpec} with the type {@code type} to return value from method. The
   * register number for this register must be 0.
   *
   * @param returnType The return type of the method.
   * @return The {@code RegisterSpec} used to return result.
   */
  @Nonnull
  RegisterSpec createReturnReg(@Nonnull JType returnType) {
    assert returnReg == null;
    Type dexRegType = RopHelper.convertTypeToDx(returnType);
    returnReg = RegisterSpec.make(0, dexRegType);
    assert returnReg != null;
    return (returnReg);
  }

  @Nonnull
  RegisterSpec getOrCreateTmpRegister(@Nonnull JType type) {
    Integer nextFreeRegister = typeToNextPosFreeRegister.get(type);

    if (nextFreeRegister == null) {
      nextFreeRegister = Integer.valueOf(0);
      typeToNextPosFreeRegister.put(type, nextFreeRegister);
    }

    List<RegisterSpec> regSpecs = typeToTmpRegister.get(type);
    if (regSpecs == null) {
      regSpecs = new ArrayList<RegisterSpec>(2);
      typeToTmpRegister.put(type, regSpecs);
    }

    typeToNextPosFreeRegister.put(type, Integer.valueOf(nextFreeRegister.intValue() + 1));

    if (nextFreeRegister.intValue() < regSpecs.size()) {
      return regSpecs.get(nextFreeRegister.intValue());
    }

    Type dexRegType = RopHelper.convertTypeToDx(type);
    RegisterSpec regSpec = RegisterSpec.make(nextFreeReg, dexRegType);
    regSpecs.add(regSpec);
    nextFreeReg += dexRegType.getCategory();

    return regSpec;
  }

  void resetFreeTmpRegister() {
    for (JType type : typeToNextPosFreeRegister.keySet()) {
      typeToNextPosFreeRegister.put(type, Integer.valueOf(0));
    }
  }
}
