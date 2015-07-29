/*
 * Copyright 2008 Google Inc.
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
import com.android.jack.ir.ast.CanBeAbstract;
import com.android.jack.ir.ast.CanBeFinal;
import com.android.jack.ir.ast.CanBeNative;
import com.android.jack.ir.ast.CanBeStatic;
import com.android.jack.ir.ast.HasName;
import com.android.jack.ir.ast.HasType;
import com.android.jack.ir.ast.JAbsentArrayDimension;
import com.android.jack.ir.ast.JAbstractMethodBody;
import com.android.jack.ir.ast.JAbstractStringLiteral;
import com.android.jack.ir.ast.JAlloc;
import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JAnnotationMethod;
import com.android.jack.ir.ast.JArrayLength;
import com.android.jack.ir.ast.JArrayLiteral;
import com.android.jack.ir.ast.JArrayRef;
import com.android.jack.ir.ast.JArrayType;
import com.android.jack.ir.ast.JAssertStatement;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JBooleanLiteral;
import com.android.jack.ir.ast.JBreakStatement;
import com.android.jack.ir.ast.JByteLiteral;
import com.android.jack.ir.ast.JCaseStatement;
import com.android.jack.ir.ast.JCatchBlock;
import com.android.jack.ir.ast.JCharLiteral;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassLiteral;
import com.android.jack.ir.ast.JConditionalExpression;
import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JContinueStatement;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JDoStatement;
import com.android.jack.ir.ast.JDoubleLiteral;
import com.android.jack.ir.ast.JDynamicCastOperation;
import com.android.jack.ir.ast.JEnumLiteral;
import com.android.jack.ir.ast.JExceptionRuntimeValue;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldInitializer;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JFloatLiteral;
import com.android.jack.ir.ast.JForStatement;
import com.android.jack.ir.ast.JGoto;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JInstanceOf;
import com.android.jack.ir.ast.JIntLiteral;
import com.android.jack.ir.ast.JLabel;
import com.android.jack.ir.ast.JLabeledStatement;
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JLock;
import com.android.jack.ir.ast.JLongLiteral;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JMultiExpression;
import com.android.jack.ir.ast.JNameValuePair;
import com.android.jack.ir.ast.JNewArray;
import com.android.jack.ir.ast.JNewInstance;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.ir.ast.JNullType;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JParameterRef;
import com.android.jack.ir.ast.JPhantomClassOrInterface;
import com.android.jack.ir.ast.JPostfixOperation;
import com.android.jack.ir.ast.JPrefixOperation;
import com.android.jack.ir.ast.JPrimitiveType;
import com.android.jack.ir.ast.JReferenceType;
import com.android.jack.ir.ast.JReinterpretCastOperation;
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JShortLiteral;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JSynchronizedBlock;
import com.android.jack.ir.ast.JThis;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.ir.ast.JThrowStatement;
import com.android.jack.ir.ast.JTryStatement;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JUnlock;
import com.android.jack.ir.ast.JWhileStatement;
import com.android.jack.ir.formatter.SourceFormatter;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.util.TextOutput;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Implements a reasonable textual representation for all JNodes. The goal is to have a common base
 * for other specific generations (e.g. {@link JNode#toString()}, {@link JNode#toSource()}, ...).
 */
public class BaseGenerationVisitor extends TextOutputVisitor {
  static final char[] CHARS_ABSTRACT = "abstract ".toCharArray();
  static final char[] CHARS_ALLOC = "alloc ".toCharArray();
  static final char[] CHARS_ASSERT = "assert ".toCharArray();
  static final char[] CHARS_BREAK = "break".toCharArray();
  static final char[] CHARS_CASE = "case ".toCharArray();
  static final char[] CHARS_CATCH = " catch ".toCharArray();
  static final char[] CHARS_CLASS = "class ".toCharArray();
  static final char[] CHARS_COMMA = ", ".toCharArray();
  static final char[] CHARS_CONTINUE = "continue".toCharArray();
  static final char[] CHARS_DEFAULT = "default".toCharArray();
  static final char[] CHARS_DO = "do".toCharArray();
  static final char[] CHARS_DOTCLASS = ".class".toCharArray();
  static final char[] CHARS_ELSE = "else".toCharArray();
  static final char[] CHARS_MULTI_CATCH = "|".toCharArray();
  static final char[] CHARS_EXTENDS = "extends ".toCharArray();
  static final char[] CHARS_FALSE = "false".toCharArray();
  static final char[] CHARS_FINAL = "final ".toCharArray();
  static final char[] CHARS_FINALLY = " finally ".toCharArray();
  static final char[] CHARS_FOR = "for ".toCharArray();
  static final char[] CHARS_GOTO = "goto".toCharArray();
  static final char[] CHARS_IF = "if ".toCharArray();
  static final char[] CHARS_IMPLEMENTS = "implements ".toCharArray();
  static final char[] CHARS_INSTANCEOF = " instanceof ".toCharArray();
  static final char[] CHARS_INTERFACE = "interface ".toCharArray();
  static final char[] CHARS_NATIVE = "native ".toCharArray();
  static final char[] CHARS_NEW = "new ".toCharArray();
  static final char[] CHARS_NONAME = "no-name ".toCharArray();
  static final char[] CHARS_NULL = "null".toCharArray();
  static final char[] CHARS_PRIVATE = "private ".toCharArray();
  static final char[] CHARS_PUBLIC = "public ".toCharArray();
  static final char[] CHARS_REINTERPRETCAST = "reinterpret-cast ".toCharArray();
  static final char[] CHARS_RETURN = "return".toCharArray();
  static final char[] CHARS_RUNTIME_EXCEPTION = "Runtime exception of type".toCharArray();
  static final char[] CHARS_STATIC = "static ".toCharArray();
  static final char[] CHARS_SUPER = "super".toCharArray();
  static final char[] CHARS_SWITCH = "switch ".toCharArray();
  static final char[] CHARS_THIS = "this".toCharArray();
  static final char[] CHARS_THROW = "throw".toCharArray();
  static final char[] CHARS_TRUE = "true".toCharArray();
  static final char[] CHARS_TRY = "try ".toCharArray();
  static final char[] CHARS_WHILE = "while ".toCharArray();
  static final char[] SYNCHRONIZED_BLOCK = "synchronized ".toCharArray();
  static final char[] LOCK = "lock ".toCharArray();
  static final char[] UNLOCK = "unlock ".toCharArray();

  static final SourceFormatter formatter = SourceFormatter.getFormatter();

  protected boolean needSemi = true;

  protected boolean suppressType = false;

  public BaseGenerationVisitor(TextOutput textOutput) {
    super(textOutput);
  }

  @Override
  public boolean visit(@Nonnull JAbsentArrayDimension x) {
    // nothing to print, parent prints []
    return false;
  }

  @Override
  public boolean visit(@Nonnull JAnnotation annotation) {
    print("@");
    printTypeName(annotation.getType());
    lparen();

    List<JNameValuePair> nameValuePairs =
        new ArrayList<JNameValuePair>(annotation.getNameValuePairs());
    Collections.sort(nameValuePairs, new Comparator<JNameValuePair>() {
      @Override
      public int compare(JNameValuePair nameValuePair1, JNameValuePair nameValuePair2) {
        return nameValuePair1.getName().compareTo(nameValuePair2.getName());
      }
    });

    visitCollectionWithCommas(nameValuePairs.iterator());

    rparen();
    return false;
  }

  @Override
  public boolean visit(@Nonnull JArrayLength x) {
    JExpression instance = x.getInstance();
    parenPush(x, instance);
    accept(instance);
    parenPop(x, instance);
    print(".length");
    return false;
  }

  @Override
  public boolean visit(@Nonnull JArrayLiteral arrayLiteral) {
    List<JLiteral> values = arrayLiteral.getValues();
    if (values.size() > 1) {
      print('{');
    }

    visitCollectionWithCommas(values.iterator());

    if (values.size() > 1) {
      print('}');
    }
    return false;
  }

  @Override
  public boolean visit(@Nonnull JAlloc x) {
    print(CHARS_ALLOC);
    print("<");
    print(x.getType().getName());
    print(">");
    return false;
  }

  @Override
  public boolean visit(@Nonnull JArrayRef x) {
    JExpression instance = x.getInstance();
    parenPush(x, instance);
    accept(instance);
    parenPop(x, instance);
    print('[');
    accept(x.getIndexExpr());
    print(']');
    return false;
  }

  @Override
  public boolean visit(@Nonnull JArrayType x) {
    accept(x.getElementType());
    print("[]");
    return false;
  }

  @Override
  public boolean visit(@Nonnull JAssertStatement x) {
    print(CHARS_ASSERT);
    accept(x.getTestExpr());
    if (x.getArg() != null) {
      print(" : ");
      accept(x.getArg());
    }
    return false;
  }

  @Override
  public boolean visit(@Nonnull JBinaryOperation x) {
    // TODO(later): associativity
    JExpression arg1 = x.getLhs();
    parenPush(x, arg1);
    accept(arg1);
    parenPop(x, arg1);

    space();
    print(x.getOp().toString());
    space();

    JExpression arg2 = x.getRhs();
    parenPush(x, arg2);
    accept(arg2);
    parenPop(x, arg2);

    return false;
  }


  @Override
  public boolean visit(@Nonnull JExceptionRuntimeValue x) {
    print(CHARS_RUNTIME_EXCEPTION);
    space();
    printTypeName(x.getType());
    return super.visit(x);
  }

  @Override
  public boolean visit(@Nonnull JCatchBlock x) {
    JLocal catchVar = x.getCatchVar();
    if (catchVar.getType().isSameType(Jack.getSession().getPhantomLookup()
        .getClass(CommonTypes.JAVA_LANG_OBJECT))) {
      print(CHARS_FINALLY);
    } else {
      print(CHARS_CATCH);
      lparen();
      boolean first = true;
      for (JClass catchedType : x.getCatchTypes()) {
        if (first) {
          first = false;
        } else {
          space();
          print(CHARS_MULTI_CATCH);
          space();
        }
        printTypeName(catchedType);
      }
      space();
      printName(catchVar);
      rparen();
    }
    space();
    openBlock();
    for (JStatement statement : x.getStatements()) {
      needSemi = true;
      accept(statement);
      if (needSemi) {
        semi();
      }
      newline();
    }
    closeBlock();
    needSemi = false;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JBlock x) {
    openBlock();
    for (JStatement statement : x.getStatements()) {
      needSemi = true;
      accept(statement);
      if (needSemi) {
        semi();
      }
      newline();
    }
    closeBlock();
    needSemi = false;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JBooleanLiteral x) {
    printBooleanLiteral(x.getValue());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JBreakStatement x) {
    print(CHARS_BREAK);
    if (x.getLabel() != null) {
      space();
      accept(x.getLabel());
    }
    return false;
  }

  @Override
  public boolean visit(@Nonnull JByteLiteral x) {
    print(Byte.toString(x.getValue()));
    return false;
  }

  @Override
  public boolean visit(@Nonnull JCaseStatement x) {
    JLiteral caseExpr = x.getExpr();
    if (caseExpr != null) {
      print(CHARS_CASE);
      accept(caseExpr);
    } else {
      print(CHARS_DEFAULT);
    }
    print(':');
    space();
    needSemi = false;
    return false;
  }

  @Override
  public boolean visit(@Nonnull JReinterpretCastOperation x) {
    lparen();
    print(CHARS_REINTERPRETCAST);
    printType(x);
    rparen();
    space();

    JExpression expr = x.getExpr();
    parenPush(x, expr);
    accept(expr);
    parenPop(x, expr);
    return false;
  }

  @Override
  public boolean visit(@Nonnull JDynamicCastOperation x) {
    lparen();
    printType(x);
    rparen();
    space();

    JExpression expr = x.getExpr();
    parenPush(x, expr);
    accept(expr);
    parenPop(x, expr);
    return false;
  }

  @Override
  public boolean visit(@Nonnull JCharLiteral x) {
    printCharLiteral(x.getValue());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JClassLiteral x) {
    printTypeName(x.getRefType());
    print(CHARS_DOTCLASS);
    return false;
  }

  @Override
  public boolean visit(@Nonnull JDefinedClass x) {
    printAnnotationLiterals(x.getAnnotations());
    printTypeFlags(x);
    print(CHARS_CLASS);
    printTypeName(x);
    space();
    JClass superClass = x.getSuperClass();
    if (superClass != null) {
      print(CHARS_EXTENDS);
      printTypeName(superClass);
      space();
    }

    if (x.getImplements().size() > 0) {
      print(CHARS_IMPLEMENTS);
      for (int i = 0, c = x.getImplements().size(); i < c; ++i) {
        if (i > 0) {
          print(CHARS_COMMA);
        }
        printTypeName(x.getImplements().get(i));
      }
      space();
    }

    return false;
  }

  @Override
  public boolean visit(@Nonnull JConditionalExpression x) {
    // TODO(later): associativity
    JExpression ifTest = x.getIfTest();
    parenPush(x, ifTest);
    accept(ifTest);
    parenPop(x, ifTest);

    print(" ? ");

    JExpression thenExpr = x.getThenExpr();
    parenPush(x, thenExpr);
    accept(thenExpr);
    parenPop(x, thenExpr);

    print(" : ");

    JExpression elseExpr = x.getElseExpr();
    parenPush(x, elseExpr);
    accept(elseExpr);
    parenPop(x, elseExpr);

    return false;
  }

  @Override
  public boolean visit(@Nonnull JConstructor x) {
    printAnnotationLiterals(x.getAnnotations());

    // Modifiers
    if (x.isPrivate()) {
      print(CHARS_PRIVATE);
    } else {
      print(CHARS_PUBLIC);
    }
    printName(x);

    // Parameters
    printParameterList(x);

    if (x.isAbstract() || !shouldPrintMethodBody()) {
      semi();
      newlineOpt();
    } else {
      JMethodBody body = x.getBody();
      assert body != null;
      accept(body);
    }

    return false;
  }

  @Override
  public boolean visit(@Nonnull JContinueStatement x) {
    print(CHARS_CONTINUE);
    if (x.getLabel() != null) {
      space();
      accept(x.getLabel());
    }
    return false;
  }

  @Override
  public boolean visit(@Nonnull JFieldInitializer x) {
    if (!suppressType) {
      printName(x.getFieldRef().getFieldId());
    } else {
      accept(x.getFieldRef());
    }
    JExpression initializer = x.getInitializer();
    print(" = ");
    accept(initializer);
    return false;
  }

  @Override
  public boolean visit(@Nonnull JDoStatement x) {
    print(CHARS_DO);
    if (x.getBody() != null) {
      nestedStatementPush(x.getBody());
      accept(x.getBody());
      nestedStatementPop(x.getBody());
    }
    if (needSemi) {
      semi();
      newline();
    } else {
      space();
      needSemi = true;
    }
    print(CHARS_WHILE);
    lparen();
    accept(x.getTestExpr());
    rparen();
    return false;
  }

  @Override
  public boolean visit(@Nonnull JDoubleLiteral x) {
    printDoubleLiteral(x.getValue());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JEnumLiteral enumLiteral) {
    printTypeName(enumLiteral.getType());
    print('.');
    print(enumLiteral.getFieldId().getName());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JExpressionStatement x) {
    accept(x.getExpr());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JField x) {
    printAnnotationLiterals(x.getAnnotations());
    print(JModifier.getStringFieldModifier(x.getModifier()));
    printType(x);
    space();
    printName(x);
    return false;
  }

  @Override
  public boolean visit(@Nonnull JFieldRef x) {
    JExpression instance = x.getInstance();
    if (instance != null) {
      parenPush(x, instance);
      accept(instance);
      parenPop(x, instance);
    } else {
      printTypeName(x.getReceiverType());
    }
    print('.');
    printName(x.getFieldId());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JFloatLiteral x) {
    printFloatLiteral(x.getValue());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JForStatement x) {
    print(CHARS_FOR);
    lparen();

    Iterator<JStatement> iter = x.getInitializers().iterator();
    if (iter.hasNext()) {
      JStatement stmt = iter.next();
      accept(stmt);
    }
    suppressType = true;
    while (iter.hasNext()) {
      print(CHARS_COMMA);
      JStatement stmt = iter.next();
      accept(stmt);
    }
    suppressType = false;

    semi();
    space();
    if (x.getTestExpr() != null) {
      accept(x.getTestExpr());
    }

    semi();
    space();
    visitCollectionWithCommas(x.getIncrements().iterator());
    rparen();

    if (x.getBody() != null) {
      nestedStatementPush(x.getBody());
      accept(x.getBody());
      nestedStatementPop(x.getBody());
    }
    return false;
  }

  @Override
  public boolean visit(@Nonnull JGoto x) {
    print(CHARS_GOTO);
    space();
    accept(x.getTargetBlock().getLabel());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JIfStatement x) {
    print(CHARS_IF);
    lparen();
    accept(x.getIfExpr());
    rparen();

    if (x.getThenStmt() != null) {
      nestedStatementPush(x.getThenStmt());
      accept(x.getThenStmt());
      nestedStatementPop(x.getThenStmt());
    }

    if (x.getElseStmt() != null) {
      if (needSemi) {
        semi();
        newline();
      } else {
        space();
        needSemi = true;
      }
      print(CHARS_ELSE);
      boolean elseIf = x.getElseStmt() instanceof JIfStatement;
      if (!elseIf) {
        nestedStatementPush(x.getElseStmt());
      } else {
        space();
      }
      accept(x.getElseStmt());
      if (!elseIf) {
        nestedStatementPop(x.getElseStmt());
      }
    }

    return false;
  }

  @Override
  public boolean visit(@Nonnull JInstanceOf x) {
    JExpression expr = x.getExpr();
    parenPush(x, expr);
    accept(expr);
    parenPop(x, expr);
    print(CHARS_INSTANCEOF);
    printTypeName(x.getTestType());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JDefinedInterface x) {
    printAnnotationLiterals(x.getAnnotations());
    printTypeFlags(x);
    print(CHARS_INTERFACE);
    printTypeName(x);
    space();

    if (x.getImplements().size() > 0) {
      print(CHARS_EXTENDS);
      for (int i = 0, c = x.getImplements().size(); i < c; ++i) {
        if (i > 0) {
          print(CHARS_COMMA);
        }
        printTypeName(x.getImplements().get(i));
      }
      space();
    }

    return false;
  }

  @Override
  public boolean visit(@Nonnull JIntLiteral x) {
    print(Integer.toString(x.getValue()));
    return false;
  }

  @Override
  public boolean visit(@Nonnull JLabel x) {
    printName(x);
    return false;
  }

  @Override
  public boolean visit(@Nonnull JLabeledStatement x) {
    accept(x.getLabel());
    print(" : ");
    accept(x.getBody());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JLocal x) {
    printAnnotationLiterals(x.getAnnotations());
    printFinalFlag(x);
    printType(x);
    space();
    printName(x);
    return false;
  }

  @Override
  public boolean visit(@Nonnull JLocalRef x) {
    printName(x.getLocal());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JLongLiteral x) {
    printLongLiteral(x.getValue());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JMethod x) {
    printMethodHeader(x);

    if (x instanceof JAnnotationMethod) {
      JLiteral defaultValue = ((JAnnotationMethod) x).getDefaultValue();
      if (defaultValue != null) {
        space();
        print(CHARS_DEFAULT);
        space();
        accept(defaultValue);

        semi();
        newlineOpt();
      }
    } else if (x.isAbstract() || !shouldPrintMethodBody()) {
      semi();
      newlineOpt();
    } else {
      space();
      JAbstractMethodBody body = x.getBody();
      assert body != null;
      accept(body);
    }

    return false;
  }

  @Override
  public boolean visit(@Nonnull JMethodBody x) {
    accept(x.getBlock());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JMethodCall x) {
    JExpression instance = x.getInstance();
    JMethodId target = x.getMethodId();
    if (instance == null) {
      // Static call.
      printTypeName(x.getReceiverType());
      print('.');
      printName(target);
    } else if (x.getInstance() instanceof JThisRef) {
      // super() or this() call.
      JReferenceType thisType = (JReferenceType) instance.getType();
      if (thisType.isSameType(x.getReceiverType())) {
        print(CHARS_THIS);
      } else {
        print(CHARS_SUPER);
      }
      if (!(x instanceof JNewInstance)) {
        print('.');
        printName(target);
      }
    } else {
      // Instance call.
      parenPush(x, instance);
      accept(instance);
      parenPop(x, instance);
      print('.');
      printName(target);
    }
    lparen();
    visitCollectionWithCommas(x.getArgs().iterator());
    rparen();
    return false;
  }

  @Override
  public boolean visit(@Nonnull JMultiExpression x) {
    lparen();
    visitCollectionWithCommas(x.exprs.iterator());
    rparen();
    return false;
  }

  @Override
  public boolean visit(@Nonnull JNameValuePair nameValuePair) {
    print(nameValuePair.getName());
    print(" = ");
    accept(nameValuePair.getValue());
    return false;
  }


  @Override
  public boolean visit(@Nonnull JNewArray x) {
    print(CHARS_NEW);
    List<JExpression> initializers = x.getInitializers();
    boolean hasInitializer = initializers.isEmpty();
    printTypeName(!hasInitializer ? x.getArrayType() : x.getArrayType().getLeafType());
    if (!hasInitializer) {
      print("{");
      visitCollectionWithCommas(initializers.iterator());
      print('}');
    } else {
      List<JExpression> dims = x.getDims();
      for (int i = 0; i < dims.size(); ++i) {
        JExpression expr = dims.get(i);
        print('[');
        accept(expr);
        print(']');
      }
    }

    return false;
  }

  @Override
  public boolean visit(@Nonnull JNewInstance x) {
    print(CHARS_NEW);
    JMethodId target = x.getMethodId();
    printName(target);
    lparen();
    visitCollectionWithCommas(x.getArgs().iterator());
    rparen();
    return false;
  }

  @Override
  public boolean visit(@Nonnull JNullLiteral x) {
    print(CHARS_NULL);
    return false;
  }

  @Override
  public boolean visit(@Nonnull JNullType x) {
    printTypeName(x);
    return false;
  }

  @Override
  public boolean visit(@Nonnull JPackage pack) {
    print(formatter.getName(pack));
    return false;
  }

  @Override
  public boolean visit(@Nonnull JParameter x) {
    printAnnotationLiterals(x.getAnnotations());
    printType(x);
    space();
    printName(x);
    return false;
  }

  @Override
  public boolean visit(@Nonnull JParameterRef x) {
    printName(x.getTarget());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JPhantomClassOrInterface x) {
    printTypeName(x);
    return false;
  }

  @Override
  public boolean visit(@Nonnull JPostfixOperation x) {
    // TODO(later): associativity
    JExpression arg = x.getArg();
    parenPush(x, arg);
    accept(arg);
    parenPop(x, arg);
    print(x.getOp().toString());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JPrefixOperation x) {
    // TODO(later): associativity
    print(x.getOp().toString());
    JExpression arg = x.getArg();
    parenPush(x, arg);
    accept(arg);
    parenPop(x, arg);
    return false;
  }

  @Override
  public boolean visit(@Nonnull JPrimitiveType x) {
    printTypeName(x);
    return false;
  }

  @Override
  public boolean visit(@Nonnull JSession x) {
    print("<JSession>");
    return false;
  }

  @Override
  public boolean visit(@Nonnull JReturnStatement x) {
    print(CHARS_RETURN);
    JExpression expr = x.getExpr();
    if (expr != null) {
      space();
      accept(expr);
    }
    return false;
  }

  @Override
  public boolean visit(@Nonnull JShortLiteral x) {
    print(Short.toString(x.getValue()));
    return false;
  }

  @Override
  public boolean visit(@Nonnull JAbstractStringLiteral x) {
    printStringLiteral(x.getValue());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JSwitchStatement x) {
    print(CHARS_SWITCH);
    lparen();
    accept(x.getExpr());
    rparen();
    space();
    nestedStatementPush(x.getBody());
    accept(x.getBody());
    nestedStatementPop(x.getBody());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JThis x) {
    printType(x);
    space();
    printName(x);
    return false;
  }

  @Override
  public boolean visit(@Nonnull JThisRef x) {
    printName(x.getTarget());
    return false;
  }

  @Override
  public boolean visit(@Nonnull JThrowStatement x) {
    print(CHARS_THROW);
    if (x.getExpr() != null) {
      space();
      accept(x.getExpr());
    }
    return false;
  }

  @Override
  public boolean visit(@Nonnull JTryStatement x) {
    print(CHARS_TRY);
    accept(x.getTryBlock());
    for (JCatchBlock catchBlock : x.getCatchBlocks()) {
      accept(catchBlock);
    }
    JBlock finallyBlock = x.getFinallyBlock();
    if (finallyBlock != null) {
      print(CHARS_FINALLY);
      accept(finallyBlock);
    }
    return false;
  }

  @Override
  public boolean visit(@Nonnull JWhileStatement x) {
    print(CHARS_WHILE);
    lparen();
    accept(x.getTestExpr());
    rparen();
    if (x.getBody() != null) {
      nestedStatementPush(x.getBody());
      accept(x.getBody());
      nestedStatementPop(x.getBody());
    }
    return false;
  }

  @Override
  public boolean visit(@Nonnull JLock x) {
    print(LOCK);
    lparen();
    accept(x.getLockExpr());
    rparen();
    return false;
  }

  @Override
  public boolean visit(@Nonnull JUnlock x) {
    print(UNLOCK);
    lparen();
    accept(x.getLockExpr());
    rparen();
    return false;
  }

  @Override
  public boolean visit(@Nonnull JSynchronizedBlock x) {
    print(SYNCHRONIZED_BLOCK);
    lparen();
    accept(x.getLockExpr());
    rparen();
    space();
    accept(x.getSynchronizedBlock());
    return false;
  }

  protected void closeBlock() {
    indentOut();
    print('}');
  }

  protected void lparen() {
    print('(');
  }

  protected void nestedStatementPop(JStatement statement) {
    if (!(statement instanceof JBlock)) {
      indentOut();
    }
  }

  protected void nestedStatementPush(JStatement statement) {
    if (!(statement instanceof JBlock)) {
      indentIn();
      newline();
    } else {
      space();
    }
  }

  protected void openBlock() {
    print('{');
    indentIn();
    newline();
  }

  protected boolean parenPop(int parentPrec, JExpression child) {
    int childPrec = JavaPrecedenceVisitor.exec(child);
    if (parentPrec < childPrec) {
      rparen();
      return true;
    } else {
      return false;
    }
  }

  protected boolean parenPop(JExpression parent, JExpression child) {
    return parenPop(JavaPrecedenceVisitor.exec(parent), child);
  }

  protected boolean parenPush(int parentPrec, JExpression child) {
    int childPrec = JavaPrecedenceVisitor.exec(child);
    if (parentPrec < childPrec) {
      lparen();
      return true;
    } else {
      return false;
    }
  }

  protected boolean parenPush(JExpression parent, JExpression child) {
    return parenPush(JavaPrecedenceVisitor.exec(parent), child);
  }

  protected void printTypeFlags(JDefinedClassOrInterface declaredType) {
    int modifier = declaredType.getModifier();
    String modifierStr = JModifier.getStringTypeModifier(modifier);
    print(modifierStr);
  }

  protected void printAbstractFlag(CanBeAbstract x) {
    if (x.isAbstract()) {
      print(CHARS_ABSTRACT);
    }
  }

  protected void printBooleanLiteral(boolean value) {
    print(value ? CHARS_TRUE : CHARS_FALSE);
  }

  protected void printChar(char c) {
    switch (c) {
      case '\b':
        print("\\b");
        break;
      case '\t':
        print("\\t");
        break;
      case '\n':
        print("\\n");
        break;
      case '\f':
        print("\\f");
        break;
      case '\r':
        print("\\r");
        break;
      case '\"':
        print("\\\"");
        break;
      case '\'':
        print("\\'");
        break;
      case '\\':
        print("\\\\");
        break;
      default:
        if (Character.isISOControl(c)) {
          print("\\u");
          if (c < 0x1000) {
            print('0');
          }

          if (c < 0x100) {
            print('0');
          }

          if (c < 0x10) {
            print('0');
          }
          print(Integer.toHexString(c));
        } else {
          print(c);
        }
    }
  }

  protected void printCharLiteral(char value) {
    print('\'');
    printChar(value);
    print('\'');
  }

  protected void printDoubleLiteral(double value) {
    print(Double.toString(value));
  }

  protected void printFinalFlag(CanBeFinal x) {
    if (x.isFinal()) {
      print(CHARS_FINAL);
    }
  }

  protected void printFloatLiteral(float value) {
    print(Float.toString(value));
    print('f');
  }

  protected void printLongLiteral(long value) {
    print(Long.toString(value));
    print('L');
  }

  protected void printMethodHeader(JMethod x) {
    printAnnotationLiterals(x.getAnnotations());

    // Modifiers
    print(JModifier.getStringMethodModifier(x.getModifier()));
    printType(x);
    space();
    printName(x);

    // Parameters
    printParameterList(x);
  }

  private void printAnnotationLiterals(Collection<JAnnotation> annotation) {
    List<JAnnotation> annotations = new ArrayList<JAnnotation>(annotation);
    Collections.sort(annotations, new Comparator<JAnnotation>() {
      @Override
      public int compare(JAnnotation annotation1, JAnnotation annotation2) {
        return (Jack.getLookupFormatter().getName(annotation1.getType()).compareTo(
            Jack.getLookupFormatter().getName(annotation2.getType())));
      }
    });
    for (JAnnotation annotationLiteral : annotations) {
      accept(annotationLiteral);
      space();
    }
  }

  protected void printName(HasName x) {
    String name = x.getName();
    if (name == null) {
      print(CHARS_NONAME);
    } else {
      print(name.replace("/", "."));
    }
  }

  protected void printNativeFlag(CanBeNative x) {
    if (x.isNative()) {
      print(CHARS_NATIVE);
    }
  }

  protected void printParameterList(JMethod x) {
    lparen();
    visitCollectionWithCommas(x.getParams().iterator());
    rparen();
  }

  protected void printStaticFlag(CanBeStatic x) {
    if (x.isStatic()) {
      print(CHARS_STATIC);
    }
  }

  protected void printStringLiteral(String string) {
    char[] s = string.toCharArray();
    print('\"');
    for (int i = 0; i < s.length; ++i) {
      printChar(s[i]);
    }
    print('\"');
  }

  protected void printType(HasType hasType) {
    printTypeName(hasType.getType());
  }

  protected void printTypeName(JType type) {
    print(formatter.getName(type));
  }

  protected void rparen() {
    print(')');
  }

  protected void semi() {
    print(';');
  }

  protected boolean shouldPrintMethodBody() {
    return false;
  }

  protected void space() {
    print(' ');
  }

  protected void visitCollectionWithCommas(Iterator<? extends JNode> iter) {
    if (iter.hasNext()) {
      accept(iter.next());
    }
    while (iter.hasNext()) {
      print(CHARS_COMMA);
      accept(iter.next());
    }
  }
}
