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

package com.android.jack.cfg;

import com.android.jack.JackEventType;
import com.android.jack.Options;
import com.android.jack.cfg.ForwardBranchResolver.ForwardBranchKind;
import com.android.jack.ir.SourceInfo;
import com.android.jack.ir.SourceOrigin;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JBreakStatement;
import com.android.jack.ir.ast.JCaseStatement;
import com.android.jack.ir.ast.JCatchBlock;
import com.android.jack.ir.ast.JContinueStatement;
import com.android.jack.ir.ast.JExceptionRuntimeValue;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JFieldInitializer;
import com.android.jack.ir.ast.JGoto;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JIntegralConstant32;
import com.android.jack.ir.ast.JLabeledStatement;
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JLock;
import com.android.jack.ir.ast.JLoop;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JStatementList;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JSynchronizedBlock;
import com.android.jack.ir.ast.JThrowStatement;
import com.android.jack.ir.ast.JTryStatement;
import com.android.jack.ir.ast.JUnlock;
import com.android.jack.ir.ast.JValueLiteral;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.transformations.ast.RefAsStatement;
import com.android.jack.transformations.ast.switches.UselessSwitches;
import com.android.jack.transformations.request.AppendStatement;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.ControlFlowHelper;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Protect;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.With;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.Event;
import com.android.sched.util.log.TracerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Builds a {@code ControlFlowGraph} instance from a {@code JMethod}.
 */
@Description("Builds a ControlFlowGraph instance from a JMethod.")
@Name("CfgBuilder")
@Constraint(need = {ThreeAddressCodeForm.class, JExceptionRuntimeValue.class}, no = {JLoop.class,
    JBreakStatement.class,
    JContinueStatement.class,
    RefAsStatement.class,
    JSynchronizedBlock.class,
    JTryStatement.class,
    JFieldInitializer.class,
    UselessSwitches.class})
@Protect(
    add = JNode.class, remove = JNode.class, unprotect = @With(remove = ControlFlowGraph.class))
@Transform(add = {ControlFlowGraph.class, JReturnStatement.class, BasicBlockMarker.class})
public class CfgBuilder implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  private static class BuilderVisitor extends JVisitor {

    @CheckForNull
    private ControlFlowGraph cfg = null;
    @Nonnull
    private List<JStatement> currentStmts = new LinkedList<JStatement>();

    /**
     * Represents statements which are not added into the control flow graph. This list will be used
     * when a cfg basic block will be created to keep a mapping between these statements and the cfg
     * basic block.
     */
    @Nonnull
    private List<JStatement> virtualStmts = new LinkedList<JStatement>();

    @Nonnull
    private final ForwardBranchResolver forwardBranchResolver = new ForwardBranchResolver();

    @Nonnull
    private List<JCatchBlock> previousCatchBlock = new ArrayList<JCatchBlock>();

    private static class JCaseStatementComparator
        implements Comparator<JCaseStatement>, Serializable {
      private static final long serialVersionUID = 1L;

      @Override
      public int compare(JCaseStatement case1, JCaseStatement case2) {
        JLiteral lit1 = case1.getExpr();
        JLiteral lit2 = case2.getExpr();

        assert lit1 instanceof JValueLiteral;
        assert lit2 instanceof JValueLiteral;

        int lit1Value = ((JIntegralConstant32) lit1).getIntValue();
        int lit2Value = ((JIntegralConstant32) lit2).getIntValue();

        return (lit1Value < lit2Value ? -1 : lit1Value == lit2Value ? 0 : 1);
      }
    }

    public BuilderVisitor() {
    }

    @Override
    public boolean visit(@Nonnull JMethodBody methodBody) {
      cfg = new ControlFlowGraph(methodBody.getMethod());
      return true;
    }

    @Override
    public void endVisit(@Nonnull JMethodBody methodBody) {
      assert cfg != null;

      if (cfg.getNodes().isEmpty() || (!virtualStmts.isEmpty()
          && methodBody.getMethod().getType() == JPrimitiveTypeEnum.VOID.getType())) {
        // Generate implicit return if there is no node or if virtualStmts is not empty (indeed, it
        // could be targeted by a branch instruction). Take virtualStmts into account only if method
        // return type is VOID, otherwise it is dead code, don't take care about it.
        generateImplicitReturn(methodBody);
      }

      // Generate all edges by resolving forward branch.
      forwardBranchResolver.resolve();
    }


    @Override
    public boolean visit(@Nonnull JCatchBlock catchBlock) {
      super.visit(catchBlock);
      assert currentStmts.isEmpty();
      List<JStatement> catchStmts = catchBlock.getStatements();
      assert catchStmts.size() >= 1;
      // Assign of exception belong to the catch block
      accept(catchStmts.get(0));

      assert cfg != null;
      CatchBasicBlock catchBasicBlock =
          new CatchBasicBlock(cfg, currentStmts, catchBlock.getCatchTypes(),
              catchBlock.getCatchVar());
      setBlockOfStatement(catchBasicBlock);

      forwardBranchResolver.addForwardBranch(ForwardBranchKind.BRANCH, catchBasicBlock,
          getNextStatement(getConcreteStatement(catchBlock)));

      accept(catchStmts.subList(1, catchStmts.size()));

      return false;
    }

    @Override
    public void endVisit(@Nonnull JCatchBlock block) {
      assert cfg != null;
      NormalBasicBlock endOfBlock = new NormalBasicBlock(cfg, currentStmts);
      setBlockOfStatement(endOfBlock);

      forwardBranchResolver.addForwardBranch(ForwardBranchKind.BRANCH, endOfBlock,
          getNextStatement(block));
    }

    @Override
    public void endVisit(@Nonnull JBlock block) {
      JNode parent = block.getParent();
      if (!currentStmts.isEmpty() || parent instanceof JLabeledStatement
          || parent instanceof JIfStatement || !block.getJCatchBlocks().isEmpty()) {
        // A basic block must be created if there are pending statements or if the block is the
        // target of a branch (explicitly through label or implicitly through if statement) or
        // if the block must reach a specific program point as for a try block which must branch
        // after catches.
        assert cfg != null;
        BasicBlock endOfBlock = new NormalBasicBlock(cfg, currentStmts);
        setBlockOfStatement(endOfBlock);

        forwardBranchResolver.addForwardBranch(
            ForwardBranchKind.BRANCH, endOfBlock, getNextStatement(block));
      }
    }

    @Override
    public boolean visit(@Nonnull JStatement statement) {
      if (!currentStmts.isEmpty() && !statement.getJCatchBlocks().equals(previousCatchBlock)) {
        previousCatchBlock = statement.getJCatchBlocks();
        assert cfg != null;
        BasicBlock tryBasicBlock = new NormalBasicBlock(cfg, currentStmts);
        setBlockOfStatement(tryBasicBlock);
        forwardBranchResolver.addForwardBranch(ForwardBranchKind.BRANCH, tryBasicBlock,
           statement instanceof JBlock ? getConcreteStatement((JBlock) statement) : statement);
      }


      if (statement instanceof JLabeledStatement || statement instanceof JBlock
          || statement instanceof JCatchBlock) {
        virtualStmts.add(statement);
      } else {
        currentStmts.add(statement);
      }

      return super.visit(statement);
    }

    @Override
    public boolean visit(@Nonnull JIfStatement ifStmt) {
      super.visit(ifStmt);

      assert cfg != null;
      BasicBlock condBlock = new ConditionalBasicBlock(cfg, currentStmts);
      setBlockOfStatement(condBlock);

      assert ifStmt.getThenStmt() != null;

      forwardBranchResolver.addForwardBranch(ForwardBranchKind.IF_THEN, condBlock,
          ifStmt.getThenStmt());
      accept(ifStmt.getThenStmt());

      if (ifStmt.getElseStmt() != null) {
        forwardBranchResolver.addForwardBranch(ForwardBranchKind.IF_ELSE, condBlock,
            ifStmt.getElseStmt());
        accept(ifStmt.getElseStmt());
      } else {
        forwardBranchResolver.addForwardBranch(ForwardBranchKind.IF_ELSE, condBlock,
            getNextStatement(ifStmt));
      }

      return false;
    }

    @Override
    public boolean visit(@Nonnull JReturnStatement retStmt) {
      super.visit(retStmt);

      assert cfg != null;
      BasicBlock returnBlock = new ReturnBasicBlock(cfg, currentStmts);
      setBlockOfStatement(returnBlock);

      return false;
    }

    @Override
    public boolean visit(@Nonnull JGoto gotoStmt) {
      super.visit(gotoStmt);

      assert cfg != null;
      NormalBasicBlock branchBlock = new NormalBasicBlock(cfg, currentStmts);
      setBlockOfStatement(branchBlock);

      JLabeledStatement labeledStatement = gotoStmt.getTargetBlock();
      BasicBlockMarker bbm = labeledStatement.getMarker(BasicBlockMarker.class);

      if (bbm == null || bbm.getBasicBlock() == null) {
        forwardBranchResolver.addForwardBranch(ForwardBranchKind.BRANCH, branchBlock,
            labeledStatement.getBody());
      } else {
        branchBlock.setTarget(bbm.getBasicBlock());
      }

      return false;
    }

    @Override
    public boolean visit(@Nonnull JLabeledStatement labeledStatement) {

      if (!currentStmts.isEmpty()) {
        assert cfg != null;
        BasicBlock normalBasicBlock = new NormalBasicBlock(cfg, currentStmts);
        setBlockOfStatement(normalBasicBlock);

        forwardBranchResolver.addForwardBranch(ForwardBranchKind.BRANCH, normalBasicBlock,
            getConcreteStatement((JBlock) labeledStatement.getBody()));
      }

      super.visit(labeledStatement);

      return true;
    }

    @Override
    public boolean visit(@Nonnull JSwitchStatement switchStatement) {
      super.visit(switchStatement);

      assert cfg != null;
      BasicBlock switchBlock = new SwitchBasicBlock(cfg, currentStmts);
      setBlockOfStatement(switchBlock);

      List<JCaseStatement> cases = switchStatement.getCases();
      Collections.sort(cases, new JCaseStatementComparator());
      for (JStatement stmt : cases) {
        forwardBranchResolver.addForwardBranch(ForwardBranchKind.SWITCH_CASE, switchBlock, stmt);
      }

      JCaseStatement defaultCase = switchStatement.getDefaultCase();
      if (defaultCase != null) {
        forwardBranchResolver.addForwardBranch(ForwardBranchKind.SWITCH_DEFAULT, switchBlock,
            defaultCase);
      } else {
        forwardBranchResolver.addForwardBranch(ForwardBranchKind.SWITCH_DEFAULT, switchBlock,
            getNextStatement(switchStatement));
      }

      return true;
    }

    @Override
    public boolean visit(@Nonnull JCaseStatement caseStatement) {
      if (!currentStmts.isEmpty()) {
        assert cfg != null;
        BasicBlock caseBlock = new NormalBasicBlock(cfg, currentStmts);
        setBlockOfStatement(caseBlock);

        forwardBranchResolver.addForwardBranch(ForwardBranchKind.BRANCH, caseBlock,
            caseStatement);
      }

      super.visit(caseStatement);

      return true;
    }


    @Override
    public boolean visit(@Nonnull JExpressionStatement exprStmt) {
      super.visit(exprStmt);

      JExpression expr = exprStmt.getExpr();

      if (expressionCanThrow(expr)) {
        buildCfgForPei(exprStmt);
      }

      return false;
    }

    @Override
    public boolean visit(@Nonnull JThrowStatement throwStmt) {
      super.visit(throwStmt);

      assert cfg != null;
      ThrowBasicBlock throwBlock = new ThrowBasicBlock(cfg, currentStmts);
      setBlockOfStatement(throwBlock);

      setExceptionEdges(throwBlock, throwStmt);

      return false;
    }

    @Override
    public boolean visit(@Nonnull JLock lockStmt) {
      super.visit(lockStmt);

      buildCfgForPei(lockStmt);

      return false;
    }

    @Override
    public boolean visit(@Nonnull JUnlock unlockStmt) {
      super.visit(unlockStmt);

      buildCfgForPei(unlockStmt);

      return false;
    }

    @Nonnull
    public ControlFlowGraph getCfg() {
      assert cfg != null;
      return cfg;
    }

    @Nonnull
    private JStatement generateImplicitReturn(@Nonnull JMethodBody methodBody) {
      assert currentStmts.isEmpty();
      JStatement stmt = null;

      if (methodBody.getMethod().getType() != JPrimitiveTypeEnum.VOID.getType()) {
        stmt = forwardBranchResolver.deadCodeStatement;
      } else {
        SourceInfo methodInfo = methodBody.getSourceInfo();
        stmt = new JReturnStatement(SourceOrigin.create(methodInfo.getEndLine(),
            methodInfo.getEndLine(), methodInfo.getFileName()), null);
        currentStmts.add(stmt);

        assert cfg != null;
        BasicBlock returnBlock = new ReturnBasicBlock(cfg, currentStmts);
        setBlockOfStatement(returnBlock);

        // Commit the transformation directly allows to generate only one time the implicit return,
        // since following needs will find it.
        TransformationRequest tr = new TransformationRequest(methodBody);
        tr.append(new AppendStatement(methodBody.getBlock(), stmt));
        tr.commit();
      }
      return stmt;
    }

    @Nonnull
    private JStatement getConcreteStatement(@Nonnull JStatementList block) {
      List<JStatement> statements = block.getStatements();

      if (statements.isEmpty()) {
        return (getNextStatement(block));
      }

      JStatement firstStmt = statements.get(0);
      if (firstStmt instanceof JBlock) {
        return getConcreteStatement((JBlock) firstStmt);
      } else if (firstStmt instanceof JLabeledStatement) {
        return (getConcreteStatement((JBlock) ((JLabeledStatement) firstStmt).getBody()));
      } else if (firstStmt instanceof JTryStatement) {
        return (getConcreteStatement(((JTryStatement) firstStmt).getTryBlock()));
      }

      return firstStmt;
    }

    @Nonnull
    private JStatement getNextStatement(@Nonnull JStatement statement) {
      JStatement nextStmt = ControlFlowHelper.getNextStatement(statement);
      if (nextStmt == null) {
         nextStmt = generateImplicitReturn(statement.getParent(JMethodBody.class));
      }
      return nextStmt;
    }

    private boolean expressionCanThrow(@Nonnull JExpression expression) {
      return (expression.canThrow() || (expression instanceof JBinaryOperation &&
          (expressionCanThrow(((JBinaryOperation) expression)
          .getLhs()) || expressionCanThrow(((JBinaryOperation) expression).getRhs()))));
    }

    private void setBlockOfStatement(@Nonnull BasicBlock bb) {
      for (JStatement statement : currentStmts) {
        statement.addMarker(new BasicBlockMarker(bb));
      }

      for (JStatement statement : virtualStmts) {
        statement.addMarker(new BasicBlockMarker(bb));
      }

      // First created block is the successor of the entry block.
      assert cfg != null;
      NormalBasicBlock entryNode = cfg.getEntryNode();
      if (entryNode.getSuccessors().isEmpty()) {
        entryNode.setTarget(bb);
      }

      currentStmts = new LinkedList<JStatement>();
      virtualStmts = new LinkedList<JStatement>();
    }

    private void buildCfgForPei(@Nonnull JStatement peiInst) {
      assert cfg != null;
      PeiBasicBlock peiBlock = new PeiBasicBlock(cfg, currentStmts);
      setBlockOfStatement(peiBlock);

      forwardBranchResolver.addForwardBranch(ForwardBranchKind.BRANCH, peiBlock,
          getNextStatement(peiInst));

      setExceptionEdges(peiBlock, peiInst);
    }


    private void setExceptionEdges(@Nonnull PeiBasicBlock peiBlock, @Nonnull JStatement peiInst) {

      for (JCatchBlock catchBlock : peiInst.getJCatchBlocks()) {
        forwardBranchResolver.addForwardBranch(ForwardBranchKind.EXCEPTION, peiBlock,
            catchBlock);
      }
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    BuilderVisitor cfgBuilder = new BuilderVisitor();
    cfgBuilder.accept(method);

    Event optEvent = TracerFactory.getTracer().start(JackEventType.REMOVE_DEAD_CODE);

    try {
      removeUnaccessibleNode(cfgBuilder.getCfg());
    } finally {
      optEvent.end();
    }

    method.addMarker(cfgBuilder.getCfg());
  }

  private void removeUnaccessibleNode(@Nonnull ControlFlowGraph cfg) {
    List<BasicBlock> nodes = cfg.getNodes();

    if (!nodes.isEmpty()) {
      List<BasicBlock> accessibleNodes = new ArrayList<BasicBlock>();

      BasicBlock entryNode = cfg.getEntryNode();
      assert entryNode != null;

      List<BasicBlock> workingList = new LinkedList<BasicBlock>();
      workingList.add(entryNode);

      // Do not use recursion to compute accessible node to avoid stack overflow on very big method.
      while (!workingList.isEmpty()) {
        BasicBlock currentBb = workingList.remove(0);

        if (accessibleNodes.contains(currentBb)) {
          continue;
        }

        if (currentBb.getStatements().isEmpty() &&
            currentBb != cfg.getEntryNode()) {
          assert currentBb instanceof NormalBasicBlock;
          BasicBlock newBlock = ((NormalBasicBlock) currentBb).getTarget();
          currentBb.replaceBy(newBlock);
        } else {
          accessibleNodes.add(currentBb);
        }

        assert !hasDeadCode(currentBb) : "JDeadCodeStatement must be removed.";

        for (BasicBlock succ : currentBb.getSuccessors()) {
          if (succ != cfg.getExitNode()) {
            workingList.add(succ);
          }
        }
      }

      for (BasicBlock node : nodes) {
        if (!accessibleNodes.contains(node)) {
          cfg.removeNode(node);
        }
      }
    }
  }

  private boolean hasDeadCode(BasicBlock currentBb) {

    for (JStatement stmt : currentBb.getStatements()) {
      if (stmt instanceof ForwardBranchResolver.JDeadCodeStatement) {
        return true;
      }
    }

    return false;
  }
}
