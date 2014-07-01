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
import com.android.jack.cfg.BasicBlock;
import com.android.jack.cfg.CatchBasicBlock;
import com.android.jack.cfg.ConditionalBasicBlock;
import com.android.jack.cfg.ControlFlowGraph;
import com.android.jack.cfg.NormalBasicBlock;
import com.android.jack.cfg.PeiBasicBlock;
import com.android.jack.cfg.ReturnBasicBlock;
import com.android.jack.cfg.SwitchBasicBlock;
import com.android.jack.cfg.ThrowBasicBlock;
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
import com.android.jack.dx.rop.type.TypeList;
import com.android.jack.dx.ssa.Optimizer;
import com.android.jack.dx.util.IntList;
import com.android.jack.ir.SideEffectOperation;
import com.android.jack.ir.ast.JAbstractMethodBody;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JAssertStatement;
import com.android.jack.ir.ast.JConcatOperation;
import com.android.jack.ir.ast.JConditionalExpression;
import com.android.jack.ir.ast.JConditionalOperation;
import com.android.jack.ir.ast.JExceptionRuntimeValue;
import com.android.jack.ir.ast.JFieldInitializer;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLoop;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JMultiExpression;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.marker.ThrownExceptionMarker;
import com.android.jack.scheduling.marker.DexCodeMarker;
import com.android.jack.scheduling.marker.DexFileMarker;
import com.android.jack.transformations.EmptyClinit;
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
import com.android.jack.transformations.booleanoperators.FallThroughMarker;
import com.android.jack.transformations.rop.cast.RopLegalCast;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.log.Event;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * CodeItemBuilder is a schedulable that generates {@link CodeItem} from {@link JMethod}. The
 * generated {@link CodeItem} is saved into the {@link DexCodeMarker}.
 */
@HasKeyId
@Description("Builds CodeItem from JMethod")
@Name("CodeItemBuilder")
@Constraint(need = {ControlFlowGraph.class,
    JExceptionRuntimeValue.class,
    NewInstanceRemoved.class,
    ThreeAddressCodeForm.class,
    RopLegalCast.class,
    InnerAccessor.class,
    DexFileMarker.class},
    no = {BooleanTestOutsideIf.class,
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
    UselessSwitches.class})
@Transform(add = DexCodeMarker.class)
public class CodeItemBuilder implements RunnableSchedulable<JMethod> {

  @Nonnull
  public static final BooleanPropertyId EMIT_SYNTHETIC_LOCAL_DEBUG_INFO = BooleanPropertyId.create(
      "jack.dex.debug.vars.synthetic",
      "Emit synthetic local variable debug info into generated dex").addDefaultValue(Boolean.FALSE);

  @Nonnull
  public static final BooleanPropertyId DEX_OPTIMIZE = BooleanPropertyId.create(
      "jack.dex.optimize", "Define if Dex optimizations are activated")
      .addDefaultValue(Boolean.TRUE);

  @Nonnull
  public static final BooleanPropertyId FORCE_JUMBO = BooleanPropertyId.create(
      "jack.dex.forcejumbo", "Force string opcodes to be emitted as jumbo in dex")
      .addDefaultValue(Boolean.FALSE);

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);
  private final boolean emitSyntheticLocalDebugInfo =
      ThreadConfig.get(EMIT_SYNTHETIC_LOCAL_DEBUG_INFO).booleanValue();
  private final boolean emitLocalDebugInfo =
      ThreadConfig.get(Options.EMIT_LOCAL_DEBUG_INFO).booleanValue();
  private final boolean runDxOptimizations =
      ThreadConfig.get(DEX_OPTIMIZE).booleanValue();
  private final boolean forceJumbo = ThreadConfig.get(FORCE_JUMBO).booleanValue()
      | ThreadConfig.get(Options.GENERATE_ONE_DEX_PER_TYPE).booleanValue();
  private final boolean emitLineNumberTable =
      ThreadConfig.get(Options.EMIT_LINE_NUMBER_DEBUG_INFO).booleanValue();

  @Override
  public void run(@Nonnull JMethod method) throws Exception {

    if (method.getEnclosingType().isExternal()
        || method.isNative()
        || method.isAbstract()
        || !filter.accept(this.getClass(), method)) {
      return;
    }

    RopRegisterManager ropReg =
        new RopRegisterManager(emitLocalDebugInfo, emitSyntheticLocalDebugInfo);

    ControlFlowGraph cfg = method.getMarker(ControlFlowGraph.class);
    assert cfg != null;

    RopBasicBlockManager ropBb = new RopBasicBlockManager(getMaxLabel(cfg));
    assert cfg.getEntryNode().getSuccessors().size() == 1;
    BasicBlock firstBlockOfCode = cfg.getEntryNode().getSuccessors().get(0);
    assert firstBlockOfCode != null;
    addSetupBlocks(method, ropReg, ropBb, firstBlockOfCode.getId());

    JAbstractMethodBody body = method.getBody();
    assert body instanceof JMethodBody;
    for (JLocal local : ((JMethodBody) body).getLocals()) {
      ropReg.createRegisterSpec(local);
    }

    if (!method.getType().equals(JPrimitiveTypeEnum.VOID.getType())) {
      ropReg.createReturnReg(method.getType());
    }

    for (BasicBlock bb : cfg.getNodes()) {
      if (bb == cfg.getEntryNode()) {
        continue;
      }
      if (bb instanceof CatchBasicBlock) {
        ropReg.createRegisterSpec(((CatchBasicBlock) bb).getCatchVar());
      }
      RopBuilderVisitor ropBuilder = new RopBuilderVisitor(ropReg, bb);

      assert !bb.getStatements().isEmpty();

      ropBuilder.accept(bb.getStatements());
      List<Insn> instructions = ropBuilder.getInstructions();
      assert instructions != null;
      JStatement lastStmt = bb.getLastInstruction();
      SourcePosition lastStmtsourcePosition = RopHelper.getSourcePosition(lastStmt);

      // TODO(mikaelpeltier) Think about a better solution to take into account control flow
      // (perhaps with meta on cfg).
      if (bb instanceof ReturnBasicBlock) {
        InsnList il = createInsnList(instructions, 0);
        il.setImmutable();
        ropBb.createBasicBlock(bb.getId(), il, IntList.EMPTY, -1);
      } else if (bb instanceof ConditionalBasicBlock) {
        InsnList il = createInsnList(instructions, 0);
        il.setImmutable();
        BasicBlock primary = ((ConditionalBasicBlock) bb).getThenBlock();
        BasicBlock secondary = ((ConditionalBasicBlock) bb).getElseBlock();

        FallThroughMarker ftm = lastStmt.getMarker(FallThroughMarker.class);
        if (ftm != null) {
          switch (ftm.getFallThrough()) {
            case ELSE: {
              primary = ((ConditionalBasicBlock) bb).getElseBlock();
              secondary = ((ConditionalBasicBlock) bb).getThenBlock();
              break;
            }
            case THEN: {
              primary = ((ConditionalBasicBlock) bb).getThenBlock();
              secondary = ((ConditionalBasicBlock) bb).getElseBlock();
              break;
            }
            default: {
              throw new AssertionError();
            }
          }
        } else {
          primary = ((ConditionalBasicBlock) bb).getThenBlock();
          secondary = ((ConditionalBasicBlock) bb).getElseBlock();
        }

        assert primary != null;
        assert secondary != null;
        int primarySuccessor = primary.getId();
        IntList successors = IntList.makeImmutable(primarySuccessor, secondary.getId());

        ropBb.createBasicBlock(bb.getId(), il, successors, primarySuccessor);
      } else if (bb instanceof ThrowBasicBlock) {
        ThrowBasicBlock throwBlock = (ThrowBasicBlock) bb;
        InsnList il = createInsnList(instructions, 0);
        il.setImmutable();

        IntList successors = new IntList();
        int primarySuccessor = -1;

        if (!throwBlock.getExceptionBlocks().isEmpty()) {
          addCatchBlockSuccessors(throwBlock.getExceptionBlocks(), successors);
        }
        successors.setImmutable();

        ropBb.createBasicBlock(bb.getId(), il, successors, primarySuccessor);

      } else if (bb instanceof PeiBasicBlock) {
        PeiBasicBlock peiBlock = (PeiBasicBlock) bb;
        Insn lastInstruction = instructions.get(instructions.size() - 1);

        List<Insn> extraInstructions = ropBuilder.getExtraInstructions();
        assert extraInstructions != null;

        InsnList il = createInsnList(instructions, 0);
        il.setImmutable();

        int extraBlockLabel = ropBb.getAvailableLabel();

        IntList successors = new IntList();
        addCatchBlockSuccessors(peiBlock.getExceptionBlocks(), successors);

        successors.add(extraBlockLabel);
        successors.setImmutable();

        ropBb.createBasicBlock(bb.getId(), il, successors, extraBlockLabel);

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
              extraInstructions.get(extraInstructions.size() - 1).getOpcode().getBranchingness()
              == Rop.BRANCH_NONE;
          il = new InsnList(extraInstructions.size() + (needsGoto ? 1 : 0));
          for (Insn inst : extraInstructions) {
            il.set(indexInstruction++, inst);
          }
          sourcePosition = extraInsn.getPosition();
        }

        if (needsGoto) {
          il.set(indexInstruction++, new PlainInsn(Rops.GOTO, sourcePosition, null,
              RegisterSpecList.EMPTY));
        }

        il.setImmutable();
        BasicBlock primarySuccessor = ((PeiBasicBlock) bb).getTarget();
        assert primarySuccessor != null;

        successors = IntList.makeImmutable(primarySuccessor.getId());

        ropBb.createBasicBlock(extraBlockLabel, il, successors, primarySuccessor.getId());
      } else if (bb instanceof SwitchBasicBlock) {
        IntList successors = new IntList();
        for (BasicBlock succ : ((SwitchBasicBlock) bb).getCasesBlock()) {
          successors.add(succ.getId());
        }

        int defaultIdBlock = ((SwitchBasicBlock) bb).getDefaultBlock().getId();
        successors.add(defaultIdBlock);

        successors.setImmutable();
        InsnList il = createInsnList(instructions, 0);
        il.setImmutable();
        ropBb.createBasicBlock(bb.getId(), il, successors, successors.get(successors.size() - 1));
      } else if (bb instanceof NormalBasicBlock) {
        List<BasicBlock> bbSuccessors = bb.getSuccessors();
        assert bbSuccessors.size() == 1;
        int primarySuccessor = bbSuccessors.get(0).getId();
        IntList successors = IntList.makeImmutable(primarySuccessor);
        InsnList il = createInsnList(instructions, 1);
        Insn gotoInstruction =
            new PlainInsn(Rops.GOTO, lastStmtsourcePosition, null, RegisterSpecList.EMPTY);
        il.set(instructions.size(), gotoInstruction);
        il.setImmutable();
        ropBb.createBasicBlock(bb.getId(), il, successors, primarySuccessor);
      } else {
        throw new AssertionError("Not yet supported");
      }
    }

    RopMethod ropMethod =
        new RopMethod(ropBb.getBasicBlockList(),
            ropBb.getSpecialLabel(RopBasicBlockManager.PARAM_ASSIGNMENT));

    Tracer tracer = TracerFactory.getTracer();

    if (runDxOptimizations) {
      Event optEvent = tracer.start(JackEventType.DX_OPTIMIZATION);

      try {
        ropMethod =
            Optimizer.optimize(ropMethod, getParameterSize(method), method.isStatic(),
                true /* inPreserveLocals */, DexTranslationAdvice.THE_ONE);
      } finally {
        optEvent.end();
      }
    }

    Event dopEvent = tracer.start(JackEventType.DOP_CREATION);
    DalvCode dalvCode;
    try {
      dalvCode = createCode(method, ropMethod);
    } finally {
      dopEvent.end();
    }

    method.addMarker(new DexCodeMarker(new CodeItem(RopHelper.createMethodRef(method), dalvCode,
        method.isStatic(), createThrows(method))));
  }

  private void addCatchBlockSuccessors(@Nonnull List<CatchBasicBlock> catchBlocks,
      @Nonnull IntList successors) {
    for (CatchBasicBlock catchblock : catchBlocks) {
      int catchTypeCount = 0;
      int catchTypesSize = catchblock.getCatchTypes().size();
      while (catchTypeCount++ < catchTypesSize) {
        successors.add(catchblock.getId());
      }
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

  private int getParameterSize(@Nonnull JMethod method) {
    int paramSize = 0;
    if (!method.isStatic()) {
      paramSize += 1;
    }
    for (JParameter param : method.getParams()) {
      if (param.getType().equals(JPrimitiveTypeEnum.LONG.getType())
          || param.getType().equals(JPrimitiveTypeEnum.DOUBLE.getType())) {
        paramSize += 2;
      } else {
        paramSize += 1;
      }
    }
    return paramSize;
  }

  private int getMaxLabel(ControlFlowGraph cfg) {
    int maxLabel = -1;

    for (BasicBlock bb : cfg.getNodes()) {
      int bbId = bb.getId();
      if (bbId > maxLabel) {
        maxLabel = bbId;
      }
    }

    // maxLabel is exclusive, thus add +1
    maxLabel++;
    return maxLabel;
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
      RegisterSpec thisReg = ropReg.createThisReg(method.getEnclosingType());
      Insn insn =
          new PlainCstInsn(Rops.opMoveParam(thisReg.getType()), pos, thisReg,
              RegisterSpecList.EMPTY, CstInteger.make(thisReg.getReg()));
      insns.set(indexParam++, insn);
    }

    for (Iterator<JParameter> paramIt = parameters.iterator(); paramIt.hasNext(); indexParam++) {
      JParameter param = paramIt.next();
      RegisterSpec paramReg = ropReg.createRegisterSpec(param);
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
    DexFileMarker dexFileMarker =
        method.getEnclosingType().getSession().getMarker(DexFileMarker.class);
    assert dexFileMarker != null;

    DexOptions options = dexFileMarker.getFinalDexFile().getDexOptions();
    options.forceJumbo = forceJumbo;
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

  private int getParameterWordCount(@Nonnull JMethod method) {
    List<JParameter> parameters = method.getParams();
    // Add size in word (1) to represent 'this' parameter if method is not static.
    int wordCount = method.isStatic() ? 0 : 1;
    for (JParameter param : parameters) {
      JType paramType = param.getType();
      if (paramType.equals(JPrimitiveTypeEnum.LONG.getType())) {
        wordCount += 2;
      } else if (paramType.equals(JPrimitiveTypeEnum.DOUBLE.getType())) {
        wordCount += 2;
      } else {
        wordCount++;
      }
    }
    return wordCount;
  }
}
