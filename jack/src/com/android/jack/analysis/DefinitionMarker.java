/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.analysis;

import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JExceptionRuntimeValue;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JThis;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JVariableRef;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Marker used to represent a variable definition and his usage.
 */
@Description("Marker used to represent a variable definition his usage.")
@ValidOn(value = {JAsgOperation.class, JParameter.class, JThis.class})
public class DefinitionMarker implements Marker {

  @Nonnull
  private final JNode definition;

  /**
   * Variables reference using this definition.
   */
  @Nonnull
  final List<JVariableRef> uses;

  @Nonnegative
  private int bitSetIdx;

  public DefinitionMarker(@Nonnull JNode definition) {
    this.definition = definition;
    uses = new ArrayList<JVariableRef>();
  }

  @Override
  public Marker cloneIfNeeded() {
    throw new AssertionError("It is not valid to use cloneIfNeeded, create a new marker.");
  }

  public boolean isUnused() {
    return uses.isEmpty();
  }

  public boolean isUsedOnlyOnce() {
    return uses.size() == 1;
  }

  @Nonnull
  public List<JVariableRef> getUses() {
    // Uses of definitions must return a new list to support concurrent modifications.
    return new ArrayList<JVariableRef>(uses);
  }

  /**
   * Remove a use of this definition and update use/defs chain.
   * @param use Variable reference to remove from definition usage.
   */
  public void removeUse(@Nonnull JVariableRef use) {
    assert uses.contains(use);
    uses.remove(use);

    removeDefFromUseDefsChain(use);
  }

  /**
   * Add a use of this definition and update use/defs chain.
   * @param use Variable reference to add into definition usage.
   */
  public void addUse(@Nonnull JVariableRef use) {
    assert !uses.contains(use);
    uses.add(use);

    UseDefsMarker udm = use.getMarker(UseDefsMarker.class);
    assert udm != null;
    // Do not addUsedDefinition of UseDefsMarker to avoid recursion.
    udm.defs.add(this);
  }

  /**
   * Remove all uses of this definition and update use/defs chains.
   */
  public void removeAllUses() {
    for (JVariableRef useOfDef : uses) {
      removeDefFromUseDefsChain(useOfDef);
    }

    uses.clear();
  }

  /**
   * Remove all uses of this definition without updating use/defs chains.
   */
  public void clearUses() {
    uses.clear();
  }

  public boolean hasValue() {
    if (definition instanceof JAsgOperation) {
      JExpression rhsExpr = ((JAsgOperation) definition).getRhs();
      return !(rhsExpr instanceof JExceptionRuntimeValue);
    }

    assert definition instanceof JParameter || definition instanceof JThis;
    return false;
  }

  @Nonnull
  public JNode getDefinedExpr() {
    if (definition instanceof JAsgOperation) {
      JExpression lhsExpr = ((JAsgOperation) definition).getLhs();
      assert lhsExpr instanceof JVariableRef;
      return lhsExpr;
    }

    assert definition instanceof JParameter || definition instanceof JThis;
    return definition;
  }

  @Nonnull
  public JVariable getDefinedVariable() {
    if (definition instanceof JAsgOperation) {
      JExpression lhsExpr = ((JAsgOperation) definition).getLhs();
      assert lhsExpr instanceof JVariableRef;
      return ((JVariableRef) lhsExpr).getTarget();
    }

    assert definition instanceof JParameter || definition instanceof JThis;
    return (JVariable) definition;
  }

  @Nonnull
  public JExpression getValue() {
    assert hasValue();

    return ((JAsgOperation) definition).getRhs();
  }

  public void setBitSetIdx(@Nonnegative int bitSetIdx) {
    this.bitSetIdx = bitSetIdx;
  }

  @Nonnegative
  public int getBitSetIdx() {
    return (bitSetIdx);
  }

  @Override
  public String toString() {
    return definition.toString();
  }

  @Nonnull
  public JNode getDefinition() {
    return definition;
  }

  private void removeDefFromUseDefsChain(@Nonnull JVariableRef use) {
    UseDefsMarker udm = use.getMarker(UseDefsMarker.class);
    assert udm != null;

    // Use directly defs to avoid recursion.
    udm.defs.remove(this);
  }
}
