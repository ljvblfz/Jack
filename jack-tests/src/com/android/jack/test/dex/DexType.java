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

package com.android.jack.test.dex;

import com.google.common.collect.Lists;

import com.android.dx.rop.code.AccessFlags;

import org.jf.dexlib.ClassDataItem;
import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.TypeListItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

/** Represents DEX type */
public class DexType {
  @Nonnull
  private final ClassDefItem item;
  @Nonnull
  private final List<DexMethod> methods = new ArrayList<>();
  @Nonnull
  private final List<DexField> fields = new ArrayList<>();
  @Nonnull
  private final List<String> interfaceNames = new ArrayList<>();

  public DexType(@Nonnull ClassDefItem item) {
    this.item = item;

    ClassDataItem classData = item.getClassData();
    if (classData != null) {
      addMethods(classData.getVirtualMethods());
      addMethods(classData.getDirectMethods());
      addFields(classData.getInstanceFields());
      addFields(classData.getStaticFields());

      // Get implemented interfaces
      TypeListItem interfaces = item.getInterfaces();
      if (interfaces != null) {
        interfaceNames.addAll(Lists.newArrayList(interfaces.getTypeListString(":").split(":")));
      }
    }
  }

  private void addMethods(@Nonnull List<ClassDataItem.EncodedMethod> virtualMethods) {
    for (ClassDataItem.EncodedMethod method : virtualMethods) {
      methods.add(new DexMethod(method));
    }
  }

  private void addFields(@Nonnull List<ClassDataItem.EncodedField> flds) {
    for (ClassDataItem.EncodedField f : flds) {
      fields.add(new DexField(f));
    }
  }

  @Nonnull
  public List<String> getInterfaceNames() {
    return Collections.unmodifiableList(interfaceNames);
  }

  @Nonnull
  public List<DexMethod> getMethods() {
    return Collections.unmodifiableList(methods);
  }

  @Nonnull
  public List<DexField> getFields() {
    return Collections.unmodifiableList(fields);
  }

  @Nonnull
  public String getName() {
    return item.getClassType().getTypeDescriptor();
  }

  public boolean isFinal() {
    return ((item.getAccessFlags() & AccessFlags.ACC_FINAL) != 0);
  }

  public boolean isPublic() {
    return ((item.getAccessFlags() & AccessFlags.ACC_PUBLIC) != 0);
  }

  @Nonnull
  public TypeIdItem getTypeId() {
    return item.getClassType();
  }
}
