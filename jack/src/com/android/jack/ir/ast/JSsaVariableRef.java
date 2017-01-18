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

package com.android.jack.ir.ast;

import com.android.jack.ir.ast.cfg.JBasicBlockElement;
import com.android.jack.ir.ast.cfg.JPhiBlockElement;
import com.android.jack.ir.ast.cfg.JVariableAsgBlockElement;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Presents a variable reference that is in SSA form.
 *
 * For all variables X in the AST, a renamed variable X_1 will be a {@link JVariableRef} with a
 * version number 1.
 *
 * JVariableRef of different version is considered to be behave like completely different variables
 * in SSA form.
 */
@Description("Represents a reference to an SSA variable.")
public class JSsaVariableRef extends JVariableRef {

  private final boolean isDef;

  @Nonnegative
  private final int version;

  @CheckForNull
  private final JBasicBlockElement def;

  @Nonnull
  private final List<JSsaVariableRef> uses = new ArrayList<>();

  /**
   * Constructs a JSsaVariableRef.
   *
   * @param target The original variable without renaming / versioning.
   * @Param version The version number of the variable if it is renamed.
   */
  public JSsaVariableRef(@Nonnull SourceInfo info, @Nonnull JVariable target,
      @Nonnegative int version, JBasicBlockElement def, boolean isDef) {
    super(info, target);
    this.version = version;
    this.def = def;
    this.isDef = isDef;
  }

  /**
   * @return The version number of the variable it is referencing.
   */
  @Nonnegative
  public int getVersion() {
    return version;
  }

  /**
   * @return true if this is variable has any uses.
   */
  public boolean hasUses() {
    return !uses.isEmpty();
  }

  /**
   * @return true if it is used in a Phi element.
   */
  public boolean isPhiUse() {
    if (isDef) {
      return false;
    }
    JNode parent = getParent();
    return parent instanceof JPhiBlockElement;
  }

  public boolean hasUsesOutsideOfPhis() {
    if (!hasUses()) {
      return false;
    }

    if (!isPhiUse()) {
      return true;
    }

    for (JSsaVariableRef use : uses) {
      if (use.hasUsesOutsideOfPhis()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    visitor.visit(this);
    visitor.endVisit(this);
  }

  @Nonnull
  public List<JSsaVariableRef> getUses() {
    return uses;
  }

  @CheckForNull
  public JSsaVariableRef getDef() {
    if (def instanceof JPhiBlockElement) {
      JPhiBlockElement phi = (JPhiBlockElement) def;
      return phi.getLhs();
    } else {
      JVariableAsgBlockElement assign = (JVariableAsgBlockElement) def;
      if (def == null) {
        return null;
      } else {
        return (JSsaVariableRef) assign.getAssignment().getLhs();
      }
    }
  }

  /**
   * @return A new JSsaVariableRef that references the same variable at the same version.
   */
  @Nonnull
  public JSsaVariableRef makeRef(@Nonnull SourceInfo info) {
    JSsaVariableRef ref = new JSsaVariableRef(info, target, version, def, false);
    uses.add(ref);
    ref.uses.add(this);
    return ref;
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }
}
