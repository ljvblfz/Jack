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

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.ir.sourceinfo.SourceInfoFactory;
import com.android.sched.item.Description;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;

import javax.annotation.Nonnull;

/**
 * A {@link RunnableSchedulable} that removes line numbers.
 */
@Description("Removes line numbers")
@Support(RemoveLineNumber.class)
public class LineNumberRemover implements RunnableSchedulable<JDefinedClassOrInterface> {

  private static class Visitor extends JVisitor {
    @Nonnull
    private final SourceInfoFactory sourceInfoFactory;

    public Visitor(@Nonnull SourceInfoFactory sourceInfoFactory) {
      this.sourceInfoFactory = sourceInfoFactory;
    }

    @Override
    public boolean visit(@Nonnull JNode node) {
      SourceInfo info = node.getSourceInfo();
      if (info != SourceInfo.UNKNOWN) {
        node.setSourceInfo(sourceInfoFactory.create(
            SourceInfo.UNKNOWN_COLUMN_NUMBER,
            SourceInfo.UNKNOWN_COLUMN_NUMBER,
            SourceInfo.UNKNOWN_LINE_NUMBER,
            SourceInfo.UNKNOWN_LINE_NUMBER,
            info.getFileName()));
      }
      return false;
    }
  }

  @Override
  public void run(@Nonnull JDefinedClassOrInterface type) throws Exception {
    Visitor visitor = new Visitor(type.getSession().getSourceInfoFactory());
    visitor.accept(type);
  }

}
