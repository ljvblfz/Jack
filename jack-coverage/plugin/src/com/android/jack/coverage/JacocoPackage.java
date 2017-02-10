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

package com.android.jack.coverage;

import com.android.jack.util.PackageCodec;
import com.android.sched.util.codec.CheckingException;
import com.android.sched.util.codec.CodecContext;
import com.android.sched.util.codec.ParsingException;
import com.android.sched.util.codec.StringCodec;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Represents the JaCoCo package name containing the class necessary for
 * instrumentation.
 */
class JacocoPackage {
  @Nonnull
  private final String packageName;

  public JacocoPackage(@Nonnull String packageName) {
    this.packageName = packageName;
  }

  @Nonnull
  public String getPackageName() {
    return packageName;
  }

  /**
   * A {@link StringCodec} for {@link JacocoPackage}.
   */
  public static final class Codec implements StringCodec<JacocoPackage> {
    @Nonnull
    private final PackageCodec parser = new PackageCodec();

    @Override
    @Nonnull
    public JacocoPackage parseString(@Nonnull CodecContext context, @Nonnull String string) {
      return new JacocoPackage(parser.parseString(context, string));
    }

    @Override
    @CheckForNull
    public JacocoPackage checkString(@Nonnull CodecContext context, @Nonnull String string)
        throws ParsingException {
      return new JacocoPackage(parser.checkString(context, string));
    }

    @Override
    @Nonnull
    public String getUsage() {
      return parser.getUsage();
    }

    @Override
    @Nonnull
    public List<com.android.sched.util.codec.Parser.ValueDescription> getValueDescriptions() {
      return parser.getValueDescriptions();
    }

    @Override
    @Nonnull
    public String getVariableName() {
      return parser.getVariableName();
    }

    @Override
    @Nonnull
    public String formatValue(@Nonnull JacocoPackage data) {
      return parser.formatValue(data.getPackageName());
    }

    @Override
    public void checkValue(@Nonnull CodecContext context, @Nonnull JacocoPackage data)
        throws CheckingException {
      parser.checkValue(context, data.getPackageName());
    }
  }
}
