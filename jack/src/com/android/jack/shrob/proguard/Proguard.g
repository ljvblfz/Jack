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
import com.android.jack.shrob.spec.ModifierSpecification.*;
import com.android.jack.shrob.proguard.GrammarActions;
import com.android.jack.shrob.proguard.GrammarActions.FilterSeparator;
}

@lexer::header {
package com.android.jack.shrob.proguard;

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

prog [Flags flags, String baseDirectory] throws ProguardFileParsingException
  :
  (
    ('-keepclassmembers' keepModifier=keepOptionModifier classSpec=classSpecification {GrammarActions.addKeepClassMembers($flags, $classSpec.classSpec, $keepModifier.modifier);})
    | ('-keepclasseswithmembers' keepModifier=keepOptionModifier classSpec=classSpecification {GrammarActions.addKeepClassesWithMembers($flags, $classSpec.classSpec, $keepModifier.modifier);})
    | ('-keep' keepModifier=keepOptionModifier classSpec=classSpecification {GrammarActions.addKeepClassSpecification($flags, $classSpec.classSpec, $keepModifier.modifier);})
    | '-dontshrink' {$flags.setShrink(false);}
    | '-dontoptimize'  {$flags.setOptimize(false);}
    | '-dontpreverify'  {$flags.setPreverify(false);}
    | ('-keepclassmembernames' classSpec=classSpecification  {GrammarActions.addKeepClassMembers($flags, $classSpec.classSpec, new KeepModifier().setAllowShrinking());})
    | ('-keepclasseswithmembernames' classSpec=classSpecification  {GrammarActions.addKeepClassesWithMembers($flags, $classSpec.classSpec, new KeepModifier().setAllowShrinking());})
    | ('-keepnames' classSpec=classSpecification {GrammarActions.addKeepClassSpecification($flags, $classSpec.classSpec, new KeepModifier().setAllowShrinking());})
    | '-dontobfuscate' {$flags.setObfuscate(false);}
    | ('-include'|'@') proguardFile=NAME {GrammarActions.parse($proguardFile.text, baseDirectory, $flags);}
    | ('-basedirectory' baseDir=NAME {baseDirectory=$baseDir.text;})
    | '-injars' inJars=classpath {GrammarActions.inJars($flags, baseDirectory, $inJars.text);}
    | '-outjars' outJars=classpath {GrammarActions.outJars($flags, baseDirectory, $outJars.text);}
    | '-libraryjars' libraryJars=classpath {GrammarActions.libraryJars($flags, baseDirectory, $libraryJars.text);}
    | ('-applymapping' mapping=NAME {GrammarActions.mapping($flags, baseDirectory, $mapping.text);})
    | ('-keepattributes' {List<FilterSpecification> attribute_filter = new ArrayList<FilterSpecification>();} filter[attribute_filter, FilterSeparator.ATTRIBUTE] {GrammarActions.attributeFilter($flags, attribute_filter);})
    | '-keepparameternames' {$flags.setKeepParameterNames(true);}
    | '-obfuscationdictionary' obfuscationDictionary=NAME {GrammarActions.obfuscationDictionary($flags, baseDirectory, $obfuscationDictionary.text);}
    | '-classobfuscationdictionary' classObfuscationDictionary=NAME {GrammarActions.classObfuscationDictionary($flags, baseDirectory, $classObfuscationDictionary.text);}
    | '-packageobfuscationdictionary' packageObfuscationDictionary=NAME {GrammarActions.packageObfuscationDictionary($flags, baseDirectory, $packageObfuscationDictionary.text);}
    | '-printmapping' outputMapping=NAME? {GrammarActions.outputMapping($flags, baseDirectory, $outputMapping.text);}
    | ('-keeppackagenames' {List<FilterSpecification> package_filter = new ArrayList<FilterSpecification>();} filter[package_filter, FilterSeparator.GENERAL] {GrammarActions.packageFilter($flags, package_filter);})
    | ('-repackageclasses' ('\'' newPackage=NAME? '\'')? {GrammarActions.repackageClasses($flags, $newPackage.text); newPackage = null;})
    | ('-flattenpackagehierarchy' ('\'' newPackage=NAME? '\'')? {GrammarActions.flattenPackageHierarchy($flags, $newPackage.text); newPackage = null;})
    | '-dontusemixedcaseclassnames' {GrammarActions.dontUseMixedCaseClassnames($flags);}
    | '-useuniqueclassmembernames' {GrammarActions.useUniqueClassMemberNames($flags);}
    | ('-adaptclassstrings' {List<FilterSpecification> filter = new ArrayList<FilterSpecification>();} filter[filter, FilterSeparator.GENERAL] {GrammarActions.adaptClassStrings($flags, filter);})
    | ('-printseeds' seedOutputFile=NAME? {GrammarActions.printseeds($flags, baseDirectory, $seedOutputFile.text);})
    | ('-adaptresourcefilenames' {List<FilterSpecification> file_filter = new ArrayList<FilterSpecification>();} filter[file_filter, FilterSeparator.FILE] {GrammarActions.adaptResourceFileNames($flags, file_filter);})
    | ('-renamesourcefileattribute' sourceFile=NAME? {GrammarActions.renameSourcefileAttribute($flags, $sourceFile.text);})
    | ('-adaptresourcefilecontents' {List<FilterSpecification> file_filter = new ArrayList<FilterSpecification>();} filter[file_filter, FilterSeparator.FILE] {GrammarActions.adaptResourceFileContents($flags, file_filter);})
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
    | ('-keepdirectories' {List<FilterSpecification> directory_filter = new ArrayList<FilterSpecification>();} filter[directory_filter, FilterSeparator.FILE])
    | ('-target' NAME) //version
    | '-forceprocessing'
    | ('-printusage' NAME) //[filename]
    | ('-whyareyoukeeping' classSpecification)
    | ('-optimizations' {List<FilterSpecification> optimization_filter = new ArrayList<FilterSpecification>();} filter[optimization_filter, FilterSeparator.GENERAL])
    | ('-optimizationpasses' NAME) //n
    | ('-assumenosideeffects' classSpecification)
    | '-allowaccessmodification'
    | '-mergeinterfacesaggressively'
    | '-overloadaggressively'
    | '-microedition'
    | '-verbose'
    | ('-dontnote' {List<FilterSpecification> class_filter = new ArrayList<FilterSpecification>();} filter[class_filter, FilterSeparator.CLASS])
    | ('-dontwarn' {List<FilterSpecification> class_filter = new ArrayList<FilterSpecification>();} filter[class_filter, FilterSeparator.CLASS])
    | '-ignorewarnings'
    | ('-printconfiguration' NAME?) //[filename]
    | ('-dump' NAME?) //[filename]
  )
  ;

private classpath
  :  NAME ((':'|';') classpath)?
  ;

private filter [List<FilterSpecification> filter, FilterSeparator format]
  :
  nonEmptytFilter[filter, format]
  | {GrammarActions.filter($filter, false, "**", format);}
  ;


private nonEmptytFilter [List<FilterSpecification> filter, FilterSeparator separator]
@init {
  boolean negator = false;
}
  :
  ((NEGATOR {negator=true;})? NAME {GrammarActions.filter($filter, negator, $NAME.text, separator);} (','
    nonEmptytFilter[filter, separator])?)
  ;

private classSpecification returns [ClassSpecification classSpec]
@init{
  ModifierSpecification modifier = new ModifierSpecification();
}
  :
  (annotation)?
  cType=classModifierAndType[modifier]
  classNames {classSpec = GrammarActions.classSpec($classNames.names, cType,
    $annotation.annotSpec, modifier);}
  (inheritanceSpec=inheritance {classSpec.setInheritance(inheritanceSpec);})?
  members[classSpec]?
  ;

private classNames returns [List<NameSpecification> names]
@init{
  names = new ArrayList<NameSpecification>();
}
  :
  firstName=className {names.add($firstName.nameSpec);}
  (',' otherName=className {names.add($otherName.nameSpec);} )*
;

private className returns [NameSpecification nameSpec]
@init{
    boolean hasNameNegator = false;
}
  :
  (NEGATOR {hasNameNegator = true;})?
  NAME {nameSpec=GrammarActions.className($NAME.text, hasNameNegator);}
;

private classModifierAndType[ModifierSpecification modifier] returns [ClassTypeSpecification cType]
@init{
  boolean hasNegator = false;
}
  :
  (NEGATOR {hasNegator = true;})?
  (
  'public' {GrammarActions.addAccessFlag(modifier, AccessFlags.PUBLIC, hasNegator);} cmat=classModifierAndType[modifier] {cType = $cmat.cType;}
  | 'abstract' {GrammarActions.addModifier(modifier, Modifier.ABSTRACT, hasNegator);} cmat=classModifierAndType[modifier] {cType = $cmat.cType;}
  | 'final' {GrammarActions.addModifier(modifier, Modifier.FINAL, hasNegator);} cmat=classModifierAndType[modifier] {cType = $cmat.cType;}
  | '@' {GrammarActions.addModifier(modifier, Modifier.ANNOTATION, hasNegator);} cmat=classModifierAndType[modifier] {cType = $cmat.cType;}
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
                  | {assert $name != null; GrammarActions.fieldOrAnyMember(classSpec, $annotation.annotSpec, typeSig, $name.text, $modifiers.modifiers, $name.getInputStream());})
      | '<methods>' {GrammarActions.method(classSpec, $annotation.annotSpec,
          GrammarActions.getSignatureRegex("***", 0), "*", "\\("+ GrammarActions.getSignatureRegex("...", 0) + "\\)",
          $modifiers.modifiers);}
      | fields='<fields>' {GrammarActions.field(classSpec, $annotation.annotSpec, null, "*", $modifiers.modifiers, $fields.getInputStream());}
    ) ';'
  ;

private annotation returns [AnnotationSpecification annotSpec]
@init{
  boolean hasNameNegator = false;
}
  :  '@' (NEGATOR {hasNameNegator = true;})? NAME {$annotSpec = GrammarActions.annotation($NAME.text, hasNameNegator);};

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
    'public' {modifiers.addAccessFlag(AccessFlags.PUBLIC, hasNegator);}
    | 'private' {modifiers.addAccessFlag(AccessFlags.PRIVATE, hasNegator);}
    | 'protected' {modifiers.addAccessFlag(AccessFlags.PROTECTED, hasNegator);}
    | 'static' {modifiers.addModifier(Modifier.STATIC, hasNegator);}
    | 'synchronized' {modifiers.addModifier(Modifier.SYNCHRONIZED, hasNegator);}
    | 'native' {modifiers.addModifier(Modifier.NATIVE, hasNegator);}
    | 'abstract' {modifiers.addModifier(Modifier.ABSTRACT, hasNegator);}
    | 'strictfp' {modifiers.addModifier(Modifier.STRICTFP, hasNegator);}
    | 'final' {modifiers.addModifier(Modifier.FINAL, hasNegator);}
    | 'transient' {modifiers.addModifier(Modifier.TRANSIENT, hasNegator);}
    | 'synthetic' {modifiers.addModifier(Modifier.SYNTHETIC, hasNegator);}
    | 'bridge' {modifiers.addModifier(Modifier.BRIDGE, hasNegator);}
    | 'varargs' {modifiers.addModifier(Modifier.VARARGS, hasNegator);}
    | 'volatile' {modifiers.addModifier(Modifier.VOLATILE, hasNegator);}
  )
  ;

private inheritance returns [InheritanceSpecification inheritanceSpec]
@init{
  boolean hasNameNegator = false;
}
  :
  ('extends' | 'implements')
  annotation? (NEGATOR {hasNameNegator = true;})? NAME {inheritanceSpec = GrammarActions.createInheritance($NAME.text, hasNameNegator, $annotation.annotSpec);};

private arguments returns [String signature]
  :
  '(' {signature = "\\(";}
    (
      (
        parameterSig=type {signature += parameterSig;}
        (',' parameterSig=type {signature += parameterSig;})*
        )?
      )
    ')' {signature += "\\)";}
  ;

private type returns [String signature]
@init {
  int dim = 0;
}
  :
  (
    typeName=('%' | NAME) ('[]' {dim++;})*  {String sig = $typeName.text; signature = GrammarActions.getSignatureRegex(sig == null ? "" : sig, dim);}
  )
  ;

private keepOptionModifier returns [KeepModifier modifier]
@init {
  modifier = new KeepModifier();
}
  : (','
  ('allowshrinking' {modifier.setAllowShrinking();}
  | 'allowoptimization' // Optimizations not supported
  | 'allowobfuscation' {modifier.setAllowObfuscation();}))*
  ;

private NAME  : ('a'..'z'|'A'..'Z'|'_'|'0'..'9'|'?'|'$'|'.'|'*'|'/'|'\\'|'-'|'+'|'<'|'>'|':'|'~')+ ;

LINE_COMMENT
  :  '#' ~( '\r' | '\n' )* {$channel=HIDDEN;}
  ;

private WS  :   ( ' '
        | '\t'
        | '\r'
        | '\n'
        ) {$channel=HIDDEN;}
    ;
