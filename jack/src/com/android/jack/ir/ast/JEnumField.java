/*
 * Copyright (C) 2008 The Android Open Source Project
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

import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Description;
import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/**
 * A field that is an enum constant.
 */
@Description("An enum constant")
public class JEnumField extends JField {

  public static final int ORDINAL_UNKNOWN = -1;

  private final int ordinal;

  public JEnumField(@Nonnull SourceInfo info, @Nonnull String name, int ordinal,
      @Nonnull JDefinedEnum enclosingType, @Nonnull JDefinedClass type) {
    super(info, name, enclosingType, type, JModifier.STATIC
        | JModifier.FINAL | JModifier.ENUM | JModifier.PUBLIC);
    this.ordinal = ordinal;
  }

  @Nonnull
  @Override
  public JDefinedEnum getEnclosingType() {
    return (JDefinedEnum) super.getEnclosingType();
  }

  public int ordinal() {
    return ordinal;
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }
}
