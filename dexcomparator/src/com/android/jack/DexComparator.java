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

package com.android.jack;

import com.android.jack.comparator.DebugInfo;
import com.android.jack.comparator.DebugInfo.LocalVar;
import com.android.jack.dx.dex.file.DebugInfoDecoder;
import com.android.jack.dx.io.ClassData;
import com.android.jack.dx.io.ClassData.Method;
import com.android.jack.dx.io.ClassDef;
import com.android.jack.dx.io.Code;
import com.android.jack.dx.io.DexBuffer;
import com.android.jack.dx.io.FieldId;
import com.android.jack.dx.io.MethodId;
import com.android.jack.dx.io.ProtoId;
import com.android.jack.dx.rop.code.AccessFlags;
import com.android.jack.dx.rop.type.Prototype;
import com.android.jack.dx.util.ByteInput;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * This tool compares the structure of two dex files.
 */
public class DexComparator {

  private final Logger logger;
  private DexBuffer referenceDexFile;
  private DexBuffer candidateDexFile;
  private static final Level ERROR_LEVEL = Level.SEVERE;
  private static final Level WARNING_LEVEL = Level.WARNING;
  private static final Level DEBUG_LEVEL = Level.FINE;
  private boolean strict;
  private boolean enableDebugInfoComparison;
  private byte[] referenceData;
  private byte[] candidateData;
  private int refThisIndex;
  private int candidateThisIndex;
  private static final boolean IGNORE_ID_COMPARISON = true;
  private static final boolean IGNORE_ANONYMOUS_CLASSES = true;
  private static final boolean TOLERATE_MISSING_SYNTHETICS = true;
  private static final boolean TOLERATE_MISSING_INITS = true;
  private static final boolean TOLERATE_MISSING_CLINITS = true;
  private boolean enableBinaryDebugInfoComparison = false;
  private boolean enableInstructionNumberComparison = false;
  private float instructionNumberTolerance = 0f;
  private boolean enableBinaryCodeComparison = false;

  private static final List<String> skippedMethods = new ArrayList<String>();

  @Nonnull
  private static final String INIT_NAME = "<init>";
  @Nonnull
  private static final String STATIC_INIT_NAME = "<clinit>";

  public DexComparator() {
    logger = Logger.getLogger(this.getClass().getName());
    logger.setLevel(WARNING_LEVEL);
  }

  /**
   * Launch the comparison between a reference Dex {@code File} and a candidate Dex {@code File}.
   *
   * @param referenceFile the reference Dex {@code File}
   * @param candidateFile the candidate Dex {@code File}
   * @param compareDebugInfo also compare debug infos
   * @param strict if false, the candidate Dex must <i>at least<i/> contain all the structures of
   *        the reference Dex; if true, the candidate Dex must <i>exactly<i/> contain all the
   *        structures of the reference Dex
   * @param compareDebugInfoBinarily enable binary comparison of debug infos, allowed only if
   * compareDebugInfo is enabled
   * @param compareInstructionNumber enable comparison of number of instructions
   * @param instructionNumberTolerance tolerance factor for comparison of number of instructions
   * @throws DifferenceFoundException if a difference between the two Dex files is found
   * @throws IOException if an error occurs while loading the dex files
   */
  public void compare(@Nonnull File referenceFile,
      @Nonnull File candidateFile,
      boolean compareDebugInfo,
      boolean strict,
      boolean compareDebugInfoBinarily,
      boolean compareInstructionNumber,
      float instructionNumberTolerance) throws DifferenceFoundException, IOException {

    this.strict = strict;
    enableBinaryDebugInfoComparison = compareDebugInfoBinarily;
    enableInstructionNumberComparison = compareInstructionNumber;
    this.instructionNumberTolerance = instructionNumberTolerance;
    enableDebugInfoComparison = compareDebugInfo;

    compare(referenceFile, candidateFile);
  }

  /**
   * Launch the comparison between a reference Dex {@code File} and a candidate Dex {@code File}.
   *
   * @param referenceFile the reference Dex {@code File}
   * @param candidateFile the candidate Dex {@code File}
   * @param compareDebugInfo also compare debug infos
   * @param strict if false, the candidate Dex must <i>at least<i/> contain all the structures of
   *        the reference Dex; if true, the candidate Dex must <i>exactly<i/> contain all the
   *        structures of the reference Dex
   * @param compareDebugInfoBinarily enable binary comparison of debug infos, allowed only if
   * compareDebugInfo is enabled
   * @param compareCodeBinarily enable code binary comparison
   * @throws DifferenceFoundException if a difference between the two Dex files is found
   * @throws IOException if an error occurs while loading the dex files
   */
  public void compare(@Nonnull File referenceFile,
      @Nonnull File candidateFile,
      boolean compareDebugInfo,
      boolean strict,
      boolean compareDebugInfoBinarily,
      boolean compareCodeBinarily) throws DifferenceFoundException, IOException {

    this.strict = strict;
    enableBinaryDebugInfoComparison = compareDebugInfoBinarily;
    enableBinaryCodeComparison = compareCodeBinarily;
    enableDebugInfoComparison = compareDebugInfo;

    compare(referenceFile, candidateFile);
  }

  private void compare(@Nonnull File referenceFile, @Nonnull File candidateFile) throws IOException,
      DifferenceFoundException {

    if (enableBinaryDebugInfoComparison && !enableDebugInfoComparison) {
      throw new IllegalArgumentException(
          "Debug info binary comparison cannot be enabled if debug info comparison is not enabled");
    }

    referenceDexFile = new DexBuffer(referenceFile);
    referenceData = referenceDexFile.getBytes();
    refThisIndex = referenceDexFile.strings().indexOf("this");
    candidateDexFile = new DexBuffer(candidateFile);
    candidateData = candidateDexFile.getBytes();
    candidateThisIndex = candidateDexFile.strings().indexOf("this");

    if (!IGNORE_ID_COMPARISON) {
      checkStringIds();
      checkTypeIds();
      checkProtoIds();
      checkFieldIds();
      checkMethodIds();
    }

    /* build a lookup table for candidate classes */
    HashMap<String, ClassDef> candidateClassDefItemLookUpTable = new HashMap<String, ClassDef>();
    for (ClassDef classDef : candidateDexFile.classDefs()) {
      String typeName = candidateDexFile.typeNames().get(classDef.getTypeIndex());
      candidateClassDefItemLookUpTable.put(typeName, classDef);
    }

    Iterable<ClassDef> refClassDefs = referenceDexFile.classDefs();

    for (ClassDef classDefItem : refClassDefs) {

      if (!IGNORE_ANONYMOUS_CLASSES
          || !isAnomymousTypeName(referenceDexFile.typeNames().get(classDefItem.getTypeIndex()))) {

        String className = getClassName(referenceDexFile, classDefItem);

        ClassDef candidateClassDefItem =
            candidateClassDefItemLookUpTable.get(className);

        /* class */
        if (candidateClassDefItem != null) {
          logger.log(DEBUG_LEVEL, "Class {0} OK", className);

          checkAccessFlags(classDefItem, candidateClassDefItem);
          checkSuperclass(classDefItem, candidateClassDefItem);
          checkInterfaces(classDefItem, candidateClassDefItem);
          checkClassData(classDefItem, candidateClassDefItem);

          candidateClassDefItemLookUpTable.remove(className);

        } else {
          logger.log(
              ERROR_LEVEL, "Class {0} NOK: missing", getClassName(referenceDexFile, classDefItem));

          if (!TOLERATE_MISSING_SYNTHETICS || !isSynthetic(classDefItem.getAccessFlags())) {
            throw new DifferenceFoundException("Class "
                + getClassName(referenceDexFile, classDefItem) + " was not found in candidate.");
          }
        }
      }
    }
    if (strict) {
      for (ClassDef classDefItem : candidateClassDefItemLookUpTable.values()) {
        if (!IGNORE_ANONYMOUS_CLASSES || !isAnomymousTypeName(
            candidateDexFile.typeNames().get(classDefItem.getTypeIndex()))) {
          String className = getClassName(candidateDexFile, classDefItem);
          logger.log(
              ERROR_LEVEL, "Class {0} NOK: missing", className);

          if (!TOLERATE_MISSING_SYNTHETICS || !isSynthetic(classDefItem.getAccessFlags())) {
            throw new DifferenceFoundException("Class " + className
                + " was not found in reference.");
          }
        }
      }
    }
  }

  private void checkStringIds() throws DifferenceFoundException {
    checkStringIterables(referenceDexFile.strings(), candidateDexFile.strings(), "String");
  }

  private void checkTypeIds() throws DifferenceFoundException {
    checkStringIterables(referenceDexFile.typeNames(), candidateDexFile.typeNames(), "Type");
  }

  private void checkFieldIds() throws DifferenceFoundException {
    List<FieldId> referenceFieldIds = referenceDexFile.fieldIds();
    List<FieldId> candidateFieldIds = candidateDexFile.fieldIds();
    List<String> referenceFieldNames = getFieldNameList(referenceFieldIds, referenceDexFile);
    List<String> candidateFieldNames = getFieldNameList(candidateFieldIds, candidateDexFile);
    checkStringIterables(referenceFieldNames, candidateFieldNames, "Field");
  }

  private void checkMethodIds() throws DifferenceFoundException {
    List<MethodId> referenceMethodIds = referenceDexFile.methodIds();
    List<MethodId> candidateMethodIds = candidateDexFile.methodIds();
    List<String> referenceMethodNames = getMethodNameList(referenceMethodIds, referenceDexFile);
    List<String> candidateMethodNames = getMethodNameList(candidateMethodIds, candidateDexFile);
    checkStringIterables(referenceMethodNames, candidateMethodNames, "Method");
  }

  private void checkProtoIds() throws DifferenceFoundException {
    List<ProtoId> referenceProtoIds = referenceDexFile.protoIds();
    List<ProtoId> candidateProtoIds = candidateDexFile.protoIds();
    List<String> referenceProtoStrings = getProtoStringList(referenceProtoIds, referenceDexFile);
    List<String> candidateProtoStrings = getProtoStringList(candidateProtoIds, candidateDexFile);
    checkStringIterables(referenceProtoStrings, candidateProtoStrings, "Proto");
  }

  private static List<String> getProtoStringList(List<ProtoId> protoIds, DexBuffer dex) {
    List<String> protoStrings = new ArrayList<String>();
    for (ProtoId protoId : protoIds) {
      protoStrings.add(getProtoString(protoId, dex));
    }
    return protoStrings;
  }

  private static String getProtoString(ProtoId protoId, DexBuffer dex) {
    return dex.readTypeList(protoId.getParametersOffset()) + dex.typeNames().get(
        protoId.getReturnTypeIndex());
  }

  private static List<String> getFieldNameList(List<FieldId> fieldIds, DexBuffer dex) {
    List<String> fieldNames = new ArrayList<String>();
    for (FieldId fieldId : fieldIds) {
      fieldNames.add(dex.strings().get(fieldId.getNameIndex()));
    }
    return fieldNames;
  }

  private static List<String> getMethodNameList(List<MethodId> methodIds, DexBuffer dex) {
    List<String> methodNames = new ArrayList<String>();
    for (MethodId methodId : methodIds) {
      ProtoId protoId = dex.protoIds().get(methodId.getProtoIndex());
      String sortableMethodName = dex.typeNames().get(methodId.getDeclaringClassIndex()) + "."
          + dex.strings().get(methodId.getNameIndex()) + getProtoString(protoId, dex);
      methodNames.add(sortableMethodName);
    }
    return methodNames;
  }

  private void checkStringIterables(
      Iterable<String> referenceStrings, Iterable<String> candidateStrings, String logTypeName)
      throws DifferenceFoundException {
    Iterator<String> candidateStringIter = candidateStrings.iterator();
    for (String refString : referenceStrings) {
      boolean found = false;
      while (!found) {
        if (!candidateStringIter.hasNext()) {
          throw new DifferenceFoundException(logTypeName + " '" + refString
              + "' was not found in candidate as expected");
        }

        String candidateString = candidateStringIter.next();
        int stringComparison = candidateString.compareTo(refString);
        if (stringComparison == 0) {
          found = true;
          logger.log(DEBUG_LEVEL, "{0} {1} OK", new Object[] {logTypeName, refString});
        } else if (stringComparison > 0 || strict) { // candidateString is after refString
          logger.log(ERROR_LEVEL, "{0} {1} NOK: missing", new Object[] {logTypeName, refString});

          throw new DifferenceFoundException(logTypeName + " '" + refString
              + "' was not found in candidate as expected");
        }
      }
    }
    if (strict && candidateStringIter.hasNext()) {
      String leftOverString = candidateStringIter.next();

      throw new DifferenceFoundException(logTypeName + " '" + leftOverString
          + "' is in candidate but not in reference");
    }
  }

  private void checkAccessFlags(ClassDef classDefItem, ClassDef candidateClassDefItem)
      throws DifferenceFoundException {
    String className = getClassName(referenceDexFile, classDefItem);
    int candidateAccessFlags = candidateClassDefItem.getAccessFlags();
    int refAccessFlags = classDefItem.getAccessFlags();
    if (refAccessFlags == candidateAccessFlags) {
      logger.log(DEBUG_LEVEL, "Class Access Flags of {0} OK", className);
    } else {
      logger.log(ERROR_LEVEL,
          "Class Access Flags of {0} NOK: reference = {1}, candidate = {2}", new Object[] {
              className, Integer.valueOf(refAccessFlags),
              Integer.valueOf(candidateAccessFlags)});

      throw new DifferenceFoundException("Access flags do not match for Class '" + className
          + "'. Candidate flags: " + candidateAccessFlags + ". Reference flags: "
          + refAccessFlags + ".");
    }
  }

  private void checkClassData(ClassDef classDefItem, ClassDef candidateClassDefItem)
      throws DifferenceFoundException {
    String className = getClassName(referenceDexFile, classDefItem);
    boolean referenceDexFileHasClassData = classDefItem.getClassDataOffset() != 0;
    boolean candidateDexFileHasClassData = candidateClassDefItem.getClassDataOffset() != 0;

    if (!referenceDexFileHasClassData && !candidateDexFileHasClassData) {
      logger.log(DEBUG_LEVEL, "ClassData of {0} OK: both are null", className);
    } else if (!referenceDexFileHasClassData || !candidateDexFileHasClassData) {
      // If one DexFile has no ClassData, we have to check if all the
      // methods in the other one are tolerated
      ClassData.Field[] emptyFieldList = new ClassData.Field[0];
      ClassData.Method[] emptyMethodList = new ClassData.Method[0];
      ClassData classDataItem;
      if (referenceDexFileHasClassData) {
        classDataItem = referenceDexFile.readClassData(classDefItem);
        handleFields(classDataItem.getInstanceFields(), emptyFieldList, className);
        handleFields(classDataItem.getStaticFields(), emptyFieldList, className);
        handleMethods(classDataItem.allMethods(), emptyMethodList, className);
      } else {
        assert candidateDexFileHasClassData;
        classDataItem = candidateDexFile.readClassData(candidateClassDefItem);
        handleFields(emptyFieldList, classDataItem.getInstanceFields(), className);
        handleFields(emptyFieldList, classDataItem.getStaticFields(), className);
        handleMethods(emptyMethodList, classDataItem.allMethods(), className);
      }
    } else {
      // TODO(benoitlamarche): check annotations

      ClassData classDataItem = referenceDexFile.readClassData(classDefItem);
      ClassData candidateClassDataItem = candidateDexFile.readClassData(candidateClassDefItem);

      checkFields(classDataItem, candidateClassDataItem, classDefItem);

      checkMethods(classDataItem, candidateClassDataItem, classDefItem);
    }
  }

  private void checkMethods(
      ClassData classDataItem, ClassData candidateClassDataItem, ClassDef classDefItem)
      throws DifferenceFoundException {
    String className = getClassName(referenceDexFile, classDefItem);

    ClassData.Method[] methods = classDataItem.allMethods();
    ClassData.Method[] candidateMethods = candidateClassDataItem.allMethods();

    handleMethods(methods, candidateMethods, className);
  }

  private void checkFields(
      ClassData classDataItem, ClassData candidateClassDataItem, ClassDef classDefItem)
      throws DifferenceFoundException {
    String className = getClassName(referenceDexFile, classDefItem);

    /* Instance fields */
    {
      ClassData.Field[] instanceFields = classDataItem.getInstanceFields();
      ClassData.Field[] candidateInstanceFields = candidateClassDataItem.getInstanceFields();

      handleFields(instanceFields, candidateInstanceFields, className);
    }

    /* Static fields */
    // TODO(benoitlamarche): should static initializers be checked?
    {
      ClassData.Field[] instanceFields = classDataItem.getStaticFields();
      ClassData.Field[] candidateInstanceFields = candidateClassDataItem.getStaticFields();

      handleFields(instanceFields, candidateInstanceFields, className);
    }
  }

  private void checkInterfaces(ClassDef classDefItem, ClassDef candidateClassDefItem)
      throws DifferenceFoundException {

    String className = getClassName(referenceDexFile, classDefItem);
    short[] interfaces = classDefItem.getInterfaces();
    short[] candidateInterfaces = candidateClassDefItem.getInterfaces();

    List<String> interfacesList = getInterfaceNames(referenceDexFile, interfaces);
    List<String> candidateInterfacesList = getInterfaceNames(candidateDexFile, candidateInterfaces);

    for (String interfaceName : interfacesList) {
      boolean contained = candidateInterfacesList.remove(interfaceName);
      if (contained) {
        logger.log(DEBUG_LEVEL, "Implemented interface of {0} OK: {1}",
            new Object[] {className, interfaceName});
      } else {
        logger.log(ERROR_LEVEL, "Implemented interface of {0} NOK: {1} missing in candidate",
            new Object[] {className, interfaceName});

        throw new DifferenceFoundException("Interface " + interfaceName + " is not implemented by "
            + className + " in candidate");
      }
    }

    if (!candidateInterfacesList.isEmpty()) {
      String leftOverInterface = candidateInterfacesList.get(0);
      logger.log(ERROR_LEVEL, "Implemented interface of {0} NOK: {1} missing in reference",
          new Object[] {className, leftOverInterface});

      throw new DifferenceFoundException("Interface " + leftOverInterface
          + " is not implemented by " + className + " in reference");
    }
  }

  private void checkSuperclass(ClassDef classDefItem, ClassDef candidateClassDefItem)
      throws DifferenceFoundException {
    String className = getClassName(referenceDexFile, classDefItem);
    String superClass = (classDefItem.getSupertypeIndex() == ClassDef.NO_INDEX) ? ("empty")
        : (getSuperclassName(referenceDexFile, classDefItem));
    String candidateSuperClass =
        (candidateClassDefItem.getSupertypeIndex() == ClassDef.NO_INDEX) ? ("empty")
            : (getSuperclassName(candidateDexFile, candidateClassDefItem));

    if (superClass.equals(candidateSuperClass)) {
      logger.log(DEBUG_LEVEL, "Superclass of {0} OK: {1}", new Object[] {className, superClass});
    } else {
      logger.log(ERROR_LEVEL, "Superclass of {0} NOK: reference = {1}, candidate = {2}",
          new Object[] {className, superClass, candidateSuperClass});

      throw new DifferenceFoundException("Superclasses of '" + className
          + "' do not match. Candidate superclass: " + candidateSuperClass
          + ". Reference superclass: " + superClass + ".");
    }
  }

  /**
   * Checks that all the elements of {@code referenceFields} can be found in
   * {@code candidateFields} based on their name and type, and check accessFlags are the same. If in
   * strict mode, all the elements of {@code candidateFields} must also be in
   * {@code referenceFields}.
   *
   * @param referenceFields Contains fields of current class in reference dex file.
   * @param candidateFields Contains fields of current class in candidate dex file
   * @param className Name of the current class
   * @throws DifferenceFoundException If a difference is found while comparing fields
   */
  private void handleFields(ClassData.Field[] referenceFields,
      ClassData.Field[] candidateFields, String className) throws DifferenceFoundException {

    boolean isFound;
    List<ClassData.Field> foundFields = null;
    if (strict) {
      foundFields = new ArrayList<ClassData.Field>(candidateFields.length);
    }
    for (ClassData.Field encField : referenceFields) {
      isFound = false;
      String refFieldName = getFieldName(referenceDexFile, encField.getFieldIndex());
      String refFieldType = getFieldTypeName(referenceDexFile, encField.getFieldIndex());
      for (ClassData.Field candidateEncField : candidateFields) {
        String candFieldName = getFieldName(candidateDexFile, candidateEncField.getFieldIndex());
        String candFieldType = getFieldTypeName(
            candidateDexFile, candidateEncField.getFieldIndex());
        if (refFieldName.equals(candFieldName) && refFieldType.equals(candFieldType)) {
          logger.log(DEBUG_LEVEL,
              "Field {0}.{1} OK", new Object[] {className, refFieldName});

          /* Access flags */
          if (encField.getAccessFlags() != candidateEncField.getAccessFlags()) {
            logger.log(ERROR_LEVEL,
                "Access Flags for Field {0}.{1} NOK: reference = {2}, candidate = {3}",
                new Object[] {className, refFieldName, Integer.valueOf(encField.getAccessFlags()),
                    Integer.valueOf(candidateEncField.getAccessFlags())});

            throw new DifferenceFoundException("Access flags do not match for Field '" + className
                + "." + refFieldName + "'. Candidate flags: " + candidateEncField.getAccessFlags()
                + ". Reference flags: " + encField.getAccessFlags() + ".");

          } else {
            logger.log(DEBUG_LEVEL, "Field Access Flags of {0}.{1} OK",
                new Object[] {className, refFieldName});
            isFound = true;
            if (strict) {
              assert foundFields != null;
              foundFields.add(candidateEncField);
            }
            break;
          }
        }
      }

      if (!isFound && !isTolerated(encField)) {
        logger.log(
            ERROR_LEVEL, "Field {0}.{1} NOK: missing", new Object[] {className, refFieldName});
        throw new DifferenceFoundException("Field " + className + "." + refFieldName + " of type '"
            + refFieldType + "' not found in candidate file.");
      }
    }
    if (strict) {
      List<ClassData.Field> candidateFieldList =
          new ArrayList<ClassData.Field>(Arrays.asList(candidateFields));
      candidateFieldList.removeAll(foundFields);

      // remove tolerated fields
      Iterator<ClassData.Field> candidateFieldIter = candidateFieldList.iterator();
      while (candidateFieldIter.hasNext()) {
        ClassData.Field field = candidateFieldIter.next();
        if (isTolerated(field)) {
          candidateFieldIter.remove();
        }
      }

      if (!candidateFieldList.isEmpty()) {
        StringBuffer sb = new StringBuffer(
            "Too many fields in candidate for class '" + className + "'. Unwanted fields are: ");
        for (ClassData.Field unwantedField: candidateFieldList) {
          sb.append(getFieldTypeName(candidateDexFile, unwantedField.getFieldIndex()));
          sb.append(" ");
          sb.append(getFieldName(candidateDexFile, unwantedField.getFieldIndex()));
          sb.append(" - ");
        }
        throw new DifferenceFoundException(sb.toString());
      }
    }
  }

  /**
   * Checks that all the elements of {@code referenceMethods} can be found in
   * {@code candidateMethods} based on their name and prototype, and check accessFlags are the same.
   * If in strict mode, all the elements of {@code candidateMethods} must also be in
   * {@code referenceMethods}.
   *
   * @param referenceMethods Contains methods of current class in reference dex file.
   * @param candidateMethods Contains methods of current class in candidate dex file
   * @param className Name of the current class
   * @throws DifferenceFoundException If a difference is found while comparing methods
   */
  private void handleMethods(
      ClassData.Method[] referenceMethods, ClassData.Method[] candidateMethods, String className)
      throws DifferenceFoundException {
    boolean isFound;
    List<ClassData.Method> foundMethods = null;
    if (strict) {
      foundMethods = new ArrayList<ClassData.Method>(candidateMethods.length);
    }
    for (ClassData.Method encMeth : referenceMethods) {
      isFound = false;
      String refMethodName = getMethodName(referenceDexFile, encMeth.getMethodIndex());
      String refMethodProto = getMethodProto(referenceDexFile, encMeth.getMethodIndex());

      if (isSkipped(className, refMethodName, refMethodProto)) {
        continue;
      }

      for (ClassData.Method candidateEncMeth : candidateMethods) {
        String candMethodName = getMethodName(candidateDexFile, candidateEncMeth.getMethodIndex());
        String candMethodProto = getMethodProto(
            candidateDexFile, candidateEncMeth.getMethodIndex());

        if (refMethodName.equals(candMethodName) && refMethodProto.equals(candMethodProto)) {
          logger.log(DEBUG_LEVEL,
              "Method {0}.{1}{2} OK", new Object[] {className, refMethodName, refMethodProto});

          if (enableInstructionNumberComparison) {
            handleInstructionNumberComparison(className,
                refMethodName,
                refMethodProto,
                encMeth,
                candidateEncMeth,
                instructionNumberTolerance);
          }

          /* Access flags */
          // TODO(?): remove testing of debugInfo and do something else to be able to not check
          // structure when comparing debug info
          if ((!enableDebugInfoComparison)
              && (encMeth.getAccessFlags() != candidateEncMeth.getAccessFlags())) {
            logger.log(ERROR_LEVEL,
                "Method Access Flags of {0}.{1}{2} NOK: reference = {3}, candidate = {4}",
                new Object[] {className, refMethodName, refMethodProto, Integer.valueOf(
                    encMeth.getAccessFlags()), Integer.valueOf(candidateEncMeth.getAccessFlags())});

            throw new DifferenceFoundException("Access flags do not match for Method '" + className
                + "." + refMethodName + "'. Candidate flags: " + candidateEncMeth.getAccessFlags()
                + ". Reference flags: " + encMeth.getAccessFlags() + ".");

          } else {
            logger.log(DEBUG_LEVEL, "Access Flags for Method {0}.{1}{2} OK",
                new Object[] {className, refMethodName, refMethodProto});
            isFound = true;

            if (strict) {
              assert foundMethods != null;
              foundMethods.add(candidateEncMeth);
            }
            if (enableDebugInfoComparison) {
              checkDebugInfo(encMeth, candidateEncMeth, className);
            }
            if (enableBinaryCodeComparison) {
              checkCodeBinarily(encMeth, candidateEncMeth, className, refMethodName,
                  refMethodProto);
            }
            break;
          }
        }
      }

      if (!isFound && !isTolerated(encMeth, refMethodName)) {
        logger.log(ERROR_LEVEL, "Method {0}.{1}{2} NOK: missing",
            new Object[] {className, refMethodName, refMethodProto});
        throw new DifferenceFoundException("Method " + className + "." + refMethodName
            + refMethodProto + " not found in candidate file.");
      }
    }
    if (strict) {
      List<ClassData.Method> candidateMethodList =
          new ArrayList<ClassData.Method>(Arrays.asList(candidateMethods));
      candidateMethodList.removeAll(foundMethods);

      // remove tolerated methods
      Iterator<ClassData.Method> candidateMethodIter = candidateMethodList.iterator();
      while (candidateMethodIter.hasNext()) {
        ClassData.Method method = candidateMethodIter.next();
        String methodName = getMethodName(candidateDexFile, method.getMethodIndex());
        if (isTolerated(method, methodName)) {
          candidateMethodIter.remove();
        }
      }

      if (!candidateMethodList.isEmpty()) {
        StringBuffer sb = new StringBuffer(
            "Too many methods in candidate for class '" + className + "'. Unwanted methods are: ");
        for (ClassData.Method unwantedMethod: candidateMethodList) {
          sb.append(getMethodName(candidateDexFile, unwantedMethod.getMethodIndex()));
          sb.append(getMethodProto(candidateDexFile, unwantedMethod.getMethodIndex()));
          sb.append(" - ");
        }
        throw new DifferenceFoundException(sb.toString());
      }
    }
  }

  private void checkCodeBinarily(@Nonnull Method encMeth, @Nonnull Method candidateEncMeth,
      @Nonnull String className, @Nonnull String methodName, @Nonnull String methodProto)
      throws DifferenceFoundException {
    handleInstructionNumberComparison(className,
        methodName,
        methodProto,
        encMeth,
        candidateEncMeth,
        0);

    if (encMeth.getCodeOffset() == 0) {
      // we already checked that if the reference has no code, neither does the candidate
      assert candidateEncMeth.getCodeOffset() == 0;
      return;
    }
    Code refMethCode = referenceDexFile.readCode(encMeth);
    Code candMethCode = candidateDexFile.readCode(candidateEncMeth);
    short[] refInstructions = refMethCode.getInstructions();
    short[] candInstructions = candMethCode.getInstructions();

    // number of instructions should have been checked already
    assert refInstructions.length == candInstructions.length;
    int size = refInstructions.length;
    int index = 0;
    while (index < size) {
      if (refInstructions[index] != candInstructions[index]) {
        String encMethOffset = getInstructionOffsetAsHexString(encMeth, index);
        String candMethOffset = getInstructionOffsetAsHexString(candidateEncMeth, index);
        throw new DifferenceFoundException("Binary instructions of '" + className + "." + methodName
            + methodProto + "' do not match at index " + index + ". Address for reference: "
            + encMethOffset + ". Address for candidate: " + candMethOffset + ".");
      }
      index++;
    }

  }

  private String getInstructionOffsetAsHexString(@Nonnull Method encMeth, int index) {
    return "0x" + Integer.toHexString(
        encMeth.getCodeOffset() + com.android.jack.dx.dex.file.Code.HEADER_SIZE + index * 2);
  }

  private void handleInstructionNumberComparison(@Nonnull String className,
      @Nonnull String methodName,
      @Nonnull String methodProto,
      @Nonnull Method refMeth,
      @Nonnull Method candidateMeth,
      float instructionNumberTolerance) throws DifferenceFoundException {

    if (refMeth.getCodeOffset() == 0 && candidateMeth.getCodeOffset() == 0) {
      logger.log(DEBUG_LEVEL, "Method {0}.{1}{2} code existence comparison OK",
          new Object[] {className, methodName, methodProto});
      return;
    }
    if (refMeth.getCodeOffset() != 0 && candidateMeth.getCodeOffset() == 0) {
      logger.log(
          ERROR_LEVEL,
          "Method {0}.{1}{2}  NOK: candidate has no code whereas reference has",
          new Object[] {className, methodName, methodProto});
      throw getDifferenceFoundException(className, refMeth, referenceDexFile,
          "Candidate method has no code whereas reference has");
    }
    if (refMeth.getCodeOffset() == 0 && candidateMeth.getCodeOffset() != 0) {
      logger.log(
          ERROR_LEVEL,
          "Method {0}.{1}{2}  NOK: candidate has code whereas reference has not",
          new Object[] {className, methodName, methodProto});
      throw getDifferenceFoundException(className, refMeth, referenceDexFile,
          "Candidate method has code whereas reference has not");
    }

    int refInsSize = referenceDexFile.readCode(refMeth).getInstructions().length;
    int candidateInsSize = candidateDexFile.readCode(candidateMeth).getInstructions().length;
    float ratio;
    if (refInsSize != 0) {
      ratio = ((float) (candidateInsSize - refInsSize)) / refInsSize;
    } else {
      if (candidateInsSize == 0) {
        ratio = 0f;
      } else {
        ratio = 1f;
      }
    }
    boolean tolerated = ratio <= instructionNumberTolerance;
    if (!tolerated) {
      logger.log(WARNING_LEVEL,
          "Method {0}.{1}{2}  NOK: number of instructions differs more than allowed: "
          + "percentage = {3}%, reference = {4}, candidate = {5}, delta allowed = {6}%",
          new Object[] {className,
              methodName,
              methodProto,
              Float.valueOf(ratio * 100),
              Integer.valueOf(refInsSize),
              Integer.valueOf(candidateInsSize),
              Float.valueOf(instructionNumberTolerance * 100)});
    }
  }

  private void handleBinaryDebugInfoComparison(String className,
      String methodName,
      String methodProto,
      Method refMeth,
      DebugInfo refDbgInfo,
      DebugInfo candidateDbgInfo)
      throws DifferenceFoundException {
    byte[] refBytes = referenceDexFile.getBytes();
    byte[] candidateBytes = candidateDexFile.getBytes();

    int refDbgInfOffset = refDbgInfo.getDebugInfoOffset();
    int candidateDbgInfOffset = candidateDbgInfo.getDebugInfoOffset();

    int refDbgInfoLength = refDbgInfo.getSizeInBytes();
    int candidateDbgInfoLength = candidateDbgInfo.getSizeInBytes();

    int i = 0;
    for (; (i < refDbgInfoLength) && ((refDbgInfOffset + i) < refBytes.length); ++i) {
      if ((candidateDbgInfOffset + i) >= candidateBytes.length
          || i >= candidateDbgInfoLength) {
        logger.log(
            ERROR_LEVEL, "Method {0}.{1}{2}  NOK: debug infos size is smaller than reference",
            new Object[] {className, methodName, methodProto});
        throw getDifferenceFoundException(className, refMeth, referenceDexFile,
            "There's less debug infos in candidate than in reference");
      } else if (refBytes[refDbgInfOffset + i] != candidateBytes[candidateDbgInfOffset + i]) {
        logger.log(ERROR_LEVEL, "Method {0}.{1}{2}  NOK: debug infos differ",
            new Object[] {className, methodName, methodProto});
        throw getDifferenceFoundException(className, refMeth, referenceDexFile,
            "Debug infos differ");
      }
    }
    assert (refDbgInfOffset + i) < refBytes.length;
    if (i == candidateDbgInfoLength) {
      logger.log(DEBUG_LEVEL, "Method {0}.{1}{2} debug infos comparison OK",
          new Object[] {className, methodName, methodProto});
    } else {
      logger.log(ERROR_LEVEL, "Method {0}.{1}{2}  NOK: debug infos size is larger than reference",
          new Object[] {className, methodName, methodProto});
      throw getDifferenceFoundException(className, refMeth, referenceDexFile,
          "There's more debug infos in candidate than in reference");
    }
  }

  private boolean isSkipped(String className, String methodName, String methodProto) {
    boolean isSkipped = skippedMethods.contains(className + "." + methodName + methodProto);
    return isSkipped;
  }

  private boolean isTolerated(ClassData.Field field) {
    return TOLERATE_MISSING_SYNTHETICS && isSynthetic(field.getAccessFlags());
  }

  private boolean isTolerated(ClassData.Method method, String methodName) {
    boolean tolerated = (TOLERATE_MISSING_SYNTHETICS && isSynthetic(method.getAccessFlags())) ||
        (TOLERATE_MISSING_INITS && methodName.equals(INIT_NAME)) ||
        (TOLERATE_MISSING_CLINITS && methodName.equals(STATIC_INIT_NAME));
    return tolerated;
  }

  private boolean isTolerated(LocalVar localVar) {
    boolean tolerated = TOLERATE_MISSING_SYNTHETICS && localVar.isSynthetic();
    return tolerated;
  }

  private void checkDebugInfo(Method reference, Method candidate, String className)
      throws DifferenceFoundException {

    if (isSynthetic(reference.getAccessFlags())) {
      assert isSynthetic(candidate.getAccessFlags());
      // ignore synthetic methods
      return;
    }

    if (AccessFlags.isConstructor(reference.getAccessFlags())) {
      assert AccessFlags.isConstructor(candidate.getAccessFlags());
      // Ignore all constructors because debug infos for default constructors may not use the same
      // line numbers. It would be better to ignore only default constructors but they are not
      // flagged as synthetic, and in the case of inner classes may have parameters.
      return;
    }

    if (reference.getCodeOffset() == 0) {
      if (candidate.getCodeOffset() != 0) {
        throw getDifferenceFoundException(className, reference, referenceDexFile,
            "Candidate has code while reference has not");

      } else {
        return;
      }
    } else if (candidate.getCodeOffset() == 0) {
      throw getDifferenceFoundException(className, reference, referenceDexFile,
          "Candidate is missing code");
    }
    DebugInfo refInfo = decodeDebugInfo(reference, referenceDexFile, referenceData,
        refThisIndex);
    DebugInfo candidateInfo = decodeDebugInfo(candidate, candidateDexFile, candidateData,
        candidateThisIndex);
    if (refInfo == null) {
      if (candidateInfo != null) {
        throw getDifferenceFoundException(className, reference, referenceDexFile,
            "Candidate has debug info while reference has not");
      } else {
        return;
      }
    } else if (candidateInfo == null) {
      throw getDifferenceFoundException(className, reference, referenceDexFile,
          "Candidate is missing debug info");

    }

    if (enableBinaryDebugInfoComparison) {
      String refMethodName = getMethodName(referenceDexFile, reference.getMethodIndex());
      String refMethodProto = getMethodProto(referenceDexFile, candidate.getMethodIndex());
      handleBinaryDebugInfoComparison(className,
          refMethodName,
          refMethodProto,
          candidate,
          refInfo,
          candidateInfo);
    }

    for (LocalVar refLocal : refInfo.getLocals()) {
      LocalVar candidateLocal = candidateInfo.getLocal(refLocal);
      if (candidateLocal == null) {
        if (!isTolerated(refLocal)) {
          throw getDifferenceFoundException(className, reference, referenceDexFile,
              "Missing local variable in candidate: " + refLocal.getTypeSignature() + " "
              + refLocal.getName());
        }
      } else {
        if (!refLocal.getScope().equals(candidateLocal.getScope())) {
          throw getDifferenceFoundException(className, reference, referenceDexFile,
              "Scope differs for local: " + refLocal.getTypeSignature() + " " +
                  refLocal.getName() + ", reference: " + refLocal.getScope() +
                  ", candidate:" + candidateLocal.getScope());
        }
      }

    }
  }

  private DifferenceFoundException getDifferenceFoundException(String inClass, Method inMethod,
      DexBuffer dexOfMethod, String message) {
    return new DifferenceFoundException("In method " + inClass + "." +
        getMethodName(dexOfMethod, inMethod.getMethodIndex()) +
        getMethodProto(dexOfMethod, inMethod.getMethodIndex()) + ":" + message);
  }

  private static DebugInfo decodeDebugInfo(Method method, DexBuffer dex, byte[] dexData,
      int thisIdx) {
    boolean isStatic = (method.getAccessFlags() & AccessFlags.ACC_STATIC) != 0;
    Prototype prototype = Prototype.intern(getMethodProto(
        dex, method.getMethodIndex()));
    Code codeItem = dex.readCode(method);
    if (codeItem.getDebugInfoOffset() == 0) {
      return null;
    }

    ByteArrayInput bai = new ByteArrayInput(codeItem.getDebugInfoOffset(), dexData);
    DebugInfoDecoder decoder = new DebugInfoDecoder(
        bai,
        codeItem.getRegistersSize(),
        isStatic,
        prototype,
        thisIdx);
    decoder.decode();
    return new DebugInfo(decoder, dex, codeItem, bai.getPosition() - codeItem.getDebugInfoOffset());
  }

  private static String getClassName(DexBuffer dex, ClassDef classDef) {
    return dex.typeNames().get(classDef.getTypeIndex());
  }

  private static String getMethodName(DexBuffer dex, int methodIndex) {
    MethodId methodId = dex.methodIds().get(methodIndex);
    return dex.strings().get(methodId.getNameIndex());
  }

  private static String getMethodProto(DexBuffer dex, int methodIndex) {
    MethodId methodId = dex.methodIds().get(methodIndex);
    ProtoId protoId = dex.protoIds().get(methodId.getProtoIndex());
    return getProtoString(protoId, dex);
  }

  private static String getFieldName(DexBuffer dex, int fieldIndex) {
    FieldId fieldId = dex.fieldIds().get(fieldIndex);
    return dex.strings().get(fieldId.getNameIndex());
  }

  private static String getFieldTypeName(DexBuffer dex, int fieldIndex) {
    FieldId fieldId = dex.fieldIds().get(fieldIndex);
    Integer stringIndex = dex.typeIds().get(fieldId.getTypeIndex());
    return dex.strings().get(stringIndex.intValue());
  }

  private static String getSuperclassName(DexBuffer dex, ClassDef classDef) {
    return dex.typeNames().get(classDef.getSupertypeIndex());
  }

  private static List<String> getInterfaceNames(DexBuffer dex, short[] interfaces) {

    List<String> interfaceNames = new ArrayList<String>(interfaces.length);
    for (short interfIndex : interfaces) {
      String typeName = dex.typeNames().get(interfIndex);
      interfaceNames.add(typeName);
    }
    return interfaceNames;
  }

  private static boolean isAnomymousTypeName(String typeName) {
    //TODO(benoitlamarche): use Annotations to determine if the class is anonymous
    int location = typeName.lastIndexOf('$');
    if (location != -1) {
      String num = typeName.substring(location + 1, typeName.length() - 1);
      try {
        Integer.parseInt(num);
        return true;
      } catch (NumberFormatException e) {
        return false;
      }
    } else {
      return false;
    }
  }

  private static boolean isSynthetic(int modifier) {
    return ((modifier & AccessFlags.ACC_SYNTHETIC) == AccessFlags.ACC_SYNTHETIC);
  }

  private static class ByteArrayInput implements ByteInput {
    private final byte[] bytes;
    private int position;

    public ByteArrayInput(int start, byte... bytes) {
      this.position = start;
      this.bytes = bytes;
    }

    @Override
    public byte readByte() {
        return bytes[position++];
    }

    public int getPosition() {
      return position;
    }
  }

}
