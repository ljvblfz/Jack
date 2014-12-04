/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.transformations.ast;

import com.android.jack.Jack;
import com.android.jack.JackAbortException;
import com.android.jack.Options;
import com.android.jack.config.id.JavaVersionPropertyId.JavaVersion;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JAsgOperation.NonReusedAsg;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JCatchBlock;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JEqOperation;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JNeqOperation;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JThrowStatement;
import com.android.jack.ir.ast.JTryStatement;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.ir.sourceinfo.SourceInfoFactory;
import com.android.jack.lookup.JMethodLookupException;
import com.android.jack.reporting.Reporter.Severity;
import com.android.jack.scheduling.feature.SourceVersion7;
import com.android.jack.transformations.LocalVarCreator;
import com.android.jack.transformations.TransformationException;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.NamingTools;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;
import com.android.sched.util.config.ThreadConfig;

import java.util.Collections;

import javax.annotation.Nonnull;

/**
 * This {@link RunnableSchedulable} generates the code that will handle auto-closeable resources
 * in try-with-resources statements.
 *
 * try (
 *    Res1 res1 = new Res1();
 *    ...
 *    ResN resN = new ResN();
 *    )
 *    {
 *       // statements
 *    }
 * catch (...) {}
 * finally {}
 *
 * =>
 *
 * try {
 *    exceptionToThrow = null;
 *    Res1 res1 = null;
 *    ...
 *    ResN resN = null;
 *
 *    try {
 *       res1 = new Res1();
 *       ...
 *       resN = new ResN();
 *
 *       // statements
 *
 *    } catch (Throwable twrExceptionInTry) {
 *       exceptionToThrow = twrExceptionInTry;
 *       throw twrExceptionInTry;
 *    } finally {
 *       try {
 *          if (resN != null) {
 *             resN.close();
 *          }
 *       } catch (Throwable twrExceptionThrownByClose_) {
 *          if (exceptionToThrow == null) {
 *             exceptionToThrow = twrExceptionThrownByClose_;
 *          } else if (exceptionToThrow != twrExceptionThrownByClose_) {
 *             exceptionToThrow.addSupressed(twrExceptionThrownByClose_);
 *          }
 *       }
 *       ... for all resources till res1 ...
 *
 *       if (exceptionToThrow != null) {
 *          throw exceptionToThrow
 *       }
 *    }
 * }
 *  catch (...) {}
 *  finally {}
 *
 */
@Description("Generates the code that will handle auto-closeable resources in try-with-resources" +
         "statements.")
@Name("TryWithResourcesTransformer")
@Constraint(need = {JTryStatement.class,
    JTryStatement.TryWithResourcesForm.class})
@Transform(add = {JTryStatement.class,
    JCatchBlock.class,
    JBlock.class,
    JLocalRef.class,
    NonReusedAsg.class,
    JNullLiteral.class,
    JExpressionStatement.class,
    JEqOperation.class,
    JNeqOperation.class,
    JMethodCall.class,
    JIfStatement.class,
    JThrowStatement.class},
    remove = JTryStatement.TryWithResourcesForm.class)
@Use({LocalVarCreator.class, SourceInfoFactory.class})
@Support(SourceVersion7.class)
public class TryWithResourcesTransformer implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  private static class Visitor extends JVisitor {

    @Nonnull
    private final JMethodBody currentMethodBody;
    @Nonnull
    private final LocalVarCreator localVarCreator;
    @Nonnull
    private final TransformationRequest request;
    @Nonnull
    private final SourceInfoFactory sourceInfoFactory = Jack.getSession().getSourceInfoFactory();

    @Nonnull
    private static final String AUTO_CLOSEABLE_SIGNATURE = "Ljava/lang/AutoCloseable;";
    @Nonnull
    private static final String CLOSE_METHOD_NAME = "close";
    @Nonnull
    private static final String THROWABLE_SIGNATURE = "Ljava/lang/Throwable;";
    @Nonnull
    private static final String ADD_SUPPRESSED_METHOD_NAME = "addSuppressed";

    public Visitor(@Nonnull JMethod method, @Nonnull LocalVarCreator localVarCreator,
        @Nonnull TransformationRequest request) {
      JMethodBody body = (JMethodBody) method.getBody();
      assert body != null;
      this.currentMethodBody = body;
      this.localVarCreator = localVarCreator;
      this.request = request;
    }

    @Override
    public void endVisit(@Nonnull JTryStatement x) {

      if (x.getResourcesDeclarations().size() > 0) {

        SourceInfo trySourceInfo = x.getSourceInfo();
        SourceInfo endOfTrySourceInfos = sourceInfoFactory.create(
            trySourceInfo.getEndLine(), trySourceInfo.getEndLine(),
            trySourceInfo.getFileSourceInfo());

        SourceInfo firstLineSourceInfos = sourceInfoFactory.create(
            trySourceInfo.getStartLine(), trySourceInfo.getStartLine(),
            trySourceInfo.getFileSourceInfo());

        JBlock finalTryBlock = new JBlock(trySourceInfo);

        // Declare exception to throw in the end, if any, and initialize it to null;
        JClass throwableClass = Jack.getSession().getPhantomLookup().getClass(THROWABLE_SIGNATURE);
        JLocal exceptionToThrow =
            localVarCreator.createTempLocal(throwableClass, firstLineSourceInfos, request);
        JAsgOperation assign = new JAsgOperation(
            firstLineSourceInfos, new JLocalRef(firstLineSourceInfos, exceptionToThrow),
            new JNullLiteral(firstLineSourceInfos));
        finalTryBlock.addStmt(new JExpressionStatement(firstLineSourceInfos, assign));

        // Init all resources to null
        for (JStatement resInit : x.getResourcesDeclarations()) {
          JAsgOperation asgOp = (JAsgOperation) ((JExpressionStatement) resInit).getExpr();
          JLocal resourceLocal = ((JLocalRef) asgOp.getLhs()).getLocal();
          assign = new JAsgOperation(
              firstLineSourceInfos, new JLocalRef(firstLineSourceInfos, resourceLocal),
              new JNullLiteral(firstLineSourceInfos));
          finalTryBlock.addStmt(new JExpressionStatement(firstLineSourceInfos, assign));
        }

        // Inner try
        JBlock tryBlock = x.getTryBlock();
        JBlock finallyBlock = new JBlock(endOfTrySourceInfos);

        // Copy resources initialisation statements
        for (int i = x.getResourcesDeclarations().size() - 1; i >= 0; i--) {
          tryBlock.addStmt(0, x.getResourcesDeclarations().get(i));
        }

        // Save exception in catch block if any
        JLocal tryException = new JLocal(endOfTrySourceInfos,
            NamingTools.getNonSourceConflictingName("twrExceptionInTry"), throwableClass,
            JModifier.SYNTHETIC, currentMethodBody);
        JCatchBlock catchBlock = new JCatchBlock(endOfTrySourceInfos,
            Collections.singletonList(throwableClass), tryException);
        JAsgOperation save = new JAsgOperation(
            endOfTrySourceInfos, new JLocalRef(endOfTrySourceInfos, exceptionToThrow),
            new JLocalRef(endOfTrySourceInfos, tryException));

        catchBlock.addStmt(new JExpressionStatement(endOfTrySourceInfos, save));
        catchBlock.addStmt(new JThrowStatement(endOfTrySourceInfos,
            new JLocalRef(endOfTrySourceInfos, exceptionToThrow)));


        JTryStatement innerTry = new JTryStatement(endOfTrySourceInfos,
            Collections.<JStatement>emptyList(),
            tryBlock,
            Collections.singletonList(catchBlock),
            finallyBlock);

        JMethodId closeMethodId;
        JMethodId addSuppressedMethodId;
        try {
          // Lookup AutoCloseable.close() method
          JInterface autoCloseableInterface =
              Jack.getSession().getPhantomLookup().getInterface(AUTO_CLOSEABLE_SIGNATURE);
          closeMethodId = autoCloseableInterface.getMethodId(
              CLOSE_METHOD_NAME, Collections.<JType>emptyList(), MethodKind.INSTANCE_VIRTUAL);

          // Lookup Throwable.addSuppressed(Throwable t) method
          addSuppressedMethodId = throwableClass.getMethodId(ADD_SUPPRESSED_METHOD_NAME,
              Collections.singletonList(throwableClass), MethodKind.INSTANCE_VIRTUAL);
        } catch (JMethodLookupException e) {
          TransformationException transformationException =
              new TransformationException(new MissingJavaSupportException(JavaVersion.JAVA_7, e));
          Jack.getSession().getReporter().report(Severity.FATAL, transformationException);
          throw new JackAbortException(transformationException);
        }

        // Fill finally block
        for (int i = x.getResourcesDeclarations().size() - 1; i >= 0; i--) {
          // Try to close resources and handle exception suppression
          JStatement resInit = x.getResourcesDeclarations().get(i);
          JAsgOperation asgOp = (JAsgOperation) ((JExpressionStatement) resInit).getExpr();
          JLocal resourceLocal = ((JLocalRef) asgOp.getLhs()).getLocal();

          // If resource != null ...
          JNeqOperation isNotNull = new JNeqOperation(
              endOfTrySourceInfos, new JLocalRef(endOfTrySourceInfos, resourceLocal),
              new JNullLiteral(endOfTrySourceInfos));

          // ... close it
          JMethodCall closeCall = new JMethodCall(endOfTrySourceInfos,
              new JLocalRef(endOfTrySourceInfos, resourceLocal),
              (JClassOrInterface) resourceLocal.getType(),
              closeMethodId,
              JPrimitiveTypeEnum.VOID.getType(),
              true);

          JBlock thenBlock = new JBlock(endOfTrySourceInfos);
          thenBlock.addStmt(new JExpressionStatement(endOfTrySourceInfos, closeCall));
          JIfStatement ifStmt = new JIfStatement(endOfTrySourceInfos, isNotNull, thenBlock, null);

          // Try to catch exception triggered by close()
          JBlock tryBlockAroundClose = new JBlock(endOfTrySourceInfos);
          JLocal exceptionThrownByClose = new JLocal(endOfTrySourceInfos,
              NamingTools.getNonSourceConflictingName("twrExceptionThrownByClose_" + i),
              throwableClass, JModifier.SYNTHETIC, currentMethodBody);
          catchBlock = new JCatchBlock(endOfTrySourceInfos,
              Collections.singletonList(throwableClass), exceptionThrownByClose);
          tryBlockAroundClose.addStmt(ifStmt);

          JTryStatement tryClose = new JTryStatement(endOfTrySourceInfos,
              Collections.<JStatement>emptyList(),
              tryBlockAroundClose,
              Collections.<JCatchBlock>singletonList(catchBlock),
              null);

          finallyBlock.addStmt(tryClose);

          // If exceptionToThrow == null ...
          JEqOperation isNull = new JEqOperation(
              endOfTrySourceInfos, new JLocalRef(endOfTrySourceInfos, exceptionToThrow),
              new JNullLiteral(endOfTrySourceInfos));

          // ... then make it the exception thrown by close() ...
          thenBlock = new JBlock(endOfTrySourceInfos);
          asgOp = new JAsgOperation(
              endOfTrySourceInfos, new JLocalRef(endOfTrySourceInfos, exceptionToThrow),
              new JLocalRef(endOfTrySourceInfos, exceptionThrownByClose));
          thenBlock.addStmt(new JExpressionStatement(endOfTrySourceInfos, asgOp));

          // ... else add exception thrown by close() to the list of suppressed exceptions
          JBlock callSuppressBlock = new JBlock(endOfTrySourceInfos);
          JNeqOperation ifExceptionsDiffer = new JNeqOperation(
              endOfTrySourceInfos, new JLocalRef(endOfTrySourceInfos, exceptionToThrow),
              new JLocalRef(endOfTrySourceInfos, exceptionThrownByClose));
          JIfStatement elseIf =
              new JIfStatement(endOfTrySourceInfos, ifExceptionsDiffer, callSuppressBlock, null);

          JMethodCall addSuppressCall = new JMethodCall(endOfTrySourceInfos,
              new JLocalRef(endOfTrySourceInfos, exceptionToThrow),
              throwableClass,
              addSuppressedMethodId,
              JPrimitiveTypeEnum.VOID.getType(),
              closeMethodId.canBeVirtual());
          addSuppressCall.addArg(new JLocalRef(endOfTrySourceInfos, exceptionThrownByClose));
          callSuppressBlock.addStmt(new JExpressionStatement(endOfTrySourceInfos, addSuppressCall));

          ifStmt = new JIfStatement(endOfTrySourceInfos, isNull, thenBlock, elseIf);

          catchBlock.addStmt(ifStmt);
        }

        // Throw an exception if any
        JThrowStatement throwStmt = new JThrowStatement(
            endOfTrySourceInfos, new JLocalRef(endOfTrySourceInfos, exceptionToThrow));
        JNeqOperation ifNotNull = new JNeqOperation(
            endOfTrySourceInfos, new JLocalRef(endOfTrySourceInfos, exceptionToThrow),
            new JNullLiteral(endOfTrySourceInfos));
        JIfStatement ifExceptionToThrow =
            new JIfStatement(endOfTrySourceInfos, ifNotNull, throwStmt, null);
        finallyBlock.addStmt(ifExceptionToThrow);

        finalTryBlock.addStmt(innerTry);

        // Replace try block by the new one and keep existing catch and finally blocks
        request.append(new Replace(x.getTryBlock(), finalTryBlock));
      }

      x.setResourcesDeclarations(Collections.<JStatement>emptyList());
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    JDefinedClassOrInterface enclosingType = method.getEnclosingType();
    if (enclosingType.isExternal() || method.isNative() || method.isAbstract()
        || !filter.accept(this.getClass(), method)) {
      return;
    }

    TransformationRequest request = new TransformationRequest(method);
    new Visitor(method, new LocalVarCreator(method, "$twr"), request).accept(method);
    request.commit();
  }

}
