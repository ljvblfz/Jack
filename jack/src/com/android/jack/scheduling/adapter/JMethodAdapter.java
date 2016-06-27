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

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.sched.item.Description;
import com.android.sched.schedulable.AdapterSchedulable;

import java.util.ArrayList;
import java.util.Iterator;

import javax.annotation.Nonnull;

/**
 * Adapts a process on {@code JDefinedClassOrInterface} onto one or several processes on
 * each {@code JMethod} declared by this type.
 */
@Description("Adapts process on JDefinedClassOrInterface to one or several processes on each of " +
    "its JMethod")
public class JMethodAdapter
  implements AdapterSchedulable<JDefinedClassOrInterface, JMethod> {

  /**
   * Returns every {@code JMethod} declared in the given {@code JDefinedClassOrInterface}.
   */
  @Override
  @Nonnull
  public Iterator<JMethod> adapt(@Nonnull JDefinedClassOrInterface declaredType) {
    return new ArrayList<JMethod>(declaredType.getMethods()).iterator();
  }
}
