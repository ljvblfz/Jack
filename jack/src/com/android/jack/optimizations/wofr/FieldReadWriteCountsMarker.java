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

package com.android.jack.optimizations.wofr;

import com.android.jack.ir.ast.JField;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;

/**
 * Marks fields of TsTBE and represents field's usage statistics, particularly it
 * tracks if the field was ever read, the number of field writes and if any of
 * those writes were assigning the field a non-literal value.
 *
 * Supports concurrent modifications.
 */
@Description("Represents counts of field reads and writes")
@ValidOn(JField.class)
public class FieldReadWriteCountsMarker implements Marker {
  @Nonnull
  private final AtomicBoolean hasReads = new AtomicBoolean(false);
  @Nonnull
  private final AtomicInteger writesCount = new AtomicInteger();
  @Nonnull
  private final AtomicBoolean hasNonLiteralWrites = new AtomicBoolean(false);

  /** Marks a field read */
  public static void markRead(@Nonnull JField field) {
    getOrCreate(field).hasReads.set(true);
  }

  /** Marks a field write */
  public static void markWrite(@Nonnull JField field, boolean isNonLiteral) {
    FieldReadWriteCountsMarker marker = getOrCreate(field);
    marker.writesCount.incrementAndGet();
    if (isNonLiteral) {
      marker.hasNonLiteralWrites.set(true);
    }
  }

  /** Un-marks a field write */
  public static void unmarkWrite(@Nonnull JField field) {
    FieldReadWriteCountsMarker marker = field.getMarker(FieldReadWriteCountsMarker.class);
    assert marker != null;
    marker.writesCount.decrementAndGet();
  }

  /** Field has any reads */
  public static boolean hasReads(@Nonnull JField field) {
    FieldReadWriteCountsMarker marker = field.getMarker(FieldReadWriteCountsMarker.class);
    return marker != null && marker.hasReads.get();
  }

  /** Field has any writes */
  public static boolean hasWrites(@Nonnull JField field) {
    FieldReadWriteCountsMarker marker = field.getMarker(FieldReadWriteCountsMarker.class);
    return marker != null && (marker.writesCount.get() > 0);
  }

  /** Field has any non-literal writes */
  public static boolean hasNonLiteralWrites(@Nonnull JField field) {
    FieldReadWriteCountsMarker marker = field.getMarker(FieldReadWriteCountsMarker.class);
    return marker != null && marker.hasNonLiteralWrites.get();
  }

  @Nonnull
  private static FieldReadWriteCountsMarker getOrCreate(@Nonnull JField field) {
    FieldReadWriteCountsMarker marker = field.getMarker(FieldReadWriteCountsMarker.class);
    if (marker != null) {
      return marker;
    }
    marker = new FieldReadWriteCountsMarker();
    FieldReadWriteCountsMarker existing = field.addMarkerIfAbsent(marker);
    return existing == null ? marker : existing;
  }

  @Override
  @Nonnull
  public Marker cloneIfNeeded() {
    throw new AssertionError();
  }
}
