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

import com.android.jack.JackEventType;
import com.android.jack.Options;
import com.android.jack.dx.dex.DexOptions;
import com.android.jack.dx.dex.code.DalvCode;
import com.android.jack.dx.dex.code.PositionList;
import com.android.jack.dx.dex.code.RopTranslator;
import com.android.jack.dx.dex.file.CodeItem;
import com.android.jack.dx.rop.code.DexTranslationAdvice;
import com.android.jack.dx.rop.code.Insn;
import com.android.jack.dx.rop.code.InsnList;
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
import com.android.jack.dx.ssa.Optimizer;
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
import com.android.jack.ir.ast.cfg.ExceptionHandlingContext;
import com.android.jack.ir.ast.cfg.JBasicBlock;
import com.android.jack.ir.ast.cfg.JBasicBlockElement;
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
import com.android.jack.library.DumpInLibrary;
import com.android.jack.library.PrebuiltCompatibility;
import com.android.jack.optimizations.cfg.CfgBasicBlockUtils;
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
import com.android.jack.util.AndroidApiLevel;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.log.Event;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;

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
@HasKeyId
@Description("Builds CodeItem from JMethod")
@Name("CodeItemBuilder")
@Constraint(
    need = {
        JMethodBodyCfg.class,
        JExceptionRuntimeValue.class,
        NewInstanceRemoved.class,
        ThreeAddressCodeForm.class,
        RopLegalCast.class,
        InnerAccessor.class,
        InvalidDefaultBridgeInInterfaceRemoved.class
    },
    no = {
        JSsaVariableRef.class,
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
public class CodeItemBuilder implements RunnableSchedulable<JMethod> {

  @Nonnull
  public static final BooleanPropertyId EMIT_SYNTHETIC_LOCAL_DEBUG_INFO =
      BooleanPropertyId.create(
              "jack.dex.debug.vars.synthetic",
              "Emit synthetic local variable debug info into generated dex")
          .addDefaultValue(Boolean.FALSE)
          .addCategory(DumpInLibrary.class)
          .addCategory(PrebuiltCompatibility.class);

  @Nonnull
  public static final BooleanPropertyId DEX_OPTIMIZE =
      BooleanPropertyId.create("jack.dex.optimize", "Define if Dex optimizations are activated")
          .addDefaultValue(Boolean.TRUE)
          .addCategory(DumpInLibrary.class)
          .addCategory(PrebuiltCompatibility.class);

  @Nonnull
  public static final BooleanPropertyId FORCE_JUMBO =
      BooleanPropertyId.create(
              "jack.dex.forcejumbo", "Force string opcodes to be emitted as jumbo in dex")
          .addDefaultValue(Boolean.TRUE)
          .addCategory(DumpInLibrary.class)
          .addCategory(PrebuiltCompatibility.class);

  @Nonnull
  public static final BooleanPropertyId OPTIMIZE_BRANCHES =
      BooleanPropertyId.create("jack.dex.optimizebranches", "Remove redundant branches in dex")
          .addDefaultValue(Boolean.TRUE)
          .addCategory(DumpInLibrary.class)
          .addCategory(PrebuiltCompatibility.class);

  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);
  private final boolean emitSyntheticLocalDebugInfo =
      ThreadConfig.get(EMIT_SYNTHETIC_LOCAL_DEBUG_INFO).booleanValue();
  private final boolean emitLocalDebugInfo =
      ThreadConfig.get(Options.EMIT_LOCAL_DEBUG_INFO).booleanValue();
  private final boolean runDxOptimizations = ThreadConfig.get(DEX_OPTIMIZE).booleanValue();
  private final boolean forceJumbo = ThreadConfig.get(FORCE_JUMBO).booleanValue();
  private final boolean removeRedundantConditionalBranch =
      ThreadConfig.get(OPTIMIZE_BRANCHES).booleanValue();
  @Nonnull
  private final AndroidApiLevel apiLevel = ThreadConfig.get(Options.ANDROID_MIN_API_LEVEL);
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

    try (Event event = tracer.open(JackEventType.DX_BACKEND)) {
      RopRegisterManager ropReg =
          new RopRegisterManager(emitLocalDebugInfo, emitSyntheticLocalDebugInfo);

      JAbstractMethodBody body = method.getBody();
      assert body instanceof JMethodBodyCfg;
      JControlFlowGraph cfg = ((JMethodBodyCfg) body).getCfg();

      final JEntryBasicBlock entryBasicBlock = cfg.getEntryBlock();
      final JExitBasicBlock exitBasicBlock = cfg.getExitBlock();

      // Before building code item, we clean all exception handling
      // context and all weakly referenced catch blocks.
      removeExceptionHandlingContext(cfg);

      final Map<JBasicBlock, Integer> basicBlocks = new LinkedHashMap<>();
      int blockId = 1; // 0 is reserved for entry block
      for (JBasicBlock block : cfg.getReachableBlocksDepthFirst()) {
        if (block != entryBasicBlock && block != exitBasicBlock) {
          basicBlocks.put(block, Integer.valueOf(blockId++));
        }
      }

      // We assume that there are no *internal* blocks in cfg
      // that are not reachable from entry block
      assert basicBlocks.size() == cfg.getInternalBlocksUnordered().size();

      final RopBasicBlockManager ropBb = new RopBasicBlockManager(blockId + 1);
      JBasicBlock firstBlockOfCode = entryBasicBlock.getOnlySuccessor();
      addSetupBlocks(method, ropReg, ropBb, basicBlocks.get(firstBlockOfCode).intValue());

      if (method.getType() != JPrimitiveTypeEnum.VOID.getType()) {
        ropReg.createReturnReg(method.getType());
      }

      for (JBasicBlock bb : basicBlocks.keySet()) {
        assert bb != entryBasicBlock && bb != exitBasicBlock;
        final RopBuilderVisitor ropBuilder = new RopBuilderVisitor(ropReg, bb, apiLevel);

        ropBuilder.processBasicBlockElements();
        final List<Insn> instructions = ropBuilder.getInstructions();
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
            InsnList il = createInsnList(instructions, 1);
            Insn gotoInstruction = new PlainInsn(
                Rops.GOTO, getLastElementPosition(bb), null, RegisterSpecList.EMPTY);
            il.set(instructions.size(), gotoInstruction);
            il.setImmutable();
            ropBb.createBasicBlock(getBlockId(bb), il, successors, getBlockId(primarySuccessor));
            return false;
          }

          @Override
          public boolean visit(@Nonnull JReturnBasicBlock bb) {
            InsnList il = createInsnList(instructions, 0);
            il.setImmutable();
            ropBb.createBasicBlock(getBlockId(bb), il, IntList.EMPTY, -1);
            return false;
          }

          @Override
          public boolean visit(@Nonnull JThrowBasicBlock bb) {
            InsnList il = createInsnList(instructions, 0);
            il.setImmutable();

            IntList successors = new IntList();
            int primarySuccessor = -1;

            if (!bb.getCatchBlocks().isEmpty()) {
              addCatchBlockSuccessors(bb.getCatchBlocks(), successors);
            }
            successors.setImmutable();

            ropBb.createBasicBlock(getBlockId(bb), il, successors, primarySuccessor);
            return false;
          }

          @Override
          public boolean visit(@Nonnull JThrowingExpressionBasicBlock bb) {
            Insn lastInstruction = instructions.get(instructions.size() - 1);
            List<Insn> extraInstructions = ropBuilder.getExtraInstructions();
            assert extraInstructions != null;

            InsnList il = createInsnList(instructions, 0);
            il.setImmutable();

            int extraBlockLabel = ropBb.getAvailableLabel();

            IntList successors = new IntList();
            addCatchBlockSuccessors(bb.getCatchBlocks(), successors);

            successors.add(extraBlockLabel);
            successors.setImmutable();

            ropBb.createBasicBlock(getBlockId(bb), il, successors, extraBlockLabel);

            int indexInstruction = 0;
            boolean needsGoto;
            SourcePosition sourcePosition;
            if (extraInstructions.isEmpty()) {
              needsGoto = true;
              sourcePosition = lastInstruction.getPosition();
              il = new InsnList(1);
            } else {
              Insn extraInsn = extraInstructions.get(0);
              needsGoto =
                  extraInstructions.get(extraInstructions.size() - 1)
                      .getOpcode().getBranchingness() == Rop.BRANCH_NONE;
              il = new InsnList(extraInstructions.size() + (needsGoto ? 1 : 0));
              for (Insn inst : extraInstructions) {
                il.set(indexInstruction++, inst);
              }
              sourcePosition = extraInsn.getPosition();
            }

            if (needsGoto) {
              il.set(indexInstruction,
                  new PlainInsn(Rops.GOTO, sourcePosition, null, RegisterSpecList.EMPTY));
            }

            il.setImmutable();

            JBasicBlock primary = bb.getPrimarySuccessor();
            successors = IntList.makeImmutable(getBlockId(primary));

            ropBb.createBasicBlock(extraBlockLabel, il, successors, getBlockId(primary));
            return false;
          }

          @Override
          public boolean visit(@Nonnull JConditionalBasicBlock bb) {
            InsnList il = createInsnList(instructions, 0);
            il.setImmutable();

            JBasicBlock primary = bb.getPrimarySuccessor();
            JBasicBlock secondary = bb.getAlternativeSuccessor();

            int primarySuccessor = getBlockId(primary);
            IntList successors = IntList.makeImmutable(primarySuccessor, getBlockId(secondary));

            ropBb.createBasicBlock(getBlockId(bb), il, successors, primarySuccessor);
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
            InsnList il = createInsnList(instructions, 0);
            il.setImmutable();
            ropBb.createBasicBlock(
                getBlockId(bb), il, successors, successors.get(successors.size() - 1));
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
            if (block == entryBasicBlock) {
              return 0;
            }
            if (block == exitBasicBlock) {
              return Integer.MAX_VALUE;
            }
            return basicBlocks.get(block).intValue();
          }
        };

        visitor.accept(bb);
      }

      RopMethod ropMethod =
          new RopMethod(ropBb.getBasicBlockList(),
              ropBb.getSpecialLabel(RopBasicBlockManager.PARAM_ASSIGNMENT));

      if (runDxOptimizations) {
        try (Event optEvent = tracer.open(JackEventType.DX_OPTIMIZATION)) {
          ropMethod =
              Optimizer.optimize(
                  ropMethod,
                  getParameterWordCount(method),
                  method.isStatic(),
                  true /* inPreserveLocals */,
                  removeRedundantConditionalBranch,
                  DexTranslationAdvice.THE_ONE);
        }
      }

      DalvCode dalvCode;
      try (Event dopEvent = tracer.open(JackEventType.DOP_CREATION)) {
        dalvCode = createCode(method, ropMethod);
      }

      method.addMarker(new DexCodeMarker(new CodeItem(RopHelper.createMethodRef(method), dalvCode,
          method.isStatic(), createThrows(method))));
    }
  }

  private void removeExceptionHandlingContext(@Nonnull JControlFlowGraph cfg) {
    for (JBasicBlock block : cfg.getInternalBlocksUnordered()) {
      for (JBasicBlockElement element : block.getElements(true)) {
        element.resetEHContext(ExceptionHandlingContext.EMPTY);
      }
    }
    new CfgBasicBlockUtils(cfg).removeUnreachableBlocks();
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
  private InsnList createInsnList(@Nonnull List<Insn> instructions, @Nonnegative int extraSize) {
    InsnList il = new InsnList(instructions.size() + extraSize);
    int indexInstruction = 0;
    for (Insn instruction : instructions) {
      il.set(indexInstruction++, instruction);
    }
    return il;
  }

  /**
   * Constructs and adds the blocks that perform setup for the rest of the method. This includes a
   * first block which merely contains assignments from parameters to the same-numbered registers
   * and a possible second block which deals with synchronization.
   */
  // TODO(mikaelpeltier) keep local variable information if required
  private void addSetupBlocks(@Nonnull JMethod method, @Nonnull RopRegisterManager ropReg,
      @Nonnull RopBasicBlockManager ropBb, @Nonnegative int entryNodeId) {
    SourcePosition pos = SourcePosition.NO_INFO;

    List<JParameter> parameters = method.getParams();
    int indexParam = 0;
    int sz = parameters.size();
    InsnList insns;

    if (method.isStatic()) {
      // +1 is to reserve space for Goto instruction
      insns = new InsnList(sz + 1);
    } else {
      // +2 is to reserve space for Goto instruction, and parameter 'this'
      insns = new InsnList(sz + 2);
      JThis jThis = method.getThis();
      assert jThis != null;
      RegisterSpec thisReg = ropReg.createThisReg(jThis);
      Insn insn =
          new PlainCstInsn(Rops.opMoveParam(thisReg.getType()), pos, thisReg,
              RegisterSpecList.EMPTY, CstInteger.make(thisReg.getReg()));
      insns.set(indexParam++, insn);
    }

    for (Iterator<JParameter> paramIt = parameters.iterator(); paramIt.hasNext(); indexParam++) {
      JParameter param = paramIt.next();
      RegisterSpec paramReg = ropReg.getOrCreateRegisterSpec(param);
      Insn insn =
          new PlainCstInsn(Rops.opMoveParam(paramReg.getType()), pos, paramReg,
              RegisterSpecList.EMPTY, CstInteger.make(paramReg.getReg()));
      insns.set(indexParam, insn);
    }

    insns.set(indexParam, new PlainInsn(Rops.GOTO, pos, null, RegisterSpecList.EMPTY));
    insns.setImmutable();

    ropBb.createBasicBlock(ropBb.getSpecialLabel(RopBasicBlockManager.PARAM_ASSIGNMENT), insns,
        IntList.makeImmutable(entryNodeId), entryNodeId);
  }

  @Nonnull
  private DalvCode createCode(@Nonnull JMethod method, @Nonnull RopMethod ropMethod) {
    DexOptions options = new DexOptions(apiLevel, forceJumbo);
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
