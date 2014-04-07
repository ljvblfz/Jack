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

package com.android.jack.load;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JPackageLookupException;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.lookup.JLookupException;
import com.android.sched.util.config.Location;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * A package loader loading from a list a loaders.
 */
public class ComposedPackageLoader {

  @Nonnull
  private final List<ComposablePackageLoader> loaders =
      new LinkedList<ComposablePackageLoader>();

  public ComposedPackageLoader appendLoader(@Nonnull ComposablePackageLoader loader) {
    loaders.add(loader);
    return this;
  }

  public ComposedPackageLoader prependLoader(@Nonnull ComposablePackageLoader loader) {
    loaders.add(0, loader);
    return this;
  }

  @Nonnull
  public List<ComposablePackageLoader> getLoaders() {
    return Jack.getUnmodifiableCollections().getUnmodifiableList(loaders);
  }

  @Nonnull
  public JPackage loadSubPackage(@Nonnull JPackage loading, @Nonnull String simpleName)
      throws JPackageLookupException {
    ComposedPackageLoader loader = null;
    for (ComposablePackageLoader composable : loaders) {
      try {
        ComposablePackageLoader subComposable =
            composable.getLoaderForSubPackage(loading, simpleName);
        if (loader == null) {
          loader = new ComposedPackageLoader();
        }
        loader.appendLoader(subComposable);
      } catch (JPackageLookupException e) {
        // ignore
      }
    }
    if (loader != null) {
      JPackage subPackage = new JPackage(simpleName, loading.getSession(), loading, loader);
      subPackage.updateParents(loading);
      return subPackage;
    } else {
      throw new JPackageLookupException(simpleName, loading);
    }
  }

  public void loadSubPackages(@Nonnull JPackage loading) {
    HashSet<String> subNames = new HashSet<String>();
    for (ComposablePackageLoader composable : loaders) {
      subNames.addAll(composable.getSubPackageNames(loading));
    }

    for (String name : subNames) {
      loading.getSubPackage(name);
    }
  }

  @Nonnull
  public JDefinedClassOrInterface loadClassOrInterface(
      @Nonnull JPackage loading, @Nonnull String simpleName) throws JLookupException {
    for (ComposablePackageLoader composable : loaders) {
      try {
        return composable.loadClassOrInterface(loading, simpleName);
      } catch (JLookupException e) {
        // ignore
      }
    }
    throw new JTypeLookupException(loading, simpleName);
  }

  public void loadClassesAndInterfaces(@Nonnull JPackage loading) {
    HashSet<String> subNames = new HashSet<String>();
    for (ComposablePackageLoader composable : loaders) {
      subNames.addAll(composable.getSubClassNames(loading));
    }

    for (String name : subNames) {
      loading.getType(name);
    }
  }

  @Nonnull
  public List<Location> getLocations(@Nonnull JPackage loaded) {
    List<Location> locations = new ArrayList<Location>(loaders.size());
    for (ComposablePackageLoader loader : loaders) {
      locations.add(loader.getLocation(loaded));
    }
    return locations;
  }

  public boolean isOnPath(@Nonnull JPackage loaded) {
    for (ComposablePackageLoader composable : loaders) {
      if (composable.isOnPath(loaded)) {
        return true;
      }
    }
    return false;
  }

}
