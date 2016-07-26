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

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.impl.SourceGenerationVisitor;
import com.android.sched.util.config.MessageDigestFactory;
import com.android.sched.util.config.ThreadConfig;

import java.security.MessageDigest;

import javax.annotation.Nonnull;

/**
 * Base class to compute the {@link MessageDigest} of a type from its IR.
 */
public abstract class SourceDigestAdder {

  @Nonnull
  private final MessageDigestFactory digestFactory =
      ThreadConfig.get(OriginDigestFeature.ORIGIN_DIGEST_ALGO);

  @Nonnull
  protected final MessageDigest computeSourceDigest(@Nonnull JDefinedClassOrInterface type) {
    MessageDigest digest = digestFactory.create();
    new SourceGenerationVisitor(new DigestOutput(digest)).accept(type);
    return digest;
  }
}

