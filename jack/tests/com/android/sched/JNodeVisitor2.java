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

package com.android.sched;

import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JUnaryOperation;
import com.android.jack.ir.ast.JVisitor;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.onlyfor.OnlyFor;
import com.android.sched.item.onlyfor.SchedTest;
import com.android.sched.schedulable.VisitorSchedulable;
import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/**
 * Remove increment and decrement
 */
@Description("Test 2")
@Name("Test 2")
@OnlyFor(SchedTest.class)
public class JNodeVisitor2 extends JVisitor implements VisitorSchedulable<JNode> {
  private JNodeVisitor2() {
  }

  @Override
  public void visit(@Nonnull JUnaryOperation unary, @Nonnull TransformRequest tr) throws Exception {
    System.out.println("JNV2 U: " + unary);
  }

  @Override
  public void visit(@Nonnull JExpressionStatement statement, @Nonnull TransformRequest tr)
      throws Exception {
    System.out.println("JNV2 S: " + statement);
  }

  @Override
  public void visit(@Nonnull JBinaryOperation binary, @Nonnull TransformRequest tr)
      throws Exception {
    System.out.println("JNV2 B: " + binary);
  }
}
