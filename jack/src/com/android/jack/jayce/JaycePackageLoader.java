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

import com.android.jack.Jack;
import com.android.jack.JackAbortException;
import com.android.jack.LibraryException;
import com.android.jack.backend.jayce.JayceFileImporter;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JPackageLookupException;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.MissingJTypeLookupException;
import com.android.jack.library.FileType;
import com.android.jack.library.HasInputLibrary;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.library.InputLibrary;
import com.android.jack.library.LibraryReadingException;
import com.android.jack.load.PackageLoader;
import com.android.jack.reporting.Reporter.Severity;
import com.android.jack.util.collect.UnmodifiableCollections;
import com.android.sched.util.codec.VariableName;
import com.android.sched.util.location.Location;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.vfs.InputVDir;
import com.android.sched.vfs.InputVElement;
import com.android.sched.vfs.InputVFile;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * {@link PackageLoader} for package containing classes defined in Jayce files.
 */
@VariableName("loader")
public class JaycePackageLoader implements PackageLoader, HasInputLibrary {

  @Nonnull
  private static Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private final InputVDir packageVDir;

  @Nonnull
  private final JSession session;

  @Nonnull
  private final NodeLevel defaultLoadLevel;

  @Nonnull
  private final InputJackLibrary inputJackLibrary;

  @Nonnull
  private final Map<String, InputVDir> vdirCache = new HashMap<String, InputVDir>();

  @Nonnull
  private final Map<String, InputVFile> jayceFileCache = new HashMap<String, InputVFile>();

  @Nonnull
  private final UnmodifiableCollections collections = Jack.getUnmodifiableCollections();

  JaycePackageLoader(@Nonnull InputJackLibrary inputJackLibrary,
      @Nonnull InputVDir packageVDir, @Nonnull JSession session,
      @Nonnull NodeLevel defaultLoadLevel) {
    assert inputJackLibrary.containsFileType(FileType.JAYCE);
    this.inputJackLibrary = inputJackLibrary;
    this.packageVDir = packageVDir;
    this.session = session;
    this.defaultLoadLevel = defaultLoadLevel;
    for (InputVElement sub : packageVDir.list()) {
      String name = sub.getName();
      if (sub.isVDir()) {
        vdirCache.put(name, (InputVDir) sub);
      } else if (JayceFileImporter.isJackFileName(name)) {
        jayceFileCache.put(
            name.substring(0, name.length() - JayceFileImporter.JACK_EXTENSION_LENGTH),
            (InputVFile) sub);
      }
    }
  }

  @Override
  @Nonnull
  public JDefinedClassOrInterface loadClassOrInterface(
      @Nonnull JPackage loading, @Nonnull String simpleName) throws MissingJTypeLookupException {
    InputVFile inputVFile = jayceFileCache.get(simpleName);

    if (inputVFile == null) {
      throw new MissingJTypeLookupException(loading, simpleName);
    }

    try {
      return new JayceClassOrInterfaceLoader(inputJackLibrary,
          loading,
          simpleName,
          inputVFile,
          session,
          defaultLoadLevel).load();
    } catch (LibraryException e) {
      LibraryReadingException reportable = new LibraryReadingException(e);
      Jack.getSession().getReporter().report(Severity.FATAL, reportable);
      throw new JackAbortException(reportable);
    }
  }

  @Override
  @Nonnull
  public Collection<String> getSubClassNames(@Nonnull JPackage loading) {
    return collections.getUnmodifiableCollection(jayceFileCache.keySet());
  }

  @Nonnull
  @Override
  public PackageLoader getLoaderForSubPackage(@Nonnull JPackage loading,
      @Nonnull String simpleName) throws JPackageLookupException {
    InputVDir input = vdirCache.get(simpleName);

    if (input == null) {
      throw new JPackageLookupException(simpleName, loading);
    }

    return new JaycePackageLoader(inputJackLibrary, input, session, defaultLoadLevel);
  }

  @Nonnull
  @Override
  public Collection<String> getSubPackageNames(@Nonnull JPackage loading) {
    return collections.getUnmodifiableCollection(vdirCache.keySet());
  }

  @Override
  @Nonnull
  public Location getLocation(@Nonnull JPackage loaded) {
      return packageVDir.getLocation();
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
