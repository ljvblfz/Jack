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

package com.android.jack.experimental.incremental;

import com.android.jack.backend.dex.TypeReferenceCollector;
import com.android.jack.ir.ast.JArrayType;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/**
 * Find all usages between java files.
 */
@Description("Find all usages between java files")
@Name("UsageFinder")
@Transform(add = CompilerState.Filled.class)
@Synchronized
public class UsageFinder implements RunnableSchedulable<JDefinedClassOrInterface> {

  private static class Visitor extends TypeReferenceCollector {

    @Nonnull
    private final CompilerState compilerState;

    @Nonnull
    private final String currentFileName;

    private boolean inBody = false;

    public Visitor(@Nonnull JType currentType, @Nonnull CompilerState compilerState) {
      this.compilerState = compilerState;
      assert currentType.getSourceInfo() != SourceInfo.UNKNOWN;
      currentFileName = currentType.getSourceInfo().getFileName();
      compilerState.addCodeUsage(currentFileName, null);
      compilerState.addCstUsage(currentFileName, null);
      compilerState.addStructUsage(currentFileName, null);
    }

    @Override
    public boolean visit(@Nonnull JMethodBody x) {
      inBody = true;
      return super.visit(x);
    }

    @Override
    public void endVisit(@Nonnull JMethodBody x) {
      super.endVisit(x);
      inBody = false;
    }

    @Override
    protected void collect(@Nonnull JType usedType) {
      if (usedType instanceof JArrayType) {
        usedType = ((JArrayType) usedType).getLeafType();
      }
      if (usedType.getSourceInfo() == SourceInfo.UNKNOWN) {
        return;
      }
      String usedTypeFileName = usedType.getSourceInfo().getFileName();
      if (inBody) {
        compilerState.addCodeUsage(currentFileName, usedTypeFileName);
      } else {
        compilerState.addStructUsage(currentFileName, usedTypeFileName);
      }
    }
  }

  @Override
  public synchronized void run(@Nonnull JDefinedClassOrInterface declaredType) throws Exception {
    // Ignore external types
    if (declaredType.isExternal()) {
      return;
    }

    Visitor v = new Visitor(declaredType, JackIncremental.getCompilerState());
    v.accept(declaredType);
  }
}
