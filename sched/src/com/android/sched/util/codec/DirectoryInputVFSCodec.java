/*
 * Copyright (C) 2015 The Android Open Source Project
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
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.vfs.GenericInputVFS;
import com.android.sched.vfs.InputVFS;
import com.android.sched.vfs.VFS;

import java.util.List;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to create an instance of {@link InputVFS}
 * to directories.
 */
public class DirectoryInputVFSCodec implements StringCodec<InputVFS> {

  @Nonnull
  private final Logger logger = LoggerFactory.getLogger();
  @CheckForNull
  private String infoString;
  @Nonnull
  private final DirectFSCodec directFSCodec = new DirectFSCodec();

  @Override
  @Nonnull
  public String getUsage() {
    return "a path to an input directory (" + directFSCodec.getDetailedUsage() + ")";
  }

  @Override
  @Nonnull
  public String getVariableName() {
    return directFSCodec.getVariableName();
  }

  @Nonnull
  public DirectoryInputVFSCodec withoutCache() {
    directFSCodec.withoutCache();

    return this;
  }

  @Override
  @Nonnull
  public InputVFS checkString(@Nonnull CodecContext context, @Nonnull String string)
      throws ParsingException {
    VFS vfs = directFSCodec.checkString(context, string);
    return new GenericInputVFS(vfs);
  }

  @Override
  @Nonnull
  public InputVFS parseString(@Nonnull CodecContext context, @Nonnull String string) {
    try {
      return checkString(context, string);
    } catch (ParsingException e) {
      throw new ConfigurationError(e);
    }
  }

  @Nonnull
  public DirectoryInputVFSCodec setInfoString(@Nonnull String infoString) {
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
  public String formatValue(@Nonnull InputVFS data) {
    return directFSCodec.formatValue(data.getVFS());
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull InputVFS data) {
    directFSCodec.checkValue(context, data.getVFS());
  }
}
