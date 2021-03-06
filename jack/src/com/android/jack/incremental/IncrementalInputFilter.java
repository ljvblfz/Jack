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
import com.android.jack.ir.ast.Resource;
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
import com.android.jack.meta.Meta;
import com.android.jack.reporting.ReportableIOException;
import com.android.jack.reporting.Reporter.Severity;
import com.android.sched.util.codec.ImplementationName;
import com.android.sched.util.config.Config;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.file.CannotDeleteFileException;
import com.android.sched.util.file.CannotGetModificationTimeException;
import com.android.sched.util.file.CannotReadException;
import com.android.sched.util.file.ReaderFile;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Counter;
import com.android.sched.util.log.stats.CounterImpl;
import com.android.sched.util.log.stats.StatisticId;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.UnionVFSReadOnlyException;
import com.android.sched.vfs.VPath;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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
      .addDefaultValue(Boolean.FALSE);

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
  private final Set<String> filesToRecompiles;

  @Nonnull
  private List<InputLibrary> importedLibraries;

  @Nonnull
  private final List<Resource> importedResources;

  @Nonnull
  private final List<Meta> importedMetas;

  @Nonnull
  private final List<? extends InputLibrary> librariesOnClasspath;

  public IncrementalInputFilter(@Nonnull Options options) {
    Config config = ThreadConfig.getConfig();

    incrementalInputLibrary = getIncrementalInternalLibrary();

    fileNamesOnCmdLine = getJavaFileNamesSpecifiedOnCommandLine(options);

    tracer.getStatistic(IncrementalInputFilter.SOURCE_FILES).incValue(fileNamesOnCmdLine.size());

    List<InputLibrary> importedLibrariesFromCommandLine = config.get(Options.IMPORTED_LIBRARIES);

    JSession session = Jack.getSession();

    boolean mergingEnabled;
    // An incremental input library without dependencies must not be used with union vfs otherwise
    // wrong dependencies can be read, moreover the method fillDependencies expects to have a
    // dependencies section into the Jack library.
    if (incrementalInputLibrary != null
        && incrementalInputLibrary.containsFileType(FileType.DEPENDENCIES)) {

      mergingEnabled = incrementalInputLibrary.canBeMerged(importedLibrariesFromCommandLine);
      if (mergingEnabled) {
        incrementalInputLibrary.mergeInputLibraries(importedLibrariesFromCommandLine);
        importedLibraries = Collections.<InputLibrary>singletonList(incrementalInputLibrary);
      } else {
        importedLibraries =
            new ArrayList<InputLibrary>(importedLibrariesFromCommandLine.size() + 1);
        importedLibraries.add(incrementalInputLibrary);
        importedLibraries.addAll(importedLibrariesFromCommandLine);
      }

      // delete all resources (they should all come from "--import-resource"), because we can't know
      // if they have been modified, so we'll have to recopy them
      deleteAllResources();

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
    } else {
      mergingEnabled = session.getJackOutputLibrary().canBeMerged(importedLibrariesFromCommandLine);
      if (mergingEnabled) {
        session.getJackOutputLibrary().mergeInputLibraries(importedLibrariesFromCommandLine);
      }
    }

    List<InputLibrary> classpathContent = config.get(Options.CLASSPATH);
    librariesOnClasspath = getClasspathLibraries(
        classpathContent,
        config.get(Jack.STRICT_CLASSPATH).booleanValue());
    session.getLibraryDependencies().addImportedLibraries(importedLibrariesFromCommandLine);
    session.getLibraryDependencies().addLibrariesOnClasspath(librariesOnClasspath);
    filesToRecompiles = getInternalFileNamesToCompile();

    importedResources = importStandaloneResources();
    importedMetas = importStandaloneMetas();

    if (config.get(INCREMENTAL_LOG).booleanValue()) {
      IncrementalLogWriter incLog;
      try {
        incLog = new IncrementalLogWriter(session.getJackOutputLibrary());
        incLog.writeString("type: " + (needFullBuild() ? "full" : "incremental"));
        incLog.writeLibraryDescriptions("classpath", librariesOnClasspath);
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
        incLog.writeStrings("compiled (" + filesToRecompiles.size() + ")", filesToRecompiles);
        incLog.writeString(
            "imported libraries have " + (mergingEnabled ? "" : "not ") + "been unified");
        incLog.close();
      } catch (LibraryIOException e) {
        LibraryWritingException reportable = new LibraryWritingException(e);
        Jack.getSession().getReporter().report(Severity.FATAL, reportable);
        throw new JackAbortException(reportable);
      }
    }

    if (needFullBuild()) {
      session.setFileDependencies(new FileDependencies());
      session.setTypeDependencies(new TypeDependencies());
      importedLibraries = importedLibrariesFromCommandLine;

      // incremental dir won't be used as an input library since we need a full build, so let's
      // close its "input" side
      if (incrementalInputLibrary != null) {
        try {
          incrementalInputLibrary.close();
        } catch (LibraryIOException e) {
          // should not happen since we only close it as an input
          throw new AssertionError(e);
        }
      }
    } else {
      try {
        updateIncrementalState();
      } catch (IncrementalException e) {
        session.getReporter().report(Severity.FATAL, e);
        throw new JackAbortException(e);
      }
    }
  }

  @Override
  @Nonnull
  public List<? extends InputLibrary> getClasspath() {
    return librariesOnClasspath;
  }

  @Override
  @Nonnull
  public Set<ReaderFile> getFileToCompile() {
    Set<ReaderFile> fileToCompile = new HashSet<>();
    for (String fileName : filesToRecompiles) {
      fileToCompile.add(path2ReaderFile.get(fileName));
    }
    return fileToCompile;
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
      for (String fileToRecompile : filesToRecompiles) {
        deleteOldFilesFromJavaFiles(fileToRecompile);
      }

      for (String deletedFileName : deletedFileNames) {
        deleteOldFilesFromJavaFiles(deletedFileName);
      }

      typeDependencies.update(fileDependencies, deletedFileNames, modifiedFileNames);
      fileDependencies.update(deletedFileNames, modifiedFileNames);

      OutputJackLibrary outputLibrary = Jack.getSession().getJackOutputLibrary();
      FileDependenciesInLibraryWriter.write(outputLibrary, fileDependencies);
      TypeDependenciesInLibraryWriter.write(outputLibrary, typeDependencies);
      LibraryDependenciesInLibraryWriter.write(outputLibrary, libraryDependencies);

      Jack.getSession().setFileDependencies(fileDependencies);
      Jack.getSession().setTypeDependencies(typeDependencies);
    }
  }

  private void deleteAllResources() {
    assert incrementalInputLibrary != null;

    Iterator<InputVFile> vFileIt = incrementalInputLibrary.iterator(FileType.RSC);
    while (vFileIt.hasNext()) {
      try {
        incrementalInputLibrary.delete(FileType.RSC, vFileIt.next().getPathFromRoot());
      } catch (CannotDeleteFileException | FileTypeDoesNotExistException e) {
        // should not happen
        throw new AssertionError(e);
      } catch (UnionVFSReadOnlyException e) {
        // ignore, we only want to delete from the incremental dir anyway
      }
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
        deleteFile(FileType.PREBUILT, vpath);
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
      return JackLibraryFactory.getInputLibrary(Jack.getSession().getJackOutputLibrary());
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
    assert incrementalInputLibrary != null;

    for (String javaFileName : fileDependencies.getCompiledJavaFiles()) {
      if (fileNamesOnCmdLine.contains(javaFileName)) {
        File javaFile = new File(javaFileName);
        for (String typeName : fileDependencies.getTypeNames(javaFileName)) {
          InputVFile dexFile;
          try {
            dexFile = incrementalInputLibrary.getFile(FileType.PREBUILT, new VPath(typeName, '/'));
          } catch (FileTypeDoesNotExistException e) {
            dexFile = null;
          }
          try {
            try {
              if (dexFile == null || ((Files.getLastModifiedTime(javaFile.toPath())
                  .compareTo(dexFile.getLastModified()) > 0))) {
                modifiedFileNames.add(javaFileName);
              }
            } catch (IOException e) {
              throw new CannotReadException(new FileLocation(javaFile), e);
            }
          } catch (CannotReadException | CannotGetModificationTimeException e) {
            ReportableIOException reportable =
                new ReportableIOException("Computing incremental state", e);
            Jack.getSession().getReporter().report(Severity.FATAL, reportable);
            throw new JackAbortException(reportable);
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
  private void fillDependencies(@Nonnull InputJackLibrary library, @Nonnull VPath dependencyVPath,
      @Nonnull Dependency dependency)
      throws CannotReadException, FileTypeDoesNotExistException {
    InputVFile dependenciesVFile = library.getFile(FileType.DEPENDENCIES, dependencyVPath);
    InputStreamReader fileReader = null;
    try {
      fileReader = new InputStreamReader(dependenciesVFile.getInputStream());
      dependency.read(fileReader);
    } catch (NoSuchElementException | WrongPermissionException | IOException e) {
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
  public List<? extends InputLibrary> getImportedLibraries() {
    return importedLibraries;
  }

  @Override
  @Nonnull
  public List<? extends Resource> getImportedResources() {
    return importedResources;
  }

  @Override
  @Nonnull
  public List<? extends Meta> getImportedMetas() {
    return importedMetas;
  }
}
