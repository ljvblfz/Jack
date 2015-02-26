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

package com.android.jack.incremental;

import com.android.jack.Jack;
import com.android.jack.JackAbortException;
import com.android.jack.Options;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.library.InputLibrary;
import com.android.jack.library.InvalidLibrary;
import com.android.jack.library.JackLibraryFactory;
import com.android.jack.library.LibraryReadingException;
import com.android.jack.library.NotJackLibraryException;
import com.android.jack.library.OutputJackLibrary;
import com.android.jack.reporting.Reportable;
import com.android.jack.reporting.ReportableException;
import com.android.jack.reporting.Reporter.Severity;
import com.android.sched.util.RunnableHooks;
import com.android.sched.util.config.Config;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.AbstractStreamFile;
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileOrDirectory;
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.InputZipFile;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.vfs.CachedDirectFS;
import com.android.sched.vfs.Container;
import com.android.sched.vfs.ReadZipFS;
import com.android.sched.vfs.VFS;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Common part of {@link InputFilter}
 */
public abstract class CommonFilter {

  private static final class ClasspathEntryIgnoredReportable implements Reportable {
    @Nonnull
    private final Exception cause;

    private ClasspathEntryIgnoredReportable(@Nonnull Exception cause) {
      this.cause = cause;
    }

    @Override
    @Nonnull
    public String getMessage() {
      return "Bad classpath entry ignored: " + cause.getMessage();
    }

    @Override
    @Nonnull
    public ProblemLevel getDefaultProblemLevel() {
      return ProblemLevel.WARNING;
    }
  }

  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private final RunnableHooks hooks;

  public CommonFilter(@Nonnull RunnableHooks hooks) {
    this.hooks = hooks;
  }

  @Nonnull
  protected Set<String> getJavaFileNamesSpecifiedOnCommandLine(@Nonnull Options options) {
    Config config = options.getConfig();
    final String extension = ".java";

    Set<String> javaFileNames = new HashSet<String>();
    for (FileOrDirectory file : config.get(Options.SOURCES)) {
      if (file instanceof Directory) {
        fillFiles(((Directory) file).getFile(), extension, javaFileNames);
      } else if (file.getPath().endsWith(extension)) {
        javaFileNames.add(file.getPath());
      }
    }

    return (javaFileNames);
  }

  private void fillFiles(@Nonnull File folder, @Nonnull String fileExt,
      @Nonnull Set<String> fileNames) {
    for (File subFile : folder.listFiles()) {
      if (subFile.isDirectory()) {
        fillFiles(subFile, fileExt, fileNames);
      } else {
        String path = subFile.getPath();
        if (subFile.getName().endsWith(fileExt)) {
          fileNames.add(path);
        }
      }
    }
  }

  @Nonnull
  protected OutputJackLibrary getOutputJackLibraryFromVfs() {
    VFS vfs;
    Container containerType = ThreadConfig.get(Options.LIBRARY_OUTPUT_CONTAINER_TYPE);

    if (containerType == Container.DIR) {
      vfs = ThreadConfig.get(Options.LIBRARY_OUTPUT_DIR);
    } else {
      vfs = ThreadConfig.get(Options.LIBRARY_OUTPUT_ZIP);
    }

    return (JackLibraryFactory.getOutputLibrary(vfs, Jack.getEmitterId(), Jack.getVersionString()));
  }

  protected List<InputLibrary> getInputLibrariesFromFiles(@Nonnull List<InputLibrary> files,
      boolean strictMode) {
    List<InputLibrary> libraries = new ArrayList<InputLibrary>();

    for (InputLibrary library : files) {
      if (library instanceof InputJackLibrary) {
        libraries.add(library);
      } else if (library instanceof InvalidLibrary) {
        // STOPSHIP: rework reporting of InvalidLibrary, the following code is pretty bad...
        // let's find why this library is invalid
        Exception exception = null;
        try {
          File file = new File(library.getPath());
          FileOrDirectory.checkPermissions(file, library.getLocation(), Permission.READ);
          AbstractStreamFile.check(file, library.getLocation());
          // permissions are OK, the file exists, so let's consider this is not a Jack library
          throw new NotJackLibraryException(library.getLocation());
        } catch (WrongPermissionException e) {
          exception = e;
        } catch (NoSuchFileException e) {
          exception = e;
        } catch (NotFileException e) {
          exception = e;
        } catch (NotJackLibraryException e) {
          exception = e;
        }

        if (strictMode) {
          ReportableException reportable = new LibraryReadingException(exception);
          Jack.getSession().getReporter().report(Severity.FATAL, reportable);
          throw new JackAbortException(reportable);
        } else {
          // Ignore bad entry
          Jack.getSession().getReporter().report(Severity.NON_FATAL,
              new ClasspathEntryIgnoredReportable(exception));
        }
      }
    }

    return libraries;
  }

  @Nonnull
  protected VFS wrapAsVDir(@Nonnull final File dirOrZip)
      throws IOException {
    final VFS vfs;
    if (dirOrZip.isDirectory()) {
      vfs = new CachedDirectFS(new Directory(dirOrZip.getPath(), hooks, Existence.MUST_EXIST,
          Permission.READ, ChangePermission.NOCHANGE), Permission.READ);
    } else { // zip
      vfs = new ReadZipFS(new InputZipFile(dirOrZip.getPath(), hooks, Existence.MUST_EXIST,
          ChangePermission.NOCHANGE));
    }

    if (hooks != null) {
      hooks.addHook(new Runnable() {
        @Override
        public void run() {
          try {
            vfs.close();
          } catch (IOException e) {
            logger.log(Level.FINE, "Failed to close vfs for '" + dirOrZip + "'.", e);
          }
        }
      });
    }

    return vfs;
  }
}
