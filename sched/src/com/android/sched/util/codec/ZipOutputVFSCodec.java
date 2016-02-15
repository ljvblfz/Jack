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
import com.android.sched.util.file.OutputZipFile.Compression;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.vfs.GenericOutputVFS;
import com.android.sched.vfs.OutputVFS;
import com.android.sched.vfs.WriteZipFS;

import java.io.IOException;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to create an instance of {@link OutputVFS} backed by a zip
 * archive.
 */
public class ZipOutputVFSCodec extends OutputVFSCodec {

  @Nonnull
  private final Logger logger = LoggerFactory.getLogger();

  public ZipOutputVFSCodec(@Nonnull Existence existence) {
    super(existence);
  }

  @Override
  @Nonnull
  public String getUsage() {
    return "a path to a zip archive (" + getUsageDetails() + ")";
  }

  @Override
  @Nonnull
  public String getVariableName() {
    return "zip";
  }

  @Override
  @Nonnull
  public OutputVFS checkString(@Nonnull CodecContext context,
      @Nonnull final String string) throws ParsingException {
    RunnableHooks hooks = context.getRunnableHooks();
    try {
      WriteZipFS vfs = new WriteZipFS(new OutputZipFile(context.getWorkingDirectory(), string,
          hooks, existence, change, Compression.COMPRESSED));
      return new GenericOutputVFS(vfs);
    } catch (IOException e) {
      throw new ParsingException(e.getMessage(), e);
    }
  }
}
