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

package com.android.sched.util.codec;


import java.io.File;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to create an instance of {@link File}.
 */
public class PathCodec implements StringCodec<File> {
  @Override
  @Nonnull
  public String getUsage() {
    return "a path to a file or directory";
  }

  @Override
  @CheckForNull
  public File checkString(@Nonnull CodecContext context, @Nonnull String value) {
    return null;
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull File file) {
  }

  @Override
  @Nonnull
  public File parseString(@Nonnull CodecContext context, @Nonnull String value) {
    return new File(value);
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull File file) {
    return file.getPath();
  }
}
