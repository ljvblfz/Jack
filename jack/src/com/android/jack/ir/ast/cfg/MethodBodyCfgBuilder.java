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

import com.google.common.collect.Lists;

import com.android.jack.Options;
import com.android.jack.cfg.BasicBlock;
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
    @Nonnull
    private final IdentityHashMap<BasicBlock, JBasicBlock> processed = new IdentityHashMap<>();

    Builder(@Nonnull EntryBlock entry, @Nonnull ExitBlock exit, @Nonnull JMethodBodyCfg cfgBody) {
      this.entry = entry;
      this.exit = exit;
      this.cfg = cfgBody.getCfg();
    }

    void build() {
      assert entry.getSuccessors().size() == 1;
      JBasicBlock block = buildBlock(entry.getSuccessors().get(0));
      cfg.entry().replaceAllSuccessors(cfg.exit(), block);
    }

    @Nonnull
    private JBasicBlock buildBlock(@Nonnull BasicBlock block) {
      if (block == exit) {
        return cfg.exit();
      }

      JBasicBlock newBlock = processed.get(block);
      if (newBlock != null) {
        return newBlock;
      }

      // Create an under-construction pseudo-block
      JBlockUnderConstruction ucBlock = new JBlockUnderConstruction(cfg);
      processed.put(block, ucBlock);

      // Process each kind of CFG marker blocks
      if (block instanceof PeiBasicBlock) {
        // NOTE: this handles both PeiBasicBlock and ThrowBasicBlock
        List<BasicBlock> successors = block.getSuccessors();
        int index = 0;

        JThrowingBasicBlock throwingBlock =
            block instanceof ThrowBasicBlock ?
                new JThrowBasicBlock(cfg,
                    buildBlock(successors.get(index++))) :
                new JThrowingExpressionBasicBlock(cfg,
                    buildBlock(successors.get(index++)),
                    buildBlock(successors.get(index++)));

        for (; index < successors.size(); index++) {
          throwingBlock.addHandler(buildBlock(successors.get(index)));
        }
        newBlock = throwingBlock;

      } else if (block instanceof SwitchBasicBlock) {
        SwitchBasicBlock switchBasicBlock = (SwitchBasicBlock) block;

        JSwitchBasicBlock throwingBlock =
            new JSwitchBasicBlock(cfg, buildBlock(switchBasicBlock.getDefaultBlock()));
        for (BasicBlock caseBlock : switchBasicBlock.getCasesBlock()) {
          throwingBlock.addCase(buildBlock(caseBlock));
        }
        newBlock = throwingBlock;

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
        newBlock = new JReturnBasicBlock(cfg, buildBlock(successors.get(0)));

      } else if (block instanceof NormalBasicBlock) {
        List<BasicBlock> successors = block.getSuccessors();
        assert successors.size() == 1;
        newBlock = new JSimpleBasicBlock(cfg, buildBlock(successors.get(0)));
      }

      if (newBlock == null) {
        throw new AssertionError();
      }

      // Move statements into newly created block
      BasicBlockEntryBuilder builder = new BasicBlockEntryBuilder(newBlock);
      for (JStatement stmt : block.getStatements()) {
        builder.accept(stmt);
      }

      // Replace temp block with real one and fix-up references
      assert newBlock.isValid();
      ArrayList<JBasicBlock> original = Lists.newArrayList(ucBlock.predecessors());
      for (JBasicBlock predecessor : original) {
        predecessor.replaceAllSuccessors(ucBlock, newBlock);
      }

      processed.put(block, newBlock);
      return newBlock;
    }

    /** Builds all the elements of the basic block */
    private static class BasicBlockEntryBuilder extends JVisitor {
      /** Basic block being built */
      private final JBasicBlock block;
      /** Basic block being built */
      private boolean sealed = false;

      private BasicBlockEntryBuilder(JBasicBlock block) {
        this.block = block;
      }

      private void addElement(@Nonnull JBasicBlockElement element) {
        assert !sealed;
        this.block.appendElement(element);
        this.sealed = element.isTerminal();
      }

      @Override
      public boolean visit(@Nonnull JGoto x) {
        assert block instanceof JSimpleBasicBlock;
        addElement(new JGotoBlockElement(x.getSourceInfo()));
        return false;
      }

      @Override
      public boolean visit(@Nonnull JReturnStatement x) {
        assert block instanceof JReturnBasicBlock;
        addElement(new JReturnBlockElement(x.getSourceInfo(), x.getExpr()));
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
            addElement(new JStoreBlockElement(x.getSourceInfo(), asg));
          } else if (lhs instanceof JVariableRef) {
            addElement(new JVariableAsgBlockElement(x.getSourceInfo(), asg));
          }

        } else if (expr instanceof JMethodCall) {
          addElement(new JMethodCallBlockElement(x.getSourceInfo(), (JMethodCall) x.getExpr()));

        } else if (expr instanceof JPolymorphicMethodCall) {
          addElement(new JPolymorphicMethodCallBlockElement(
              x.getSourceInfo(), (JPolymorphicMethodCall) x.getExpr()));

        } else {
          throw new AssertionError();
        }
        return false;
      }

      @Override
      public boolean visit(@Nonnull JThrowStatement x) {
        assert block instanceof JThrowingBasicBlock;
        addElement(new JThrowBlockElement(x.getSourceInfo(), x.getExpr()));
        return false;
      }

      @Override
      public boolean visit(@Nonnull JIfStatement x) {
        // If statement must end JConditionalBasicBlock
        assert block instanceof JConditionalBasicBlock;
        addElement(new JConditionalBlockElement(x.getSourceInfo(), x.getIfExpr()));

        FallThroughMarker marker = x.getMarker(FallThroughMarker.class);
        if (marker != null && marker.getFallThrough() == FallThroughMarker.FallThroughEnum.ELSE) {
          ((JConditionalBasicBlock) this.block).setInverted(true);
        }

        return false;
      }

      @Override
      public boolean visit(@Nonnull JCaseStatement x) {
        addElement(new JCaseBlockElement(x.getSourceInfo(), x.getExpr()));
        return false;
      }

      @Override
      public boolean visit(@Nonnull JUnlock x) {
        assert block instanceof JThrowingBasicBlock;
        addElement(new JUnlockBlockElement(x.getSourceInfo(), x.getLockExpr()));
        return false;
      }

      @Override
      public boolean visit(@Nonnull JLock x) {
        assert block instanceof JThrowingBasicBlock;
        addElement(new JLockBlockElement(x.getSourceInfo(), x.getLockExpr()));
        return false;
      }

      @Override
      public boolean visit(@Nonnull JSwitchStatement x) {
        assert block instanceof JSwitchBasicBlock;
        addElement(new JSwitchBlockElement(x.getSourceInfo(), x.getExpr()));
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
