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
import com.android.sched.util.config.ConfigurationError;
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.InputZipFile;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.vfs.DirectVFS;
import com.android.sched.vfs.InputVDir;
import com.android.sched.vfs.InputVFS;
import com.android.sched.vfs.InputZipVFS;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to create an instance of {@link InputVDir}.
 */
public class InputVDirCodec extends FileOrDirCodec
    implements StringCodec<InputVFS> {

  @Nonnull
  private final Logger logger = LoggerFactory.getLogger();

  public InputVDirCodec() {
    super(Existence.MUST_EXIST, Permission.READ);
  }

  @Nonnull
  public InputVDirCodec changeOwnerPermission() {
    setChangePermission(ChangePermission.OWNER);

    return this;
  }

  @Nonnull
  public InputVDirCodec changeAllPermission() {
    setChangePermission(ChangePermission.EVERYBODY);

    return this;
  }

  @Override
  @Nonnull
  public String getUsage() {
    StringBuilderAppender sb = new StringBuilderAppender(", ");

    sb.append("a path to an input directory or zip archive");
    sb.append(" (must ");

    sb.append(existence == Existence.MUST_EXIST, "exist");
    sb.append(existence == Existence.NOT_EXIST,  "not exist");

    sb.append((permissions & Permission.READ)     != 0, "be readable");
    sb.append((permissions & Permission.WRITE)    != 0, "be writable");

    sb.append(")");

    return sb.toString();
  }

  @Override
  @Nonnull
  public InputVFS checkString(@Nonnull CodecContext context, @Nonnull final String string)
      throws ParsingException {
    final InputVFS vfs;
    try {
      File dirOrZip = new File(string);
      if (dirOrZip.isDirectory()) {
        vfs = new DirectVFS(new Directory(string, context.getRunnableHooks(),
            Existence.MUST_EXIST, Permission.READ, change));
      } else {
        RunnableHooks hooks = context.getRunnableHooks();
        assert hooks != null;
        vfs = new InputZipVFS(new InputZipFile(string, hooks, Existence.MUST_EXIST, change));
        hooks.addHook(new Runnable() {
          @Override
          public void run() {
            try {
              vfs.close();
            } catch (IOException e) {
              logger.log(Level.FINE, "Failed to close zip for '" + string + "'.", e);
            }
          }
        });
      }

      return vfs;
    } catch (IOException e) {
      throw new ParsingException(e.getMessage(), e);
    }
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull InputVFS dir) {
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

  @Override
  @Nonnull
  public String formatValue(@Nonnull InputVFS directory) {
    return directory.getLocation().getDescription();
  }
}
