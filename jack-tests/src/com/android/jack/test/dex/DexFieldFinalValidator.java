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

import javax.annotation.Nonnull;

/** Method validator checking field's methods */
public class DexFieldFinalValidator extends DexValidator<DexField> {
  private final boolean flag;

  public DexFieldFinalValidator(boolean flag) {
    this.flag = flag;
  }

  @Override
  protected void validateImpl(@Nonnull DexField field) {
    Assert.assertEquals(
        "Modifier 'final' of field " + field.getId(), flag, field.isFinal());
  }
}
