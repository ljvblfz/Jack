/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.sched.util.log.tracer.probe;

import com.android.sched.util.codec.ImplementationName;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Probe which count the heap memory usage.
 */
@ImplementationName(iface = Probe.class, name = "heap-alloc-count")
public class HeapAllocationCountProbe extends HeapAllocationProbe {
  public HeapAllocationCountProbe() {
    super("Heap allocation count");
  }

  @Override
  @Nonnegative
  public long read() {
    return alloc.get().count;
  }

  @Override
  @Nonnull
  public String formatValue(long value) {
    return Long.valueOf(value).toString();
  }
}
