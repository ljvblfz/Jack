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

package com.android.jack.preprocessor;

import com.android.sched.item.Description;
import com.android.sched.item.Feature;
import com.android.sched.util.codec.InputStreamCodec;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.file.InputStreamFile;

import javax.annotation.Nonnull;

/**
 * A {@link Feature} that represents Preprocessor support.
 */
@Description("Preprocessor support")
@HasKeyId
public class PreProcessor implements Feature {

  @Nonnull
  public static final BooleanPropertyId ENABLE = BooleanPropertyId.create(
      "jack.preprocessor", "Enable the Jack preprocessor")
      .addDefaultValue(false);

  @Nonnull
  public static final PropertyId<InputStreamFile> FILE = PropertyId.create(
      "jack.preprocessor.file", "Preprocessor source file",
      new InputStreamCodec()).requiredIf(ENABLE.getValue().isTrue());

}
