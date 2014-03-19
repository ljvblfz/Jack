/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.transformations.threeaddresscode;

import com.android.sched.item.Description;
import com.android.sched.item.Tag;

/**
 * The tag {@code ThreeAddressCodeForm} allows to know that
 * the code has been transformed in three address form.
 */
@Description("Code in three address form.")
public final class ThreeAddressCodeForm implements Tag {
  // TODO(delphinemartin) It seems not to be a good solution,
  // discussion about sched lib is needed.
  /**
   * The tag {@code ThreeAddressCodeForm.checked} allows to know that
   * the code has been verified and is in three address form
   */
  @Description("Three address form is checked.")
  public static final class Checked implements Tag {
  }
}