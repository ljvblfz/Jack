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

package com.android.jack.digest;

import com.android.sched.item.Description;
import com.android.sched.item.Feature;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.config.id.MessageDigestPropertyId;

import javax.annotation.Nonnull;

/**
 * Feature required to compute the origin digest
 */
@Description("Digest on the origin of the type")
@HasKeyId
public class OriginDigestFeature implements Feature {
  @Nonnull
  public static final BooleanPropertyId ORIGIN_DIGEST = BooleanPropertyId
      .create("jack.source.digest", "Generate digest for source identification")
      .addDefaultValue(true);

  @Nonnull
  public static final MessageDigestPropertyId ORIGIN_DIGEST_ALGO = MessageDigestPropertyId
      .create("jack.source.digest.algo", "Digest algorithm use for source identification")
      .requiredIf(OriginDigestFeature.ORIGIN_DIGEST.getValue().isTrue()).addDefaultValue("SHA");
}
