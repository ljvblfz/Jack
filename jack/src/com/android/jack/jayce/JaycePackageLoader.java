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

package com.android.jack.jayce;

import com.android.jack.JackFileException;
import com.android.jack.backend.jayce.JayceFileImporter;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JPackageLookupException;
import com.android.jack.ir.ast.MissingJTypeLookupException;
import com.android.jack.library.HasInputLibrary;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.library.InputLibrary;
import com.android.jack.load.JackLoadingException;
import com.android.jack.load.PackageLoader;
import com.android.jack.lookup.JPhantomLookup;
import com.android.sched.util.location.Location;
import com.android.sched.vfs.InputVDir;
import com.android.sched.vfs.InputVElement;
import com.android.sched.vfs.InputVFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * {@link PackageLoader} for package containing classes defined in Jayce files.
 */
public class JaycePackageLoader implements PackageLoader, HasInputLibrary {

  @Nonnull
  private final InputVDir dir;

  @Nonnull
  private final JPhantomLookup lookup;

  @Nonnull
  private final NodeLevel defaultLoadLevel;

  @Nonnull
  private final InputJackLibrary inputJackLibrary;

  JaycePackageLoader(@Nonnull InputJackLibrary inputJackLibrary, @Nonnull InputVDir dir,
      @Nonnull JPhantomLookup lookup, @Nonnull NodeLevel defaultLoadLevel) {
    this.inputJackLibrary = inputJackLibrary;
    this.dir = dir;
    this.lookup = lookup;
    this.defaultLoadLevel = defaultLoadLevel;
  }

  @Override
  @Nonnull
  public JDefinedClassOrInterface loadClassOrInterface(
      @Nonnull JPackage loading, @Nonnull String simpleName) {
    for (InputVElement sub : dir.list()) {
      if (!sub.isVDir() && isJackFileNameOf(sub.getName(), simpleName)) {
        try {
          return new JayceClassOrInterfaceLoader(inputJackLibrary,
              loading,
              simpleName,
              (InputVFile) sub,
              lookup,
              defaultLoadLevel).load();
        } catch (IOException e) {
          throw new JackLoadingException(sub.getLocation(), e);
        } catch (JackFileException e) {
          throw new JackLoadingException(sub.getLocation() , e);
        }
      }
    }
    throw new MissingJTypeLookupException(loading, simpleName);
  }

  @Override
  @Nonnull
  public Collection<String> getSubClassNames(@Nonnull JPackage loading) {
    List<String> subs = new ArrayList<String>();
    for (InputVElement sub : dir.list()) {
      String fileName = sub.getName();
      if (!sub.isVDir() && JayceFileImporter.isJackFileName(fileName)) {
        subs.add(
            fileName.substring(0, fileName.length() - JayceFileImporter.JACK_EXTENSION_LENGTH));
      }
    }
    return subs;
  }

  @Nonnull
  @Override
  public PackageLoader getLoaderForSubPackage(@Nonnull JPackage loading,
      @Nonnull String simpleName) throws JPackageLookupException {
    for (InputVElement sub : dir.list()) {
      if (sub.isVDir() && sub.getName().equals(simpleName)) {
        return new JaycePackageLoader(inputJackLibrary, (InputVDir) sub, lookup, defaultLoadLevel);
      }
    }
    throw new JPackageLookupException(simpleName, loading);
  }

  @Nonnull
  @Override
  public Collection<String> getSubPackageNames(@Nonnull JPackage loading) {
    List<String> subs = new ArrayList<String>();
    for (InputVElement sub : dir.list()) {
      if (sub.isVDir()) {
        subs.add(sub.getName());
      }
    }
    return subs;
  }

  @Override
  @Nonnull
  public Location getLocation(@Nonnull JPackage loaded) {
    return dir.getLocation();
  }

  private boolean isJackFileNameOf(@Nonnull String fileName, @Nonnull String typeName) {
    return (fileName.length() > JayceFileImporter.JACK_EXTENSION_LENGTH) && (fileName.substring(0,
        fileName.length() - JayceFileImporter.JACK_EXTENSION_LENGTH).equals(typeName)) && (fileName
        .substring(fileName.length() - JayceFileImporter.JACK_EXTENSION_LENGTH).equalsIgnoreCase(
        JayceFileImporter.JAYCE_FILE_EXTENSION));
  }

  @Override
  public boolean isOnPath(@Nonnull JPackage loaded) {
    return true;
  }

  @Override
  @Nonnull
  public InputLibrary getInputLibrary() {
    return inputJackLibrary;
  }
}
