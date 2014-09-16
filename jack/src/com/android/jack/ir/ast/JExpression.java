/*
 * Copyright 2007 Google Inc.
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

import com.android.jack.ir.InternalCompilerException;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Description;

/**
 * Base class for all Java expressions.
 */
@Description("Java expressions")
public abstract class JExpression extends JNode implements HasType {

  public JExpression(SourceInfo info) {
    super(info);
  }

  public boolean isResultUsed() {
    JNode parent = getParent();
    assert parent != null;
    return parent.isResultOfExpressionUsed(this);
  }

  @Override
  protected boolean isResultOfExpressionUsed(JExpression expr) {
    return true;
  }

  public JExpressionStatement makeStatement() {
    return new JExpressionStatement(getSourceInfo(), this);
  }

  @Override
  public void checkValidity() {
    if (!(parent instanceof JExpression || parent instanceof JStatement)) {
      throw new InternalCompilerException(this, "Invalid parent");
    }
  }
}
