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

/** Type validator checking type's methods */
public class DexTypeMethodsValidator
    extends DexCollectionValidator<DexTypeMethodsValidator, DexType, DexValidator<DexMethod>> {
  @Override
  protected void validateImpl(@Nonnull DexType type) {
    TreeSet<String> notProcessedMethods = new TreeSet<String>(validators.keySet());
    for (DexMethod method : type.getMethods()) {
      String name = method.getId();
      DexValidator<DexMethod> validator = validators.get(name);
      if (validator != null) {
        notProcessedMethods.remove(name);
        validator.validate(method);
      }
    }

    for (String method : notProcessedMethods) {
      Assert.fail("Not processed method: " + method);
    }
  }
}
