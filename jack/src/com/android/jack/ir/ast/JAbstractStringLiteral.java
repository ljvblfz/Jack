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

import com.android.jack.Jack;
import com.android.jack.ir.JNodeInternalError;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.lookup.CommonTypes;
import com.android.sched.item.Description;

import javax.annotation.Nonnull;

/**
 * Common part of literal expression that evaluates to a string.
 */
@Description("Commmon part of literal expression that evaluates to a string")
public abstract class JAbstractStringLiteral extends JValueLiteral implements Cloneable {

  protected JAbstractStringLiteral(@Nonnull SourceInfo sourceInfo) {
    super(sourceInfo);
  }

  @Override
  @Nonnull
  public JClass getType() {
    return Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_STRING);
  }

  @Nonnull
  public abstract String getValue();

  @Override
  public boolean canThrow() {
    // STOPSHIP: Return true when final version of box-lambda will be supported by the runtime.
    return false;
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

  @Override
  public void checkValidity() {
    if (!(parent instanceof JExpression
        || parent instanceof JNameValuePair
        || parent instanceof JAssertStatement
        || parent instanceof JCaseStatement
        || parent instanceof JReturnStatement
        || parent instanceof JSwitchStatement
        || parent instanceof JAnnotationMethod
        || parent instanceof JFieldInitializer
        || parent instanceof JSynchronizedBlock)) {
      throw new JNodeInternalError(this, "Invalid parent");
    }
  }
}
