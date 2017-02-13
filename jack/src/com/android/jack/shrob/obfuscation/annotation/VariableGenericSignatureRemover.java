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

import com.android.jack.debug.DebugVariableInfoMarker;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.marker.GenericSignature;
import com.android.sched.item.Description;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;

import javax.annotation.Nonnull;

/**
 * A {@link RunnableSchedulable} that removes signatures from variables.
 */
@Description("Removes signatures from variables")
@Support(RemoveVariableGenericSignature.class)
public class VariableGenericSignatureRemover implements RunnableSchedulable<JMethod> {

  private static class Visitor extends JVisitor {

    @Override
    public boolean visit(@Nonnull JVariableRef x) {
      DebugVariableInfoMarker debugInfo = x.getMarker(DebugVariableInfoMarker.class);
      if (debugInfo != null) {
        debugInfo.setGenericSignature(null);
      }
      return false;
    }

    @Override
    public boolean visit(@Nonnull JVariable var) {
      var.removeMarker(GenericSignature.class);
      return false;
    }
  }

  @Override
  public void run(@Nonnull JMethod method) {
    Visitor visitor = new Visitor();
    visitor.accept(method);
  }

}
