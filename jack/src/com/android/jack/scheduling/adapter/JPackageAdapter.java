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

package com.android.jack.scheduling.adapter;

import com.google.common.collect.Iterators;

import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JSession;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.AdapterSchedulable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Adapts a process on {@code JSession} onto one or several processes on
 * each {@code JPackage} known by this session.
 */
@Description("Adapts process on JSession to one or several processes on each of its JPackage")
@Name("JPackageAdapter")
public class JPackageAdapter implements AdapterSchedulable<JSession, JPackage> {
  @Nonnull
  private Iterator<JPackage> process(@Nonnull JPackage pack) {
    // Use another list to scan packages in order to support concurrent modification.
    List<JPackage> packages = new ArrayList<JPackage>(pack.getSubPackages());

    Iterator<JPackage> iter = packages.iterator();

    for (JPackage subPackage : packages) {
      iter = Iterators.concat(iter, process(subPackage));
    }

    return iter;
  }

  /**
   * Returns every {@code JPackage} known by the given {@code JSession}.
   */
  @Override
  @Nonnull
  public Iterator<JPackage> adapt(@Nonnull JSession session) {
    return Iterators.concat(Iterators.singletonIterator(session.getTopLevelPackage()),
        process(session.getTopLevelPackage()));
  }

}
