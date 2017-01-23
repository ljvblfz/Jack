/*
 * Copyright (C) 2017 The Android Open Source Project
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

import com.google.common.collect.Lists;

import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * This version of the SSA variable reference only appears on the left hand side of an assignment
 * except for a few exceptions.
 *
 */
public class JSsaVariableDefRef extends JSsaVariableRef {

  @Nonnull
  private final List<JSsaVariableUseRef> uses;

  private int regNum = -1;

  public JSsaVariableDefRef(@Nonnull SourceInfo info, @Nonnull JVariable target,
      @Nonnegative int version) {
    super(info, target, version);
    this.uses = Lists.newLinkedList();
  }

  public JSsaVariableUseRef makeRef(@Nonnull SourceInfo info) {
    JSsaVariableUseRef use = new JSsaVariableUseRef(info, target, this.getVersion(), this);
    uses.add(use);
    return use;
  }

  @Nonnull
  public List<JSsaVariableUseRef> getUses() {
    return uses;
  }

  public boolean removeUse(JSsaVariableUseRef use) {
    int index = uses.indexOf(use);
    if (index == -1) {
      return false;
    }
    uses.remove(index);
    return true;
  }

  public boolean hasUses() {
    return !uses.isEmpty();
  }

  public boolean hasUsesOutsideOfPhis() {
    for (JSsaVariableUseRef use : uses) {
      if (use.isPhiUse()) {
        return true;
      }
    }
    return false;
  }

  public void setRopRegister(@Nonnegative int reg) {
    assert this.regNum == -1;
    this.regNum = reg;
  }

  public int getRopRegister() {
    return this.regNum;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    visitor.visit(this);
    visitor.endVisit(this);
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
