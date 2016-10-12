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

package com.android.jack.api.v04.impl;

import com.android.jack.api.v03.impl.Api03ConfigImpl;
import com.android.jack.api.v04.Api04Config;
import com.android.jack.api.v04.HasCharset;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * A configuration implementation for API level 04 of the Jack compiler.
 */
public class Api04ConfigImpl extends Api03ConfigImpl implements Api04Config {
  public Api04ConfigImpl() {
    super();
  }

  @Override
  public void setDefaultCharset(@Nonnull Charset charset) {
    options.setDefaultCharset(charset);
  }

  public static File manageFileCharset(@Nonnull File file) {
    if (file instanceof HasCharset) {
      return new File(file.getParent(),
          file.getName() + "[" + ((HasCharset) file).getCharset().name() + "]");
    } else {
      return file;
    }
  }

  public static List<File> manageFilesCharsetAsList(@Nonnull Collection<File> files) {
    List<File> list = new ArrayList<>(files.size());
    for (File file : files) {
      list.add(manageFileCharset(file));
    }

    return list;
  }
}
