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
import com.android.jack.JackIOException;
import com.android.jack.JackUserException;
import com.android.jack.JarTransformationException;
import com.android.jack.LibraryException;
import com.android.jack.Options;
import com.android.jack.config.id.Carnac;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.Resource;
import com.android.jack.library.DumpInLibrary;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.library.InputJackLibraryCodec;
import com.android.jack.library.InputLibrary;
import com.android.jack.library.InvalidLibrary;
import com.android.jack.library.JackLibraryFactory;
import com.android.jack.library.JarLibrary;
import com.android.jack.library.LibraryReadingException;
import com.android.jack.library.NotJackLibraryException;
import com.android.jack.meta.Meta;
import com.android.jack.meta.MetaImporter;
import com.android.jack.reporting.Reportable;
import com.android.jack.reporting.ReportableException;
import com.android.jack.reporting.Reporter.Severity;
import com.android.jack.resource.ResourceImporter;
import com.android.jack.resource.ResourceReadingException;
import com.android.jill.Jill;
import com.android.jill.JillException;
import com.android.jill.utils.FileUtils;
import com.android.sched.util.codec.CodecContext;
import com.android.sched.util.codec.ParsingException;
import com.android.sched.util.config.Config;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.file.AbstractStreamFile;
import com.android.sched.util.file.CannotChangePermissionException;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileOrDirectory;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.Files;
import com.android.sched.util.file.InputZipFile;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.file.NotFileOrDirectoryException;
import com.android.sched.util.file.ReaderFile;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.file.ZipException;
import com.android.sched.util.location.DirectoryLocation;
import com.android.sched.util.location.FileLocation;
import com.android.sched.vfs.PrefixedFS;
import com.android.sched.vfs.ReadZipFS;
import com.android.sched.vfs.VFS;
import com.android.sched.vfs.VPath;
import com.android.sched.vfs.WrongVFSTypeException;
import com.android.sched.vfs.ZipUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Common part of {@link InputFilter}
 */
@HasKeyId
public abstract class CommonFilter {

  @Nonnull
  protected final Map<String, ReaderFile> path2ReaderFile = new HashMap<>();

  @Nonnull
  public static final BooleanPropertyId IMPORTED_JAR_DEBUG_INFO = BooleanPropertyId.create(
      "jack.import.jar.debug-info", "Keep debug info when importing Jars")
      .addDefaultValue(Boolean.TRUE).addCategory(DumpInLibrary.class).addCategory(Carnac.class);

  @Nonnull
  public static final BooleanPropertyId CLASSPATH_JAR_DEBUG_INFO = BooleanPropertyId.create(
      "jack.classpath.jar.debug-info", "Keep debug info when using Jars on classpath")
      .addDefaultValue(Boolean.FALSE).addCategory(DumpInLibrary.class);

  @Nonnull
  public static final BooleanPropertyId CLASSPATH_JAR_TOLERANT = BooleanPropertyId.create(
      "jack.classpath.jar.tolerant", "Tolerate malformed Jars on classpath")
      .addDefaultValue(Boolean.FALSE).addCategory(DumpInLibrary.class);

  @Nonnull
  public static final BooleanPropertyId IMPORTED_JAR_TOLERANT = BooleanPropertyId.create(
      "jack.import.jar.tolerant", "Tolerate import of malformed Jars")
      .addDefaultValue(Boolean.FALSE).addCategory(DumpInLibrary.class);

  /**
   * List of folders inside Jack jar file that can be used as embedded default jack libraries.
   */
  @Nonnull
  private static final String[] JACK_DEFAULT_LIB_PATH = new String[]{"jack-default-lib"};

  @Nonnull
  private final boolean importedJarDebugInfo =
      ThreadConfig.get(IMPORTED_JAR_DEBUG_INFO).booleanValue();

  @Nonnull
  private final boolean classpathJarDebugInfo =
      ThreadConfig.get(CLASSPATH_JAR_DEBUG_INFO).booleanValue();

  @Nonnull
  private final boolean classpathJarTolerant =
      ThreadConfig.get(CLASSPATH_JAR_TOLERANT).booleanValue();

  @Nonnull
  private final boolean importedJarTolerant =
      ThreadConfig.get(IMPORTED_JAR_TOLERANT).booleanValue();

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
    private final Throwable cause;

    private ClasspathEntryIgnoredReportable(@Nonnull Throwable cause) {
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
  protected Set<String> getJavaFileNamesSpecifiedOnCommandLine(@Nonnull Options options) {
    Config config = options.getConfig();
    final String extension = ".java";

    Set<String> javaFileNames = new HashSet<>();
    for (FileOrDirectory file : config.get(Options.SOURCES)) {
      if (file instanceof Directory) {
        fillFiles(((Directory) file).getFile(), extension, javaFileNames);
      } else if (file.getPath().endsWith(extension)) {
        // File already checked by codec
        javaFileNames.add(file.getPath());
        path2ReaderFile.put(file.getPath(), (ReaderFile) file);
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
        if (subFile.getName().endsWith(fileExt)) {
          String path = subFile.getPath();
          try {
            // Let's check the files contained in the folder since they have not checked by codec
            FileLocation location = new FileLocation(subFile);
            // We still need to check existence, because of non-existing symbolic link targets
            AbstractStreamFile.check(subFile, location);
            FileOrDirectory.checkPermissions(subFile, location, Permission.READ);
            fileNames.add(path);
            path2ReaderFile.put(path, new ReaderFile(subFile.getPath()));
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
  protected List<InputLibrary> getClasspathLibraries(
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
      } else if (library instanceof JarLibrary) {
        File libraryFile = new File(library.getPath());
        if (FileUtils.isJarFile(libraryFile)) {
          try {
            com.android.jill.Options jillOptions = new com.android.jill.Options();
            jillOptions.setBinaryFile(libraryFile);
            jillOptions.setEmitDebugInfo(classpathJarDebugInfo);
            jillOptions.setTolerant(classpathJarTolerant);
            libraries.add(convertJarWithJill(jillOptions));
          } catch (JarTransformationException e) {
            Jack.getSession().getReporter().report(Severity.FATAL, e);
            throw new JackAbortException(e);
          }
        } else {
          // We know this is a valid zip that does not have the .jar extension
          reportInvalidClasspathLibrary(new NotJackLibraryException(library.getLocation()),
              strictMode);
        }
      } else if (library instanceof InvalidLibrary) {
        reportInvalidClasspathLibrary(((InvalidLibrary) library).getInvalidCauses().get(0),
            strictMode);
      } else {
        throw new AssertionError();
      }
    }

    return libraries;
  }

  protected List<? extends InputLibrary> getImportedLibraries(@Nonnull List<InputLibrary> files) {
    List<InputLibrary> libraries = new ArrayList<InputLibrary>();
    for (InputLibrary library : files) {
      if (library instanceof InputJackLibrary) {
        libraries.add(library);
      } else if (library instanceof JarLibrary) {
        File libraryFile = new File(library.getPath());
        if (FileUtils.isJarFile(libraryFile)) {
          try {
            com.android.jill.Options jillOptions = new com.android.jill.Options();
            jillOptions.setBinaryFile(libraryFile);
            jillOptions.setEmitDebugInfo(importedJarDebugInfo);
            jillOptions.setTolerant(importedJarTolerant);
            libraries.add(convertJarWithJill(jillOptions));
          } catch (JarTransformationException e) {
            Jack.getSession().getReporter().report(Severity.FATAL, e);
            throw new JackAbortException(e);
          }
        } else {
          // We know this is a valid zip that does not have the .jar extension
          ReportableException reportable =
              new LibraryReadingException(new NotJackLibraryException(library.getLocation()));
          Jack.getSession().getReporter().report(Severity.FATAL, reportable);
          throw new JackAbortException(reportable);
        }
      } else if (library instanceof InvalidLibrary) {
        ReportableException reportable =
            new LibraryReadingException(((InvalidLibrary) library).getInvalidCauses().get(0));
        Jack.getSession().getReporter().report(Severity.FATAL, reportable);
        throw new JackAbortException(reportable);
      } else {
        throw new AssertionError();
      }
    }
    return libraries;
  }

  private void reportInvalidClasspathLibrary(@Nonnull Throwable cause, boolean strictMode) {

    if (strictMode) {
      ReportableException reportable = new LibraryReadingException(cause);
      Jack.getSession().getReporter().report(Severity.FATAL, reportable);
      throw new JackAbortException(reportable);
    } else {
      // Ignore bad entry
      Jack.getSession().getReporter().report(Severity.NON_FATAL,
          new ClasspathEntryIgnoredReportable(cause));
    }
  }

  private List<InputLibrary> getDefaultLibraries() {
    URL location = Jack.class.getProtectionDomain().getCodeSource().getLocation();
    JSession session = Jack.getSession();
    if (location != null) {
      List<InputLibrary> libraries = new ArrayList<InputLibrary>();
      try {
        File jackJar = new File(location.toURI().getPath());
        for (String prefix: JACK_DEFAULT_LIB_PATH) {
          VFS jackVfs = new PrefixedFS(new ReadZipFS(new InputZipFile(jackJar.getPath())),
              new VPath(prefix, ZipUtils.ZIP_SEPARATOR), Existence.MUST_EXIST);
          libraries.add(JackLibraryFactory.getInputLibrary(jackVfs));
        }
        return libraries;
      } catch (LibraryException | WrongVFSTypeException e) {
        EmbeddedLibraryLoadingException reportable = new EmbeddedLibraryLoadingException(e);
        session.getReporter().report(Severity.FATAL, reportable);
        throw new JackAbortException(reportable);
      } catch (URISyntaxException e) {
        throw new AssertionError();
      } catch (CannotCreateFileException e) {
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
  private InputJackLibrary convertJarWithJill(@Nonnull com.android.jill.Options jillOptions)
      throws JarTransformationException {
    try {
      final File tempFile = Files.createTempFile("jill-", ".jack");
      Runnable tempFileDeleter = new Runnable() {
        @Override
        public void run() {
          boolean deleted = tempFile.delete();
          if (!deleted) {
            throw new JackIOException("Failed to delete temporary file " + tempFile.getPath());
          }
        }
      };
      Jack.getSession().getHooks().addHook(tempFileDeleter);

      jillOptions.setOutput(tempFile);
      Jill.process(jillOptions);
      InputJackLibraryCodec codec = new InputJackLibraryCodec();
      CodecContext context = new CodecContext();
      InputJackLibrary inputLib = codec.checkString(context, tempFile.getPath());
      if (inputLib == null) {
        inputLib = codec.parseString(context, tempFile.getPath());
      }
      return inputLib;

    } catch (ParsingException e) {
      throw new JarTransformationException(e.getCause());
    } catch (CannotCreateFileException e) {
      throw new JarTransformationException(e);
    } catch (CannotChangePermissionException e) {
      throw new JarTransformationException(e);
    } catch (JillException e) {
      throw new JarTransformationException(e);
    }
  }

  @Nonnull
  protected List<Resource> importStandaloneResources() {
    JSession session = Jack.getSession();
    List<Resource> resources;
    try {
      resources = new ResourceImporter(ThreadConfig.get(ResourceImporter.IMPORTED_RESOURCES))
          .getImports();
    } catch (ResourceReadingException e) {
      session.getReporter().report(Severity.FATAL, e);
      throw new JackAbortException(e);
    }
    return resources;
  }

  @Nonnull
  protected List<Meta> importStandaloneMetas() {
    return new MetaImporter(ThreadConfig.get(MetaImporter.IMPORTED_META)).getImports();
  }
}
