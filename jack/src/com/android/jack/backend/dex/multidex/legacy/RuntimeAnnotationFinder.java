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

package com.android.jack.backend.dex.multidex.legacy;

import com.android.jack.backend.dex.MultiDexLegacy;
import com.android.jack.backend.dex.MultiDexLegacyTracerBrush.MultiDexInstallerMarker;
import com.android.jack.ir.ast.JDefinedAnnotation;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JRetentionPolicy;
import com.android.sched.item.Description;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;

/**
 * Mark runtime visible annotations as MainDexInstaller.
 *
 * We do this as a rough workaround for a Dalvik bug. The bug occurs when accessing annotations
 * containing enum value on an annotated class that is not in the same dex as the enum. The bug
 * causes unjustified {@link IllegalAccessError} to be thrown.
 */
@Description("Mark runtime visible annotations as MainDexInstaller")
@Transform(add = MultiDexInstallerMarker.class)
@Support(MultiDexLegacy.class)
public class RuntimeAnnotationFinder implements RunnableSchedulable<JDefinedClassOrInterface> {

  @Override
  public void run(JDefinedClassOrInterface type) throws Exception {
    if (type instanceof JDefinedAnnotation &&
        (((JDefinedAnnotation) type).getRetentionPolicy() == JRetentionPolicy.RUNTIME)) {
      type.addMarker(MultiDexInstallerMarker.INSTANCE);
    }
  }
}
