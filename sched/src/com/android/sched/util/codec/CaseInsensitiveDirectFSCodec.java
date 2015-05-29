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
import com.android.sched.util.config.MessageDigestFactory;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.vfs.CaseInsensitiveFS;
import com.android.sched.vfs.DirectFS;
import com.android.sched.vfs.VFS;
import com.android.sched.vfs.WrongVFSFormatException;

import java.security.Provider.Service;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to create an instance of a {@link CaseInsensitiveFS} backed by a
 * {@link DirectFS}.
 */
public class CaseInsensitiveDirectFSCodec implements StringCodec<VFS> {

  @Nonnull
  private final DirectFSCodec codec;
  @Nonnull
  private final MessageDigestCodec messageDigestCodec = new MessageDigestCodec();

  public CaseInsensitiveDirectFSCodec() {
    codec = new DirectFSCodec();
  }

  public CaseInsensitiveDirectFSCodec(@Nonnull Existence mustExist) {
    codec = new DirectFSCodec(mustExist);
  }

  @Override
  @Nonnull
  public VFS parseString(@Nonnull CodecContext context, @Nonnull String string) {
    try {
      return checkString(context, string);
    } catch (ParsingException e) {
      throw new ConfigurationError(e);
    }
  }

  @Override
  @CheckForNull
  public VFS checkString(@Nonnull CodecContext context, @Nonnull String string)
      throws ParsingException {
    try {
      Service service = messageDigestCodec.checkString(context, "SHA");
      return new CaseInsensitiveFS(codec.checkString(context, string), /* nbGroup = */ 1,
          /* szGroup = */ 2, new MessageDigestFactory(service), /* debug = */ false);
    } catch (WrongVFSFormatException e) {
      throw new ParsingException(e);
    }
  }

  @Override
  @Nonnull
  public String getUsage() {
    return codec.getUsage();
  }

  @Override
  @Nonnull
  public String getVariableName() {
    return codec.getVariableName();
  }

  @Override
  @Nonnull
  public List<ValueDescription> getValueDescriptions() {
    return codec.getValueDescriptions();
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull VFS data) {
    return codec.formatValue(data);
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull VFS data) {
    codec.checkValue(context, data);
  }
}
