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

import com.android.jack.Jack;
import com.android.jack.JackEventType;
import com.android.jack.Options;
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
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JStatementList;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JSynchronizedBlock;
import com.android.jack.ir.ast.JThrowStatement;
import com.android.jack.ir.ast.JTryStatement;
import com.android.jack.ir.ast.JUnlock;
import com.android.jack.ir.ast.JValueLiteral;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfoFactory;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.transformations.ast.RefAsStatement;
import com.android.jack.transformations.ast.switches.UselessSwitches;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.ControlFlowHelper;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.Protect;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;
import com.android.sched.schedulable.With;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.Event;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnegative;
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
@Use(SourceInfoFactory.class)
@Filter(TypeWithoutPrebuiltFilter.class)
public class CfgBuilder implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  private final JSession session = Jack.getSession();

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

  class BuilderVisitor extends JVisitor {

    @Nonnegative
    private int basicBlockId;

    @Nonnull
    private final EntryBlock entryBlock;
    @Nonnull
    private final ExitBlock exitBlock;
    @Nonnull
    private final ArrayList<BasicBlock> blocks;
    @Nonnull
    private final JMethod method;

    @Nonnull
    private List<JStatement> currentStmts = new LinkedList<JStatement>();
    private boolean firstStmtCreated = false;

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

    @Nonnull
    private final SourceInfoFactory sourceInfoFactory = session.getSourceInfoFactory();

    public BuilderVisitor(@Nonnull JMethod method) {
      assert method != null;
      this.method = method;
      blocks = new ArrayList<BasicBlock>();
      entryBlock = new EntryBlock(basicBlockId++);
      blocks.add(entryBlock);
      exitBlock = new ExitBlock();
    }

    @Override
    public void endVisit(@Nonnull JMethodBody methodBody) {
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

      CatchBasicBlock catchBasicBlock =
          new CatchBasicBlock(basicBlockId++, currentStmts, catchBlock.getCatchTypes(),
              catchBlock.getCatchVar());
      setBlockOfStatement(catchBasicBlock);

      JStatement nextStatement =
          ControlFlowHelper.getNextStatement(getConcreteStatement(catchBlock));
      if (nextStatement != null) {
        forwardBranchResolver.addNormalBasicBlock(catchBasicBlock, nextStatement);
      }

      accept(catchStmts.subList(1, catchStmts.size()));

      return false;
    }

    @Override
    public void endVisit(@Nonnull JCatchBlock block) {
      NormalBasicBlock endOfBlock = new NormalBasicBlock(basicBlockId++, currentStmts);
      setBlockOfStatement(endOfBlock);

      JStatement nextStatement = ControlFlowHelper.getNextStatement(block);
      if (nextStatement != null) {
        forwardBranchResolver.addNormalBasicBlock(endOfBlock, nextStatement);
      }
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
        NormalBasicBlock endOfBlock = new NormalBasicBlock(basicBlockId++, currentStmts);
        setBlockOfStatement(endOfBlock);

        JStatement nextStatement = ControlFlowHelper.getNextStatement(block);
        if (nextStatement != null) {
          forwardBranchResolver.addNormalBasicBlock(endOfBlock, nextStatement);
        }
      }
    }

    @Override
    public boolean visit(@Nonnull JStatement statement) {
      if (!currentStmts.isEmpty() && !statement.getJCatchBlocks().equals(previousCatchBlock)) {
        previousCatchBlock = statement.getJCatchBlocks();
        NormalBasicBlock tryBasicBlock = new NormalBasicBlock(basicBlockId++, currentStmts);
        setBlockOfStatement(tryBasicBlock);
        forwardBranchResolver.addNormalBasicBlock(tryBasicBlock,
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

      ConditionalBasicBlock condBlock = new ConditionalBasicBlock(basicBlockId++, currentStmts);
      setBlockOfStatement(condBlock);

      assert ifStmt.getThenStmt() != null;

      accept(ifStmt.getThenStmt());

      JStatement elseStmt = ifStmt.getElseStmt();
      if (elseStmt != null) {
        accept(elseStmt);
      } else {
        elseStmt = ControlFlowHelper.getNextStatement(ifStmt);
      }
      forwardBranchResolver.addConditionalBasicBlock(condBlock, ifStmt.getThenStmt(),
          elseStmt);

      return false;
    }

    @Override
    public boolean visit(@Nonnull JReturnStatement retStmt) {
      super.visit(retStmt);

      BasicBlock returnBlock = new ReturnBasicBlock(basicBlockId++, exitBlock, currentStmts);
      setBlockOfStatement(returnBlock);

      return false;
    }

    @Override
    public boolean visit(@Nonnull JGoto gotoStmt) {
      super.visit(gotoStmt);

      NormalBasicBlock branchBlock = new NormalBasicBlock(basicBlockId++, currentStmts);
      setBlockOfStatement(branchBlock);

      JLabeledStatement labeledStatement = gotoStmt.getTargetBlock();
      BasicBlockMarker bbm = labeledStatement.getMarker(BasicBlockMarker.class);

      if (bbm == null || bbm.getBasicBlock() == null) {
        forwardBranchResolver.addNormalBasicBlock(branchBlock, labeledStatement.getBody());
      } else {
        branchBlock.setTarget(bbm.getBasicBlock());
      }

      return false;
    }

    @Override
    public boolean visit(@Nonnull JLabeledStatement labeledStatement) {

      if (!currentStmts.isEmpty()) {
        NormalBasicBlock normalBasicBlock = new NormalBasicBlock(basicBlockId++, currentStmts);
        setBlockOfStatement(normalBasicBlock);

        forwardBranchResolver.addNormalBasicBlock(normalBasicBlock,
            getConcreteStatement((JBlock) labeledStatement.getBody()));
      }

      super.visit(labeledStatement);

      return true;
    }

    @Override
    public boolean visit(@Nonnull JSwitchStatement switchStatement) {
      super.visit(switchStatement);

      SwitchBasicBlock switchBlock = new SwitchBasicBlock(basicBlockId++, currentStmts);
      setBlockOfStatement(switchBlock);

      List<JCaseStatement> cases = switchStatement.getCases();
      Collections.sort(cases, new JCaseStatementComparator());

      JStatement defaultCase = switchStatement.getDefaultCase();
      if (defaultCase == null) {
        defaultCase = ControlFlowHelper.getNextStatement(switchStatement);
      }
      forwardBranchResolver.addSwitchBasicBlock(switchBlock, cases, defaultCase);

      return true;
    }

    @Override
    public boolean visit(@Nonnull JCaseStatement caseStatement) {
      if (!currentStmts.isEmpty()) {
        NormalBasicBlock caseBlock = new NormalBasicBlock(basicBlockId++, currentStmts);
        setBlockOfStatement(caseBlock);

        forwardBranchResolver.addNormalBasicBlock(caseBlock, caseStatement);
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

      ThrowBasicBlock throwBlock = new ThrowBasicBlock(basicBlockId++, currentStmts);
      setBlockOfStatement(throwBlock);

      forwardBranchResolver.addPeiBasicBlock(throwBlock, null /* targetStatement */,
          throwStmt.getJCatchBlocks());

      throwBlock.setExitBlockWhenUncaught(exitBlock);

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
      Event optEvent = tracer.start(JackEventType.REMOVE_DEAD_CODE);

      try {
        removeUnaccessibleNode(blocks, entryBlock, exitBlock, basicBlockId);
      } finally {
        optEvent.end();
      }
      return new ControlFlowGraph(method, basicBlockId, entryBlock, exitBlock, blocks);
    }

    @Nonnull
    private JStatement getConcreteStatement(@Nonnull JStatementList block) {
      List<JStatement> statements = block.getStatements();

      if (statements.isEmpty()) {
        JStatement nextStatement = ControlFlowHelper.getNextStatement(block);
        assert nextStatement != null;
        return (nextStatement);
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

    private boolean expressionCanThrow(@Nonnull JExpression expression) {
      return (expression.canThrow() || (expression instanceof JBinaryOperation &&
          (expressionCanThrow(((JBinaryOperation) expression)
          .getLhs()) || expressionCanThrow(((JBinaryOperation) expression).getRhs()))));
    }

    private void setBlockOfStatement(@Nonnull BasicBlock bb) {
      blocks.add(bb);
      BasicBlockMarker marker = new BasicBlockMarker(bb);
      for (JStatement statement : currentStmts) {
        statement.addMarker(marker);
      }

      for (JStatement statement : virtualStmts) {
        statement.addMarker(marker);
      }

      // First created block is the successor of the entry block.
      if (!firstStmtCreated) {
        entryBlock.setTarget(bb);
        firstStmtCreated = true;
      }

      currentStmts = new LinkedList<JStatement>();
      virtualStmts = new LinkedList<JStatement>();
    }

    private void buildCfgForPei(@Nonnull JStatement peiInst) {
      PeiBasicBlock peiBlock = new PeiBasicBlock(basicBlockId++, currentStmts);
      setBlockOfStatement(peiBlock);

      JStatement nextStatement = ControlFlowHelper.getNextStatement(peiInst);
      forwardBranchResolver.addPeiBasicBlock(peiBlock, nextStatement, peiInst.getJCatchBlocks());

      peiBlock.setExitBlockWhenUncaught(exitBlock);
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    BuilderVisitor cfgBuilder = new BuilderVisitor(method);
    cfgBuilder.accept(method);

    method.addMarker(cfgBuilder.getCfg());
  }

  private static void removeUnaccessibleNode(@Nonnull ArrayList<BasicBlock> nodes,
      @Nonnull BasicBlock entryNode, @Nonnull BasicBlock exitNode,
      @Nonnegative int maxBasicBlockId) {

    if (nodes.isEmpty()) {
      return;
    }

    // 0 - no state, 1 - queued, 2 - accessible
    byte[] state = new byte[maxBasicBlockId];

    List<BasicBlock> workingList = new LinkedList<BasicBlock>();
    workingList.add(entryNode);

    int accessibleNodesCount = 0;

    // Do not use recursion to compute accessible node to avoid stack overflow on very big method.
    while (!workingList.isEmpty()) {
      BasicBlock currentBb = workingList.remove(0);

      if (currentBb.getStatements().isEmpty() && currentBb != entryNode) {
        assert currentBb instanceof NormalBasicBlock;
        BasicBlock newBlock = ((NormalBasicBlock) currentBb).getTarget();
        currentBb.replaceBy(newBlock);
      } else {
        state[currentBb.getId()] = 2;
        ++accessibleNodesCount;
      }

      for (BasicBlock succ : currentBb.getSuccessors()) {
        if (succ != exitNode && state[succ.getId()] == 0) {
          state[succ.getId()] = 1;
          workingList.add(succ);
        }
      }
    }

    ArrayList<BasicBlock> accessibleBlocks = new ArrayList<BasicBlock>(accessibleNodesCount);
    for (int i = 0, len = nodes.size(); i < len; ++i) {
      BasicBlock block = nodes.get(i);
      if (state[i] == 2) {
        accessibleBlocks.add(block);
      } else {
        for (BasicBlock succ : block.getSuccessors()) {
          succ.removePredecessor(block);
        }
      }
    }
    nodes.clear();
    nodes.addAll(accessibleBlocks);
    nodes.trimToSize();
  }
}
