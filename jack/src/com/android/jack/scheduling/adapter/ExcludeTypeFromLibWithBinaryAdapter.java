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

package com.android.jack.scheduling.adapter;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JSession;
import com.android.jack.library.InputLibrary;
import com.android.jack.library.TypeInInputLibraryLocation;
import com.android.sched.item.Description;
import com.android.sched.schedulable.AdapterSchedulable;
import com.android.sched.util.location.Location;

import java.util.ArrayList;
import java.util.Iterator;

import javax.annotation.Nonnull;

/**
 * Adapts a process on {@code JSession} onto one or several processes on each
 * {@code JDefinedClassOrInterface} to emit respecting a filter during this session.
 */
@Description("Adapts process on JSession to one or several processes on each of its " +
  "JDefinedClassOrInterface respecting a filter")
public class ExcludeTypeFromLibWithBinaryAdapter
    implements AdapterSchedulable<JSession, JDefinedClassOrInterface> {

  /**
   * Return every {@code JDefinedClassOrInterface} to emit during the given {@code JSession}.
   */
  @Override
  @Nonnull
  public Iterator<JDefinedClassOrInterface> adapt(@Nonnull final JSession session)
      throws Exception {

    // Use a copy to scan types in order to support concurrent modification.
    return (Iterators.filter(
        new ArrayList<JDefinedClassOrInterface>(session.getTypesToEmit()).iterator(),
        new Predicate<JDefinedClassOrInterface>() {
          @Override
          public boolean apply(JDefinedClassOrInterface clOrI) {
            Location location = clOrI.getLocation();

            if (location instanceof TypeInInputLibraryLocation) {
              InputLibrary inputLibrary = ((TypeInInputLibraryLocation) location)
                  .getInputLibraryLocation().getInputLibrary();
              if (inputLibrary.getBinaryKinds().containsAll(session.getGeneratedBinaryKinds())) {
                return false;
              }
            }

            return true;
          }
        }));
  }
}
