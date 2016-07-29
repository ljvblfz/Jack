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

package com.android.jack.scheduling.adapter;

import com.android.jack.ir.ast.JAbstractMethodBody;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBodyCfg;
import com.android.jack.ir.ast.cfg.JControlFlowGraph;
import com.android.sched.item.Description;
import com.android.sched.schedulable.AdapterSchedulable;

import java.util.ArrayList;
import java.util.Iterator;
import javax.annotation.Nonnull;

/**
 * Adapts a process on {@code JDefinedClassOrInterface} onto one or several processes on
 * each {@code JControlFlowGraph} of all the methods declared by this type.
 */
@Description("Adapts process on JDefinedClassOrInterface to one or "
    + "several processes on each of its methods' control flow graphs")
public class JMethodControlFlowGraphAdapter
    implements AdapterSchedulable<JDefinedClassOrInterface, JControlFlowGraph> {

  /**
   * Returns every {@code JControlFlowGraph} of the methods declared
   * in the given {@code JDefinedClassOrInterface}.
   */
  @Override
  @Nonnull
  public Iterator<JControlFlowGraph> adapt(@Nonnull JDefinedClassOrInterface declaredType) {
    ArrayList<JControlFlowGraph> cfgList = new ArrayList<>();
    for (JMethod method : declaredType.getMethods()) {
      JAbstractMethodBody body = method.getBody();
      if (body instanceof JMethodBodyCfg) {
        cfgList.add(((JMethodBodyCfg) body).getCfg());
      }
    }
    return cfgList.iterator();
  }
}
