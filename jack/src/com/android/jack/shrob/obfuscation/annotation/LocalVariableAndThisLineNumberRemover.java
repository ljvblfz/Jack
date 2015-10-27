/*
 * Copyright (C) 2014 The Android Open Source Project
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

import com.android.jack.Jack;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JThis;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.ir.sourceinfo.SourceInfoFactory;
import com.android.sched.item.Description;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;

import javax.annotation.Nonnull;

/**
 * A {@link RunnableSchedulable} that removes line numbers for local variables and 'this'.
 */
@Description("Removes line numbers for local variable and 'this'")
@Support(RemoveLocalLineNumber.class)
public class LocalVariableAndThisLineNumberRemover
    implements RunnableSchedulable<JMethod> {

  private static class Visitor extends JVisitor {
    @Nonnull
    private final SourceInfoFactory sourceInfoFactory;

    public Visitor(@Nonnull SourceInfoFactory sourceInfoFactory) {
      this.sourceInfoFactory = sourceInfoFactory;
    }

    @Override
    public boolean visit(@Nonnull JLocal node) {
      SourceInfo info = node.getSourceInfo();
      if (info != SourceInfo.UNKNOWN) {
        node.setSourceInfo(info.getFileSourceInfo());
      }
      return true;
    }

    @Override
    public boolean visit(@Nonnull JThis node) {
      SourceInfo info = node.getSourceInfo();
      if (info != SourceInfo.UNKNOWN) {
        node.setSourceInfo(info.getFileSourceInfo());
      }
      return true;
    }
  }

  @Override
  public void run(@Nonnull JMethod method) {
    Visitor visitor = new Visitor(Jack.getSession().getSourceInfoFactory());
    visitor.accept(method);
  }

}
