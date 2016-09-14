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

package com.android.sched.util.codec;

import com.android.sched.util.file.FileOrDirectory;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;

import javax.annotation.Nonnull;

/**
 * This {@link ReaderFileOrDirectoryCodec} is used to create an instance of {@link FileOrDirectory}.
 * The file or directory must exist and be readable.
 */
public class ReaderFileOrDirectoryCodec extends OrCodec<FileOrDirectory> {
  @SuppressWarnings("unchecked")
  public ReaderFileOrDirectoryCodec() {
    super(new ReaderFileCodec().allowCharset(),
        new DirectoryCodec(Existence.MUST_EXIST, Permission.READ));
  }

  @Override
  @Nonnull
  public String formatValue(FileOrDirectory data) {
    return data.getPath();
  }
}