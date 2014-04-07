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

import com.android.jack.ir.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/**
 * Java class literal expression.
 *
 * NOTE: This class is modeled as if it were a JFieldRef to a field declared in
 * ClassLiteralHolder. That field contains the class object allocation
 * initializer.
 */
@Description("Java class literal expression")
public class JClassLiteral extends JLiteral {

  private static final long serialVersionUID = 1L;

  private JClass javaLangClass;

  private final JType refType;

  public JClassLiteral(SourceInfo sourceInfo, JType type, JClass javaLangClass) {
    super(sourceInfo);
    refType = type;
    this.javaLangClass = javaLangClass;
  }

  public JType getRefType() {
    return refType;
  }

  @Override
  public JType getType() {
    return javaLangClass;
  }

  public void setType(@Nonnull JClass type) {
     javaLangClass = type;
  }

  @Override
  public boolean canThrow() {
    return true;
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
