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
import com.android.jack.analysis.dependency.file.FileDependencies;
import com.android.jack.analysis.dependency.file.FileDependenciesInLibraryWriter;
import com.android.jack.analysis.dependency.type.TypeDependencies;
import com.android.jack.analysis.dependency.type.TypeDependenciesInLibraryWriter;
import com.android.jack.backend.dex.DexFileWriter;
import com.android.jack.library.FileType;
import com.android.jack.library.FileTypeDoesNotExistException;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.library.JackLibraryFactory;
import com.android.jack.library.LibraryFormatException;
import com.android.jack.library.LibraryReadingException;
import com.android.jack.library.LibraryVersionException;
import com.android.jack.library.NotJackLibraryException;
import com.android.jack.library.OutputJackLibrary;
import com.android.jack.reporting.Reporter.Severity;
import com.android.sched.util.codec.ImplementationName;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.CannotDeleteFileException;
import com.android.sched.util.file.CannotReadException;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Counter;
import com.android.sched.util.log.stats.CounterImpl;
import com.android.sched.util.log.stats.StatisticId;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.VPath;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;


/**
 * {@link InputFilter} that returns filtered inputs required by incremental support.
 */
@ImplementationName(iface = InputFilter.class, name = "incremental")
public class IncrementalInputFilter extends CommonFilter implements InputFilter {

  @Nonnull
  public static final StatisticId<Counter> RECOMPILED_FILES = new StatisticId<Counter>(
  "jack.incremental.source.recompiled",
  "Source files that must be recompiled from the previous incremental compilation",
  CounterImpl.class, Counter.class);

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

  @CheckForNull
  private FileDependencies fileDependencies;

  @CheckForNull
  private TypeDependencies typeDependencies;

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

  public IncrementalInputFilter(@Nonnull Options options) {
    this.options = options;
    incrementalInputLibrary = getIncrementalInternalLibrary();
    fileNamesOnCmdLine = getJavaFileNamesSpecifiedOnCommandLine(options);

    tracer.getStatistic(IncrementalInputFilter.SOURCE_FILES).incValue(fileNamesOnCmdLine.size());

    if (incrementalInputLibrary != null) {
      try {
        fileDependencies = getFileDependencies(incrementalInputLibrary);
        typeDependencies = getTypeDependencies(incrementalInputLibrary);
      } catch (CannotReadException e) {
        LibraryReadingException reportable = new LibraryReadingException(
            new LibraryFormatException(incrementalInputLibrary.getLocation()));
        Jack.getSession().getReporter().report(Severity.FATAL, reportable);
        throw new JackAbortException(reportable);
      } catch (FileTypeDoesNotExistException e) {
        LibraryReadingException reportable = new LibraryReadingException(
            new LibraryFormatException(incrementalInputLibrary.getLocation()));
        Jack.getSession().getReporter().report(Severity.FATAL, reportable);
        throw new JackAbortException(reportable);
      }

      fillAddedFileNames(addedFileNames);
      fillModifiedFileNames(modifiedFileNames);
      fillDeletedFileNames(deletedFileNames);
    }

    filesToRecompile = getInternalFileNamesToCompile();
  }

  @Override
  @Nonnull
  public List<File> getClasspath() {
    return options.getClasspath();
  }

  @Override
  @Nonnull
  public Set<String> getFileNamesToCompile() {
    return filesToRecompile;
  }

  @Nonnull
  private Set<String> getInternalFileNamesToCompile() {
    InputJackLibrary incrementalInputLibrary = getIncrementalInternalLibrary();

    if (incrementalInputLibrary == null || needFullRebuild()) {
      return fileNamesOnCmdLine;
    }

    assert typeDependencies != null;
    Map<String, Set<String>> typeRecompileDependencies =
        typeDependencies.getRecompileDependencies();

    Set<String> filesToRecompile = new HashSet<String>();

    filesToRecompile.addAll(addedFileNames);
    filesToRecompile.addAll(modifiedFileNames);

    addDependencies(filesToRecompile, typeRecompileDependencies, modifiedFileNames);
    addDependencies(filesToRecompile, typeRecompileDependencies, deletedFileNames);

    tracer.getStatistic(IncrementalInputFilter.RECOMPILED_FILES).incValue(filesToRecompile.size());

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

  private void updateLibrary() throws IncrementalException {
    assert fileDependencies != null;
    assert typeDependencies != null;

    for (String fileToRecompile : getFileNamesToCompile()) {
      deleteOldFilesFromJavaFiles(fileToRecompile);
    }

    for (String deletedFileName : deletedFileNames) {
      deleteOldFilesFromJavaFiles(deletedFileName);
    }

    typeDependencies.update(fileDependencies, deletedFileNames, modifiedFileNames);
    fileDependencies.update(deletedFileNames, modifiedFileNames);

    OutputJackLibrary outputLibrary = JackLibraryFactory.getOutputLibrary(
        ThreadConfig.get(Options.LIBRARY_OUTPUT_DIR), Jack.getEmitterId(),
        Jack.getVersionString());

    FileDependenciesInLibraryWriter.write(outputLibrary, fileDependencies);
    TypeDependenciesInLibraryWriter.write(outputLibrary, typeDependencies);

    Jack.getSession().setFileDependencies(fileDependencies);
    Jack.getSession().setTypeDependencies(typeDependencies);
  }

  private void deleteOldFilesFromJavaFiles(@Nonnull String javaFileName)
      throws IncrementalException {
    assert fileDependencies != null;
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
      // Check that file exists
      incrementalInputLibrary.getFile(fileType, vpath);
      incrementalInputLibrary.delete(fileType, vpath);
    } catch (FileTypeDoesNotExistException e) {
      // Nothing to do, file does no longer exists
    } catch (CannotDeleteFileException e) {
      throw new IncrementalException(e);
    }
  }

  /*
   * A full rebuild is needed when an imported library was modified or when a library from classpath
   * was modified.
   */
  private boolean needFullRebuild() {
    if (!options.isAutomaticFullRebuildEnabled()) {
      return false;
    }

    long timestamp = new File(options.getOutputDir(), DexFileWriter.DEX_FILENAME).lastModified();

    for (File lib : options.getClasspath()) {
      if (isModifiedLibrary(lib, timestamp)) {
        return true;
      }
    }

    for (File importedJackFiles : options.getImportedLibraries()) {
      if (isModifiedLibrary(importedJackFiles, timestamp)) {
        return true;
      }
    }

    return false;
  }

  private boolean isModifiedLibrary(@Nonnull File inputLibrary, long timestamp) {
    if (inputLibrary.isFile() && (inputLibrary.lastModified() > timestamp)) {
      return true;
    } else if (inputLibrary.isDirectory() && hasModifiedFile(inputLibrary, timestamp)) {
      return true;
    }

    return false;
  }

  private boolean hasModifiedFile(@Nonnull File inputLibrary, long timestamp) {
    assert inputLibrary.isDirectory();

    for (File f : inputLibrary.listFiles()) {
      if (f.isDirectory()) {
        if (hasModifiedFile(f, timestamp)) {
          return true;
        }
      } else if (f.lastModified() > timestamp) {
          return true;
      }
    }

    return false;
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
      return JackLibraryFactory.getInputLibrary(
          ThreadConfig.get(Options.LIBRARY_OUTPUT_DIR));
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
    return new File(options.getIncrementalFolder(), FileType.DEX.buildFileVPath(
        new VPath(typeName, '/')).getPathAsString(File.separatorChar));
  }

  @Nonnull
  private FileDependencies getFileDependencies(@Nonnull InputJackLibrary library)
      throws CannotReadException, FileTypeDoesNotExistException {
    InputVFile fileDependenciesVFile =
        library.getFile(FileType.DEPENDENCIES, FileDependencies.vpath);


    FileDependencies fileDependencies = new FileDependencies();
    InputStreamReader fileReader = null;
    try {
      fileReader = new InputStreamReader(fileDependenciesVFile.openRead());
      fileDependencies.read(fileReader);
    } catch (IOException e) {
      throw new CannotReadException(fileDependenciesVFile.getLocation(), e);
    } finally {
      if (fileReader != null) {
        try {
          fileReader.close();
        } catch (IOException e) {
        }
      }
    }

    return fileDependencies;
  }

  @Nonnull
  private TypeDependencies getTypeDependencies(@Nonnull InputJackLibrary library)
      throws CannotReadException, FileTypeDoesNotExistException {
    InputVFile typeDependenciesVFile =
        library.getFile(FileType.DEPENDENCIES, TypeDependencies.vpath);

    TypeDependencies typeDependencies = new TypeDependencies();
    InputStreamReader fileReader = null;
    try {
      fileReader = new InputStreamReader(typeDependenciesVFile.openRead());
      typeDependencies.read(fileReader);
    } catch (IOException e) {
      throw new CannotReadException(typeDependenciesVFile.getLocation(), e);
    } finally {
      if (fileReader != null) {
        try {
          fileReader.close();
        } catch (IOException e) {
        }
      }
    }

    return typeDependencies;
  }

  @Override
  @Nonnull
  public List<File> getImportedLibrary() {
    if (incrementalInputLibrary == null || needFullRebuild()) {
      Jack.getSession().setFileDependencies(new FileDependencies());
      Jack.getSession().setTypeDependencies(new TypeDependencies());
      return options.getImportedLibraries();
    }

    try {
      updateLibrary();
    } catch (IncrementalException e) {
      Jack.getSession().getReporter().report(Severity.FATAL, e);
      throw new JackAbortException(e);
    }

    List<File> importedJackLibrary = new ArrayList<File>(options.getImportedLibraries());
    importedJackLibrary.add(options.getIncrementalFolder());
    return importedJackLibrary;
  }

  @Override
  @Nonnull
  public OutputJackLibrary getOutputJackLibrary() {
    if (incrementalInputLibrary == null) {
      return getOutputJackLibraryFromVfs();
    }

    return (JackLibraryFactory.getOutputLibrary(incrementalInputLibrary, Jack.getEmitterId(),
        Jack.getVersionString()));
  }
}
