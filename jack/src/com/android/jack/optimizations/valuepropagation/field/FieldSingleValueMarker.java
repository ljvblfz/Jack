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

package com.android.jack.optimizations.valuepropagation.field;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JValueLiteral;
import com.android.jack.optimizations.common.ConcurrentLiteralValueTracker;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import java.util.Collection;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Marker is used to track values assigned to a field.
 * Allows concurrent modifications.
 */
@Description("Marker is used to represent a known value of a final field")
@ValidOn(JField.class)
public class FieldSingleValueMarker implements Marker {
  @Nonnull
  private final ConcurrentLiteralValueTracker tracker = new ConcurrentLiteralValueTracker();

  @Override
  @Nonnull
  public Marker cloneIfNeeded() {
    throw new AssertionError();
  }

  /** Returns true if there are multiple values, or the value is non-literal */
  boolean isMultipleOrNonLiteralValue() {
    return tracker.isMultipleOrNonLiteralValue();
  }

  /**
   * Return the consolidated single value. Note that the value may be 'null'
   * in case there are not constructors or exit blocks of the constructors
   * are not reachable, thus no value reaches the end.
   */
  @CheckForNull
  JValueLiteral getConsolidatedValue() {
    return tracker.getConsolidatedValue();
  }

  @CheckForNull
  private static FieldSingleValueMarker create(@Nonnull JField field) {
    JDefinedClassOrInterface type = field.getEnclosingType();
    // Only fields of types to be emitted are supposed to be tracked
    assert type.isToEmit();

    FieldSingleValueMarker marker = new FieldSingleValueMarker();

    // If the type does not have a static initializer, all static fields
    // should be assumed to have be assigned default values
    if (field.isStatic()) {
      boolean hasStaticInit = false;
      for (JMethod method : type.getMethods()) {
        if (JMethod.isClinit(method)) {
          hasStaticInit = true;
          break;
        }
      }
      if (!hasStaticInit) {
        marker.tracker.markExpression(
            field.getType().createDefaultValue(field.getSourceInfo()));
      }
    }
    return marker;
  }

  /** Return a marker on the field, creates it the field is to be tracked. */
  @CheckForNull
  public static FieldSingleValueMarker getOrCreate(@Nonnull JField field) {
    if (field.isVolatile()) {
      return null; // Don't track volatile fields
    }

    FieldSingleValueMarker marker = field.getMarker(FieldSingleValueMarker.class);
    if (marker != null) {
      return marker;
    }

    marker = create(field);
    if (marker == null) {
      return null; // Field is not tracked
    }

    FieldSingleValueMarker existing = field.addMarkerIfAbsent(marker);
    return existing != null ? existing : marker;
  }

  /** Mark the value assignment on the field, create marker if necessary */
  public static void markValue(@Nonnull JField field, @Nonnull JExpression expression) {
    FieldSingleValueMarker marker = getOrCreate(field);
    if (marker != null) {
      marker.tracker.markExpression(expression);
    }
  }

  /** Mark the value assignment on the field, create marker if necessary */
  public static void markValues(
      @Nonnull JField field, @Nonnull Collection<JExpression> expressions) {
    FieldSingleValueMarker marker = getOrCreate(field);
    if (marker != null) {
      for (JExpression expression : expressions) {
        marker.tracker.markExpression(expression);
      }
    }
  }
}
