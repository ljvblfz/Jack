/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.android.jack.ir.impl;

import com.android.jack.Jack;
import com.android.jack.experimental.incremental.JackIncremental;
import com.android.jack.frontend.ParentSetter;
import com.android.jack.ir.InternalCompilerException;
import com.android.jack.ir.ast.Annotable;
import com.android.jack.ir.ast.JAbsentArrayDimension;
import com.android.jack.ir.ast.JAbstractMethodBody;
import com.android.jack.ir.ast.JAbstractStringLiteral;
import com.android.jack.ir.ast.JAnnotationLiteral;
import com.android.jack.ir.ast.JAnnotationMethod;
import com.android.jack.ir.ast.JArrayLength;
import com.android.jack.ir.ast.JArrayLiteral;
import com.android.jack.ir.ast.JArrayRef;
import com.android.jack.ir.ast.JArrayType;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JAssertStatement;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JBinaryOperator;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JBooleanLiteral;
import com.android.jack.ir.ast.JBreakStatement;
import com.android.jack.ir.ast.JByteLiteral;
import com.android.jack.ir.ast.JCaseStatement;
import com.android.jack.ir.ast.JCatchBlock;
import com.android.jack.ir.ast.JCharLiteral;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassLiteral;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JConcatOperation;
import com.android.jack.ir.ast.JConditionalExpression;
import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JContinueStatement;
import com.android.jack.ir.ast.JDefinedAnnotation;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedEnum;
import com.android.jack.ir.ast.JDoStatement;
import com.android.jack.ir.ast.JDoubleLiteral;
import com.android.jack.ir.ast.JDynamicCastOperation;
import com.android.jack.ir.ast.JEnumField;
import com.android.jack.ir.ast.JEnumLiteral;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldInitializer;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JFloatLiteral;
import com.android.jack.ir.ast.JForStatement;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JInstanceOf;
import com.android.jack.ir.ast.JIntLiteral;
import com.android.jack.ir.ast.JIntegralConstant32;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JLabel;
import com.android.jack.ir.ast.JLabeledStatement;
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JLongLiteral;
import com.android.jack.ir.ast.JLtOperation;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JMultiExpression;
import com.android.jack.ir.ast.JNameValuePair;
import com.android.jack.ir.ast.JNativeMethodBody;
import com.android.jack.ir.ast.JNewArray;
import com.android.jack.ir.ast.JNewInstance;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.ir.ast.JNullType;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JParameterRef;
import com.android.jack.ir.ast.JPostfixOperation;
import com.android.jack.ir.ast.JPrefixIncOperation;
import com.android.jack.ir.ast.JPrefixNotOperation;
import com.android.jack.ir.ast.JPrefixOperation;
import com.android.jack.ir.ast.JPrimitiveType;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JReferenceType;
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JShortLiteral;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JStringLiteral;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JSynchronizedBlock;
import com.android.jack.ir.ast.JThis;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.ir.ast.JThrowStatement;
import com.android.jack.ir.ast.JTryStatement;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JUnaryOperator;
import com.android.jack.ir.ast.JValueLiteral;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JWhileStatement;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.ast.marker.GenericSignature;
import com.android.jack.ir.ast.marker.ThisRefTypeInfo;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.ir.sourceinfo.SourceInfoFactory;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.util.NamingTools;
import com.android.sched.util.config.ThreadConfig;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.AND_AND_Expression;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.AnnotationMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ArrayReference;
import org.eclipse.jdt.internal.compiler.ast.AssertStatement;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.BinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.BreakStatement;
import org.eclipse.jdt.internal.compiler.ast.CaseStatement;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.CharLiteral;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.Clinit;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompoundAssignment;
import org.eclipse.jdt.internal.compiler.ast.ConditionalExpression;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ContinueStatement;
import org.eclipse.jdt.internal.compiler.ast.DoStatement;
import org.eclipse.jdt.internal.compiler.ast.DoubleLiteral;
import org.eclipse.jdt.internal.compiler.ast.EmptyStatement;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.ExtendedStringLiteral;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.FloatLiteral;
import org.eclipse.jdt.internal.compiler.ast.ForStatement;
import org.eclipse.jdt.internal.compiler.ast.ForeachStatement;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.InstanceOfExpression;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.ast.LabeledStatement;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LongLiteral;
import org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.OR_OR_Expression;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.PostfixExpression;
import org.eclipse.jdt.internal.compiler.ast.PrefixExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedSuperReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedThisReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.StringLiteralConcatenation;
import org.eclipse.jdt.internal.compiler.ast.SuperReference;
import org.eclipse.jdt.internal.compiler.ast.SwitchStatement;
import org.eclipse.jdt.internal.compiler.ast.SynchronizedStatement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.ThrowStatement;
import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
import org.eclipse.jdt.internal.compiler.ast.TryStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.UnaryExpression;
import org.eclipse.jdt.internal.compiler.ast.UnionTypeReference;
import org.eclipse.jdt.internal.compiler.ast.WhileStatement;
import org.eclipse.jdt.internal.compiler.impl.BooleanConstant;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.NestedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticArgumentBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.jdt.internal.compiler.util.Util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Stack;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Constructs a Jack IR from a single isolated compilation unit. The AST is
 * not associated with any {@link com.android.jack.ir.ast.JSession} and will
 * contain unresolved references.
 */
public class JackIrBuilder {

  /**
   * Visit the JDT AST and produce our own AST. By the end of this pass, the
   * produced AST should contain every piece of information we'll ever need
   * about the code. The JDT nodes should never again be referenced after this.
   *
   * NOTE ON JDT FORCED OPTIMIZATIONS - If JDT statically determines that a
   * section of code in unreachable, it won't fully resolve that section of
   * code. This invalid-state code causes us major problems. As a result, we
   * have to optimize out those dead blocks early and never try to translate
   * them to our AST.
   */
  class AstVisitor extends ASTVisitor {

    private final Stack<ClassInfo> classStack = new Stack<ClassInfo>();

    private ClassInfo curClass = null;

    private MethodInfo curMethod = null;

    private final Stack<MethodInfo> methodStack = new Stack<MethodInfo>();

    private final ArrayList<JNode> nodeStack = new ArrayList<JNode>();

    @Nonnegative
    private long newInstanceQualifierSuffix = 0;

    @Nonnegative
    private long superInstanceQualifierSuffix = 0;

    @Nonnull
    private final Stack<List<JCaseStatement>> switchCases = new Stack<List<JCaseStatement>>();

    @Override
    public void endVisit(AllocationExpression x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        List<JExpression> arguments = popCallArgs(info, x.arguments, x.binding);
        pushNewExpression(info, x, null, arguments, scope);
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(AND_AND_Expression x, BlockScope scope) {
      pushBinaryOp(x, JBinaryOperator.AND);
    }

    @Override
    public void endVisit(AnnotationMethodDeclaration x, ClassScope classScope) {
      popMethodInfo();
    }

    @Override
    public void endVisit(ArrayAllocationExpression x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        JArrayType type = (JArrayType) getTypeMap().get(x.resolvedType);

        if (x.initializer != null) {
          // handled by ArrayInitializer.
        } else {
          // Annoyingly, JDT only visits non-null dims, so we can't popList().
          List<JExpression> dims = new ArrayList<JExpression>();
          for (int i = x.dimensions.length - 1; i >= 0; --i) {
            JExpression dimension = pop(x.dimensions[i]);
            // can be null if index expression was empty
            if (dimension == null) {
              dimension = new JAbsentArrayDimension(SourceInfo.UNKNOWN);
            }
            dims.add(dimension);
          }
          // Undo the stack reversal.
          Collections.reverse(dims);
          push(JNewArray.createWithDims(info, type, dims));
        }
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(ArrayInitializer x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        JArrayType type = (JArrayType) getTypeMap().get(x.resolvedType);

        Expression[] expressions = x.expressions;
        List<JExpression> values;

        if (expressions != null) {
          values = new ArrayList<JExpression>(expressions.length);
          List<? extends JNode> result = popList(expressions.length);

          for (int i = 0; i < expressions.length; ++i) {
            assert result.get(i) instanceof JExpression;
            JExpression expr = (JExpression) result.get(i);
            expr = simplify(expr, expressions[i]);

            if (type.getElementType() instanceof JPrimitiveType && expr instanceof JValueLiteral
                && !expr.getType().equals(type.getElementType())) {
              // We have a constant with a different type than array type, change it to the right
              // type
              values.add(changeTypeOfLiteralValue(
                  ((JPrimitiveType) type.getElementType()).getPrimitiveTypeEnum(),
                  (JValueLiteral) expr));
            } else {
              values.add(expr);
            }
          }
        } else {
          values = Collections.emptyList();
        }

        push(JNewArray.createWithInits(info, type, values));
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @SuppressWarnings("incomplete-switch")
    @Nonnull
    private JValueLiteral changeTypeOfLiteralValue(@Nonnull JPrimitiveTypeEnum expectedType,
        @Nonnull JValueLiteral expr) throws AssertionError {
      SourceInfo sourceInfo = expr.getSourceInfo();

      switch (expectedType) {
        case BYTE: {
          return (new JByteLiteral(sourceInfo, (byte) ((JIntegralConstant32) expr).getIntValue()));
        }
        case CHAR: {
          return (new JCharLiteral(sourceInfo, (char) ((JIntegralConstant32) expr).getIntValue()));
        }
        case SHORT: {
          return (new JShortLiteral(sourceInfo,
              (short) ((JIntegralConstant32) expr).getIntValue()));
        }
        case LONG: {
          return (new JLongLiteral(sourceInfo, ((JIntegralConstant32) expr).getIntValue()));
        }
        case FLOAT: {
          if (expr instanceof JIntLiteral) {
            return (new JFloatLiteral(sourceInfo, ((JIntLiteral) expr).getValue()));
          } else if (expr instanceof JLongLiteral) {
            return (new JFloatLiteral(sourceInfo, ((JLongLiteral) expr).getValue()));
          }
          break;
        }
        case DOUBLE: {
          if (expr instanceof JIntLiteral) {
            return (new JDoubleLiteral(sourceInfo, ((JIntLiteral) expr).getValue()));
          } else if (expr instanceof JFloatLiteral) {
            return (new JDoubleLiteral(sourceInfo, ((JFloatLiteral) expr).getValue()));
          } else if (expr instanceof JLongLiteral) {
            return (new JDoubleLiteral(sourceInfo, ((JLongLiteral) expr).getValue()));
          }
          break;
        }
        case INT:
        case BOOLEAN: {
          return (expr);
        }
      }

      throw new AssertionError();
    }

    @Override
    public void endVisit(ArrayReference x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        JExpression position = pop(x.position);
        JExpression receiver = pop(x.receiver);
        push(new JArrayRef(info, receiver, position));
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(AssertStatement x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        JExpression exceptionArgument = pop(x.exceptionArgument);
        JExpression assertExpression = pop(x.assertExpression);
        push(new JAssertStatement(info, assertExpression, exceptionArgument));
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(Assignment x, BlockScope scope) {
      pushBinaryOp(x, JBinaryOperator.ASG);
    }

    @Override
    public void endVisit(BinaryExpression x, BlockScope scope) {
      JBinaryOperator op;
      int binOp = (x.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT;
      switch (binOp) {
        case OperatorIds.LEFT_SHIFT:
          op = JBinaryOperator.SHL;
          break;
        case OperatorIds.RIGHT_SHIFT:
          op = JBinaryOperator.SHR;
          break;
        case OperatorIds.UNSIGNED_RIGHT_SHIFT:
          op = JBinaryOperator.SHRU;
          break;
        case OperatorIds.PLUS:
          if (javaLangString.equals(getTypeMap().get(x.resolvedType))) {
            op = JBinaryOperator.CONCAT;
          } else {
            op = JBinaryOperator.ADD;
          }
          break;
        case OperatorIds.MINUS:
          op = JBinaryOperator.SUB;
          break;
        case OperatorIds.REMAINDER:
          op = JBinaryOperator.MOD;
          break;
        case OperatorIds.XOR:
          op = JBinaryOperator.BIT_XOR;
          break;
        case OperatorIds.AND:
          op = JBinaryOperator.BIT_AND;
          break;
        case OperatorIds.MULTIPLY:
          op = JBinaryOperator.MUL;
          break;
        case OperatorIds.OR:
          op = JBinaryOperator.BIT_OR;
          break;
        case OperatorIds.DIVIDE:
          op = JBinaryOperator.DIV;
          break;
        case OperatorIds.LESS_EQUAL:
          op = JBinaryOperator.LTE;
          break;
        case OperatorIds.GREATER_EQUAL:
          op = JBinaryOperator.GTE;
          break;
        case OperatorIds.GREATER:
          op = JBinaryOperator.GT;
          break;
        case OperatorIds.LESS:
          op = JBinaryOperator.LT;
          break;
        default:
          throw new AssertionError("Unexpected operator for BinaryExpression");
      }
      pushBinaryOp(x, op);
    }

    @Override
    public void endVisit(Block x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        JBlock block = popBlock(info, x.statements);
        push(block);
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(BreakStatement x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        push(new JBreakStatement(info, getOrCreateLabel(info, x.label)));
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(CaseStatement x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        JExpression constantExpression = pop(x.constantExpression);
        JLiteral caseLiteral;
        if (constantExpression == null) {
          caseLiteral = null;
        } else if (constantExpression instanceof JLiteral) {
          caseLiteral = (JLiteral) constantExpression;
        } else {
          // Adapted from CaseStatement.resolveCase().
          assert x.constantExpression.resolvedType.isEnum();
          NameReference reference = (NameReference) x.constantExpression;
          FieldBinding field = reference.fieldBinding();
          JField enumfield = getTypeMap().get(field);
          assert enumfield instanceof JEnumField;
          caseLiteral = new JEnumLiteral(makeSourceInfo(reference), enumfield.getId());
        }
        JCaseStatement jcase = new JCaseStatement(info, caseLiteral);
        push(jcase);
        switchCases.peek().add(jcase);
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(CastExpression x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        JType type = getTypeMap().get(x.resolvedType);
        JExpression expression = pop(x.expression);
        push(new JDynamicCastOperation(info, type, expression));
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(CharLiteral x, BlockScope scope) {
      try {
        push(new JCharLiteral(makeSourceInfo(x), x.constant.charValue()));
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(ClassLiteralAccess x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        JType type = getTypeMap().get(x.targetType);
        push(new JClassLiteral(info, type, javaLangClass));
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(CompoundAssignment x, BlockScope scope) {
      JBinaryOperator op;
      switch (x.operator) {
        case OperatorIds.PLUS:
          if (javaLangString.equals(getTypeMap().get(x.resolvedType))) {
            op = JBinaryOperator.ASG_CONCAT;
          } else {
            op = JBinaryOperator.ASG_ADD;
          }
          break;
        case OperatorIds.MINUS:
          op = JBinaryOperator.ASG_SUB;
          break;
        case OperatorIds.MULTIPLY:
          op = JBinaryOperator.ASG_MUL;
          break;
        case OperatorIds.DIVIDE:
          op = JBinaryOperator.ASG_DIV;
          break;
        case OperatorIds.AND:
          op = JBinaryOperator.ASG_BIT_AND;
          break;
        case OperatorIds.OR:
          op = JBinaryOperator.ASG_BIT_OR;
          break;
        case OperatorIds.XOR:
          op = JBinaryOperator.ASG_BIT_XOR;
          break;
        case OperatorIds.REMAINDER:
          op = JBinaryOperator.ASG_MOD;
          break;
        case OperatorIds.LEFT_SHIFT:
          op = JBinaryOperator.ASG_SHL;
          break;
        case OperatorIds.RIGHT_SHIFT:
          op = JBinaryOperator.ASG_SHR;
          break;
        case OperatorIds.UNSIGNED_RIGHT_SHIFT:
          op = JBinaryOperator.ASG_SHRU;
          break;
        default:
          throw new AssertionError("Unexpected operator for CompoundAssignment");
      }
      pushBinaryOp(x, op);
    }

    @Override
    public void endVisit(ConditionalExpression x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        JExpression valueIfFalse = pop(x.valueIfFalse);
        JExpression valueIfTrue = pop(x.valueIfTrue);
        JExpression condition = pop(x.condition);
        push(new JConditionalExpression(info, condition, valueIfTrue,
            valueIfFalse));
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(ConstructorDeclaration x, ClassScope scope) {
      try {
        List<JStatement> statements = pop(x.statements);
        JStatement constructorCall = pop(x.constructorCall);
        JBlock block = curMethod.body.getBlock();
        SourceInfo info = curMethod.method.getSourceInfo();

        /*
         * Determine if we have an explicit this call. The presence of an
         * explicit this call indicates we can skip certain initialization steps
         * (as the callee will perform those steps for us). These skippable
         * steps are 1) assigning synthetic args to fields and 2) running
         * initializers.
         */
        boolean hasExplicitThis = (x.constructorCall != null) && !x.constructorCall.isSuperAccess();

        /*
         * All synthetic fields must be assigned, unless we have an explicit
         * this constructor call, in which case the callee will assign them for
         * us.
         */
        if (!hasExplicitThis) {
          ReferenceBinding declaringClass = (ReferenceBinding) x.binding.declaringClass.erasure();
          if (isNested(declaringClass)) {
            NestedTypeBinding nestedBinding = (NestedTypeBinding) declaringClass;
            if (nestedBinding.enclosingInstances != null) {
              for (SyntheticArgumentBinding arg : nestedBinding.enclosingInstances) {
                if (arg.actualOuterLocalVariable != null || arg.matchingField != null) {
                  JBinaryOperation asg = assignSyntheticField(info, arg);
                  block.addStmt(asg.makeStatement());
                }
              }
            }

            if (nestedBinding.outerLocalVariables != null) {
              for (SyntheticArgumentBinding arg : nestedBinding.outerLocalVariables) {
                JBinaryOperation asg = assignSyntheticField(info, arg);
                block.addStmt(asg.makeStatement());
              }
            }
          }
        }

        if (constructorCall != null) {
          block.addStmt(constructorCall);
        }

        /*
         * Call the synthetic instance initializer method, unless we have an
         * explicit this constructor call, in which case the callee will.
         */
        if (!hasExplicitThis) {
          JDefinedClassOrInterface curType = curClass.type;
          JMethod initMethod =
              curType.getMethod(INIT_METHOD_NAME, JPrimitiveTypeEnum.VOID.getType());
          JMethodCall initCall = makeMethodCall(info, makeThisRef(info), curType, initMethod);
          block.addStmt(initCall.makeStatement());
        }

        // user code (finally!)
        block.addStmts(statements);

        if ((x.bits & ASTNode.NeedFreeReturn) != 0) {
          generateImplicitReturn();
        }

        popMethodInfo();
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(ContinueStatement x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        push(new JContinueStatement(info, getOrCreateLabel(info, x.label)));
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(DoStatement x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        JExpression condition = pop(x.condition);
        JStatement action = pop(x.action);
        if (action == null) {
          // IR contains empty block rather than null value
          action = new JBlock(info);
        }
        push(new JDoStatement(info, condition, action));
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(DoubleLiteral x, BlockScope scope) {
      try {
        push(new JDoubleLiteral(makeSourceInfo(x), x.constant.doubleValue()));
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(EmptyStatement x, BlockScope scope) {
      push(null);
    }

    @Override
    public void endVisit(EqualExpression x, BlockScope scope) {
      JBinaryOperator op;
      switch ((x.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT) {
        case OperatorIds.EQUAL_EQUAL:
          op = JBinaryOperator.EQ;
          break;
        case OperatorIds.NOT_EQUAL:
          op = JBinaryOperator.NEQ;
          break;
        default:
          throw new AssertionError("Unexpected operator for EqualExpression");
      }
      pushBinaryOp(x, op);
    }

    @Override
    public void endVisit(ExplicitConstructorCall x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        JConstructor ctor = (JConstructor) getTypeMap().get(x.binding);
        JExpression trueQualifier = makeThisRef(info);
        JMethodCall call = makeMethodCall(info, trueQualifier, ctor.getEnclosingType(), ctor);
        List<JExpression> callArgs = popCallArgs(info, x.arguments, x.binding);

        if (getEnumSuperClass(curClass.classType) != null) {
          // Enums: wire up synthetic name/ordinal params to the super method.
          JParameterRef enumNameRef = new JParameterRef(info, curMethod.method.getParams().get(0));
          call.addArg(enumNameRef);
          JParameterRef enumOrdinalRef =
              new JParameterRef(info, curMethod.method.getParams().get(1));
          call.addArg(enumOrdinalRef);
        }

        if (x.isSuperAccess()) {
          JExpression qualifier = pop(x.qualification);

          if (qualifier != null) {
            // JLS 8.8.7.1. Explicit Constructor Invocations
            // If the explicit constructor has a qualifier, we have to check for a null pointer
            // d.super(...) => new A((tmp = d, tmp.getClass(), super(...)));
            List<JExpression> exprs = new ArrayList<JExpression>();
            JLocal tmp =
                new JLocal(info, ".superInstanceQualifier" + superInstanceQualifierSuffix++,
                    qualifier.getType(), JModifier.FINAL | JModifier.SYNTHETIC, curMethod.body);
            JAsgOperation asg = new JAsgOperation(info, new JLocalRef(info, tmp), qualifier);
            exprs.add(asg);
            curMethod.body.addLocal(tmp);

            JMethodCall getClassCall =
                makeMethodCall(info, new JLocalRef(info, tmp), javaLangObject, getGetClassMethod());
            exprs.add(getClassCall);

            exprs.add(call);

            qualifier = new JLocalRef(info, tmp);

            JMultiExpression multiExpr = new JMultiExpression(info, exprs);
            push(multiExpr.makeStatement());
          } else {
            push(call.makeStatement());
          }

          ReferenceBinding superClass = x.binding.declaringClass;
          boolean nestedSuper = isNested(superClass);
          if (x.qualification != null
              && (!x.qualification.resolvedType.isCompatibleWith(superClass.enclosingType())
                  || !nestedSuper)) {
            // JLS 8.8.7.1. Explicit Constructor Invocations
            // Let C be the class being instantiated, and let S be the direct superclass of C.
            // Let O be the innermost lexically enclosing class of S
            // If invocation is qualified, it is a compile-time error if the type of qualified
            // expression is not O or a subclass of O
            scope.problemReporter().unnecessaryEnclosingInstanceSpecification(
                x.qualification,
                superClass);
          }
          if (nestedSuper) {
            processSuperCallThisArgs(superClass, call, qualifier, x.qualification);
          }
          call.addArgs(callArgs);
          if (nestedSuper) {
            processSuperCallLocalArgs(superClass, call);
          }
        } else {
          assert (x.qualification == null);
          ReferenceBinding declaringClass = x.binding.declaringClass;
          boolean nested = isNested(declaringClass);
          if (nested) {
            processThisCallThisArgs(declaringClass, call);
          }
          call.addArgs(callArgs);
          if (nested) {
            processThisCallLocalArgs(declaringClass, call);
          }

          push(call.makeStatement());
        }
      } catch (Throwable e) {
        throw translateException(x, e);
      } finally {
        scope.methodScope().isConstructorCall = false;
      }
    }

    @Override
    public void endVisit(ExtendedStringLiteral x, BlockScope scope) {
      endVisit((StringLiteral) x, scope);
    }

    @Override
    public void endVisit(FalseLiteral x, BlockScope scope) {
      push(new JBooleanLiteral(makeSourceInfo(x), false));
    }

    @Override
    public void endVisit(FieldDeclaration x, MethodScope scope) {
      try {
        JExpression initialization = pop(x.initialization);

        if (initialization != null) {
          JField field = getTypeMap().get(x.binding);
          assert !(field instanceof JEnumField) || (initialization instanceof JNewInstance);

          SourceInfo info = makeSourceInfo(x);
          JExpression instance = null;
          if (!x.isStatic()) {
            instance = makeThisRef(info);
          }

          if ((x.initialization.constant != Constant.NotAConstant)
              && isConstantType(x.binding.type.id)) {
            initialization = getConstant(x.initialization, x.binding.type.id);
          }

          // ctor sets up the field's initializer.
          JFieldInitializer decl = new JFieldInitializer(info,
              new JFieldRef(info, instance, field.getId(), curClass.type),
              initialization);
          field.setFieldInitializer(decl);
          // will either be init or clinit
          curMethod.body.getBlock().addStmt(decl);
        }
        popMethodInfo();
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(FieldReference x, BlockScope scope) {
      try {
        FieldBinding fieldBinding = x.binding;
        SourceInfo info = makeSourceInfo(x);
        JExpression instance = pop(x.receiver);
        JExpression expr;
        if (fieldBinding.declaringClass == null) {
          assert ARRAY_LENGTH_FIELD.equals(String.valueOf(fieldBinding.name)) :
            "Expected [array].length.";
          expr = new JArrayLength(info, instance);
        } else {
          JField field = getTypeMap().get(fieldBinding);
          // TODO(delphinemartin): it would be better to use the codegenBinding
          // of the FieldReference but the field is protected
          expr = new JFieldRef(info, instance, field.getId(),
              (JClassOrInterface) getTypeMap().get(x.actualReceiverType));
        }

        if (x.genericCast != null) {
          JType castType = getTypeMap().get(x.genericCast);
          /*
           * Note, this may result in an invalid AST due to an LHS cast
           * operation. We fix this up in FixAssignmentToUnbox.
           */
          expr = maybeCast(castType, expr);
        }
        push(expr);
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(FloatLiteral x, BlockScope scope) {
      try {
        push(new JFloatLiteral(makeSourceInfo(x), x.constant.floatValue()));
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(ForeachStatement x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);

        JBlock body = popBlock(info, x.action);
        JExpression collection = pop(x.collection);
        JStatement elementDecl = pop(x.elementVariable);
        assert (elementDecl == null);

        JLocal elementVar = (JLocal) curMethod.locals.get(x.elementVariable.binding);
        String elementVarName = elementVar.getName();

        JForStatement result;
        if (x.collectionVariable != null) {
          /**
           * <pre>
         * for (final T[] i$array = collection,
         *          int i$index = 0,
         *          final int i$max = i$array.length;
         *      i$index < i$max; ++i$index) {
         *   T elementVar = i$array[i$index];
         *   // user action
         * }
         * </pre>
           */
          JLocal arrayVar =
              new JLocal(info, elementVarName + "$array", collection.getType(), JModifier.FINAL
                  | JModifier.SYNTHETIC, curMethod.body);
          curMethod.body.addLocal(arrayVar);
          JLocal indexVar =
              new JLocal(info, elementVarName + "$index", JPrimitiveTypeEnum.INT.getType(),
                  JModifier.SYNTHETIC, curMethod.body);
          curMethod.body.addLocal(indexVar);
          JLocal maxVar =
              new JLocal(info, elementVarName + "$max", JPrimitiveTypeEnum.INT.getType(),
                  JModifier.FINAL | JModifier.SYNTHETIC, curMethod.body);
          curMethod.body.addLocal(maxVar);

          List<JStatement> initializers = new ArrayList<JStatement>(3);
          // T[] i$array = arr
          initializers.add(makeAssignStatement(info, arrayVar, collection));
          // int i$index = 0
          initializers.add(makeAssignStatement(info, indexVar, new JIntLiteral(info, 0)));
          // int i$max = i$array.length
          initializers.add(makeAssignStatement(
              info, maxVar, new JArrayLength(info, new JLocalRef(info,
              arrayVar))));

          // i$index < i$max
          JExpression condition =
              new JLtOperation(info, new JLocalRef(
                  info, indexVar), new JLocalRef(info, maxVar));

          // ++i$index
          List<JExpressionStatement> increments = new ArrayList<JExpressionStatement>(1);
          increments.add(new JPrefixIncOperation(info, new JLocalRef(info,
              indexVar)).makeStatement());

          // T elementVar = i$array[i$index];
          elementDecl = new JAsgOperation(info, new JLocalRef(info, elementVar),
              new JArrayRef(info, new JLocalRef(info, arrayVar), new JLocalRef(info, indexVar)))
                .makeStatement();
          body.addStmt(0, elementDecl);

          result = new JForStatement(info, initializers, condition, increments, body);
        } else {
          /**
           * <pre>
           * for (Iterator&lt;T&gt; i$iterator = collection.iterator(); i$iterator.hasNext();) {
           *   T elementVar = i$iterator.next();
           *   // user action
           * }
           * </pre>
           */
          CompilationUnitScope cudScope = scope.compilationUnitScope();
          ReferenceBinding javaUtilIterator = scope.getJavaUtilIterator();
          ReferenceBinding javaLangIterable = scope.getJavaLangIterable();
          MethodBinding iterator = javaLangIterable.getExactMethod(ITERATOR, NO_TYPES, cudScope);
          MethodBinding hasNext = javaUtilIterator.getExactMethod(HAS_NEXT, NO_TYPES, cudScope);
          MethodBinding next = javaUtilIterator.getExactMethod(NEXT, NO_TYPES, cudScope);
          JLocal iteratorVar =
              new JLocal(info, (elementVarName + "$iterator"), getTypeMap().get(javaUtilIterator),
                  JModifier.DEFAULT, curMethod.body);
          curMethod.body.addLocal(iteratorVar);

          List<JStatement> initializers = new ArrayList<JStatement>(1);
          // Iterator<T> i$iterator = collection.iterator()
          JMethod jIteratorMethod = getTypeMap().get(iterator);
          JDefinedClassOrInterface receiverType = jIteratorMethod.getEnclosingType();
          initializers.add(makeAssignStatement(info, iteratorVar,
              makeMethodCall(info, collection, receiverType, jIteratorMethod)));

          JDefinedClassOrInterface jIterator =
              (JDefinedClassOrInterface) getTypeMap().get(javaUtilIterator);
          // i$iterator.hasNext()
          JExpression condition =
              makeMethodCall(info, new JLocalRef(info, iteratorVar), jIterator,
                  getTypeMap().get(hasNext));

          // i$iterator.next();
          JExpression callToNext = makeMethodCall(info, new JLocalRef(info, iteratorVar), jIterator,
              getTypeMap().get(next));

          // Perform any implicit reference type casts (due to generics).
          // Note this occurs before potential unboxing.
          if (!elementVar.getType().equals(javaLangObject)) {
            TypeBinding collectionElementType = (TypeBinding) collectionElementTypeField.get(x);
            JType toType = getTypeMap().get(collectionElementType);
            assert (toType instanceof JReferenceType);
            callToNext = maybeCast(toType, callToNext);
          }
          // T elementVar = (T) i$iterator.next();
          elementDecl = new JAsgOperation(info, new JLocalRef(info, elementVar), callToNext)
                .makeStatement();
          body.addStmt(0, elementDecl);

          result =
              new JForStatement(info, initializers, condition, Collections
                  .<JExpressionStatement> emptyList(), body);
        }

        push(result);
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(ForStatement x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        JStatement action = pop(x.action);
        List<JExpressionStatement> increments = pop(x.increments);
        JExpression condition = pop(x.condition);
        List<JStatement> initializations = pop(x.initializations);
        if (action == null) {
          // IR contains empty block rather than null value
          action = new JBlock(info);
        }
        if (condition == null) {
          condition = new JBooleanLiteral(info, true);
        }
        push(new JForStatement(info, initializations, condition, increments, action));
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(IfStatement x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        JStatement elseStatement = pop(x.elseStatement);
        JStatement thenStatement = pop(x.thenStatement);
        JExpression condition = pop(x.condition);
        Constant optimizedBooleanConstant = x.condition.optimizedBooleanConstant();

        if (thenStatement == null) {
          if (optimizedBooleanConstant != Constant.NotAConstant) {
            assert x.thenStatement != null || optimizedBooleanConstant instanceof BooleanConstant;
            assert x.thenStatement != null
                || !(((BooleanConstant) optimizedBooleanConstant).booleanValue());
            if (x.condition.constant != Constant.NotAConstant) {
              // Condition is constant without side effect, generates only else
              if (elseStatement != null) {
                push(elseStatement);
              } else {
                push(null);
              }
            } else {
              if (elseStatement != null) {
                // Condition is constant with side effect, generates condition and else
                JBlock block = new JBlock(info);
                block.addStmt(condition.makeStatement());
                block.addStmt(elseStatement);
                push(block);
              } else {
                // Condition is constant with side effect, generates only condition
                push(condition.makeStatement());
              }
            }
          } else {
            if (elseStatement != null) {
              // Condition is not constant, generates if with invert condition
              push(new JIfStatement(info, new JPrefixNotOperation(info, condition), elseStatement,
                null));
            } else {
              // Condition is not constant, generate only cond
              push(condition.makeStatement());
            }
          }
        } else {
          if (optimizedBooleanConstant != Constant.NotAConstant) {
            assert optimizedBooleanConstant instanceof BooleanConstant;
            assert ((BooleanConstant) optimizedBooleanConstant).booleanValue();
            assert elseStatement == null;
            if (x.condition.constant != Constant.NotAConstant) {
              // Condition is constant without side effect, generates only then
              push(thenStatement);
            } else {
              // Condition is constant with side effect, generates condition and then
              JBlock block = new JBlock(info);
              block.addStmt(condition.makeStatement());
              block.addStmt(thenStatement);
              push(block);
            }

          } else {
            // Condition is not constant, generates classical if
            push(new JIfStatement(info, condition, thenStatement, elseStatement));
          }
        }
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(Initializer x, MethodScope scope) {
      try {
        JBlock block = pop(x.block);
        if (block != null) {
          curMethod.body.getBlock().addStmt(block);
        }
        popMethodInfo();
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(InstanceOfExpression x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        JExpression expr = pop(x.expression);
        JReferenceType testType = (JReferenceType) getTypeMap().get(x.type.resolvedType);
        push(new JInstanceOf(info, testType, expr));
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(IntLiteral x, BlockScope scope) {
      try {
        push(new JIntLiteral(makeSourceInfo(x), x.constant.intValue()));
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(LabeledStatement x, BlockScope scope) {
      try {
        JStatement statement = pop(x.statement);
        if (statement == null) {
          push(null);
          return;
        }
        SourceInfo info = makeSourceInfo(x);
        push(new JLabeledStatement(info, getOrCreateLabel(info, x.label), statement));
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(LocalDeclaration x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        JLocal local = (JLocal) curMethod.locals.get(x.binding);
        assert local != null;
        JLocalRef localRef = new JLocalRef(info, local);
        JExpression initialization = pop(x.initialization);
        if (initialization != null) {
          push(new JAsgOperation(info, localRef, initialization).makeStatement());
        } else {
          push(null);
        }
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(LongLiteral x, BlockScope scope) {
      try {
        push(new JLongLiteral(makeSourceInfo(x), x.constant.longValue()));
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(MessageSend x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        JMethod method = getTypeMap().get(x.binding);

        List<JExpression> arguments = popCallArgs(info, x.arguments, x.binding);
        JExpression receiver = pop(x.receiver);
        if (x.receiver instanceof ThisReference) {
          if (method.isStatic()) {
            // don't bother qualifying it, it's a no-op
            receiver = null;
          } else if ((x.bits & ASTNode.DepthMASK) != 0) {
            // outer method can be reached through emulation if implicit access
            ReferenceBinding targetType =
                scope.enclosingSourceType().enclosingTypeAt(
                    (x.bits & ASTNode.DepthMASK) >> ASTNode.DepthSHIFT);
            receiver = makeThisReference(info, targetType, true, scope);
          }
        }

        JDefinedClassOrInterface receiverType;

        JType jType = getTypeMap().get(x.actualReceiverType);
        if (jType instanceof JClassOrInterface) {
          if (jType instanceof JInterface && method.getEnclosingType().equals(javaLangObject)) {
            receiverType = method.getEnclosingType();
          } else {
            receiverType = (JDefinedClassOrInterface) jType;
          }
        } else {
          receiverType = method.getEnclosingType();
        }

        JMethodCall call;
        // On a super ref, make a super call. Oddly enough,
        // QualifiedSuperReference not derived from SuperReference!
         boolean isSuperRef =
             x.receiver instanceof SuperReference || x.receiver instanceof QualifiedSuperReference;
         if (isSuperRef) {
           call = makeSuperCall(info, receiver, receiverType, method);
         } else {
           call = makeMethodCall(info, receiver, receiverType, method);
         }

        // The arguments come first...
        call.addArgs(arguments);

        if (x.valueCast != null) {
          JType castType = getTypeMap().get(x.valueCast);
          push(maybeCast(castType, call));
        } else {
          push(call);
        }
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(MethodDeclaration x, ClassScope scope) {
      try {
        if (x.isNative()) {
          processNativeMethod();
        } else {
          List<JStatement> statements = pop(x.statements);
          curMethod.body.getBlock().addStmts(statements);
          if ((x.bits & ASTNode.NeedFreeReturn) != 0) {
            generateImplicitReturn();
          }
        }
        popMethodInfo();
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(NullLiteral x, BlockScope scope) {
      push(new JNullLiteral(makeSourceInfo(x)));
    }

    @Override
    public void endVisit(OR_OR_Expression x, BlockScope scope) {
      pushBinaryOp(x, JBinaryOperator.OR);
    }

    @Override
    public void endVisit(PostfixExpression x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        JUnaryOperator op;
        switch (x.operator) {
          case OperatorIds.MINUS:
            op = JUnaryOperator.DEC;
            break;

          case OperatorIds.PLUS:
            op = JUnaryOperator.INC;
            break;

          default:
            throw new AssertionError("Unexpected postfix operator");
        }

        JExpression lhs = pop(x.lhs);
        push(JPostfixOperation.create(info, op, lhs));
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(PrefixExpression x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        JUnaryOperator op;
        switch (x.operator) {
          case OperatorIds.MINUS:
            op = JUnaryOperator.DEC;
            break;

          case OperatorIds.PLUS:
            op = JUnaryOperator.INC;
            break;

          default:
            throw new AssertionError("Unexpected prefix operator");
        }

        JExpression lhs = pop(x.lhs);
        push(JPrefixOperation.create(info, op, lhs));
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(QualifiedAllocationExpression x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        List<JExpression> arguments = popCallArgs(info, x.arguments, x.binding);
        pushNewExpression(info, x, x.enclosingInstance(), arguments, scope);
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(QualifiedNameReference x, BlockScope scope) {
      try {
        JExpression curRef = resolveNameReference(x, scope);
        if (curRef == null) {
          push(null);
          return;
        }
        if (x.genericCast != null) {
          JType castType = getTypeMap().get(x.genericCast);
          curRef = maybeCast(castType, curRef);
        }
        SourceInfo info = curRef.getSourceInfo();

        /*
         * JDT represents multiple field access as an array of fields, each
         * qualified by everything to the left. So each subsequent item in
         * otherBindings takes the current expression as a qualifier.
         */
        if (x.otherBindings != null) {
          for (int i = 0; i < x.otherBindings.length; ++i) {
            FieldBinding fieldBinding = x.otherBindings[i];
            if (fieldBinding.declaringClass == null) {
              // probably array.length
              assert ARRAY_LENGTH_FIELD.equals(String.valueOf(fieldBinding.name)) :
                "Expected [array].length.";
              curRef = new JArrayLength(info, curRef);
            } else {
              JField field = getTypeMap().get(fieldBinding);
              curRef =
                  new JFieldRef(info, curRef, field.getId(), (JClassOrInterface) curRef.getType());
            }
            if (x.otherGenericCasts != null && x.otherGenericCasts[i] != null) {
              JType castType = getTypeMap().get(x.otherGenericCasts[i]);
              curRef = maybeCast(castType, curRef);
            }
          }
        }
        push(curRef);
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(QualifiedSuperReference x, BlockScope scope) {
      try {
        // Oddly enough, super refs can be modeled as this refs, because
        // whatever expression they qualify has already been resolved.
        endVisit((QualifiedThisReference) x, scope);
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(QualifiedThisReference x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        ReferenceBinding targetType = (ReferenceBinding) x.qualification.resolvedType;
        push(makeThisReference(info, targetType, true, scope));
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(ReturnStatement x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        JExpression expression = pop(x.expression);
        push(new JReturnStatement(info, expression));
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(SingleNameReference x, BlockScope scope) {
      try {
        JExpression result = resolveNameReference(x, scope);
        if (result == null) {
          push(null);
          return;
        }
        if (x.genericCast != null) {
          JType castType = getTypeMap().get(x.genericCast);
          result = maybeCast(castType, result);
        }
        push(result);
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(StringLiteral x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        push(getStringLiteral(info, x.constant.stringValue()));
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(StringLiteralConcatenation x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        push(getStringLiteral(info, x.constant.stringValue()));
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(SuperReference x, BlockScope scope) {
      try {
        assert getTypeMap().get(x.resolvedType).equals(curClass.classType.getSuperClass());
        // Super refs can be modeled as a this ref.
        push(makeThisRef(makeSourceInfo(x)));
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(SwitchStatement x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);

        JBlock block = popBlock(info, x.statements);
        JExpression expression = pop(x.expression);

        JCaseStatement defaultCase = null;
        List<JCaseStatement> cases = switchCases.pop();
        for (JCaseStatement switchCase : cases) {
          if (switchCase.getExpr() == null) {
            defaultCase = switchCase;
            break;
          }
        }
        cases.remove(defaultCase);
        push(new JSwitchStatement(info, expression, block, cases, defaultCase));
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(SynchronizedStatement x, BlockScope scope) {
      try {
        JBlock block = pop(x.block);
        JExpression expression = pop(x.expression);
        JSynchronizedBlock syncBlock = new JSynchronizedBlock(makeSourceInfo(x), expression, block);
        push(syncBlock);
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(ThisReference x, BlockScope scope) {
      try {
        assert getTypeMap().get(x.resolvedType).equals(curClass.type);
        push(makeThisRef(makeSourceInfo(x)));
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(ThrowStatement x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        JExpression exception = pop(x.exception);
        push(new JThrowStatement(info, exception));
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(TrueLiteral x, BlockScope scope) {
      push(new JBooleanLiteral(makeSourceInfo(x), true));
    }

    @Override
    public void endVisit(TryStatement x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);

        JBlock finallyBlock = pop(x.finallyBlock);
        List<JBlock> blocks = pop(x.catchBlocks);
        JBlock tryBlock = pop(x.tryBlock);
        List<JStatement> resourceInits = pop(x.resources);

        List<JCatchBlock> catchBlocks;

        if (x.catchBlocks != null) {
          catchBlocks = new ArrayList<JCatchBlock>(blocks.size());
          int index = 0;
          for (JBlock b : blocks) {
            Argument argument = x.catchArguments[index];
            JLocal local = (JLocal) curMethod.locals.get(argument.binding);

            List<JClass> catchTypes = new ArrayList<JClass>();
            if (argument.type instanceof UnionTypeReference) {
              for (TypeReference type : ((UnionTypeReference) argument.type).typeReferences) {
                JType jType = getTypeMap().get(type.resolvedType);
                catchTypes.add((JClass) jType);
              }
            } else {
              JType jType = getTypeMap().get(argument.binding.type);
              catchTypes.add((JClass) jType);
            }

            JCatchBlock catchBlock = new JCatchBlock(b.getSourceInfo(), catchTypes, local);

            catchBlock.addStmts(b.getStatements());
            catchBlocks.add(catchBlock);
            index++;
          }
        } else {
          catchBlocks = new ArrayList<JCatchBlock>(0);
        }

        push(
            new JTryStatement(info, resourceInits, tryBlock, catchBlocks, finallyBlock));

      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(TypeDeclaration x, ClassScope scope) {
      endVisit(x);
    }

    @Override
    public void endVisit(TypeDeclaration x, CompilationUnitScope scope) {
      endVisit(x);
    }

    @Override
    public void endVisit(UnaryExpression x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        JUnaryOperator op;
        int operator = ((x.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT);

        switch (operator) {
          case OperatorIds.MINUS:
            op = JUnaryOperator.NEG;
            break;

          case OperatorIds.NOT:
            op = JUnaryOperator.NOT;
            break;

          case OperatorIds.PLUS:
            // Odd case.. useless + operator; just leave the operand on the
            // stack.
            return;

          case OperatorIds.TWIDDLE:
            op = JUnaryOperator.BIT_NOT;
            break;

          default:
            throw new AssertionError("Unexpected operator for unary expression");
        }

        JExpression expression = pop(x.expression);
        push(JPrefixOperation.create(info, op, expression));
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(WhileStatement x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        JStatement action = pop(x.action);
        JExpression condition = pop(x.condition);
        if (action == null) {
          // IR contains empty block rather than null value
          action = new JBlock(info);
        }
        push(new JWhileStatement(info, condition, action));
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public final void endVisit(TypeDeclaration typeDecl, BlockScope scope) {
      if (typeDecl.binding == null || typeDecl.binding.constantPoolName() == null) {
        assert false;
        /*
         * Weird case: if JDT determines that this local class is totally
         * uninstantiable, it won't bother allocating a local name.
         */
        return;
      }
      endVisit(typeDecl);
      if (!typeDecl.binding.isAnonymousType()) {
        // Class declaration as a statement; insert a dummy statement.
        push(null);
      }
    }

    @Override
    public boolean visit(AnnotationMethodDeclaration x, ClassScope classScope) {
      JAnnotationMethod method = (JAnnotationMethod) getTypeMap().get(x.binding);
      assert method.getEnclosingType().getLoader() instanceof EcjSourceTypeLoader;
      JMethodBody body = null;
      pushMethodInfo(new MethodInfo(method, body, x.scope));

      Annotation[] annotations = x.annotations;
      if (annotations != null) {
        for (Annotation annotation : annotations) {
          annotation.traverse(this, x.scope);
        }
      }
      if (x.returnType != null) {
        x.returnType.traverse(this, x.scope);
      }

      if (x.defaultValue != null) {
        JLiteral defaultValue = annotationParser.parseLiteral(x.defaultValue, x.binding.returnType,
            x.scope);
        method.setDefaultValue(defaultValue);
        defaultValue.updateParents(method);
      }

      return false;
    }

    @Override
    public boolean visit(Argument x, BlockScope scope) {
      // handled by parents
      return true;
    }

    @Override
    public boolean visit(Block x, BlockScope scope) {
      x.statements = reduceToReachable(x.statements);
      return true;
    }

    @Override
    public boolean visit(ConstructorDeclaration x, ClassScope scope) {
      try {
        JConstructor method = (JConstructor) getTypeMap().get(x.binding);
        assert method.getEnclosingType().getLoader() instanceof EcjSourceTypeLoader;
        SourceInfo sourceInfo = method.getSourceInfo();
        JMethodBody body = new JMethodBody(sourceInfo, new JBlock(sourceInfo));
        method.setBody(body);
        pushMethodInfo(new MethodInfo(method, body, x.scope));

        // Map all arguments.
        Iterator<JParameter> it = method.getParams().iterator();

        // Enum arguments have no mapping.
        if (getEnumSuperClass(curClass.classType) != null) {
          // Skip past name and ordinal.
          it.next();
          it.next();
        }

        // Map synthetic arguments for outer this.
        ReferenceBinding declaringClass = (ReferenceBinding) x.binding.declaringClass.erasure();
        boolean isNested = isNested(declaringClass);
        if (isNested) {
          NestedTypeBinding nestedBinding = (NestedTypeBinding) declaringClass;
          if (nestedBinding.enclosingInstances != null) {
            for (int i = 0; i < nestedBinding.enclosingInstances.length; ++i) {
              SyntheticArgumentBinding arg = nestedBinding.enclosingInstances[i];
              curMethod.locals.put(arg, it.next());
            }
          }
        }

        // Map user arguments.
        if (x.arguments != null) {
          for (Argument argument : x.arguments) {
            curMethod.locals.put(argument.binding, it.next());
          }
        }

        // Map synthetic arguments for locals.
        if (isNested) {
          // add synthetic args for locals
          NestedTypeBinding nestedBinding = (NestedTypeBinding) declaringClass;
          // add synthetic args for outer this and locals
          if (nestedBinding.outerLocalVariables != null) {
            for (int i = 0; i < nestedBinding.outerLocalVariables.length; ++i) {
              SyntheticArgumentBinding arg = nestedBinding.outerLocalVariables[i];
              curMethod.locals.put(arg, it.next());
            }
          }
        }

        x.statements = reduceToReachable(x.statements);
        return true;
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public boolean visit(ExplicitConstructorCall explicitConstructor, BlockScope scope) {
      scope.methodScope().isConstructorCall = true;
      return true;
    }

    @Override
    public boolean visit(FieldDeclaration x, MethodScope scope) {
      try {
        assert getTypeMap().get(x.binding).getEnclosingType().getLoader()
          instanceof EcjSourceTypeLoader;

        pushInitializerMethodInfo(x, scope);
        return true;
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public boolean visit(ForStatement x, BlockScope scope) {
      // SEE NOTE ON JDT FORCED OPTIMIZATIONS
      if (isOptimizedFalse(x.condition)) {
        x.action = null;
      }
      return true;
    }

    @Override
    public boolean visit(IfStatement x, BlockScope scope) {
      // SEE NOTE ON JDT FORCED OPTIMIZATIONS
      if (isOptimizedFalse(x.condition)) {
        x.thenStatement = null;
      } else if (isOptimizedTrue(x.condition)) {
        x.elseStatement = null;
      }
      return true;
    }

    @Override
    public boolean visit(Initializer x, MethodScope scope) {
      try {
        pushInitializerMethodInfo(x, scope);
        return true;
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public boolean visit(LocalDeclaration x, BlockScope scope) {
      try {
        curMethod.body.addLocal(createLocal(x));
        return true;
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public boolean visit(MarkerAnnotation annotation, BlockScope scope) {
      return visit((Annotation) annotation, scope);
    }

    @Override
    public boolean visit(MethodDeclaration x, ClassScope scope) {
      try {
        JMethod method = getTypeMap().get(x.binding);
        assert method.getEnclosingType().getLoader() instanceof EcjSourceTypeLoader;
        JMethodBody body = null;
        if (!method.isNative()) {
          SourceInfo sourceInfo = method.getSourceInfo();
          body = new JMethodBody(sourceInfo, new JBlock(sourceInfo));
          method.setBody(body);
        }
        pushMethodInfo(new MethodInfo(method, body, x.scope));

        // Map user arguments.
        Iterator<JParameter> it = method.getParams().iterator();
        if (x.arguments != null) {
          for (Argument argument : x.arguments) {
            curMethod.locals.put(argument.binding, it.next());
          }
        }
        x.statements = reduceToReachable(x.statements);
        return true;
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public boolean visit(NormalAnnotation annotation, BlockScope scope) {
      return visit((Annotation) annotation, scope);
    }

    @Override
    public boolean visit(SingleMemberAnnotation annotation, BlockScope scope) {
      return visit((Annotation) annotation, scope);
    }

    @Override
    public boolean visit(SwitchStatement x, BlockScope scope) {
      x.statements = reduceToReachable(x.statements);
      switchCases.push(new LinkedList<JCaseStatement>());
      return true;
    }

    @Override
    public boolean visit(TryStatement x, BlockScope scope) {
      try {
        if (x.catchBlocks != null) {
          for (Argument argument : x.catchArguments) {
            createLocal(argument);
          }
        }
        return true;
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    @Override
    public boolean visit(TypeDeclaration x, ClassScope scope) {
      return visit(x);
    }

    @Override
    public boolean visit(TypeDeclaration x, CompilationUnitScope scope) {
      return visit(x);
    }

    @Override
    public boolean visit(WhileStatement x, BlockScope scope) {
      // SEE NOTE ON JDT FORCED OPTIMIZATIONS
      if (isOptimizedFalse(x.condition)) {
        x.action = null;
      }
      return true;
    }



    @Override
    public final boolean visit(TypeDeclaration typeDecl, BlockScope scope) {
      if (typeDecl.binding == null || typeDecl.binding.constantPoolName() == null) {
        assert false;
        /*
         * Weird case: if JDT determines that this local class is totally
         * uninstantiable, it won't bother allocating a local name.
         */
        return false;
      }

      // Local types actually need to be created now.
      createTypes(typeDecl);
      createMembers(typeDecl);
      return visit(typeDecl);
    }

    protected void endVisit(TypeDeclaration x) {
      JDefinedClassOrInterface type = curClass.type;

      if (type instanceof JDefinedEnum) {
        processEnumType((JDefinedEnum) type);
      }

      if (type instanceof JDefinedClass) {
        addBridgeMethods(x.binding);
      }

      JMethod method =
          type.getMethod(NamingTools.STATIC_INIT_NAME, JPrimitiveTypeEnum.VOID.getType());
      JAbstractMethodBody body = method.getBody();
      assert body != null;
      ((JMethodBody) body).getBlock().addStmt(
          new JReturnStatement(session.getSourceInfoFactory().create(
              method.getSourceInfo().getEndLine(), method.getSourceInfo().getEndLine(),
              method.getSourceInfo().getFileName()), null));

      curClass = classStack.pop();
    }

    protected JBlock pop(Block x) {
      return (x == null) ? null : (JBlock) pop();
    }

    protected JExpression pop(Expression x) {
      if (x == null) {
        return null;
      }
      JExpression result = (JExpression) pop();
      if (result == null) {
        assert x instanceof NameReference || x instanceof ThisReference;
        return null;
      }
      result = simplify(result, x);
      return result;
    }

    @SuppressWarnings("unchecked")
    protected <T extends JExpression> List<T> pop(Expression[] expressions) {
      if (expressions == null) {
        return Collections.emptyList();
      }
      List<T> result = (List<T>) popList(expressions.length);
      for (int i = 0; i < expressions.length; ++i) {
        result.set(i, (T) simplify(result.get(i), expressions[i]));
      }
      return result;
    }

    protected JStatement pop(LocalDeclaration decl) {
      return (decl == null) ? null : (JStatement) pop();
    }

    protected JStatement pop(Statement x) {
      JNode pop = (x == null) ? null : pop();
      if (x instanceof Expression) {
        return simplify((JExpression) pop, (Expression) x).makeStatement();
      }
      return (JStatement) pop;
    }

    @SuppressWarnings("unchecked")
    protected <T extends JStatement> List<T> pop(Statement[] statements) {
      if (statements == null) {
        return Collections.emptyList();
      }
      List<T> result = (List<T>) popList(statements.length);
      int i = 0;
      for (ListIterator<T> it = result.listIterator(); it.hasNext(); ++i) {
        Object element = it.next();
        if (element == null) {
          it.remove();
        } else if (element instanceof JExpression) {
          it.set((T) simplify((JExpression) element, (Expression) statements[i]).makeStatement());
        }
      }
      return result;
    }

    protected JBlock popBlock(SourceInfo info, Statement statement) {
      JStatement stmt = pop(statement);
      if (stmt instanceof JBlock) {
        return (JBlock) stmt;
      }
      JBlock block = new JBlock(info);
      if (stmt != null) {
        block.addStmt(stmt);
      }
      return block;
    }

    protected JBlock popBlock(SourceInfo info, Statement[] statements) {
      List<JStatement> stmts = pop(statements);
      JBlock block = new JBlock(info);
      block.addStmts(stmts);
      return block;
    }

    protected void pushBinaryOp(Assignment x, JBinaryOperator op) {
      pushBinaryOp(x, op, x.lhs, x.expression);
    }

    protected void pushBinaryOp(BinaryExpression x, JBinaryOperator op) {
      pushBinaryOp(x, op, x.left, x.right);
    }

    protected boolean visit(@Nonnull Annotation annotation, @Nonnull BlockScope scope) {
      JAnnotationLiteral literal = (JAnnotationLiteral) annotationParser.parseLiteral(annotation,
          annotation.resolvedType, scope);

      Binding recipient = annotation.recipient;
      Annotable annotable;
      switch (recipient.kind()) {
        case Binding.PACKAGE:
          throw new AssertionError("Not yet supported");
        case Binding.GENERIC_TYPE:
        case Binding.TYPE:
          assert curClass.typeDecl.binding == recipient;
          annotable = curClass.type;
          break;
        case Binding.METHOD:
          annotable = getTypeMap().get((MethodBinding) recipient);
          break;
        case Binding.FIELD:
          annotable = getTypeMap().get((FieldBinding) recipient);
          break;
        case Binding.LOCAL:
          annotable = curMethod.locals.get(recipient);
          assert annotable != null;
          break;
        default:
          throw new AssertionError();
      }
      annotable.addAnnotation(literal);
      literal.updateParents((JNode) annotable);
      return false;
    }

    protected boolean visit(TypeDeclaration x) {
      JDefinedClassOrInterface type = (JDefinedClassOrInterface) getTypeMap().get(x.binding);
      assert type.getLoader() instanceof EcjSourceTypeLoader;
      classStack.push(curClass);
      curClass = new ClassInfo(type, x);

      /*
       * It's okay to defer creation of synthetic fields, they can't be
       * referenced until we analyze the code.
       */
      SourceTypeBinding binding = x.binding;
      if (isNested(binding)) {
        // add synthetic fields for outer this and locals
        assert (type instanceof JDefinedClass);
        NestedTypeBinding nestedBinding = (NestedTypeBinding) binding;

        if (x.binding.syntheticFields() != null) {
          for (FieldBinding fieldBinding : x.binding.syntheticFields()) {
            JType fieldType = getTypeMap().get(fieldBinding.type);
            SourceInfo info = type.getSourceInfo();
            int modifier = JModifier.FINAL | JModifier.SYNTHETIC;
            JField field = new JField(
                info, ReferenceMapper.intern(fieldBinding.name), type, fieldType, modifier);
            type.addField(field);
            getTypeMap().setField(fieldBinding, field);
            field.updateParents(type);
          }
        }

        if (nestedBinding.outerLocalVariables != null) {
          for (int i = 0; i < nestedBinding.outerLocalVariables.length; ++i) {
            SyntheticArgumentBinding arg = nestedBinding.outerLocalVariables[i];
            if (arg.matchingField == null) {
              // Create a field is not required, need works to remove them due to $init
              createSyntheticField(arg, type, JModifier.FINAL | JModifier.SYNTHETIC);
            }

          }
        }
      }
      return true;
    }

    /**
     * <p>
     * Add a bridge method to <code>clazzBinding</code> for any method it
     * inherits that implements an interface method but that has a different
     * erased signature from the interface method.
     * </p>
     *
     * <p>
     * The need for these bridges was pointed out in issue 3064. The goal is
     * that virtual method calls through an interface type are translated to
     * JavaScript that will function correctly. If the interface signature
     * matches the signature of the implementing method, then nothing special
     * needs to be done. If they are different, due to the use of generics, then
     * GenerateJavaScriptAST is careful to do the right thing. There is a
     * remaining case, though, that GenerateJavaScriptAST is not in a good
     * position to fix: a method could be inherited from a superclass, used to
     * implement an interface method that has a different type signature, and
     * does not have the interface method in its list of overrides. In that
     * case, a bridge method should be added that overrides the interface method
     * and then calls the implementation method.
     * </p>
     *
     * <p>
     * This method should only be called once all regular, non-bridge methods
     * have been installed on the Jack types.
     * </p>
     */
    private void addBridgeMethods(SourceTypeBinding clazzBinding) {
      /*
       * JDT adds bridge methods in all the places Jack needs them. Use JDT's
       * bridge methods.
       */
      if (clazzBinding.syntheticMethods() != null) {
        for (SyntheticMethodBinding synthmeth : clazzBinding.syntheticMethods()) {
          if (synthmeth.purpose == SyntheticMethodBinding.BridgeMethod && !synthmeth.isStatic()) {
            createBridgeMethod(synthmeth);
          }
        }
      }
    }

    private JBinaryOperation assignSyntheticField(SourceInfo info, SyntheticArgumentBinding arg) {
      JParameter param = (JParameter) curMethod.locals.get(arg);
      assert param != null;

      JField field = null;
      if (arg.matchingField == null) {
        field = curClass.syntheticArgToFields.get(arg);
      } else {
        field = typeMap.get(arg.matchingField);
      }

      assert field != null;
      JFieldRef lhs = makeInstanceFieldRef(info, field);
      JParameterRef rhs = new JParameterRef(info, param);
      JBinaryOperation asg = new JAsgOperation(info, lhs, rhs);
      return asg;
    }

    /**
     * Create a bridge method. It calls a same-named method with the same
     * arguments, but with a different type signature.
     */
    private void createBridgeMethod(@Nonnull SyntheticMethodBinding jdtBridgeMethod) {
      JMethod implMethod = getTypeMap().get(jdtBridgeMethod.targetMethod);
      SourceInfo info = implMethod.getSourceInfo();
      String[] paramNames = null;
      List<JParameter> implParams = implMethod.getParams();
      if (jdtBridgeMethod.parameters != null) {
        int paramCount = implParams.size();
        assert paramCount == jdtBridgeMethod.parameters.length;
        paramNames = new String[paramCount];
        for (int i = 0; i < paramCount; ++i) {
          paramNames[i] = implParams.get(i).getName();
        }
      }
      // bridge methods should not be flagged as VARARGS
      jdtBridgeMethod.modifiers &= ~JModifier.VARARGS;
      JMethod bridgeMethod = createSyntheticMethodFromBinding(info, jdtBridgeMethod, paramNames);
      JMethodBody body = (JMethodBody) bridgeMethod.getBody();
      assert body != null;

      pushMethodInfo(new MethodInfo(bridgeMethod, body, null /* no available scope */));

      // create a call and pass all arguments through, casting if necessary
      JMethodCall call = makeMethodCall(info, makeThisRef(info), implMethod.getEnclosingType(),
          implMethod);
      for (int i = 0; i < bridgeMethod.getParams().size(); i++) {
        JParameter param = bridgeMethod.getParams().get(i);
        JParameterRef paramRef = new JParameterRef(info, param);
        call.addArg(maybeCast(implParams.get(i).getType(), paramRef));
      }

      if (bridgeMethod.getType().equals(JPrimitiveTypeEnum.VOID.getType())) {
        body.getBlock().addStmt(call.makeStatement());
        body.getBlock().addStmt(new JReturnStatement(info, null));
      } else {
        body.getBlock().addStmt(new JReturnStatement(info, call));
      }
      popMethodInfo();
    }

    private JField createEnumValuesField(JDefinedEnum type) {
      // $VALUES = new E[]{A,B,B};
      JArrayType enumArrayType =
          (JArrayType) getTypeMap().get("[" + Jack.getLookupFormatter().getName(type));

      JField valuesField = new JField(type.getSourceInfo(), "$VALUES", type,
          enumArrayType,
          JModifier.STATIC | JModifier.FINAL | JModifier.PRIVATE | JModifier.SYNTHETIC);
      type.addField(valuesField);
      SourceInfo info = type.getSourceInfo();
      List<JExpression> initializers = new ArrayList<JExpression>();
      for (JEnumField field : type.getEnumList()) {
        JFieldRef fieldRef = new JFieldRef(info, null, field.getId(), type);
        initializers.add(fieldRef);
      }
      JNewArray newExpr = JNewArray.createWithInits(info, enumArrayType, initializers);
      JFieldRef valuesRef = new JFieldRef(info, null, valuesField.getId(), type);
      JAsgOperation assignValues = new JAsgOperation(info, valuesRef, newExpr);
      JMethod clinit = type.getMethod(NamingTools.STATIC_INIT_NAME,
          JPrimitiveTypeEnum.VOID.getType());
      JAbstractMethodBody body = clinit.getBody();
      assert body instanceof JMethodBody;
      JBlock clinitBlock = ((JMethodBody) body).getBlock();

      /*
       * HACKY: the $VALUES array must be initialized immediately after all of
       * the enum fields, but before any user initialization (which might rely
       * on $VALUES).
       */
      int insertionPoint = type.getEnumList().size();
      assert clinitBlock.getStatements().size() >= initializers.size();
      clinitBlock.addStmt(insertionPoint, assignValues.makeStatement());
      valuesField.updateParents(type);
      return valuesField;
    }

    private JLocal createLocal(LocalDeclaration x) {
      SourceInfo info = makeSourceInfo(x);

      LocalVariableBinding b = x.binding;
      TypeBinding resolvedType = x.type.resolvedType;

      JType localType;
      if (resolvedType.constantPoolName() != null) {
        localType = getTypeMap().get(resolvedType);
      } else {
        // Special case, a statically unreachable local type.
        localType = JNullType.INSTANCE;
      }

      JLocal newLocal =
          new JLocal(info, ReferenceMapper.intern(x.name), localType, b.isFinal() ? JModifier.FINAL
              : JModifier.DEFAULT, curMethod.body);

      char[] signature = b.type.signature();
      char[] genericSignature = b.type.genericTypeSignature();
      // Check if the generic signature really contains generic types i.e. is different from the
      // non-generic signature
      if (!CharOperation.equals(signature, genericSignature)) {
        newLocal.addMarker(new GenericSignature(ReferenceMapper.intern(genericSignature)));
      }
      curMethod.locals.put(b, newLocal);
      return newLocal;
    }

    private JField createSyntheticField(SyntheticArgumentBinding arg,
        JDefinedClassOrInterface enclosingType,
        int modifier) {
      JType type = getTypeMap().get(arg.type);
      SourceInfo info = enclosingType.getSourceInfo();
      JField field =
          new JField(info, ReferenceMapper.intern(arg.name), enclosingType, type,
              modifier | JModifier.SYNTHETIC);
      enclosingType.addField(field);
      curClass.syntheticArgToFields.put(arg, field);
      if (arg.matchingField != null) {
        getTypeMap().setField(arg.matchingField, field);
      }
      field.updateParents(enclosingType);
      return field;
    }

    /**
     * Get a new label of a particular name, or create a new one if it doesn't
     * exist already.
     */
    private JLabel getOrCreateLabel(SourceInfo info, char[] name) {
      if (name == null) {
        return null;
      }
      String sname = ReferenceMapper.intern(name);
      JLabel jlabel = new JLabel(info, sname);
      return jlabel;
    }

    private void implementMethod(JMethod method, JExpression returnValue) {
      JMethodBody body = (JMethodBody) method.getBody();
      assert body != null;
      JBlock block = body.getBlock();
      SourceInfo info;
      if (block.getStatements().size() > 0) {
        info = block.getStatements().get(0).getSourceInfo();
      } else {
        info = method.getSourceInfo();
      }
      block.clear();
      block.addStmt(new JReturnStatement(info, returnValue));
    }

    @Nonnull
    private JStatement makeAssignStatement(@Nonnull SourceInfo info, @Nonnull JLocal local,
        JExpression value) {
        return new JAsgOperation(info, new JLocalRef(info, local), value).makeStatement();
    }

    private JFieldRef makeInstanceFieldRef(SourceInfo info, JField field) {
      return new JFieldRef(info, makeThisRef(info), field.getId(), field.getEnclosingType());
    }

    private JExpression makeLocalRef(SourceInfo info, LocalVariableBinding b) {
      JVariable variable = curMethod.locals.get(b);
      assert variable != null;
      if (variable instanceof JLocal) {
        return new JLocalRef(info, (JLocal) variable);
      } else {
        return new JParameterRef(info, (JParameter) variable);
      }
    }

    private JThisRef makeThisRef(SourceInfo info) {
      if (curMethod.method == null || curMethod.method.isStatic()) {
        return null;
      }
      assert !(curMethod.method.isAbstract() || curMethod.method.isNative());
      JThis jThis = curMethod.method.getThis();
      assert jThis != null;
      return new JThisRef(info, jThis);
    }

    private JExpression makeThisReference(SourceInfo info, ReferenceBinding targetType,
        boolean exactMatch, BlockScope scope) {
      targetType = (ReferenceBinding) targetType.erasure();
      Object[] path = scope.getEmulationPath(targetType, exactMatch, false);
      assert path != null : "No emulation path.";
      if (path == BlockScope.EmulationPathToImplicitThis) {
        return makeThisRef(info);
      }
      JExpression ref;
      ReferenceBinding type;
      if (curMethod.scope.isInsideInitializer() && path[0] instanceof SyntheticArgumentBinding) {
        SyntheticArgumentBinding b = (SyntheticArgumentBinding) path[0];
        JField field = typeMap.get(b.matchingField);
        assert field != null;
        ref = makeInstanceFieldRef(info, field);
        type = (ReferenceBinding) b.type.erasure();
      } else if (path[0] instanceof SyntheticArgumentBinding) {
        SyntheticArgumentBinding b = (SyntheticArgumentBinding) path[0];
        JParameter param = (JParameter) curMethod.locals.get(b);
        assert param != null;
        ref = new JParameterRef(info, param);
        type = (ReferenceBinding) b.type.erasure();
      } else if (path[0] instanceof FieldBinding) {
        FieldBinding b = (FieldBinding) path[0];
        JField field = getTypeMap().get(b);
        assert field != null;
        ref = makeInstanceFieldRef(info, field);
        type = (ReferenceBinding) b.type.erasure();
      } else {
        throw new AssertionError("Unknown emulation path.");
      }
      for (int i = 1; i < path.length; ++i) {
        SyntheticMethodBinding b = (SyntheticMethodBinding) path[i];
        assert type == b.declaringClass.erasure();
        FieldBinding fieldBinding = b.targetReadField;
        JField field = getTypeMap().get(fieldBinding);
        assert field != null;
        ref = new JFieldRef(info, ref, field.getId(), field.getEnclosingType());
        type = (ReferenceBinding) fieldBinding.type.erasure();
      }
      return ref;
    }

    private JExpression maybeCast(JType expected, JExpression expression) {
      if (!expected.equals(expression.getType())) {
        // Must be a generic; insert a cast operation.
        JReferenceType toType = (JReferenceType) expected;
        return new JDynamicCastOperation(expression.getSourceInfo(), toType, expression);
      } else {
        return expression;
      }
    }

    private JNode pop() {
      return nodeStack.remove(nodeStack.size() - 1);
    }

    private List<JExpression> popCallArgs(SourceInfo info, Expression[] jdtArgs,
        MethodBinding binding) {
      List<JExpression> args = pop(jdtArgs);
      if (!binding.isVarargs()) {
        return args;
      }

      // Handle the odd var-arg case.
      if (jdtArgs == null) {
        // Get writable collection (args is currently Collections.emptyList()).
        args = new ArrayList<JExpression>(1);
      }

      TypeBinding[] params = binding.parameters;
      int varArg = params.length - 1;

      // See if there's a single varArg which is already an array.
      if (args.size() == params.length) {
        assert jdtArgs != null;
        if (jdtArgs[varArg].resolvedType.isCompatibleWith(params[varArg])) {
          // Already the correct array type.
          return args;
        }
      }

      // Need to synthesize an appropriately-typed array.
      List<JExpression> tail = args.subList(varArg, args.size());
      ArrayList<JExpression> initializers = new ArrayList<JExpression>(tail);
      tail.clear();
      JArrayType lastParamType = (JArrayType) getTypeMap().get(params[varArg]);
      JNewArray newArray = JNewArray.createWithInits(info, lastParamType, initializers);
      args.add(newArray);
      return args;
    }

    private List<? extends JNode> popList(int count) {
      List<JNode> tail = nodeStack.subList(nodeStack.size() - count, nodeStack.size());
      // Make a copy.
      List<JNode> result = new ArrayList<JNode>(tail);
      // Causes the tail to be removed.
      tail.clear();
      return result;
    }

    private void popMethodInfo() {
      curMethod = methodStack.pop();
    }

    private void processEnumType(JDefinedEnum type) {
      JField valuesField = createEnumValuesField(type);

      {
        JMethod valueOfMethod = type.getMethod(VALUE_OF_STRING, type, javaLangString);
        assert VALUE_OF_STRING.equals(valueOfMethod.getName());
        writeEnumValueOfMethod(type, valueOfMethod);
      }
      {
        JMethod valuesMethod = type.getMethod(VALUES_STRING,
            getTypeMap().get("[" + Jack.getLookupFormatter().getName(type)));
        assert VALUES_STRING.equals(valuesMethod.getName());
        writeEnumValuesMethod(type, valuesMethod, valuesField);
      }
    }

    private void processNativeMethod() {
      JMethod method = curMethod.method;
      SourceInfo info = method.getSourceInfo();
      JNativeMethodBody body = new JNativeMethodBody(info);
      method.setBody(body);
      body.updateParents(method);
    }

    private void processSuperCallLocalArgs(ReferenceBinding superClass, JMethodCall call) {
      if (superClass.syntheticOuterLocalVariables() != null) {
        for (SyntheticArgumentBinding arg : superClass.syntheticOuterLocalVariables()) {
          // TODO(gwt): use emulation path here.
          // Got to be one of my params
          JType varType = getTypeMap().get(arg.type);
          String varName = ReferenceMapper.intern(arg.name);
          JParameter param = null;
          for (JParameter paramIt : curMethod.method.getParams()) {
            if (varType.equals(paramIt.getType()) && varName.equals(paramIt.getName())) {
              param = paramIt;
            }
          }
          assert param != null : "Could not find matching local arg for explicit super ctor call.";
          call.addArg(new JParameterRef(call.getSourceInfo(), param));
        }
      }
    }

    private void processSuperCallThisArgs(ReferenceBinding superClass, JMethodCall call,
        JExpression qualifier, Expression qualification) {
      if (superClass.syntheticEnclosingInstanceTypes() != null) {
        for (ReferenceBinding targetType : superClass.syntheticEnclosingInstanceTypes()) {
          if (qualification != null && superClass.enclosingType() == targetType) {
            assert qualification.resolvedType.erasure().isCompatibleWith(targetType);
            call.addArg(qualifier);
          } else {
            call.addArg(makeThisReference(call.getSourceInfo(), targetType, false,
                curMethod.scope));
          }
        }
      }
    }

    private void processThisCallLocalArgs(ReferenceBinding binding, JMethodCall call) {
      if (binding.syntheticOuterLocalVariables() != null) {
        for (SyntheticArgumentBinding arg : binding.syntheticOuterLocalVariables()) {
          JParameter param = (JParameter) curMethod.locals.get(arg);
          assert param != null;
          call.addArg(new JParameterRef(call.getSourceInfo(), param));
        }
      }
    }

    private void processThisCallThisArgs(ReferenceBinding binding, JMethodCall call) {
      if (binding.syntheticEnclosingInstanceTypes() != null) {
        Iterator<JParameter> paramIt = curMethod.method.getParams().iterator();
        if (getEnumSuperClass(curClass.classType) != null) {
          // Skip past the enum args.
          paramIt.next();
          paramIt.next();
        }
        for (int i = 0; i < binding.syntheticEnclosingInstanceTypes().length; i++) {
          JParameter param = paramIt.next();
          call.addArg(new JParameterRef(call.getSourceInfo(), param));
        }
      }
    }

    private void push(JNode node) {
      nodeStack.add(node);
    }

    private void pushBinaryOp(Expression x, JBinaryOperator op, Expression lhs, Expression rhs) {
      try {
        SourceInfo info = makeSourceInfo(x);
        JExpression exprArg2 = pop(rhs);
        JExpression exprArg1 = pop(lhs);
        JBinaryOperation binary;
        if (op == JBinaryOperator.CONCAT) {
          binary = new JConcatOperation(info, javaLangString, exprArg1, exprArg2);
        } else {
          binary = JBinaryOperation.create(info, op, exprArg1, exprArg2);
        }
          push(binary);
      } catch (Throwable e) {
        throw translateException(x, e);
      }
    }

    private void pushInitializerMethodInfo(FieldDeclaration x, MethodScope scope) {
      JMethod initMeth;
      if (x.isStatic()) {
        initMeth =
            curClass.type.getMethod(NamingTools.STATIC_INIT_NAME,
                JPrimitiveTypeEnum.VOID.getType());
      } else {
        initMeth = curClass.type.getMethod(INIT_METHOD_NAME, JPrimitiveTypeEnum.VOID.getType());
      }
      pushMethodInfo(new MethodInfo(initMeth, (JMethodBody) initMeth.getBody(), scope));
    }

    private void pushMethodInfo(MethodInfo newInfo) {
      methodStack.push(curMethod);
      curMethod = newInfo;
    }

    @Nonnull
    private JMethod getGetClassMethod() {
      if (getClassMethod == null) {
        ReferenceBinding refBinding = lookupEnvironment.getType(LookupEnvironment.JAVA_LANG_OBJECT);
        for (MethodBinding method : refBinding.methods()) {
          char[] methodSig = method.signature();
          if (new String(method.constantPoolName()).equals("getClass")
              && new String(methodSig).equals("()" + CommonTypes.JAVA_LANG_CLASS)) {
            return getTypeMap().get(method);
          }
        }

      }
      throw new AssertionError();
    }

    private void pushNewExpression(SourceInfo info, AllocationExpression x, Expression qualifier,
        List<JExpression> arguments, BlockScope scope) {
      TypeBinding typeBinding = x.resolvedType;
      if (typeBinding.constantPoolName() == null) {
        /*
         * Weird case: if JDT determines that this local class is totally
         * uninstantiable, it won't bother allocating a local name.
         */
        push(new JNullLiteral(SourceInfo.UNKNOWN));
        return;
      }
      assert typeBinding.isClass() || typeBinding.isEnum();

      MethodBinding b = x.binding;
      assert b.isConstructor();
      JConstructor ctor = (JConstructor) getTypeMap().get(b);
      JMethodCall call = new JNewInstance(info, ctor.getEnclosingType(), ctor.getMethodId());
      call.addMarker(new ResolutionTargetMarker(ctor));
      JExpression qualExpr = pop(qualifier);

      // Enums: hidden arguments for the name and id.
      if (x.enumConstant != null) {
        call.addArgs(getStringLiteral(info, x.enumConstant.name),
            new JIntLiteral(info, x.enumConstant.binding.original().id));
      }

      // Synthetic args for inner classes
      ReferenceBinding targetBinding = (ReferenceBinding) b.declaringClass.erasure();
      boolean isNested = isNested(targetBinding);

      ReferenceBinding checkedTargetType =
          targetBinding.isAnonymousType() ? (ReferenceBinding) targetBinding.superclass()
              .erasure() : targetBinding;

      if (qualifier != null
          && (!isNested || checkedTargetType.isStatic())) {
        // If the class is not an inner class or is declared in a static context,
        // a compile-time error occurs
        curClass.scope.problemReporter().unnecessaryEnclosingInstanceSpecification(qualifier,
            checkedTargetType);
        return;
      }

      if (isNested) {
        // Synthetic this args for inner classes
        if (targetBinding.syntheticEnclosingInstanceTypes() != null) {
          ReferenceBinding targetEnclosingType = checkedTargetType.enclosingType();
          for (ReferenceBinding argType : targetBinding.syntheticEnclosingInstanceTypes()) {
            argType = (ReferenceBinding) argType.erasure();
            if (qualifier != null && argType == targetEnclosingType) {
              // If the constructor has a qualifier, we have to check for a null pointer
              // d.new A() => new A((tmp = d, tmp.getClass(), tmp));
              List<JExpression> exprs = new ArrayList<JExpression>();
              SourceInfo sourceInfo = qualExpr.getSourceInfo();
              JLocal tmp = new JLocal(sourceInfo,
                  ".newInstanceQualifier" + newInstanceQualifierSuffix++, qualExpr.getType(),
                  JModifier.FINAL | JModifier.SYNTHETIC, curMethod.body);
              JAsgOperation asg = new JAsgOperation(sourceInfo, new JLocalRef(sourceInfo, tmp),
                  new CloneExpressionVisitor().cloneExpression(qualExpr));
              exprs.add(asg);
              curMethod.body.addLocal(tmp);

              JMethodCall getClassCall = makeMethodCall(sourceInfo, new JLocalRef(sourceInfo, tmp),
                  javaLangObject, getGetClassMethod());
              exprs.add(getClassCall);

              exprs.add(new JLocalRef(sourceInfo, tmp));

              JMultiExpression multiExpr = new JMultiExpression(info, exprs);
              call.addArg(multiExpr);
            } else {
              JExpression thisRef = makeThisReference(info, argType, false, scope);
              call.addArg(thisRef);
              Object[] emulationPath = scope.getEmulationPath(
                  argType,
                  false /* onlyExactMatch */,
                  true /* denyEnclosingArgInConstructorCall */);
              if (emulationPath == BlockScope.NoEnclosingInstanceInConstructorCall) {
                scope.problemReporter().noSuchEnclosingInstance(checkedTargetType,
                    x.concreteStatement(), true);
              }
            }
          }
        }
      }

      // Plain old regular user arguments
      call.addArgs(arguments);

      // Synthetic args for inner classes
      if (isNested) {
        // Synthetic locals for local classes
        if (targetBinding.syntheticOuterLocalVariables() != null) {
          for (SyntheticArgumentBinding arg : targetBinding.syntheticOuterLocalVariables()) {
            LocalVariableBinding targetVariable = arg.actualOuterLocalVariable;
            VariableBinding[] path = scope.getEmulationPath(targetVariable);
            assert path.length == 1;
            if (curMethod.scope.isInsideInitializer()
                && path[0] instanceof SyntheticArgumentBinding) {
              SyntheticArgumentBinding sb = (SyntheticArgumentBinding) path[0];
              JField field;
              if (sb.matchingField == null) {
                field = curClass.syntheticArgToFields.get(sb);
              } else {
                field = typeMap.get(sb.matchingField);
              }
              assert field != null;
              call.addArg(makeInstanceFieldRef(info, field));
            } else if (path[0] instanceof LocalVariableBinding) {
              JExpression localRef = makeLocalRef(info, (LocalVariableBinding) path[0]);
              call.addArg(localRef);
            } else if (path[0] instanceof FieldBinding) {
              JField field = getTypeMap().get((FieldBinding) path[0]);
              assert field != null;
              call.addArg(makeInstanceFieldRef(info, field));
            } else {
              throw new AssertionError("Unknown emulation path.");
            }
          }
        }
      }

      push(call);
    }

    /**
     * Don't process unreachable statements, because JDT doesn't always fully
     * resolve them, which can crash us.
     */
    private Statement[] reduceToReachable(Statement[] statements) {
      if (statements == null) {
        return null;
      }
      int reachableCount = 0;
      for (Statement statement : statements) {
        if ((statement.bits & ASTNode.IsReachable) != 0) {
          ++reachableCount;
        }
      }
      if (reachableCount == statements.length) {
        return statements;
      }
      Statement[] newStatments = new Statement[reachableCount];
      int index = 0;
      for (Statement statement : statements) {
        if ((statement.bits & ASTNode.IsReachable) != 0) {
          newStatments[index++] = statement;
        }
      }
      return newStatments;
    }

    private JExpression resolveNameReference(NameReference x, BlockScope scope) {
      SourceInfo info = makeSourceInfo(x);

      if (x.constant != Constant.NotAConstant) {
        if (ThreadConfig.get(JackIncremental.GENERATE_COMPILER_STATE).booleanValue()) {
          if (x.binding instanceof FieldBinding) {
            FieldBinding b = ((FieldBinding) x.binding).original();
            JField field = getTypeMap().get(b);
            JackIncremental.getCompilerState().addCstUsage(info.getFileName(),
                field.getSourceInfo().getFileName());
          }
        }

        return getConstant(info, x.constant);
      }

      Binding binding = x.binding;
      JExpression result = null;
      if (binding instanceof LocalVariableBinding) {
        LocalVariableBinding b = (LocalVariableBinding) binding;
        if ((x.bits & ASTNode.DepthMASK) != 0) {
          VariableBinding[] path = scope.getEmulationPath(b);
          if (path == null) {
            /*
             * Don't like this, but in rare cases (e.g. the variable is only
             * ever used as an unnecessary qualifier) JDT provides no emulation
             * to the desired variable.
             */
            // throw new InternalCompilerException("No emulation path.");
            return null;
          }
          assert path.length == 1;
          if (curMethod.scope.isInsideInitializer()
              && path[0] instanceof SyntheticArgumentBinding) {
            SyntheticArgumentBinding sb = (SyntheticArgumentBinding) path[0];
            JField field = null;
            if (sb.matchingField == null) {
              field = curClass.syntheticArgToFields.get(sb);
            } else {
              field = typeMap.get(sb.matchingField);
            }
            assert field != null;
            result = makeInstanceFieldRef(info, field);
          } else if (path[0] instanceof LocalVariableBinding) {
            result = makeLocalRef(info, (LocalVariableBinding) path[0]);
          } else if (path[0] instanceof FieldBinding) {
            FieldBinding fb = (FieldBinding) path[0];
            assert curClass.typeDecl.binding.isCompatibleWith(x.actualReceiverType.erasure());
            JField field = getTypeMap().get(fb);
            assert field != null;
            result = makeInstanceFieldRef(info, field);
          } else {
            throw new AssertionError("Unknown emulation path.");
          }
        } else {
          result = makeLocalRef(info, b);
        }
      } else if (binding instanceof FieldBinding) {
        FieldBinding b = ((FieldBinding) x.binding).original();
        JField field = getTypeMap().get(b);
        assert field != null;
        JExpression thisRef = null;
        if (!b.isStatic()) {
          if ((x.bits & ASTNode.DepthMASK) != 0) {
            // The field is accessed through outer this
            ReferenceBinding targetType =
                scope.enclosingSourceType().enclosingTypeAt(
                    (x.bits & ASTNode.DepthMASK) >> ASTNode.DepthSHIFT);
            thisRef = makeThisReference(info, targetType, true /* exactMatch */, scope);
          } else {
            thisRef = makeThisRef(info);
          }
        }
        result = new JFieldRef(info, thisRef, field.getId(),
            (JClassOrInterface) getTypeMap().get(x.actualReceiverType));
     } else {
        return null;
      }
      assert result != null;
      return result;
    }

    private JExpression simplify(JExpression result, Expression x) {
      if (x.constant != null && x.constant != Constant.NotAConstant) {
        // Prefer JDT-computed constant value to the actual written expression.
        result = getConstant(result.getSourceInfo(), x.constant);
      } else if (x instanceof FieldReference) {
        FieldBinding binding = ((FieldReference) x).binding;
        Constant constant = binding.constant();
        if (constant != Constant.NotAConstant) {
          // Prefer JDT-computed constant value to the actual written expression.
          JLiteral cst = getConstant(result.getSourceInfo(), constant);

          if (!(((FieldReference) x).receiver instanceof ThisReference)
              && !binding.isStatic()) {
              return generateGetClassFollowedByConstant(result, cst);
          }

          result = cst;
        }
      } else if (x instanceof QualifiedNameReference) {
        FieldBinding[] otherBindings = ((QualifiedNameReference) x).otherBindings;
        if (otherBindings != null && otherBindings.length != 0) {
          FieldBinding lastBinding = otherBindings[otherBindings.length - 1];
          Constant constant = lastBinding.constant();
          if (constant != Constant.NotAConstant) {
            JLiteral cst = getConstant(result.getSourceInfo(), constant);

            if (!lastBinding.isStatic()) {
              return generateGetClassFollowedByConstant(result, cst);
            }

            // Prefer JDT-computed constant value to the actual written expression.
            result = cst;
          }
        }
      }
      return result;
    }

    private JExpression generateGetClassFollowedByConstant(JExpression result, JLiteral cst) {
      assert result instanceof JFieldRef;
      // a.x => (a.getClass(), constant)

      List<JExpression> exprs = new ArrayList<JExpression>();
      SourceInfo sourceInfo = result.getSourceInfo();

      JMethodCall getClassCall = makeMethodCall(
          sourceInfo, ((JFieldRef) result).getInstance(), javaLangObject, getGetClassMethod());
      exprs.add(getClassCall);
      exprs.add(cst);

      return (new JMultiExpression(sourceInfo, exprs));
    }

    private void writeEnumValueOfMethod(JDefinedEnum type, JMethod method) {
      ReferenceBinding enumType = curCud.scope.getJavaLangEnum();
      ReferenceBinding classType = curCud.scope.getJavaLangClass();

      /*
       * return Enum.valueOf(<enum>.class, name);
       */
      {
        SourceInfo info = method.getSourceInfo();

        MethodBinding[] valueOfBindings =
            enumType.getMethods(VALUE_OF);
        assert valueOfBindings.length == 1;
        MethodBinding valueOfBinding = valueOfBindings[0];

        JClassLiteral clazz = new JClassLiteral(info, method.getEnclosingType(),
            (JDefinedClass) getTypeMap().get(classType));
        JParameterRef nameRef = new JParameterRef(info, method.getParams().get(0));
        JMethod jValueOfBinding = getTypeMap().get(valueOfBinding);
        JMethodCall call = makeMethodCall(info, null, jValueOfBinding.getEnclosingType(),
            jValueOfBinding);
        call.addArgs(clazz, nameRef);
        implementMethod(method, new JDynamicCastOperation(info, type, call));
      }
    }

    private void writeEnumValuesMethod(JDefinedEnum type, JMethod method, JField valuesField) {
      // return $VALUES;
      JFieldRef valuesRef = new JFieldRef(method.getSourceInfo(), null, valuesField.getId(), type);
      implementMethod(method, valuesRef);
    }

    private void generateImplicitReturn() {
      curMethod.body.getBlock().addStmt(new JReturnStatement(session.getSourceInfoFactory().create(
          curMethod.method.getSourceInfo().getEndLine(),
          curMethod.method.getSourceInfo().getEndLine(),
          curMethod.method.getSourceInfo().getFileName()), null));
    }
  }

  static class ClassInfo {
    public final JDefinedClass classType;
    public final ClassScope scope;
    // This should be remove, since it keeps a mapping between SyntheticArgumentBinding and useless
    // JField. Nevertheless, until $init exists it must be kept
    public final Map<SyntheticArgumentBinding, JField> syntheticArgToFields =
        new IdentityHashMap<SyntheticArgumentBinding, JField>();
    public final JDefinedClassOrInterface type;
    public final TypeDeclaration typeDecl;

    public ClassInfo(JDefinedClassOrInterface type, TypeDeclaration x) {
      this.type = type;
      this.classType = (type instanceof JDefinedClass) ? (JDefinedClass) type : null;
      this.typeDecl = x;
      this.scope = x.scope;
    }
  }

  static class MethodInfo {
    public final JMethodBody body;
    public final Map<LocalVariableBinding, JVariable> locals =
        new IdentityHashMap<LocalVariableBinding, JVariable>();
    public final JMethod method;
    public final MethodScope scope;

    public MethodInfo(JMethod method, JMethodBody methodBody, MethodScope methodScope) {
      this.method = method;
      this.body = methodBody;
      this.scope = methodScope;
    }
  }

  class AnnotationValueParser extends ASTVisitor {
    @CheckForNull
    private JLiteral parsed = null;

    @Nonnull
    public JLiteral parseLiteral(@Nonnull Expression value, @Nonnull TypeBinding expectedType,
        @Nonnull BlockScope scope) {
      Constant constantValue = value.constant;
      JLiteral result;
      if ((constantValue != null) && (constantValue != Constant.NotAConstant)) {
        int constantTypeId;
        if (expectedType.isArrayType()) {
          ArrayBinding expetectedArrayType = (ArrayBinding) expectedType;
          assert expetectedArrayType.dimensions == 1;
          constantTypeId = expetectedArrayType.leafComponentType.id;
        } else {
          constantTypeId = expectedType.id;
        }
        result = getConstant(makeSourceInfo(value), constantValue, constantTypeId);
      } else {
        parsed = null;
        value.traverse(this, scope);
        assert parsed != null;
        result = parsed;
      }

      if (expectedType.isArrayType() && !(result instanceof JArrayLiteral)) {
        result = new JArrayLiteral(makeSourceInfo(value),
            Collections.singletonList(result));
      }
      return result;
    }

    @Override
    public boolean visit(ArrayInitializer arrayInitializer, BlockScope scope) {
      Expression[] expressions = arrayInitializer.expressions;
      List<JLiteral> values;
      if (expressions != null) {
        values = new ArrayList<JLiteral>(expressions.length);
        for (Expression expression : expressions) {
          JLiteral element;
          TypeBinding componentType = arrayInitializer.binding.elementsType();
          int componentTypeId = componentType.id;
          if (isConstantType(componentTypeId)) {
            element = getConstant(expression, componentTypeId);
          } else {
            element = parseLiteral(expression, componentType, scope);
          }
          values.add(element);
        }
      } else {
        values = Collections.emptyList();
      }
      parsed = new JArrayLiteral(makeSourceInfo(arrayInitializer), values);
      return false;
    }

    @Override
    public boolean visit(MarkerAnnotation annotation, BlockScope scope) {
      visit((Annotation) annotation, scope);
      return false;
    }

    @Override
    public boolean visit(SingleMemberAnnotation annotation, BlockScope scope) {
      visit((Annotation) annotation, scope);
      return false;
    }

    @Override
    public boolean visit(NormalAnnotation annotation, BlockScope scope) {
      visit((Annotation) annotation, scope);
      return false;
    }

    @Override
    public boolean visit(
        QualifiedNameReference nameReference,
        BlockScope scope) {
      visit(nameReference);
      return false;
    }

    @Override
    public boolean visit(
        SingleNameReference nameReference,
        BlockScope scope) {
      visit(nameReference);
      return false;
    }

    @Override
    public boolean visit(ClassLiteralAccess x, BlockScope scope) {
        parsed = new JClassLiteral(makeSourceInfo(x), getTypeMap().get(x.targetType),
            javaLangClass);
        return false;
    }

    protected void visit(@Nonnull Annotation annotation, @Nonnull BlockScope scope) {
      JDefinedAnnotation jAnnotation =
          (JDefinedAnnotation) getTypeMap().get(annotation.resolvedType);
      JAnnotationLiteral literal =
          new JAnnotationLiteral(makeSourceInfo(annotation), jAnnotation.getRetentionPolicy(),
              jAnnotation);

      MemberValuePair[] pairs = annotation.memberValuePairs();
      for (MemberValuePair pair : pairs) {
        JMethodId methodId = getTypeMap().get(pair.binding).getMethodId();
        literal.add(new JNameValuePair(makeSourceInfo(pair), methodId,
            parseLiteral(pair.value, pair.binding.returnType, scope)));
      }

      parsed = literal;
    }

    private void visit(NameReference nameReference) throws AssertionError {
      Binding binding = nameReference.binding;
      if (binding instanceof FieldBinding) {
        JField field = getTypeMap().get((FieldBinding) binding);
        assert field instanceof JEnumField;
        parsed = new JEnumLiteral(makeSourceInfo(nameReference), field.getId());
      } else {
        throw new AssertionError("Not yet supported " + nameReference);
      }
    }
  }

  private static final String ARRAY_LENGTH_FIELD = "length";

  /**
   * Reflective access to {@link ForeachStatement#collectionElementType}.
   */
  @SuppressWarnings("javadoc")
  private static final Field collectionElementTypeField;

  private static final char[] HAS_NEXT = "hasNext".toCharArray();
  private static final char[] ITERATOR = "iterator".toCharArray();
  private static final char[] NEXT = "next".toCharArray();
  private static final TypeBinding[] NO_TYPES = new TypeBinding[0];
  private static final String VALUE_OF_STRING = "valueOf";
  private static final String INIT_METHOD_NAME = "$init";
  private static final char[] VALUE_OF = VALUE_OF_STRING.toCharArray();
  private static final String VALUES_STRING = "values";
  private static final char[] VALUES = VALUES_STRING.toCharArray();

  static {
    InternalCompilerException.preload();
    try {
      collectionElementTypeField = ForeachStatement.class.getDeclaredField("collectionElementType");
      collectionElementTypeField.setAccessible(true);
    } catch (Exception e) {
      throw new RuntimeException(
          "Unexpectedly unable to access ForeachStatement.collectionElementType via reflection", e);
    }
  }

  static String slashify(char[][] name) {
    StringBuffer result = new StringBuffer();
    for (int i = 0; i < name.length; ++i) {
      if (i > 0) {
        result.append('/');
      }

      result.append(name[i]);
    }
    return result.toString();
  }

  static boolean isNested(ReferenceBinding binding) {
    return binding.isNestedType() && !binding.isStatic();
  }

  /**
   * Returns <code>true</code> if JDT optimized the condition to
   * <code>false</code>.
   */
  private static boolean isOptimizedFalse(Expression condition) {
    if (condition != null) {
      Constant cst = condition.optimizedBooleanConstant();
      if (cst != Constant.NotAConstant) {
        if (cst.booleanValue() == false) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns <code>true</code> if JDT optimized the condition to
   * <code>true</code>.
   */
  private static boolean isOptimizedTrue(Expression condition) {
    if (condition != null) {
      Constant cst = condition.optimizedBooleanConstant();
      if (cst != Constant.NotAConstant) {
        if (cst.booleanValue()) {
          return true;
        }
      }
    }
    return false;
  }

  CudInfo curCud = null;

  JDefinedClass javaLangClass = null;

  JDefinedClass javaLangObject = null;

  JDefinedClass javaLangString = null;

  @CheckForNull
  private JMethod getClassMethod;

  @Nonnull
  private final ReferenceMapper typeMap;

  private final AstVisitor astVisitor = new AstVisitor();

  private final AnnotationValueParser annotationParser = new AnnotationValueParser();

  private List<JDefinedClassOrInterface> newTypes;

  @Nonnull
  private final LookupEnvironment lookupEnvironment;

  @Nonnull
  private final JSession session;

  public JackIrBuilder(@Nonnull LookupEnvironment lookupEnvironment,
      @Nonnull JSession session) {
    this.lookupEnvironment = lookupEnvironment;
    typeMap =
        new ReferenceMapper(session.getLookup(), lookupEnvironment, session.getSourceInfoFactory());
    this.session = session;
  }

  /**
   * @return the typeMap
   */
  @Nonnull
  public ReferenceMapper getTypeMap() {
    return typeMap;
  }

  public List<JDefinedClassOrInterface> process(CompilationUnitDeclaration cud) {
    if (cud.types == null) {
      return Collections.emptyList();
    }
    newTypes = new ArrayList<JDefinedClassOrInterface>();
    curCud = new CudInfo(cud);

    for (TypeDeclaration typeDecl : cud.types) {
      if (typeDecl.hasErrors()) {
        return (Collections.emptyList());
      }
    }

    for (TypeDeclaration typeDecl : cud.types) {
      createTypes(typeDecl);
    }

    // Now that types exist, cache Object, String, etc.
    javaLangObject = (JDefinedClass) getTypeMap().get(cud.scope.getJavaLangObject());
    javaLangString = (JDefinedClass) getTypeMap().get(cud.scope.getJavaLangString());
    javaLangClass = (JDefinedClass) getTypeMap().get(cud.scope.getJavaLangClass());

    for (TypeDeclaration typeDecl : cud.types) {
      // Create fields and empty methods.
      createMembers(typeDecl);
    }
    for (TypeDeclaration typeDecl : cud.types) {
      // Build the code.
      typeDecl.traverse(astVisitor, cud.scope);
    }

    List<JDefinedClassOrInterface> result = newTypes;

    ParentSetter parentSetter = new ParentSetter();
    for (JDefinedClassOrInterface classOrInterface : result) {
      for (JMethod method : classOrInterface.getMethods()) {
        // method parents are set but nothing is done below
        method.traverse(parentSetter);
      }
    }

    // Clean up.
    newTypes = null;
    curCud = null;
    javaLangObject = null;
    javaLangString = null;
    javaLangClass = null;
    getClassMethod = null;

    return result;
  }

  @Nonnull
  static SourceInfo makeSourceInfo(@Nonnull CudInfo cuInfo, int start, int end,
      @Nonnull SourceInfoFactory factory) {
    int startLine =
        Util.getLineNumber(start, cuInfo.separatorPositions, 0,
            cuInfo.separatorPositions.length - 1);
    int startCol =
        Util.searchColumnNumber(cuInfo.separatorPositions, startLine, start);
    int endLine =
        Util.getLineNumber(end, cuInfo.separatorPositions, 0,
            cuInfo.separatorPositions.length - 1);
    int endCol =
        Util.searchColumnNumber(cuInfo.separatorPositions, endLine, end);
    return factory.create(startCol, endCol, startLine, endLine, cuInfo.fileName);
  }

  @Nonnull
  SourceInfo makeSourceInfo(int start, int end, @Nonnull SourceInfoFactory factory) {
    return makeSourceInfo(curCud, start, end, factory);
  }

  @Nonnull
  SourceInfo makeSourceInfo(@Nonnull ASTNode x) {
    return makeSourceInfo(x.sourceStart, x.sourceEnd, session.getSourceInfoFactory());
  }

  InternalCompilerException translateException(Throwable e) {
    if (e instanceof VirtualMachineError) {
      // Always rethrow VM errors (an attempt to wrap may fail).
      throw (VirtualMachineError) e;
    }
    InternalCompilerException ice;
    if (e instanceof InternalCompilerException) {
      ice = (InternalCompilerException) e;
    } else {
      ice = new InternalCompilerException("Error constructing Java AST", e);
    }
    return ice;
  }

  InternalCompilerException translateException(ASTNode node, Throwable e) {
    InternalCompilerException ice = translateException(e);
    if (node != null) {
      ice.addNode(node.getClass().getName(), node.toString(), makeSourceInfo(node));
    }
    return ice;
  }

  InternalCompilerException translateException(
      TypeDeclaration typeDeclaration, Throwable e, SourceInfo info) {
    InternalCompilerException ice = translateException(e);
    if (typeDeclaration != null) {
      StringBuffer sb = new StringBuffer();
      typeDeclaration.printHeader(0, sb);
      ice.addNode(typeDeclaration.getClass().getName(), sb.toString(), info);
    }
    return ice;
  }

  @Nonnull
  static JAbstractStringLiteral getStringLiteral(@Nonnull SourceInfo info, @Nonnull char[] chars) {
    return new JStringLiteral(info, ReferenceMapper.intern(chars));
  }

  @Nonnull
  static JAbstractStringLiteral getStringLiteral(@Nonnull SourceInfo info, @Nonnull String string) {
    return new JStringLiteral(info, ReferenceMapper.intern(string));
  }

  private JLiteral getConstant(Expression expression, int componentTypeId) {
    return getConstant(makeSourceInfo(expression), expression.constant, componentTypeId);
  }

  private JLiteral getConstant(SourceInfo info, Constant constant) {
    return getConstant(info, constant, constant.typeID());
  }

  private boolean isConstantType(int typeId) {
    switch (typeId) {
      case TypeIds.T_int:
      case TypeIds.T_byte:
      case TypeIds.T_short:
      case TypeIds.T_char:
      case TypeIds.T_float:
      case TypeIds.T_double:
      case Constant.T_boolean:
      case Constant.T_long:
      case Constant.T_JavaLangString:
      case Constant.T_null:
        return true;
      default:
        return false;
    }
  }

  private JLiteral getConstant(SourceInfo info, Constant constant, int typeId) {
    switch (typeId) {
      case TypeIds.T_int:
        return new JIntLiteral(info, constant.intValue());
      case TypeIds.T_byte:
        return new JByteLiteral(info, constant.byteValue());
      case TypeIds.T_short:
        return new JShortLiteral(info, constant.shortValue());
      case TypeIds.T_char:
        return new JCharLiteral(info, constant.charValue());
      case TypeIds.T_float:
        return new JFloatLiteral(info, constant.floatValue());
      case TypeIds.T_double:
        return new JDoubleLiteral(info, constant.doubleValue());
      case Constant.T_boolean:
        return new JBooleanLiteral(info, constant.booleanValue());
      case Constant.T_long:
        return new JLongLiteral(info, constant.longValue());
      case Constant.T_JavaLangString:
        return getStringLiteral(info, constant.stringValue());
      case Constant.T_null:
        return new JNullLiteral(info);
      default:
        throw new AssertionError("Unknown Constant type: value type " + constant.typeID()
            + " needed type " + typeId);
    }
  }

  private void createField(FieldDeclaration x) {
    if (x instanceof Initializer) {
      return;
    }

    getTypeMap().get(x.binding);
  }

  private void createMembers(TypeDeclaration x) {
    SourceTypeBinding binding = x.binding;
    JDefinedClassOrInterface type = (JDefinedClassOrInterface) getTypeMap().get(binding);
    SourceInfo info = type.getSourceInfo();
    try {
      ((EcjSourceTypeLoader) type.getLoader()).loadFully(type);

      createStaticInitializer(info, type);

      char[] signature = binding.signature();
      char[] genericSignature = binding.genericTypeSignature();
      // Check if the generic signature really contains generic types i.e. is different from the
      // non-generic signature
      if (!CharOperation.equals(signature, genericSignature)) {
        ThisRefTypeInfo thisMarker = new ThisRefTypeInfo(ReferenceMapper.intern(genericSignature));
        type.addMarker(thisMarker);
      }

      if (type instanceof JDefinedClass) {
        createSyntheticMethod(info, INIT_METHOD_NAME, type, JPrimitiveTypeEnum.VOID.getType(),
            JModifier.FINAL | JModifier.PRIVATE);
      }

      if (type instanceof JDefinedEnum) {
        {
          MethodBinding valueOfBinding =
              binding.getExactMethod(VALUE_OF, new TypeBinding[]{x.scope.getJavaLangString()},
                  curCud.scope);
          assert valueOfBinding != null;
          createSyntheticMethodFromBinding(info, valueOfBinding, new String[]{"name"});
        }
        {
          MethodBinding valuesBinding = binding.getExactMethod(VALUES, NO_TYPES, curCud.scope);
          assert valuesBinding != null;
          createSyntheticMethodFromBinding(info, valuesBinding, null);
        }
      }

      if (x.fields != null) {
        for (FieldDeclaration field : x.fields) {
          createField(field);
        }
      }

      if (x.methods != null) {
        for (AbstractMethodDeclaration method : x.methods) {
          createMethod(method);
        }
      }

      if (x.memberTypes != null) {
        for (TypeDeclaration memberType : x.memberTypes) {
          createMembers(memberType);
        }
      }
    } catch (Throwable e) {
      throw translateException(x, e, info);
    }
  }

  private void createMethod(AbstractMethodDeclaration x) {
    if (x instanceof Clinit) {
      return;
    }
    getTypeMap().get(x.binding);
  }

  private JMethod createStaticInitializer(SourceInfo info, JDefinedClassOrInterface enclosingType) {
    int modifier = JModifier.STATIC | JModifier.STATIC_INIT;
    JMethod method =
        new JMethod(info,
            new JMethodId(NamingTools.STATIC_INIT_NAME, MethodKind.STATIC),
            enclosingType,
            JPrimitiveTypeEnum.VOID.getType(),
            modifier);
    method.setBody(new JMethodBody(info, new JBlock(info)));
    enclosingType.addMethod(method);
    method.updateParents(enclosingType);
    return method;
  }

  private JMethod createSyntheticMethod(SourceInfo info, String name,
      JDefinedClassOrInterface enclosingType,
      JType returnType, int modifier) {
    JMethod method =
        new JMethod(info, new JMethodId(name, ReferenceMapper.getMethodKind(modifier)),
            enclosingType, returnType, ReferenceMapper.removeSynchronizedOnBridge(modifier
                | JModifier.SYNTHETIC));
    method.setBody(new JMethodBody(info, new JBlock(info)));
    enclosingType.addMethod(method);
    method.updateParents(enclosingType);
    return method;
  }

  @Nonnull
  private JMethod createSyntheticMethodFromBinding(@Nonnull SourceInfo info,
      @Nonnull MethodBinding binding, @CheckForNull String[] paramNames) {
    JMethod method = getTypeMap().get(binding);
    method.setSourceInfo(info);
    int i = 0;
    for (JParameter param : method.getParams()) {
      param.setSourceInfo(info);
      if (paramNames != null) {
        param.setName(paramNames[i]);
        i++;
      }
    }
    assert method.getEnclosingType().getLoader() instanceof EcjSourceTypeLoader;
    method.setBody(new JMethodBody(info, new JBlock(info)));
    return method;
  }

  private void createTypes(TypeDeclaration x) {
    SourceInfo info = makeSourceInfo(x);
    try {
      JDefinedClassOrInterface type = (JDefinedClassOrInterface) getTypeMap().get(x.binding);
      newTypes.add(type);
      if (x.memberTypes != null) {
        for (TypeDeclaration memberType : x.memberTypes) {
          createTypes(memberType);
        }
      }
    } catch (Throwable e) {
      throw translateException(x, e, info);
    }
  }

  @CheckForNull
  JDefinedEnum getEnumSuperClass(@Nonnull JDefinedClass jClass) {
    if (jClass instanceof JDefinedEnum) {
      return (JDefinedEnum) jClass;
    } else {
      JClass superClass = jClass.getSuperClass();
      if (superClass != null) {
        return getEnumSuperClass((JDefinedClass) superClass);
      }
    }
    return null;
  }

  @Nonnull
  private static JMethodCall makeMethodCall(@Nonnull SourceInfo info,
      @CheckForNull JExpression instance,
      @Nonnull JDefinedClassOrInterface receiverType, @Nonnull JMethod targetMethod) {

    JMethodId methodId = targetMethod.getMethodId();
    assert methodId.getKind() == MethodKind.STATIC || instance != null;
    JMethodCall call = new JMethodCall(info, instance,
        receiverType, methodId, targetMethod.getType(), methodId.canBeVirtual());
    call.addMarker(new ResolutionTargetMarker(targetMethod));
    return call;
  }

  @Nonnull
  private static JMethodCall makeSuperCall(@Nonnull SourceInfo info,
      @CheckForNull JExpression instance,
      @Nonnull JDefinedClassOrInterface receiverType,
      @Nonnull JMethod targetMethod) {

    JMethodCall call = new JMethodCall(info, instance,
        receiverType, targetMethod.getMethodId(), targetMethod.getType(),
        false /* isVirtualDispatch */);
    call.addMarker(new ResolutionTargetMarker(targetMethod));
    return call;
  }

}
