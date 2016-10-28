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

package com.android.jack.ir.ast.cfg;

import com.android.jack.ir.JNodeInternalError;
import com.android.jack.ir.ast.JVisitor;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/** Class validates expressions inside the CFG basic block elements */
class CfgExpressionValidator extends JVisitor {
  @CheckForNull
  private JBasicBlockElement element = null;

  private CfgExpressionValidator() {
  }

  public static void validate(@Nonnull JBasicBlockElement element) {
    new CfgExpressionValidator().accept(element);
  }

  @Override
  public boolean visit(@Nonnull JBasicBlockElement element) {
    if (this.element != null) {
      throw new JNodeInternalError(element, "Nested block element");
    }
    this.element = element;
    return true;
  }
}
