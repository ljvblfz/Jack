/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.shrob.obfuscation.annotation;

import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JThis;
import com.android.jack.ir.ast.JVisitor;
import com.android.sched.item.Description;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/**
 * A {@link RunnableSchedulable} that removes local variables and 'this' names.
 */
@Description("Removes local variables and 'this' names")
@Transform(modify = {JLocal.class, JThis.class})
public class LocalVariableAndThisNameRemover implements RunnableSchedulable<JMethod> {

  private static class Visitor extends JVisitor {

    @Override
    public boolean visit(@Nonnull JLocal node) {
      node.setName(null);
      return false;
    }

    @Override
    public boolean visit(@Nonnull JThis node) {
      node.setName(null);
      return false;
    }
  }

  @Override
  public void run(@Nonnull JMethod t) throws Exception {
    Visitor visitor = new Visitor();
    visitor.accept(t);
  }
}
