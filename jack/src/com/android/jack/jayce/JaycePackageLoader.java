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
import com.android.jack.ir.ast.MissingJTypeLookupException;
import com.android.jack.library.FileType;
import com.android.jack.library.HasInputLibrary;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.library.InputLibrary;
import com.android.jack.library.LibraryReadingException;
import com.android.jack.load.PackageLoader;
import com.android.jack.lookup.JPhantomLookup;
import com.android.jack.reporting.Reporter.Severity;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.NotFileOrDirectoryException;
import com.android.sched.util.location.Location;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.vfs.InputVDir;
import com.android.sched.vfs.InputVElement;
import com.android.sched.vfs.VPath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * {@link PackageLoader} for package containing classes defined in Jayce files.
 */
public class JaycePackageLoader implements PackageLoader, HasInputLibrary {

  @Nonnull
  private static Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private final InputVDir packageVDir;

  @Nonnull
  private final JPhantomLookup lookup;

  @Nonnull
  private final NodeLevel defaultLoadLevel;

  @Nonnull
  private final InputJackLibrary inputJackLibrary;

  JaycePackageLoader(@Nonnull InputJackLibrary inputJackLibrary,
      @Nonnull InputVDir packageVDir, @Nonnull JPhantomLookup lookup,
      @Nonnull NodeLevel defaultLoadLevel) {
    assert inputJackLibrary.containsFileType(FileType.JAYCE);
    this.inputJackLibrary = inputJackLibrary;
    this.packageVDir = packageVDir;
    this.lookup = lookup;
    this.defaultLoadLevel = defaultLoadLevel;
  }

  @Override
  @Nonnull
  public JDefinedClassOrInterface loadClassOrInterface(@Nonnull JPackage loading,
      @Nonnull String simpleName) throws MissingJTypeLookupException {
    try {
      return new JayceClassOrInterfaceLoader(inputJackLibrary,
          loading,
          simpleName,
          packageVDir.getInputVFile(new VPath(simpleName + FileType.JAYCE.getFileExtension(), '/')),
          lookup,
          defaultLoadLevel).load();
    } catch (LibraryException e) {
      LibraryReadingException reportable = new LibraryReadingException(e);
      Jack.getSession().getReporter().report(Severity.FATAL, reportable);
      throw new JackAbortException(reportable);
    } catch (NotFileOrDirectoryException e1) {
      throw new MissingJTypeLookupException(loading, simpleName);
    } catch (NoSuchFileException e1) {
      throw new MissingJTypeLookupException(loading, simpleName);
    }
  }

  @Override
  @Nonnull
  public Collection<String> getSubClassNames(@Nonnull JPackage loading) {
    List<String> subs = new ArrayList<String>();
    for (InputVElement sub : packageVDir.list()) {
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
  public PackageLoader getLoaderForSubPackage(@Nonnull JPackage loading, @Nonnull String simpleName)
      throws JPackageLookupException {
    try {
      return new JaycePackageLoader(inputJackLibrary,
          packageVDir.getInputVDir(new VPath(simpleName, '/')), lookup, defaultLoadLevel);
    } catch (NotDirectoryException e) {
      throw new JPackageLookupException(simpleName, loading);
    } catch (NoSuchFileException e) {
      throw new JPackageLookupException(simpleName, loading);
    }
  }

  @Nonnull
  @Override
  public Collection<String> getSubPackageNames(@Nonnull JPackage loading) {
    List<String> subs = new ArrayList<String>();
    for (InputVElement sub : packageVDir.list()) {
      if (sub.isVDir()) {
        subs.add(sub.getName());
      }
    }
    return subs;
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
