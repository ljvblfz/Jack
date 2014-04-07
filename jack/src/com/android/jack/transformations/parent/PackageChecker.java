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

package com.android.jack.transformations.parent;

import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JSession;
import com.android.jack.transformations.SanityChecks;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;

import javax.annotation.Nonnull;

/**
 * Check that the package is consistent with the {@link JClassOrInterface} name.
 */
@Description("Check that the package is consistent with the DeclareType name.")
@Name("PackageChecker")
@Support(SanityChecks.class)
public class PackageChecker implements RunnableSchedulable<JPackage> {

  @Override
  public void run(@Nonnull JPackage pack) throws Exception {
    /* Use pack.getLoadedTypes() to avoid loading of every class in every package */
    for (JClassOrInterface type : pack.getLoadedTypes()) {
      //TODO(delphinemartin): remove this condition when external types will be properly handled
      if (!type.isExternal()) {
        if (type.getEnclosingPackage() != pack) {
          throw new AssertionError("Wrong enclosing package");
        }
      }
    }

    JNode parent = pack.getParent();
    if (parent instanceof JSession) {
      if (pack.getEnclosingPackage() != null) {
        throw new AssertionError("Wrong enclosing package");
      }
    } else {
      if (parent != pack.getEnclosingPackage()) {
        throw new AssertionError("Wrong enclosing package");
      }
    }
  }
}