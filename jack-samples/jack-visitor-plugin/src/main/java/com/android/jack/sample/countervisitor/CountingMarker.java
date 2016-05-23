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

package com.android.jack.sample.countervisitor;

import com.android.jack.ir.ast.JSession;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A marker used for counting elements. It supports thread concurrency.
 * <p>
 * This marker is created and updated by the {@link PostfixCounter} schedulable. It is intended to
 * be attached only to the {@link JSession} of the compilation. Therefore, we describe that by
 * annotating this class with the {@link ValidOn} annotation.
 * <p>
 * Besides, the class is annotated by the mandatory {@link Description} annotation.
 */
@Description("Counting marker")
@ValidOn(JSession.class)
public class CountingMarker implements Marker {

  /**
   * The counter of this marker. By using an {@link AtomicInteger}, we ensure thread safety.
   */
  @Nonnull
  private final AtomicInteger counter = new AtomicInteger();

  /**
   * Increments the marker's counter by the given value.
   * <p>
   * This method ensures thread safety because multiple threads may attempt to increment
   * the counter at the same time.
   *
   * @param value the value to add to the current counter
   */
  public void incrementCounter(int value) {
    this.counter.addAndGet(value);
  }

  /**
   * @return the current count of the marker
   */
  @Nonnegative
  public int getCounter() {
    return counter.get();
  }

  /**
   * @return a new {@link CountingMarker} initialized with the current value of the counter.
   */
  @Override
  public Marker cloneIfNeeded() {
    CountingMarker marker = new CountingMarker();
    marker.counter.addAndGet(this.counter.get());
    return marker;
  }
}
