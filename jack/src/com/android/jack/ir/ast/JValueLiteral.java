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

import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Description;

import javax.annotation.Nonnull;

/**
 * Base class for any Java literal expression.
 */
@Description("Java literal expression")
public abstract class JValueLiteral extends JLiteral implements Cloneable {

  public JValueLiteral(SourceInfo sourceInfo) {
    super(sourceInfo);
  }

  @Override
  @Nonnull
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }

  /** Does the literal represent default type value, i.e. it is zero, false or null */
  public abstract boolean isTypeValue();
}
