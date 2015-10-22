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

package com.android.jack.preprocessor;

import com.android.jack.ir.ast.JAnnotationType;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JType;
import com.android.jack.util.NamingTools;
import com.android.sched.util.location.Location;

import java.util.Collection;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

class RuleBuilder {
  @Nonnull
  private final JSession session;
  @Nonnull
  private final Location location;

  public RuleBuilder(@Nonnull JSession session, @Nonnull Location location) {
    this.session = session;
    this.location = location;
  }

  @SuppressWarnings("unchecked")
  @Nonnull
  public Expression<Collection<? extends JType>, Scope> newTypeFilter(
      @Nonnull Expression<Collection<? extends JType>, Scope> typeSet, @Nonnegative int dim) {
    if (dim == 0) {
      return typeSet;
    } else {
      return (Expression<Collection<? extends JType>, Scope>) (Object)
          new ArrayFilter(typeSet, dim);
    }
  }

  @Nonnull
  public JAnnotationType getAnnotationType(@Nonnull String annotationName) {
    return session.getLookup().getAnnotationType(
        NamingTools.getTypeSignatureName(annotationName));
  }

  @Nonnull
  public Location getLocation() {
    return location;
  }
}
