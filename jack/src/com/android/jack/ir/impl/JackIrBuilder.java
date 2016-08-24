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
import com.android.jack.Options;
import com.android.jack.frontend.ParentSetter;
import com.android.jack.ir.JNodeInternalError;
import com.android.jack.ir.ast.Annotable;
import com.android.jack.ir.ast.JAbsentArrayDimension;
import com.android.jack.ir.ast.JAbstractMethodBody;
import com.android.jack.ir.ast.JAbstractStringLiteral;
import com.android.jack.ir.ast.JAnnotation;
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
import com.android.jack.ir.ast.JDefinedAnnotationType;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedEnum;
import com.android.jack.ir.ast.JDefinedInterface;
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
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JLabel;
import com.android.jack.ir.ast.JLabeledStatement;
import com.android.jack.ir.ast.JLambda;
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JLongLiteral;
import com.android.jack.ir.ast.JLtOperation;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JMethodIdRef;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JMultiExpression;
import com.android.jack.ir.ast.JNameValuePair;
import com.android.jack.ir.ast.JNativeMethodBody;
import com.android.jack.ir.ast.JNewArray;
import com.android.jack.ir.ast.JNewInstance;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.ir.ast.JNullType;
import com.android.jack.ir.ast.JNumberLiteral;
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
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.ast.JUnaryOperator;
import com.android.jack.ir.ast.JValueLiteral;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.ir.ast.JWhileStatement;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.ast.Number;
import com.android.jack.ir.ast.marker.ThisRefTypeInfo;
import com.android.jack.ir.formatter.BinaryQualifiedNameFormatter;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.ir.sourceinfo.SourceInfoFactory;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.lookup.JLookupException;
import com.android.jack.lookup.JMethodLookupException;
import com.android.jack.transformations.ast.TypeLegalizer;
import com.android.jack.util.CloneExpressionVisitor;
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
import org.eclipse.jdt.internal.compiler.ast.FunctionalExpression;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.InstanceOfExpression;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.ast.LabeledStatement;
import org.eclipse.jdt.internal.compiler.ast.LambdaExpression;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LongLiteral;
import org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.NameReferenceCaller;
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
import org.eclipse.jdt.internal.compiler.ast.ReferenceExpression;
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
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.BooleanConstant;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.IntersectionTypeBinding18;
import org.eclipse.jdt.internal.compiler.lookup.LocalTypeBinding;
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
import org.eclipse.jdt.internal.compiler.problem.AbortMethod;
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

  private final boolean generateJackLibrary =
      ThreadConfig.get(Options.GENERATE_JACK_LIBRARY).booleanValue();

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

    private void addAnnotations(@CheckForNull Annotation[] annotations,
        @Nonnull Annotable annotable) {
      if (annotations != null) {
        List<JExpression> jannotations = pop(annotations);
        for (JExpression jannotation : jannotations) {
          annotable.addAnnotation((JAnnotation) jannotation);
          jannotation.updateParents((JNode) annotable);
        }
      }
    }

    @Override
    public void endVisit(AllocationExpression x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        List<JExpression> arguments = popCallArgs(info, x.arguments, x.binding);
        pushNewExpression(info, x, null, arguments, scope);
      } catch (JTypeLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(AND_AND_Expression x, BlockScope scope) {
      pushBinaryOp(x, JBinaryOperator.AND);
    }

    @Override
    public void endVisit(AnnotationMethodDeclaration x, ClassScope classScope) {
      addAnnotations(x.annotations, curMethod.method);

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
      } catch (JTypeLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
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

            if (type.getElementType() instanceof JPrimitiveType && expr instanceof JNumberLiteral
                && !expr.getType().isSameType(type.getElementType())) {
              // We have a constant with a different type than array type, change it to the right
              // type
              values.add(changeTypeOfLiteralValue(
                  ((JPrimitiveType) type.getElementType()).getPrimitiveTypeEnum(),
                  (JNumberLiteral) expr));
            } else {
              values.add(expr);
            }
          }
        } else {
          values = Collections.emptyList();
        }

        push(JNewArray.createWithInits(info, type, values));
      } catch (JTypeLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
    }

    @Nonnull
    private JValueLiteral changeTypeOfLiteralValue(@Nonnull JPrimitiveTypeEnum expectedType,
        @Nonnull JNumberLiteral expr) throws AssertionError {
      SourceInfo sourceInfo = expr.getSourceInfo();
      Number number = expr.getNumber();

      switch (expectedType) {
        case BYTE: {
          return (new JByteLiteral(sourceInfo, number.byteValue()));
        }
        case CHAR: {
          return (new JCharLiteral(sourceInfo, number.charValue()));
        }
        case SHORT: {
          return (new JShortLiteral(sourceInfo, number.shortValue()));
        }
        case LONG: {
          return (new JLongLiteral(sourceInfo, number.longValue()));
        }
        case FLOAT: {
          return (new JFloatLiteral(sourceInfo, number.floatValue()));
        }
        case DOUBLE: {
          return (new JDoubleLiteral(sourceInfo, number.doubleValue()));
        }
        case INT: {
          return (new JIntLiteral(sourceInfo, number.intValue()));
        }
        case BOOLEAN:
        case VOID: {
          throw new AssertionError();
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
      } catch (RuntimeException e) {
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
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(Assignment x, BlockScope scope) {
      pushBinaryOp(x, JBinaryOperator.ASG);
    }

    @Override
    public void endVisit(BinaryExpression x, BlockScope scope) {
      try {
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
            if (javaLangString.isSameType(getTypeMap().get(x.resolvedType))) {
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
      } catch (JTypeLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(Block x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        JBlock block = popBlock(info, x.statements);
        push(block);
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(BreakStatement x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        push(new JBreakStatement(info, getOrCreateLabel(info, x.label)));
      } catch (RuntimeException e) {
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
      } catch (JTypeLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(CastExpression x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        JExpression expression = pop(x.expression);
        JDynamicCastOperation castOp = null;
        if (x.resolvedType instanceof IntersectionTypeBinding18) {
          castOp = new JDynamicCastOperation(info, expression,
              getTypeMap().getBounds((IntersectionTypeBinding18) x.resolvedType));
        } else {
          castOp =
              new JDynamicCastOperation(info, expression, getTypeMap().get(x.resolvedType));
        }
        push(castOp);
      } catch (JTypeLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(CharLiteral x, BlockScope scope) {
      try {
        push(new JCharLiteral(makeSourceInfo(x), x.constant.charValue()));
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(ClassLiteralAccess x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        JType type = getTypeMap().get(x.targetType);
        push(new JClassLiteral(info, type, javaLangClass));
      } catch (JTypeLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(CompoundAssignment x, BlockScope scope) {
      try {
        JBinaryOperator op;
        switch (x.operator) {
          case OperatorIds.PLUS:
            if (javaLangString.isSameType(getTypeMap().get(x.resolvedType))) {
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
      } catch (JTypeLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(ConditionalExpression x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);

        boolean optimizedTrue = isOptimizedTrue(x.condition);
        boolean optimizedFalse = isOptimizedFalse(x.condition);
        if (optimizedTrue || optimizedFalse) {
          // One branch of (condition ? valueIfTrue : valueIfFalse) is dead code,
          // drop the dead code by keeping only (condition, value).
          // The condition must be kept even if its value is unused because it may have side effect
          JExpression value;
          JExpression condition;
          if (optimizedTrue) {
            assert !optimizedFalse;
            value = pop(x.valueIfTrue);
            condition = pop(x.condition);
          } else {
            value = pop(x.valueIfFalse);
            condition = pop(x.condition);
          }
          push(new JMultiExpression(info,
             condition,  generateImplicitConversion(x.implicitConversion, value)));
        } else {
          JExpression valueIfFalse = pop(x.valueIfFalse);
          JExpression valueIfTrue = pop(x.valueIfTrue);
          JExpression condition = pop(x.condition);
          push(new JConditionalExpression(info, condition, valueIfTrue, valueIfFalse));
        }

      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
    }

    @Nonnull
    private JExpression generateImplicitConversion(int implicitConversionCode,
        @Nonnull JExpression expr) {

      if (implicitConversionCode == TypeIds.T_undefined) {
        return expr;
      }

      JExpression convertedExpression = expr;

      if ((implicitConversionCode & TypeIds.UNBOXING) != 0) {
        final int typeId = implicitConversionCode & TypeIds.COMPILE_TYPE_MASK;
        convertedExpression =
            TypeLegalizer.unbox(convertedExpression, getJType(typeId).getWrapperType());
      }

      final int typeId = (implicitConversionCode & TypeIds.IMPLICIT_CONVERSION_MASK) >> 4;
      JPrimitiveType primitiveType = getJType(typeId);
      convertedExpression = new JDynamicCastOperation(convertedExpression.getSourceInfo(),
          convertedExpression, primitiveType);

      if ((implicitConversionCode & TypeIds.BOXING) != 0) {
        convertedExpression = TypeLegalizer.box(convertedExpression,
            primitiveType.getWrapperType());
      }

      return convertedExpression;
    }

    @Nonnull
    private JPrimitiveType getJType(final int typeId) throws AssertionError {
      JPrimitiveType type = null;

      switch (typeId) {
        case TypeIds.T_byte:
          type = JPrimitiveTypeEnum.BYTE.getType();
          break;
        case TypeIds.T_short:
          type = JPrimitiveTypeEnum.SHORT.getType();
          break;
        case TypeIds.T_char:
          type = JPrimitiveTypeEnum.CHAR.getType();
          break;
        case TypeIds.T_int:
          type = JPrimitiveTypeEnum.INT.getType();
          break;
        case TypeIds.T_long:
          type = JPrimitiveTypeEnum.LONG.getType();
          break;
        case TypeIds.T_float:
          type = JPrimitiveTypeEnum.FLOAT.getType();
          break;
        case TypeIds.T_double:
          type = JPrimitiveTypeEnum.DOUBLE.getType();
          break;
        case TypeIds.T_boolean:
          type = JPrimitiveTypeEnum.BOOLEAN.getType();
          break;
        default: {
          throw new AssertionError();
        }
      }

      return type;
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

        addAnnotations(x.annotations, curMethod.method);

        popMethodInfo();
      } catch (JLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(ContinueStatement x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        push(new JContinueStatement(info, getOrCreateLabel(info, x.label)));
      } catch (RuntimeException e) {
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
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(DoubleLiteral x, BlockScope scope) {
      try {
        push(new JDoubleLiteral(makeSourceInfo(x), x.constant.doubleValue()));
      } catch (RuntimeException e) {
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
          JParameterRef enumNameRef = curMethod.method.getParams().get(0).makeRef(info);
          call.addArg(enumNameRef);
          JParameterRef enumOrdinalRef = curMethod.method.getParams().get(1).makeRef(info);
          call.addArg(enumOrdinalRef);
        }

        if (x.isSuperAccess()) {
          JExpression qualifier = pop(x.qualification);

          if (qualifier != null) {
            // JLS 8.8.7.1. Explicit Constructor Invocations
            // If the explicit constructor has a qualifier, we have to check for a null pointer
            // d.super(...) => new A((tmp = d, tmp.getClass(), super(...)));
            JLocal tmp =
                new JLocal(info, ".superInstanceQualifier" + superInstanceQualifierSuffix++,
                    qualifier.getType(), JModifier.FINAL | JModifier.SYNTHETIC, curMethod.body);
            JAsgOperation asg = new JAsgOperation(info, tmp.makeRef(info), qualifier);
            curMethod.body.addLocal(tmp);

            JMethodCall getClassCall =
                makeMethodCall(info, tmp.makeRef(info), javaLangObject, getGetClassMethod());

            qualifier = tmp.makeRef(info);

            JMultiExpression multiExpr = new JMultiExpression(info,
                asg, getClassCall, call);
            push(multiExpr.makeStatement());
          } else {
            push(call.makeStatement());
          }

          ReferenceBinding superClass = x.binding.declaringClass;
          boolean nestedSuper = isNested(superClass);
          if (nestedSuper) {
            processSuperCallThisArgs(superClass, call, qualifier, x);
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
      } catch (JTypeLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
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

        addAnnotations(x.annotations, getTypeMap().get(x.binding));

        popMethodInfo();
      } catch (JTypeLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
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
      } catch (JTypeLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(FloatLiteral x, BlockScope scope) {
      try {
        push(new JFloatLiteral(makeSourceInfo(x), x.constant.floatValue()));
      } catch (RuntimeException e) {
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
        JLocal elementVar = (JLocal) curMethod.getJVariable(x.elementVariable.binding);
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
          initializers.add(
              makeAssignStatement(info, maxVar, new JArrayLength(info, arrayVar.makeRef(info))));

          // i$index < i$max
          JExpression condition =
              new JLtOperation(info, indexVar.makeRef(info), maxVar.makeRef(info));

          // ++i$index
          List<JExpressionStatement> increments = new ArrayList<JExpressionStatement>(1);
          increments.add(new JPrefixIncOperation(info, indexVar.makeRef(info)).makeStatement());

          // T elementVar = i$array[i$index];
          elementDecl = new JAsgOperation(info, elementVar.makeRef(info),
              new JArrayRef(info, arrayVar.makeRef(info), indexVar.makeRef(info))).makeStatement();
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
              makeMethodCall(info, iteratorVar.makeRef(info), jIterator, getTypeMap().get(hasNext));

          // i$iterator.next();
          JExpression callToNext =
              makeMethodCall(info, iteratorVar.makeRef(info), jIterator, getTypeMap().get(next));

          // Perform any implicit reference type casts (due to generics).
          // Note this occurs before potential unboxing.
          if (!elementVar.getType().isSameType(javaLangObject)) {
            TypeBinding collectionElementType = (TypeBinding) collectionElementTypeField.get(x);
            JType toType = getTypeMap().get(collectionElementType);
            assert (toType instanceof JReferenceType);
            callToNext = maybeCast(toType, callToNext);
          }
          // T elementVar = (T) i$iterator.next();
          elementDecl =
              new JAsgOperation(info, elementVar.makeRef(info), callToNext).makeStatement();
          body.addStmt(0, elementDecl);

          result =
              new JForStatement(info, initializers, condition, Collections
                  .<JExpressionStatement> emptyList(), body);
        }

        push(result);
      } catch (JTypeLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
        throw translateException(x, e);
      } catch (IllegalAccessException e) {
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
      } catch (RuntimeException e) {
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
      } catch (RuntimeException e) {
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
      } catch (RuntimeException e) {
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
      } catch (JTypeLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(IntLiteral x, BlockScope scope) {
      try {
        push(new JIntLiteral(makeSourceInfo(x), x.constant.intValue()));
      } catch (RuntimeException e) {
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
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
    }


    /**
     * Create {@JMethod} that will be used by a {@link JLambda} do implement a method reference.
     */
    @Nonnull
    private JMethod createLambdaMethodForMthRef(@Nonnull MethodBinding lambdaMethodBinding,
        @Nonnull ComputeShouldCapture csc) throws JTypeLookupException {
      SourceInfo info = SourceInfo.UNKNOWN;

      JType returnType = getTypeMap().get(lambdaMethodBinding.returnType);

      JMethodId methodId = new JMethodId(
          new JMethodIdWide(
              ReferenceMapper.intern(NamingTools.getNonSourceConflictingName(
                  BinaryQualifiedNameFormatter.getFormatter().getName(curClass.type) + "-mthref-"
                      + (curClass.mthRefCount++))),
              csc.shouldCaptureInstance ? MethodKind.INSTANCE_VIRTUAL : MethodKind.STATIC),
          returnType);

      // All lambda implementation methods are flag 'package' (JModifier.DEFAULT) into Jack to avoid
      // useless inner accessors.
      JMethod lambdaMethod =
          new JMethod(info, methodId, curClass.type,
              (csc.shouldCaptureInstance ? JModifier.DEFAULT : JModifier.STATIC)
                  | JModifier.SYNTHETIC | JModifier.LAMBDA_METHOD
                  | (curClass.type instanceof JInterface ? JModifier.PUBLIC : JModifier.DEFAULT));

      int pIndex = 0;
      for (TypeBinding argType : lambdaMethodBinding.parameters) {
        getTypeMap().createParameter(info, lambdaMethod, "arg" + (pIndex++), argType,
            JModifier.SYNTHETIC);
      }

      JMethodBody lambdaMethodBody = new JMethodBody(info, new JBlock(info));
      lambdaMethod.setBody(lambdaMethodBody);

      curClass.type.addMethod(lambdaMethod);
      lambdaMethod.updateParents(curClass.type);

      return lambdaMethod;
    }

    @Override
    public boolean visit(@Nonnull ReferenceExpression referenceExpression,
        @Nonnull BlockScope blockScope) {
      ComputeShouldCapture csc = new ComputeShouldCapture();
      referenceExpression.traverse(csc, blockScope);

      SourceInfo sourceInfo = makeSourceInfo(referenceExpression);
      JMethod lambdaMethod = createLambdaMethodForMthRef(referenceExpression.descriptor, csc);
      MethodInfo newMethodInfo =
          new MethodInfo(this, lambdaMethod, (JMethodBody) lambdaMethod.getBody(), curMethod.scope);

      JMethodId methodIdToImplement = getJMethodId(referenceExpression.descriptor.original());
      JBlock lambdaBodyBlock = newMethodInfo.body.getBlock();
      List<JParameter> argsOfLambdaMth = lambdaMethod.getParams();

      JExpression  exprRepresentingLambda =
          new JLambda(sourceInfo, methodIdToImplement,
              new JMethodIdRef(sourceInfo, lambdaMethod.getEnclosingType(), lambdaMethod
                  .getMethodId()),
          (JDefinedInterface) getTypeMap().get(getLambdaType(referenceExpression, blockScope)),
          getInterfaceBounds(referenceExpression, blockScope),
          getJMethodId(referenceExpression.descriptor));

      if (csc.shouldCaptureInstance) {
        ((JLambda) exprRepresentingLambda).addCapturedVariable(makeThisRef(sourceInfo));
      }

      ((JLambda) exprRepresentingLambda).addBridgeMethodIds(getBridges(referenceExpression));

      if (referenceExpression.isMethodReference()) {
        JMethod methodToCall = getTypeMap().get(referenceExpression.binding);

        int firstParamIdx = 0;

        if (!(referenceExpression.lhs instanceof TypeReference)) {
          referenceExpression.lhs.traverse(this, blockScope);
          JExpression lhsExprOutsideLambdaMethod = pop(referenceExpression.lhs);

          if (!csc.shouldCaptureInstance && lhsExprOutsideLambdaMethod != null) {
            JType lhsJType = getTypeMap().get(referenceExpression.lhs.resolvedType);
            String tmpName = "-lambdaCtx";
            JLocal tmp = new JLocal(sourceInfo, tmpName, lhsJType,
                JModifier.FINAL | JModifier.SYNTHETIC, curMethod.body);
            curMethod.body.addLocal(tmp);

            JParameter jParameter = getTypeMap().createParameter(SourceInfo.UNKNOWN, lambdaMethod,
                tmpName, referenceExpression.lhs.resolvedType,
                JModifier.SYNTHETIC | JModifier.FINAL | JModifier.CAPTURED_VARIABLE,
                /* paramIndex= */ 0);

            ((JLambda) exprRepresentingLambda).addCapturedVariable(tmp.makeRef(sourceInfo));

            if (referenceExpression.lhs.localVariableBinding() != null) {
              newMethodInfo.addVariableMapping(referenceExpression.lhs.localVariableBinding(),
                  jParameter);
            }

            JAsgOperation asg =
                new JAsgOperation(sourceInfo, tmp.makeRef(sourceInfo), lhsExprOutsideLambdaMethod);

            // Null pointer exception on lhsExpr must be thrown as required by the JLS 15.3.3
            JMethodCall getClassCall = makeMethodCall(sourceInfo, tmp.makeRef(sourceInfo),
                javaLangObject, getGetClassMethod());

            exprRepresentingLambda = new JMultiExpression(sourceInfo,
                asg, getClassCall, exprRepresentingLambda);
          } else {
            assert lhsExprOutsideLambdaMethod != null || lambdaMethod.isStatic();
          }
        }

        JExpression instanceExpr = null;
        if (!methodToCall.isStatic()) {
          if (!csc.shouldCaptureInstance) {
            instanceExpr = argsOfLambdaMth.get(0).makeRef(sourceInfo);
            firstParamIdx = 1;
          } else {
            pushMethodInfo(newMethodInfo);
            referenceExpression.lhs.traverse(this, blockScope);
            instanceExpr = pop(referenceExpression.lhs);
            popMethodInfo();
          }
        }

        boolean isSuperRef = referenceExpression.lhs instanceof SuperReference
            || referenceExpression.lhs instanceof QualifiedSuperReference;
        boolean isVirtualDispatch = methodToCall.getMethodIdWide().canBeVirtual() && !isSuperRef;

        JMethodCall methodCall =
            new JMethodCall(sourceInfo, instanceExpr, methodToCall.getEnclosingType(),
                methodToCall.getMethodIdWide(), methodToCall.getType(), isVirtualDispatch);

        addArgToMethodCall(referenceExpression, argsOfLambdaMth, methodCall, firstParamIdx);

        JType returnTypeOfLambdaMethod = lambdaMethod.getType();
        if (returnTypeOfLambdaMethod.isSameType(JPrimitiveTypeEnum.VOID.getType())) {
          lambdaBodyBlock.addStmt(methodCall.makeStatement());
          lambdaBodyBlock.addStmt(new JReturnStatement(sourceInfo, null));
        } else {
          lambdaBodyBlock.addStmt(
              new JReturnStatement(sourceInfo, maybeCast(returnTypeOfLambdaMethod, methodCall)));
        }
      } else if (referenceExpression.isArrayConstructorReference()) {
        assert argsOfLambdaMth.size() == 1;

        Expression lhs = referenceExpression.lhs;
        JArrayType arrayType = (JArrayType) getTypeMap().get(lhs.resolvedType);
        List<JExpression> dims = new ArrayList<JExpression>();

        dims.add(argsOfLambdaMth.get(0).makeRef(sourceInfo));
        for (int dim = 0; dim < ((TypeReference) lhs).dimensions() - 1; dim++) {
          dims.add(new JAbsentArrayDimension(SourceInfo.UNKNOWN));
        }

        lambdaBodyBlock.addStmt(new JReturnStatement(sourceInfo,
            JNewArray.createWithDims(sourceInfo, arrayType, dims)));
      } else {
        assert referenceExpression.isConstructorReference();
        constructorMethodReference(referenceExpression, blockScope, newMethodInfo,
            ((JLambda) exprRepresentingLambda));
      }

      push(exprRepresentingLambda);

      return false;
    }

    // Transform method references to constructor into lambda
    // A::new
    // will be transform to:
    // (parameters) -> return new A(parameters);
    // Be careful, lambda method as the following form m(captured var, ..., arg, ...)
    // But constructor as the following form init([this,] arg, ..., captured var, ...)
    private void constructorMethodReference(@Nonnull ReferenceExpression referenceExpression,
        @Nonnull BlockScope blockScope, @Nonnull MethodInfo lambdaMethodInfo,
        @Nonnull JLambda lambda) {
      SourceInfo sourceInfo = makeSourceInfo(referenceExpression);
      JMethod lambdaMethod = lambdaMethodInfo.method;

      JType type = getTypeMap().get(referenceExpression.lhs.resolvedType);
      assert type instanceof JClassOrInterface;

      JMethod constructor = getTypeMap().get(referenceExpression.binding);
      assert constructor instanceof JConstructor;

      JNewInstance newInstance =
          new JNewInstance(sourceInfo, (JClassOrInterface) type, constructor.getMethodIdWide());

      boolean isNestedType = referenceExpression.receiverType.isNestedType();

      List<JExpression> capturedVariables = new ArrayList<JExpression>();
      int captureCount = 0;

      if (isNestedType) {
        // In A::New if A is a nested class that has outer variables, then capture them and add them
        // to the lambda method as parameters
        ReferenceBinding nestedType = (ReferenceBinding) referenceExpression.receiverType;
        if (nestedType.syntheticOuterLocalVariables() != null) {
          for (SyntheticArgumentBinding synthArg : nestedType.syntheticOuterLocalVariables()) {
            VariableBinding[] paths =
                blockScope.getEmulationPath(synthArg.actualOuterLocalVariable);
            assert paths != null;
            assert paths.length == 1;
            JExpression exprPath = generateEmulationPath(sourceInfo, paths);

            JParameter jParameter = getTypeMap().createParameter(SourceInfo.UNKNOWN, lambdaMethod,
                new String(synthArg.actualOuterLocalVariable.name), paths[0].type,
                JModifier.SYNTHETIC | JModifier.FINAL | JModifier.CAPTURED_VARIABLE, captureCount);

            lambdaMethodInfo.addVariableMapping(synthArg.actualOuterLocalVariable, jParameter);

            if (exprPath instanceof JVariableRef) {
              JVariable var = curMethod.getJVariable(synthArg.actualOuterLocalVariable);
              capturedVariables.add(var.makeRef(sourceInfo));
            } else {
              assert exprPath instanceof JFieldRef;
              JField field = ((JFieldRef) exprPath).getFieldId().getField();
              assert field != null;
              JFieldRef fieldRef = makeInstanceFieldRef(sourceInfo, field);
              capturedVariables.add(fieldRef);
            }
            captureCount++;
          }
        }
      }

      pushMethodInfo(lambdaMethodInfo);

      // In A::New if A is a nested class, it requires to pass the outer this to the constructor as
      // the first parameter
      if (isNestedType) {
        ReferenceBinding[] syntheticEnclosingInstanceTypes =
            ((ReferenceBinding) referenceExpression.receiverType).syntheticEnclosingInstanceTypes();
        if (syntheticEnclosingInstanceTypes != null) {
          assert syntheticEnclosingInstanceTypes.length == 1;
          JExpression thisRef = makeThisReference(sourceInfo, syntheticEnclosingInstanceTypes[0],
              false, blockScope, referenceExpression);
          newInstance.addArg(thisRef);
        }
      }

      for (JExpression capturedVar : capturedVariables) {
        lambda.addCapturedVariable(capturedVar);
      }

      addArgToMethodCall(referenceExpression, lambdaMethod.getParams(), newInstance, captureCount);

      // Pass captured variables to the new-instance
      for (JParameter parameter :lambdaMethod.getParams()) {
        if (parameter.isCapturedVariable()) {
          newInstance.addArg(parameter.makeRef(sourceInfo));
        }
      }

      JBlock lambdaMthBodyblock = lambdaMethodInfo.body.getBlock();
      if (lambdaMethod.getType().isSameType(JPrimitiveTypeEnum.VOID.getType())) {
        lambdaMthBodyblock.addStmt(newInstance.makeStatement());
        lambdaMthBodyblock.addStmt(new JReturnStatement(sourceInfo, null));
      } else {
        lambdaMthBodyblock.addStmt(new JReturnStatement(sourceInfo, newInstance));
      }

      popMethodInfo();
    }

    private void addArgToMethodCall(@Nonnull ReferenceExpression referenceExpression,
        @Nonnull List<JParameter> argsOfLambdaMth, @Nonnull JMethodCall mthCall,
        @Nonnegative int firstParameter) {
      SourceInfo sourceInfo = makeSourceInfo(referenceExpression);
      MethodBinding targetMthBinding = referenceExpression.binding;
      int parameterCountTargetMethod = targetMthBinding.parameters.length;
      int regularParameterCount = parameterCountTargetMethod;
      if (targetMthBinding.isVarargs()) {
        regularParameterCount--;
      }

      for (int pIndex = firstParameter; pIndex < firstParameter + regularParameterCount; pIndex++) {
        mthCall.addArg(argsOfLambdaMth.get(pIndex).makeRef(sourceInfo));
      }

      if (targetMthBinding.isVarargs()) {
        boolean needArrayForVarArg = true;
        int countArgsOfLambdaMth = argsOfLambdaMth.size();
        JArrayType varArgType = (JArrayType) getTypeMap()
            .get(targetMthBinding.parameters[targetMthBinding.parameters.length - 1]);

        if (countArgsOfLambdaMth - regularParameterCount == 1) {
          // it remains only one parameter, check if it can be pass directly without create an array
          JType lastArgType = argsOfLambdaMth.get(countArgsOfLambdaMth - 1).getType();
          if (lastArgType instanceof JArrayType) {
            JType lastArgElementType = varArgType.getElementType();
            JType lastParameterElementType = ((JArrayType) lastArgType).getElementType();
            if (!((lastArgElementType instanceof JPrimitiveType
                && !(lastParameterElementType instanceof JPrimitiveType))
                || (!(lastArgElementType instanceof JPrimitiveType)
                    && lastParameterElementType instanceof JPrimitiveType))) {
              needArrayForVarArg = false;
              mthCall.addArg(argsOfLambdaMth.get(countArgsOfLambdaMth - 1).makeRef(sourceInfo));
            }
          }
        }

        if (needArrayForVarArg) {
          ArrayList<JExpression> initializers = new ArrayList<JExpression>();
          for (int pIndex =
              firstParameter + regularParameterCount; pIndex < countArgsOfLambdaMth; pIndex++) {
            initializers.add(argsOfLambdaMth.get(pIndex).makeRef(sourceInfo));
          }
          mthCall.addArg(JNewArray.createWithInits(sourceInfo, varArgType, initializers));
        }
      }
    }

    /**
     * Compute if a {@link ReferenceExpression} or {@link LambdaExpression} should capture instance.
     */
    class ComputeShouldCapture extends ASTVisitor {
      private final Stack<Boolean> shouldCaptureStack = new Stack<Boolean>();

      public boolean shouldCaptureInstance;

      @Override
      public boolean visit(@Nonnull ReferenceExpression referenceExpression,
          @Nonnull BlockScope blockScope) {
        if (referenceExpression.isMethodReference()) {
          boolean isSuperRef = referenceExpression.lhs instanceof SuperReference
              || referenceExpression.lhs instanceof QualifiedSuperReference;
          boolean isThisRef = referenceExpression.lhs instanceof ThisReference
              || referenceExpression.lhs instanceof QualifiedThisReference;
          shouldCaptureInstance = isSuperRef || isThisRef;
        } else if (referenceExpression.isArrayConstructorReference()) {
          shouldCaptureInstance = false;
        } else {
          boolean isNestedType = referenceExpression.receiverType.isNestedType();

          if (isNestedType) {
            ReferenceBinding[] syntheticEnclosingInstanceTypes =
                ((ReferenceBinding) referenceExpression.receiverType)
                    .syntheticEnclosingInstanceTypes();
            if (syntheticEnclosingInstanceTypes != null) {
              shouldCaptureInstance = true;
            }
          }
        }
        return false;
      }

      @Override
      public boolean visit(@Nonnull LambdaExpression lambdaExpression,
          @Nonnull BlockScope blockScope) {
        boolean needToCaptureThis = lambdaExpression.shouldCaptureInstance;

        shouldCaptureStack.push(Boolean.valueOf(needToCaptureThis));

        return !needToCaptureThis;
      }

      @Override
      public void endVisit(LambdaExpression lambdaExpression, BlockScope blockScope) {
        shouldCaptureInstance = shouldCaptureStack.pop().booleanValue();
      }

      @Override
      public boolean visit(@Nonnull AllocationExpression allocationExpression,
          @Nonnull BlockScope scope) {
        MethodBinding b = allocationExpression.binding;
        assert b.isConstructor();
        ReferenceBinding targetBinding = (ReferenceBinding) b.declaringClass.erasure();

        shouldCaptureStack
            .push(Boolean.valueOf(shouldCaptureStack.pop().booleanValue() | (isNested(targetBinding)
                && targetBinding.syntheticEnclosingInstanceTypes() != null)));
        return true;
      }
    }

    @Override
    public boolean visit(LambdaExpression lambdaExpression, BlockScope blockScope) {
      SourceInfo lambdaSourceInfo = makeSourceInfo(lambdaExpression);

      ComputeShouldCapture csc = new ComputeShouldCapture();
      lambdaExpression.traverse(csc, blockScope);

      JMethod lambdaMethod = null;
      try {
        assert lambdaExpression.binding.isPrivate();
        // All lambda implementation methods are flag 'package' into Jack to avoid useless inner
        // accessors. Change flags only for the method creation and restore it after.
        lambdaExpression.binding.modifiers &= ~ClassFileConstants.AccPrivate;
        if (!csc.shouldCaptureInstance) {
          // No need to capture instance, thus force lambda method to be static, modifier into ECJ
          // is not yet set to static, it will be done later into the code generator.
          assert !lambdaExpression.binding.isStatic();
          lambdaExpression.binding.modifiers |= ClassFileConstants.AccStatic;
        } else {
          assert !lambdaExpression.binding.isStatic();
        }
        lambdaMethod = getTypeMap().get(lambdaExpression.binding);
        lambdaMethod.setModifier(lambdaMethod.getModifier() | JModifier.LAMBDA_METHOD);
        if (lambdaExpression.binding.declaringClass.isInterface()) {
          lambdaMethod.setModifier(lambdaMethod.getModifier() | JModifier.PUBLIC);
        }
        lambdaExpression.binding.modifiers |= ClassFileConstants.AccPrivate;
        lambdaExpression.binding.modifiers &= ~ClassFileConstants.AccStatic;
        // 'lambda$' prefix is mandatory for IntelliJ to be able to 'step into' lambda
        // implementation during debug session, otherwise 'step into' does not stop on synthetic
        // method.
        lambdaMethod.getMethodIdWide()
            .setName("lambda$" + ReferenceMapper.intern(NamingTools.getNonSourceConflictingName(
                BinaryQualifiedNameFormatter.getFormatter().getName(curClass.type)
                    + "_" + lambdaMethod.getMethodIdWide().getName())));
      } catch (JTypeLookupException e) {
        throw translateException(lambdaExpression, e);
      } catch (RuntimeException e) {
        throw translateException(lambdaExpression, e);
      }
      assert lambdaMethod != null;

      SourceInfo lambdaMthSourceInfo = lambdaMethod.getSourceInfo();
      JBlock lambdaBodyBlock = new JBlock(lambdaMthSourceInfo);
      JMethodBody lambdaBody = new JMethodBody(lambdaMthSourceInfo, lambdaBodyBlock);
      lambdaMethod.setBody(lambdaBody);

      MethodInfo lambdaMethodInfo =
          new MethodInfo(this, lambdaMethod, lambdaBody, lambdaExpression.scope);
      List<JExpression> capturedVars = getCapturedVariables(lambdaExpression, lambdaMethodInfo);

      pushMethodInfo(lambdaMethodInfo);

      // Map ECJ argument to Jack IR arguments and the set name of Jack IR arguments. Indeed
      // during the JMethod creation, name of parameters come from a MethodDeclaration, but lambda
      // method does not have MethodDeclaration thus set it now.
      if (lambdaExpression.arguments != null && lambdaExpression.arguments.length != 0) {
        Iterator<JParameter> parameterIt = lambdaMethod.getParams().iterator();
        assert parameterIt.hasNext();
        JParameter jparameter = parameterIt.next();
        while (jparameter.isCapturedVariable()) {
          jparameter = parameterIt.next();
        }
        for (Argument argument : lambdaExpression.arguments) {
          jparameter.setName(new String(argument.name));
          curMethod.addVariableMapping(argument.binding, jparameter);
          if (parameterIt.hasNext()) {
            jparameter = parameterIt.next();
          }
        }
      }

      lambdaExpression.body.traverse(this, curMethod.scope);

      if (lambdaExpression.body instanceof Expression) {
        JExpression bodyExpression = pop((Expression) lambdaExpression.body);
        if (!lambdaMethod.getType().isSameType(JPrimitiveTypeEnum.VOID.getType())) {
          lambdaBodyBlock
              .addStmt(new JReturnStatement(bodyExpression.getSourceInfo(), bodyExpression));
        } else {
          lambdaBodyBlock.addStmt(bodyExpression.makeStatement());
          generateImplicitReturn();
        }
      } else {
        assert lambdaExpression.body instanceof Block;
        JStatement block = pop(lambdaExpression.body);
        lambdaBodyBlock.addStmts(((JBlock) block).getStatements());

        if ((lambdaExpression.bits & ASTNode.NeedFreeReturn) != 0) {
          generateImplicitReturn();
        }
      }

      popMethodInfo();

      JLambda lambda =
          new JLambda(lambdaSourceInfo, getJMethodId(lambdaExpression.descriptor.original()),
              new JMethodIdRef(lambdaSourceInfo, lambdaMethod.getEnclosingType(),
                  lambdaMethod.getMethodId()),
          (JDefinedInterface) getTypeMap().get(getLambdaType(lambdaExpression, blockScope)),
          getInterfaceBounds(lambdaExpression, blockScope),
          getJMethodId(lambdaExpression.descriptor));
      lambda.addBridgeMethodIds(getBridges(lambdaExpression));

      if (csc.shouldCaptureInstance) {
        lambda.addCapturedVariable(makeThisRef(lambdaSourceInfo));
      }

      for (JExpression capturedVar : capturedVars) {
        lambda.addCapturedVariable(capturedVar);
      }

      push(lambda);

      return false;
    }

    @Nonnull
    private List<JExpression> getCapturedVariables(@Nonnull LambdaExpression lambdaExpression,
        @Nonnull MethodInfo lambdaMethodInfo) {
      SourceInfo lambdaSourceInfo = makeSourceInfo(lambdaExpression);

      List<JExpression> capturedVars = new ArrayList<JExpression>();

      List<SyntheticArgumentBinding> argAlreadyCaptured = new ArrayList<SyntheticArgumentBinding>();

      // Check if captured variables (outerLocalVariables) are already move into a field of the
      // current class due to an inner class.
      SyntheticArgumentBinding[] curClassSyntheticOuterLocalVariables =
          curClass.typeDecl.binding.syntheticOuterLocalVariables();
      if (curClassSyntheticOuterLocalVariables != null) {
        for (SyntheticArgumentBinding curClassSynthArg : curClassSyntheticOuterLocalVariables) {
          for (SyntheticArgumentBinding lambdaSynthArg : lambdaExpression.outerLocalVariables) {
            if (curClassSynthArg.actualOuterLocalVariable
                == lambdaSynthArg.actualOuterLocalVariable) {
              assert curClassSynthArg.matchingField != null;
              JField field = typeMap.get(curClassSynthArg.matchingField);
              assert field != null;
              JFieldRef fieldRef = makeInstanceFieldRef(lambdaSourceInfo, field);
              capturedVars.add(fieldRef);
              argAlreadyCaptured.add(lambdaSynthArg);
              break;
            }
          }
        }
      }

      Iterator<JParameter> it = lambdaMethodInfo.method.getParams().iterator();
      // Add remaining captured variables that are not yet move into a field due to an inner class
      for (SyntheticArgumentBinding synthArg : lambdaExpression.outerLocalVariables) {
        JParameter jparameter = it.next();
        jparameter.setCapturedVariable();
        lambdaMethodInfo.addVariableMapping(synthArg.actualOuterLocalVariable, jparameter);
        jparameter.setName(new String(synthArg.actualOuterLocalVariable.name));
        if (synthArg.matchingField == null && !argAlreadyCaptured.contains(synthArg)) {
          JVariable var = curMethod.getJVariable(synthArg.actualOuterLocalVariable);
          capturedVars.add(var.makeRef(lambdaSourceInfo));
        }
      }

      return capturedVars;
    }

    @Nonnull
    private JMethodId getJMethodId(@Nonnull MethodBinding mb) {
      JMethodIdWide methodIdWide =
          new JMethodIdWide(ReferenceMapper.intern(mb.selector), MethodKind.INSTANCE_VIRTUAL);

      for (TypeBinding parameterType : mb.parameters) {
        methodIdWide.addParam(getTypeMap().get(parameterType));
      }

      return new JMethodId(methodIdWide, getTypeMap().get(mb.returnType));
    }

    @Nonnull
    private List<JMethodId> getBridges(@Nonnull FunctionalExpression fe) {
      MethodBinding[] bridges = fe.getRequiredBridges();
      List<JMethodId> mds = new ArrayList<JMethodId>();

      if (bridges != null) {
        for (MethodBinding bridge : bridges) {
          mds.add(getJMethodId(bridge));
        }
      }

      return mds;
    }

    @Nonnull
    private TypeBinding getLambdaType(@Nonnull FunctionalExpression functionalExpression,
        @Nonnull BlockScope blockScope) {
      TypeBinding resolvedType = functionalExpression.resolvedType;

      if (resolvedType instanceof IntersectionTypeBinding18) {
        resolvedType = ((IntersectionTypeBinding18) resolvedType).getSAMType(blockScope);
      }

      assert resolvedType != null;
      return resolvedType;
    }

    @Nonnull
    private List<JInterface> getInterfaceBounds(
        @Nonnull FunctionalExpression functionalExpression,
        @Nonnull BlockScope blockScope) {
      List<JInterface> bounds = new ArrayList<JInterface>();

      TypeBinding expectedType = functionalExpression.expectedType();
      if (expectedType instanceof IntersectionTypeBinding18) {
        List<JType> types = getTypeMap().getBounds((IntersectionTypeBinding18) expectedType);

        for (JType type :types) {
          if (type instanceof JInterface) {
            bounds.add((JInterface) type);
          } else {
            blockScope.problemReporter().targetTypeIsNotAFunctionalInterface(functionalExpression);
            throw new FrontendCompilationError();
          }
        }

        ReferenceBinding[] intersectingTypes =
            ((IntersectionTypeBinding18) expectedType).intersectingTypes;
        int samCount = 0;
        for (int i = 0; i < intersectingTypes.length; i++) {
          MethodBinding method = intersectingTypes[i].getSingleAbstractMethod(blockScope,
              /* replaceWildcards= */ true);
          if (method != null) {
            if (method.isValidBinding()) {
              samCount++;
            } else {
              if (intersectingTypes[i].methods().length != 0 && samCount > 0) {
                blockScope.problemReporter()
                    .targetTypeIsNotAFunctionalInterface(functionalExpression);
                throw new FrontendCompilationError();
              }
            }
          }
        }
      }

      return bounds;
    }

    @Override
    public void endVisit(Argument argument, BlockScope scope) {
      JVariable jvar = curMethod.getJVariable(argument.binding);
      addAnnotations(argument.annotations, jvar);
    }

    @Override
    public void endVisit(LocalDeclaration x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        JLocal local = (JLocal) curMethod.getJVariable(x.binding);
        assert local != null;
        JLocalRef localRef = local.makeRef(info);
        JExpression initialization = pop(x.initialization);

        addAnnotations(x.annotations, local);

        if (initialization != null) {
          push(new JAsgOperation(info, localRef, initialization).makeStatement());
        } else {
          push(null);
        }
      } catch (JTypeLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(LongLiteral x, BlockScope scope) {
      try {
        push(new JLongLiteral(makeSourceInfo(x), x.constant.longValue()));
      } catch (RuntimeException e) {
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
            receiver = makeThisReference(info, targetType, true, scope, x);
          }
        }

        JDefinedClassOrInterface receiverType;
        if (x.actualReceiverType instanceof IntersectionTypeBinding18) {
          if (method.getEnclosingType() instanceof JDefinedInterface) {
            receiverType = method.getEnclosingType();
          } else {
            ReferenceBinding firstBound =
                ((IntersectionTypeBinding18) x.actualReceiverType).intersectingTypes[0];
            assert firstBound.isClass();
            receiverType = (JDefinedClass) getTypeMap().get(firstBound);
          }
        } else {
          JType jType = getTypeMap().get(x.actualReceiverType);
          if (jType instanceof JClassOrInterface) {
            if (jType instanceof JInterface && method.getEnclosingType() != null
                && method.getEnclosingType().isSameType(javaLangObject)) {
              receiverType = method.getEnclosingType();
            } else {
              receiverType = (JDefinedClassOrInterface) jType;
            }
          } else {
            receiverType = method.getEnclosingType();
          }
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
      } catch (JTypeLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
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

        addAnnotations(x.annotations, curMethod.method);

        popMethodInfo();
      } catch (RuntimeException e) {
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
      } catch (RuntimeException e) {
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
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(QualifiedAllocationExpression x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        List<JExpression> arguments = popCallArgs(info, x.arguments, x.binding);
        pushNewExpression(info, x, x.enclosingInstance(), arguments, scope);
      } catch (JTypeLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
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
      } catch (JTypeLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(QualifiedSuperReference x, BlockScope scope) {
      try {
        // Oddly enough, super refs can be modeled as this refs, because
        // whatever expression they qualify has already been resolved.
        endVisit((QualifiedThisReference) x, scope);
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(QualifiedThisReference x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        ReferenceBinding targetType = (ReferenceBinding) x.qualification.resolvedType;
        if ((x.bits & ASTNode.DepthMASK) == 0) {
          push(makeThisRef(info));
        } else {
          push(makeThisReference(info, targetType, true, scope, x));
        }
      } catch (JTypeLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(ReturnStatement x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        JExpression expression = pop(x.expression);
        push(new JReturnStatement(info, expression));
      } catch (RuntimeException e) {
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
      } catch (JTypeLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(StringLiteral x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        push(getStringLiteral(info, x.constant.stringValue()));
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(StringLiteralConcatenation x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        push(getStringLiteral(info, x.constant.stringValue()));
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(SuperReference x, BlockScope scope) {
      try {
        JClass superClass;
        assert (superClass = curClass.classType.getSuperClass()) == null
            || getTypeMap().get(x.resolvedType).isSameType(superClass);
        // Super refs can be modeled as a this ref.
        push(makeThisRef(makeSourceInfo(x)));
      } catch (JTypeLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
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
      } catch (RuntimeException e) {
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
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(ThisReference x, BlockScope scope) {
      try {
        assert getTypeMap().get(x.resolvedType).isSameType(curClass.type);
        push(makeThisRef(makeSourceInfo(x)));
      } catch (JTypeLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
    }

    @Override
    public void endVisit(ThrowStatement x, BlockScope scope) {
      try {
        SourceInfo info = makeSourceInfo(x);
        JExpression exception = pop(x.exception);
        push(new JThrowStatement(info, exception));
      } catch (RuntimeException e) {
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
            JLocal local = (JLocal) curMethod.getJVariable(argument.binding);

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

      } catch (JTypeLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
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
      } catch (RuntimeException e) {
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
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
    }

    @Override
    public final void endVisit(TypeDeclaration typeDecl, BlockScope scope) {
      assert !JackIrBuilder.hasError(typeDecl);
      if (typeDecl.binding.constantPoolName() == null) {
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
      try {
        JAnnotationMethod method = (JAnnotationMethod) getTypeMap().get(x.binding);
        JMethodBody body = null;
        pushMethodInfo(new MethodInfo(this, method, body, x.scope));

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
          JLiteral defaultValue =
              annotationParser.parseLiteral(x.defaultValue, x.binding.returnType, x.scope);
          method.setDefaultValue(defaultValue);
          defaultValue.updateParents(method);
        }

        return false;
      } catch (JTypeLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
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
        SourceInfo sourceInfo = method.getSourceInfo();
        JMethodBody body = new JMethodBody(sourceInfo, new JBlock(sourceInfo));
        method.setBody(body);
        pushMethodInfo(new MethodInfo(this, method, body, x.scope));

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
              curMethod.addVariableMapping(nestedBinding.enclosingInstances[i], it.next());
            }
          }
        }

        // Map user arguments.
        if (x.arguments != null) {
          for (Argument argument : x.arguments) {
            curMethod.addVariableMapping(argument.binding, it.next());
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
              curMethod.addVariableMapping(arg, it.next());
            }
          }
        }

        x.statements = reduceToReachable(x.statements);
        return true;
      } catch (JTypeLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
    }

    @Override
    public boolean visit(ExplicitConstructorCall explicitConstructor, BlockScope scope) {
      ReferenceBinding allocated = explicitConstructor.binding.declaringClass;
      if (explicitConstructor.isSuperAccess() && explicitConstructor.qualification != null
          && !explicitConstructor.qualification.resolvedType.isCompatibleWith(
              allocated.enclosingType())) {
        // JLS 8.8.7.1. Explicit Constructor Invocations
        // Let C be the class being instantiated, and let S be the direct superclass of C.
        // Let O be the innermost lexically enclosing class of S
        // If invocation is qualified, it is a compile-time error if the type of qualified
        // expression is not O or a subclass of O
        scope.problemReporter().unnecessaryEnclosingInstanceSpecification(
            explicitConstructor.qualification,
            allocated);
        throw new FrontendCompilationError();
      } else if (explicitConstructor.qualification != null && isNested(allocated)) {
        ReferenceBinding targetType;
        if (allocated.isAnonymousType()) {
          targetType = (ReferenceBinding) allocated.superclass().erasure();
        } else {
          targetType = allocated;
        }
        scope.problemReporter().unnecessaryEnclosingInstanceSpecification(
            explicitConstructor.qualification, targetType);
        throw new FrontendCompilationError();
      }

      scope.methodScope().isConstructorCall = true;
      return true;
    }

    @Override
    public boolean visit(FieldDeclaration x, MethodScope scope) {
      try {
        pushInitializerMethodInfo(x, scope);
        return true;
      } catch (JMethodLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
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
    public boolean visit(ConditionalExpression conditionalExpression, BlockScope scope) {
      conditionalExpression.condition.traverse(this, scope);

      if (isOptimizedTrue(conditionalExpression.condition)) {
        conditionalExpression.valueIfTrue.traverse(this, scope);
      } else if (isOptimizedFalse(conditionalExpression.condition)) {
        conditionalExpression.valueIfFalse.traverse(this, scope);
      } else {
        conditionalExpression.valueIfTrue.traverse(this, scope);
        conditionalExpression.valueIfFalse.traverse(this, scope);
      }

      return false;
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
      } catch (JMethodLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
    }

    @Override
    public boolean visit(LocalDeclaration x, BlockScope scope) {
      try {
        curMethod.body.addLocal(createLocal(x));
        return true;
      } catch (JTypeLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
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
        JMethodBody body = null;
        if (!method.isNative()) {
          SourceInfo sourceInfo = method.getSourceInfo();
          body = new JMethodBody(sourceInfo, new JBlock(sourceInfo));
          method.setBody(body);
        }
        pushMethodInfo(new MethodInfo(this, method, body, x.scope));

        // Map user arguments.
        Iterator<JParameter> it = method.getParams().iterator();
        if (x.arguments != null) {
          for (Argument argument : x.arguments) {
            curMethod.addVariableMapping(argument.binding, it.next());
          }
        }
        x.statements = reduceToReachable(x.statements);
        return true;
      } catch (JTypeLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
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
      } catch (JTypeLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
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
      assert !JackIrBuilder.hasError(typeDecl);
      if (typeDecl.binding.constantPoolName() == null) {
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

    @Override
    public boolean visit(QualifiedAllocationExpression allocation, BlockScope scope) {
      ReferenceBinding allocated = allocation.binding.declaringClass;
      if (allocation.enclosingInstance != null && !isNested(allocated)) {
        ReferenceBinding targetType;
        if (allocated.isAnonymousType()) {
          targetType = (ReferenceBinding) allocated.superclass().erasure();
        } else {
          targetType = allocated;
        }
        scope.problemReporter().unnecessaryEnclosingInstanceSpecification(allocation,
            targetType);
        throw new FrontendCompilationError();
      }
      return super.visit(allocation, scope);
    }

    protected void endVisit(TypeDeclaration x) {
      assert !JackIrBuilder.hasError(x);
      try {
        JDefinedClassOrInterface type = curClass.type;

        if (type instanceof JDefinedEnum) {
          processEnumType((JDefinedEnum) type);
        }

        addBridgeMethods(x.binding);

        JMethod method =
            type.getMethod(NamingTools.STATIC_INIT_NAME, JPrimitiveTypeEnum.VOID.getType());
        JAbstractMethodBody body = method.getBody();
        assert body != null;
        ((JMethodBody) body).getBlock().addStmt(
            new JReturnStatement(session.getSourceInfoFactory().create(
                method.getSourceInfo().getEndLine(), method.getSourceInfo().getEndLine(),
                method.getSourceInfo().getFileName()), null));

        addAnnotations(x.annotations, curClass.type);

        curClass = classStack.pop();
      } catch (JLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
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
      if (annotation.recipient.kind() == Binding.TYPE_USE
          || annotation.recipient.kind() == Binding.TYPE_PARAMETER) {
        return false;
      }
      try {
        JAnnotation jAnnotation = (JAnnotation) annotationParser.parseLiteral(annotation,
            annotation.resolvedType, scope);
        push(jAnnotation);
        return false;
      } catch (JTypeLookupException e) {
        throw translateException(annotation, e);
      } catch (RuntimeException e) {
        throw translateException(annotation, e);
      }
    }

    protected boolean visit(TypeDeclaration x) {
      assert !JackIrBuilder.hasError(x);
      try {
        JDefinedClassOrInterface type = (JDefinedClassOrInterface) getTypeMap().get(x.binding);
        classStack.push(curClass);
        curClass = new ClassInfo(type, x);

        SourceTypeBinding binding = x.binding;
        if (isNested(binding)) {

          assert (type instanceof JDefinedClass);

          if (!binding.isMemberType() && binding.isLocalType()
              && ((LocalTypeBinding) binding).enclosingMethod != null) {
            ((JDefinedClass) type).setEnclosingMethod(curMethod.method);
          }

          // add synthetic fields for outer this and locals
          NestedTypeBinding nestedBinding = (NestedTypeBinding) binding;
          if (nestedBinding.outerLocalVariables != null) {
            for (int i = 0; i < nestedBinding.outerLocalVariables.length; ++i) {
              SyntheticArgumentBinding arg = nestedBinding.outerLocalVariables[i];
              // Force creation of synthetic arg even if normally it is useless since a local should
              // be used directly. Nevertheless, $init method could requires it because it does not
              // have access to the local.
              if (arg.matchingField == null) {
                nestedBinding.addSyntheticArgumentAndField(arg.actualOuterLocalVariable);
              }
            }
          }

          if (!binding.isMemberType() && binding.isLocalType()
              && ((LocalTypeBinding) binding).enclosingMethod != null) {
            ((JDefinedClass) type).setEnclosingMethod(curMethod.method);
          }

          if (x.binding.syntheticFields() != null) {
            for (FieldBinding fieldBinding : x.binding.syntheticFields()) {
              JType fieldType = getTypeMap().get(fieldBinding.type);
              SourceInfo info = type.getSourceInfo();
              int modifier = JModifier.FINAL | JModifier.SYNTHETIC;
              JField field = new JField(info, ReferenceMapper.intern(fieldBinding.name), type,
                  fieldType, modifier);
              type.addField(field);
              getTypeMap().setField(fieldBinding, field);
              field.updateParents(type);
            }
          }
        }
        return true;
      } catch (JTypeLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
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
     * @throws JTypeLookupException
     */
    private void addBridgeMethods(SourceTypeBinding clazzBinding) throws JTypeLookupException {
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

    private JBinaryOperation assignSyntheticField(SourceInfo info, SyntheticArgumentBinding arg)
        throws JTypeLookupException {
      JParameter param = (JParameter) curMethod.getJVariable(arg);
      assert param != null;

      JField field = typeMap.get(arg.matchingField);
      assert field != null;

      JFieldRef lhs = makeInstanceFieldRef(info, field);
      JParameterRef rhs = param.makeRef(info);
      JBinaryOperation asg = new JAsgOperation(info, lhs, rhs);
      return asg;
    }

    /**
     * Create a bridge method. It calls a same-named method with the same
     * arguments, but with a different type signature.
     * @throws JTypeLookupException
     */
    private void createBridgeMethod(@Nonnull SyntheticMethodBinding jdtBridgeMethod)
        throws JTypeLookupException {
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

      pushMethodInfo(new MethodInfo(this, bridgeMethod, body, null /* no available scope */));

      // create a call and pass all arguments through, casting if necessary
      JMethodCall call = makeMethodCall(info, makeThisRef(info), implMethod.getEnclosingType(),
          implMethod);
      for (int i = 0; i < bridgeMethod.getParams().size(); i++) {
        JParameter param = bridgeMethod.getParams().get(i);
        JParameterRef paramRef = param.makeRef(info);
        call.addArg(maybeCast(implParams.get(i).getType(), paramRef));
      }

      if (bridgeMethod.getType() == JPrimitiveTypeEnum.VOID.getType()) {
        body.getBlock().addStmt(call.makeStatement());
        body.getBlock().addStmt(new JReturnStatement(info, null));
      } else {
        body.getBlock().addStmt(
            new JReturnStatement(info, maybeCast(bridgeMethod.getType(), call)));
      }
      popMethodInfo();
    }

    private JField createEnumValuesField(JDefinedEnum type) throws JTypeLookupException,
        JMethodLookupException {
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

    private JLocal createLocal(LocalDeclaration x) throws JTypeLookupException {
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

      typeMap.addGenericSignatureMarker(b.type, newLocal);

      curMethod.addVariableMapping(b, newLocal);
      return newLocal;
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
        return new JAsgOperation(info, local.makeRef(info), value).makeStatement();
    }

    private JFieldRef makeInstanceFieldRef(SourceInfo info, JField field) {
      return new JFieldRef(info, makeThisRef(info), field.getId(), field.getEnclosingType());
    }

    private JExpression makeLocalRef(SourceInfo info, LocalVariableBinding b)
        throws JTypeLookupException {
      JVariable variable = curMethod.getJVariable(b);
      assert variable != null;
      return variable.makeRef(info);
    }

    private JThisRef makeThisRef(SourceInfo info) {
      if (curMethod.method == null || curMethod.method.isStatic()) {
        return null;
      }
      assert !(curMethod.method.isAbstract() || curMethod.method.isNative());
      JThis jThis = curMethod.method.getThis();
      assert jThis != null;
      return jThis.makeRef(info);
    }

    @Nonnull
    private Object[] getEmulationPath(@Nonnull BlockScope scope,
        @Nonnull ReferenceBinding targetType,
        boolean exactMatch,
        boolean denyEnclosingArgInConstructorCall,
        ASTNode node) {
      Object[] path = scope.getEmulationPath(targetType, exactMatch,
          denyEnclosingArgInConstructorCall);
      if (path == BlockScope.NoEnclosingInstanceInConstructorCall) {
        scope.problemReporter().noSuchEnclosingInstance(targetType, node,
            /* isConstructorCall = */ true);
        throw new FrontendCompilationError();
      } else if (path == BlockScope.NoEnclosingInstanceInStaticContext || path == null) {
        scope.problemReporter().noSuchEnclosingInstance(targetType, node,
            /* isConstructorCall = */ false);
        throw new FrontendCompilationError();
      }
      return path;
    }

    @Nonnull
    private VariableBinding[] getEmulationPath(@Nonnull BlockScope scope,
        @Nonnull LocalVariableBinding localVariable,
        ASTNode node) {
      VariableBinding[] path = scope.getEmulationPath(localVariable);
      if (path == null) {
        scope.problemReporter().needImplementation(node);
        throw new FrontendCompilationError();
      }
      return path;
    }

    private JExpression makeThisReference(SourceInfo info, ReferenceBinding targetType,
        boolean exactMatch, BlockScope scope, ASTNode node) throws JTypeLookupException {
      targetType = (ReferenceBinding) targetType.erasure();
      Object[] path = getEmulationPath(scope, targetType, exactMatch,
          /* denyEnclosingArgInConstructorCall = */ false, node);
      if (path == BlockScope.EmulationPathToImplicitThis) {
        return makeThisRef(info);
      }
      JExpression ref;
      ReferenceBinding type;
      // Field representing synthetic arg must not be used into constructor because this field could
      // be not initialized yet when constructor call another constructor through 'this' (See
      // InnerTest.test021). In this case (see the condition) use directly the parameter
      // representing the synthetic arg.
      if (!(curMethod.method instanceof JConstructor) &&
          path[0] instanceof SyntheticArgumentBinding) {
        SyntheticArgumentBinding b = (SyntheticArgumentBinding) path[0];
        assert b.matchingField != null;
        JField field = typeMap.get(b.matchingField);
        assert field != null;
        ref = makeInstanceFieldRef(info, field);
        type = (ReferenceBinding) b.type.erasure();
      } else if (path[0] instanceof SyntheticArgumentBinding) {
        SyntheticArgumentBinding b = (SyntheticArgumentBinding) path[0];
        JParameter param = (JParameter) curMethod.getJVariable(b);
        assert param != null;
        ref = param.makeRef(info);
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
      if (!expected.isSameType(expression.getType())) {
        return new JDynamicCastOperation(expression.getSourceInfo(), expression, expected);
      } else {
        return expression;
      }
    }

    private JNode pop() {
      return nodeStack.remove(nodeStack.size() - 1);
    }

    private List<JExpression> popCallArgs(SourceInfo info, Expression[] jdtArgs,
        MethodBinding binding) throws JTypeLookupException {
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

    private void processEnumType(JDefinedEnum type) throws JMethodLookupException,
        JTypeLookupException {
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

    private void processSuperCallLocalArgs(ReferenceBinding superClass, JMethodCall call)
        throws JTypeLookupException {
      if (superClass.syntheticOuterLocalVariables() != null) {
        for (SyntheticArgumentBinding arg : superClass.syntheticOuterLocalVariables()) {
          // TODO(gwt): use emulation path here.
          // Got to be one of my params
          JType varType = getTypeMap().get(arg.type);
          String varName = ReferenceMapper.intern(arg.name);
          JParameter param = null;
          for (JParameter paramIt : curMethod.method.getParams()) {
            if (varType.isSameType(paramIt.getType()) && varName.equals(paramIt.getName())) {
              param = paramIt;
            }
          }
          assert param != null : "Could not find matching local arg for explicit super ctor call.";
          call.addArg(param.makeRef(call.getSourceInfo()));
        }
      }
    }

    private void processSuperCallThisArgs(ReferenceBinding superClass, JMethodCall call,
        JExpression qualifier, ExplicitConstructorCall expression) throws JTypeLookupException {
      if (superClass.syntheticEnclosingInstanceTypes() != null) {
        Expression qualification = expression.qualification;
        for (ReferenceBinding targetType : superClass.syntheticEnclosingInstanceTypes()) {
          if (qualification != null && superClass.enclosingType() == targetType) {
            assert qualification.resolvedType.erasure().isCompatibleWith(targetType);
            call.addArg(qualifier);
          } else {
            call.addArg(makeThisReference(call.getSourceInfo(), targetType, false,
                curMethod.scope, expression));
          }
        }
      }
    }

    private void processThisCallLocalArgs(ReferenceBinding binding, JMethodCall call)
        throws JTypeLookupException {
      if (binding.syntheticOuterLocalVariables() != null) {
        for (SyntheticArgumentBinding arg : binding.syntheticOuterLocalVariables()) {
          JParameter param = (JParameter) curMethod.getJVariable(arg);
          assert param != null;
          call.addArg(param.makeRef(call.getSourceInfo()));
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
          call.addArg(param.makeRef(call.getSourceInfo()));
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
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
    }

    private void pushInitializerMethodInfo(FieldDeclaration x, MethodScope scope)
        throws JMethodLookupException {
      JMethod initMeth;
      if (x.isStatic()) {
        initMeth =
            curClass.type.getMethod(NamingTools.STATIC_INIT_NAME,
                JPrimitiveTypeEnum.VOID.getType());
      } else {
        initMeth = curClass.type.getMethod(INIT_METHOD_NAME, JPrimitiveTypeEnum.VOID.getType());
      }
      pushMethodInfo(new MethodInfo(this, initMeth, (JMethodBody) initMeth.getBody(), scope));
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
              && new String(methodSig).equals("()" + CommonTypes.JAVA_LANG_CLASS.toString())) {
            try {
              return getTypeMap().get(method);
            } catch (JTypeLookupException e) {
              throw new AssertionError(e);
            }
          }
        }

      }
      throw new AssertionError();
    }

    private void pushNewExpression(SourceInfo info, AllocationExpression x, Expression qualifier,
        List<JExpression> arguments, BlockScope scope) throws JTypeLookupException {
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
      JMethodCall call = new JNewInstance(info, ctor.getEnclosingType(), ctor.getMethodIdWide());
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

      if (isNested) {
        // Synthetic this args for inner classes
        if (targetBinding.syntheticEnclosingInstanceTypes() != null) {
          ReferenceBinding targetEnclosingType = checkedTargetType.enclosingType();
          for (ReferenceBinding argType : targetBinding.syntheticEnclosingInstanceTypes()) {
            argType = (ReferenceBinding) argType.erasure();
            if (qualifier != null && argType == targetEnclosingType) {
              // If the constructor has a qualifier, we have to check for a null pointer
              // d.new A() => new A((tmp = d, tmp.getClass(), tmp));
              SourceInfo sourceInfo = qualExpr.getSourceInfo();
              JLocal tmp = new JLocal(sourceInfo,
                  ".newInstanceQualifier" + newInstanceQualifierSuffix++, qualExpr.getType(),
                  JModifier.FINAL | JModifier.SYNTHETIC, curMethod.body);
              JAsgOperation asg = new JAsgOperation(sourceInfo, tmp.makeRef(sourceInfo),
                  new CloneExpressionVisitor().cloneExpression(qualExpr));
              curMethod.body.addLocal(tmp);

              JMethodCall getClassCall = makeMethodCall(sourceInfo, tmp.makeRef(sourceInfo),
                  javaLangObject, getGetClassMethod());

              JMultiExpression multiExpr = new JMultiExpression(info,
                  asg, getClassCall, tmp.makeRef(sourceInfo));
              call.addArg(multiExpr);
            } else {
              // check supplementary error
              getEmulationPath(scope, argType, false /* onlyExactMatch */,
                  true /* denyEnclosingArgInConstructorCall */, x);

              JExpression thisRef = makeThisReference(info, argType, false, scope, x);
              call.addArg(thisRef);
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
            call.addArg(generateEmulationPath(info, path));
          }
        }
      }

      push(call);
    }

    @Nonnull
    private JExpression generateEmulationPath(@Nonnull SourceInfo info,
        @Nonnull VariableBinding[] paths) {
      assert paths.length == 1;
      VariableBinding path = paths[0];
      JExpression result;

      // Field representing synthetic arg must not be used into constructor because this field could
      // not be initialized when constructor call another constructor through 'this' (See
      // InnerTest.test021). In this case (see the condition) use directly the parameter
      // representing the synthetic arg.
      if (!(curMethod.method instanceof JConstructor)
          && path instanceof SyntheticArgumentBinding) {
        SyntheticArgumentBinding sb = (SyntheticArgumentBinding) path;
        if (sb.matchingField == null) {
          // If matchingField is null, then it is a captured variable that can be retrieve into the
          // JVariable of the current method
          result = makeLocalRef(info, sb.actualOuterLocalVariable);
        } else {

          JField field = typeMap.get(sb.matchingField);
          assert field != null;
          result = makeInstanceFieldRef(info, field);
        }
      } else if (path instanceof LocalVariableBinding) {
        result = makeLocalRef(info, (LocalVariableBinding) path);
      } else if (path instanceof FieldBinding) {
        JField field = getTypeMap().get((FieldBinding) path);
        assert field != null;
        result = makeInstanceFieldRef(info, field);
      } else {
        throw new AssertionError("Unknown emulation path.");
      }

      return result;
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

    private JExpression resolveNameReference(NameReference x, BlockScope scope)
        throws JTypeLookupException {
      SourceInfo info = makeSourceInfo(x);

      if (x.constant != Constant.NotAConstant) {
        if (generateJackLibrary) {
          if (x.binding instanceof FieldBinding) {
            FieldBinding b = ((FieldBinding) x.binding).original();
            JField field = getTypeMap().get(b);
            session.getTypeDependencies().addConstantDependency(curClass.type,
                field.getEnclosingType());
          }
        }

        return getConstant(info, x.constant);
      }

      Binding binding = x.binding;
      JExpression result = null;
      if (binding instanceof LocalVariableBinding) {
        LocalVariableBinding b = (LocalVariableBinding) binding;

        // Check for one front end compilation error that was not reported during parsing
        try {
          NameReferenceCaller.checkEffectiveFinality(x, b, scope);
        } catch (AbortMethod e) {
          throw new FrontendCompilationError();
        }

        if ((x.bits & ASTNode.DepthMASK) != 0) {
          VariableBinding[] path = getEmulationPath(scope, b, x);
          result = generateEmulationPath(info, path);
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
            thisRef = makeThisReference(info, targetType, true /* exactMatch */, scope, x);
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

      SourceInfo sourceInfo = result.getSourceInfo();

      JMethodCall getClassCall = makeMethodCall(
          sourceInfo, ((JFieldRef) result).getInstance(), javaLangObject, getGetClassMethod());

      return (new JMultiExpression(sourceInfo,
          getClassCall, cst));
    }

    private void writeEnumValueOfMethod(JDefinedEnum type, JMethod method)
        throws JTypeLookupException {
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
        JParameterRef nameRef = method.getParams().get(0).makeRef(info);
        JMethod jValueOfBinding = getTypeMap().get(valueOfBinding);
        JMethodCall call = makeMethodCall(info, null, jValueOfBinding.getEnclosingType(),
            jValueOfBinding);
        call.addArgs(clazz, nameRef);
        implementMethod(method, new JDynamicCastOperation(info, call, type));
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
    public final JDefinedClassOrInterface type;
    public final TypeDeclaration typeDecl;
    @Nonnegative
    private int mthRefCount = 0;

    public ClassInfo(JDefinedClassOrInterface type, TypeDeclaration x) {
      this.type = type;
      this.classType = (type instanceof JDefinedClass) ? (JDefinedClass) type : null;
      this.typeDecl = x;
      this.scope = x.scope;
    }
  }

  static class MethodInfo {
    public final JMethodBody body;
    @Nonnull
    public final Map<Object, JVariable> locals = new IdentityHashMap<Object, JVariable>();
    @Nonnull
    public final JMethod method;
    public final MethodScope scope;
    @Nonnull
    private final AstVisitor ast;

    public MethodInfo(@Nonnull AstVisitor ast, @Nonnull JMethod method,
        @CheckForNull JMethodBody methodBody, @CheckForNull MethodScope methodScope) {
      this.method = method;
      this.body = methodBody;
      this.scope = methodScope;
      this.ast = ast;
    }

    @Nonnull
    public JVariable getJVariable(@Nonnull LocalVariableBinding ecjVar)
        throws JTypeLookupException {
      JVariable jackVar = null;
      if (ecjVar.declaration == null) {
        jackVar = locals.get(ecjVar);
      } else {
        jackVar = locals.get(ecjVar.declaration);
        if (jackVar == null && ((ecjVar.declaration.bits & ASTNode.IsReachable) == 0)) {
          // Variable declaration appears in dead code but the variable is used in non dead code,
          // thus force variable declaration creation
          body.addLocal(ast.createLocal(ecjVar.declaration));
          jackVar = locals.get(ecjVar.declaration);
        }
      }
      assert jackVar != null;
      return jackVar;
    }

    public void addVariableMapping(@Nonnull LocalVariableBinding ecjVar,
        @Nonnull JVariable jackVar) {
      if (ecjVar.declaration == null) {
        locals.put(ecjVar, jackVar);
      } else {
        locals.put(ecjVar.declaration, jackVar);
      }
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
        List<JLiteral> elements = new ArrayList<JLiteral>(1);
        elements.add(result);
        result = new JArrayLiteral(makeSourceInfo(value), elements);
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
        values = new ArrayList<JLiteral>(0);
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
      try {
        parsed =
            new JClassLiteral(makeSourceInfo(x), getTypeMap().get(x.targetType), javaLangClass);
        return false;
      } catch (JTypeLookupException e) {
        throw translateException(x, e);
      } catch (RuntimeException e) {
        throw translateException(x, e);
      }
    }

    protected void visit(@Nonnull Annotation annotation, @Nonnull BlockScope scope) {
      try {
        JDefinedAnnotationType jAnnotationType =
            (JDefinedAnnotationType) getTypeMap().get(annotation.resolvedType);
        JAnnotation jAnnotation = new JAnnotation(makeSourceInfo(annotation),
            jAnnotationType.getRetentionPolicy(), jAnnotationType);

        MemberValuePair[] pairs = annotation.memberValuePairs();
        for (MemberValuePair pair : pairs) {
          JMethodIdWide methodId = getTypeMap().get(pair.binding).getMethodIdWide();
          jAnnotation.add(new JNameValuePair(makeSourceInfo(pair), methodId,
              parseLiteral(pair.value, pair.binding.returnType, scope)));
        }

        parsed = jAnnotation;
      } catch (JTypeLookupException e) {
        throw translateException(annotation, e);
      } catch (RuntimeException e) {
        throw translateException(annotation, e);
      }
    }

    private void visit(NameReference nameReference) {
      try {
        Binding binding = nameReference.binding;
        if (binding instanceof FieldBinding) {
          JField field = getTypeMap().get((FieldBinding) binding);
          assert field instanceof JEnumField;
          parsed = new JEnumLiteral(makeSourceInfo(nameReference), field.getId());
        } else {
          throw new AssertionError("Not yet supported " + nameReference.toString());
        }
      } catch (JTypeLookupException e) {
        throw translateException(nameReference, e);
      } catch (RuntimeException e) {
        throw translateException(nameReference, e);
      }
    }
  }

  private static class FrontendCompilationError extends Error {

    private static final long serialVersionUID = 1L;

    public FrontendCompilationError() {
      super();
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
    JNodeInternalError.preload();
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

  public static boolean hasError(@Nonnull TypeDeclaration typeDeclaration) {
    return (typeDeclaration.hasErrors()
    || (typeDeclaration.getCompilationUnitDeclaration() != null
        && typeDeclaration.getCompilationUnitDeclaration().hasErrors())
    // This should be redundant with typeDeclaration.getCompilationUnitDeclaration().hasErrors() but
    // since it could be null, lets be safe
    || typeDeclaration.binding == null);
  }

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

  public List<JDefinedClassOrInterface> process(CompilationUnitDeclaration cud)
      throws SourceCompilationException {
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
    try {
      javaLangObject = (JDefinedClass) getTypeMap().get(cud.scope.getJavaLangObject());
      javaLangString = (JDefinedClass) getTypeMap().get(cud.scope.getJavaLangString());
      javaLangClass = (JDefinedClass) getTypeMap().get(cud.scope.getJavaLangClass());
    } catch (JTypeLookupException e) {
      throw new AssertionError(e);
    }

    for (TypeDeclaration typeDecl : cud.types) {
      // Create fields and empty methods.
      createMembers(typeDecl);
    }

    boolean hasErrors = false;

    for (TypeDeclaration typeDecl : cud.types) {
      try {
        // hasErrors of CompilationUnitDeclaration indicates if we should continue to investigate
        // this compilation unit or not
        if (typeDecl.getCompilationUnitDeclaration() != null
            && typeDecl.getCompilationUnitDeclaration().hasErrors()) {
          hasErrors = true;
          break;
        }

        // Build the code.
        typeDecl.traverse(astVisitor, cud.scope);
      } catch (FrontendCompilationError e) {
        hasErrors = true;
      }
    }

    if (hasErrors) {
      throw new SourceCompilationException();
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

  private JNodeInternalError translateException(Exception e) {
    return new JNodeInternalError("Error building Jack IR", e);
  }

  private JNodeInternalError translateException(ASTNode node, Exception e) {
    JNodeInternalError ice = translateException(e);
    if (node != null) {
      ice.addNode(node.getClass().getName(), node.toString(), makeSourceInfo(node));
    }
    return ice;
  }

  private JNodeInternalError translateException(
      TypeDeclaration typeDeclaration, Exception e, SourceInfo info) {
    JNodeInternalError ice = translateException(e);
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
    try {
      if (x instanceof Initializer) {
        return;
      }

      getTypeMap().get(x.binding);
    } catch (JTypeLookupException e) {
      throw translateException(x, e);
    } catch (RuntimeException e) {
      throw translateException(x, e);
    }
  }

  private void createMembers(TypeDeclaration x) {
    try {
      assert !JackIrBuilder.hasError(x);
      SourceTypeBinding binding = x.binding;
      JDefinedClassOrInterface type = (JDefinedClassOrInterface) getTypeMap().get(binding);
      SourceInfo info = type.getSourceInfo();
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
    } catch (JTypeLookupException e) {
      throw translateException(x, e);
    } catch (RuntimeException e) {
      throw translateException(x, e);
    }
  }

  private void createMethod(AbstractMethodDeclaration x) {
    try {
      if (x instanceof Clinit) {
        return;
      }
      getTypeMap().get(x.binding);
    } catch (JTypeLookupException e) {
      throw translateException(x, e);
    } catch (RuntimeException e) {
      throw translateException(x, e);
    }
  }

  private JMethod createStaticInitializer(SourceInfo info, JDefinedClassOrInterface enclosingType) {
    int modifier = JModifier.STATIC | JModifier.STATIC_INIT;
    JMethod method =
        new JMethod(info,
            new JMethodId(new JMethodIdWide(NamingTools.STATIC_INIT_NAME, MethodKind.STATIC),
                JPrimitiveTypeEnum.VOID.getType()),
            enclosingType,
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
        new JMethod(info,
            new JMethodId(new JMethodIdWide(name, ReferenceMapper.getMethodKind(modifier)),
                returnType),
            enclosingType, ReferenceMapper.removeSynchronizedOnBridge(modifier
                | JModifier.SYNTHETIC));
    method.setBody(new JMethodBody(info, new JBlock(info)));
    enclosingType.addMethod(method);
    method.updateParents(enclosingType);
    return method;
  }

  @Nonnull
  private JMethod createSyntheticMethodFromBinding(@Nonnull SourceInfo info,
      @Nonnull MethodBinding binding, @CheckForNull String[] paramNames)
      throws JTypeLookupException {
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
    method.setBody(new JMethodBody(info, new JBlock(info)));
    return method;
  }

  private void createTypes(TypeDeclaration x) {
    assert !JackIrBuilder.hasError(x);
    SourceInfo info = makeSourceInfo(x);
    try {
      JDefinedClassOrInterface type = (JDefinedClassOrInterface) getTypeMap().get(x.binding);
      newTypes.add(type);
      if (x.memberTypes != null) {
        for (TypeDeclaration memberType : x.memberTypes) {
          createTypes(memberType);
        }
      }
    } catch (JTypeLookupException e) {
      throw translateException(x, e, info);
    } catch (RuntimeException e) {
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

    JMethodIdWide methodId = targetMethod.getMethodIdWide();
    assert methodId.getKind() == MethodKind.STATIC || instance != null;
    JMethodCall call = new JMethodCall(info, instance,
        receiverType, methodId, targetMethod.getType(), methodId.canBeVirtual());
    return call;
  }

  @Nonnull
  private static JMethodCall makeSuperCall(@Nonnull SourceInfo info,
      @CheckForNull JExpression instance,
      @Nonnull JDefinedClassOrInterface receiverType,
      @Nonnull JMethod targetMethod) {

    JMethodCall call = new JMethodCall(info, instance,
        receiverType, targetMethod.getMethodIdWide(), targetMethod.getType(),
        false /* isVirtualDispatch */);
    return call;
  }
}
