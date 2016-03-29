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

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to help the creation of an instance inherited from
 * {@link StreamFile}.
 */
public abstract class FileCodec<T> extends FileOrDirCodec<T> {
  @Nonnull
  protected static final String STANDARD_IO_NAME = "-";
  @Nonnull
  protected static final String STANDARD_ERROR_NAME = "--";

  protected boolean allowStandardIO = false;
  protected boolean allowStandardError = false;
  @CheckForNull
  protected CharsetCodec charsetCodec = null;
  @CheckForNull
  protected Charset defaultCharset = null;

  protected FileCodec(@Nonnull Existence existence, int permissions) {
    super(existence, permissions);

    assert ((permissions & Permission.READ)  != 0) ||
           ((permissions & Permission.WRITE) != 0);
  }

  @CheckForNull
  protected Charset parseCharset(@Nonnull CodecContext context, @Nonnull String string)
      throws ParsingException {
    Charset charset = null;

    if (charsetCodec != null) {
      String encoding;

      if (string.charAt(string.length() - 1) == ']') {
        int idx = string.lastIndexOf('[');
        if (idx == -1) {
          throw new ParsingException(
              "The value may be ended by '[<charset>]' but is '" + string + "'");
        }

        encoding = string.substring(idx + 1, string.length() - 1);
        try {
          charset = charsetCodec.checkString(context, encoding);
          if (charset == null) {
            charset = charsetCodec.parseString(context, encoding);
          }
        } catch (ParsingException e) {
          throw new ParsingException("The value must be " + getLongUsage()
              + " but <charset> is '" + encoding + "'");
        }
      }
    }

    return charset;
  }

  @Nonnull
  protected Charset getCharset(@Nonnull CodecContext context, @CheckForNull Charset localCharset) {
    if (localCharset != null) {
      return localCharset;
    } else if (defaultCharset != null) {
      return defaultCharset;
    } else {
      return context.getDefaultCharset();
    }
  }

  @Override
  @Nonnull
  public String getUsage() {
    StringBuilder sb = new StringBuilder();

    sb.append("a path to a file (");
    sb.append(getDetailedUsage());
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

    if (charsetCodec != null) {
      sb.append(", can be ended by [<charset>] where <charset> is ");
      sb.append(charsetCodec.getUsage());
    }

    return sb.toString();
  }

  @Override
  @Nonnull
  public String getVariableName() {
    return "file";
  }

  @Nonnull
  protected String getLongUsage() {
    StringBuilder sb = new StringBuilder();

    sb.append("a path to a file");
    if (charsetCodec != null) {
      sb.append(", can be ended by [<charset>] where <charset> is ");
      sb.append(charsetCodec.getDetailedUsage());
    }

    return sb.toString();
  }

  @Override
  @Nonnull
  public List<ValueDescription> getValueDescriptions() {
    return Collections.<ValueDescription> emptyList();
  }
}
