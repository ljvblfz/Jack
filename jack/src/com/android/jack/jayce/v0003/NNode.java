/*
* Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.jayce.v0003;

import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.formatter.InternalFormatter;
import com.android.jack.ir.formatter.TypeAndMethodFormatter;
import com.android.jack.jayce.Node;
import com.android.jack.jayce.v0003.io.ExportSession;
import com.android.jack.jayce.v0003.io.ImportHelper;
import com.android.jack.jayce.v0003.io.JayceInternalReaderImpl;
import com.android.jack.jayce.v0003.io.JayceInternalWriterImpl;
import com.android.jack.jayce.v0003.io.Token;
import com.android.jack.lookup.JMethodLookupException;

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * NNode.
 */
public abstract class NNode implements Node {

  @Nonnull
  private static final TypeAndMethodFormatter formatter = InternalFormatter.getFormatter();

  public abstract void importFromJast(@Nonnull ImportHelper loader, @Nonnull Object node);

  @Nonnull
  public abstract Object exportAsJast(@Nonnull ExportSession exportSession)
      throws JTypeLookupException, JMethodLookupException;

  public abstract void writeContent(@Nonnull JayceInternalWriterImpl out) throws IOException;

  public abstract void readContent(@Nonnull JayceInternalReaderImpl in) throws IOException;

  @Nonnull
  public abstract Token getToken();

  @Nonnull
  public static TypeAndMethodFormatter getFormatter() {
    return formatter;
  }

  public void setIndex(@SuppressWarnings("unused") int index) {
  }
}
