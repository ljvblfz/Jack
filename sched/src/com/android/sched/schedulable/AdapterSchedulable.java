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

package com.android.sched.schedulable;

import com.android.sched.item.Component;

import java.util.Iterator;

import javax.annotation.Nonnull;

/**
 * A {@link Schedulable} which adapts a process on <i>data</i> of a given source type into one or
 * several processes on <i>data</i> of a destination type.
 *
 * @param <SRC> the source type
 * @param <DST> the destination type
 */
public interface AdapterSchedulable<SRC extends Component, DST extends Component>
    extends Schedulable {

  /**
   * Returns an iterator over all instances of type DST contained inside the instance {@code data}.
   *
   * @throws Exception if any Exception is thrown during the visit
   */
  @Nonnull
  public Iterator<DST> adapt(@Nonnull SRC data);
}
