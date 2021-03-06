/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.shrob.obfuscation.annotation;

import com.android.sched.item.Description;
import com.android.sched.item.Feature;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.id.BooleanPropertyId;

import javax.annotation.Nonnull;

/**
 * Represents the removal of line numbers of local variables and 'this' in source info.
 */
@HasKeyId
@Description("The removal of line numbers of local variables and 'this' in source info")
public class RemoveLocalLineNumber implements Feature {

  /**
   * This property indicates if the line numbers of local variables and 'this' in source info must
   * be removed.
   * If a flags file (provided with --config-proguard) contradicts this property, the property is
   * overridden.
   */
  @Nonnull
  public static final BooleanPropertyId KEEP_LOCAL_LINE_NUMBER = BooleanPropertyId.create(
      "jack.obfuscation.local.keep-line-number", "Keep line numbers for locals")
      .addDefaultValue(Boolean.TRUE);
}
