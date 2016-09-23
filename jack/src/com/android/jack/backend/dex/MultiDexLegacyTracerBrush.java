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

package com.android.jack.backend.dex;

import com.android.jack.analysis.tracer.AbstractTracerBrush;
import com.android.jack.analysis.tracer.BaseTracerMarker;
import com.android.jack.analysis.tracer.Tracer;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JNode;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/**
 * A {@link Tracer} brush that marks all classes and members that may be used by legacy
 * multidex installation.
 */
@Description("Marks classes and members that may be used by legacy multidex installation")
@Transform(add = MultiDexLegacyTracerBrush.TracerMarker.class)
@Constraint(need = MultiDexLegacyTracerBrush.MultiDexInstallerMarker.class)
public class MultiDexLegacyTracerBrush
  extends AbstractTracerBrush<MultiDexLegacyTracerBrush.TracerMarker> {

  /** Indicates that this node was identified during the main dex legacy tracing. */
  @ValidOn({JDefinedClassOrInterface.class, JMethod.class, JField.class})
  @Description("Indicates that this node was identified during the main dex legacy tracing")
  public static class TracerMarker extends BaseTracerMarker {
  }

  /**
   * Indicates that this node may be an entry point used by multidex legacy support installation.
   */
  @ValidOn({JDefinedClassOrInterface.class, JMethod.class, JField.class})
  @Description(
      "Indicates that this node may be an entry point used by multidex legacy support installation")
  public static class MultiDexInstallerMarker implements Marker {

    @Nonnull
    public static final MultiDexInstallerMarker INSTANCE = new MultiDexInstallerMarker();

    private MultiDexInstallerMarker() {
    }

    @Nonnull
    @Override
    public Marker cloneIfNeeded() {
      return this;
    }
  }

  public MultiDexLegacyTracerBrush() {
    super(false, TracerMarker.class, MultiDexInstallerMarker.class);
  }

  @Nonnull
  @Override
  protected TracerMarker createMarkerFor(@Nonnull JNode node) {
    return new TracerMarker();
  }

}
