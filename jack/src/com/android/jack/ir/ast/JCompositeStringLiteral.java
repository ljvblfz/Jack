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

package com.android.jack.ir.ast;

import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/**
 * Composition of {@code JStringLiteral}.
 */
@Description("Composition of JStringLiteral")
public class JCompositeStringLiteral extends JAbstractStringLiteral {

  @Nonnull
  private JAbstractStringLiteral leftStr;

  @Nonnull
  private JAbstractStringLiteral rightStr;

  public JCompositeStringLiteral(@Nonnull SourceInfo sourceInfo,
      @Nonnull JAbstractStringLiteral leftStr, JAbstractStringLiteral rightStr) {
    super(sourceInfo);
    this.leftStr = leftStr;
    this.rightStr = rightStr;
  }

  @Override
  @Nonnull
  public String getValue() {
    return leftStr.getValue() + rightStr.getValue();
  }

  @Override
  @Nonnull
  public JCompositeStringLiteral clone() {
    JCompositeStringLiteral newCompositeString = (JCompositeStringLiteral) super.clone();
    newCompositeString.leftStr = (JAbstractStringLiteral) leftStr.clone();
    newCompositeString.rightStr = (JAbstractStringLiteral) rightStr.clone();
    return newCompositeString;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      visitor.accept(leftStr);
      visitor.accept(rightStr);
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    leftStr.traverse(schedule);
    rightStr.traverse(schedule);
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }

  @Override
  protected void replaceImpl(@Nonnull JNode existingNode, @Nonnull JNode newNode)
      throws UnsupportedOperationException {
    if (rightStr == existingNode) {
      rightStr = (JAbstractStringLiteral) newNode;
    } else if (leftStr == existingNode) {
      leftStr = (JAbstractStringLiteral) newNode;
    } else {
      super.replaceImpl(existingNode, newNode);
    }
  }
}
