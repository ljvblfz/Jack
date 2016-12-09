/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.sched.util;

import com.android.sched.util.file.CannotCloseException;
import com.android.sched.vfs.InputVFS;

import java.net.URLClassLoader;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * ClassLoader loading from a {@link InputVFS} managing close of the underlying {@link InputVFS}. It
 * provides a more predictable behavior than {@link URLClassLoader} around closing the opened files.
 * Either by explicit {@link #close()} when possible or by closing during finalization of this
 * {@link ClassLoader} while {@link URLClassLoader} let other instances finalization handle that,
 * possibly at a later time than the {@link ClassLoader} garbage collection.
 */
public class CloseableVFSClassLoader extends VFSClassLoader implements AutoCloseable {

  public CloseableVFSClassLoader(@Nonnull InputVFS vfs,
      @CheckForNull ClassLoader parentClassLoader) {
    super(vfs, parentClassLoader);
  }

  @Override
  public void close() throws CannotCloseException {
    vfs.close();
  }

  @Override
  protected void finalize() throws Throwable {
    close();
    super.finalize();
  }

}
