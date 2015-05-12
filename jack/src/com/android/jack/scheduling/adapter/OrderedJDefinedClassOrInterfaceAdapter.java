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

package com.android.jack.scheduling.adapter;

import com.android.jack.backend.dex.DexFileWriter;
import com.android.jack.backend.dex.DexWritingTool;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JSession;
import com.android.sched.item.Description;
import com.android.sched.schedulable.AdapterSchedulable;
import com.android.sched.util.config.ThreadConfig;

import java.util.Iterator;

import javax.annotation.Nonnull;

/**
 * Adapts a process on {@code JSession} onto one or several processes on each
 * {@code JDefinedClassOrInterface} to emit during this session. Output
 * JDefinedClassOrInterfaces are sorted and numbered according to dex writing policy.
 */
@Description("Adapts process on JSession to one or several processes on each of its " +
  "JDefinedClassOrInterface. Output JDefinedClassOrInterfaces are sorted and numbered " +
  "according to dex writing policy.")
public class OrderedJDefinedClassOrInterfaceAdapter
    implements AdapterSchedulable<JSession, JDefinedClassOrInterface> {

  /**
   * Return every {@code JDefinedClassOrInterface} to emit during the given {@code JSession}.
   */
  @Override
  @Nonnull
  public Iterator<JDefinedClassOrInterface> adapt(@Nonnull JSession session)
      throws Exception {
    DexWritingTool writingTool = ThreadConfig.get(DexFileWriter.DEX_WRITING_POLICY);
    return writingTool.sortAndNumber(session.getTypesToEmit());
  }
}
