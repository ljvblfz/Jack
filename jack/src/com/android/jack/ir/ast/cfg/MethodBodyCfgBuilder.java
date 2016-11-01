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

package com.android.jack.ir.ast.cfg;

import com.android.jack.Options;
import com.android.jack.cfg.BasicBlock;
import com.android.jack.cfg.BasicBlockMarker;
import com.android.jack.cfg.CatchBasicBlock;
import com.android.jack.cfg.ConditionalBasicBlock;
import com.android.jack.cfg.ControlFlowGraph;
import com.android.jack.cfg.EntryBlock;
import com.android.jack.cfg.ExitBlock;
import com.android.jack.cfg.NormalBasicBlock;
import com.android.jack.cfg.PeiBasicBlock;
import com.android.jack.cfg.ReturnBasicBlock;
import com.android.jack.cfg.SwitchBasicBlock;
import com.android.jack.cfg.ThrowBasicBlock;
import com.android.jack.ir.ast.JAbstractMethodBody;
import com.android.jack.ir.ast.JArrayRef;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JCaseStatement;
import com.android.jack.ir.ast.JCatchBlock;
import com.android.jack.ir.ast.JExceptionRuntimeValue;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JGoto;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JLock;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JMethodBodyCfg;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JPolymorphicMethodCall;
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JThrowStatement;
import com.android.jack.ir.ast.JUnlock;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.cfg.mutations.BasicBlockBuilder;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.transformations.booleanoperators.FallThroughMarker;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.rop.cast.RopLegalCast;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

/** Builds a ControlFlowGraph body representation for all methods. */
@Description("Builds a ControlFlowGraph body representation for all methods.")
@Constraint(need = { ControlFlowGraph.class,
                     JExceptionRuntimeValue.class,
                     ThreeAddressCodeForm.class,
                     RopLegalCast.class })
@Transform(add = { JMethodBodyCfg.class })
@Filter(TypeWithoutPrebuiltFilter.class)
public class MethodBodyCfgBuilder implements RunnableSchedulable<JMethod> {
  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);

  @Override
  public void run(@Nonnull JMethod method) {
    if (method.isAbstract() ||
        method.isNative() ||
        !filter.accept(this.getClass(), method)) {
      return;
    }

    ControlFlowGraph cfgMarker = method.getMarker(ControlFlowGraph.class);
    assert cfgMarker != null;

    JAbstractMethodBody body = method.getBody();
    assert body instanceof JMethodBody;

    JMethodBodyCfg cfgBody =
        new JMethodBodyCfg(body.getSourceInfo(), ((JMethodBody) body).getLocals());

    new Builder(
        cfgMarker.getEntryNode(),
        (ExitBlock) cfgMarker.getExitNode(),
        cfgBody).build();

    TransformationRequest request = new TransformationRequest(method);
    request.append(new Replace(body, cfgBody));
    request.commit();
  }

  /** Cfg builder */
  private static class Builder {
    @Nonnull
    private final EntryBlock entry;
    @Nonnull
    private final ExitBlock exit;
    @Nonnull
    private final JControlFlowGraph cfg;
    /** Maps CFG marker blocks into a created basic blocks or a placeholder */
    @Nonnull
    private final Map<BasicBlock, JBasicBlock> processed = new IdentityHashMap<>();
    /** Maps all basic block elements into original statements */
    @Nonnull
    private final Map<JBasicBlockElement, JStatement> bbElements = new IdentityHashMap<>();

    Builder(@Nonnull EntryBlock entry, @Nonnull ExitBlock exit, @Nonnull JMethodBodyCfg cfgBody) {
      this.entry = entry;
      this.exit = exit;
      this.cfg = cfgBody.getCfg();
    }

    void build() {
      assert entry.getSuccessors().size() == 1;
      JBasicBlock block = buildBlock(entry.getSuccessors().get(0));
      cfg.getEntryBlock().replaceAllSuccessors(cfg.getExitBlock(), block);
      setUpCatchBlockReferences();
    }

    private void setUpCatchBlockReferences() {
      // Use exception handling context pool to reduce allocations
      ExceptionHandlingContext.Pool pool = new ExceptionHandlingContext.Pool();

      // Assign exception handling context for all created basic block elements
      for (Map.Entry<JBasicBlockElement, JStatement> entry : bbElements.entrySet()) {
        List<JCatchBlock> origCatchBlocks = entry.getValue().getJCatchBlocks();

        if (!origCatchBlocks.isEmpty()) {
          // Create an ordered list of just created basic blocks to match the list of
          // catch blocks returned by getJCatchBlocks() of the original statement.

          List<JCatchBasicBlock> newCatchBlocks = new ArrayList<>(origCatchBlocks.size());
          for (JCatchBlock origCatchBlock : origCatchBlocks) {
            BasicBlockMarker marker = origCatchBlock.getMarker(BasicBlockMarker.class);
            assert marker != null;
            JBasicBlock newCatchBlock = processed.get(marker.getBasicBlock());
            assert newCatchBlock != null;
            assert newCatchBlock instanceof JCatchBasicBlock;
            newCatchBlocks.add((JCatchBasicBlock) newCatchBlock);
          }

          // Reset exception handling context of the block element
          entry.getKey().resetEHContext(pool.getOrCreate(newCatchBlocks));
        }
      }

      // Refresh throwing basic block's catch block lists
      for (JBasicBlock block : processed.values()) {
        if (block instanceof JThrowingBasicBlock) {
          ((JThrowingBasicBlock) block).resetCatchBlocks();
        }
      }
    }

    @Nonnull
    private JBasicBlock buildBlock(@Nonnull BasicBlock block) {
      if (block == exit) {
        return cfg.getExitBlock();
      }

      JBasicBlock newBlock = processed.get(block);
      if (newBlock != null) {
        return newBlock;
      }

      // Create an under-construction pseudo-block
      JPlaceholderBasicBlock placeholderBlock = new JPlaceholderBasicBlock(cfg);
      processed.put(block, placeholderBlock);

      // Process each kind of CFG marker blocks
      if (block instanceof PeiBasicBlock) {
        // NOTE: this handles both PeiBasicBlock and ThrowBasicBlock
        List<BasicBlock> successors = block.getSuccessors();
        int index = 0;

        JThrowingBasicBlock throwingBlock =
            (block instanceof ThrowBasicBlock)
                ? new JThrowBasicBlock(cfg)
                : new JThrowingExpressionBasicBlock(cfg,
                    buildBlock(successors.get(index++)));

        // Build uncaught exception block (which should be CFGs exit block)
        JBasicBlock uncaughtExceptionBlock = buildBlock(successors.get(index++));
        assert uncaughtExceptionBlock == cfg.getExitBlock();

        for (; index < successors.size(); index++) {
          // Build the catch block, note that catch blocks will be assigned later
          // when block elements are initialized with their exception handling context
          buildBlock(successors.get(index));
        }
        newBlock = throwingBlock;

      } else if (block instanceof SwitchBasicBlock) {
        SwitchBasicBlock switchBasicBlock = (SwitchBasicBlock) block;

        JSwitchBasicBlock switchBlock =
            new JSwitchBasicBlock(cfg, buildBlock(switchBasicBlock.getDefaultBlock()));
        for (BasicBlock caseBlock : switchBasicBlock.getCasesBlock()) {
          switchBlock.addCase(buildBlock(caseBlock));
        }
        newBlock = switchBlock;

      } else if (block instanceof ConditionalBasicBlock) {
        List<BasicBlock> successors = block.getSuccessors();
        assert successors.size() == 2;
        newBlock = new JConditionalBasicBlock(cfg,
            buildBlock(successors.get(0)), buildBlock(successors.get(1)));

      } else if (block instanceof CatchBasicBlock) {
        List<BasicBlock> successors = block.getSuccessors();
        assert successors.size() == 1;
        CatchBasicBlock catchBlock = (CatchBasicBlock) block;
        newBlock = new JCatchBasicBlock(cfg,
            buildBlock(successors.get(0)), catchBlock.getCatchTypes());

      } else if (block instanceof ReturnBasicBlock) {
        List<BasicBlock> successors = block.getSuccessors();
        assert successors.size() == 1;
        newBlock = new JReturnBasicBlock(cfg);

        // Must always point ot the exit block
        JBasicBlock exitBlock = buildBlock(successors.get(0));
        assert exitBlock == cfg.getExitBlock();

      } else if (block instanceof NormalBasicBlock) {
        List<BasicBlock> successors = block.getSuccessors();
        assert successors.size() == 1;
        newBlock = new JSimpleBasicBlock(cfg, buildBlock(successors.get(0)));
      }

      if (newBlock == null) {
        throw new AssertionError();
      }

      // Move statements into newly created block
      BasicBlockEntryBuilder builder = new BasicBlockEntryBuilder(bbElements, newBlock);
      for (JStatement stmt : block.getStatements()) {
        builder.accept(stmt);
      }

      // Make sure Simple block element always has a trailing goto element
      if (newBlock instanceof JSimpleBasicBlock) {
        assert newBlock.hasElements();
        if (!(newBlock.getLastElement() instanceof JGotoBlockElement)) {
          newBlock.appendElement(new JGotoBlockElement(SourceInfo.UNKNOWN,
              newBlock.getLastElement().getEHContext()));
        }
      }

      // We have to special-case creation of JCaseBasicBlock with does not exist in CFG marker.
      // If the first element of the created basic block is JCaseBlockElement, we will
      // split the block we just created into two blocks and make a new JCaseBasicBlock
      // of the first one.
      assert newBlock.hasElements();
      if (newBlock.getFirstElement() instanceof JCaseBlockElement) {
        JSimpleBasicBlock firstBlock = newBlock.split(1);
        assert firstBlock.getElementCount() == 2;
        assert firstBlock.getPredecessors().isEmpty();

        // Note that the created basic block is going to
        // represent the original CFG marker block.
        newBlock = new BasicBlockBuilder(cfg)
            .append(firstBlock).removeLast()
            .createCaseBlock(firstBlock.getPrimarySuccessor());

        firstBlock.detach(newBlock);
      }

      // Replace temp block with real one and fix-up references
      placeholderBlock.detach(newBlock);

      processed.put(block, newBlock);
      return newBlock;
    }

    /** Builds all the elements of the basic block */
    private static class BasicBlockEntryBuilder extends JVisitor {
      /** Maps *all* created basic block elements into original statements */
      @Nonnull
      private final Map<JBasicBlockElement, JStatement> elements;
      /** Basic block being built */
      private final JBasicBlock block;
      /**
       * Indicates the block is sealed, i.e. already saw potentially
       * throwing expression, for assertion purpose only.
       */
      private boolean sealed = false;

      private BasicBlockEntryBuilder(
          @Nonnull Map<JBasicBlockElement, JStatement> elements, @Nonnull JBasicBlock block) {
        this.elements = elements;
        this.block = block;
      }

      private void addElement(@Nonnull JStatement statement, @Nonnull JBasicBlockElement element) {
        assert !sealed;
        this.elements.put(element, statement);
        this.block.appendElement(element);
        // NOTE: don't consider case elements as terminal, the block will be split
        //       later into a case basic block and rest representing the current block
        this.sealed = element.isTerminal() &&
            !(element instanceof JCaseBlockElement);
      }

      @Override
      public boolean visit(@Nonnull JGoto x) {
        assert block instanceof JSimpleBasicBlock;
        addElement(x, new JGotoBlockElement(
            x.getSourceInfo(), ExceptionHandlingContext.EMPTY));
        return false;
      }

      @Override
      public boolean visit(@Nonnull JReturnStatement x) {
        assert block instanceof JReturnBasicBlock;
        addElement(x, new JReturnBlockElement(
            x.getSourceInfo(), ExceptionHandlingContext.EMPTY, x.getExpr()));
        return false;
      }

      @Override
      public boolean visit(@Nonnull JExpressionStatement x) {
        JExpression expr = x.getExpr();
        // Depending on the expression structure we create different basic block elements
        if (expr instanceof JAsgOperation) {
          JAsgOperation asg = (JAsgOperation) expr;
          JExpression lhs = asg.getLhs();
          if (lhs instanceof JArrayRef || lhs instanceof JFieldRef) {
            addElement(x, new JStoreBlockElement(
                x.getSourceInfo(), ExceptionHandlingContext.EMPTY, asg));
          } else if (lhs instanceof JVariableRef) {
            addElement(x, new JVariableAsgBlockElement(
                x.getSourceInfo(), ExceptionHandlingContext.EMPTY, asg));
          } else {
            throw new AssertionError();
          }

        } else if (expr instanceof JMethodCall) {
          addElement(x, new JMethodCallBlockElement(
              x.getSourceInfo(), ExceptionHandlingContext.EMPTY, (JMethodCall) x.getExpr()));

        } else if (expr instanceof JPolymorphicMethodCall) {
          addElement(x, new JPolymorphicMethodCallBlockElement(x.getSourceInfo(),
              ExceptionHandlingContext.EMPTY, (JPolymorphicMethodCall) x.getExpr()));

        } else {
          throw new AssertionError();
        }
        return false;
      }

      @Override
      public boolean visit(@Nonnull JThrowStatement x) {
        assert block instanceof JThrowingBasicBlock;
        addElement(x, new JThrowBlockElement(
            x.getSourceInfo(), ExceptionHandlingContext.EMPTY, x.getExpr()));
        return false;
      }

      @Override
      public boolean visit(@Nonnull JIfStatement x) {
        // If statement must end JConditionalBasicBlock
        assert block instanceof JConditionalBasicBlock;
        addElement(x, new JConditionalBlockElement(
            x.getSourceInfo(), ExceptionHandlingContext.EMPTY, x.getIfExpr()));

        FallThroughMarker marker = x.getMarker(FallThroughMarker.class);
        if (marker != null && marker.getFallThrough() == FallThroughMarker.FallThroughEnum.ELSE) {
          ((JConditionalBasicBlock) this.block).setInverted(true);
        }

        return false;
      }

      @Override
      public boolean visit(@Nonnull JCaseStatement x) {
        addElement(x, new JCaseBlockElement(
            x.getSourceInfo(), ExceptionHandlingContext.EMPTY, x.getExpr()));
        return false;
      }

      @Override
      public boolean visit(@Nonnull JUnlock x) {
        assert block instanceof JThrowingBasicBlock;
        addElement(x, new JUnlockBlockElement(
            x.getSourceInfo(), ExceptionHandlingContext.EMPTY, x.getLockExpr()));
        return false;
      }

      @Override
      public boolean visit(@Nonnull JLock x) {
        assert block instanceof JThrowingBasicBlock;
        addElement(x, new JLockBlockElement(
            x.getSourceInfo(), ExceptionHandlingContext.EMPTY, x.getLockExpr()));
        return false;
      }

      @Override
      public boolean visit(@Nonnull JSwitchStatement x) {
        assert block instanceof JSwitchBasicBlock;
        addElement(x, new JSwitchBlockElement(
            x.getSourceInfo(), ExceptionHandlingContext.EMPTY, x.getExpr()));
        return false;
      }

      @Override
      public boolean visit(@Nonnull JNode jnode) {
        throw new AssertionError(
            "Unsupported JNode in JBasicBlock builder: " + jnode.getClass().getCanonicalName());
      }
    }
  }
}
