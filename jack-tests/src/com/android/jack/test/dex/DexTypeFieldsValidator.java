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

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nonnull;

/** Type validator checking type's fields */
public class DexTypeFieldsValidator
    extends DexCollectionValidator<DexTypeFieldsValidator, DexType, DexValidator<DexField>> {

  @Override
  protected void validateImpl(@Nonnull DexType type) {
    TreeSet<String> notProcessedFields = new TreeSet<String>(validators.keySet());
    for (DexField field : type.getFields()) {
      String name = field.getId();
      DexValidator<DexField> validator = validators.get(name);
      if (validator != null) {
        notProcessedFields.remove(name);
        validator.validate(field);
      }
    }

    for (String field : notProcessedFields) {
      Assert.fail("Not processed field: " + field);
    }
  }
}
