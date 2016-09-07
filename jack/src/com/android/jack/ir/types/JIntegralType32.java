/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.ir.types;

import com.android.jack.ir.ast.JNumberValueLiteral;
import com.android.jack.ir.sourceinfo.SourceInfo;

import javax.annotation.Nonnull;

/**
 * Interface representing integral type smaller or equals to 32-bits.
 */
public interface JIntegralType32 extends JIntegralType {

  /**
   * Check if integer value could be represented by IntegralType32.
   * @param value Value to check.
   * @return true if value is inside range of IntegralType32.
   */
  public boolean isValidValue(int value);

  /** Creates a literal representing the value */
  @Nonnull
  JNumberValueLiteral createLiteral(@Nonnull SourceInfo sourceInfo, int value);
}
