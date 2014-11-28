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

package com.android.jack.jayce;

import com.android.jack.jayce.v0002.io.JayceInternalWriterImpl;
import com.android.jack.library.FileType;
import com.android.jack.library.OutputJackLibrary;

import java.io.OutputStream;

import javax.annotation.Nonnull;

/**
 * {@link JayceInternalWriter} Factory.
 */
public abstract class JayceWriterFactory {

  public static final int DEFAULT_MAJOR_VERSION = 2;

  @Nonnull
  public static JayceInternalWriter get(@Nonnull OutputJackLibrary outputJackLibrary,
      @Nonnull OutputStream out) {
    JayceInternalWriterImpl jayceWriter = new JayceInternalWriterImpl(out);

    outputJackLibrary.putProperty(FileType.JAYCE.buildPropertyName(null /* suffix */),
        String.valueOf(true));
    outputJackLibrary.putProperty(JayceProperties.KEY_JAYCE_MAJOR_VERSION,
        String.valueOf(DEFAULT_MAJOR_VERSION));
    outputJackLibrary.putProperty(JayceProperties.KEY_JAYCE_MINOR_VERSION,
        String.valueOf(jayceWriter.getCurrentMinor()));

    return jayceWriter;
  }
}
