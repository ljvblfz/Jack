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

package com.android.jack.util.filter;

import com.android.sched.util.codec.CodecContext;
import com.android.sched.util.codec.StringCodec;

import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;


/**
 * This {@link SignatureCodec} is used to check that the string is a valid signature
 */
public class SignatureCodec implements StringCodec<String>{

  @Override
  @Nonnull
  public String getUsage() {
    return "a method signature (for instance \"methodname(ILpackage1/package2/Classname;)B\")";
  }

  @Override
  @Nonnull
  public String getVariableName() {
    return "signature";
  }

  @Override
  @Nonnull
  public List<ValueDescription> getValueDescriptions() {
    return Collections.<ValueDescription> emptyList();
  }
  @Override
  @Nonnull
  public String parseString(@Nonnull CodecContext context, @Nonnull String string) {
    return string;
  }

  @Override
  @CheckForNull
  public String checkString(@Nonnull CodecContext context, @Nonnull String string) {
    return string;
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull String data) {
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull String name) {
    return name;
  }
}
