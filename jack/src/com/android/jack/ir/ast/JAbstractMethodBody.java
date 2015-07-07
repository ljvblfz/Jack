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

import com.android.jack.ir.JNodeInternalError;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Description;

/**
 * Represents the body of a method.
 */
@Description("Represents the body of a method")
public abstract class JAbstractMethodBody extends JNode {

  protected JMethod method;

  protected JAbstractMethodBody(SourceInfo info) {
    super(info);
  }

  public JMethod getMethod() {
    return method;
  }

  public abstract boolean isNative();

  public void setMethod(JMethod method) {
    this.method = method;
  }

  @Override
  public void checkValidity() {
    if (!(parent instanceof JMethod || parent instanceof JLambda)) {
      throw new JNodeInternalError(this, "Invalid parent");
    }
  }
}
