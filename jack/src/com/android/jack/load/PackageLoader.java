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

package com.android.jack.load;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JPackageLookupException;
import com.android.jack.lookup.JLookupException;
import com.android.sched.util.location.Location;

import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * Loader for {@link JPackage}.
 */
public interface PackageLoader {

  @Nonnull
  JDefinedClassOrInterface loadClassOrInterface(
      @Nonnull JPackage enclosing, @Nonnull String simpleName) throws JLookupException;

  @Nonnull
  PackageLoader getLoaderForSubPackage(@Nonnull JPackage enclosing,
      @Nonnull String simpleName) throws JPackageLookupException;

  @Nonnull
  Collection<String> getSubPackageNames(@Nonnull JPackage enclosing);

  @Nonnull
  Collection<String> getSubClassNames(@Nonnull JPackage enclosing);

  @Nonnull
  Location getLocation(@Nonnull JPackage loaded);

  boolean isOnPath(@Nonnull JPackage loaded);
}
