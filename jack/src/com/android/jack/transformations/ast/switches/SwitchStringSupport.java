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

package com.android.jack.transformations.ast.switches;

import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.ir.ast.JAbstractStringLiteral;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JBreakStatement;
import com.android.jack.ir.ast.JCaseStatement;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JGoto;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JLabel;
import com.android.jack.ir.ast.JLabeledStatement;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.lookup.JPhantomLookup;
import com.android.jack.scheduling.feature.SourceVersion7;
import com.android.jack.transformations.LocalVarCreator;
import com.android.jack.transformations.request.AppendBefore;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.ControlFlowHelper;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;
import com.android.sched.util.config.ThreadConfig;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * This {@link RunnableSchedulable} transforms switches using strings into "if" statements.
 */
@Description("Transforms switches using strings into \"if\" statements")
@Constraint(need = {JSwitchStatement.class, JCaseStatement.class}, no = JBreakStatement.class)
@Transform(add = {JLabeledStatement.class, JLabel.class, JBlock.class, JMethodCall.class,
    JGoto.class, JIfStatement.class, JLocalRef.class, JAsgOperation.class})
@Use(value = {LocalVarCreator.class})
@Support(SourceVersion7.class)
public class SwitchStringSupport implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  @Nonnull
  private final JMethodId equalsMethodId;

  {
    JSession session = Jack.getSession();
    JPhantomLookup lookup = session.getPhantomLookup();
    JClass jlo = lookup.getClass(CommonTypes.JAVA_LANG_OBJECT);
    JClass jls = lookup.getClass(CommonTypes.JAVA_LANG_STRING);
    equalsMethodId =
        jls.getMethodId("equals", Collections.singletonList((JType) jlo),
            MethodKind.INSTANCE_VIRTUAL);

  }

  private class Visitor extends JVisitor {

    @Nonnull
    private final TransformationRequest tr;

    @Nonnull
    private final LocalVarCreator localVarCreator;

    @Nonnegative
    private int switchCount = 0;

    public Visitor(@Nonnull TransformationRequest tr, @Nonnull JMethod method) {
      this.tr = tr;
      localVarCreator = new LocalVarCreator(method, "switch_var_");
    }


    @Override
    public boolean visit(@Nonnull JStatement stmt) {
      return super.visit(stmt);
    }

    @Override
    public boolean visit(@Nonnull JSwitchStatement switchStmt) {
      if (switchHasString(switchStmt)) {
        assert allCasesAreStrings(switchStmt);
        SourceInfo dbgInfo = switchStmt.getSourceInfo();

        JLocal tempLocal =
            localVarCreator.createTempLocal(switchStmt.getExpr().getType(), dbgInfo, tr);

        JAsgOperation asg =
            new JAsgOperation(dbgInfo, new JLocalRef(dbgInfo, tempLocal), switchStmt.getExpr());

        tr.append(new AppendBefore(switchStmt, asg.makeStatement()));

        for (JCaseStatement caseStmt : switchStmt.getCases()) {
          JAbstractStringLiteral caseExpr = (JAbstractStringLiteral) caseStmt.getExpr();
          assert caseExpr != null;
          caseExpr.setSourceInfo(dbgInfo);

          JLabeledStatement labelStmt =
              new JLabeledStatement(dbgInfo, new JLabel(dbgInfo, "label_"
                  + caseExpr.getValue() + "_" + switchCount), new JBlock(
                      dbgInfo));
          tr.append(new Replace(caseStmt, labelStmt));

          JMethodCall equalsCall =
              new JMethodCall(dbgInfo,
                  new JLocalRef(dbgInfo, tempLocal), (JClassOrInterface) switchStmt
                      .getExpr().getType(), equalsMethodId, JPrimitiveTypeEnum.BOOLEAN.getType(),
                  true);
          equalsCall.addArg(caseExpr);
          JBlock thenBlock = new JBlock(dbgInfo);
          thenBlock.addStmt(new JGoto(dbgInfo, labelStmt));
          JIfStatement ifStmt = new JIfStatement(dbgInfo, equalsCall, thenBlock, null);
          tr.append(new AppendBefore(switchStmt, ifStmt));
        }

        JCaseStatement defaultCase = switchStmt.getDefaultCase();
        JLabeledStatement defaultLabelStmt =
            new JLabeledStatement(dbgInfo, new JLabel(SourceInfo.UNKNOWN, "label_default_"
                + switchCount), new JBlock(SourceInfo.UNKNOWN));
        tr.append(new AppendBefore(switchStmt, new JGoto(dbgInfo, defaultLabelStmt)));

        if (defaultCase != null) {
          tr.append(new Replace(defaultCase, defaultLabelStmt));
        } else {
          JStatement nextStatement = ControlFlowHelper.getNextStatement(switchStmt);
          assert nextStatement != null;

          tr.append(new AppendBefore(nextStatement, defaultLabelStmt));
        }

        tr.append(new Replace(switchStmt, switchStmt.getBody()));

        switchCount++;
      }

      return super.visit(switchStmt);
    }

    private boolean switchHasString(@Nonnull JSwitchStatement switchStmt) {
      List<JCaseStatement> cases = switchStmt.getCases();
      return cases.size() > 0 && (cases.get(0).getExpr() instanceof JAbstractStringLiteral);
    }

    private boolean allCasesAreStrings(@Nonnull JSwitchStatement switchStmt) {
      List<JCaseStatement> cases = switchStmt.getCases();
      for (JCaseStatement caseStmt : cases) {
        JExpression caseExpr = caseStmt.getExpr();
        if (!(caseExpr instanceof JAbstractStringLiteral)) {
          return false;
        }
      }
      return cases.size() > 0;
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    TransformationRequest tr = new TransformationRequest(method);
    Visitor visitor = new Visitor(tr, method);
    visitor.accept(method);
    tr.commit();
  }
}
