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

package com.android.jack.transformations.lambda;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDynamicCastOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JLambda;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JMethodIdRef;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JNewInstance;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JParameterRef;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JThis;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.ast.marker.SimpleName;
import com.android.jack.ir.formatter.IdentifierFormatter;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.ir.sourceinfo.SourceInfoFactory;
import com.android.jack.load.NopClassOrInterfaceLoader;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.lookup.JLookup;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.transformations.request.AppendField;
import com.android.jack.transformations.request.AppendMethod;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.NamingTools;
import com.android.sched.item.Description;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Convert lambda to anonymous class.
 */
@Description("Convert lambda to anonymous class implementation.")
@Constraint(need = JLambda.class)
@Transform(remove = JLambda.class,
    add = {JAsgOperation.class, JBlock.class, JConstructor.class, JDynamicCastOperation.class,
        JDefinedClass.class, JLocal.class, JExpressionStatement.class, JField.class,
        JFieldRef.class, JLocalRef.class, JMethod.class, JMethodBody.class, JMethodCall.class,
        JNewInstance.class, JParameter.class, JParameterRef.class, JReturnStatement.class,
        JThisRef.class})
// Lambda converter must be synchronized, otherwise several schedulables can add member types to the
// same class in the same time.
@Support(LambdaToAnonymousConverter.class)
@Synchronized
@Filter(TypeWithoutPrebuiltFilter.class)
public class LambdaConverter implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final JLookup lookup = Jack.getSession().getPhantomLookup();

  private class LambdaToAnonymousConverter extends JVisitor {

    @Nonnull
    private final Map<JExpression, JField> captureVar2InnerPath =
        new HashMap<JExpression, JField>();

    @Nonnull
    private final TransformationRequest tr;

    @Nonnull
    private final JDefinedClassOrInterface currentClass;

    @Nonnull
    private final JClass jlo;

    @Nonnull
    private final JMethodIdWide jloInitMethodId;

    @Nonnegative
    private int anonymousCountByMeth = 0;

    @Nonnull
    private final String lambdaClassNamePrefix;

    @Nonnull
    private final JMethod currentMethod;

    public LambdaToAnonymousConverter(@Nonnull TransformationRequest tr, @Nonnull JMethod method) {
      this.tr = tr;
      this.currentClass = method.getEnclosingType();
      jlo = lookup.getClass(CommonTypes.JAVA_LANG_OBJECT);
      jloInitMethodId = jlo.getMethodIdWide(NamingTools.INIT_NAME, Collections.<JType>emptyList(),
          MethodKind.INSTANCE_NON_VIRTUAL);
      currentMethod = method;
      lambdaClassNamePrefix = NamingTools.getNonSourceConflictingName(
          IdentifierFormatter.getFormatter().getName(method) + "LambdaImpl");
    }

    @Override
    public boolean visit(@Nonnull JLambda lambdaExpr) {
      JMethodIdRef mthIdRef = lambdaExpr.getMethodIdRef();
      JMethod lambdaMethod = mthIdRef.getEnclosingType().getMethod(
          mthIdRef.getMethodId().getMethodIdWide().getName(), mthIdRef.getMethodId().getType(),
          mthIdRef.getMethodId().getMethodIdWide().getParamTypes());

      JDefinedClass lambdaInnerClass = createInnerClass(lambdaExpr);

      JMethodId mthIdWithErasure = lambdaExpr.getMethodIdWithErasure();
      JMethod mthToImplement =
          createMethod(lambdaInnerClass, mthIdWithErasure, /* isBridge= */ false);
      JThis thisOfLambda = mthToImplement.getThis();
      assert thisOfLambda != null;

      for (JMethodId bridgeMthId : lambdaExpr.getBridgeMethodIds()) {
        JMethod bridge =
            createMethod(lambdaInnerClass, bridgeMthId, /* isBridge= */ true);
        JThis thisOfBridge = bridge.getThis();
        assert thisOfBridge != null;
        delegateCall(bridge, mthToImplement, lambdaExpr.getMethodIdWithoutErasure(),
            Collections.<JExpression>emptyList(),
            thisOfBridge.makeRef(SourceInfo.UNKNOWN), lambdaInnerClass);
      }

      // Build <init> method of class implementing lambda and fields for all captured variables.
      // Generated code looks like
      // public final synthetic class <current class name>$LambdaImpl<class counter> {
      // private synthetic <captured variable type> val$<captured variable name>;
      // ....
      // public synthetic <current class name>$LambdaImpl<class counter>(<captured variable type>
      // <captured variable name>, ...) {
      // super.init();
      // val$<captured variable name> = <captured variable name>;
      // ...
      // }
      // }T

      JConstructor lambdaImplCons = new JConstructor(SourceInfo.UNKNOWN, lambdaInnerClass,
          JModifier.PUBLIC | JModifier.SYNTHETIC);

      JBlock constructorBody = new JBlock(SourceInfo.UNKNOWN);
      lambdaImplCons.setBody(new JMethodBody(SourceInfo.UNKNOWN, constructorBody));

      JThis thisOfConstructor = lambdaImplCons.getThis();
      assert thisOfConstructor != null;

      constructorBody.addStmt(
          new JMethodCall(SourceInfo.UNKNOWN, thisOfConstructor.makeRef(SourceInfo.UNKNOWN),
              jlo, jloInitMethodId, JPrimitiveTypeEnum.VOID.getType(), false).makeStatement());

      for (JExpression capturedVar : lambdaExpr.getCapturedVariables()) {
        createFieldAndAssignment(lambdaImplCons, capturedVar);
      }

      delegateCall(mthToImplement, lambdaMethod, lambdaExpr.getMethodIdWithoutErasure(),
          lambdaExpr.getCapturedVariables(), null, lambdaInnerClass);

      tr.append(new AppendMethod(lambdaInnerClass, lambdaImplCons));
      constructorBody.addStmt(new JReturnStatement(SourceInfo.UNKNOWN, null));

      // Replace a lambda expression by the following code:
      // new <current class name>$LambdaImpl<class counter>(value of captured variables,...)
      JNewInstance newAnnonymous = new JNewInstance(lambdaExpr.getSourceInfo(),
          lambdaImplCons.getEnclosingType(), lambdaImplCons.getMethodIdWide());

      for (JExpression capturedVar : lambdaExpr.getCapturedVariables()) {
        newAnnonymous.addArg(capturedVar);
      }

      tr.append(new Replace(lambdaExpr, newAnnonymous));

      return false;
    }

    @Nonnull
    private void createFieldAndAssignment(@Nonnull JConstructor constructor,
        @Nonnull JExpression capturedVar) {
      JDefinedClass lambdaImplClass = constructor.getEnclosingType();
      JMethodBody body = constructor.getBody();
      assert body != null;
      JBlock constructorBody = body.getBlock();
      JThis thisOfConstructor = constructor.getThis();
      assert thisOfConstructor != null;
      String name = null;
      if (capturedVar instanceof JVariableRef) {
        name = ((JVariableRef) capturedVar).getTarget().getName();
      } else {
        if (capturedVar instanceof JFieldRef) {
          JField field = ((JFieldRef) capturedVar).getFieldId().getField();
          if (field != null) {
            name = field.getName();
          }
        }
        if (name == null) {
          name = "arg" + constructor.getParams().size();
        }
      }

      JField field = new JField(SourceInfo.UNKNOWN, "val$" + name, lambdaImplClass,
          capturedVar.getType(), JModifier.PRIVATE | JModifier.SYNTHETIC);
      tr.append(new AppendField(lambdaImplClass, field));
      captureVar2InnerPath.put(capturedVar, field);

      JParameter parameter = new JParameter(SourceInfo.UNKNOWN, name, capturedVar.getType(),
          JModifier.SYNTHETIC, constructor);
      constructor.addParam(parameter);
      constructor.getMethodIdWide().addParam(parameter.getType());

      JAsgOperation asg = new JAsgOperation(
          SourceInfo.UNKNOWN, new JFieldRef(SourceInfo.UNKNOWN,
              thisOfConstructor.makeRef(SourceInfo.UNKNOWN), field.getId(), lambdaImplClass),
          parameter.makeRef(SourceInfo.UNKNOWN));
      constructorBody.addStmt(asg.makeStatement());
    }

    @Nonnull
    private JMethod createMethod(@Nonnull JDefinedClass jClass, @Nonnull JMethodId methId,
        boolean isBridge) {
      SourceInfo sourceInfo = SourceInfo.UNKNOWN;

      int mthModifier =
          JModifier.PUBLIC | (isBridge ? (JModifier.SYNTHETIC | JModifier.BRIDGE) : 0);

      JMethod mth = new JMethod(sourceInfo, methId, jClass, mthModifier);

      int pIdx = 0;
      for (JType parameterType : methId.getMethodIdWide().getParamTypes()) {
        mth.addParam(
            new JParameter(sourceInfo, "arg" + pIdx++, parameterType, JModifier.DEFAULT, mth));
      }

      tr.append(new AppendMethod(jClass, mth));

      return mth;
    }

    private void delegateCall(@Nonnull JMethod mth, @Nonnull JMethod mthToCall,
        @Nonnull JMethodId enforceMthId, @Nonnull List<JExpression> capturedVariables,
        @CheckForNull JExpression instanceOfMthCall, @Nonnull JDefinedClass lambdaInnerClass) {
      SourceInfo sourceInfo = SourceInfo.UNKNOWN;

      JBlock bodyBlock = new JBlock(sourceInfo);
      JMethodBody body = new JMethodBody(sourceInfo, bodyBlock);

      int firstArg = 0;
      if (instanceOfMthCall == null && !mthToCall.isStatic()) {
        JThis mthThis = mth.getThis();
        assert mthThis != null;
        instanceOfMthCall =
            getInnerPath(capturedVariables.get(0), lambdaInnerClass, mthThis.makeRef(sourceInfo));
        assert instanceOfMthCall != null;
        firstArg = 1;
      }

      JMethodCall call = new JMethodCall(sourceInfo, instanceOfMthCall,
          mthToCall.getEnclosingType(), mthToCall.getMethodId().getMethodIdWide(),
          mthToCall.getType(), mthToCall.getMethodId().getMethodIdWide().canBeVirtual());

      // Captured variables on a delegate call are always previously captured into a field of the
      // inner class
      for (int argIdx = firstArg; argIdx < capturedVariables.size(); argIdx++) {
        JExpression capturedVar = capturedVariables.get(argIdx);
        JThis mthThis = mth.getThis();
        assert mthThis != null;
        JExpression innerPath =
            getInnerPath(capturedVar, lambdaInnerClass, mthThis.makeRef(sourceInfo));
        assert innerPath != null;
        call.addArg(innerPath);
      }

      List<JType> paramType = enforceMthId.getMethodIdWide().getParamTypes();
      int pIndex = 0;
      for (JParameter param : mth.getParams()) {
        call.addArg(new JDynamicCastOperation(sourceInfo, param.makeRef(sourceInfo),
            paramType.get(pIndex++)));
      }

      if (mth.getType() != JPrimitiveTypeEnum.VOID.getType()) {
        bodyBlock.addStmt(new JReturnStatement(sourceInfo, call));
      } else {
        bodyBlock.addStmt(new JExpressionStatement(sourceInfo, call));
        bodyBlock.addStmt(new JReturnStatement(sourceInfo, null));
      }

      mth.setBody(body);
    }

    @Nonnull
    private JDefinedClass createInnerClass(@Nonnull JLambda lambdaExpr) {
      String simpleName = lambdaClassNamePrefix + anonymousCountByMeth++;
      JDefinedClass lambdaImpl = new JDefinedClass(
          new SourceInfoFactory().create(currentClass.getSourceInfo().getFileName()),
          currentClass.getName() + "$" + simpleName, JModifier.FINAL | JModifier.SYNTHETIC,
          currentClass.getEnclosingPackage(), NopClassOrInterfaceLoader.INSTANCE);

      currentClass.addMemberType(lambdaImpl);
      lambdaImpl.setEnclosingType(currentClass);

      lambdaImpl.setSuperClass(jlo);

      lambdaImpl.addImplements(lambdaExpr.getType());
      for (JInterface bound : lambdaExpr.getInterfaceBounds()) {
        if (!bound.isSameType(lambdaExpr.getType())) {
          lambdaImpl.addImplements(bound);
        }
      }

      lambdaImpl.addMarker(new SimpleName(simpleName));

      Jack.getSession().addTypeToEmit(lambdaImpl);

      return lambdaImpl;
    }

    @CheckForNull
    public JFieldRef getInnerPath(@Nonnull JExpression capturedVar,
        @Nonnull JDefinedClass innerClass, @Nonnull JExpression instance) {
      assert captureVar2InnerPath.containsKey(capturedVar);
      JField innerField = captureVar2InnerPath.get(capturedVar);
      return new JFieldRef(SourceInfo.UNKNOWN, instance, innerField.getId(), innerClass);
    }
  }

  @Override
  public void run(JMethod method) throws Exception {
    TransformationRequest request = new TransformationRequest(method);
    LambdaToAnonymousConverter visitor = new LambdaToAnonymousConverter(request, method);
    visitor.accept(method);
    request.commit();
  }
}
