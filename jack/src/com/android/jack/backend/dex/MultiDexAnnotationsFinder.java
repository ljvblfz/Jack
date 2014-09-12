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

package com.android.jack.backend.dex;

import com.android.jack.Jack;
import com.android.jack.backend.dex.MultiDexLegacyTracerBrush.MultiDexInstallerMarker;
import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/**
 * A visitor adding markers corresponding to annotation configuring multidex.
 */
@Description("Add markers corresponding to annotation configuring multidex.")
@Constraint(need = OriginalNames.class)
@Transform(add = {MultiDexLegacyTracerBrush.MultiDexInstallerMarker.class,
    ForceInMainDexMarker.class})
public class MultiDexAnnotationsFinder implements RunnableSchedulable<JDefinedClassOrInterface> {

  private final JVisitor visitor = new JVisitor() {
    @Override
    public boolean visit(@Nonnull JDefinedClassOrInterface node) {
      if (!node.getAnnotations(installerAnnotation).isEmpty()) {
        markIfNecessary(node);
      }
      return super.visit(node);
    }
    @Override
    public boolean visit(@Nonnull JField node) {
      if (!node.getAnnotations(installerAnnotation).isEmpty()) {
        markIfNecessary(node);
      }
      return false;
    }
    @Override
    public boolean visit(@Nonnull JMethod node) {
      if (!node.getAnnotations(installerAnnotation).isEmpty()) {
        markIfNecessary(node);
      }
      return false;
    }
  };

  @Nonnull
  private final JAnnotation installerAnnotation;
  @Nonnull
  private final JAnnotation mainDexAnnotation;

  public MultiDexAnnotationsFinder() {
    installerAnnotation = Jack.getSession().getPhantomLookup()
        .getAnnotation("Lcom/android/jack/annotations/MultiDexInstaller;");
    mainDexAnnotation = Jack.getSession().getPhantomLookup()
        .getAnnotation("Lcom/android/jack/annotations/ForceInMainDex;");
  }

  @Override
  public void run(@Nonnull JDefinedClassOrInterface type) throws Exception {
    // Ignore external types
    if (type.isExternal()) {
      return;
    }
    visitor.accept(type);

    if (!type.getAnnotations(mainDexAnnotation).isEmpty()) {
      type.addMarker(ForceInMainDexMarker.INSTANCE);
    }
  }

  private synchronized void markIfNecessary(@Nonnull JNode node) {
    if (!node.containsMarker(MultiDexInstallerMarker.class)) {
      node.addMarker(MultiDexInstallerMarker.INSTANCE);
    }
  }
}
