/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.shrob.obfuscation;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldId;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.shrob.obfuscation.key.FieldKey;
import com.android.jack.shrob.obfuscation.key.MethodKey;
import com.android.jack.transformations.request.TransformationRequest;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.annotation.Nonnull;

/**
 * A class that parses a mapping file and renames all fields and methods with the same signature
 * with the same name.
 */
public class CollectingMappingApplier extends MappingApplier {

  @Nonnull
  private final Map<FieldKey, String> fieldNames = new HashMap<FieldKey, String>();

  @Nonnull
  private final Map<MethodKey, String> methodNames = new HashMap<MethodKey, String>();

  @Nonnull
  public Map<FieldKey, String> getFieldNames() {
    return fieldNames;
  }

  @Nonnull
  public Map<MethodKey, String> getMethodNames() {
    return methodNames;
  }

  public CollectingMappingApplier(@Nonnull TransformationRequest request) {
    super(request);
  }

  @Override
  protected void renameField(
      @Nonnull JField field, @Nonnull File mappingFile, int lineNumber, @Nonnull String newName) {
    JFieldId id = field.getId();
    if (!id.containsMarker(OriginalNameMarker.class)) {
      super.renameField(field, mappingFile, lineNumber, newName);
      FieldKey oldKey = new FieldKey(id);
      String previousNewName = fieldNames.get(oldKey);
      if (previousNewName != null && !previousNewName.equals(newName)) {
        logger.log(Level.WARNING, "{0}:{1}: Cannot rename field {2} in {3} to {4} "
            + "because it has already been mapped to {5}",
            new Object[] {mappingFile.getPath(),
                Integer.valueOf(lineNumber),
                oldKey,
                Jack.getUserFriendlyFormatter().getName(field.getEnclosingType()),
                previousNewName,
                newName});
      } else {
        fieldNames.put(new FieldKey(id), newName);
      }
    }
  }

  @Override
  protected void renameMethod(
      @Nonnull JMethod method, @Nonnull File mappingFile, int lineNumber, @Nonnull String newName) {
    JMethodIdWide id = method.getMethodIdWide();
    if (!id.containsMarker(OriginalNameMarker.class)) {
      super.renameMethod(method, mappingFile, lineNumber, newName);
      if (methodNames != null) {
        MethodKey methodKey = new MethodKey(id);
        String previousNewName = methodNames.get(methodKey);
        if (previousNewName != null && !previousNewName.equals(newName)) {
          logger.log(Level.WARNING, "{0}:{1}: Cannot rename method {2} in {3} to {4} "
              + "because it has already been mapped to {5}",
              new Object[] {mappingFile.getPath(),
                  Integer.valueOf(lineNumber),
                  Jack.getUserFriendlyFormatter().getName(method),
                  Jack.getUserFriendlyFormatter().getName(method.getEnclosingType()),
                  previousNewName,
                  newName});
        } else {
          methodNames.put(methodKey, newName);
        }
      }
    }
  }

}
