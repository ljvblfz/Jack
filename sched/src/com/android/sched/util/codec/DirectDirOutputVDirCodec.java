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

package com.android.sched.util.codec;

import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.vfs.DirectVFS;
import com.android.sched.vfs.OutputVFS;

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to create an instance of {@link OutputVFS} backed by a
 * filesystem directory.
 */
public class DirectDirOutputVDirCodec extends OutputVDirCodec {
  public DirectDirOutputVDirCodec(@Nonnull Existence existence) {
    super(existence);
  }

  @Override
  @Nonnull
  public String getUsage() {
    return "a path to an output directory (" + getUsageDetails() + ")";
  }

  @Override
  @Nonnull
  public OutputVFS checkString(@Nonnull CodecContext context,
      @Nonnull final String string) throws ParsingException {
    try {
      return new DirectVFS(
          new Directory(string, context.getRunnableHooks(), existence, permissions, change));
    } catch (IOException e) {
      throw new ParsingException(e.getMessage(), e);
    }
  }

}
