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

import com.android.jack.ir.ast.JCaseStatement;
import com.android.jack.ir.ast.JCatchBlock;
import com.android.jack.ir.ast.JStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Resolve forward branch that appears during control flow graph building.
 */
class ForwardBranchResolver {

  @Nonnull
  private final ExitBlock exitBlock;

  public ForwardBranchResolver(@Nonnull ExitBlock exitBlock) {
    this.exitBlock = exitBlock;
  }

  private static interface BlockToResolve {
    void resolve();
  }

  @Nonnull
  private final ArrayList<BlockToResolve> blocksToResolve = new ArrayList<BlockToResolve>();

  void addNormalBasicBlock(@Nonnull NormalBasicBlock block, @Nonnull JStatement targetStatement) {
    assert block != null;
    assert targetStatement != null;
    blocksToResolve.add(new NormalBasicBlockToResolve(block, targetStatement));
  }

  void addConditionalBasicBlock(@Nonnull ConditionalBasicBlock block,
    @Nonnull JStatement thenStatement, @CheckForNull JStatement elseStatement) {
    assert block != null;
    assert thenStatement != null;
    blocksToResolve.add(new ConditionalBasicBlockToResolve(block, thenStatement, elseStatement));
  }

  void addSwitchBasicBlock(@Nonnull SwitchBasicBlock block, @Nonnull List<JCaseStatement> cases,
      @CheckForNull JStatement defaultCase) {
    assert block != null;
    assert cases != null;
    blocksToResolve.add(new SwitchBasicBlockToResolve(block, cases, defaultCase));
  }

  void addPeiBasicBlock(@Nonnull PeiBasicBlock block, @CheckForNull JStatement targetStatement,
      @Nonnull List<JCatchBlock> catchBlocks) {
    assert block != null;
    assert catchBlocks != null;
    blocksToResolve.add(new PeiBasicBlockToResolve(block, targetStatement, catchBlocks));
  }

  /**
   * Resolve forward branches by updating basic bloc successors.
   */
  void resolve() {
    for (int i = 0, len = blocksToResolve.size(); i < len; ++i) {
      blocksToResolve.get(i).resolve();
    }
  }

  @Nonnull
  private static BasicBlock getTargetBlock(@Nonnull JStatement statement) {
    assert statement != null;
    BasicBlockMarker bbm = statement.getMarker(BasicBlockMarker.class);
    assert bbm != null;
    BasicBlock targetBb = bbm.getBasicBlock();
    assert targetBb != null;
    return targetBb;
  }

  private static class NormalBasicBlockToResolve implements BlockToResolve {
    @Nonnull
    private final NormalBasicBlock block;
    @Nonnull
    private final JStatement statement;

    public NormalBasicBlockToResolve(@Nonnull NormalBasicBlock block,
        @Nonnull JStatement statement) {
      assert block != null;
      assert statement != null;
      this.block = block;
      this.statement = statement;
    }

    @Override
    public void resolve() {
      block.setTarget(getTargetBlock(statement));
    }
  }

  private  class ConditionalBasicBlockToResolve implements BlockToResolve {
    @Nonnull
    private final ConditionalBasicBlock block;
    @Nonnull
    private final JStatement ifStatement;
    @CheckForNull
    private final JStatement elseStatement;

    public ConditionalBasicBlockToResolve(@Nonnull ConditionalBasicBlock block,
        @Nonnull JStatement ifStatement, @CheckForNull JStatement elseStatement) {
      assert block != null;
      assert ifStatement != null;
      this.block = block;
      this.ifStatement = ifStatement;
      this.elseStatement = elseStatement;
    }

    @Override
    public void resolve() {
      block.setThenBlock(getTargetBlock(ifStatement));
      if (elseStatement == null) {
        // elseStatement means statement contained by the else block of the JIfStatement or the
        // statement following the JIfStatement.
        // A conditional block without an else statement will target the exit block. Indeed, by
        // building a conditional block must have two targets (it is required by the backend).
        // A conditional without else statement can happen after FinallyRemover where a finally
        // block containing a JIfStatement is inlined at the end of the try block that is composed
        // by an infinite loop for instance. In this case, JIfStatement will be dead code and
        // elseStatement will be null.
        block.setElseBlock(exitBlock);
      } else {
        block.setElseBlock(getTargetBlock(elseStatement));
      }
    }
  }

  private static class SwitchBasicBlockToResolve implements BlockToResolve {
    @Nonnull
    private final SwitchBasicBlock block;
    @Nonnull
    private final List<JCaseStatement> cases;
    @CheckForNull
    private final JStatement defaultCase;

    public SwitchBasicBlockToResolve(@Nonnull SwitchBasicBlock block,
        @Nonnull List<JCaseStatement> cases, @CheckForNull JStatement defaultCase) {
      assert block != null;
      assert cases != null;
      this.block = block;
      this.cases = cases;
      this.defaultCase = defaultCase;
    }

    @Override
    public void resolve() {
      for (JCaseStatement caseStatement : cases) {
        block.addCaseBlock(getTargetBlock(caseStatement));
      }
      if (defaultCase != null) {
        block.setDefault(getTargetBlock(defaultCase));
      }
    }
  }

  private static class PeiBasicBlockToResolve implements BlockToResolve {
    @Nonnull
    private final PeiBasicBlock block;
    @CheckForNull
    private final JStatement statement;
    @Nonnull
    private final List<JCatchBlock> catchBlocks;

    public PeiBasicBlockToResolve(@Nonnull PeiBasicBlock block, @CheckForNull JStatement statement,
        @Nonnull List<JCatchBlock> catchBlocks) {
      assert block != null;
      assert catchBlocks != null;
      this.block = block;
      this.statement = statement;
      this.catchBlocks = catchBlocks;
    }

    @Override
    public void resolve() {
      if (statement != null) {
        block.setTarget(getTargetBlock(statement));
      }
      // addExceptionBlock has to be called in reverse order because it adds an exception block
      // before all exception blocks which have been already added.
      ListIterator<JCatchBlock> catchBlocksIter = catchBlocks.listIterator(catchBlocks.size());
      while (catchBlocksIter.hasPrevious()) {
        block.addExceptionBlock((CatchBasicBlock) getTargetBlock(catchBlocksIter.previous()));
      }
    }
  }
}
