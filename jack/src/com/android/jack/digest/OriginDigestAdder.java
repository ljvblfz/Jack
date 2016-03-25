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

import com.android.jack.Jack;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.impl.SourceGenerationVisitor;
import com.android.jack.scheduling.filter.SourceTypeFilter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.MessageDigestFactory;
import com.android.sched.util.config.ThreadConfig;

import java.security.MessageDigest;
import java.util.EnumSet;

import javax.annotation.Nonnull;

/**
 * Schedulable which adds the {@link OriginDigestMarker}
 */
@Description("Add digest on the origin of the type")
@Transform(add = OriginDigestMarker.class)
@Filter(SourceTypeFilter.class)
public class OriginDigestAdder implements RunnableSchedulable<JDefinedClassOrInterface> {
  @Nonnull
  public MessageDigestFactory digestFactory =
      ThreadConfig.get(OriginDigestFeature.ORIGIN_DIGEST_ALGO);

  @Nonnull
  private final String emitter = Jack.getEmitterId();
  private final int    major   = Jack.getVersion().getReleaseCode();
  private final int    minor   = Jack.getVersion().getSubReleaseCode();
  @Nonnull
  private final EnumSet<OriginDigestElement> descriptor =
      EnumSet.of(OriginDigestElement.SOURCE, OriginDigestElement.LOCAL_NAME,
          OriginDigestElement.PRIVATE_NAME, OriginDigestElement.PACKAGE_NAME,
          OriginDigestElement.PROTECTED_NAME, OriginDigestElement.PUBLIC_NAME);

  @Override
  public void run(@Nonnull JDefinedClassOrInterface type) throws Exception {
    MessageDigest digest = digestFactory.create();
    new SourceGenerationVisitor(new DigestOutput(digest)).accept(type);
    OriginDigestMarker marker = new OriginDigestMarker(descriptor,
        digest.getAlgorithm(), digest.digest(), emitter, major, minor);
    type.addMarker(marker);
  }
}
