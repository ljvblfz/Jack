/*
 * Copyright 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.android.jack.ir.ast;

import com.android.jack.ir.JNodeInternalError;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Description;

/**
 * Base class for any Java literal expression.
 */
@Description("Java literal expression")
public abstract class JLiteral extends JExpression {

  public JLiteral(SourceInfo sourceInfo) {
    super(sourceInfo);
  }

  @Override
  public void checkValidity() {
    if (!(parent instanceof JExpression || parent instanceof JNameValuePair
        || parent instanceof JAssertStatement || parent instanceof JCaseStatement
        || parent instanceof JDoStatement || parent instanceof JForStatement
        || parent instanceof JIfStatement || parent instanceof JReturnStatement
        || parent instanceof JSwitchStatement || parent instanceof JAnnotationMethod
        || parent instanceof JFieldInitializer || parent instanceof JWhileStatement
        || parent instanceof JSynchronizedBlock)) {
      throw new JNodeInternalError(this, "Invalid parent");
    }
  }
}
