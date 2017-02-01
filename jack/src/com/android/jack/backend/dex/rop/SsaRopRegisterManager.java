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

import com.android.jack.debug.DebugVariableInfoMarker;
import com.android.jack.dx.rop.code.LocalItem;
import com.android.jack.dx.rop.code.RegisterSpec;
import com.android.jack.dx.rop.cst.CstString;
import com.android.jack.dx.rop.cst.CstType;
import com.android.jack.dx.rop.type.Type;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JSsaVariableDefRef;
import com.android.jack.ir.ast.JSsaVariableRef;
import com.android.jack.ir.ast.JSsaVariableUseRef;
import com.android.jack.ir.ast.JThis;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.marker.GenericSignature;
import com.android.jack.ir.ast.marker.ThisRefTypeInfo;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

class SsaRopRegisterManager {

  private int nextFreeReg = 0;

  @CheckForNull
  private RegisterSpec returnReg = null;
  @CheckForNull
  private RegisterSpec thisReg = null;

  private final boolean emitSyntheticDebugInfo;

  private final boolean emitDebugInfo;

  public SsaRopRegisterManager(boolean emitDebugInfo, boolean emitSyntheticDebugInfo) {
    this.emitDebugInfo = emitDebugInfo;
    this.emitSyntheticDebugInfo = emitSyntheticDebugInfo;
  }

  public int getRegisterCount() {
    return nextFreeReg;
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
      assert jThis.getMarker(GenericSignature.class) == null;
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
   * Return the register number associated with a {@link JVariable}
   * @return The register number of a  {@link JVariable}.
   */
  @Nonnegative
  int getRegisterNumber(@Nonnull JSsaVariableRef ref) {
    JVariable variable = ref.getTarget();
    Type dexRegType = RopHelper.convertTypeToDx(variable.getType());
    JSsaVariableDefRef def = ref instanceof JSsaVariableUseRef ? ((JSsaVariableUseRef) ref).getDef()
        : (JSsaVariableDefRef) ref;
    int regNum = def.getRopRegister();
    if (regNum != -1) {
      if (nextFreeReg <= regNum) {
        nextFreeReg = regNum + dexRegType.getCategory();
      }
      return regNum;
    } else {
      def.setRopRegister(nextFreeReg);
      nextFreeReg += dexRegType.getCategory();
      return def.getRopRegister();
    }
  }

  @Nonnull
  RegisterSpec getOrCreateRegisterSpec(@Nonnull JParameter param) {
    return getOrCreateRegisterSpec(
        new JSsaVariableDefRef(param.getSourceInfo(), param, 0));
  }

  /**
   * Get a {@code RegisterSpec} from a {@code JVariableRef}.
   *
   * @param varRef The {@code JVariableRef} we want the {@code RegisterSpec} of.
   * @return The previously built {@code RegisterSpec}.
   */
  @Nonnull
  RegisterSpec getOrCreateRegisterSpec(@Nonnull JSsaVariableRef varRef) {
    if (varRef.getTarget() instanceof JThis) {
      assert thisReg != null : "This register was not created.";
      return (thisReg);
    }

    JVariable variable = varRef.getTarget();
    RegisterSpec register = getRegisterSpec(getRegisterNumber(varRef), variable,
        varRef.getMarker(DebugVariableInfoMarker.class));

    assert RopHelper.areTypeCompatible(
        RopHelper.convertTypeToDx(varRef.getType()),
        register.getType());

    return register;
  }

  @Nonnull
  RegisterSpec getOrCreateRegisterSpec(@Nonnull JSsaVariableRef varRef,
      @CheckForNull LocalItem localItem) {
    if (varRef.getTarget() instanceof JThis) {
      assert thisReg != null : "This register was not created.";
      return (thisReg);
    }

    JVariable variable = varRef.getTarget();
    RegisterSpec register = getRegisterSpec(getRegisterNumber(varRef), variable, localItem);

    assert RopHelper.areTypeCompatible(
        RopHelper.convertTypeToDx(varRef.getType()),
        register.getType());

    return register;
  }

  @Nonnull
  private RegisterSpec getRegisterSpec(@Nonnegative int regNum, @Nonnull JVariable variable,
      @CheckForNull DebugVariableInfoMarker debugInfo) {
    RegisterSpec reg;
    JType variableType = variable.getType();
    Type regType = RopHelper.convertTypeToDx(variableType);

    if (emitDebugInfo && variable.getName() != null
        && (emitSyntheticDebugInfo || !variable.isSynthetic())) {
      if (debugInfo != null) {
        // Debug info marker exists, uses debug information from it
        CstString cstSignature = null;
        if (debugInfo.getGenericSignature() != null) {
          cstSignature = new CstString(debugInfo.getGenericSignature());
        }
        LocalItem localItem = LocalItem.make(new CstString(debugInfo.getName()),
            CstType.intern(RopHelper.convertTypeToDx(debugInfo.getType())), cstSignature);
        reg = RegisterSpec.make(regNum, regType, localItem);
      } else {
        CstString cstSignature = null;
        GenericSignature infoMarker = variable.getMarker(GenericSignature.class);
        if (infoMarker != null) {
          cstSignature = new CstString(infoMarker.getGenericSignature());
        }
        LocalItem localItem = LocalItem.make(new CstString(variable.getName()),
            CstType.intern(regType), cstSignature);
        reg = RegisterSpec.make(regNum, regType, localItem);
      }
    } else {
      reg = RegisterSpec.make(regNum, regType);
    }

    return (reg);
  }

  @Nonnull
  private RegisterSpec getRegisterSpec(@Nonnegative int regNum, @Nonnull JVariable variable,
      @Nonnull LocalItem localItem) {
    RegisterSpec reg;
    JType variableType = variable.getType();
    Type regType = RopHelper.convertTypeToDx(variableType);
    reg = RegisterSpec.make(regNum, regType, localItem);
    return reg;
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
  RegisterSpec getOrCreateTmpRegister(@Nonnull Type dexRegType) {
    RegisterSpec regSpec = RegisterSpec.make(nextFreeReg, dexRegType);
    nextFreeReg += dexRegType.getCategory();
    return regSpec;
  }

  @Nonnull
  RegisterSpec getThisReg() {
    assert thisReg != null;
    return thisReg;
  }
}