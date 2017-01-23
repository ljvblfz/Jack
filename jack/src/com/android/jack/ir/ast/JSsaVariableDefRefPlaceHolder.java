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

import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * This version of the SSA variable reference only appears on the left hand side of an assignment
 * except for a few exceptions.
 *
 */
public class JSsaVariableDefRefPlaceHolder extends JSsaVariableDefRef {

  public JSsaVariableDefRefPlaceHolder(@Nonnull SourceInfo info, @Nonnull JVariable target) {
    super(info, target, 0);
  }

  @Override
  public JSsaVariableUseRef makeRef(@Nonnull SourceInfo info) {
    return new JSsaVariableUseRefPlaceHolder(info, this.getTarget(), this);
  }

  @Override
  @Nonnull
  public List<JSsaVariableUseRef> getUses() {
    throw new UnsupportedOperationException("Should not be called on place holder variables");
  }

  @Override
  public boolean removeUse(JSsaVariableUseRef use) {
    throw new UnsupportedOperationException("Should not be called on place holder variables");
  }

  @Override
  public boolean hasUses() {
    throw new UnsupportedOperationException("Should not be called on place holder variables");
  }

  @Override
  public boolean hasUsesOutsideOfPhis() {
    throw new UnsupportedOperationException("Should not be called on place holder variables");
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
