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
import com.android.jack.analysis.dependency.Dependency;
import com.android.jack.analysis.dependency.file.FileDependencies;
import com.android.jack.analysis.dependency.file.FileDependenciesInLibraryWriter;
import com.android.jack.analysis.dependency.library.LibraryDependencies;
import com.android.jack.analysis.dependency.library.LibraryDependenciesInLibraryWriter;
import com.android.jack.analysis.dependency.type.TypeDependencies;
import com.android.jack.analysis.dependency.type.TypeDependenciesInLibraryWriter;
import com.android.jack.ir.ast.JSession;
import com.android.jack.library.FileType;
import com.android.jack.library.FileTypeDoesNotExistException;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.library.InputLibrary;
import com.android.jack.library.JackLibraryFactory;
import com.android.jack.library.LibraryFormatException;
import com.android.jack.library.LibraryIOException;
import com.android.jack.library.LibraryReadingException;
import com.android.jack.library.LibraryVersionException;
import com.android.jack.library.LibraryWritingException;
import com.android.jack.library.NotJackLibraryException;
import com.android.jack.library.OutputJackLibrary;
import com.android.jack.reporting.Reporter.Severity;
import com.android.sched.util.RunnableHooks;
import com.android.sched.util.codec.ImplementationName;
import com.android.sched.util.config.Config;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.file.CannotDeleteFileException;
import com.android.sched.util.file.CannotReadException;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Counter;
import com.android.sched.util.log.stats.CounterImpl;
import com.android.sched.util.log.stats.StatisticId;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.VFS;
import com.android.sched.vfs.VPath;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;


/**
 * {@link InputFilter} that returns filtered inputs required by incremental support.
 */
@ImplementationName(iface = InputFilter.class, name = "incremental")
@HasKeyId
public class IncrementalInputFilter extends CommonFilter implements InputFilter {

  @Nonnull
  public static final BooleanPropertyId INCREMENTAL_LOG = BooleanPropertyId
      .create("jack.incremental.log", "Enable incremental log")
      .addDefaultValue(Boolean.TRUE);

  @Nonnull
  public static final StatisticId<Counter> COMPILED_FILES = new StatisticId<Counter>(
      "jack.incremental.source.compiled", "Source files that will be compile", CounterImpl.class,
      Counter.class);

  @Nonnull
  public static final StatisticId<Counter> MODIFIED_FILES = new StatisticId<Counter>(
  "jack.incremental.source.modified",
  "Source files modified from the previous incremental compilation", CounterImpl.class,
  Counter.class);

  @Nonnull
  public static final StatisticId<Counter> DELETED_FILES = new StatisticId<Counter>(
  "jack.incremental.source.deleted",
  "Source files deleted from the previous incremental compilation", CounterImpl.class,
  Counter.class);

  @Nonnull
  public static final StatisticId<Counter> ADDED_FILES = new StatisticId<Counter>(
  "jack.incremental.source.added",
  "Source files added from the previous incremental compilation", CounterImpl.class,
  Counter.class);

  @Nonnull
  public static final StatisticId<Counter> SOURCE_FILES = new StatisticId<Counter>(
  "jack.incremental.source", "Source files to compile",
  CounterImpl.class, Counter.class);

  @Nonnull
  private final Options options;

  @CheckForNull
  private final InputJackLibrary incrementalInputLibrary;

  @Nonnull
  private final LibraryDependencies libraryDependencies = new LibraryDependencies();

  @Nonnull
  private final FileDependencies fileDependencies = new FileDependencies();

  @Nonnull
  private final TypeDependencies typeDependencies = new TypeDependencies();

  @Nonnull
  private final Set<String> fileNamesOnCmdLine;

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  @Nonnull
  private final Set<String> deletedFileNames = new HashSet<String>();

  @Nonnull
  private final Set<String> addedFileNames = new HashSet<String>();

  @Nonnull
  private final Set<String> modifiedFileNames = new HashSet<String>();

  @Nonnull
  private final Set<String> filesToRecompile;

  @Nonnull
  private final List<? extends InputLibrary> importedLibrariesFromCommandLine;

  @Nonnull
  private final List<? extends InputLibrary> librariesOnClasspathFromCommandLine;

  @Nonnull
  private final File incrementalFolder;

  public IncrementalInputFilter(@Nonnull Options options, @Nonnull RunnableHooks hooks) {
    super(hooks);
    Config config = ThreadConfig.getConfig();
    incrementalFolder = new File(config.get(Options.LIBRARY_OUTPUT_DIR).getPath());

    this.options = options;
    incrementalInputLibrary = getIncrementalInternalLibrary();
    fileNamesOnCmdLine = getJavaFileNamesSpecifiedOnCommandLine(options);

    tracer.getStatistic(IncrementalInputFilter.SOURCE_FILES).incValue(fileNamesOnCmdLine.size());

    JSession session = Jack.getSession();
    if (incrementalInputLibrary != null) {
      try {
        fillDependencies(incrementalInputLibrary, FileDependencies.vpath, fileDependencies);
        fillDependencies(incrementalInputLibrary, TypeDependencies.vpath, typeDependencies);
        fillDependencies(incrementalInputLibrary, LibraryDependencies.vpath,
            libraryDependencies);
      } catch (CannotReadException e) {
        LibraryReadingException reportable = new LibraryReadingException(
            new LibraryFormatException(incrementalInputLibrary.getLocation()));
        session.getReporter().report(Severity.FATAL, reportable);
        throw new JackAbortException(reportable);
      } catch (FileTypeDoesNotExistException e) {
        LibraryReadingException reportable = new LibraryReadingException(
            new LibraryFormatException(incrementalInputLibrary.getLocation()));
        session.getReporter().report(Severity.FATAL, reportable);
        throw new JackAbortException(reportable);
      }

      fillAddedFileNames(addedFileNames);
      fillModifiedFileNames(modifiedFileNames);
      fillDeletedFileNames(deletedFileNames);
    }

    importedLibrariesFromCommandLine = config.get(Options.IMPORTED_LIBRARIES);
    List<InputLibrary> classpathContent = config.get(Options.CLASSPATH);
    librariesOnClasspathFromCommandLine = getInputLibrariesFromFiles(
        classpathContent,
        config.get(Jack.STRICT_CLASSPATH).booleanValue());
    session.getLibraryDependencies().addImportedLibraries(importedLibrariesFromCommandLine);
    session.getLibraryDependencies().addLibrariesOnClasspath(librariesOnClasspathFromCommandLine);
    filesToRecompile = getInternalFileNamesToCompile();

    if (config.get(INCREMENTAL_LOG).booleanValue()) {
      IncrementalLogWriter incLog;
      try {
        VFS incrementalFolder = config.get(Options.LIBRARY_OUTPUT_DIR);
        assert incrementalFolder != null;
        incLog = new IncrementalLogWriter(getOutputJackLibrary(), incrementalFolder);
        incLog.writeString("type: " + (incrementalInputLibrary == null ? "full" : "incremental"));
        incLog.writeLibraryDescriptions("classpath", classpathContent);
        incLog.writeStrings("classpath digests (" + (libraryDependencies.hasSameLibraryOnClasspath(
            session.getLibraryDependencies()) ? "identical"
            : "modified") + ")",
            session.getLibraryDependencies().getDigestOfLibrariesOnClasspath());
        incLog.writeLibraryDescriptions("import", importedLibrariesFromCommandLine);
        incLog.writeStrings("import digests (" + (libraryDependencies.hasSameImportedLibrary(
            session.getLibraryDependencies()) ? "identical"
            : "modified") + ")",
            session.getLibraryDependencies().getDigestOfImportedLibraries());
        incLog.writeStrings("added (" + addedFileNames.size() + ")", addedFileNames);
        incLog.writeStrings("deleted (" + deletedFileNames.size() + ")", deletedFileNames);
        incLog.writeStrings("modified (" + modifiedFileNames.size() + ")", modifiedFileNames);
        incLog.writeStrings("compiled (" + filesToRecompile.size() + ")", filesToRecompile);
        incLog.close();
      } catch (LibraryIOException e) {
        LibraryWritingException reportable = new LibraryWritingException(e);
        Jack.getSession().getReporter().report(Severity.FATAL, reportable);
        throw new JackAbortException(reportable);
      }
    }
  }

  @Override
  @Nonnull
  public List<? extends InputLibrary> getClasspath() {
    return librariesOnClasspathFromCommandLine;
  }

  @Override
  @Nonnull
  public Set<String> getFileNamesToCompile() {
    return filesToRecompile;
  }

  @Nonnull
  private Set<String> getInternalFileNamesToCompile() {
    if (needFullBuild()) {
      tracer.getStatistic(IncrementalInputFilter.COMPILED_FILES).incValue(
          fileNamesOnCmdLine.size());
      return fileNamesOnCmdLine;
    }

    Map<String, Set<String>> typeRecompileDependencies =
        typeDependencies.getRecompileDependencies();

    Set<String> filesToRecompile = new HashSet<String>();

    filesToRecompile.addAll(addedFileNames);
    filesToRecompile.addAll(modifiedFileNames);

    addDependencies(filesToRecompile, typeRecompileDependencies, modifiedFileNames);
    addDependencies(filesToRecompile, typeRecompileDependencies, deletedFileNames);

    tracer.getStatistic(IncrementalInputFilter.COMPILED_FILES).incValue(filesToRecompile.size());

    return filesToRecompile;
  }

  private void addDependencies(@Nonnull Set<String> filesToRecompile,
      @Nonnull Map<String, Set<String>> typeRecompileDependencies, @Nonnull Set<String> fileNames) {
    for (String fileName : fileNames) {
      for (String dependencyFileName :
          getDependencyFileNamesToRecompile(typeRecompileDependencies, fileName)) {
        filesToRecompile.add(dependencyFileName);
      }
    }
  }

  private void updateIncrementalState()
      throws IncrementalException {
    if (incrementalInputLibrary != null) {
      for (String fileToRecompile : getFileNamesToCompile()) {
        deleteOldFilesFromJavaFiles(fileToRecompile);
      }

      for (String deletedFileName : deletedFileNames) {
        deleteOldFilesFromJavaFiles(deletedFileName);
      }

      typeDependencies.update(fileDependencies, deletedFileNames, modifiedFileNames);
      fileDependencies.update(deletedFileNames, modifiedFileNames);

      OutputJackLibrary outputLibrary = getOutputJackLibrary();
      FileDependenciesInLibraryWriter.write(outputLibrary, fileDependencies);
      TypeDependenciesInLibraryWriter.write(outputLibrary, typeDependencies);
      LibraryDependenciesInLibraryWriter.write(outputLibrary, libraryDependencies);

      Jack.getSession().setFileDependencies(fileDependencies);
      Jack.getSession().setTypeDependencies(typeDependencies);
    }
  }

  private void deleteOldFilesFromJavaFiles(@Nonnull String javaFileName)
      throws IncrementalException {
    List<String> deletedTypes = new ArrayList<String>();
    for (String typeNameToRemove : fileDependencies.getTypeNames(javaFileName)) {
      if (!deletedTypes.contains(typeNameToRemove)) {
        deletedTypes.add(typeNameToRemove);
        VPath vpath = new VPath(typeNameToRemove, '/');
        deleteFile(FileType.JAYCE, vpath);
        deleteFile(FileType.DEX, vpath);
      }
    }
  }

  private void deleteFile(@Nonnull FileType fileType, @Nonnull VPath vpath)
      throws IncrementalException {
    assert incrementalInputLibrary != null;
    try {
      incrementalInputLibrary.delete(fileType, vpath);
    } catch (FileTypeDoesNotExistException e) {
      // Nothing to do, file does no longer exists
    } catch (CannotDeleteFileException e) {
      throw new IncrementalException(e);
    }
  }

  /*
   * A full build is needed when an imported library was modified or when a library from classpath
   * was modified or that the library representing incremental state does not exists.
   */
  private boolean needFullBuild() {
    JSession session = Jack.getSession();
    return incrementalInputLibrary == null ||
        !libraryDependencies.hasSameLibraryOnClasspath(session.getLibraryDependencies())
        || !libraryDependencies.hasSameImportedLibrary(session.getLibraryDependencies());
  }

  @Nonnull
  private List<String> getDependencyFileNamesToRecompile(
      @Nonnull Map<String, Set<String>> typeRecompileDependencies,
      @Nonnull String modifiedJavaFileName) {
    List<String> fileNamesToRecompile = new ArrayList<String>();

    assert fileDependencies != null;
    for (String modifiedTypeName : fileDependencies.getTypeNames(modifiedJavaFileName)) {
      for (String typeName : typeRecompileDependencies.get(modifiedTypeName)) {
        String dependentFileName = fileDependencies.getJavaFileName(typeName);
        if (dependentFileName != null && !deletedFileNames.contains(dependentFileName)) {
          fileNamesToRecompile.add(dependentFileName);
        }
      }
    }

    return fileNamesToRecompile;
  }

  @CheckForNull
  private InputJackLibrary getIncrementalInternalLibrary() {
    try {
      return JackLibraryFactory.getInputLibrary(ThreadConfig.get(Options.LIBRARY_OUTPUT_DIR));
    } catch (NotJackLibraryException e) {
      // No incremental internal library, it is the first compilation
    } catch (LibraryVersionException e) {
      // Incremental internal library has changed, do not reuse it
    } catch (LibraryFormatException e) {
      // Incremental internal library has changed, do not reuse it
    }
    return null;
  }

  @Nonnull
  private void fillAddedFileNames(@Nonnull Set<String> addedFileNames) {
    assert fileDependencies != null;
    Set<String> previousFiles = fileDependencies.getCompiledJavaFiles();

    for (String javaFileName : fileNamesOnCmdLine) {
      if (!previousFiles.contains(javaFileName)) {
        addedFileNames.add(javaFileName);
      }
    }

    tracer.getStatistic(IncrementalInputFilter.ADDED_FILES).incValue(addedFileNames.size());
  }

  @Nonnull
  private void fillModifiedFileNames(@Nonnull Set<String> modifiedFileNames) {
    assert fileDependencies != null;

    for (String javaFileName : fileDependencies.getCompiledJavaFiles()) {
      if (fileNamesOnCmdLine.contains(javaFileName)) {
        File javaFile = new File(javaFileName);
        for (String typeName : fileDependencies.getTypeNames(javaFileName)) {
          File dexFile = getDexFile(typeName);
          if (!dexFile.exists() || ((javaFile.lastModified() > dexFile.lastModified()))) {
            modifiedFileNames.add(javaFileName);
          }
        }
      }
    }

    tracer.getStatistic(IncrementalInputFilter.MODIFIED_FILES).incValue(modifiedFileNames.size());
  }


  @Nonnull
  private void fillDeletedFileNames(@Nonnull Set<String> deletedFileNames) {
    assert fileDependencies != null;

    for (String javaFileName : fileDependencies.getCompiledJavaFiles()) {
      if (!fileNamesOnCmdLine.contains(javaFileName)) {
        deletedFileNames.add(javaFileName);
      }
    }

    tracer.getStatistic(IncrementalInputFilter.DELETED_FILES).incValue(deletedFileNames.size());
  }

  @Nonnull
  private File getDexFile(@Nonnull String typeName) {
    return new File(incrementalFolder, FileType.DEX.buildFileVPath(
        new VPath(typeName, '/')).getPathAsString(File.separatorChar));
  }

  @Nonnull
  private void fillDependencies(@Nonnull InputJackLibrary library, @Nonnull VPath dependencyVPath,
      @Nonnull Dependency dependency)
      throws CannotReadException, FileTypeDoesNotExistException {
    InputVFile dependenciesVFile = library.getFile(FileType.DEPENDENCIES, dependencyVPath);
    InputStreamReader fileReader = null;
    try {
      fileReader = new InputStreamReader(dependenciesVFile.openRead());
      dependency.read(fileReader);
    } catch (NoSuchElementException e) {
      throw new CannotReadException(dependenciesVFile, e);
    } catch (IOException e) {
      throw new CannotReadException(dependenciesVFile, e);
    } finally {
      if (fileReader != null) {
        try {
          fileReader.close();
        } catch (IOException e) {
        }
      }
    }
  }

  @Override
  @Nonnull
  public List<InputLibrary> getImportedLibrary() {
    List<InputLibrary> inputLibraries =
        new ArrayList<InputLibrary>(importedLibrariesFromCommandLine);

    if (needFullBuild()) {
      Jack.getSession().setFileDependencies(new FileDependencies());
      Jack.getSession().setTypeDependencies(new TypeDependencies());
      return inputLibraries;
    }

    try {
      updateIncrementalState();
    } catch (IncrementalException e) {
      Jack.getSession().getReporter().report(Severity.FATAL, e);
      throw new JackAbortException(e);
    }

    inputLibraries.add(0, incrementalInputLibrary);

    return inputLibraries;
  }

  @Override
  @Nonnull
  public OutputJackLibrary getOutputJackLibrary() {
    if (incrementalInputLibrary == null) {
      return getOutputJackLibraryFromVfs();
    }

    return (JackLibraryFactory.getOutputLibrary(ThreadConfig.get(Options.LIBRARY_OUTPUT_DIR),
        Jack.getEmitterId(), Jack.getVersion().getVerboseVersion()));
  }
}
