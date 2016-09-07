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

package com.android.jack.sample.countervisitor;

import com.android.jack.ir.ast.JPostfixOperation;
import com.android.jack.ir.ast.JVisitor;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * This class visits every {@link JPostfixOperation} in the Jack IR of a method.
 * <p>
 * We extends the class {@link JVisitor} that allows to visit every node in the IR. In our case,
 * we only want to visit {@link JPostfixOperation} nodes in order to count them.
 * <p>
 * Contrary to schedulables, a visitor is not managed by the scheduler. Therefore, it does not need
 * to describe anything with annotations.
 */
public class PostfixVisitor extends JVisitor {

  @Nonnegative
  private int counter = 0;

  @Nonnegative
  public int getCounter() {
    return counter;
  }

  /**
   * Increments the counter each time we visit a postfix operation.
   */
  @Override
  public void endVisit(@Nonnull JPostfixOperation x) {
    ++counter;
  }
}
