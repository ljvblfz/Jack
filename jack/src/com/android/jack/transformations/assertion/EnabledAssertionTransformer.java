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

package com.android.jack.transformations.assertion;

import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.ir.ast.JAssertStatement;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JNewInstance;
import com.android.jack.ir.ast.JPrefixNotOperation;
import com.android.jack.ir.ast.JThrowStatement;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.transformations.ast.BooleanTestOutsideIf;
import com.android.jack.transformations.ast.NewInstanceRemoved;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.NamingTools;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
/**
 * This {@link RunnableSchedulable} transforms "assert" into a "if" and "throw" statement.
 */
@Description("Transforms assert into a throw and if statement")
@Name("ForcedAssertionTransformer")
@Constraint(need = {JAssertStatement.class})
@Transform(add = {
    BooleanTestOutsideIf.class,
    JIfStatement.class,
    JThrowStatement.class,
    JPrefixNotOperation.class,
    JMethodCall.class,
    JBlock.class,
    JNewInstance.class,
    JExpressionStatement.class},
    remove = {JAssertStatement.class, ThreeAddressCodeForm.class, NewInstanceRemoved.class})
@Support(EnabledAssertionFeature.class)
public class EnabledAssertionTransformer implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  @Nonnull
  private final JClass jlo =
      Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_OBJECT);

  @Nonnull
  private final JClass assertionError =
      Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_ASSERTION_ERROR);

  private class Visitor extends JVisitor {

    @Override
    public void endVisit(@Nonnull JAssertStatement assertSt) {
      // assert test : message
      // =>
      // if (!test)
      // throw new AssertionError(message);
      TransformationRequest request = new TransformationRequest(assertSt);

      JExpression testExpression = assertSt.getTestExpr();
      JExpression notTestCondition =
          new JPrefixNotOperation(testExpression.getSourceInfo(), testExpression);

      List<JType> ctorDescriptor = new ArrayList<JType>();
      if (assertSt.getArg() != null) {
        ctorDescriptor.add(jlo);
      }

      JNewInstance newAssertionError = new JNewInstance(assertSt.getSourceInfo(),
          assertionError,
          assertionError.getOrCreateMethodId(NamingTools.INIT_NAME, ctorDescriptor,
              MethodKind.INSTANCE_NON_VIRTUAL));

      if (assertSt.getArg() != null) {
        newAssertionError.addArg(assertSt.getArg());
      }

      JThrowStatement throwAssertionError =
          new JThrowStatement(assertSt.getSourceInfo(), newAssertionError);
      JBlock blockThrow = new JBlock(assertSt.getSourceInfo());
      blockThrow.addStmt(throwAssertionError);

      JIfStatement ifNotTest = new JIfStatement(assertSt.getSourceInfo(),
          notTestCondition, blockThrow, null);

      request.append(new Replace(assertSt, ifNotTest));
      request.commit();
    }
  }

  @Override
  public synchronized void run(@Nonnull JMethod method) throws Exception {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }
    Visitor visitor = new Visitor();
    visitor.accept(method);
  }
}
