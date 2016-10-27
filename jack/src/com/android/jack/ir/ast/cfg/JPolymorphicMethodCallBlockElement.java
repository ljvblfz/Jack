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

package com.android.jack.ir.ast.cfg;

import com.android.jack.ir.ast.JPolymorphicMethodCall;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/** Represents polymorphic method call basic block element */
public class JPolymorphicMethodCallBlockElement extends JBasicBlockElement {
  @Nonnull
  private JPolymorphicMethodCall call;

  JPolymorphicMethodCallBlockElement(@Nonnull SourceInfo info,
      @Nonnull ExceptionHandlingContext ehc, @Nonnull JPolymorphicMethodCall call) {
    super(info, ehc);
    this.call = call;
    this.call.updateParents(this);
  }

  @Nonnull
  public JPolymorphicMethodCall getCall() {
    return call;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      visitor.accept(call);
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    call.traverse(schedule);
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest request) throws Exception {
    visitor.visit(this, request);
  }

  @Override
  public boolean isTerminal() {
    return true;
  }
}
