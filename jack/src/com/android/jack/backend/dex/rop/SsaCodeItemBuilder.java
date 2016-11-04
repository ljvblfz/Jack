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
package com.android.jack.backend.dex.rop;

import com.google.common.collect.Lists;

import com.android.jack.JackEventType;
import com.android.jack.Options;
import com.android.jack.dx.dex.DexOptions;
import com.android.jack.dx.dex.code.DalvCode;
import com.android.jack.dx.dex.code.PositionList;
import com.android.jack.dx.dex.code.RopTranslator;
import com.android.jack.dx.dex.file.CodeItem;
import com.android.jack.dx.rop.code.DexTranslationAdvice;
import com.android.jack.dx.rop.code.Insn;
import com.android.jack.dx.rop.code.LocalVariableExtractor;
import com.android.jack.dx.rop.code.LocalVariableInfo;
import com.android.jack.dx.rop.code.PlainCstInsn;
import com.android.jack.dx.rop.code.PlainInsn;
import com.android.jack.dx.rop.code.RegisterSpec;
import com.android.jack.dx.rop.code.RegisterSpecList;
import com.android.jack.dx.rop.code.Rop;
import com.android.jack.dx.rop.code.RopMethod;
import com.android.jack.dx.rop.code.Rops;
import com.android.jack.dx.rop.code.SourcePosition;
import com.android.jack.dx.rop.cst.CstInteger;
import com.android.jack.dx.rop.type.StdTypeList;
import com.android.jack.dx.rop.type.Type;
import com.android.jack.dx.rop.type.TypeList;
import com.android.jack.dx.ssa.NormalSsaInsn;
import com.android.jack.dx.ssa.Optimizer;
import com.android.jack.dx.ssa.Optimizer.OptionalStep;
import com.android.jack.dx.ssa.SsaBasicBlock;
import com.android.jack.dx.ssa.SsaInsn;
import com.android.jack.dx.ssa.SsaMethod;
import com.android.jack.dx.util.IntList;
import com.android.jack.ir.SideEffectOperation;
import com.android.jack.ir.ast.JAbstractMethodBody;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JAssertStatement;
import com.android.jack.ir.ast.JCastOperation;
import com.android.jack.ir.ast.JConcatOperation;
import com.android.jack.ir.ast.JConditionalExpression;
import com.android.jack.ir.ast.JConditionalOperation;
import com.android.jack.ir.ast.JExceptionRuntimeValue;
import com.android.jack.ir.ast.JFieldInitializer;
import com.android.jack.ir.ast.JLoop;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBodyCfg;
import com.android.jack.ir.ast.JMultiExpression;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JSsaVariableRef;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JThis;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.cfg.JBasicBlock;
import com.android.jack.ir.ast.cfg.JCaseBasicBlock;
import com.android.jack.ir.ast.cfg.JCatchBasicBlock;
import com.android.jack.ir.ast.cfg.JConditionalBasicBlock;
import com.android.jack.ir.ast.cfg.JControlFlowGraph;
import com.android.jack.ir.ast.cfg.JEntryBasicBlock;
import com.android.jack.ir.ast.cfg.JExitBasicBlock;
import com.android.jack.ir.ast.cfg.JRegularBasicBlock;
import com.android.jack.ir.ast.cfg.JReturnBasicBlock;
import com.android.jack.ir.ast.cfg.JSimpleBasicBlock;
import com.android.jack.ir.ast.cfg.JSwitchBasicBlock;
import com.android.jack.ir.ast.cfg.JThrowBasicBlock;
import com.android.jack.ir.ast.cfg.JThrowingExpressionBasicBlock;
import com.android.jack.ir.ast.marker.ThrownExceptionMarker;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.scheduling.marker.DexCodeMarker;
import com.android.jack.transformations.EmptyClinit;
import com.android.jack.transformations.InvalidDefaultBridgeInInterfaceRemoved;
import com.android.jack.transformations.ast.BooleanTestOutsideIf;
import com.android.jack.transformations.ast.ImplicitBoxingAndUnboxing;
import com.android.jack.transformations.ast.ImplicitCast;
import com.android.jack.transformations.ast.InitInNewArray;
import com.android.jack.transformations.ast.JPrimitiveClassLiteral;
import com.android.jack.transformations.ast.MultiDimensionNewArray;
import com.android.jack.transformations.ast.NewInstanceRemoved;
import com.android.jack.transformations.ast.RefAsStatement;
import com.android.jack.transformations.ast.UnassignedValues;
import com.android.jack.transformations.ast.inner.InnerAccessor;
import com.android.jack.transformations.ast.switches.UselessSwitches;
import com.android.jack.transformations.cast.SourceCast;
import com.android.jack.transformations.rop.cast.RopLegalCast;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.Event;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * CodeItemBuilder is a schedulable that generates {@link CodeItem} from {@link JMethod}. The
 * generated {@link CodeItem} is saved into the {@link DexCodeMarker}.
 */
@Description("Builds CodeItem from JMethod in SSA form")
@Name("SsaCodeItemBuilder")
@Constraint(
  need = {
    JSsaVariableRef.class,
    JMethodBodyCfg.class,
    JExceptionRuntimeValue.class,
    NewInstanceRemoved.class,
    ThreeAddressCodeForm.class,
    RopLegalCast.class,
    InnerAccessor.class,
    InvalidDefaultBridgeInInterfaceRemoved.class
  },
  no = {
    BooleanTestOutsideIf.class,
    InitInNewArray.class,
    JAsgOperation.class,
    JPrimitiveClassLiteral.class,
    JMultiExpression.class,
    JConditionalExpression.class,
    JFieldInitializer.class,
    JConcatOperation.class,
    JLoop.class,
    SideEffectOperation.class,
    UnassignedValues.class,
    RefAsStatement.class,
    MultiDimensionNewArray.class,
    JSwitchStatement.SwitchWithEnum.class,
    ImplicitBoxingAndUnboxing.class,
    ImplicitCast.class,
    JAssertStatement.class,
    JConditionalOperation.class,
    EmptyClinit.class,
    UselessSwitches.class,
    SourceCast.class,
    JCastOperation.WithIntersectionType.class
  }
)
@Transform(add = DexCodeMarker.class)
@Use(RopHelper.class)
@Filter(TypeWithoutPrebuiltFilter.class)
public class SsaCodeItemBuilder implements RunnableSchedulable<JMethod> {
  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);
  private final boolean emitSyntheticLocalDebugInfo =
      ThreadConfig.get(CodeItemBuilder.EMIT_SYNTHETIC_LOCAL_DEBUG_INFO).booleanValue();
  private final boolean emitLocalDebugInfo =
      ThreadConfig.get(Options.EMIT_LOCAL_DEBUG_INFO).booleanValue();
  private final boolean runDxOptimizations =
      ThreadConfig.get(CodeItemBuilder.DEX_OPTIMIZE).booleanValue();
  private final boolean forceJumbo = ThreadConfig.get(CodeItemBuilder.FORCE_JUMBO).booleanValue();
  private final boolean removeRedundantConditionalBranch =
      ThreadConfig.get(CodeItemBuilder.OPTIMIZE_BRANCHES).booleanValue();
  private final int apiLevel = ThreadConfig.get(Options.ANDROID_MIN_API_LEVEL).intValue();
  private final boolean emitLineNumberTable =
      ThreadConfig.get(Options.EMIT_LINE_NUMBER_DEBUG_INFO).booleanValue();

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  @Override
  public void run(@Nonnull JMethod method) {
    if (method.isNative()
        || method.isAbstract()
        || !filter.accept(this.getClass(), method)) {
      return;
    }
    buildSsaCodeItem(method, false);
  }

  @SuppressWarnings("boxing")
  public void buildSsaCodeItem(@Nonnull JMethod method, boolean minimizeRegister) {
    try (Event event = tracer.open(JackEventType.DX_BACKEND)) {
      SsaRopRegisterManager ropReg =
          new SsaRopRegisterManager(emitLocalDebugInfo, emitSyntheticLocalDebugInfo);

      JAbstractMethodBody body = method.getBody();
      assert body instanceof JMethodBodyCfg;
      JControlFlowGraph cfg = ((JMethodBodyCfg) body).getCfg();

      final JEntryBasicBlock entryBasicBlock = cfg.getEntryBlock();
      final JExitBasicBlock exitBasicBlock = cfg.getExitBlock();

      final Map<JBasicBlock, Integer> basicBlocks = new LinkedHashMap<>();
      int blockId = 0; // 0 is reserved for entry block
      // TODO(acleung): We don't need to generate blocks that is not reachable. We are only doing
      // this for now because the Phi nodes will not have a valid prececessor otherwise. DX normally
      // remove any unreachable nodes so this will not be a performance issue.
      for (JBasicBlock block : cfg.getAllBlocksUnordered()) {
        if (block == exitBasicBlock) {
          basicBlocks.put(block, Integer.MAX_VALUE);
        } else {
          basicBlocks.put(block, Integer.valueOf(blockId++));
        }
      }

      int maxLabel = blockId + 2;
      SsaMethod ssaMethod =
          new SsaMethod(getParameterWordCount(method), method.isStatic(), maxLabel, 0);
      final SsaRopBasicBlockManager ropBb = new SsaRopBasicBlockManager(ssaMethod, maxLabel);

      JBasicBlock firstBlockOfCode = entryBasicBlock.getOnlySuccessor();

      // Rop-SSA needs an extra entry block compare the ROP.
      int firstJackBlockIndex = basicBlocks.get(firstBlockOfCode).intValue();
      SsaBasicBlock initBb = ropBb.createBasicBlock();
      initBb.setSuccessors(
          IntList.makeImmutable(ropBb.getSpecialLabel(SsaRopBasicBlockManager.PARAM_ASSIGNMENT)),
          ropBb.getSpecialLabel(SsaRopBasicBlockManager.PARAM_ASSIGNMENT));
      initBb.setInsns(Lists.<SsaInsn>newArrayList());
      initBb.setRopLabel(blockId + 2);

      addSetupBlocks(method, ropReg, ropBb, firstJackBlockIndex);

      if (method.getType() != JPrimitiveTypeEnum.VOID.getType()) {
        ropReg.createReturnReg(method.getType());
      }

      for (JBasicBlock bb : basicBlocks.keySet()) {
        if (bb instanceof JExitBasicBlock || bb instanceof JEntryBasicBlock) {
          continue;
        }
        assert bb != entryBasicBlock && bb != exitBasicBlock;
        final SsaBasicBlock ssaBb = ropBb.createBasicBlock();
        final SsaRopBuilderVisitor ropBuilder =
            new SsaRopBuilderVisitor(ropReg, bb, ssaBb, basicBlocks);

        ropBuilder.processBasicBlockElements();
        final List<SsaInsn> instructions = ropBuilder.getInstructions();
        assert instructions != null;

        JVisitor visitor = new JVisitor() {
          @Override
          public boolean visit(@Nonnull JRegularBasicBlock bb) {
            assert bb instanceof JSimpleBasicBlock
                || bb instanceof JCatchBasicBlock
                || bb instanceof JCaseBasicBlock;
            assert bb.hasPrimarySuccessor();

            JBasicBlock primarySuccessor = bb.getPrimarySuccessor();
            IntList successors = IntList.makeImmutable(getBlockId(primarySuccessor));
            List<SsaInsn> il = createInsnList(instructions, 1);
            Insn gotoInstruction = new PlainInsn(
                Rops.GOTO, getLastElementPosition(bb), null, RegisterSpecList.EMPTY);
            il.add(new NormalSsaInsn(gotoInstruction, ssaBb));
            ssaBb.setRopLabel(getBlockId(bb));
            ssaBb.setInsns(il);
            ssaBb.setSuccessors(successors, getBlockId(primarySuccessor));
            return false;
          }

          @Override
          public boolean visit(@Nonnull JReturnBasicBlock bb) {
            List<SsaInsn> il = createInsnList(instructions, 0);
            ssaBb.setRopLabel(getBlockId(bb));
            ssaBb.setInsns(il);
            ssaBb.setSuccessors(IntList.EMPTY, -1);
            return false;
          }

          @Override
          public boolean visit(@Nonnull JThrowBasicBlock bb) {
            List<SsaInsn> il = createInsnList(instructions, 0);

            IntList successors = new IntList();
            int primarySuccessor = -1;

            if (!bb.getCatchBlocks().isEmpty()) {
              addCatchBlockSuccessors(bb.getCatchBlocks(), successors);
            }
            successors.setImmutable();
            ssaBb.setRopLabel(getBlockId(bb));
            ssaBb.setInsns(il);
            ssaBb.setSuccessors(successors, primarySuccessor);
            return false;
          }

          @Override
          public boolean visit(@Nonnull JThrowingExpressionBasicBlock bb) {
            SsaInsn lastInstruction = instructions.get(instructions.size() - 1);
            List<Insn> extraInstructions = ropBuilder.getExtraInstructions();
            assert extraInstructions != null;

            List<SsaInsn> il = createInsnList(instructions, 0);

            int extraBlockLabel = ropBb.getAvailableLabel();

            IntList successors = new IntList();
            addCatchBlockSuccessors(bb.getCatchBlocks(), successors);

            successors.add(extraBlockLabel);
            successors.setImmutable();

            ssaBb.setRopLabel(getBlockId(bb));
            ssaBb.setInsns(il);
            ssaBb.setSuccessors(successors, extraBlockLabel);

            boolean needsGoto;
            SourcePosition sourcePosition;

            SsaBasicBlock extraSsaBb = ropBb.createBasicBlock();

            if (extraInstructions.isEmpty()) {
              needsGoto = true;
              sourcePosition = lastInstruction.getOriginalRopInsn().getPosition();
              il = new ArrayList<SsaInsn>(1);
            } else {
              Insn extraInsn = extraInstructions.get(0);
              needsGoto =
                  extraInstructions.get(extraInstructions.size() - 1)
                      .getOpcode().getBranchingness() == Rop.BRANCH_NONE;
              il = new ArrayList<SsaInsn>(extraInstructions.size() + (needsGoto ? 1 : 0));
              for (Insn inst : extraInstructions) {
                il.add(new NormalSsaInsn(inst, extraSsaBb));
              }
              sourcePosition = extraInsn.getPosition();
            }

            if (needsGoto) {
              il.add(new NormalSsaInsn(
                  new PlainInsn(Rops.GOTO, sourcePosition, null, RegisterSpecList.EMPTY),
                  extraSsaBb));
            }

            JBasicBlock primary = bb.getPrimarySuccessor();
            successors = IntList.makeImmutable(getBlockId(primary));
            extraSsaBb.setRopLabel(extraBlockLabel);
            extraSsaBb.setInsns(il);
            extraSsaBb.setSuccessors(successors, getBlockId(primary));
            return false;
          }

          @Override
          public boolean visit(@Nonnull JConditionalBasicBlock bb) {
            List<SsaInsn> il = createInsnList(instructions, 0);

            JBasicBlock primary = bb.getPrimarySuccessor();
            JBasicBlock secondary = bb.getAlternativeSuccessor();

            int primarySuccessor = getBlockId(primary);
            IntList successors = IntList.makeImmutable(primarySuccessor, getBlockId(secondary));

            ssaBb.setRopLabel(getBlockId(bb));
            ssaBb.setInsns(il);
            ssaBb.setSuccessors(successors, primarySuccessor);
            return false;
          }

          @Override
          public boolean visit(@Nonnull JSwitchBasicBlock bb) {
            IntList successors = new IntList();
            for (JBasicBlock caseBb : bb.getCases()) {
              successors.add(getBlockId(caseBb));
            }

            successors.add(getBlockId(bb.getDefaultCase()));

            successors.setImmutable();
            List<SsaInsn> il = createInsnList(instructions, 0);

            ssaBb.setRopLabel(getBlockId(bb));
            ssaBb.setInsns(il);
            ssaBb.setSuccessors(successors, successors.get(successors.size() - 1));
            return false;
          }

          @Override
          public boolean visit(@Nonnull JBasicBlock x) {
            throw new AssertionError("Not implemented yet: " + x.toString());
          }

          private SourcePosition getLastElementPosition(@Nonnull JBasicBlock bb) {
            return RopHelper.getSourcePosition(bb.hasElements() ?
                bb.getLastElement().getSourceInfo() : SourceInfo.UNKNOWN);
          }

          private void addCatchBlockSuccessors(
              @Nonnull List<JBasicBlock> catchBlocks,
              @Nonnull IntList successors) {
            for (JBasicBlock catchBlock : catchBlocks) {
              int catchTypeCount = 0;
              int catchTypesSize = ((JCatchBasicBlock) catchBlock).getCatchTypes().size();
              while (catchTypeCount++ < catchTypesSize) {
                successors.add(getBlockId(catchBlock));
              }
            }
          }

          private int getBlockId(@Nonnull JBasicBlock block) {
            return basicBlocks.get(block).intValue();
          }
        };

        visitor.accept(bb);
      }

      ssaMethod.setBlocks(ropBb.computeSsaBasicBlockList());
      ssaMethod.setRegisterCount(ropReg.getRegisterCount());
      ssaMethod.makeExitBlock();

      RopMethod ropMethod = null;
      if (runDxOptimizations) {
        if (!minimizeRegister) {
            ropMethod = Optimizer.optimize(ssaMethod, getParameterWordCount(method),
                method.isStatic(), true /* inPreserveLocals */, removeRedundantConditionalBranch,
                DexTranslationAdvice.THE_ONE);
          if (ropMethod.getBlocks().getRegCount() > DexTranslationAdvice.THE_ONE
              .getMaxOptimalRegisterCount()) {
              // Try to see if we can squeeze it under the register count bar
            buildSsaCodeItem(method, true);
          }
        } else {
          EnumSet<OptionalStep> steps = EnumSet.allOf(OptionalStep.class);
          steps.remove(OptionalStep.CONST_COLLECTOR);
          ropMethod = Optimizer.optimizeMinimizeRegisters(ssaMethod, getParameterWordCount(method),
              method.isStatic(), true /* inPreserveLocals */, removeRedundantConditionalBranch,
              DexTranslationAdvice.THE_ONE);
        }
      } else {
        ropMethod = Optimizer.optimize(ssaMethod, getParameterWordCount(method),
            method.isStatic(), true /* inPreserveLocals */, removeRedundantConditionalBranch,
            DexTranslationAdvice.THE_ONE, EnumSet.noneOf(OptionalStep.class));
      }

      DalvCode dalvCode;
      try (Event dopEvent = tracer.open(JackEventType.DOP_CREATION)) {
        dalvCode = createCode(method, ropMethod);
      }

      method.addMarker(new DexCodeMarker(new CodeItem(RopHelper.createMethodRef(method), dalvCode,
          method.isStatic(), createThrows(method))));
    }
  }

  @Nonnull
  private static TypeList createThrows(@Nonnull JMethod method) {
    ThrownExceptionMarker marker = method.getMarker(ThrownExceptionMarker.class);
    if (marker != null) {
      return RopHelper.createTypeList(marker.getThrownExceptions());
    } else {
      return StdTypeList.EMPTY;
    }
  }

  @Nonnull
  private List<SsaInsn> createInsnList(@Nonnull List<SsaInsn> instructions,
      @Nonnegative int extraSize) {
    List<SsaInsn> il = Lists.newArrayListWithCapacity(instructions.size() + extraSize);
    for (SsaInsn instruction : instructions) {
      il.add(instruction);
    }
    return il;
  }

  /**
   * Constructs and adds the blocks that perform setup for the rest of the method. This includes a
   * first block which merely contains assignments from parameters to the same-numbered registers
   * and a possible second block which deals with synchronization.
   */
  // TODO(mikaelpeltier) keep local variable information if required
  private void addSetupBlocks(@Nonnull JMethod method, @Nonnull SsaRopRegisterManager ropReg,
      @Nonnull SsaRopBasicBlockManager ropBb, @Nonnegative int entryNodeId) {
    SsaBasicBlock ssaBb = ropBb.createBasicBlock();
    SourcePosition pos = SourcePosition.NO_INFO;

    List<JParameter> parameters = method.getParams();
    int indexParam = 0;
    int sz = parameters.size();
    List<SsaInsn> insns;

    if (method.isStatic()) {
      // +1 is to reserve space for Goto instruction
      insns = new ArrayList<SsaInsn>(sz + 1);
    } else {
      // +2 is to reserve space for Goto instruction, and parameter 'this'
      insns = new ArrayList<SsaInsn>(sz + 2);
      JThis jThis = method.getThis();
      assert jThis != null;
      RegisterSpec thisReg = ropReg.createThisReg(jThis);
      Insn insn =
          new PlainCstInsn(Rops.opMoveParam(thisReg.getType()), pos, thisReg,
              RegisterSpecList.EMPTY, CstInteger.make(thisReg.getReg()));
      insns.add(new NormalSsaInsn(insn, ssaBb));
    }

    for (Iterator<JParameter> paramIt = parameters.iterator(); paramIt.hasNext(); indexParam++) {
      JParameter param = paramIt.next();
      RegisterSpec paramReg = ropReg.getOrCreateRegisterSpec(param);
      Insn insn =
          new PlainCstInsn(Rops.opMoveParam(paramReg.getType()), pos, paramReg,
              RegisterSpecList.EMPTY, CstInteger.make(paramReg.getReg()));
      insns.add(new NormalSsaInsn(insn, ssaBb));
    }

    insns
        .add(new NormalSsaInsn(new PlainInsn(Rops.GOTO, pos, null, RegisterSpecList.EMPTY), ssaBb));

    ssaBb.setRopLabel(ropBb.getSpecialLabel(SsaRopBasicBlockManager.PARAM_ASSIGNMENT));
    ssaBb.setInsns(insns);
    ssaBb.setSuccessors(IntList.makeImmutable(entryNodeId), entryNodeId);
  }

  @Nonnull
  private DalvCode createCode(@Nonnull JMethod method, @Nonnull RopMethod ropMethod) {
    DexOptions options = new DexOptions();
    options.forceJumbo = forceJumbo;
    options.targetApiLevel = apiLevel;
    int paramSize = getParameterWordCount(method);
    int positionListKind;
    LocalVariableInfo lvInfo;
    if (emitLocalDebugInfo) {
      lvInfo = LocalVariableExtractor.extract(ropMethod);
    } else {
      lvInfo = null;
    }
    if (emitLineNumberTable) {
      positionListKind = PositionList.LINES;
    } else {
      positionListKind = PositionList.NONE;
    }

    return RopTranslator.translate(ropMethod, positionListKind, lvInfo, paramSize, options);
  }

  @Nonnegative
  private int getParameterWordCount(@Nonnull JMethod method) {
    // Add size in word (1) to represent 'this' parameter if method is not static.
    int wordCount = method.isStatic() ? 0 : Type.OBJECT.getWordCount();

    for (JParameter param : method.getParams()) {
      wordCount += RopHelper.convertTypeToDx(param.getType()).getWordCount();
    }

    return wordCount;
  }
}
