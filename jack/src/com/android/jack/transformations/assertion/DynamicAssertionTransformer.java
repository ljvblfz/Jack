/*
 * Copyright (C) 2012 The Android Open Source Project
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
import com.android.jack.ir.ast.FieldKind;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JAssertStatement;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassLiteral;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldId;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JNewInstance;
import com.android.jack.ir.ast.JPrefixNotOperation;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JThrowStatement;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.lookup.JLookupException;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.transformations.InitializationExpression;
import com.android.jack.transformations.ast.BooleanTestOutsideIf;
import com.android.jack.transformations.ast.NewInstanceRemoved;
import com.android.jack.transformations.request.AppendField;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.NamingTools;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
/**
 * This {@link RunnableSchedulable} transforms "assert" into a "throw" if assertions are enabled.
 */
@Description("Transforms assert into a throw if assertions are enabled")
@Name("DynamicAssertionTransformer")
@Synchronized
@Constraint(need = {JAssertStatement.class})
@Transform(add = {AssertionTransformerSchedulingSeparator.SeparatorTag.class,
    BooleanTestOutsideIf.class,
    JIfStatement.class,
    JThrowStatement.class,
    JPrefixNotOperation.class,
    JMethodCall.class,
    JBlock.class,
    JFieldRef.class,
    JField.class,
    JClassLiteral.class,
    JNewInstance.class,
    JAsgOperation.NonReusedAsg.class,
    InitializationExpression.class,
    JExpressionStatement.class},
    remove = {JAssertStatement.class, ThreeAddressCodeForm.class, NewInstanceRemoved.class})
@Support(DynamicAssertionFeature.class)
@Filter(TypeWithoutPrebuiltFilter.class)
public class DynamicAssertionTransformer implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);

  @Nonnull
  private final JClass jlo =
      Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_OBJECT);

  @Nonnull
  private static final String ASSERTION_FIELD_NAME =
      NamingTools.getNonSourceConflictingName("assertionsDisabled");

  private class Visitor extends JVisitor {

    @Nonnull
    private final JDefinedClassOrInterface currentType;

    public Visitor(@Nonnull JDefinedClassOrInterface type) {
      this.currentType = type;
    }

    @Nonnull
    private JFieldId getOrCreateAssertionstatusField(@Nonnull TransformationRequest request) {
      try {
        JFieldId id = currentType.getFieldId(ASSERTION_FIELD_NAME,
            JPrimitiveTypeEnum.BOOLEAN.getType(), FieldKind.STATIC);
        JField field = id.getField();
        if (field != null && field.getEnclosingType().isSameType(currentType)) {
          // return only direct field
          return id;
        }
      } catch (JLookupException e) {
        // fallback to create the field
      }
      return addAssertionStatusToType(currentType, request);
    }

    @Nonnull
    private JFieldId addAssertionStatusToType(@Nonnull JDefinedClassOrInterface type,
        @Nonnull TransformationRequest request) {
      SourceInfo sourceInfo = SourceInfo.UNKNOWN;

      // Create field $assertionsDisabled
      int modifier = JModifier.FINAL | JModifier.STATIC | JModifier.SYNTHETIC;
      JField assertionStatus = new JField(SourceInfo.UNKNOWN, ASSERTION_FIELD_NAME,
          currentType, JPrimitiveTypeEnum.BOOLEAN.getType(), modifier);
      JFieldId assertionStatusId = assertionStatus.getId();
      request.append(new AppendField(currentType, assertionStatus));

      // A.$assertionsDisabled = !A.class.desiredAssertionStatus();
      JClass javaLangClass =
          Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_CLASS);
      JClassLiteral thisClass = new JClassLiteral(sourceInfo, type, javaLangClass);
      JFieldRef lhs = new JFieldRef(sourceInfo, null, assertionStatusId, type);
      JExpression rhs = new JPrefixNotOperation(sourceInfo,
          new JMethodCall(sourceInfo, thisClass, javaLangClass,
              javaLangClass.getOrCreateMethodIdWide("desiredAssertionStatus",
                  Collections.<JType>emptyList(), MethodKind.INSTANCE_VIRTUAL),
              JPrimitiveTypeEnum.BOOLEAN.getType(), true /* isVirtualDispatch */));
      JAsgOperation asg = new JAsgOperation(SourceInfo.UNKNOWN, lhs, rhs);

      assertionStatus.addMarker(new InitializationExpression(asg.makeStatement()));

      return assertionStatusId;
    }

    @Override
    public void endVisit(@Nonnull JAssertStatement assertSt) {
      // assert test : message
      // =>
      // if (!$assertionsDisabled)
      // if (!test)
      // throw new AssertionError(message);
      TransformationRequest request = new TransformationRequest(assertSt);
      JFieldId assertionStatus = getOrCreateAssertionstatusField(request);

      JExpression assertionEnabledCondition =
          new JPrefixNotOperation(assertSt.getSourceInfo(), new JFieldRef(assertSt.getSourceInfo(),
              null, assertionStatus, currentType));

      JExpression testExpression = assertSt.getTestExpr();
      JExpression notTestCondition =
          new JPrefixNotOperation(testExpression.getSourceInfo(), testExpression);

      List<JType> ctorDescriptor = new ArrayList<JType>();
      JExpression arg = assertSt.getArg();
      if (arg != null) {
        ctorDescriptor.add(jlo);
      }

      JClass assertionError =
          Jack.getSession().getPhantomLookup()
              .getClass(CommonTypes.JAVA_LANG_ASSERTION_ERROR);
      JNewInstance newAssertionError = new JNewInstance(assertSt.getSourceInfo(),
          assertionError,
          assertionError.getOrCreateMethodIdWide(NamingTools.INIT_NAME, ctorDescriptor,
              MethodKind.INSTANCE_NON_VIRTUAL));

      if (arg != null) {
        newAssertionError.addArg(arg);
      }

      JThrowStatement throwAssertionError =
          new JThrowStatement(assertSt.getSourceInfo(), newAssertionError);
      JBlock blockThrow = new JBlock(assertSt.getSourceInfo());
      blockThrow.addStmt(throwAssertionError);

      JIfStatement ifNotTest = new JIfStatement(assertSt.getSourceInfo(),
          notTestCondition, blockThrow, null);

      JBlock thenAssertionEnabled = new JBlock(assertSt.getSourceInfo());
      thenAssertionEnabled.addStmt(ifNotTest);

      JIfStatement ifAssertionEnabled = new JIfStatement(assertSt.getSourceInfo(),
          assertionEnabledCondition, thenAssertionEnabled, null);
      request.append(new Replace(assertSt, ifAssertionEnabled));
      request.commit();
    }
  }

  @Override
  public synchronized void run(@Nonnull JMethod method) throws Exception {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }
    Visitor visitor = new Visitor(method.getEnclosingType());
    visitor.accept(method);
  }
}
