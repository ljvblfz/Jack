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

import com.android.jack.cfg.BasicBlock;
import com.android.jack.cfg.CatchBasicBlock;
import com.android.jack.cfg.PeiBasicBlock;
import com.android.jack.cfg.SwitchBasicBlock;
import com.android.jack.dx.rop.code.FillArrayDataInsn;
import com.android.jack.dx.rop.code.Insn;
import com.android.jack.dx.rop.code.PlainCstInsn;
import com.android.jack.dx.rop.code.PlainInsn;
import com.android.jack.dx.rop.code.RegisterSpec;
import com.android.jack.dx.rop.code.RegisterSpecList;
import com.android.jack.dx.rop.code.Rop;
import com.android.jack.dx.rop.code.Rops;
import com.android.jack.dx.rop.code.SourcePosition;
import com.android.jack.dx.rop.code.SwitchInsn;
import com.android.jack.dx.rop.code.ThrowingCstInsn;
import com.android.jack.dx.rop.code.ThrowingInsn;
import com.android.jack.dx.rop.cst.Constant;
import com.android.jack.dx.rop.cst.CstDouble;
import com.android.jack.dx.rop.cst.CstFieldRef;
import com.android.jack.dx.rop.cst.CstFloat;
import com.android.jack.dx.rop.cst.CstInteger;
import com.android.jack.dx.rop.cst.CstKnownNull;
import com.android.jack.dx.rop.cst.CstLong;
import com.android.jack.dx.rop.cst.CstMethodRef;
import com.android.jack.dx.rop.cst.CstType;
import com.android.jack.dx.rop.type.Prototype;
import com.android.jack.dx.rop.type.StdTypeList;
import com.android.jack.dx.rop.type.Type;
import com.android.jack.dx.rop.type.TypeBearer;
import com.android.jack.dx.rop.type.TypeList;
import com.android.jack.dx.util.IntList;
import com.android.jack.ir.ast.FieldKind;
import com.android.jack.ir.ast.JAbsentArrayDimension;
import com.android.jack.ir.ast.JAbstractStringLiteral;
import com.android.jack.ir.ast.JAlloc;
import com.android.jack.ir.ast.JArrayLength;
import com.android.jack.ir.ast.JArrayRef;
import com.android.jack.ir.ast.JArrayType;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JBinaryOperator;
import com.android.jack.ir.ast.JBooleanLiteral;
import com.android.jack.ir.ast.JByteLiteral;
import com.android.jack.ir.ast.JCaseStatement;
import com.android.jack.ir.ast.JCatchBlock;
import com.android.jack.ir.ast.JCharLiteral;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassLiteral;
import com.android.jack.ir.ast.JDoubleLiteral;
import com.android.jack.ir.ast.JDynamicCastOperation;
import com.android.jack.ir.ast.JExceptionRuntimeValue;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JFloatLiteral;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JInstanceOf;
import com.android.jack.ir.ast.JIntLiteral;
import com.android.jack.ir.ast.JIntegralConstant32;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JLambda;
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JLock;
import com.android.jack.ir.ast.JLongLiteral;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodCall.DispatchKind;
import com.android.jack.ir.ast.JNewArray;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.ir.ast.JParameterRef;
import com.android.jack.ir.ast.JPrefixNotOperation;
import com.android.jack.ir.ast.JPrimitiveType;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JReferenceType;
import com.android.jack.ir.ast.JReinterpretCastOperation;
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JShortLiteral;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.ir.ast.JThrowStatement;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JUnaryOperation;
import com.android.jack.ir.ast.JUnlock;
import com.android.jack.ir.ast.JValueLiteral;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.types.JIntegralType32;
import com.android.jack.transformations.booleanoperators.FallThroughMarker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

class RopBuilderVisitor extends JVisitor {

  @Nonnull
  private final RopRegisterManager ropReg;

  @CheckForNull
  private List<Insn> instructions;

  @CheckForNull
  private List<Insn> extraInstructions;

  @Nonnull
  private final BasicBlock currentBasicBlock;

  /**
   * A guard for {@code instructions}. Does not protect {@code extraInstructions}.
   */
  private boolean noMoreInstruction = true;

  private class AssignBuilderVisitor extends JVisitor {

    @Nonnull
    private final JStatement declaration;
    @Nonnull
    private final RegisterSpec destReg;
    @Nonnull
    SourcePosition sourcePosition;

    public AssignBuilderVisitor(@Nonnull JStatement declaration, @Nonnull JVariableRef destRef) {
      this.declaration = declaration;
      this.destReg = ropReg.getRegisterSpec(destRef);
      this.sourcePosition = RopHelper.getSourcePosition(declaration);
    }

    @Override
    public boolean visit(@Nonnull JNode node) {
      throw new AssertionError(declaration.toSource() + " not yet supported.");
    }

    @Override
    public boolean visit(@Nonnull JAlloc alloc) {
      buildAlloc(destReg, alloc, sourcePosition);
      return false;
    }

    @Override
    public boolean visit(@Nonnull JArrayLength arrayLength) {
      buildArrayLength(destReg, arrayLength);
      return false;
    }

    @Override
    public boolean visit(@Nonnull JArrayRef arrayRef) {
      buildArrayRead(destReg, arrayRef, sourcePosition);
      return false;
    }

    @Override
    public boolean visit(@Nonnull JBinaryOperation binOp) {
      buildBinaryOperation(destReg, binOp);
      return false;
    }

    @Override
    public boolean visit(@Nonnull JReinterpretCastOperation cast) {
      // Nothing to do it is a nop operation, generate it by a mov instruction.
      SourcePosition sourcePosition = RopHelper.getSourcePosition(cast);
      RegisterSpec fromReg = getRegisterSpec(cast.getExpr());
      RegisterSpecList sources = RegisterSpecList.make(fromReg);
      addInstruction(new PlainInsn(
          Rops.opMove(fromReg.getTypeBearer()), sourcePosition,
          destReg, sources));
      return false;
    }

    @Override
    public boolean visit(@Nonnull JDynamicCastOperation cast) {
      buildCast(destReg, cast);
      return false;
    }

    @Override
    public boolean visit(@Nonnull JFieldRef fieldRef) {
      buildReadField(destReg, fieldRef, sourcePosition);
      return false;
    }

    @Override
    public boolean visit(@Nonnull JInstanceOf instanceOf) {
      buildInstanceOf(destReg, instanceOf);
      return false;
    }

    @Override
    public boolean visit(@Nonnull JLocalRef localRef) {
      buildAssignVariableRef(destReg, localRef, sourcePosition);
      return false;
    }

    @Override
    public boolean visit(@Nonnull JParameterRef paramRef) {
      buildAssignVariableRef(destReg, paramRef, sourcePosition);
      return false;
    }

    @Override
    public boolean visit(@Nonnull JLambda lambda) {
      throw new AssertionError();
    }

    @Override
    public boolean visit(@Nonnull JMethodCall call) {
      buildCall(destReg, call);
      return false;
    }

    @Override
    public boolean visit(@Nonnull JThisRef thisRef) {
      RegisterSpec valueReg = ropReg.getRegisterSpec(thisRef);
      RegisterSpecList sources = RegisterSpecList.make(valueReg);
      addInstruction(new PlainInsn(
          Rops.opMove(valueReg.getTypeBearer()), sourcePosition,
          destReg, sources));
      return false;
    }

    @Override
    public boolean visit(@Nonnull JUnaryOperation unaryOp) {
      buildUnaryOperation(destReg, unaryOp);
      return false;
    }

    @Override
    public boolean visit(@Nonnull JValueLiteral valueLit) {
      buildConstant(destReg, valueLit);
      return false;
    }

    @Override
    public boolean visit(@Nonnull JClassLiteral literal) {
      Constant cst = RopHelper.getCstType(literal.getRefType());

      JType type = literal.getType();

      Rop constOp = Rops.opConst(RopHelper.convertTypeToDx(type));
      SourcePosition literalSrcPos = RopHelper.getSourcePosition(literal);
      Insn constInst = new ThrowingCstInsn(constOp, literalSrcPos,
          RegisterSpecList.EMPTY, getCatchTypes(), cst);
      addInstruction(constInst);
      addMoveResultPseudoAsExtraInstruction(destReg, literalSrcPos);
      return false;
    }

    private boolean isDexFilledNewArrayCompatible(@Nonnull JNewArray newArray) {
      JType elementType = newArray.getArrayType().getElementType();
      List<JExpression> initializers = newArray.getInitializers();
      if (!initializers.isEmpty() && initializers.size() <= 5 && newArray.getDims().size() == 1
          && elementType == JPrimitiveTypeEnum.INT.getType()) {
            return true;
      }
      return false;
    }

    @Override
    public boolean visit(@Nonnull JNewArray newArray) {
      JArrayType type = newArray.getType();
      CstType cstType = RopHelper.getCstType(type);
      SourcePosition newArraySourcePosition = RopHelper.getSourcePosition(newArray);
      List<JExpression> valuesSize = newArray.getInitializers();

      if (isDexFilledNewArrayCompatible(newArray)) {
        // Array with few initializer uses filled-new-array instructions
        int i = 0;
        RegisterSpecList sources = new RegisterSpecList(valuesSize.size());
        for (JExpression expr : valuesSize) {
          sources.set(i++, getRegisterSpec(expr));
        }

        Type arrayType = RopHelper.convertTypeToDx(newArray.getType());
        Rop op = Rops.opFilledNewArray(arrayType, valuesSize.size());

        Insn insn =
            new ThrowingCstInsn(op, newArraySourcePosition, sources, getCatchTypes(), cstType);
        addInstruction(insn);
        addMoveResultAsExtraInstruction(arrayType, destReg, newArraySourcePosition);
      } else {
        // Uses instructions new-array and fill-array-data

        List<JExpression> dims = newArray.getDims();
        assert dims.size() >= 1;
        assert isDexNewArrayCompatible(newArray);
        RegisterSpecList sources =
            RegisterSpecList.make(getRegisterSpec(dims.get(0)));

        Rop op = Rops.opNewArray(cstType.getClassType());

        Insn insn =
            new ThrowingCstInsn(op, newArraySourcePosition, sources, getCatchTypes(), cstType);
        addInstruction(insn);
        addMoveResultPseudoAsExtraInstruction(destReg, newArraySourcePosition);

        if (!newArray.getInitializers().isEmpty()) {
          assert newArray.hasConstantInitializer();
          ArrayList<Constant> initValues = new ArrayList<Constant>();
          for (JExpression initializer : newArray.getInitializers()) {
            initValues.add(buildPrimitiveConstant((JValueLiteral) initializer));
          }
          insn = new FillArrayDataInsn(
              Rops.FILL_ARRAY_DATA, newArraySourcePosition, RegisterSpecList.make(destReg),
              initValues, cstType);
          addExtraInstruction(insn);
        }
      }

      return false;
    }

    /**
     * Return true if the given JNewArray is compatible with a translation to dex opcode new-array.
     */
    private boolean isDexNewArrayCompatible(JNewArray newArray) {
      List<JExpression> dims = newArray.getDims();
      if (dims.size() < 1) {
        return false;
      }
      Iterator<JExpression> iter = dims.iterator();
      if (iter.next() instanceof JAbsentArrayDimension) {
        return false;
      }

      while (iter.hasNext()) {
        if (!(iter.next() instanceof JAbsentArrayDimension)) {
          return false;
        }
      }

      return true;
    }

    private void buildArrayRead(@Nonnull RegisterSpec destReg, @Nonnull JArrayRef arrayRef,
        @Nonnull SourcePosition sourcePosition) {
      assert arrayRef.getInstance() instanceof JVariableRef
          || arrayRef.getInstance() instanceof JNullLiteral;
      RegisterSpec instanceReg = getRegisterSpec(arrayRef.getInstance());
      RegisterSpec indexReg = getRegisterSpec(arrayRef.getIndexExpr());
      RegisterSpecList sources = RegisterSpecList.make(instanceReg, indexReg);

      Rop rop = Rops.opAget(getComponentType(instanceReg));
      addInstruction(new ThrowingInsn(rop, sourcePosition, sources, getCatchTypes()));
      addMoveResultPseudoAsExtraInstruction(destReg, sourcePosition);
    }

    private void buildReadField(@Nonnull RegisterSpec destReg, @Nonnull JFieldRef fieldRef,
        @Nonnull SourcePosition sourcePosition) {
      CstFieldRef cstField =
          RopHelper.createFieldRef(fieldRef.getFieldId(), fieldRef.getReceiverType());
      Type ropFieldType = RopHelper.convertTypeToDx(fieldRef.getType());
      if (fieldRef.getFieldId().getKind() == FieldKind.STATIC) {
        Rop rop = Rops.opGetStatic(ropFieldType);
        addInstruction(new ThrowingCstInsn(rop, sourcePosition, RegisterSpecList.EMPTY,
            getCatchTypes(), cstField));
      } else {
        JExpression instance = fieldRef.getInstance();
        assert instance != null;
        assert instance instanceof JVariableRef || instance instanceof JNullLiteral;
        RegisterSpec instanceReg = getRegisterSpec(instance);
        RegisterSpecList sources = RegisterSpecList.make(instanceReg);

        Rop rop = Rops.opGetField(ropFieldType);
        addInstruction(
            new ThrowingCstInsn(rop, sourcePosition, sources, getCatchTypes(), cstField));
      }
      addMoveResultPseudoAsExtraInstruction(destReg, sourcePosition);
    }

    private void buildAssignVariableRef(@Nonnull RegisterSpec destReg, @Nonnull JVariableRef vRef,
        @Nonnull SourcePosition sourcePosition) {
      RegisterSpec valueReg = ropReg.getRegisterSpec(vRef);
      RegisterSpecList sources = RegisterSpecList.make(valueReg);
      addInstruction(
          new PlainInsn(Rops.opMove(valueReg.getTypeBearer()), sourcePosition, destReg, sources));
    }
  }

  RopBuilderVisitor(@Nonnull RopRegisterManager ropReg, @Nonnull BasicBlock currentBasicBlock) {
    this.ropReg = ropReg;
    this.currentBasicBlock = currentBasicBlock;
  }

  @CheckForNull
  List<Insn> getInstructions() {
    return instructions;
  }

  @CheckForNull
  List<Insn> getExtraInstructions() {
    return extraInstructions;
  }

  public void accept(@Nonnull List<JStatement> list) {
    instructions = new LinkedList<Insn>();
    extraInstructions = new LinkedList<Insn>();
    noMoreInstruction = false;

    super.accept(list);
  }

  @Override
  public boolean visit(@Nonnull JExpressionStatement exprStmt) {
    JExpression expr = exprStmt.getExpr();

    if (expr instanceof JMethodCall) {
      buildCall(null, (JMethodCall) expr);
    } else if (expr instanceof JLocalRef) {
      // "lv;" This is a nop, do nothing
    } else if (expr instanceof JBinaryOperation) {
      JBinaryOperation binaryOperation = (JBinaryOperation) expr;

      // ensured by ThreeAddressCodeForm
      assert binaryOperation.getOp() == JBinaryOperator.ASG;
      JExpression lhs = binaryOperation.getLhs();
      assert   (lhs instanceof JVariableRef)
            || (lhs instanceof JFieldRef)
            || (lhs instanceof JArrayRef);

      buildAssign(exprStmt, lhs, binaryOperation.getRhs());
    } else {
      throw new AssertionError(exprStmt.toSource() + " not yet supported.");
    }

    return false;
  }

  @Override
  public boolean visit(@Nonnull JIfStatement ifStmt) {
    JExpression condExpr = ifStmt.getIfExpr();
    SourcePosition ifStmtSrcPos = RopHelper.getSourcePosition(ifStmt);
    Rop ifOp;
    RegisterSpecList sources;
    JBinaryOperator op = null;

    if (condExpr instanceof JBinaryOperation) {
      JBinaryOperation binCondExpr = (JBinaryOperation) condExpr;
      JExpression right = binCondExpr.getRhs();
      RegisterSpec rightReg = getRegisterSpec(right);

      JExpression left = binCondExpr.getLhs();
      JType type = right.getType();
      JType leftType = left.getType();
      assert leftType.isSameType(type)
          || (leftType instanceof JIntegralType32 && type instanceof JIntegralType32)
          || (leftType instanceof JReferenceType && type instanceof JReferenceType);

      op = binCondExpr.getOp();
      RegisterSpec leftReg = getRegisterSpec(left);
      sources = RegisterSpecList.make(leftReg, rightReg);
      if (type instanceof JPrimitiveType) {
        switch (((JPrimitiveType) type).getPrimitiveTypeEnum()) {
          case LONG:
          case FLOAT:
          case DOUBLE: {
            RegisterSpec dest = ropReg.createRegisterSpec(JPrimitiveTypeEnum.BOOLEAN.getType());
            Rop cmpOp = null;
            Type dxType = RopHelper.convertTypeToDx(type);

            if (type == JPrimitiveTypeEnum.LONG.getType()) {
              cmpOp = Rops.opCmpl(dxType);
            } else {
              cmpOp = getCmpOperatorForFloatDouble(op, dxType);
            }

            Insn ifInst = new PlainInsn(cmpOp, ifStmtSrcPos, dest, sources);
            addInstruction(ifInst);
            sources = RegisterSpecList.make(dest);
            break;
          }
          case BOOLEAN:
          case BYTE:
          case CHAR:
          case SHORT:
          case INT:
            // Nothing to do.
            break;
          case VOID:
            throw new AssertionError("Void type not supported.");
        }
      }
    } else if (condExpr instanceof JPrefixNotOperation) {
      RegisterSpec sourceReg = getRegisterSpec(((JPrefixNotOperation) condExpr).getArg());
      sources = RegisterSpecList.make(sourceReg);
      op = JBinaryOperator.EQ;
    } else {
      RegisterSpec sourceReg = getRegisterSpec(condExpr);
      sources = RegisterSpecList.make(sourceReg);
      op = JBinaryOperator.NEQ;
    }

    assert op != null;
    FallThroughMarker ftm = ifStmt.getMarker(FallThroughMarker.class);
    if (ftm != null) {
      switch (ftm.getFallThrough()) {
        case ELSE: {
          ifOp = getOperatorForIf(op, sources);
          break;
        }
        case THEN: {
          ifOp = getReverseOperatorForIf(op, sources);
          break;
        }
        default: {
          throw new AssertionError();
        }
      }
    } else {
      ifOp = getReverseOperatorForIf(op, sources);
    }
    assert ifOp != null;

    Insn ifInst = new PlainInsn(ifOp, ifStmtSrcPos, null, sources);
    addInstruction(ifInst);

    return false;
  }

  /**
   * Get the @link{Rop} corresponding to the inverse of the @link{JBinaryOperator} provided for
   * float or double type.
   * @param op the operator to convert
   * @param type dx type of operator
   * @return the reverse @code{Rop}
   */
  @Nonnull
  public Rop getCmpOperatorForFloatDouble(@Nonnull JBinaryOperator op,
      @Nonnull Type type) {
    assert type == Type.FLOAT || type == Type.DOUBLE;
    switch (op) {
      case LTE:
      case LT:
        return Rops.opCmpg(type);
       case GT:
       case GTE:
       case EQ:
       case NEQ:
         return Rops.opCmpl(type);
      default:
        throw new AssertionError("Operator " + op.toString() + " not yet supported into IfStmt.");
    }
  }

  /**
   * Get the @link{Rop} corresponding of the @link{JBinaryOperator} provided.
   * @param op the operator to convert
   * @param sources the sources that will be used with the @code{Rop}
   * @return the reverse @code{Rop}
   */
  @Nonnull
  public Rop getOperatorForIf(@Nonnull JBinaryOperator op,
      @Nonnull RegisterSpecList sources) {
    switch (op) {
      case LT:
        return Rops.opIfLt(sources);
       case GT:
         return Rops.opIfGt(sources);
       case LTE:
         return Rops.opIfLe(sources);
       case GTE:
         return Rops.opIfGe(sources);
       case EQ:
         return Rops.opIfEq(sources);
       case NEQ:
         return Rops.opIfNe(sources);
      default:
        throw new AssertionError("Operator " + op.toString()
            + " not yet supported into IfStmt.");
    }
  }

  /**
   * Get the @link{Rop} corresponding to the inverse of the @link{JBinaryOperator} provided.
   * @param op the operator to convert
   * @param sources the sources that will be used with the @code{Rop}
   * @return the reverse @code{Rop}
   */
  @Nonnull
  public Rop getReverseOperatorForIf(@Nonnull JBinaryOperator op,
      @Nonnull RegisterSpecList sources) {
    switch (op) {
      case LT:
        return Rops.opIfGe(sources);
       case GT:
         return Rops.opIfLe(sources);
       case LTE:
         return Rops.opIfGt(sources);
       case GTE:
         return Rops.opIfLt(sources);
       case EQ:
         return Rops.opIfNe(sources);
       case NEQ:
         return Rops.opIfEq(sources);
      default:
        throw new AssertionError("Operator " + op.toString()
            + " not yet supported into IfStmt.");
    }
  }

  @Override
  public boolean visit(@Nonnull JReturnStatement retStmt) {
    JExpression returnedExpr = retStmt.getExpr();
    RegisterSpecList sources;

    if (returnedExpr != null) {
      sources = RegisterSpecList.make(getRegisterSpec(returnedExpr));
    } else {
      sources = RegisterSpecList.EMPTY;
    }

    Insn retInst = new PlainInsn(Rops.opReturn(RopHelper.convertTypeToDx(
        returnedExpr == null ? JPrimitiveTypeEnum.VOID.getType() : returnedExpr.getType())),
        RopHelper.getSourcePosition(retStmt), null, sources);
    addInstruction(retInst);

    return false;
  }

  @Override
  public boolean visit(@Nonnull JSwitchStatement jswitch) {
    assert currentBasicBlock instanceof SwitchBasicBlock;

    SourcePosition switchStmtSrcPos = RopHelper.getSourcePosition(jswitch);
    IntList cases = new IntList();
    for (BasicBlock caseBb : ((SwitchBasicBlock) currentBasicBlock).getCasesBlock()) {
          JStatement firstStatement = caseBb.getStatements().get(0);
          assert firstStatement instanceof JCaseStatement;
          JLiteral caseValue = ((JCaseStatement) firstStatement).getExpr();
          if (caseValue instanceof JIntLiteral) {
            cases.add(((JIntLiteral) caseValue).getValue());
          } else if (caseValue instanceof JCharLiteral) {
            cases.add(((JCharLiteral) caseValue).getValue());
          } else if (caseValue instanceof JShortLiteral) {
            cases.add(((JShortLiteral) caseValue).getValue());
          } else if (caseValue instanceof JByteLiteral) {
            cases.add(((JByteLiteral) caseValue).getValue());
          } else {
            throw new AssertionError("Unsupported value");
          }
    }

    RegisterSpecList sources = RegisterSpecList.make(getRegisterSpec(jswitch.getExpr()));

    Insn switchInst = new SwitchInsn(Rops.SWITCH, switchStmtSrcPos, null, sources, cases);
    addInstruction(switchInst);
    return false;
  }

  @Override
  public boolean visit(@Nonnull JThrowStatement throwStmt) {
    Insn throwInsn = new ThrowingInsn(Rops.THROW, RopHelper.getSourcePosition(throwStmt),
        RegisterSpecList.make(getRegisterSpec(throwStmt.getExpr())),
        getCatchTypes());

    addInstruction(throwInsn);

    return false;
  }

  @Override
  public boolean visit(@Nonnull JLock lockStmt) {
    SourcePosition srcPosition = RopHelper.getSourcePosition(lockStmt);
    RegisterSpec lockReg = getRegisterSpec(lockStmt.getLockExpr());

    Insn lockInsn = new ThrowingInsn(Rops.MONITOR_ENTER, srcPosition,
        RegisterSpecList.make(lockReg), getCatchTypes());

    addInstruction(lockInsn);

    return false;
  }

  @Override
  public boolean visit(@Nonnull JUnlock unlockStmt) {
    RegisterSpec unlockReg = getRegisterSpec(unlockStmt.getLockExpr());

    Insn unlockInsn = new ThrowingInsn(Rops.MONITOR_EXIT, RopHelper.getSourcePosition(unlockStmt),
        RegisterSpecList.make(unlockReg), getCatchTypes());

    addInstruction(unlockInsn);

    return false;
  }

  private void buildAlloc(@Nonnull RegisterSpec destReg, @Nonnull JAlloc alloc,
      @Nonnull SourcePosition sourcePosition) {
    CstType type = RopHelper.getCstType(alloc.getInstanceType());
    Rop rop = Rops.NEW_INSTANCE;
    addInstruction(
        new ThrowingCstInsn(rop, sourcePosition, RegisterSpecList.EMPTY, getCatchTypes(), type));
    addMoveResultPseudoAsExtraInstruction(destReg, sourcePosition);
  }

  private void buildAssign(
      @Nonnull JStatement declaration,
      @Nonnull JExpression dest,
      @Nonnull JExpression value)
          throws AssertionError {
    if (value instanceof JExceptionRuntimeValue) {
      assert dest instanceof JVariableRef;
      assert declaration.getParent() instanceof JCatchBlock
          && ((JCatchBlock) declaration.getParent()).getStatements().get(0) == declaration;
      RegisterSpec exceptionReg = ropReg.getRegisterSpec((JVariableRef) dest);
      addInstruction(new PlainInsn(
          Rops.opMoveException(exceptionReg.getTypeBearer()), RopHelper.getSourcePosition(dest),
          exceptionReg, RegisterSpecList.EMPTY));
    } else if (dest instanceof JFieldRef) {
      buildWriteField((JFieldRef) dest, value, RopHelper.getSourcePosition(declaration));
    } else if (dest instanceof JArrayRef) {
      buildArrayWrite((JArrayRef) dest, value, RopHelper.getSourcePosition(declaration));
    } else {
      assert dest instanceof JVariableRef;
      JVariableRef destRef = (JVariableRef) dest;

      JVisitor rhsHandler = new AssignBuilderVisitor(declaration, destRef);
      rhsHandler.accept(value);
    }
  }

  private void buildArrayWrite(JArrayRef arrayRef, JExpression value,
      SourcePosition sourcePosition) {
    assert arrayRef.getInstance() instanceof JVariableRef
    || arrayRef.getInstance() instanceof JNullLiteral;
    RegisterSpec valueReg = getRegisterSpec(value);
    RegisterSpec instanceReg = getRegisterSpec(arrayRef.getInstance());
    RegisterSpec indexReg = getRegisterSpec(arrayRef.getIndexExpr());
    RegisterSpecList sources = RegisterSpecList.make(valueReg, instanceReg, indexReg);

    Rop rop = Rops.opAput(getComponentType(instanceReg));
    addInstruction(new ThrowingInsn(rop, sourcePosition, sources, getCatchTypes()));
  }

  private void buildInstanceOf(RegisterSpec destReg, JInstanceOf instanceOf) {
    SourcePosition srcPos = RopHelper.getSourcePosition(instanceOf);
    RegisterSpec regExpr = getRegisterSpec(instanceOf.getExpr());
    CstType type = RopHelper.getCstType(instanceOf.getTestType());
    addInstruction(new ThrowingCstInsn(Rops.INSTANCE_OF, srcPos, RegisterSpecList.make(regExpr),
        getCatchTypes(), type));
    addMoveResultPseudoAsExtraInstruction(destReg, srcPos);
  }

  @Nonnull
  private static Type getComponentType(@Nonnull TypeBearer arrayTypeBearer) {
    Type arrayType = arrayTypeBearer.getType();

    if (arrayType.isArray()) {
      return arrayType.getComponentType();
    }

    assert arrayType.equals(Type.KNOWN_NULL);
    return arrayType.getType();
  }

  private void buildArrayLength(RegisterSpec destReg, JArrayLength value) {
    RegisterSpec reg = getRegisterSpec(value.getInstance());
    SourcePosition srcPos = RopHelper.getSourcePosition(value);
    addInstruction(new ThrowingInsn(Rops.ARRAY_LENGTH, srcPos, RegisterSpecList.make(reg),
        getCatchTypes()));
    addMoveResultPseudoAsExtraInstruction(destReg, srcPos);
  }

  private void buildWriteField(@Nonnull JFieldRef fieldRef, @Nonnull JExpression value,
      @Nonnull SourcePosition sourcePosition) {

    RegisterSpec valueReg = getRegisterSpec(value);

    CstFieldRef cstField = RopHelper.createFieldRef(fieldRef.getFieldId(),
        fieldRef.getReceiverType());

    if (fieldRef.getFieldId().getKind() == FieldKind.STATIC) {
      Rop rop = Rops.opPutStatic(RopHelper.convertTypeToDx(fieldRef.getType()));
      addInstruction(new ThrowingCstInsn(rop, sourcePosition, RegisterSpecList.make(valueReg),
          getCatchTypes(), cstField));
    } else {
      JExpression instance = fieldRef.getInstance();
      assert instance != null;
      assert instance instanceof JVariableRef || instance instanceof JNullLiteral;
      RegisterSpec instanceReg = getRegisterSpec(instance);
      RegisterSpecList sources = RegisterSpecList.make(valueReg, instanceReg);

      Rop rop = Rops.opPutField(RopHelper.convertTypeToDx(fieldRef.getType()));
      addInstruction(new ThrowingCstInsn(rop, sourcePosition, sources, getCatchTypes(), cstField));
    }
  }


  private void buildCast(@Nonnull RegisterSpec destReg, @Nonnull JDynamicCastOperation cast) {
    JExpression from = cast.getExpr();
    SourcePosition sourcePosition = RopHelper.getSourcePosition(cast);
    RegisterSpec fromReg = getRegisterSpec(from);

    JType castTo = cast.getType();
    JType castedFrom = from.getType();

    if (castTo instanceof JPrimitiveType) {

      assert castedFrom instanceof JPrimitiveType;

      if (castTo == castedFrom) {
        RegisterSpecList sources = RegisterSpecList.make(fromReg);
        addInstruction(new PlainInsn(
            Rops.opMove(fromReg.getTypeBearer()), sourcePosition,
            destReg, sources));
        return;
      }

      /* Rop has 2 groups of cast instructions:
       * - Casts form int to byte, char and short.
       * - Casts between int, long, float and double.
       * Casts from larger values than int to smaller values must be done with 2 instructions, one
       * from each group. These two instructions are created by RopCastLegalier.
       */

      if (((castTo == JPrimitiveTypeEnum.BYTE.getType())
            || (castTo == JPrimitiveTypeEnum.SHORT.getType())
            || (castTo == JPrimitiveTypeEnum.CHAR.getType())
            || (castTo == JPrimitiveTypeEnum.INT.getType())
            || (castTo == JPrimitiveTypeEnum.BOOLEAN.getType())
            )
            &&
            ((castedFrom == JPrimitiveTypeEnum.INT.getType())
            || (castedFrom == JPrimitiveTypeEnum.BYTE.getType())
            || (castedFrom == JPrimitiveTypeEnum.CHAR.getType())
            || (castedFrom == JPrimitiveTypeEnum.SHORT.getType())
            || (castedFrom == JPrimitiveTypeEnum.BOOLEAN.getType())
            )) {
        addTruncateIntOrMoveInstruction(sourcePosition,
            ((JPrimitiveType) castTo).getPrimitiveTypeEnum(), fromReg, destReg);
      } else {

        Insn inst =
            new PlainInsn(Rops.opConv(destReg, fromReg), sourcePosition, destReg,
                RegisterSpecList.make(fromReg));
        addInstruction(inst);

      }
    } else {
      RegisterSpecList sources = RegisterSpecList.make(fromReg);

      Insn insn =
          new ThrowingCstInsn(Rops.CHECK_CAST, sourcePosition, sources, getCatchTypes(),
              RopHelper.getCstType(castTo));
      addInstruction(insn);

      addMoveResultPseudoAsExtraInstruction(destReg, sourcePosition);
    }
  }

  private void addTruncateIntOrMoveInstruction(@Nonnull SourcePosition sourcePosition,
      @Nonnull JPrimitiveTypeEnum castTo, @Nonnull RegisterSpec fromReg,
      @CheckForNull RegisterSpec destReg) throws AssertionError {
    Rop rop;
    switch (castTo) {
      case BYTE:
        rop = Rops.TO_BYTE;
        break;
      case CHAR:
        rop = Rops.TO_CHAR;
        break;
      case SHORT:
        rop = Rops.TO_SHORT;
        break;
      case BOOLEAN:
      case INT:
        rop = Rops.MOVE_INT;
        break;
      default:
        throw new AssertionError(castTo + " not supported");
    }

    RegisterSpecList sources = RegisterSpecList.make(fromReg);
    Insn inst = new PlainInsn(
        rop, sourcePosition, destReg, sources);
    addInstruction(inst);
  }

  @Nonnull
  private Constant buildPrimitiveConstant(@Nonnull JValueLiteral literal) {
    Constant cst = null;

    assert literal.getType() instanceof JPrimitiveType;

    JPrimitiveTypeEnum primitiveType = ((JPrimitiveType) literal.getType()).getPrimitiveTypeEnum();

    switch (primitiveType) {
      case BOOLEAN:
        cst = CstInteger.make(((JBooleanLiteral) literal).getValue() ? 1 : 0);
        break;
      case BYTE:
        cst = CstInteger.make(((JByteLiteral) literal).getValue());
        break;
      case CHAR:
        cst = CstInteger.make(((JCharLiteral) literal).getValue());
        break;
      case DOUBLE:
        cst = CstDouble.make(Double.doubleToLongBits(((JDoubleLiteral) literal).getValue()));
        break;
      case FLOAT:
        cst = CstFloat.make(Float.floatToIntBits(((JFloatLiteral) literal).getValue()));
        break;
      case INT:
        cst = CstInteger.make(((JIntLiteral) literal).getValue());
        break;
      case LONG:
        cst = CstLong.make(((JLongLiteral) literal).getValue());
        break;
      case SHORT:
        cst = CstInteger.make(((JShortLiteral) literal).getValue());
        break;
      case VOID:
        throw new AssertionError(literal.toSource() + " not supported.");
    }

    assert cst != null;
    return cst;
  }

  @Nonnull
  private Constant getConstant(@Nonnull JValueLiteral literal) {
    Constant cst = null;

    JType type = literal.getType();
    if (type instanceof JPrimitiveType) {
      cst = buildPrimitiveConstant(literal);
    } else  if (literal instanceof JAbstractStringLiteral){
      cst = RopHelper.createString((JAbstractStringLiteral) literal);
    } else if (literal instanceof JNullLiteral) {
      cst = CstKnownNull.THE_ONE;
    } else {
      throw new AssertionError(literal.toSource() + " not supported.");
    }

    return cst;
  }

  private void buildConstant(@Nonnull RegisterSpec destReg, @Nonnull JValueLiteral literal) {
    JType type = literal.getType();
    Rop constOp = Rops.opConst(RopHelper.convertTypeToDx(type));
    Insn constInst;
    SourcePosition sourcePosition = RopHelper.getSourcePosition(literal);
    if (type instanceof JPrimitiveType) {
      constInst = new PlainCstInsn(
          constOp, sourcePosition, destReg, RegisterSpecList.EMPTY,
          getConstant(literal));
      addInstruction(constInst);
    } else  if (literal instanceof JAbstractStringLiteral){
      constInst = new ThrowingCstInsn(constOp, sourcePosition,
          RegisterSpecList.EMPTY, getCatchTypes(), getConstant(literal));
      addInstruction(constInst);
      addMoveResultPseudoAsExtraInstruction(destReg, sourcePosition);
    } else if (literal instanceof JNullLiteral) {
      constInst = new PlainCstInsn(
          constOp, sourcePosition, destReg,
          RegisterSpecList.EMPTY, getConstant(literal));
      addInstruction(constInst);
    } else {
      throw new AssertionError(literal.toSource() + " not supported.");
    }
  }

  private void buildUnaryOperation(@Nonnull RegisterSpec destReg,
      @Nonnull JUnaryOperation unary) {
    SourcePosition unarySrcPos = RopHelper.getSourcePosition(unary);

    RegisterSpec srcRegisterSpec = getRegisterSpec(unary.getArg());
    RegisterSpecList sources = RegisterSpecList.make(srcRegisterSpec);

    Rop opcode = null;

    switch(unary.getOp()) {
      case NEG: {
        assert unary.getType() == JPrimitiveTypeEnum.BYTE.getType()
            || unary.getType() == JPrimitiveTypeEnum.CHAR.getType()
            || unary.getType() == JPrimitiveTypeEnum.SHORT.getType()
            || unary.getType() == JPrimitiveTypeEnum.INT.getType()
            || unary.getType() == JPrimitiveTypeEnum.LONG.getType()
            || unary.getType() == JPrimitiveTypeEnum.FLOAT.getType()
            || unary.getType() == JPrimitiveTypeEnum.DOUBLE.getType();
        opcode = Rops.opNeg(srcRegisterSpec);
        break;
      }
      case BIT_NOT: {
        assert unary.getType() == JPrimitiveTypeEnum.BYTE.getType()
            || unary.getType() == JPrimitiveTypeEnum.CHAR.getType()
            || unary.getType() == JPrimitiveTypeEnum.SHORT.getType()
            || unary.getType() == JPrimitiveTypeEnum.INT.getType()
            || unary.getType() == JPrimitiveTypeEnum.LONG.getType();
        opcode = Rops.opNot(srcRegisterSpec);
        break;
      }
      default: {
        throw new AssertionError("Unary operation not supported.");
      }
    }

    addInstruction(new PlainInsn(opcode, unarySrcPos, destReg, sources));
  }

  private void buildBinaryOperation(
      @Nonnull RegisterSpec destReg, @Nonnull JBinaryOperation binary) {

    RegisterSpecList sources;
    SourcePosition declarationSrcPos = RopHelper.getSourcePosition(binary);
    Constant cst = null;
    JBinaryOperator binOp = binary.getOp();
    JExpression rhs = binary.getRhs();
    JExpression lhs = binary.getLhs();

    if (lhs instanceof JVariableRef && binary.getType() instanceof JIntegralType32
        && rhs instanceof JIntegralConstant32 && ((JIntegralType32) JPrimitiveTypeEnum.SHORT
            .getType()).isValidValue(((JIntegralConstant32) rhs).getIntValue())) {
      assert rhs instanceof JValueLiteral;

      // Sub with constant does not exist, check if it can be replace by an add
      if (binOp == JBinaryOperator.SUB) {
        int newCst = -((JIntegralConstant32) rhs).getIntValue();
        if (((JIntegralType32) JPrimitiveTypeEnum.SHORT.getType()).isValidValue(newCst)) {
          binOp = JBinaryOperator.ADD;
          sources = RegisterSpecList.make(ropReg.getRegisterSpec((JVariableRef) lhs));
          cst = CstInteger.make(newCst);
        } else {
          sources = RegisterSpecList.make(getRegisterSpec(lhs), getRegisterSpec(rhs));
        }
      } else {
        sources = RegisterSpecList.make(ropReg.getRegisterSpec((JVariableRef) lhs));
        cst = getConstant((JValueLiteral) rhs);
      }
    } else {
      if (rhs instanceof JVariableRef) {
        // Check if rsub can be generated
        if (binOp == JBinaryOperator.SUB
            && lhs instanceof JIntegralConstant32 && ((JIntegralType32) JPrimitiveTypeEnum.SHORT
                .getType()).isValidValue(((JIntegralConstant32) lhs).getIntValue())) {
          sources = RegisterSpecList.make(ropReg.getRegisterSpec((JVariableRef) rhs));
          cst = getConstant((JValueLiteral) lhs);
        } else {
          sources = RegisterSpecList.make(getRegisterSpec(lhs),
            ropReg.getRegisterSpec((JVariableRef) rhs));
        }
      } else {
        assert rhs instanceof JValueLiteral;
        sources = RegisterSpecList.make(
            getRegisterSpec(lhs), getRegisterSpec(rhs));
      }
    }

    Rop opcode;

    switch (binOp) {
      case ADD:
        opcode = Rops.opAdd(sources);
        break;
      case SUB:
        opcode = Rops.opSub(sources);
        break;
      case ASG: // all assign not removed in ThreeAddressCodeForm are to be handled by buildAssign
      case ASG_ADD: // assigns with operation are removed in ThreeAddressCodeForm
      case ASG_BIT_AND:
      case ASG_BIT_OR:
      case ASG_BIT_XOR:
      case ASG_CONCAT:
      case ASG_DIV:
      case ASG_MOD:
      case ASG_MUL:
      case ASG_SHL:
      case ASG_SHR:
      case ASG_SHRU:
      case ASG_SUB:
      case EQ: // TODO(yroussel) add constraint on Ropper to ensure no boolean BinaryOperation
      case GT:
      case GTE:
      case LT:
      case LTE:
      case NEQ:
      case OR:
      case AND:
        throw new AssertionError();
      case BIT_AND:
        opcode = Rops.opAnd(sources);
        break;
      case BIT_OR:
        opcode = Rops.opOr(sources);
        break;
      case BIT_XOR:
        opcode = Rops.opXor(sources);
        break;
      case DIV:
        opcode = Rops.opDiv(sources);
        break;
      case MOD:
        opcode = Rops.opRem(sources);
        break;
      case MUL:
        opcode = Rops.opMul(sources);
        break;
      case SHL:
        opcode = Rops.opShl(sources);
        break;
      case SHR:
        opcode = Rops.opShr(sources);
        break;
      case SHRU:
        opcode = Rops.opUshr(sources);
        break;
      default:
        throw new AssertionError();
    }
    if (opcode.canThrow()) {
      if (cst == null) {
        addInstruction(new ThrowingInsn(opcode, declarationSrcPos, sources, getCatchTypes()));
      } else {
        addInstruction(
            new ThrowingCstInsn(opcode, declarationSrcPos, sources, getCatchTypes(), cst));
      }
      addMoveResultPseudoAsExtraInstruction(destReg, declarationSrcPos);
    } else {
      if (cst == null) {
        addInstruction(new PlainInsn(opcode, declarationSrcPos, destReg, sources));
      } else {
        addInstruction(new PlainCstInsn(opcode, declarationSrcPos, destReg, sources, cst));
      }
    }
  }

  private void buildCall(@CheckForNull RegisterSpec result, @Nonnull JMethodCall methodCall) {
    String signatureWithoutName = RopHelper.getMethodSignatureWithoutName(methodCall);
    SourcePosition methodCallSrcPos = RopHelper.getSourcePosition(methodCall);

    Prototype prototype = Prototype.intern(signatureWithoutName);

    RegisterSpecList sources;
    int paramIndex = 0;

    Rop callOp;
    MethodKind methodKind = methodCall.getMethodId().getKind();
    if (methodKind == MethodKind.STATIC) {
      // Reserve space for the method arguments
      sources = new RegisterSpecList(methodCall.getArgs().size());
    } else {
      // Reserve space for the instance and the method arguments
      sources = new RegisterSpecList(1 + methodCall.getArgs().size());
    }

    switch(methodKind) {
      case STATIC:
        callOp = Rops.opInvokeStatic(prototype);
        break;
      case INSTANCE_NON_VIRTUAL: {
        callOp = Rops.opInvokeDirect(prototype);
        // Add the instance as first parameter
        JExpression instance = methodCall.getInstance();
        assert instance != null;
        sources.set(paramIndex++, getRegisterSpec(instance));
        break;
      }
      case INSTANCE_VIRTUAL: {
        JExpression instance = methodCall.getInstance();
        assert instance != null;
        RegisterSpec instanceReg = getRegisterSpec(instance);
        if (methodCall.getDispatchKind() == DispatchKind.DIRECT) {
          callOp = Rops.opInvokeSuper(prototype);
        } else {
          if (methodCall.getReceiverType() instanceof JInterface) {
            callOp = Rops.opInvokeInterface(prototype);
          } else {
            callOp = Rops.opInvokeVirtual(prototype);
          }
        }
        sources.set(paramIndex++, instanceReg);
        break;
      }
      default:
        throw new AssertionError(methodCall.toSource() + " not yet supported.");
    }

    assert prototype.getParameterTypes().size() == methodCall.getArgs().size();
    for (JExpression exprArg : methodCall.getArgs()) {
      sources.set(paramIndex++, getRegisterSpec(exprArg));
    }

    CstMethodRef methodRef = RopHelper.createMethodRef(methodCall);
    Insn callInst =
        new ThrowingCstInsn(callOp, methodCallSrcPos, sources, getCatchTypes(), methodRef);
    addInstruction(callInst);

    if (result != null) {
      addMoveResultAsExtraInstruction(prototype.getReturnType(), result, methodCallSrcPos);
    }
  }

  @Nonnull
  private RegisterSpec getRegisterSpec(@Nonnull JExpression expr) {
    RegisterSpec regSpec;

    if (expr instanceof JVariableRef) {
      regSpec = ropReg.getRegisterSpec((JVariableRef) expr);
    } else {
      assert expr instanceof JValueLiteral;
      regSpec =
          ropReg.getOrCreateTmpRegister(RopHelper.convertTypeToDx(expr.getType()));
      buildConstant(regSpec, (JValueLiteral) expr);
    }

    return regSpec;
  }
  private void addMoveResultAsExtraInstruction(@Nonnull TypeBearer type,
      @Nonnull RegisterSpec destReg, @Nonnull SourcePosition sourcePosition) {
    Rop moveResultOp = Rops.opMoveResult(type);
    Insn moveResultInst =
        new PlainInsn(moveResultOp, sourcePosition, destReg, RegisterSpecList.EMPTY);
    addExtraInstruction(moveResultInst);
  }

  private void addMoveResultPseudoAsExtraInstruction(
      @Nonnull RegisterSpec destReg, @Nonnull SourcePosition sourcePosition) {
    PlainInsn moveResult = new PlainInsn(
        Rops.opMoveResultPseudo(destReg.getTypeBearer()),
        sourcePosition, destReg, RegisterSpecList.EMPTY);
    addExtraInstruction(moveResult);
  }

  private void addExtraInstruction(@Nonnull Insn insn) {
    assert extraInstructions != null;
    extraInstructions.add(insn);
    noMoreInstruction = true;
  }

  private boolean addInstruction(@Nonnull Insn insn) {
    assert instructions != null;
    assert !noMoreInstruction;
    return instructions.add(insn);
  }

  /**
   * Get the catch types list containing the types of every catch block accessible from the given
   * block.
   */
  private TypeList getCatchTypes() {
    assert currentBasicBlock instanceof PeiBasicBlock;
    PeiBasicBlock peiBlock = (PeiBasicBlock) currentBasicBlock;

    List<JType> catchTypes = new ArrayList<JType>();

    for (CatchBasicBlock bb : peiBlock.getExceptionBlocks()) {
      for (JClass catchType : bb.getCatchTypes()) {
        catchTypes.add(catchType);
      }
    }

    if (catchTypes.isEmpty()) {
      return (StdTypeList.EMPTY);
    } else {
      return (RopHelper.createTypeList(catchTypes));
    }
  }

  @Override
  public void endVisit(@Nonnull JStatement x) {
    ropReg.resetFreeTmpRegister();
  }
}
