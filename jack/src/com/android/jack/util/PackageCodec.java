/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.util;

import com.android.sched.util.codec.CheckingException;
import com.android.sched.util.codec.CodecContext;
import com.android.sched.util.codec.ParsingException;
import com.android.sched.util.codec.StringCodec;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;


/**
 * This {@link StringCodec} is used to check that the string is a valid package name
 */
public class PackageCodec implements StringCodec<String>{

  @Override
  @Nonnull
  public String getUsage() {
    return "a java package name (e.g. com.android)";
  }

  @Override
  @Nonnull
  public String parseString(@Nonnull CodecContext context, @Nonnull String string) {
    return string;
  }

  @Override
  @CheckForNull
  public String checkString(@Nonnull CodecContext context, @Nonnull String string)
      throws ParsingException {
    if (!NamingTools.isPackageSourceName(string)) {
      throw new ParsingException(
          "The value must be " + getUsage() + " but is '" + string + "'");
    }
    return string;
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull String data)
      throws CheckingException {
    if (!NamingTools.isPackageSourceName(data)) {
      throw new CheckingException(
          "The value must be " + getUsage() + " but is '" + data + "'");
    }
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull String name) {
    return name;
  }
}
