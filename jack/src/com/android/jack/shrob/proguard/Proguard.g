grammar Proguard;

options{
  k = 3;
}

tokens {
  NEGATOR = '!';
}

@header {
package com.android.jack.shrob.proguard;

import com.android.jack.shrob.spec.*;
import com.android.jack.shrob.spec.ClassTypeSpecification.TypeEnum;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.shrob.proguard.GrammarActions;
}

@lexer::header {
package com.android.jack.shrob.proguard;

import com.android.jack.ir.ast.JModifier;
import com.android.jack.shrob.spec.KeepModifier;
}

@members {
@Override
protected Object recoverFromMismatchedToken(IntStream input, int ttype, BitSet follow) throws RecognitionException {
    throw new RecognitionException(input);
}

@Override
public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
    throw new RecoverableRecognitionException(e);
}
}

@lexer::members {
@Override
protected Object recoverFromMismatchedToken(IntStream input, int ttype, BitSet follow) throws RecognitionException {
    throw new RecognitionException(input);
}

@Override
public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
    throw new RecoverableRecognitionException(e);
}
}

prog [Flags flags, String baseDirectory]
  :
  (
    ('-keepclassmembers' keepModifier=keepOptionModifier? classSpec=classSpecification {GrammarActions.addKeepClassMembers($flags, $classSpec.classSpec, $keepModifier.modifier);})
    | ('-keepclasseswithmembers' keepModifier=keepOptionModifier? classSpec=classSpecification {GrammarActions.addKeepClassesWithMembers($flags, $classSpec.classSpec, $keepModifier.modifier);})
    | ('-keep' keepModifier=keepOptionModifier? classSpec=classSpecification {GrammarActions.addKeepClassSpecification($flags, $classSpec.classSpec, $keepModifier.modifier);})
    | '-dontshrink' {$flags.setShrink(false);}
    | ('-keepclassmembernames' classSpec=classSpecification  {GrammarActions.addKeepClassMembers($flags, $classSpec.classSpec, KeepModifier.ALLOW_SHRINKING);})
    | ('-keepclasseswithmembernames' classSpec=classSpecification  {GrammarActions.addKeepClassesWithMembers($flags, $classSpec.classSpec, KeepModifier.ALLOW_SHRINKING);})
    | ('-keepnames' classSpec=classSpecification {GrammarActions.addKeepClassSpecification($flags, $classSpec.classSpec, KeepModifier.ALLOW_SHRINKING);})
    | '-dontobfuscate' {$flags.setObfuscate(false);}
    | ('-include'|'@') proguardFile=NAME {GrammarActions.parse($proguardFile.text, baseDirectory, $flags);}
    | ('-basedirectory' baseDir=NAME {baseDirectory=$baseDir.text;})
    | '-injars' inJars=classpath {GrammarActions.inJars($flags, baseDirectory, $inJars.text);}
    | '-outjars' outJars=classpath {GrammarActions.outJars($flags, baseDirectory, $outJars.text);}
    | '-libraryjars' libraryJars=classpath {GrammarActions.libraryJars($flags, baseDirectory, $libraryJars.text);}
    | ('-applymapping' mapping=NAME {GrammarActions.mapping($flags, baseDirectory, $mapping.text);})
    | ('-keepattributes' {FilterSpecification attribute_filter = new FilterSpecification();} filter[attribute_filter] {GrammarActions.attributeFilter($flags, attribute_filter);})
    | '-keepparameternames' {$flags.setKeepParameterNames(true);}
    | '-obfuscationdictionary' obfuscationDictionary=NAME {GrammarActions.obfuscationDictionary($flags, baseDirectory, $obfuscationDictionary.text);}
    | '-classobfuscationdictionary' classObfuscationDictionary=NAME {GrammarActions.classObfuscationDictionary($flags, baseDirectory, $classObfuscationDictionary.text);}
    | '-packageobfuscationdictionary' packageObfuscationDictionary=NAME {GrammarActions.packageObfuscationDictionary($flags, baseDirectory, $packageObfuscationDictionary.text);}
    | '-printmapping' outputMapping=NAME? {GrammarActions.outputMapping($flags, baseDirectory, $outputMapping.text);}
    | ('-keeppackagenames' {FilterSpecification package_filter = new FilterSpecification();} filter[package_filter] {GrammarActions.packageFilter($flags, package_filter);})
    | ('-repackageclasses' ('\'' newPackage=NAME? '\'')? {GrammarActions.repackageClasses($flags, $newPackage.text);})
    | ('-flattenpackagehierarchy' ('\'' newPackage=NAME? '\'')? {GrammarActions.flattenPackageHierarchy($flags, $newPackage.text);})
    | '-dontusemixedcaseclassnames' {GrammarActions.dontUseMixedCaseClassnames($flags);}
    | '-useuniqueclassmembernames' {GrammarActions.useUniqueClassMemberNames($flags);}
    | ('-adaptclassstrings' {FilterSpecification filter = new FilterSpecification();} filter[filter] {GrammarActions.adaptClassStrings($flags, filter);})
    | ('-printseeds' seedOutputFile=NAME? {GrammarActions.printseeds($flags, baseDirectory, $seedOutputFile.text);})
    | unFlag=unsupportedFlag {GrammarActions.printUnsupportedFlag($unFlag.text);}
  )*
  EOF
  ;
  catch [RecognitionException e] {
    throw e;
  }

private unsupportedFlag
  :
  ('-skipnonpubliclibraryclasses'
    | '-dontskipnonpubliclibraryclasses'
    | '-dontskipnonpubliclibraryclassmembers'
    | ('-keepdirectories' {FilterSpecification directory_filter = new FilterSpecification();} filter[directory_filter])
    | ('-target' NAME) //version
    | '-forceprocessing'
    | ('-printusage' NAME) //[filename]
    | ('-whyareyoukeeping' classSpecification)
    | '-dontoptimize'
    | ('-optimizations' {FilterSpecification optimization_filter = new FilterSpecification();} filter[optimization_filter])
    | ('-optimizationpasses' NAME) //n
    | ('-assumenosideeffects' classSpecification)
    | '-allowaccessmodification'
    | '-mergeinterfacesaggressively'
    | '-overloadaggressively'
    | ('-renamesourcefileattribute' NAME?) //[string]
    | ('-adaptresourcefilenames' {FilterSpecification file_filter = new FilterSpecification();} filter[file_filter])
    | ('-adaptresourcefilecontents' {FilterSpecification file_filter = new FilterSpecification();} filter[file_filter])
    | '-dontpreverify'
    | '-microedition'
    | '-verbose'
    | ('-dontnote' {FilterSpecification class_filter = new FilterSpecification();} filter[class_filter])
    | ('-dontwarn' {FilterSpecification class_filter = new FilterSpecification();} filter[class_filter])
    | '-ignorewarnings'
    | ('-printconfiguration' NAME?) //[filename]
    | ('-dump' NAME?) //[filename]
  )
  ;

private classpath
  :  NAME (':' classpath)?
  ;

private filter [FilterSpecification filter]
  :
  nonEmptytFilter[filter]
  | {GrammarActions.filter($filter, false, "**");}
  ;


private nonEmptytFilter [FilterSpecification filter]
@init {
  boolean negator = false;
}
  :
  ((NEGATOR {negator=true;})? NAME {GrammarActions.filter($filter, negator, $NAME.text);} (',' nonEmptytFilter[filter])?)
  ;

private classSpecification returns [ClassSpecification classSpec]
@init{
  ModifierSpecification modifier = new ModifierSpecification();
}
  :
  (annotation)?
  cType=classModifierAndType[modifier]
  NAME {classSpec = GrammarActions.classSpec($NAME.text, cType, $annotation.annotSpec, modifier);}
  (inheritanceSpec=inheritance {classSpec.setInheritance(inheritanceSpec);})?
  members[classSpec]?
  ;

private classModifierAndType[ModifierSpecification modifier] returns [ClassTypeSpecification cType]
@init{
  boolean hasNegator = false;
}
  :
  (NEGATOR {hasNegator = true;})?
  (
  'public' {GrammarActions.addModifier(modifier, JModifier.PUBLIC, hasNegator);} cmat=classModifierAndType[modifier] {cType = $cmat.cType;}
  | 'abstract' {GrammarActions.addModifier(modifier, JModifier.ABSTRACT, hasNegator);} cmat=classModifierAndType[modifier] {cType = $cmat.cType;}
  | 'final' {GrammarActions.addModifier(modifier, JModifier.FINAL, hasNegator);} cmat=classModifierAndType[modifier] {cType = $cmat.cType;}
  | classType {cType=GrammarActions.classType($classType.type, hasNegator); }
  )
  ;

private classType returns [TypeEnum type]
  :
    'interface' {$type = TypeEnum.INTERFACE;}
  | 'enum' {$type = TypeEnum.ENUM;}
  | 'class' {$type = TypeEnum.CLASS;}
  ;

private members [ClassSpecification classSpec]
  :
  '{'
    member[classSpec]*
  '}'
  ;

private member [ClassSpecification classSpec]
  :
    annotation? modifiers
    (
      (typeSig=type)? name=(NAME|'<init>') (signature=arguments {GrammarActions.method(classSpec, $annotation.annotSpec, typeSig, $name.text, signature, $modifiers.modifiers);}
                  | {GrammarActions.fieldOrAnyMember(classSpec, $annotation.annotSpec, typeSig, $name.text, $modifiers.modifiers);})
      | '<methods>' {GrammarActions.method(classSpec, $annotation.annotSpec,
          GrammarActions.getSignature("***", 0), "*", "("+ GrammarActions.getSignature("...", 0) + ")",
          $modifiers.modifiers);}
      | '<fields>' {GrammarActions.field(classSpec, $annotation.annotSpec, null, "*", $modifiers.modifiers);}
    ) ';'
  ;

private annotation returns [AnnotationSpecification annotSpec]
  :  '@' NAME {$annotSpec = GrammarActions.annotation($NAME.text);};

private modifiers returns [ModifierSpecification modifiers]
@init{
  modifiers = new ModifierSpecification();
}
  :
  modifier[modifiers]*
  ;

private modifier [ModifierSpecification modifiers]
@init{
  boolean hasNegator = false;
}
  :
  (NEGATOR {hasNegator = true;})?
  (
    'public' {modifiers.addModifier(JModifier.PUBLIC, hasNegator);}
    | 'private' {modifiers.addModifier(JModifier.PRIVATE, hasNegator);}
    | 'protected' {modifiers.addModifier(JModifier.PROTECTED, hasNegator);}
    | 'static' {modifiers.addModifier(JModifier.STATIC, hasNegator);}
    | 'synchronized' {modifiers.addModifier(JModifier.SYNCHRONIZED, hasNegator);}
    | 'native' {modifiers.addModifier(JModifier.NATIVE, hasNegator);}
    | 'abstract' {modifiers.addModifier(JModifier.ABSTRACT, hasNegator);}
    | 'strictfp' {modifiers.addModifier(JModifier.STRICTFP, hasNegator);}
    | 'final' {modifiers.addModifier(JModifier.FINAL, hasNegator);}
    | 'transient' {modifiers.addModifier(JModifier.TRANSIENT, hasNegator);}
    | 'synthetic' {modifiers.addModifier(JModifier.SYNTHETIC, hasNegator);}
    | 'bridge' {modifiers.addModifier(JModifier.BRIDGE, hasNegator);}
    | 'varargs' {modifiers.addModifier(JModifier.VARARGS, hasNegator);}
  )
  ;

private inheritance returns [InheritanceSpecification inheritanceSpec]
  :
  ('extends' | 'implements')
  annotation? NAME {inheritanceSpec = GrammarActions.createInheritance($NAME.text, $annotation.annotSpec);};

private arguments returns [String signature]
  :
  '(' {signature = "(";}
    (
      (
        parameterSig=type {signature += parameterSig;}
        (',' parameterSig=type {signature += parameterSig;})*
        )?
      )
    ')' {signature += ")";}
  ;

private type returns [String signature]
@init {
  int dim = 0;
}
  :
  (
    typeName='%' {String sig = $typeName.text; signature = GrammarActions.getSignature(sig == null ? "" : sig, 0);}
    |
    (typeName=NAME ('[]' {dim++;})*  {String sig = $typeName.text; signature = GrammarActions.getSignature(sig == null ? "" : sig, dim);})
  )
  ;

private keepOptionModifier returns [KeepModifier modifier]
  : ','
  ('allowshrinking' {modifier = KeepModifier.ALLOW_SHRINKING;}
  | 'allowoptimization' // Optimizations not supported
  | 'allowobfuscation' {modifier = KeepModifier.ALLOW_OBFUSCATION;})
  ;

private NAME  : ('a'..'z'|'A'..'Z'|'_'|'0'..'9'|'?'|'$'|'.'|'*'|'/'|'-'|'<'|'>')+ ;

LINE_COMMENT
  :  '#' ~( '\r' | '\n' )* {$channel=HIDDEN;}
  ;

private WS  :   ( ' '
        | '\t'
        | '\r'
        | '\n'
        ) {$channel=HIDDEN;}
    ;