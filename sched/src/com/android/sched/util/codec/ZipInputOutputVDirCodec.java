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
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.OutputZipFile;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.vfs.InputOutputVDir;
import com.android.sched.vfs.InputOutputZipRootVDir;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to create an instance of {@link InputOutputVDir} backed by a
 * filesystem directory, which is then zipped when closed.
 */
public class ZipInputOutputVDirCodec extends InputOutputVDirCodec
    implements StringCodec<InputOutputVDir> {

  @Nonnull
  private final Logger logger = LoggerFactory.getLogger();

  public ZipInputOutputVDirCodec(@Nonnull Existence existence) {
    super(existence);
  }

  @Override
  @Nonnull
  public String getUsage() {
    StringBuilderAppender sb = new StringBuilderAppender(", ");

    sb.append("a path to a zip archive (must ");

    sb.append(existence == Existence.MUST_EXIST, "exist");
    sb.append(existence == Existence.NOT_EXIST,  "not exist");

    sb.append((permissions & Permission.READ)     != 0, "be readable");
    sb.append((permissions & Permission.WRITE)    != 0, "be writable");

    sb.append(")");

    return sb.toString();
  }

  @Override
  @Nonnull
  public InputOutputVDir checkString(@Nonnull CodecContext context, @Nonnull final String string)
      throws ParsingException {
    RunnableHooks hooks = context.getRunnableHooks();
    try {
      final InputOutputZipRootVDir vDir =
          new InputOutputZipRootVDir(new OutputZipFile(string, hooks, existence, change));
      assert hooks != null;
      hooks.addHook(new Runnable() {
        @Override
        public void run() {
          try {
            vDir.close();
          } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to create zip for '" + string + "'.", e);
          }
        }
      });
      return vDir;
    } catch (IOException e) {
      throw new ParsingException(e.getMessage(), e);
    }
  }
}
