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

import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.StreamFile;

import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to help the creation of an instance inherited from
 * {@link StreamFile}.
 */
public abstract class StreamCodec extends FileOrDirCodec {
  @Nonnull
  protected static final String STANDARD_IO_NAME = "-";
  @Nonnull
  protected static final String STANDARD_ERROR_NAME = "--";

  protected boolean allowStandardIO = false;
  protected boolean allowStandardError = false;

  protected StreamCodec(@Nonnull Existence existence, int permissions) {
    super(existence, permissions);

    assert ((permissions & Permission.READ)  != 0) ||
           ((permissions & Permission.WRITE) != 0);
  }

  @Nonnull
  public String getUsage() {
    StringBuilder sb = new StringBuilder();

    sb.append("a path to a file (");
    sb.append(getUsageDetails());
    sb.append(")");

    if (allowStandardIO) {
      StringBuilderAppender sbSlash = new StringBuilderAppender("/");

      sbSlash.append(", can be '");
      sbSlash.append(STANDARD_IO_NAME);
      sbSlash.append("' for standard ");
      sbSlash.append((permissions & Permission.READ)  != 0, "input");
      sbSlash.append((permissions & Permission.WRITE) != 0, "output");

      sb.append(sbSlash.toString());
    }

    if (allowStandardError) {
      sb.append(", can be '");
      sb.append(STANDARD_ERROR_NAME);
      sb.append("' for standard error");
    }

    return sb.toString();
  }
}
