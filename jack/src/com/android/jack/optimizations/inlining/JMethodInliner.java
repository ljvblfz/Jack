/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.optimizations.inlining;

import com.google.common.collect.Maps;

import com.android.jack.analysis.UseDefsMarker;
import com.android.jack.ir.ast.JAbstractMethodBody;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JLabel;
import com.android.jack.ir.ast.JLabeledStatement;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.optimizations.Optimizations;
import com.android.jack.transformations.LocalVarCreator;
import com.android.jack.transformations.request.AddJLocalInMethodBody;
import com.android.jack.transformations.request.AppendBefore;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.ControlFlowHelper;
import com.android.sched.item.Description;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.ExclusiveAccess;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;

import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A utility that inlines the {@link JMethodBody} of a {@link JMethod} into the {@link JMethodCall}
 * site.
 *
 * <p>
 * This class makes no effort to check the correctness, verification nor benefit of inlining the
 * given method. The decision should be made by the caller of this class.
 */
@Description("Performs method inlining")
@Constraint(need = {InlineMarker.class, ThreeAddressCodeForm.class},
    no = {UseDefsMarker.class})
@Transform(remove = {InlineMarker.class},
    add = {JLocal.class, JLocalRef.class, JAsgOperation.NonReusedAsg.class,
           JExpressionStatement.class, JLabeledStatement.class})
@Use({InlineCloneStatementVisitor.class})
@Support(Optimizations.InlineAnnotatedMethods.class)
@ExclusiveAccess(JSession.class)
@Synchronized
public class JMethodInliner implements RunnableSchedulable<JMethod> {
  /**
   * This class contains all the information we need to gather before performing inlining. All the
   * members of this class should contains pointers to nodes in the existing AST.
   */
  private static class CandidateInfo {

    // The enclosing function that contains the call that will be inlined.
    @Nonnull
    private final JMethod callSiteMethod;
    @Nonnull
    private final JMethodBody callSiteMethodBody;

    // The call site that will be removed and replaced by the target's body.
    @Nonnull
    private final JMethodCall callSite;

    // The statement that contains callSite.
    @Nonnull
    private final JExpressionStatement callSiteStmt;

    // The target of the function that contains the method of that callSite will be replaced with.
    @Nonnull
    private final JMethod target;

    private CandidateInfo(@Nonnull JMethod callSiteMethod, @Nonnull JMethodCall callSite,
        @Nonnull JMethod target) {
      this.callSiteMethod = callSiteMethod;
      JAbstractMethodBody callSiteMethodBody = callSiteMethod.getBody();
      assert callSiteMethodBody != null;
      this.callSiteMethodBody = (JMethodBody) callSiteMethodBody;
      callSiteStmt = callSite.getParent(JExpressionStatement.class);
      this.callSite = callSite;
      this.target = target;
    }
  }

  /**
   * This class contains all the information related to the prologue portion of the function call.
   * It provides information on how parameters are passed to the target that would be inlined.
   */
  private static class PrologueInfo {
    // Maps the parameter of the target function to the newly created temporary locals that will
    // store the arguments.
    @Nonnull
    private final Map<JParameter, JLocal> parameterMap = Maps.newHashMap();

    // The target function's 'this' variable. Note that at the call site, it does not have to be
    // a JThis unless the call site and the target is the same class.
    @CheckForNull
    private final JVariable targetThis;

    private PrologueInfo(@Nonnull CandidateInfo candidate) {
      JExpression instance = candidate.callSite.getInstance();
      this.targetThis = instance == null ? null : ((JVariableRef) instance).getTarget();
    }
  }

  /**
   * This class contains all the information related to epilogue portion of the function call. It
   * provides information on where the return value should be stored as well as the label that the
   * body should branch on JReturn statements.
   */
  private static class EpilogueInfo {
    @CheckForNull
    private final JLocal returnLocal;
    @CheckForNull
    private JLabeledStatement returnLabel = null;

    private EpilogueInfo(@Nonnull CandidateInfo candidate) {
      if (candidate.callSiteStmt.getExpr() instanceof JAsgOperation) {
        JAsgOperation assign = (JAsgOperation) candidate.callSiteStmt.getExpr();
        returnLocal = ((JLocalRef) assign.getLhs()).getLocal();
      } else {
        returnLocal = null;
      }
    }
  }

  private static void inline(@Nonnull JMethod callSiteMethod, @Nonnull JMethodCall callSite,
      @Nonnull JMethod target, @Nonnull TransformationRequest tr, @Nonnull LocalVarCreator lvc) {
    // This is the core of the inlining algorithm.
    //
    // We first gather all the information we need starting by filling in all the information
    // about the call-site and call target. We then use this information to fill in the prologue
    // and epilogue information.
    CandidateInfo candidate = new CandidateInfo(callSiteMethod, callSite, target);
    PrologueInfo prologue = new PrologueInfo(candidate);
    EpilogueInfo epilogue = new EpilogueInfo(candidate);

    // For readability, the algorithm will be mostly state-free. It will make sure of three helper
    // static methods passes information down the pipeline.

    // 1. Prepare the returns of the function and store the branch targets into the epilogue info.
    prepareEpilogue(candidate, epilogue, tr);

    // 2. Prepare the beginning of the function by initializing the temporary variables that would
    // hold the function arguments and instance object references.
    preparePrologue(candidate, prologue, tr, lvc);

    // 3. Now that we know where the values of where the parameters are stored as well as where
    // we should be branching in cases of return statements, we can clone the target method's
    // body, modify the variable references and embed it into the function call.
    inlineBody(candidate, prologue, epilogue, tr);
  }

  private static void preparePrologue(@Nonnull CandidateInfo candidate,
      @Nonnull PrologueInfo prologue, @Nonnull TransformationRequest tr,
      @Nonnull LocalVarCreator lvc) {
    SourceInfo src = candidate.callSite.getSourceInfo();
    List<JParameter> params = candidate.target.getParams();
    for (int i = 0; i < candidate.callSite.getArgs().size(); i++) {
      JExpression arg = candidate.callSite.getArgs().get(i);
      JLocal local = lvc.createTempLocal(params.get(i).getType(), src, tr);

      prologue.parameterMap.put(params.get(i), local);
      JMethodBody enclosingBody = candidate.callSiteMethodBody;
      assert enclosingBody != null;
      tr.append(new AddJLocalInMethodBody(local, enclosingBody));
      tr.append(new AppendBefore(candidate.callSiteStmt,
          new JExpressionStatement(src, new JAsgOperation(src, local.makeRef(src), arg))));
    }
  }

  private static void inlineBody(@Nonnull CandidateInfo candidate, @Nonnull PrologueInfo prologue,
      @Nonnull EpilogueInfo epilogue, @Nonnull TransformationRequest tr) {
    JMethodBody body = (JMethodBody) candidate.target.getBody();
    JLabeledStatement returnLabel = epilogue.returnLabel;
    assert returnLabel != null;
    assert body != null;
    InlineCloneStatementVisitor cloner = new InlineCloneStatementVisitor(tr,
        candidate.callSiteMethod, candidate.callSiteStmt.getJCatchBlocks(), epilogue.returnLocal,
        returnLabel, prologue.targetThis, prologue.parameterMap);
    JBlock newBlock = cloner.cloneMethodBody(body);
    tr.append(new Replace(candidate.callSiteStmt, newBlock));
  }

  private static void prepareEpilogue(@Nonnull CandidateInfo candidate,
      @Nonnull EpilogueInfo epilogue, @Nonnull TransformationRequest tr) {
    SourceInfo src = candidate.callSite.getSourceInfo();
    JStatement next = ControlFlowHelper.getNextStatement(candidate.callSiteStmt);
    assert next != null;
    JBlock nextBlock = new JBlock(src);
    nextBlock.addStmt(next);
    epilogue.returnLabel =
        new JLabeledStatement(src, new JLabel(src, ".inline_return_label"), nextBlock);
    tr.append(new Replace(next, epilogue.returnLabel));
  }

  private static class Visitor extends JVisitor {
    @Nonnull
    private final JMethod enclosingMethod;
    @Nonnull
    private final TransformationRequest tr;
    @Nonnull
    private final LocalVarCreator lvc;

    private boolean inlined = false;

    private Visitor(@Nonnull JMethod enclosingMethod, @Nonnull TransformationRequest tr) {
      this.enclosingMethod = enclosingMethod;
      this.tr = tr;
      this.lvc = new LocalVarCreator(enclosingMethod, ".inline_tmp");
    }

    @Override
    public void endVisit(@Nonnull JMethodCall jmc) {
      InlineMarker marker = jmc.removeMarker(InlineMarker.class);
      if (marker == null) {
        return;
      }
      assert enclosingMethod != null;
      assert lvc != null;
      // This assert make sure we never run into inlining loop.
      assert enclosingMethod != marker.getTarget();
      inline(enclosingMethod, jmc, marker.getTarget(), tr, lvc);
      inlined = true;
    }
  }

  @Override
  public void run(JMethod jm) {
    if (jm.isNative() || jm.isAbstract()) {
      return;
    }

    Visitor v = null;
    do {
      TransformationRequest tr = new TransformationRequest(jm);
      v = new Visitor(jm, tr);
      v.accept(jm);
      tr.commit();
    } while (v.inlined);
  }
}
