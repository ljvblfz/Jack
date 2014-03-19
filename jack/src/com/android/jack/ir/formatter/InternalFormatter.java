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

package com.android.jack.ir.formatter;

import com.android.jack.ir.ast.JPackage;

import javax.annotation.Nonnull;


/**
 * Provides formatted Strings for an internal usage.
 */
public class InternalFormatter extends BinarySignatureFormatter
    implements TypePackageAndMethodFormatter {

  @Nonnull
  private static final InternalFormatter formatter = new InternalFormatter();

  protected InternalFormatter() {
  }

  @Nonnull
  public static InternalFormatter getFormatter() {
    return formatter;
  }

  @Override
  @Nonnull
  public String getName(@Nonnull JPackage pack) {
    return getNameInternal(pack).toString();
  }

}
