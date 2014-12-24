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
  private final Options options;

  @CheckForNull
  private final InputJackLibrary incrementalInputLibrary;

  @CheckForNull
  private FileDependencies fileDependencies;

  @CheckForNull
  private TypeDependencies typeDependencies;

  @Nonnull
  private final Set<String> fileNamesOnCmdLine;

  public IncrementalInputFilter(@Nonnull Options options) {
    this.options = options;
    incrementalInputLibrary = getIncrementalInternalLibrary();
    fileNamesOnCmdLine = getJavaFileNamesSpecifiedOnCommandLine(options);
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
    }
  }

  @Override
  @Nonnull
  public List<File> getClasspath() {
    return options.getClasspath();
  }

  @Override
  @Nonnull
  public Set<String> getFileNamesToCompile() {
    InputJackLibrary incrementalInputLibrary = getIncrementalInternalLibrary();

    if (incrementalInputLibrary == null || needFullRebuild()) {
      return fileNamesOnCmdLine;
    }

    assert typeDependencies != null;

    Map<String, Set<String>> typeRecompileDependencies =
        typeDependencies.getRecompileDependencies();

    Set<String> filesToRecompile = new HashSet<String>();

    filesToRecompile.addAll(getAddedFileNames());

    addDependencies(filesToRecompile, typeRecompileDependencies, getModifiedFileNames());

    addDependencies(filesToRecompile, typeRecompileDependencies, getDeleteFileNames());

    return filesToRecompile;
  }

  private void addDependencies(@Nonnull Set<String> filesToRecompile,
      @Nonnull Map<String, Set<String>> typeRecompileDependencies, @Nonnull Set<String> fileNames) {
    for (String fileName : fileNames) {
      if (filesToRecompile.add(fileName)) {
        for (String dependencyFileName :
            getDependencyFileNamesToRecompile(typeRecompileDependencies, fileName)) {
          filesToRecompile.add(dependencyFileName);
        }
      }
    }
  }

  private void updateLibrary() throws IncrementalException {
    assert fileDependencies != null;
    assert typeDependencies != null;

    Set<String> deleteFileNames = getDeleteFileNames();
    Set<String> modifiedFileNames = getModifiedFileNames();


    for (String fileToRecompile : getFileNamesToCompile()) {
      deleteOldFilesFromJavaFiles(fileToRecompile);
    }

    for (String deletedFileName : deleteFileNames) {
      deleteOldFilesFromJavaFiles(deletedFileName);
    }

    typeDependencies.update(fileDependencies, deleteFileNames, modifiedFileNames);
    fileDependencies.update(deleteFileNames, modifiedFileNames);

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

    for (File importedJackFiles : options.getJayceImport()) {
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
    Set<String> deleteFileNames = getDeleteFileNames();

    assert fileDependencies != null;
    for (String modifiedTypeName : fileDependencies.getTypeNames(modifiedJavaFileName)) {
      for (String typeName : typeRecompileDependencies.get(modifiedTypeName)) {
        String dependentFileName = fileDependencies.getJavaFileName(typeName);
        if (dependentFileName != null && !deleteFileNames.contains(dependentFileName)) {
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
  private Set<String> getAddedFileNames() {
    assert fileDependencies != null;
    Set<String> addedFileNames = new HashSet<String>();
    Set<String> previousFiles = fileDependencies.getCompiledJavaFiles();

    for (String javaFileName : fileNamesOnCmdLine) {
      if (!previousFiles.contains(javaFileName)) {
        addedFileNames.add(javaFileName);
      }
    }

    return addedFileNames;
  }

  @Nonnull
  private Set<String> getModifiedFileNames() {
    assert fileDependencies != null;
    Set<String> modifiedFileNames = new HashSet<String>();

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

    return modifiedFileNames;
  }


  @Nonnull
  private Set<String> getDeleteFileNames() {
    assert fileDependencies != null;
    Set<String> deletedFileNames = new HashSet<String>();

    for (String javaFileName : fileDependencies.getCompiledJavaFiles()) {
      if (!fileNamesOnCmdLine.contains(javaFileName)) {
        deletedFileNames.add(javaFileName);
      }
    }

    return deletedFileNames;
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
      return options.getJayceImport();
    }

    try {
      updateLibrary();
    } catch (IncrementalException e) {
      Jack.getSession().getReporter().report(Severity.FATAL, e);
      throw new JackAbortException(e);
    }

    List<File> importedJackLibrary = new ArrayList<File>(options.getJayceImport());
    importedJackLibrary.add(options.getIncrementalFolder());
    return importedJackLibrary;
  }
}
