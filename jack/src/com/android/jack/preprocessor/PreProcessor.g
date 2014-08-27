/*
 * Copyright (C) 2014 The Android Open Source Project
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

grammar PreProcessor;
import PreProcessorL;

@header {
package com.android.jack.preprocessor;

import com.android.jack.ir.ast.HasModifier;
import com.android.jack.ir.ast.JDefinedAnnotation;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JType;

import java.util.Collection;

}

@lexer::header {
package com.android.jack.preprocessor;
}

@members {
  public RuleBuilder builder;
}

rules [JSession session] returns [Collection<Rule> rules]
@init {
    builder = new RuleBuilder(session);
    rules = new ArrayList<Rule>();
}
    :   (rule=namedRule {rules.add($rule.rule);})* EOF
    ;

namedRule returns [Rule rule]
    :   name=Identifier  ':'
        classSet=annotatedCoiSet
        {rule = new Rule($name.text, (Expression<Collection<?>, Scope>)(Object)$classSet.set);}
    ;

annotatedCoiSet returns [Expression<Collection<JClassOrInterface>, Scope> set]
    :   (classSet=coiSet {set=$coiSet.set;})
    |   (toAdd=addAnnotation annotated=annotatedCoiSet
        {set=new AddAnnotationExpression<JClassOrInterface>($toAdd.add, $annotated.set);})
    ;

set returns [Expression<Collection<?>, Scope> set]
    :   (classSet=coiSet {set=(Expression<Collection<?>, Scope>)(Object)$coiSet.set;})
    |   (fSet=fieldSet {set=(Expression<Collection<?>, Scope>)(Object)$fSet.set;})
    |   (mSet=methodSet {set=(Expression<Collection<?>, Scope>)(Object)$mSet.set;})
    |   (toAdd=addAnnotation annotated=set
        {set=(Expression<Collection<?>, Scope>)(Object)new AddAnnotationExpression<JNode>(
            $toAdd.add, (Expression<Collection<JNode>, Scope>)(Object)$annotated.set);})
    ;

addAnnotation returns [JDefinedAnnotation add]
    :  '@@' name=Identifier {add = builder.getAnnotation($name.text);}
    ;

typeSet returns [Expression<Collection<? extends JType>, Scope> set]
@init{
  int dim;
}
    :   STAR {set = (Expression<Collection<?extends JType>, Scope>)(Object)AnyTypeFilter.INSTANCE;}
    |   ({dim = 0;} nonArray=nonArraySet (('[]' {dim++;})+)?
            {set = builder.newTypeFilter($nonArray.set, dim);}
        )
    ;

nonArraySet returns [Expression<Collection<? extends JType>, Scope> set]
    :   (   classSet=coiSet
            {set = (Expression<Collection<?extends JType>, Scope>)(Object)$classSet.set;}
        )
    |   (   primitiveSet=primitiveTypeSet
            {set = (Expression<Collection<?extends JType>, Scope>)(Object)$primitiveSet.set;}
        )
    ;

primitiveTypeSet returns [PrimitiveTypeFilter set]
    :   (VOID {set = PrimitiveTypeFilter.VOID;})
    |   (BOOLEAN {set = PrimitiveTypeFilter.BOOLEAN;})
    |   (BYTE {set = PrimitiveTypeFilter.BYTE;})
    |   (CHAR {set = PrimitiveTypeFilter.CHAR;})
    |   (SHORT {set = PrimitiveTypeFilter.SHORT;})
    |   (INT {set = PrimitiveTypeFilter.INT;})
    |   (FLOAT {set = PrimitiveTypeFilter.FLOAT;})
    |   (LONG {set = PrimitiveTypeFilter.LONG;})
    |   (DOUBLE {set = PrimitiveTypeFilter.DOUBLE;})
     ;

coiSet returns [ClassFilter set]
    :   CLASS name=matchName {set = new ClassFilter(new NamePattern($name.text));}
    (   (extendsExpr=extendsExpression {set.setExtendsExpression($extendsExpr.expression);})
    |   (containsExpr=containsExpression {set.setContainsExpression($containsExpr.expression);})
    |   (annotates=annotateSets {set.setAnnotateSets($annotates.sets);})
    |   (mod=modifiers {set.setModifierExpression($mod.expression);})
    )*
    ;

fieldSet returns [FieldFilter set]
    :   FIELD declaredType=typeSet name=matchName
        {set = new FieldFilter(new NamePattern($name.text), $declaredType.set);}
        (mod=modifiers {set.setModifierExpression($mod.expression);})?
    ;

methodSet returns [MethodFilter set]
    :   METHOD declaredType=typeSet name=matchName
        {set = new MethodFilter(new NamePattern($name.text), $declaredType.set);}
        '('
        ( arg0=typeSet {set.addArg($arg0.set);}
        ( ',' argN=typeSet {set.addArg($argN.set);})*) ?
        ')'
        (mod=modifiers {set.setModifierExpression($mod.expression);})?
    ;

extendsExpression returns [Expression<Boolean, Scope> expression]
    :   'extends' '{' bExpression=booleanExpression '}' {expression = $bExpression.expression;}
    ;

containsExpression returns [Expression<Boolean, Scope> expression]
    :   CONTAINS '{' bExpression=booleanExpression '}' {expression = $bExpression.expression;}
    ;

modifiers returns [Expression<Boolean, HasModifier> expression]
    :   MODIFIERS '{' expr=modifierExpression {expression=$expr.expression;} '}'
    ;

annotateSets returns [Collection<Expression<Collection<?>, Scope>> sets]
@init{
    sets = new ArrayList<Expression<Collection<?>, Scope>>();
}
@after {
    assert sets != null;
}
    :   ANNOTATE '{' (aSet=set ';' {sets.add($aSet.set);})* '}'
    ;

modifierExpression returns [Expression<Boolean, HasModifier> expression]
@after {
    assert expression != null;
}
    :   expr=modifierOrExpression {expression=$expr.expression;}
  ;

modifierPrimary returns [Expression<Boolean, HasModifier> expression]
@after {
    assert expression != null;
}
    :   (expr=modifierParExpression {expression = $expr.expression;})
    |   (PRIVATE {expression = ModifierPrimaryExpression.PRIVATE;})
    |   (PACKAGE {expression = ModifierPrimaryExpression.PACKAGE;})
    |   (PROTECTED {expression = ModifierPrimaryExpression.PROTECTED;})
    |   (PUBLIC {expression = ModifierPrimaryExpression.PUBLIC;})
    |   (ABSTRACT {expression = ModifierPrimaryExpression.ABSTRACT;})
    |   (FINAL {expression = ModifierPrimaryExpression.FINAL;})
    |   (NATIVE {expression = ModifierPrimaryExpression.NATIVE;})
    |   (STATIC {expression = ModifierPrimaryExpression.STATIC;})
    |   (STRICT {expression = ModifierPrimaryExpression.STRICT;})
    |   (SYNCHRONIZED {expression = ModifierPrimaryExpression.SYNCHRONIZED;})
    |   (TRANSIENT {expression = ModifierPrimaryExpression.TRANSIENT;})
    |   (VOLATILE {expression = ModifierPrimaryExpression.VOLATILE;})
    ;

modifierParExpression returns [Expression<Boolean, HasModifier> expression]
@after {
    assert expression != null;
}
    :   '(' expr=modifierExpression ')' {expression = $expr.expression;}
    ;

modifierUnary returns [Expression<Boolean, HasModifier> expression]
@after {
    assert expression != null;
}
    :   (   '!' uExpr=modifierUnary
            {expression = new BooleanNotExpression<HasModifier>($uExpr.expression);}
        )
    |   (pExpr=modifierPrimary {expression = $pExpr.expression;})
    ;

modifierEqualsExpression returns [Expression<Boolean, HasModifier> expression]
@after {
    assert expression != null;
}
    :   left=modifierUnary {expression = $left.expression;}
        (   '==' right=modifierUnary
            {
                expression = new BooleanBinaryExpression<HasModifier>(expression,
                    BooleanBinaryOperator.EQUALS,
                    $left.expression);
            }
        )*
    ;

modifierNotEqualsExpression returns [Expression<Boolean, HasModifier> expression]
@after {
    assert expression != null;
}
    :   left=modifierEqualsExpression {expression = $left.expression;}
        ('!=' right=modifierEqualsExpression
            {
                expression = new BooleanBinaryExpression<HasModifier>(expression,
                    BooleanBinaryOperator.NOT_EQUALS,
                    $left.expression);
            }
        )*
    ;

modifierAndExpression returns [Expression<Boolean, HasModifier> expression]
@after {
    assert expression != null;
}
    :   left=modifierNotEqualsExpression {expression = $left.expression;}
        ('&' right=modifierNotEqualsExpression
            {
                expression = new BooleanBinaryExpression<HasModifier>(expression,
                    BooleanBinaryOperator.AND,
                    $left.expression);
            }
        )*
    ;

modifierOrExpression returns [Expression<Boolean, HasModifier> expression]
@after {
    assert expression != null;
}
    :   left=modifierAndExpression {expression = $left.expression;}
        ('|' right=modifierAndExpression
            {
                expression = new BooleanBinaryExpression<HasModifier>(expression,
                    BooleanBinaryOperator.OR,
                    $left.expression);
            }
        )*
    ;

booleanExpression returns [Expression<Boolean, Scope> expression]
@after {
    assert expression != null;
}
    :   bExpression=booleanOrExpression {expression = $bExpression.expression;}
    ;

booleanPrimaryExpression returns [Expression<Boolean, Scope> expression]
@after {
    assert expression != null;
}
    :   (parExpression=booleanParExpression {expression = $parExpression.expression;})
    |   ('true' {expression = BooleanExpression.getTrue();})
    |   ('false' {expression = BooleanExpression.getFalse();})
    |   (aSet=set {expression = new IsNotEmpty($aSet.set);})
    ;

booleanParExpression returns [Expression<Boolean, Scope> expression]
@after {
    assert expression != null;
}
    :   '(' bExpression=booleanExpression ')' {expression=$bExpression.expression;}
    ;

booleanUnaryExpression returns [Expression<Boolean, Scope> expression]
@after {
    assert expression != null;
}
    :   ('!' uExpression=booleanUnaryExpression
          {expression = new BooleanNotExpression<Scope>($uExpression.expression);}
        )
    |   (pExpression=booleanPrimaryExpression {expression = $pExpression.expression;})
    ;

booleanEqualsExpression returns [Expression<Boolean, Scope> expression]
@after {
    assert expression != null;
}
    :   left=booleanUnaryExpression {expression = $left.expression;}
        ('==' right=booleanUnaryExpression
            {
                expression = new BooleanBinaryExpression<Scope>(expression,
                    BooleanBinaryOperator.EQUALS,
                    $right.expression);
            }
        )*
    ;

booleanNotEqualsExpression returns [Expression<Boolean, Scope> expression]
@after {
    assert expression != null;
}
    :   left=booleanEqualsExpression {expression = $left.expression;}
        ('!=' right=booleanEqualsExpression
            {
                expression = new BooleanBinaryExpression<Scope>(expression,
                    BooleanBinaryOperator.NOT_EQUALS,
                    $right.expression);
            }
        )*
    ;

booleanAndExpression returns [Expression<Boolean, Scope> expression]
@after {
    assert expression != null;
}
    :   left=booleanNotEqualsExpression {expression = $left.expression;}
        ('&' right=booleanNotEqualsExpression
            {
                expression = new BooleanBinaryExpression<Scope>(expression,
                    BooleanBinaryOperator.AND,
                    $right.expression);
            }
        )*
    ;

booleanOrExpression returns [Expression<Boolean, Scope> expression]
@after {
    assert expression != null;
}
    :   left=booleanAndExpression {expression = $left.expression;}
        ('|' right=booleanAndExpression
            {
                expression = new BooleanBinaryExpression<Scope>(expression,
                    BooleanBinaryOperator.OR,
                    $right.expression);
            }
        )*
    ;

matchName
    :   STAR
    |   CONTAINS
    |   ANNOTATE
    |   TYPE
    |   FIELD
    |   METHOD
    |   MODIFIERS
    |   KIND
    |   NAME
    |   Identifier
    ;

