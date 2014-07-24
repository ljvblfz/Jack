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
import com.android.sched.util.file.OutputZipFile;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.vfs.Container;
import com.android.sched.vfs.DirectDir;
import com.android.sched.vfs.OutputVDir;
import com.android.sched.vfs.OutputZipRootVDir;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to create an instance of {@link OutputVDir}.
 */
public class OutputVDirCodec extends FileOrDirCodec
    implements StringCodec<OutputVDir> {

  @Nonnull
  private final Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private final Container type;

  public OutputVDirCodec(@Nonnull Existence existence, @Nonnull Container type) {
    super(existence, Permission.READ | Permission.WRITE);

    this.type = type;
  }

  @Nonnull
  public OutputVDirCodec changeOwnerPermission() {
    setChangePermission(ChangePermission.OWNER);

    return this;
  }

  @Nonnull
  public OutputVDirCodec changeAllPermission() {
    setChangePermission(ChangePermission.EVERYBODY);

    return this;
  }

  @Override
  @Nonnull
  public String getUsage() {
    StringBuilderAppender sb = new StringBuilderAppender(", ");

    String containerName;
    switch (type) {
      case DIR:
        containerName = "an output directory";
        break;
      case ZIP:
        containerName = "a zip archive";
        break;
      case FILE:
        throw new AssertionError();
      default:
        throw new AssertionError();
    }

    sb.append("a path to " + containerName + " (must ");

    sb.append(existence == Existence.MUST_EXIST, "exist");
    sb.append(existence == Existence.NOT_EXIST,  "not exist");

    sb.append((permissions & Permission.READ)     != 0, "be readable");
    sb.append((permissions & Permission.WRITE)    != 0, "be writable");

    sb.append(")");

    return sb.toString();
  }

  @Override
  @Nonnull
  public OutputVDir checkString(@Nonnull CodecContext context, @Nonnull final String string)
      throws ParsingException {
    OutputVDir dir = null;
    RunnableHooks hooks = context.getRunnableHooks();
    try {
      if (type == Container.DIR) {
        dir = new DirectDir(new Directory(string, hooks, existence, permissions, change));
      } else if (type == Container.ZIP) {
        final OutputZipRootVDir vDir =
            new OutputZipRootVDir(new OutputZipFile(string, hooks, existence, change));
        assert hooks != null;
        hooks.addHook(new Runnable() {
          @Override
          public void run() {
            try {
              vDir.close();
            } catch (IOException e) {
              logger.log(Level.WARNING, "Failed to close zip for '" + string + "'.", e);
            }
          }
        });
        dir = vDir;
      }
      assert dir != null;
      return dir;
    } catch (IOException e) {
      throw new ParsingException(e.getMessage(), e);
    }
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull OutputVDir dir) {
  }

  @Override
  @Nonnull
  public OutputVDir parseString(@Nonnull CodecContext context, @Nonnull String string) {
    try {
      return checkString(context, string);
    } catch (ParsingException e) {
      throw new ConfigurationError(e);
    }
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull OutputVDir directory) {
    return directory.getLocation().getDescription();
  }
}
