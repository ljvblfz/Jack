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

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JNode;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.onlyfor.OnlyFor;
import com.android.sched.item.onlyfor.SchedTest;
import com.android.sched.schedulable.AdapterSchedulable;

import java.util.Iterator;

import javax.annotation.Nonnull;

/**
 * Adapts a process on {@code JDeclaredType} onto one or several processes on
 * each {@code JMethod} declared by this type.
 */
@Description("Adapt a process of JDeclaredType to a visit of JNode")
@Name("JNodeAdapter")
@OnlyFor(SchedTest.class)
public class JNodeAdapter implements AdapterSchedulable<JDefinedClassOrInterface, JNode> {
  @Override
  @Nonnull
  public Iterator<JNode> adapt(@Nonnull JDefinedClassOrInterface type) {
    throw new AssertionError("NYI");
  }
}
