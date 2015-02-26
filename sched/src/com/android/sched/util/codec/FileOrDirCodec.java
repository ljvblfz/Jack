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

import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Abstract class for a File or Directory {@link StringCodec}.
 */
public abstract class FileOrDirCodec<T> implements StringCodec<T> {
  @Nonnull
  protected ChangePermission change = ChangePermission.NOCHANGE;
  protected final int        permissions;
  @Nonnull
  protected Existence        existence;

  protected FileOrDirCodec(@Nonnull Existence existence, int permissions) {
    assert permissions != 0 : "At least one permission must be defined";

    this.permissions = permissions;
    this.existence   = existence;
  }

  protected void setChangePermission(@Nonnull ChangePermission change) {
    this.change = change;
  }

  /**
   * This class manages a StringBuilder with some management of separator.
   */
  protected static class StringBuilderAppender {
    private boolean needSeparator = false;
    @Nonnull
    private final StringBuilder sb = new StringBuilder();
    @Nonnull
    private final String separator;

    StringBuilderAppender(@Nonnull String separator) {
      this.separator = separator;
    }

    @Nonnull
    public StringBuilderAppender append(@Nonnull String string) {
      needSeparator = false;
      sb.append(string);
      return this;
    }

    @Nonnull
    public StringBuilderAppender append(boolean condition, @Nonnull String string) {
      if (condition) {
        if (needSeparator) {
          sb.append(separator);
        }
        needSeparator = true;
        sb.append(string);
      }

      return this;
    }

    @Override
    public String toString() {
      return sb.toString();
    }
  }

  public String getUsageDetails() {
    StringBuilderAppender sb = new StringBuilderAppender(", ");

    sb.append("must ");

    sb.append(existence == Existence.MUST_EXIST, "exist");
    sb.append(existence == Existence.NOT_EXIST,  "not exist");

    sb.append((permissions & Permission.READ)    != 0, "be readable");
    sb.append((permissions & Permission.WRITE)   != 0, "be writable");
    sb.append((permissions & Permission.EXECUTE) != 0, "be executable");

    return sb.toString();
  }

  @Override
  @Nonnull
  public List<ValueDescription> getValueDescriptions() {
    return Collections.<ValueDescription> emptyList();
  }
}
