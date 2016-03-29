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

import javax.annotation.Nonnull;

/**
 * A visitor that visits a type hierarchy and search for a field name.
 */
public class FieldInHierarchyFinderVisitor extends OneTimeHierarchyVisitor {

  @Nonnull
  private final String fieldName;

  private boolean hasFoundField = false;

  private FieldInHierarchyFinderVisitor(@Nonnull String fieldName) {
    this.fieldName = fieldName;
  }

  public void startVisit(@Nonnull JDefinedClassOrInterface type) {
    // Search static and private fields
    for (JField field : type.getFields()) {
      if (field.isPrivate() && !Renamer.mustBeRenamed(field.getId())) {
        if (field.getName().equals(fieldName)) {
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
    NewFieldNameMarker marker = type.getMarker(NewFieldNameMarker.class);
    if (marker != null && marker.getNewNames().contains(fieldName)) {
      hasFoundField = true;
      return false;
    }

    // Search field in impacted types excluding static and private methods
    for (JField field : type.getFields()) {
      if (!field.isPrivate() && !Renamer.mustBeRenamed(field.getId())) {
        if (field.getName().equals(fieldName)) {
          hasFoundField = true;
          return false;
        }
      }
    }
    return true;
  }

  public static boolean containsFieldKey(@Nonnull String name, @Nonnull JField field) {
    FieldInHierarchyFinderVisitor visitor = new FieldInHierarchyFinderVisitor(name);
    visitor.startVisit(field.getEnclosingType());
    return visitor.hasFoundField;
  }
}

