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

package com.android.sched.filter;

import com.google.common.collect.Iterators;

import com.android.sched.item.Description;
import com.android.sched.item.onlyfor.OnlyFor;
import com.android.sched.schedulable.AdapterSchedulable;

import java.util.Iterator;

import javax.annotation.Nonnull;

@Description("Adapter from A to B")
@OnlyFor(FilterTest.class)
public class A_AB implements AdapterSchedulable<C_A, C_B> {
  @Override
  @Nonnull
  public Iterator<C_B> adapt(@Nonnull C_A a) {
    R_Common.adapterCount++;

    return Iterators.forArray(new C_B(a.getString() + "/B.1"), new C_B(a.getString() + "/B.2"),
        new C_B(a.getString() + "/B.3"));
  }
}
