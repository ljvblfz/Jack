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

package com.android.jack.optimizations.valuepropagation.argument;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/** Represent tainted methods on a type to be emitted and defined types it inherits/extends */
@Description("Represent tainted methods on a defined type")
@ValidOn(JDefinedClassOrInterface.class)
public class TaintedVirtualMethodsMarker implements Marker {
  /**
   * Tainted marker used for all the phantom types and other
   * types with all virtual methods tainted
   */
  @Nonnull
  private static final TaintedVirtualMethodsMarker ALL_VIRTUAL_METHODS_TAINTED =
      new TaintedVirtualMethodsMarker(null);

  // null means all virtual methods are tainted
  @CheckForNull
  private final Set<String> tainted;

  private TaintedVirtualMethodsMarker(@CheckForNull Set<String> tainted) {
    this.tainted = tainted;
  }

  /** Checks if this method is tainted. */
  public boolean isMethodTainted(@Nonnull String signature) {
    return (this.tainted == null) || tainted.contains(signature);
  }

  /** Checks if all virtual methods are tainted. */
  public boolean allMethodsAreTainted() {
    return this.tainted == null;
  }

  /**
   * Gets the marker if it exists on the type, note that all phantom types
   * implicitly have this marker tainting all the virtual methods
   */
  @CheckForNull
  public static TaintedVirtualMethodsMarker getMarker(
      @Nonnull JDefinedClassOrInterface type) {
    return type.getMarker(TaintedVirtualMethodsMarker.class);
  }

  /** Marker builder */
  public static class Builder {
    @Nonnull
    private final JDefinedClassOrInterface type;
    @Nonnull
    private final Set<String> tainted = new HashSet<>();

    public Builder(@Nonnull JDefinedClassOrInterface type) {
      this.type = type;
    }

    /** Creates a new 'all methods tainted' marker and sets it on the type */
    @Nonnull
    public TaintedVirtualMethodsMarker createAndAddAsAllTainted() {
      TaintedVirtualMethodsMarker existing =
          type.addMarkerIfAbsent(ALL_VIRTUAL_METHODS_TAINTED);
      return existing != null ? existing : ALL_VIRTUAL_METHODS_TAINTED;
    }

    /**
     * Creates a new marker with calculated set of tainted
     * methods and sets it on the type
     */
    @Nonnull
    public TaintedVirtualMethodsMarker createAndAdd() {
      TaintedVirtualMethodsMarker marker = new TaintedVirtualMethodsMarker(tainted);
      TaintedVirtualMethodsMarker existing = type.addMarkerIfAbsent(marker);
      return existing != null ? existing : marker;
    }

    /** Merge with other non-all-tainted marker */
    public void mergeWith(@Nonnull TaintedVirtualMethodsMarker other) {
      assert other.tainted != null;
      this.tainted.addAll(other.tainted);
    }

    /** Add tainted method */
    public void addTaintedMethod(@Nonnull String signature) {
      this.tainted.add(signature);
    }
  }

  @Override
  @Nonnull
  public Marker cloneIfNeeded() {
    throw new AssertionError();
  }
}
