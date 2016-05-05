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

import junit.framework.Assert;

import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.DexFile;

import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;

/** Collection of type validators */
public final class DexFileTypesValidator
    extends DexCollectionValidator<DexFileTypesValidator, DexFile, DexValidator<DexType>> {

  @Override
  protected void validateImpl(@Nonnull DexFile dexFile) {
    TreeMap<String, DexType> name2type = new TreeMap<String, DexType>();
    for (ClassDefItem item : dexFile.ClassDefsSection.getItems()) {
      DexType clazz = new DexType(item);
      name2type.put(clazz.getTypeId().getTypeDescriptor(), clazz);
    }

    for (Map.Entry<String, DexValidator<DexType>> e : validators.entrySet()) {
      String typeName = e.getKey();
      DexType dexType = name2type.get(typeName);
      Assert.assertNotNull(
          "The type " + typeName + " does not exist",
          dexType);
      e.getValue().validate(dexType);
    }
  }
}
