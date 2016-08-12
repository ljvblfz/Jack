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
import com.android.jack.JackIOException;
import com.android.jack.Options;
import com.android.jack.backend.dex.rop.CodeItemBuilder;
import com.android.jack.dx.dex.DexOptions;
import com.android.jack.dx.dex.file.CallSiteIdsSection;
import com.android.jack.dx.dex.file.DexFile;
import com.android.jack.dx.dex.file.FieldIdsSection;
import com.android.jack.dx.dex.file.LazyCstIndexMap;
import com.android.jack.dx.dex.file.MethodHandleIdsSection;
import com.android.jack.dx.dex.file.MethodIdsSection;
import com.android.jack.dx.dex.file.ProtoIdsSection;
import com.android.jack.dx.dex.file.StringIdsSection;
import com.android.jack.dx.dex.file.TypeIdsSection;
import com.android.jack.dx.rop.cst.CstCallSiteRef;
import com.android.jack.dx.rop.cst.CstFieldRef;
import com.android.jack.dx.rop.cst.CstMethodHandleRef;
import com.android.jack.dx.rop.cst.CstMethodRef;
import com.android.jack.dx.rop.cst.CstPrototypeRef;
import com.android.jack.dx.rop.cst.CstString;
import com.android.jack.dx.rop.type.Type;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.formatter.BinaryQualifiedNameFormatter;
import com.android.jack.library.FileType;
import com.android.jack.library.FileTypeDoesNotExistException;
import com.android.jack.library.InputLibrary;
import com.android.jack.library.LibraryLocation;
import com.android.jack.library.OutputJackLibrary;
import com.android.jack.library.TypeInInputLibraryLocation;
import com.android.jack.scheduling.marker.ClassDefItemMarker;
import com.android.jack.scheduling.marker.ImportedDexClassMarker;
import com.android.jack.util.AndroidApiLevel;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.CannotCloseException;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotReadException;
import com.android.sched.util.file.CannotWriteException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.Location;
import com.android.sched.util.log.Event;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.OutputVFile;
import com.android.sched.vfs.VPath;

import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.Nonnull;

/**
 * Abstract class to write dex files into library.
 */
public abstract class DexInLibraryWriter extends DexWriter implements
    RunnableSchedulable<JDefinedClassOrInterface> {

  @Nonnull
  private final OutputJackLibrary outputLibrary = Jack.getSession().getJackOutputLibrary();

  private final boolean forceJumbo = ThreadConfig.get(CodeItemBuilder.FORCE_JUMBO).booleanValue();

  @Nonnull
  private final AndroidApiLevel apiLevel = ThreadConfig.get(Options.ANDROID_MIN_API_LEVEL);

  private final boolean useWholeDexPrebuilts =
      ThreadConfig.get(Options.USE_PREBUILT_FROM_LIBRARY).booleanValue()
      && ThreadConfig.get(Options.USE_WHOLE_DEX_PREBUILT).booleanValue();

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  @Override
  public void run(@Nonnull JDefinedClassOrInterface type) {
    OutputVFile vFile;

    if (useWholeDexPrebuilts) {
      Location loc = type.getLocation();
      if (loc instanceof TypeInInputLibraryLocation) {
        InputVFile in;
        InputLibrary inputLibrary = ((TypeInInputLibraryLocation) loc).getInputLibrary();
        LibraryLocation inputLibraryLocation = inputLibrary.getLocation();
        if (outputLibrary.containsLibraryLocation(inputLibraryLocation)) {
          return;
        }
        try {
          in = inputLibrary.getFile(FileType.PREBUILT,
              new VPath(BinaryQualifiedNameFormatter.getFormatter().getName(type), '/'));
          try {
            vFile = outputLibrary.createFile(FileType.PREBUILT,
                new VPath(BinaryQualifiedNameFormatter.getFormatter().getName(type), '/'));
          } catch (CannotCreateFileException e) {
            throw new JackIOException("Could not create Dex file in output "
                + outputLibrary.getLocation().getDescription() + " for type "
                + Jack.getUserFriendlyFormatter().getName(type), e);
          }

          try {
            vFile.copy(in);
          } catch (CannotCloseException | CannotReadException | CannotWriteException
              | WrongPermissionException e) {
            throw new JackIOException("Could not copy Dex file from "
                + in.getLocation().getDescription() + " to " + vFile.getLocation().getDescription(),
                e);
          }
          return;
        } catch (FileTypeDoesNotExistException e) {
          // Pre-dex is not accessible, thus write dex file from type
        }
      }
    }
    ClassDefItemMarker cdiMarker = type.getMarker(ClassDefItemMarker.class);
    assert cdiMarker != null;

    try (Event event = tracer.open(JackEventType.DX_BACKEND)) {
      DexOptions options = new DexOptions(apiLevel, forceJumbo);
      DexFile typeDex = new DexFile(options);

      ImportedDexClassMarker importedDex = type.getMarker(ImportedDexClassMarker.class);
      if (importedDex != null) {
        try (Event importEvent = tracer.open(JackEventType.DEX_CODE_IMPORT)) {
          LazyCstIndexMap indexMap = importedDex.getIndexMap();

          StringIdsSection stringIds = typeDex.getStringIds();
          for (CstString cstString : indexMap.getUsedCstString()) {
            stringIds.intern(cstString);
          }

          TypeIdsSection typeIds = typeDex.getTypeIds();
          for (Type typeRef : indexMap.getUsedType()) {
            typeIds.intern(typeRef);
          }

          MethodIdsSection methodIds = typeDex.getMethodIds();
          for (CstMethodRef cstMethodRef : indexMap.getUsedCstMethodRef()) {
            methodIds.intern(cstMethodRef);
          }

          FieldIdsSection fieldIds = typeDex.getFieldIds();
          for (CstFieldRef cstFieldRef : indexMap.getUsedCstFieldRef()) {
            fieldIds.intern(cstFieldRef);
          }

          ProtoIdsSection protoIds = typeDex.getProtoIds();
          for (CstPrototypeRef cstPrototypeRef : indexMap.getUsedCstPrototypeRef()) {
            protoIds.intern(cstPrototypeRef.getPrototype());
          }

          MethodHandleIdsSection methodHandleIds = typeDex.getMethodHandleIds();
          for (CstMethodHandleRef cstMethodHandleRef : indexMap.getUsedCstMethodHandleRef()) {
            methodHandleIds.intern(cstMethodHandleRef);
          }

          CallSiteIdsSection callSiteIds = typeDex.getCallSiteIds();
          for (CstCallSiteRef cstCallSiteRef : indexMap.getUsedCstCallSiteRef()) {
            callSiteIds.add(cstCallSiteRef);
          }
        }
      }

      typeDex.add(cdiMarker.getClassDefItem());
      try {
        vFile = outputLibrary.createFile(FileType.PREBUILT,
            new VPath(BinaryQualifiedNameFormatter.getFormatter().getName(type), '/'));
      } catch (CannotCreateFileException e) {
        throw new JackIOException(
            "Could not create Dex file in output " + outputLibrary.getLocation().getDescription()
                + " for type " + Jack.getUserFriendlyFormatter().getName(type),
            e);
      }
      try (OutputStream outStream = vFile.getOutputStream()) {
        typeDex.getStringIds().intern(DexWriter.getJackDexTag());
        typeDex.prepare();
        typeDex.writeTo(outStream, null, false);
      } catch (IOException | WrongPermissionException e) {
        throw new JackIOException(
            "Could not write Dex file to output " + vFile.getLocation().getDescription(), e);
      }
    }
  }
}
