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

import com.android.sched.util.config.ConfigurationError;
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.StreamFile;

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to create an instance of {@link StreamFile}.
 */
public class StreamCodec extends FileOrDirCodec
    implements StringCodec<StreamFile> {
  private static final String STANDARD_IO_NAME = "-";

  private boolean allowStandard = false;
  private boolean append        = false;

  public StreamCodec(@Nonnull Existence existence, int permissions) {
    super(existence, permissions);

    assert ((permissions & Permission.READ)  != 0) ||
           ((permissions & Permission.WRITE) != 0);
  }

  @Nonnull
  public StreamCodec changeOwnerPermission() {
    setChangePermission(ChangePermission.OWNER);

    return this;
  }

  @Nonnull
  public StreamCodec changeAllPermission() {
    setChangePermission(ChangePermission.EVERYBODY);

    return this;
  }

  @Nonnull
  public StreamCodec allowStandard() {
    this.allowStandard = true;

    return this;
  }

  @Nonnull
  public StreamCodec makeAppendable() {
    this.append = true;

    return this;
  }

  @Override
  @Nonnull
  public String getUsage() {
    StringBuilderAppender sb = new StringBuilderAppender(", ");

    sb.append("a path to a file");

    sb.append(" (must ");
    sb.append(existence == Existence.MUST_EXIST, "exist");
    sb.append(existence == Existence.NOT_EXIST,  "not exist");
    sb.append((permissions & Permission.READ)    != 0, "be readable");
    sb.append((permissions & Permission.WRITE)   != 0, "be writable");
    sb.append((permissions & Permission.EXECUTE) != 0, "be executable");

    if (allowStandard) {
      StringBuilderAppender ssb = new StringBuilderAppender("/");

      ssb.append("can be '");
      ssb.append(STANDARD_IO_NAME);
      ssb.append("' for standard ");
      ssb.append((permissions & Permission.READ)  != 0, "input");
      ssb.append((permissions & Permission.WRITE) != 0, "output");

      sb.append(true, ssb.toString());
    }

    sb.append(")");

    return sb.toString();
  }

  @Override
  @Nonnull
  public StreamFile checkString(@Nonnull CodecContext context, @Nonnull String value)
      throws ParsingException {
    StreamFile streamFile;

    if (value.equals(STANDARD_IO_NAME)) {
      if (!allowStandard) {
        StringBuilderAppender sb = new StringBuilderAppender("/");

        sb.append("Standard ");
        sb.append((permissions & Permission.READ)  != 0, "input");
        sb.append((permissions & Permission.WRITE) != 0, "output");
        sb.append(" can not be used");

        throw new ParsingException(sb.toString());
      }

      streamFile = new StreamFile();
    } else {
      try {
        streamFile = new StreamFile(value,
            context.getRunnableHooks(),
            existence,
            permissions,
            change,
            append);
      } catch (IOException e) {
        throw new ParsingException(e.getMessage(), e);
      }
    }

    return streamFile;
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull StreamFile stream) {
  }

  @Override
  @Nonnull
  public StreamFile parseString(@Nonnull CodecContext context, @Nonnull String string) {
    try {
      return checkString(context, string);
    } catch (ParsingException e) {
      throw new ConfigurationError(e);
    }
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull StreamFile stream) {
    if (stream.isStandard()) {
      return STANDARD_IO_NAME;
    } else {
      return stream.getName();
    }
  }
}
