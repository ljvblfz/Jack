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

package com.android.jack.transformations.ast;

import com.android.jack.Options;
import com.android.jack.ir.SourceInfo;
import com.android.jack.ir.SourceOrigin;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JCatchBlock;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassLiteral;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JLock;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JSynchronizedBlock;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.ir.ast.JTryStatement;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JUnlock;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.jack.transformations.LocalVarCreator;
import com.android.jack.transformations.request.AppendBefore;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.BooleanPropertyId;

import java.util.Collections;

import javax.annotation.Nonnull;

/**
 * Transform {@link JSynchronizedBlock} and synchronized flag of method into try/finally with
 * {@link JLock} and {@link JUnlock} statement.
 */
@Description("Transform synchronization into try/finally with jlock/junlock statement.")
@Name("SynchronizeTransformer")
@Constraint(need = {NoImplicitBlock.class, OriginalNames.class})
@Transform(remove = {JSynchronizedBlock.class, ThreeAddressCodeForm.class}, add = {JBlock.class,
    JTryStatement.class,
    JLock.class,
    JUnlock.class,
    JLocalRef.class,
    JAsgOperation.NonReusedAsg.class,
    JClassLiteral.class,
    JThisRef.class,
    JExpressionStatement.class})
@Use(LocalVarCreator.class)
@HasKeyId
public class SynchronizeTransformer implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  @Nonnull
  public static final BooleanPropertyId REUSE_SYNC_VARIABLE = BooleanPropertyId.create(
      "jack.transformation.reusesyncvariable",
      "Reduce the 'get class' usage in static synchronized methods by reusing a local variable")
      .addDefaultValue(Boolean.TRUE);

  private final boolean reuseSyncVariable = ThreadConfig.get(REUSE_SYNC_VARIABLE).booleanValue();

  private class Visitor extends JVisitor {

    @Nonnull
    private final TransformationRequest tr;
    @Nonnull
    private final JSession session;
    @Nonnull
    private final LocalVarCreator lvCreator;

    public Visitor(@Nonnull TransformationRequest tr, @Nonnull JSession session,
        @Nonnull LocalVarCreator lvCreator) {
      this.tr = tr;
      this.lvCreator = lvCreator;
      this.session = session;
    }

    @Override
    public boolean visit(@Nonnull JMethodBody methodBody) {
      JMethod enclosingMethod = methodBody.getMethod();

      if (enclosingMethod.isSynchronized()) {
        JBlock bodyBlock = methodBody.getBlock();

        JTryStatement tryStmt = getTryFinally(SourceOrigin.UNKNOWN, bodyBlock);

        JType enclosingType = enclosingMethod.getEnclosingType();
        JExpression lockExpr = null;
        JExpression unlockExpr = null;

        JBlock newBodyBlock = new JBlock(methodBody.getSourceInfo());

        if (reuseSyncVariable && enclosingMethod.isStatic()) {
          JLocal syncVar = lvCreator.createTempLocal(enclosingType, SourceOrigin.UNKNOWN, tr);
          JExpression syncVarValue = new JClassLiteral(
              SourceOrigin.UNKNOWN, enclosingType, getJLClass());
          JAsgOperation asg =
              new JAsgOperation(SourceOrigin.UNKNOWN, new JLocalRef(SourceOrigin.UNKNOWN, syncVar),
                  syncVarValue);
          newBodyBlock.addStmt(asg.makeStatement());
          lockExpr = new JLocalRef(SourceOrigin.UNKNOWN, syncVar);
          unlockExpr = new JLocalRef(SourceOrigin.UNKNOWN, syncVar);
        } else {
          if (enclosingMethod.isStatic()) {
            lockExpr = new JClassLiteral(
                SourceOrigin.UNKNOWN, enclosingType, getJLClass());
            unlockExpr = new JClassLiteral(
                SourceOrigin.UNKNOWN, enclosingType, getJLClass());
          } else {
            assert enclosingType instanceof JDefinedClass;
            JVariable thisVar = enclosingMethod.getThis();
            assert thisVar != null;
            lockExpr = new JThisRef(SourceOrigin.UNKNOWN, thisVar);
            unlockExpr = new JThisRef(SourceOrigin.UNKNOWN, thisVar);
          }
        }

        newBodyBlock.addStmt(new JLock(SourceOrigin.UNKNOWN, lockExpr));
        newBodyBlock.addStmt(tryStmt);

        JBlock finallyBlock = tryStmt.getFinallyBlock();
        assert finallyBlock != null;
        finallyBlock.addStmt(new JUnlock(SourceOrigin.UNKNOWN, unlockExpr));

        tr.append(new Replace(bodyBlock, newBodyBlock));
      }

      return super.visit(methodBody);
    }

    @Override
    public boolean visit(@Nonnull JSynchronizedBlock syncBlock) {
      SourceInfo srcInfo = syncBlock.getSourceInfo();
      JBlock bodyBlock = syncBlock.getSynchronizedBlock();

      JTryStatement tryStmt = getTryFinally(srcInfo, bodyBlock);
      JExpression lockExpr = syncBlock.getLockExpr();
      JType lockExprType = lockExpr.getType();

      // $sync0 = lockExpr
      JLocal syncVar = lvCreator.createTempLocal(lockExprType, srcInfo, tr);
      JLocalRef asgLhs = new JLocalRef(srcInfo, syncVar);
      JAsgOperation asg = new JAsgOperation(srcInfo, asgLhs, lockExpr);

      JBlock finallyBlock = tryStmt.getFinallyBlock();
      assert finallyBlock != null;
      finallyBlock.addStmt(new JUnlock(SourceOrigin.UNKNOWN, new JLocalRef(srcInfo, syncVar)));

      tr.append(new AppendBefore(syncBlock, asg.makeStatement()));
      tr.append(new AppendBefore(syncBlock, new JLock(srcInfo, new JLocalRef(srcInfo, syncVar))));
      tr.append(new Replace(syncBlock, tryStmt));

      return super.visit(syncBlock);
    }

    @Nonnull
    private JTryStatement getTryFinally(@Nonnull SourceInfo mthSrcInfo, @Nonnull JBlock bodyBlock) {
      JBlock finallyBlock = new JBlock(mthSrcInfo);
      JTryStatement tryStmt = new JTryStatement(mthSrcInfo,
          Collections.<JStatement>emptyList(),
          bodyBlock,
          Collections.<JCatchBlock>emptyList(),
          finallyBlock);
      return tryStmt;
    }

    @Nonnull
    private JClass getJLClass() {
      return session.getPhantomLookup().getClass(CommonTypes.JAVA_LANG_CLASS);
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    JDefinedClassOrInterface enclosingType = method.getEnclosingType();
    if (enclosingType.isExternal() || method.isNative() || method.isAbstract()
        || !filter.accept(this.getClass(), method)) {
      return;
    }

    TransformationRequest tr = new TransformationRequest(method);
    LocalVarCreator lvCreator = new LocalVarCreator(method, "sync");
    Visitor visitor = new Visitor(tr, enclosingType.getSession(), lvCreator);
    visitor.accept(method);
    tr.commit();
  }

}
