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
import com.android.sched.vfs.DirectFS;
import com.android.sched.vfs.GenericInputOutputVFS;
import com.android.sched.vfs.InputOutputVDir;
import com.android.sched.vfs.InputOutputVFS;

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to create an instance of {@link InputOutputVDir} backed by a
 * filesystem directory.
 */
public class DirectDirInputOutputVDirCodec extends InputOutputVDirCodec
    implements StringCodec<InputOutputVFS> {

  public DirectDirInputOutputVDirCodec(@Nonnull Existence existence) {
    super(existence);
  }

  @Override
  @Nonnull
  public String getUsage() {
    return "a path to a directory (" + getUsageDetails() + ")";
  }

  @Override
  @Nonnull
  public InputOutputVFS checkString(@Nonnull CodecContext context,
      @Nonnull final String string) throws ParsingException {
    try {
      return new GenericInputOutputVFS(new DirectFS(new Directory(string,
          context.getRunnableHooks(), existence, permissions, change), permissions));
    } catch (IOException e) {
      throw new ParsingException(e);
    }
  }
}
