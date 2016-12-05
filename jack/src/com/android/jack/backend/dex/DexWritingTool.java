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

package com.android.jack.backend.dex;

import com.android.jack.Jack;
import com.android.jack.JackEventType;
import com.android.jack.Options;
import com.android.jack.backend.dex.rop.CodeItemBuilder;
import com.android.jack.dx.dex.DexOptions;
import com.android.jack.dx.dex.file.DexFile;
import com.android.jack.dx.io.DexBuffer;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.formatter.BinaryQualifiedNameFormatter;
import com.android.jack.ir.formatter.TypePackageAndMethodFormatter;
import com.android.jack.ir.formatter.UserFriendlyFormatter;
import com.android.jack.library.FileType;
import com.android.jack.library.FileTypeDoesNotExistException;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.library.InputLibrary;
import com.android.jack.library.OutputJackLibrary;
import com.android.jack.library.TypeInInputLibraryLocation;
import com.android.jack.tools.merger.JackMerger;
import com.android.jack.tools.merger.MergingOverflowException;
import com.android.jack.util.AndroidApiLevel;
import com.android.sched.util.codec.VariableName;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotReadException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.Location;
import com.android.sched.util.log.Event;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.OutputVFS;
import com.android.sched.vfs.OutputVFile;
import com.android.sched.vfs.VPath;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A helper to write dex files.
 */
@VariableName("writer")
public abstract class DexWritingTool {
  @Nonnull
  protected final Tracer tracer = TracerFactory.getTracer();

  /**
   * {@link MatchableInputVFile} is used to deduplicate {@link InputVFile}.
   */
  static class MatchableInputVFile {

    @Nonnull
    private final InputVFile inputVFile;

    public MatchableInputVFile(@Nonnull InputVFile inputVFile) {
      this.inputVFile = inputVFile;
    }

    @Override
    public final boolean equals(@CheckForNull Object obj) {
      if (!(obj instanceof MatchableInputVFile)) {
        return false;
      }

      return inputVFile.getPathFromRoot()
          .equals(((MatchableInputVFile) obj).getInputVFile().getPathFromRoot());
    }

    @Override
    public final int hashCode() {
      return inputVFile.getPathFromRoot().hashCode();
    }

    @Nonnull
    public InputVFile getInputVFile() {
      return inputVFile;
    }
  }

  @Nonnull
  private static final TypePackageAndMethodFormatter FORMATTER = Jack.getLookupFormatter();

  @Nonnull
  private static Logger logger = LoggerFactory.getLogger();

  private final boolean forceJumbo = ThreadConfig.get(CodeItemBuilder.FORCE_JUMBO).booleanValue();

  @Nonnull
  private final AndroidApiLevel apiLevel = ThreadConfig.get(Options.ANDROID_MIN_API_LEVEL);

  protected final boolean usePrebuilts =
            ThreadConfig.get(Options.USE_PREBUILT_FROM_LIBRARY).booleanValue();


  @Nonnull
  protected DexFile createDexFile() {
    DexOptions options = new DexOptions(apiLevel, forceJumbo);
    return new DexFile(options);
  }

  public abstract void write(@Nonnull OutputVFS outputVDir) throws DexWritingException;

  protected void finishMerge(@Nonnull JackMerger merger, @Nonnull OutputVFile out)
      throws DexWritingException {
    try (Event event = tracer.open(JackEventType.DEX_MERGER_FINISH)) {
      OutputStream os = null;
      try {
        try {
          os = new BufferedOutputStream(out.getOutputStream());
          merger.finish(os);
        } finally {
          if (os != null) {
            os.close();
          }
        }
      } catch (IOException | WrongPermissionException e) {
        throw new DexWritingException(e);
      }
    }
  }

  protected void mergeDex(@Nonnull JackMerger merger, InputVFile inputDex)
      throws MergingOverflowException, DexWritingException {
    InputStream inputStream = null;
    try {
      inputStream = inputDex.getInputStream();
      merger.addDexFile(new DexBuffer(inputStream));
    } catch (IOException e) {
      throw new DexWritingException(new CannotReadException(inputDex, e));
    } catch (WrongPermissionException e) {
      throw new DexWritingException(e);
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          logger.log(
              Level.WARNING, "Failed to close ''{0}''", inputDex.getLocation().getDescription());
        }
      }
    }
  }

  @Nonnull
  protected OutputVFile getOutputDex(@Nonnull OutputVFS outputVfs, int dexCount)
      throws DexWritingException {
    assert dexCount >= 1;
    String dexName;
    if (dexCount == 1) {
      dexName = DexFileWriter.DEX_FILENAME;
    } else {
      dexName = DexFileWriter.DEX_PREFIX + dexCount + DexFileWriter.DEX_FILE_EXTENSION;
    }
    try {
      return outputVfs.getRootOutputVDir().createOutputVFile(new VPath(dexName, '/'));
    } catch (CannotCreateFileException e) {
      throw new DexWritingException(e);
    }
  }

  protected void fillDexLists(@Nonnull Set<MatchableInputVFile> mainDexList,
      @Nonnull List<MatchableInputVFile> anyDexList) {
    final OutputJackLibrary jackOutputLibrary = Jack.getSession().getJackOutputLibrary();
    Collection<JDefinedClassOrInterface> typesToEmit = Jack.getSession().getTypesToEmit();

    List<JDefinedClassOrInterface> anyTypeList = new ArrayList<JDefinedClassOrInterface>(
        typesToEmit.size());

    for (JDefinedClassOrInterface type : typesToEmit) {
      if (type.containsMarker(MainDexMarker.class)) {
        mainDexList.add(new MatchableInputVFile(getDexInputVFileOfType(jackOutputLibrary, type)));
      } else {
        anyTypeList.add(type);
      }
    }
    Collections.sort(anyTypeList, new Comparator<JDefinedClassOrInterface>() {
      @Override
      public int compare(@Nonnull JDefinedClassOrInterface first,
          @Nonnull JDefinedClassOrInterface second) {
        return FORMATTER.getName(first).compareTo(FORMATTER.getName(second));
      }});
    for (JDefinedClassOrInterface type : anyTypeList) {
      anyDexList.add(new MatchableInputVFile(getDexInputVFileOfType(jackOutputLibrary, type)));
    }

    if (usePrebuilts) {
      DexWritingTool.addOrphanDexFiles(/*outputLibrary = */ null, mainDexList,
          new HashSet<MatchableInputVFile>(anyDexList));
    }
  }

  @Nonnull
  protected InputVFile getDexInputVFileOfType(@Nonnull OutputJackLibrary jackOutputLibrary,
      @Nonnull JDefinedClassOrInterface type) {
    InputVFile inputVFile = null;
    Location location = type.getLocation();
    try {
      if (location instanceof TypeInInputLibraryLocation) {
        InputLibrary inputLibrary = ((TypeInInputLibraryLocation) location).getInputLibrary();
        if (inputLibrary.containsFileType(FileType.PREBUILT)) {
          inputVFile = inputLibrary.getFile(FileType.PREBUILT,
              new VPath(BinaryQualifiedNameFormatter.getFormatter().getName(type), '/'));
        }
      }

      if (inputVFile == null) {
        inputVFile = jackOutputLibrary.getFile(FileType.PREBUILT,
            new VPath(BinaryQualifiedNameFormatter.getFormatter().getName(type), '/'));
      }
    } catch (FileTypeDoesNotExistException e) {
      // this was created by Jack, so this should not happen
      throw new AssertionError(
          UserFriendlyFormatter.getFormatter().getName(type) + " does not exist");
    }

    return inputVFile;
  }

  static void addOrphanDexFiles(@CheckForNull OutputJackLibrary outputLibrary,
      @Nonnull Set<MatchableInputVFile> mainDexToMerge) {
    DexWritingTool.addOrphanDexFiles(outputLibrary, mainDexToMerge,
        Collections.<MatchableInputVFile>emptySet());
  }

  /**
   * Orphan dex file is a dex file without an associated Jayce file.
   */
  static void addOrphanDexFiles(@CheckForNull OutputJackLibrary outputLibrary,
      @Nonnull Set<MatchableInputVFile> mainDexToMerge,
      @Nonnull Set<MatchableInputVFile> othersDexToMerge) {
    for (InputLibrary inputLibrary : Jack.getSession().getImportedLibraries()) {
      if (inputLibrary instanceof InputJackLibrary) {
        InputJackLibrary inputJackLibrary = (InputJackLibrary) inputLibrary;
        if (outputLibrary != null
            && outputLibrary.containsLibraryLocation(inputJackLibrary.getLocation())) {
          continue;
        }
        Iterator<InputVFile> dexFileIt = inputJackLibrary.iterator(FileType.PREBUILT);
        while (dexFileIt.hasNext()) {
          InputVFile dexFile = dexFileIt.next();
          String dexFilePath = dexFile.getPathFromRoot().getPathAsString('/');
          int indexOfDexExtension = dexFilePath.indexOf(DexFileWriter.DEX_FILE_EXTENSION);
          // Prebuilt section of library does not contains only dex files
          if (indexOfDexExtension != -1) {
            String type =
                dexFilePath.substring(0, dexFilePath.indexOf(DexFileWriter.DEX_FILE_EXTENSION));
            try {
              inputJackLibrary.getFile(FileType.JAYCE, new VPath(type, '/'));
            } catch (FileTypeDoesNotExistException e) {
              MatchableInputVFile orphanDex = new MatchableInputVFile(dexFile);
              if (!othersDexToMerge.contains(orphanDex)) {
                mainDexToMerge.add(orphanDex);
              }
            }
          }
        }
      }
    }
  }
}
