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
import com.android.jack.JackUserException;
import com.android.jack.LibraryException;
import com.android.jack.Options;
import com.android.jack.ir.ast.JSession;
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
import com.android.sched.util.config.Config;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.AbstractStreamFile;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotSetPermissionException;
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileAlreadyExistsException;
import com.android.sched.util.file.FileOrDirectory;
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.InputZipFile;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.file.NotFileOrDirectoryException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.DirectoryLocation;
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.vfs.Container;
import com.android.sched.vfs.PrefixedFS;
import com.android.sched.vfs.ReadWriteZipFS;
import com.android.sched.vfs.ReadZipFS;
import com.android.sched.vfs.VFS;
import com.android.sched.vfs.VPath;
import com.android.sched.vfs.ZipUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.ZipException;

import javax.annotation.Nonnull;

/**
 * Common part of {@link InputFilter}
 */
public abstract class CommonFilter {

  /**
   * List of folders inside Jack jar file that can be used as embedded default jack libraries.
   */
  @Nonnull
  private static final String[] JACK_DEFAULT_LIB_PATH = new String[]{"jack-default-lib"};

  private static final class FailedToLocateJackJarException extends Exception {

    private static final long serialVersionUID = 1L;

    @Override
    public String getMessage() {
      return "Failed to locate jack jar file";
    }
  }

  private static final class EmbeddedLibraryLoadingException extends ReportableException {

    private static final long serialVersionUID = 1L;

    private EmbeddedLibraryLoadingException(@Nonnull Exception cause) {
      super(cause);
    }

    @Override
    @Nonnull
    public String getMessage() {
      return "Error while loading embedded libraries. Try specifying jack classpath: "
          + getCause().getMessage();
    }

    @Override
    @Nonnull
    public ProblemLevel getDefaultProblemLevel() {
      return ProblemLevel.ERROR;
    }
  }

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
  protected final VFS incrementalVfs;

  @Nonnull
  private final OutputJackLibrary outputJackLibrary;

  public CommonFilter() {
    if (ThreadConfig.get(Options.GENERATE_LIBRARY_FROM_INCREMENTAL_FOLDER).booleanValue()) {
      VFS dirVFS = ThreadConfig.get(Options.LIBRARY_OUTPUT_DIR);
      incrementalVfs = ThreadConfig.get(Options.LIBRARY_OUTPUT_ZIP);
      ((ReadWriteZipFS) incrementalVfs).setWorkVFS(dirVFS);
    } else {
      if (ThreadConfig.get(Options.LIBRARY_OUTPUT_CONTAINER_TYPE) == Container.DIR) {
        incrementalVfs = ThreadConfig.get(Options.LIBRARY_OUTPUT_DIR);
      } else {
        incrementalVfs = ThreadConfig.get(Options.LIBRARY_OUTPUT_ZIP);
      }
    }

    outputJackLibrary = JackLibraryFactory.getOutputLibrary(incrementalVfs,
        Jack.getEmitterId(), Jack.getVersion().getVerboseVersion());
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
        // File already checked by codec
        javaFileNames.add(file.getPath());
      }
    }

    return (javaFileNames);
  }

  protected void fillFiles(@Nonnull File folder, @Nonnull String fileExt,
      @Nonnull Set<String> fileNames) {
    File[] fileList = folder.listFiles();
    if (fileList == null) {
      throw new JackUserException(new IOException("Failed to list "
          + new DirectoryLocation(folder).getDescription()));
    }
    for (File subFile : fileList) {
      if (subFile.isDirectory()) {
        fillFiles(subFile, fileExt, fileNames);
      } else {
        String path = subFile.getPath();
        if (subFile.getName().endsWith(fileExt)) {
          try {
            // Let's check the files contained in the folder since they have not checked by codec
            FileLocation location = new FileLocation(subFile);
            // We still need to check existence, because of non-existing symbolic link targets
            AbstractStreamFile.check(subFile, location);
            FileOrDirectory.checkPermissions(subFile, location, Permission.READ);
            fileNames.add(path);
          } catch (WrongPermissionException e) {
            throw new JackUserException(e);
          } catch (NotFileException e) {
            throw new JackUserException(e);
          } catch (NoSuchFileException e) {
            throw new JackUserException(e);
          }
        }
      }
    }
  }

  @Nonnull
  protected List<InputLibrary> getInputLibrariesFromFiles(
      @Nonnull List<InputLibrary> files,
      boolean strictMode) {
    List<InputLibrary> libraries;
    if (!ThreadConfig.get(Options.USE_DEFAULT_LIBRARIES).booleanValue()) {
      libraries = new ArrayList<InputLibrary>();
    } else {
      libraries = getDefaultLibraries();
    }

    for (InputLibrary library : files) {
      if (library instanceof InputJackLibrary) {
        libraries.add(library);
      } else if (library instanceof InvalidLibrary) {
        // let's find why this library is invalid
        Exception exception = null;
        try {
          File file = new File(library.getPath());
          AbstractStreamFile.check(file, library.getLocation());
          FileOrDirectory.checkPermissions(file, library.getLocation(), Permission.READ);
          // the file exists, permissions are OK, so let's consider this is not a Jack library
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

  private List<InputLibrary> getDefaultLibraries() {
    URL location = Jack.class.getProtectionDomain().getCodeSource().getLocation();
    JSession session = Jack.getSession();
    if (location != null) {
      List<InputLibrary> libraries = new ArrayList<InputLibrary>();
      try {
        File jackJar = new File(location.toURI().getPath());
        for (String prefix: JACK_DEFAULT_LIB_PATH) {
          VFS jackVfs = new PrefixedFS(new ReadZipFS(new InputZipFile(jackJar.getPath(),
              session.getHooks(),
              Existence.MUST_EXIST,
              ChangePermission.NOCHANGE)), new VPath(prefix, ZipUtils.ZIP_SEPARATOR));
          libraries.add(JackLibraryFactory.getInputLibrary(jackVfs));
        }
        return libraries;
      } catch (LibraryException e) {
        EmbeddedLibraryLoadingException reportable = new EmbeddedLibraryLoadingException(e);
        session.getReporter().report(Severity.FATAL, reportable);
        throw new JackAbortException(reportable);
      } catch (URISyntaxException e) {
        throw new AssertionError();
      } catch (FileAlreadyExistsException e) {
        throw new AssertionError();
      } catch (CannotCreateFileException e) {
        throw new AssertionError();
      } catch (CannotSetPermissionException e) {
        throw new AssertionError();
      } catch (WrongPermissionException e) {
        EmbeddedLibraryLoadingException reportable = new EmbeddedLibraryLoadingException(e);
        session.getReporter().report(Severity.FATAL, reportable);
        throw new JackAbortException(reportable);
      } catch (NoSuchFileException e) {
        EmbeddedLibraryLoadingException reportable = new EmbeddedLibraryLoadingException(e);
        session.getReporter().report(Severity.FATAL, reportable);
        throw new JackAbortException(reportable);
      } catch (NotFileOrDirectoryException e) {
        EmbeddedLibraryLoadingException reportable = new EmbeddedLibraryLoadingException(e);
        session.getReporter().report(Severity.FATAL, reportable);
        throw new JackAbortException(reportable);
      } catch (ZipException e) {
        EmbeddedLibraryLoadingException reportable = new EmbeddedLibraryLoadingException(e);
        session.getReporter().report(Severity.FATAL, reportable);
        throw new JackAbortException(reportable);
      }
    } else {
      EmbeddedLibraryLoadingException e = new EmbeddedLibraryLoadingException(
          new FailedToLocateJackJarException());
      session.getReporter().report(Severity.FATAL, e);
      throw new JackAbortException(e);
    }
  }

  @Nonnull
  public OutputJackLibrary getOutputJackLibrary() {
    return outputJackLibrary;
  }
}
