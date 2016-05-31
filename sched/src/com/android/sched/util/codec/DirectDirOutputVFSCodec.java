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

import com.android.sched.util.config.ConfigurationError;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.vfs.GenericOutputVFS;
import com.android.sched.vfs.OutputVFS;
import com.android.sched.vfs.VFS;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to create an instance of {@link OutputVFS} backed by a
 * filesystem directory.
 */
public class DirectDirOutputVFSCodec implements StringCodec<OutputVFS> {

  private final DirectFSCodec directFSCodec;

  public DirectDirOutputVFSCodec(@Nonnull Existence existence) {
    directFSCodec = new DirectFSCodec(existence).withoutCache();
  }

  @Override
  @Nonnull
  public String getUsage() {
    return "a path to an output directory (" + directFSCodec.getDetailedUsage() + ")";
  }

  @Override
  @Nonnull
  public String getVariableName() {
    return directFSCodec.getVariableName();
  }

  @Override
  @Nonnull
  public OutputVFS checkString(@Nonnull CodecContext context,
      @Nonnull String string) throws ParsingException {
    VFS vfs = directFSCodec.checkString(context, string);
    return new GenericOutputVFS(vfs);
  }

  @Override
  @Nonnull
  public OutputVFS parseString(@Nonnull CodecContext context, @Nonnull String string) {
    try {
      return checkString(context, string);
    } catch (ParsingException e) {
      throw new ConfigurationError(e);
    }
  }

  @Nonnull
  public DirectDirOutputVFSCodec setInfoString(@CheckForNull String infoString) {
    directFSCodec.setInfoString(infoString);
    return this;
  }

  @Override
  @Nonnull
  public List<ValueDescription> getValueDescriptions() {
    return directFSCodec.getValueDescriptions();
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull OutputVFS data) {
    return directFSCodec.formatValue(data.getVFS());
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull OutputVFS data) {
    directFSCodec.checkValue(context, data.getVFS());
  }

}
