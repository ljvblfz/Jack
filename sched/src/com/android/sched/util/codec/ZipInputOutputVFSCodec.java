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


import com.android.sched.util.RunnableHooks;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.OutputZipFile;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.vfs.GenericInputOutputVFS;
import com.android.sched.vfs.InputOutputVFS;
import com.android.sched.vfs.ReadWriteZipFS;

import java.io.IOException;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to create an instance of {@link InputOutputVFS} backed by a
 * filesystem directory, which is then zipped when closed.
 */
public class ZipInputOutputVFSCodec extends InputOutputVFSCodec
    implements StringCodec<InputOutputVFS> {

  @Nonnull
  private final Logger logger = LoggerFactory.getLogger();

  public ZipInputOutputVFSCodec(@Nonnull Existence existence) {
    super(existence);
  }

  @Override
  @Nonnull
  public String getUsage() {
    return "a path to a zip archive (" + getUsageDetails() + ")";
  }

  @Override
  @Nonnull
  public InputOutputVFS checkString(@Nonnull CodecContext context,
      @Nonnull final String string) throws ParsingException {
    RunnableHooks hooks = context.getRunnableHooks();
    try {
      final ReadWriteZipFS vfs = new ReadWriteZipFS(
          new OutputZipFile(context.getWorkingDirectory(), string, hooks, existence, change));
      return new GenericInputOutputVFS(vfs);
    } catch (IOException e) {
      throw new ParsingException(e.getMessage(), e);
    }
  }
}
