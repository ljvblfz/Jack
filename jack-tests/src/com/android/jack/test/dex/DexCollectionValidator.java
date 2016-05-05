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

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

/** Dex validators collection */
public abstract class DexCollectionValidator<U extends DexCollectionValidator<U, T, V>, T, V>
    extends DexValidator<T> {
  @Nonnull
  protected final Map<String, V> validators = new HashMap<String, V>();

  @Nonnull
  @SuppressWarnings("unchecked")
  public U insert(@Nonnull String id, @Nonnull V validator) {
    Assert.assertFalse(
        "insert(...) must be used to add validators for new element, "
            + "use insert(...) for updating validators for existing element",
        validators.containsKey(id));
    validators.put(id, validator);
    return (U) this;
  }

  @Nonnull
  @SuppressWarnings("unchecked")
  public U update(@Nonnull String id, @Nonnull V validator) {
    Assert.assertTrue(
        "update(...) must be used to update validators for a element, "
            + "use insert(...) for adding validators for new element",
        validators.containsKey(id));
    validators.put(id, validator);
    return (U) this;
  }
}
