/*
 * Copyright (C) 2013 The Android Open Source Project
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

import com.android.jack.ir.SourceInfo;
import com.android.jack.ir.naming.TypeName;
import com.android.jack.ir.naming.TypeName.Kind;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/**
 * String representing the source name of a type.
 */
@Description("String representing the source name of a type")
public class JTypeStringLiteral extends JAbstractStringLiteral {

  @Nonnull
  private final TypeName typeName;

  public JTypeStringLiteral(@Nonnull SourceInfo sourceInfo, @Nonnull Kind kind,
      @Nonnull JType type) {
    super(sourceInfo);
    this.typeName = new TypeName(kind, type);
  }

  @Override
  @Nonnull
  public String getValue() {
    return typeName.toString();
  }

  @Nonnull
  public JType getReferencedType() {
    return typeName.getReferencedType();
  }

  @Override
  @Nonnull
  public JTypeStringLiteral clone() {
    return (JTypeStringLiteral) super.clone();
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }
}
