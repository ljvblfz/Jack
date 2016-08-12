/*
 * Copyright (C) 2016 The Android Open Source Project
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

import com.google.common.collect.Maps;

import com.android.jack.Jack;
import com.android.jack.JackAbortException;
import com.android.jack.dx.dex.file.LazyCstIndexMap;
import com.android.jack.dx.io.ClassData;
import com.android.jack.dx.io.ClassData.Method;
import com.android.jack.dx.io.ClassDef;
import com.android.jack.dx.io.DexBuffer;
import com.android.jack.dx.io.MethodId;
import com.android.jack.dx.io.ProtoId;
import com.android.jack.dx.io.TypeList;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.formatter.BinaryQualifiedNameFormatter;
import com.android.jack.ir.formatter.TypePackageAndMethodFormatter;
import com.android.jack.library.FileType;
import com.android.jack.library.FileTypeDoesNotExistException;
import com.android.jack.library.InputLibrary;
import com.android.jack.library.TypeInInputLibraryLocation;
import com.android.jack.reporting.ReportableException;
import com.android.jack.reporting.Reporter.Severity;
import com.android.jack.scheduling.filter.TypeWithValidMethodPrebuilt;
import com.android.jack.scheduling.filter.TypeWithoutValidTypePrebuilt;
import com.android.jack.scheduling.marker.ImportedDexClassMarker;
import com.android.jack.scheduling.marker.ImportedDexMethodMarker;
import com.android.jack.transformations.EmptyClinit;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.Protect;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotReadException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.Location;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.VPath;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Builds markers required for prebuilt dex code importation.
 */
@Description("Builds markers required for prebuilt dex code importation.")
@Constraint(no = EmptyClinit.class)
@Transform(add = {ImportedDexClassMarker.class, ImportedDexMethodMarker.class})
@Protect(add = {JDefinedClassOrInterface.class, JMethod.class},
         modify = {JDefinedClassOrInterface.class, JMethod.class},
         remove = {JDefinedClassOrInterface.class, JMethod.class})
@Filter({TypeWithValidMethodPrebuilt.class, TypeWithoutValidTypePrebuilt.class})
public class ImportedDexMarkerBuilder implements RunnableSchedulable<JDefinedClassOrInterface> {

  /**
   * Creates the {@link LazyCstIndexMap} for the given {@link JDefinedClassOrInterface}. The created
   * {@link LazyCstIndexMap} instance is then attached to the {@link JDefinedClassOrInterface} in a
   * {@link ImportedDexClassMarker} to be accessible from other schedulables.
   *
   * @param declaredType a {@link JDefinedClassOrInterface} for which a
   *        {@link ImportedDexClassMarker} is created.
   */
  @Override
  public void run(@Nonnull JDefinedClassOrInterface declaredType) {
    Location loc = declaredType.getLocation();
    assert loc instanceof TypeInInputLibraryLocation;
    InputVFile vFile;
    InputLibrary inputLibrary = ((TypeInInputLibraryLocation) loc).getInputLibrary();
    try {
      vFile = inputLibrary.getFile(FileType.PREBUILT,
          new VPath(BinaryQualifiedNameFormatter.getFormatter().getName(declaredType), '/'));
      try (InputStream in = vFile.getInputStream()) {
        try {
          DexBuffer dexBuffer = new DexBuffer(in);
          LazyCstIndexMap indexMap = new LazyCstIndexMap(dexBuffer);
          Iterator<ClassDef> classDefs = dexBuffer.classDefs().iterator();
          assert classDefs.hasNext();
          ClassDef classDef = classDefs.next();
          assert (!classDefs.hasNext())
          && classDef.getTypeName().equals(Jack.getLookupFormatter().getName(declaredType));

          ImportedDexClassMarker marker = new ImportedDexClassMarker(indexMap, dexBuffer, classDef);
          declaredType.addMarker(marker);

          if (classDef.getClassDataOffset() != 0) {
            ClassData classDataToMerge = dexBuffer.readClassData(classDef);

            Method[] allMethods = classDataToMerge.allMethods();
            Map<String, Method> dexMethods = Maps.newHashMapWithExpectedSize(allMethods.length);
            for (Method method : allMethods) {
              dexMethods.put(getFullDescriptor(dexBuffer, method), method);
            }

            TypePackageAndMethodFormatter formater = Jack.getLookupFormatter();
            for (JMethod jMethod : declaredType.getMethods()) {
              Method dexMethod = dexMethods.get(formater.getName(jMethod));
              assert dexMethod != null;
              jMethod.addMarker(new ImportedDexMethodMarker(dexMethod));
            }
          }
        } catch (IOException e) {
          throw new CannotReadException(vFile, e);
        }
      } catch (IOException e) {
        throw new CannotCreateFileException(vFile, e);
      }
    } catch (FileTypeDoesNotExistException | WrongPermissionException e) {
      // handled by @Filter
      throw new AssertionError(e);
    } catch (CannotReadException | CannotCreateFileException e) {
      PrebuiltImportException reportable = new PrebuiltImportException(e);
      Jack.getSession().getReporter().report(Severity.FATAL, reportable);
      throw new JackAbortException(reportable);
    }
  }

  @Nonnull
  private static String getFullDescriptor(@Nonnull DexBuffer dexBuffer, @Nonnull Method method) {
    MethodId id = dexBuffer.methodIds().get(method.getMethodIndex());
    StringBuilder sb = new StringBuilder(dexBuffer.strings().get(id.getNameIndex()));
    sb.append('(');
    ProtoId protoId = dexBuffer.protoIds().get(id.getProtoIndex());
    TypeList paramTypes = dexBuffer.readTypeList(protoId.getParametersOffset());
    for (short paramTypeId : paramTypes.getTypes()) {
      sb.append(dexBuffer.typeNames().get(paramTypeId));
    }
    sb.append(')');
    sb.append(dexBuffer.typeNames().get(protoId.getReturnTypeIndex()));
    return sb.toString();
  }


  private static class PrebuiltImportException extends ReportableException {
    private static final long serialVersionUID = 1L;

    public PrebuiltImportException (@Nonnull Throwable cause) {
      super(cause);
    }

    @Nonnull
    @Override
    public String getMessage() {
      return "Prebuilt import: " + getCause().getMessage();
    }

    @Override
    @Nonnull
    public ProblemLevel getDefaultProblemLevel() {
      return ProblemLevel.ERROR;
    }
  }
}
