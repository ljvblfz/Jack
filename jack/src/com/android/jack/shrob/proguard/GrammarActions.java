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

package com.android.jack.shrob.proguard;

import com.android.jack.ir.formatter.BinarySignatureFormatter;
import com.android.jack.ir.formatter.SourceFormatter;
import com.android.jack.ir.formatter.TypeAndMethodFormatter;
import com.android.jack.ir.formatter.TypePackageAndMethodFormatter;
import com.android.jack.shrob.spec.AnnotationSpecification;
import com.android.jack.shrob.spec.ClassSpecification;
import com.android.jack.shrob.spec.ClassTypeSpecification;
import com.android.jack.shrob.spec.ClassTypeSpecification.TypeEnum;
import com.android.jack.shrob.spec.FieldSpecification;
import com.android.jack.shrob.spec.FilterSpecification;
import com.android.jack.shrob.spec.Flags;
import com.android.jack.shrob.spec.InheritanceSpecification;
import com.android.jack.shrob.spec.KeepModifier;
import com.android.jack.shrob.spec.MethodSpecification;
import com.android.jack.shrob.spec.ModifierSpecification;
import com.android.jack.shrob.spec.ModifierSpecification.AccessFlags;
import com.android.jack.shrob.spec.ModifierSpecification.Modifier;
import com.android.jack.shrob.spec.NameSpecification;
import com.android.jack.util.NamingTools;
import com.android.sched.util.file.AbstractStreamFile;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.log.LoggerFactory;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Factory creating the specification using the output of the parser.
 */
// JSR305 annotation are commented out when there should be an annotation but
// the generated grammar code does not allow the code analyzer to validate it.
public class GrammarActions {

  public static final char SHROB_REGEX_PATH_SEPARATOR = '/';

  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private static final TypeAndMethodFormatter signatureFormatter =
      BinarySignatureFormatter.getFormatter();

  @Nonnull
  private static final TypePackageAndMethodFormatter sourceFormatter =
      SourceFormatter.getFormatter();

  enum FilterSeparator {
    GENERAL(".", "[^./]*"),
    FILE(".", "[^/]*"),
    CLASS("[^.]", "[^.]*"),
    ATTRIBUTE(".", ".*");

    /**
     * Represents the pattern equivalent to Proguard's "?"
     */
    @Nonnull
    private final String singleCharWilcard;

    /**
     * Represents the pattern equivalent to Proguard's "*"
     */
    @Nonnull
    private final String multipleCharWildcard;

    FilterSeparator(@Nonnull String singleCharWilcard, @Nonnull String multipleCharWildcard) {
      this.singleCharWilcard = singleCharWilcard;
      this.multipleCharWildcard = multipleCharWildcard;
    }
  }

  private GrammarActions() {
  }

  @Nonnull
  public static TypeAndMethodFormatter getSignatureFormatter() {
    return signatureFormatter;
  }

  @Nonnull
  public static TypePackageAndMethodFormatter getSourceFormatter() {
    return sourceFormatter;
  }

  @Nonnull
  public static String getSignature(@Nonnull String name) {
    assert name != null;
    if (name.contains("[")) {
      String nameWithoutArray = name.substring(0, name.lastIndexOf('['));
      return '[' + getSignature(nameWithoutArray);
    }
    StringBuilder sig = new StringBuilder();
    if (name.equals("boolean")) {
      sig.append("Z");
    } else if (name.equals("byte")) {
      sig.append("B");
    } else if (name.equals("char")) {
      sig.append("C");
    } else if (name.equals("short")) {
      sig.append("S");
    } else if (name.equals("int")) {
      sig.append("I");
    } else if (name.equals("float")) {
      sig.append("F");
    } else if (name.equals("double")) {
      sig.append("D");
    } else if (name.equals("long")) {
      sig.append("J");
    } else if (name.equals("void")) {
      sig.append("V");
    } else {
      sig.append(NamingTools.getTypeSignatureName(name));
    }

    return sig.toString();
  }

  @Nonnull
  static String getSignature(@Nonnull String name, int dim) {
    assert name != null;

    StringBuilder sig = new StringBuilder();

    for (int i = 0; i < dim; i++) {
      sig.append("\\[");
    }

    // ... matches any number of arguments of any type
    if (name.equals("...")) {
      sig.append(".*");
      // *** matches any type (primitive or non-primitive, array or non-array)
    } else if (name.equals("***")) {
      sig.append(".*");
      // % matches any primitive type ("boolean", "int", etc, but not "void")
    } else if (name.equals("%")) {
      sig.append("(B|C|D|F|I|J|S|Z)");
    } else if (name.equals("boolean")) {
      sig.append("Z");
    } else if (name.equals("byte")) {
      sig.append("B");
    } else if (name.equals("char")) {
      sig.append("C");
    } else if (name.equals("short")) {
      sig.append("S");
    } else if (name.equals("int")) {
      sig.append("I");
    } else if (name.equals("float")) {
      sig.append("F");
    } else if (name.equals("double")) {
      sig.append("D");
    } else if (name.equals("long")) {
      sig.append("J");
    } else if (name.equals("void")) {
      sig.append("V");
    } else {
      sig.append(
          convertNameToPattern(NamingTools.getTypeSignatureName(name), FilterSeparator.CLASS));
    }

    return sig.toString();
  }

  @Nonnull
  private static String convertNameToPattern(
      @Nonnull String name, @Nonnull FilterSeparator separator) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      switch (c) {
        case '?':
          sb.append(separator.singleCharWilcard);
          break;
        case '*':
          int j = i + 1;
          if (j < name.length() && name.charAt(j) == '*') {
            // ** matches any part of a name, possibly containing
            // any number of package separators or directory separators
            sb.append(".*");
            i++;
          } else {
            // * matches any part of a name not containing
            // the package separator or directory separator
            sb.append(separator.multipleCharWildcard);
          }
          break;
        case '$':
          sb.append("\\$");
          break;
        default:
          sb.append(c);
          break;
      }
    }
    return sb.toString();
  }

  @Nonnull
  static NameSpecification name(/*@Nonnull*/ String name, @Nonnull FilterSeparator separator) {
    assert name != null;
    String transformedName = "^" +
        convertNameToPattern(name, separator) + "$";

    Pattern pattern = Pattern.compile(transformedName);
    return new NameSpecification(pattern);
  }

  static void addModifier(@Nonnull ModifierSpecification modSpec,
      @Nonnull Modifier modifier, boolean hasNegator) {
    modSpec.addModifier(modifier, hasNegator);
  }

  static void addAccessFlag(@Nonnull ModifierSpecification modSpec,
      @Nonnull AccessFlags accessFlag, boolean hasNegator) {
    modSpec.addAccessFlag(accessFlag, hasNegator);
  }


  @Nonnull
  static ClassTypeSpecification classType(/*@Nonnull*/ TypeEnum type, boolean hasNegator) {
    assert type != null;
    ClassTypeSpecification classSpec = new ClassTypeSpecification(type);
    classSpec.setNegator(hasNegator);
    return classSpec;
  }

  @Nonnull
  static InheritanceSpecification createInheritance(
      /*@Nonnull*/ String className, boolean hasNameNegator,
      @CheckForNull AnnotationSpecification annotationType) {
    NameSpecification nameSpec = name(className, FilterSeparator.CLASS);
    nameSpec.setNegator(hasNameNegator);
    return new InheritanceSpecification(nameSpec, annotationType);
  }

  @Nonnull
  static AnnotationSpecification annotation(/*@Nonnull*/ String annotationName,
      boolean hasNameNegator) {
    NameSpecification name = name(annotationName, FilterSeparator.CLASS);
    name.setNegator(hasNameNegator);
    return new AnnotationSpecification(name);
  }

  @Nonnull
  static ClassSpecification classSpec(/* @Nonnull */
      List<NameSpecification> classNames,
      @Nonnull ClassTypeSpecification classType, /* @Nonnull */
      AnnotationSpecification annotation, @Nonnull ModifierSpecification modifier) {
    ClassSpecification classSpec = new ClassSpecification(classNames, classType, annotation);
    classSpec.setModifier(modifier);
    return classSpec;
  }

  static NameSpecification className(/* @Nonnull */ String name, boolean hasNameNegator) {
    NameSpecification nameSpec;
    if (name.equals("*")) {
      nameSpec = name("**", FilterSeparator.CLASS);
    } else {
      nameSpec = name(name, FilterSeparator.CLASS);
    }
    nameSpec.setNegator(hasNameNegator);
    return nameSpec;
  }

  static void method(@Nonnull ClassSpecification classSpec,
      @CheckForNull AnnotationSpecification annotationType,
      @CheckForNull String typeSig, /*@Nonnull*/ String name, @Nonnull String signature,
      @CheckForNull ModifierSpecification modifier) {
    assert name != null;
    String fullName = "^" + convertNameToPattern(name, FilterSeparator.CLASS);
    fullName += signature;
    if (typeSig != null) {
      fullName += typeSig;
    } else {
      fullName += "V";
    }
    fullName += "$";
    Pattern pattern = Pattern.compile(fullName);
    classSpec.add(new MethodSpecification(new NameSpecification(pattern),
        modifier, annotationType));
  }

  static void fieldOrAnyMember(@Nonnull ClassSpecification classSpec,
      @CheckForNull AnnotationSpecification annotationType, @CheckForNull String typeSig,
      /*@Nonnull*/ String name, @Nonnull ModifierSpecification modifier) {
    assert name != null;
    if (typeSig == null) {
      assert name.equals("*");
      // This is the "any member" case, we have to handle methods as well.
      method(classSpec,
          annotationType,
          getSignature("***", 0),
          "*",
          "\\(" + getSignature("...", 0) + "\\)",
          modifier);
    }
    field(classSpec, annotationType, typeSig, name, modifier);
  }

  static void field(@Nonnull ClassSpecification classSpec,
      @CheckForNull AnnotationSpecification annotationType, @CheckForNull String typeSig,
      /*@Nonnull*/ String name, @Nonnull ModifierSpecification modifier) {
    assert name != null;
    NameSpecification typeSignature = null;
    if (typeSig != null) {
      typeSignature = name(typeSig, FilterSeparator.CLASS);
    } else {
      assert name.equals("*");
    }
    classSpec.add(
        new FieldSpecification(
            name(name, FilterSeparator.GENERAL), modifier, typeSignature, annotationType));
  }

  @CheckForNull
  private static ProguardParser createParserFromFile(@Nonnull File file)
      throws ProguardFileParsingException {
    try {
      AbstractStreamFile.check(file, new FileLocation(file.getPath()));
      ProguardParser parser = createParserCommon(new ANTLRFileStream(file.getPath()));
      return parser;
    } catch (NoSuchFileException e) {
      throw new ProguardFileParsingException(e);
    } catch (NotFileException e) {
      throw new ProguardFileParsingException(e);
    } catch (IOException e) {
      throw new ProguardFileParsingException(e);
    }
  }

  @Nonnull
  private static ProguardParser createParserCommon(@Nonnull CharStream stream) {
    ProguardLexer lexer = new ProguardLexer(stream);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    ProguardParser parser = new ProguardParser(tokens);
    return parser;
  }

  public static void parse(/*@Nonnull*/ String proguardFileName,
      /*@Nonnull*/ String baseDir, @Nonnull Flags flags) throws RecognitionException,
  ProguardFileParsingException {
    assert proguardFileName != null;
    assert baseDir != null;
    File proguardFile = getFileFromBaseDir(baseDir, proguardFileName);
    ProguardParser parser = createParserFromFile(proguardFile);
    if (parser != null) {
      try {
        parser.prog(flags, proguardFile.getParentFile().getPath());
      } catch (RecoverableRecognitionException e) {
        // A recoverable error happened, but we do not want to ignore it.
        throw e.getCause();
      }
    }
  }

  static void addKeepClassMembers(
      @Nonnull Flags flags,
      /*@Nonnull*/ ClassSpecification classSpecification,
      @Nonnull KeepModifier keepModifier) {
    assert classSpecification != null;
    classSpecification.setKeepModifier(keepModifier);
    flags.addKeepClassMembers(classSpecification);
  }

  static void addKeepClassSpecification(
      @Nonnull Flags flags,
      /*@Nonnull*/ ClassSpecification classSpecification,
      @Nonnull KeepModifier keepModifier) {
    assert classSpecification != null;
    classSpecification.setKeepModifier(keepModifier);
    flags.addKeepClassSpecification(classSpecification);
  }

  static void addKeepClassesWithMembers(
      @Nonnull Flags flags,
      /*@Nonnull*/ ClassSpecification classSpecification,
      @Nonnull KeepModifier keepModifier) {
    assert classSpecification != null;
    classSpecification.setKeepModifier(keepModifier);
    flags.addKeepClassesWithMembers(classSpecification);
  }

  static void mapping(@Nonnull Flags flags, /*@Nonnull*/ String baseDir,
      /*@Nonnull*/ String mappingFilename) {
    assert mappingFilename != null;
    assert baseDir != null;
    File mappingFile = getFileFromBaseDir(baseDir, mappingFilename);
    if (!mappingFile.exists()) {
      logger.log(Level.WARNING, "Mapping file {0} not found", mappingFile.getPath());
    } else {
      flags.setObfuscationMapping(mappingFile);
    }
  }

  static void filter(
      @Nonnull List<FilterSpecification> filter,
      boolean negator, /*@Nonnull*/
      String filterName,
      @Nonnull FilterSeparator separator) {
    assert filterName != null;
    filter.add(new FilterSpecification(name(filterName, separator), negator));
  }

  static void attributeFilter(
      @Nonnull Flags flags, @Nonnull List<FilterSpecification> attributeSpec) {
    flags.addAllKeepAttribute(attributeSpec);
  }

  static void packageFilter(
      @Nonnull Flags flags, @Nonnull List<FilterSpecification> packageSpec) {
    flags.addAllKeepPackageName(packageSpec);
  }


  static void obfuscationDictionary(@Nonnull Flags flags, /*@Nonnull*/ String baseDir,
      /*@Nonnull*/ String fileName) {
    assert fileName != null;
    assert baseDir != null;
    File dictionary = getFileFromBaseDir(baseDir, fileName);
    if (!dictionary.exists()) {
      throw new AssertionError(dictionary.getPath() + " not found");
    }
    flags.setObfuscationDictionary(dictionary);
  }

  static void classObfuscationDictionary(
      @Nonnull Flags flags, /*@Nonnull*/ String baseDir, /*@Nonnull*/ String fileName) {
    assert fileName != null;
    assert baseDir != null;
    File dictionary = getFileFromBaseDir(baseDir, fileName);
    if (!dictionary.exists()) {
      throw new AssertionError(dictionary.getPath() + " not found");
    }
    flags.setClassObfuscationDictionary(dictionary);
  }

  static void packageObfuscationDictionary(
      @Nonnull Flags flags, /*@Nonnull*/ String baseDir, /*@Nonnull*/ String fileName) {
    assert fileName != null;
    assert baseDir != null;
    File dictionary = getFileFromBaseDir(baseDir, fileName);
    if (!dictionary.exists()) {
      throw new AssertionError(dictionary.getPath() + " not found");
    }
    flags.setPackageObfuscationDictionary(dictionary);
  }

  static void inJars(
      @Nonnull Flags flags, /*@Nonnull*/ String baseDir, /*@Nonnull*/ String inJars) {
    assert inJars != null;
    assert baseDir != null;
    List<File> pathList = getPathFromBaseDirAsList(baseDir, inJars);
    flags.addInJars(pathList);
  }

  static void outJars(
      @Nonnull Flags flags, /*@Nonnull*/ String baseDir, /*@Nonnull*/ String outJars) {
    assert outJars != null;
    assert baseDir != null;
    List<File> pathList = getPathFromBaseDirAsList(baseDir, outJars);
    flags.addOutJars(pathList);
    if (flags.getOutJars().size() != 1) {
      throw new AssertionError("Only one archive supported with -outjars for now");
    }
  }

  static void libraryJars(
      @Nonnull Flags flags, /*@Nonnull*/ String baseDir, /*@Nonnull*/ String libraryJars) {
    assert libraryJars != null;
    assert baseDir != null;
    flags.addLibraryJars(getPathFromBaseDir(baseDir, libraryJars));
  }

  static void outputMapping(@Nonnull Flags flags, /*@Nonnull*/ String baseDir,
      @CheckForNull String outputMapping) {
    assert baseDir != null;
    flags.setPrintMapping(true);
    if (outputMapping != null) {
      File mappingFile = getFileFromBaseDir(baseDir, outputMapping);
      flags.setOutputMapping(mappingFile);
    }
  }

  static void repackageClasses(@Nonnull Flags flags, @CheckForNull String newPackage) {
    // TODO(delphinemartin): error when newPackage contains '*' or '?'
    if (newPackage == null) {
      newPackage = "";
    }
    flags.setPackageForRenamedClasses(newPackage);
    flags.addKeepPackageNames(name(newPackage, FilterSeparator.GENERAL), false);
  }

  static void flattenPackageHierarchy(
      @Nonnull Flags flags, @CheckForNull String newPackage) {
    // TODO(delphinemartin): error when newPackage contains '*' or '?'
    if (newPackage == null) {
      newPackage = "";
    }
    flags.setPackageForFlatHierarchy(newPackage);
    flags.addKeepPackageNames(name(newPackage, FilterSeparator.GENERAL), false);
  }

  static void dontUseMixedCaseClassnames(@Nonnull Flags flags) {
    flags.setUseMixedCaseClassName(false);
  }


  static void useUniqueClassMemberNames(@Nonnull Flags flags) {
    flags.setUseUniqueClassMemberNames(true);
  }

  @Nonnull
  private static File getFileFromBaseDir(@Nonnull String baseDir, @Nonnull String path) {
    File file = new File(path);
    if (!file.isAbsolute()) {
      file = new File(baseDir, path);
    }
    return file;
  }

  @Nonnull
  private static String getPathFromBaseDir(@Nonnull String baseDir, @Nonnull String path) {
    List<File> pathList = getPathFromBaseDirAsList(baseDir, path);
    StringBuilder sb = new StringBuilder();
    for (Iterator<File> iter = pathList.iterator(); iter.hasNext(); ) {
      sb.append(iter.next().getPath());
      if (iter.hasNext()) {
        sb.append(File.pathSeparatorChar);
      }
    }
    return sb.toString();
  }

  @Nonnull
  private static List<File> getPathFromBaseDirAsList(
      @Nonnull String baseDir, @Nonnull String path) {
    String[] pathElements = path.split(File.pathSeparator);
    List<File> pathList = new ArrayList<File>(pathElements.length);
    for (String pathElement : pathElements) {
      pathList.add(getFileFromBaseDir(baseDir, pathElement));
    }
    return pathList;
  }

  static void adaptClassStrings(@Nonnull Flags flags, @Nonnull List<FilterSpecification> filter) {
    flags.addAdaptClassStrings(filter);
  }

  static void printUnsupportedFlag(/*@Nonnull*/ @SuppressWarnings("unused") String flag) {
  }

  static void printseeds(@Nonnull Flags flags, /* @Nonnull */
      String baseDir, @CheckForNull String fileName) {
    assert baseDir != null;
    flags.setPrintSeeds(true);
    if (fileName != null) {
      flags.setSeedsFile(getFileFromBaseDir(baseDir, fileName));
    }
  }

  static void adaptResourceFileNames(@Nonnull Flags flags,
      @Nonnull List<FilterSpecification>  filter) {
    flags.adaptResourceFileNames(filter);
  }

  static void renameSourcefileAttribute(@Nonnull Flags flags,
      @CheckForNull String newSourceFileName) {
    if (newSourceFileName == null) {
      newSourceFileName = "";
    }
    flags.setRenameSourceFileAttribute(newSourceFileName);
  }

  static void adaptResourceFileContents(@Nonnull Flags flags,
      @Nonnull List<FilterSpecification> filter) {
    flags.adaptResourceFileContents(filter);
  }
}
