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

package com.android.jack.ir.naming;

import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.formatter.BinaryQualifiedNameFormatter;
import com.android.jack.ir.formatter.PackageFormatter;
import com.android.jack.ir.formatter.SourceFormatter;

import javax.annotation.Nonnull;

/**
 * An {@link AbstractName} referencing a package. This implementation is not thread-safe.
 * If multiple threads modify the referenced package, it must be synchronized externally.
 */
public class PackageName extends AbstractName {

  @Nonnull
  private static final PackageFormatter binaryQnFormatter =
      BinaryQualifiedNameFormatter.getFormatter();

  @Nonnull
  private static final PackageFormatter sourceQnFormatter = SourceFormatter.getFormatter();

  /**
   * kind of literal type representation.
   */
  public enum Kind {
    BINARY_QN,          // means java/lang
    SRC_QN,             // means java.lang
    SIMPLE_NAME         // means lang
  }

  @Nonnull
  private final JPackage pack;

  @Nonnull
  private final Kind kind;

  public PackageName(@Nonnull Kind kind, @Nonnull JPackage pack) {
    this.kind = kind;
    this.pack = pack;
  }

  @Nonnull
  private static String getValue(@Nonnull JPackage pack, @Nonnull Kind kind) {
    switch (kind) {
      case BINARY_QN:
        return binaryQnFormatter.getName(pack);
      case SRC_QN:
        return sourceQnFormatter.getName(pack);
      case SIMPLE_NAME:
        return pack.getName();
      default: {
        throw new AssertionError();
      }
    }
  }

  @Override
  @Nonnull
  public String toString() {
    return getValue(pack, kind);
  }

  @Nonnull
  public JPackage getReferencedPackage() {
    return pack;
  }
}
