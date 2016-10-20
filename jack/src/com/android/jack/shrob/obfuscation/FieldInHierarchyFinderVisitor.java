/*
 * Copyright (C) 2015 The Android Open Source Project
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

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldId;
import com.android.jack.shrob.obfuscation.key.FieldKey;

import javax.annotation.Nonnull;

/**
 * A visitor that visits a type hierarchy and search for a field name.
 */
public class FieldInHierarchyFinderVisitor extends OneTimeHierarchyVisitor {

  @Nonnull
  private final FieldKey fieldKey;

  private boolean hasFoundField = false;

  private FieldInHierarchyFinderVisitor(@Nonnull FieldKey fieldKey) {
    this.fieldKey = fieldKey;
  }

  public void startVisit(@Nonnull JDefinedClassOrInterface type) {
    // Search static and private fields
    for (JField field : type.getFields()) {
      JFieldId id = field.getId();
      if (field.isPrivate() && !Renamer.mustBeRenamed(id)) {
        if (new FieldKey(id).equals(fieldKey)) {
          hasFoundField = true;
          return;
        }
      }
    }
    visitSuperTypes(type);
    visitSubTypes(type);
  }

  @Override
  public boolean doAction(@Nonnull JDefinedClassOrInterface type) {
    // Search in already renamed fields
    NewFieldKeyMarker marker = type.getMarker(NewFieldKeyMarker.class);
    if (marker != null && marker.getNewKeys().contains(fieldKey)) {
      hasFoundField = true;
      return false;
    }

    // Search field in impacted types excluding static and private methods
    for (JField field : type.getFields()) {
      JFieldId id = field.getId();
      if (!field.isPrivate() && !Renamer.mustBeRenamed(id)) {
        if (new FieldKey(id).equals(fieldKey)) {
          hasFoundField = true;
          return false;
        }
      }
    }
    return true;
  }

  public static boolean containsFieldKey(@Nonnull FieldKey key, @Nonnull JField field) {
    FieldInHierarchyFinderVisitor visitor = new FieldInHierarchyFinderVisitor(key);
    visitor.startVisit(field.getEnclosingType());
    return visitor.hasFoundField;
  }
}

