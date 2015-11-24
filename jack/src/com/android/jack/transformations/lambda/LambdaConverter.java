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
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JNewInstance;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JParameterRef;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JThis;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVariable;
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
import com.android.jack.scheduling.feature.SourceVersion8;
import com.android.jack.transformations.request.AppendField;
import com.android.jack.transformations.request.AppendMethod;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.NamingTools;
import com.android.sched.item.Description;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

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
@Support(SourceVersion8.class)
@Synchronized
public class LambdaConverter implements RunnableSchedulable<JMethod> {

  /**
   * LambdaCtx save the relation between captured variables and theirs corresponding fields.
   * It also keep JThis of the method implementing the lambda.
   */
  private static class LambdaCtx {
    @Nonnull
    private final Map<JVariable, JField> capturedVar2Field = new HashMap<JVariable, JField>();

    @Nonnull
    private final JThis thisOfLambdaImpl;

    public LambdaCtx(@Nonnull JThis thisOfLambdaImpl) {
      this.thisOfLambdaImpl = thisOfLambdaImpl;
    }

    public void addVar2FieldMapping(@Nonnull JVariable capturedVar, @Nonnull JField field) {
      assert !capturedVar2Field.containsKey(capturedVar);
      capturedVar2Field.put(capturedVar, field);
    }

    @CheckForNull
    public JFieldRef getCapturedVar(@Nonnull JVariable capturedVar) {
      JField field = capturedVar2Field.get(capturedVar);
      if (field != null) {
        return (new JFieldRef(SourceInfo.UNKNOWN,
            new JThisRef(SourceInfo.UNKNOWN, thisOfLambdaImpl), field.getId(),
            field.getEnclosingType()));
      }

      return null;
    }
  }

  private static class LambdaToAnonymousConverter extends JVisitor {

    @Nonnull
    private final TransformationRequest tr;

    @Nonnull
    private final JDefinedClassOrInterface currentClass;

    @Nonnull
    private final JClass jlo;

    @Nonnull
    private final JMethodId jloInitMethodId;

    @Nonnull
    private final Stack<LambdaCtx> lambdaCtxStack = new Stack<LambdaCtx>();

    @Nonnegative
    private int anonymousCountByMeth = 0;

    @Nonnull
    private final String lambdaClassNamePrefix;

    @Nonnull
    private final JMethod currentMethod;

    /**
     * Save method representing lambda that are already transformed to reuse the same implementation
     */
    private final Map<JMethod, JConstructor> lambdaToLambaImplConst =
        new HashMap<JMethod, JConstructor>();

    public LambdaToAnonymousConverter(@Nonnull TransformationRequest tr, @Nonnull JMethod method) {
      this.tr = tr;
      this.currentClass = method.getEnclosingType();
      JSession session = Jack.getSession();
      JLookup lookup = session.getLookup();
      jlo = lookup.getClass(CommonTypes.JAVA_LANG_OBJECT);
      jloInitMethodId = jlo.getMethodId(NamingTools.INIT_NAME, Collections.<JType>emptyList(),
          MethodKind.INSTANCE_NON_VIRTUAL);
      currentMethod = method;
      lambdaClassNamePrefix = NamingTools.getNonSourceConflictingName(
          IdentifierFormatter.getFormatter().getName(method) + "LambdaImpl");
    }

    @Override
    public boolean visit(@Nonnull JLambda lambdaExpr) {
      JMethod lambdaMethod = lambdaExpr.getMethod();
      LambdaCtx lambdaCtx = null;
      JConstructor lambdaImplCons = lambdaToLambaImplConst.get(lambdaMethod);

      JThis capturedInstance = null;

      if (lambdaExpr.needToCaptureInstance()) {
        capturedInstance = currentMethod.getThis();
      }

      if (lambdaImplCons == null) {
        JDefinedClass lambdaImplClass = createLambdaImplClass(lambdaExpr);

        JMethod samMethod = lambdaExpr.getType().getSingleAbstractMethod();
        assert samMethod != null;
        if (needBridgeMethod(samMethod, lambdaExpr)) {
          synthesizeBridge(lambdaImplClass, samMethod, lambdaMethod);
        }

        // Move method representing lambda body into the class implementing lambda.
        // It is possible to do this, since lambda expression will be removed from
        // IR and thus the corresponding JMethod will not be longer attach to the IR and can be
        // directly reused.
        tr.append(new AppendMethod(lambdaImplClass, lambdaMethod));
        lambdaMethod.setModifier(JModifier.FINAL | JModifier.SYNTHETIC | JModifier.PUBLIC);
        lambdaMethod.setEnclosingType(lambdaImplClass);
        JThis thisOfLambdaImpl = new JThis(lambdaMethod);
        assert lambdaMethod.getThis() == null;
        lambdaMethod.setThis(thisOfLambdaImpl);
        lambdaCtx = new LambdaCtx(thisOfLambdaImpl);


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

        lambdaImplCons = new JConstructor(SourceInfo.UNKNOWN, lambdaImplClass,
            JModifier.PUBLIC | JModifier.SYNTHETIC);
        lambdaToLambaImplConst.put(lambdaMethod, lambdaImplCons);

        JBlock constructorBody = new JBlock(SourceInfo.UNKNOWN);
        lambdaImplCons.setBody(new JMethodBody(SourceInfo.UNKNOWN, constructorBody));

        JThis thisOfConstructor = lambdaImplCons.getThis();
        assert thisOfConstructor != null;

        constructorBody.addStmt(
            new JMethodCall(SourceInfo.UNKNOWN, new JThisRef(SourceInfo.UNKNOWN, thisOfConstructor),
                jlo, jloInitMethodId, JPrimitiveTypeEnum.VOID.getType(), false).makeStatement());

        for (JVariableRef capturedVarRef : lambdaExpr.getCapturedVariables()) {
          createFieldAndAssignment(lambdaCtx, lambdaImplCons, capturedVarRef.getTarget());
        }

        if (capturedInstance != null) {
          createFieldAndAssignment(lambdaCtx, lambdaImplCons, capturedInstance);
        }

        constructorBody.addStmt(new JReturnStatement(SourceInfo.UNKNOWN, null));
        tr.append(new AppendMethod(lambdaImplClass, lambdaImplCons));
      }

      assert lambdaImplCons != null;

      // Replace a lambda expression by the following code:
      // new <current class name>$LambdaImpl<class counter>(value of captured variables,...)
      JNewInstance newAnnonymous = new JNewInstance(SourceInfo.UNKNOWN,
          lambdaImplCons.getEnclosingType(), lambdaImplCons.getMethodId());

      for (JVariableRef capturedVarRef : lambdaExpr.getCapturedVariables()) {
        JVariable capturedVar = capturedVarRef.getTarget();
        JExpression arg = getCapturedVar(capturedVar);
        if (arg == null) {
          if (capturedVar instanceof JParameter) {
            arg = new JParameterRef(SourceInfo.UNKNOWN, (JParameter) capturedVar);
          } else {
            arg = new JLocalRef(SourceInfo.UNKNOWN, (JLocal) capturedVar);
          }
        }
        newAnnonymous.addArg(arg);
      }

      if (capturedInstance != null) {
        JExpression arg = getCapturedVar(capturedInstance);
        if (arg == null) {
          newAnnonymous.addArg(new JThisRef(SourceInfo.UNKNOWN, capturedInstance));
        } else {
          newAnnonymous.addArg(arg);
        }
      }

      tr.append(new Replace(lambdaExpr, newAnnonymous));

      lambdaCtxStack.push(lambdaCtx);

      accept(lambdaExpr.getBody());

      return false;
    }

    @CheckForNull
    private JFieldRef getCapturedVar(@Nonnull JVariable capturedVar) {
      JFieldRef fieldRef = null;
      if (!lambdaCtxStack.isEmpty()) {
        fieldRef = lambdaCtxStack.peek().getCapturedVar(capturedVar);
      }
      return fieldRef;
    }

    @Nonnull
    private void createFieldAndAssignment(@Nonnull LambdaCtx lambdaCtx,
        @Nonnull JConstructor constructor, @Nonnull JVariable capturedVar) {
      JDefinedClass lambdaImplClass = constructor.getEnclosingType();
      JMethodBody body = constructor.getBody();
      assert body != null;
      JBlock constructorBody = body.getBlock();
      JThis thisOfConstructor = constructor.getThis();
      assert thisOfConstructor != null;

      JField field = new JField(SourceInfo.UNKNOWN, "val$" + capturedVar.getName(), lambdaImplClass,
          capturedVar.getType(), JModifier.PRIVATE | JModifier.SYNTHETIC);
      tr.append(new AppendField(lambdaImplClass, field));
      lambdaCtx.addVar2FieldMapping(capturedVar, field);

      JParameter parameter = new JParameter(SourceInfo.UNKNOWN, capturedVar.getName(),
          capturedVar.getType(), JModifier.SYNTHETIC, constructor);
      constructor.addParam(parameter);
      constructor.getMethodId().addParam(parameter.getType());

      JAsgOperation asg = new JAsgOperation(
          SourceInfo.UNKNOWN, new JFieldRef(SourceInfo.UNKNOWN,
              new JThisRef(SourceInfo.UNKNOWN, thisOfConstructor), field.getId(), lambdaImplClass),
          new JParameterRef(SourceInfo.UNKNOWN, parameter));
      constructorBody.addStmt(asg.makeStatement());
    }

    @Override
    public void endVisit(@Nonnull JLambda lambdaExpr) {
      lambdaCtxStack.pop();
      super.endVisit(lambdaExpr);
    }

    private void synthesizeBridge(@Nonnull JDefinedClass jClass, @Nonnull JMethod method,
        @Nonnull JMethod lamdbaMethodImpl) {
      SourceInfo sourceInfo = SourceInfo.UNKNOWN;
      JMethodId methodId = method.getMethodId();

      int bridgeModifier = method.getModifier();
      bridgeModifier &= ~(JModifier.ABSTRACT);
      bridgeModifier |= JModifier.SYNTHETIC | JModifier.BRIDGE;

      JMethod bridge = new JMethod(sourceInfo, methodId, jClass, method.getType(), bridgeModifier);

      for (JParameter param : method.getParams()) {
        bridge.addParam(new JParameter(sourceInfo, param.getName(), param.getType(),
            param.getModifier(), bridge));
      }

      JBlock bodyBlock = new JBlock(sourceInfo);
      JMethodBody body = new JMethodBody(sourceInfo, bodyBlock);
      JThis jThis = bridge.getThis();
      assert jThis != null;

      JMethodCall callToSuper = new JMethodCall(sourceInfo, new JThisRef(sourceInfo, jThis), jClass,
          lamdbaMethodImpl.getMethodId(), lamdbaMethodImpl.getType(), true /* isVirtualDispatch */);

      List<JType> paramType = lamdbaMethodImpl.getMethodId().getParamTypes();
      int pIndex =  0;
      for (JParameter param : bridge.getParams()) {
        callToSuper.addArg(new JDynamicCastOperation(sourceInfo,
            new JParameterRef(sourceInfo, param), paramType.get(pIndex++)));
      }

      if (method.getType() != JPrimitiveTypeEnum.VOID.getType()) {
        bodyBlock.addStmt(new JReturnStatement(sourceInfo, callToSuper));
      } else {
        bodyBlock.addStmt(new JExpressionStatement(sourceInfo, callToSuper));
        bodyBlock.addStmt(new JReturnStatement(sourceInfo, null));
      }

      bridge.setBody(body);

      tr.append(new AppendMethod(jClass, bridge));
    }

    @Override
    public boolean visit(@Nonnull JVariableRef varRef) {
      JExpression exprToUse = getCapturedVar(varRef.getTarget());
      if (exprToUse != null) {
        tr.append(new Replace(varRef, exprToUse));
      }
      return super.visit(varRef);
    }

    @Nonnull
    private JDefinedClass createLambdaImplClass(@Nonnull JLambda lambdaExpr) {
      String simpleName = lambdaClassNamePrefix + anonymousCountByMeth;
      JDefinedClass lambdaImpl =
          new JDefinedClass(new SourceInfoFactory().create(currentClass.getSourceInfo()
              .getFileName()), currentClass.getName() + "$" + simpleName,
              JModifier.PUBLIC | JModifier.SYNTHETIC, currentClass.getEnclosingPackage(),
              NopClassOrInterfaceLoader.INSTANCE);
      anonymousCountByMeth++;

      currentClass.addMemberType(lambdaImpl);

      lambdaImpl.setSuperClass(jlo);
      lambdaImpl.addImplements(lambdaExpr.getType());
      for (JInterface bound : lambdaExpr.getInterfaceBounds()) {
        if (!bound.isSameType(lambdaExpr.getType())) {
          lambdaImpl.addImplements(bound);
        }
      }
      lambdaImpl.setEnclosingType(currentClass);
      lambdaImpl.addMarker(new SimpleName(simpleName));

      Jack.getSession().addTypeToEmit(lambdaImpl);

      return lambdaImpl;
    }

    private boolean needBridgeMethod(@Nonnull JMethod samMethod, @Nonnull JLambda lambdaExpr) {
      assert samMethod.getParams().size() == lambdaExpr.getParameters().size();
      JMethod lambdaMethod = lambdaExpr.getMethod();

      if (!lambdaMethod.getType().equals(samMethod.getType())) {
        return true;
      }

      Iterator<JParameter> lambdaParameter = lambdaMethod.getParams().iterator();
      for (JParameter samParameter : samMethod.getParams()) {
        if (!samParameter.getType().isSameType(lambdaParameter.next().getType())) {
           return true;
           }
      }
      return false;
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
