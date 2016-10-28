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

package com.android.jack.ir.ast;

import com.android.jack.ir.JNodeInternalError;
import com.android.jack.ir.ast.cfg.JReturnBlockElement;
import com.android.jack.ir.sourceinfo.SourceInfo;

/**
 * Base class for numeric Java literal expression.
 */
public abstract class JNumberValueLiteral extends JValueLiteral {

  public JNumberValueLiteral(SourceInfo sourceInfo) {
    super(sourceInfo);
  }

  @Override
  public void checkValidity() {
    if (!(parent instanceof JExpression
        || parent instanceof JNameValuePair
        || parent instanceof JAnnotationMethod
        || parent instanceof JReturnStatement
        || parent instanceof JReturnBlockElement
        || parent instanceof JFieldInitializer)) {
      throw new JNodeInternalError(this, "Invalid parent");
    }
  }
}
