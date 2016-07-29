/*
 * Copyright 2010 Google Inc.
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

import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.load.MethodLoader;
import com.android.jack.util.NamingTools;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/**
 * A Java constructor method.
 */
@Description("A Java constructor method")
public class JConstructor extends JMethod {

  public JConstructor(@Nonnull SourceInfo info, @Nonnull JDefinedClass enclosingType,
      int modifier) {
    super(info, new JMethodId(
          new JMethodIdWide(NamingTools.INIT_NAME, MethodKind.INSTANCE_NON_VIRTUAL),
          JPrimitiveTypeEnum.VOID.getType()),
        enclosingType, modifier);
    assert JModifier.isMethodModifier(modifier);
    assert JModifier.isValidMethodModifier(modifier);
    assert !JModifier.isAbstract(modifier);
    assert !JModifier.isBridge(modifier);
    assert !JModifier.isFinal(modifier);
    assert !JModifier.isNative(modifier);
    assert !JModifier.isStatic(modifier);
    assert !JModifier.isSynchronized(modifier);
  }

  public JConstructor(@Nonnull SourceInfo info, @Nonnull JDefinedClass enclosingType, int modifier,
      @Nonnull MethodLoader loader) {
    super(info, new JMethodId(
          new JMethodIdWide(NamingTools.INIT_NAME, MethodKind.INSTANCE_NON_VIRTUAL),
          JPrimitiveTypeEnum.VOID.getType()),
        enclosingType, modifier, loader);
    assert JModifier.isMethodModifier(modifier);
    assert JModifier.isValidMethodModifier(modifier);
    assert !JModifier.isAbstract(modifier);
    assert !JModifier.isBridge(modifier);
    assert !JModifier.isFinal(modifier);
    assert !JModifier.isNative(modifier);
    assert !JModifier.isStatic(modifier);
    assert !JModifier.isSynchronized(modifier);
  }

  @Override
  public boolean canBeVirtual() {
    return false;
  }

  @Nonnull
  @Override
  public JDefinedClass getEnclosingType() {
    return (JDefinedClass) super.getEnclosingType();
  }

  public JClass getNewType() {
    return getEnclosingType();
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      visitChildren(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    visitChildren(schedule);
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }
}
