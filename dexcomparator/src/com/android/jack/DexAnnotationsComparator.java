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

import com.android.jack.dx.rop.code.AccessFlags;

import org.jf.dexlib.AnnotationDirectoryItem;
import org.jf.dexlib.AnnotationDirectoryItem.MethodAnnotation;
import org.jf.dexlib.AnnotationItem;
import org.jf.dexlib.AnnotationSetItem;
import org.jf.dexlib.AnnotationSetRefList;
import org.jf.dexlib.ClassDataItem;
import org.jf.dexlib.ClassDataItem.EncodedField;
import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.ItemType;
import org.jf.dexlib.MethodIdItem;
import org.jf.dexlib.ProtoIdItem;
import org.jf.dexlib.Section;
import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.TypeListItem;

import org.jf.dexlib.EncodedValue.AnnotationEncodedSubValue;
import org.jf.dexlib.EncodedValue.ArrayEncodedSubValue;
import org.jf.dexlib.EncodedValue.ArrayEncodedValue;
import org.jf.dexlib.EncodedValue.EncodedValue;
import org.jf.dexlib.EncodedValue.EnumEncodedValue;
import org.jf.dexlib.EncodedValue.IntEncodedValue;
import org.jf.dexlib.EncodedValue.MethodEncodedValue;
import org.jf.dexlib.EncodedValue.StringEncodedValue;
import org.jf.dexlib.EncodedValue.TypeEncodedValue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This tool compares 2 dex files. The candidate is compared to the reference dex file, and MUST
 * have at least all the features present in the reference. (Class, super class, fields, methods)
 */
public class DexAnnotationsComparator {

  @Nonnull
  private static final String MEMBER_CLASSES_DESCRIPTOR = "Ldalvik/annotation/MemberClasses;";
  @Nonnull
  private static final String THROWS_DESCRIPTOR = "Ldalvik/annotation/Throws;";
  @Nonnull
  private final Logger logger;
  @Nonnull
  private static final Level ERROR_LEVEL = Level.SEVERE;
  @Nonnull
  private static final Level DEBUG_LEVEL = Level.FINE;

  private static final boolean IGNORE_ANONYMOUS_CLASSES = true;
  private static final boolean TOLERATE_MISSING_SYNTHETICS = true;
  private static final boolean TOLERATE_MISSING_INITS = true;
  private static final boolean TOLERATE_MISSING_CLINITS = true;

  public DexAnnotationsComparator() {
    logger = Logger.getLogger(this.getClass().getName());
    logger.setLevel(ERROR_LEVEL);
  }

  @SuppressWarnings("unchecked")
  public void compare(@Nonnull File referenceFile, @Nonnull File candidateFile)
      throws DifferenceFoundException, IOException {
    DexFile referenceDexFile = new DexFile(referenceFile);
    DexFile candidateDexFile = new DexFile(candidateFile);

    /* Reuse delegate instance for maximum memory saving */
    CompareElementAnnotation compareElementAnnotation =
        new CompareElementAnnotation(logger);

    /* build a lookup table for candidate classes */
    HashMap<String, ClassDefItem> candidateClassDefItemLookUpTable =
        new HashMap<String, ClassDefItem>();
    Section<ClassDefItem> candidateClassDefSection =
        candidateDexFile.getSectionForType(ItemType.TYPE_CLASS_DEF_ITEM);
    for (ClassDefItem classDefItem : candidateClassDefSection.getItems()) {
      candidateClassDefItemLookUpTable.put(
          classDefItem.getClassType().getTypeDescriptor(), classDefItem);
    }

    Section<ClassDefItem> classDefSection =
        referenceDexFile.getSectionForType(ItemType.TYPE_CLASS_DEF_ITEM);

    for (ClassDefItem classDefItem : classDefSection.getItems()) {

      ClassDefItem candidateClassDefItem =
          candidateClassDefItemLookUpTable.get(classDefItem.getClassType().getTypeDescriptor());
      String className = classDefItem.getClassType().getTypeDescriptor();

      if (!IGNORE_ANONYMOUS_CLASSES || !isAnonymousTypeName(className)) {

      /* class */
      if (candidateClassDefItem != null) {

        logger.log(DEBUG_LEVEL, "Class {0} OK", className);
        int accessFlags = classDefItem.getAccessFlags();
        boolean synthetic = isSynthetic(accessFlags);

        if (synthetic && TOLERATE_MISSING_SYNTHETICS) {
          continue;
        }

        /* check ClassData field */
        ClassDataItem classDataItem = classDefItem.getClassData();
        ClassDataItem candidateClassDataItem = candidateClassDefItem.getClassData();

        if (classDataItem == null && candidateClassDataItem == null) {
          logger.log(DEBUG_LEVEL, "ClassData of {0} OK: both are null", className);
        } else if (classDataItem == null || candidateClassDataItem == null) {
          String errorString =
              ((classDataItem == null) ? ("reference's is null and not candidate's")
                  : ("candidate's is null and not reference's"));
          throw new DifferenceFoundException(
              "ClassDatas of '" + className + "' do not match: " + errorString);
        } else {
          /* Annotation pre check */
          boolean doCheckAnnotations = false;
          AnnotationDirectoryItem annotationDirectoryItem = classDefItem.getAnnotations();
          AnnotationDirectoryItem candidateAnnotationDirectoryItem =
              candidateClassDefItem.getAnnotations();

          if (annotationDirectoryItem == null && candidateAnnotationDirectoryItem == null) {
            logger.log(DEBUG_LEVEL, "AnnotationDirectoryItem of {0} OK: both are null", className);
          } else if (annotationDirectoryItem == null || candidateAnnotationDirectoryItem == null) {
            String errorString =
                ((annotationDirectoryItem == null) ? ("reference's is null and not candidate's")
                    : ("candidate's is null and not reference's"));

            logger.log(ERROR_LEVEL, "AnnotationDirectoryItem of {0} NOK: {1}",
                new Object[] {className, errorString});

            if (annotationDirectoryItem != null) {
              boolean containsOnlyAnnotationsOnSyntheticMethods = true;
                for (MethodAnnotation anno : annotationDirectoryItem.getMethodAnnotations()) {
                  containsOnlyAnnotationsOnSyntheticMethods =
                      containsOnlyAnnotationsOnSyntheticMethods
                      && isSynthetic(getMethodAccessFlags(classDataItem, anno.method));

                }

              if (containsOnlyAnnotationsOnSyntheticMethods) {
                continue;
              }
            }

            throw new DifferenceFoundException(
                "AnnotationDirectoryItems of '" + className + "' do not match: " + errorString);
          } else {
            logger.log(DEBUG_LEVEL, "AnnotationDirectoryItem of {0}: not null", className);
            doCheckAnnotations = true;
          }

          /* Class Annotations */
          {
              if (doCheckAnnotations) {
                checkClassAnnotations(annotationDirectoryItem, candidateAnnotationDirectoryItem,
                    compareElementAnnotation, className);
              }
          } /* class annotation */

          /* Field Annotations */
          {
            if (doCheckAnnotations) {
              compareElementAnnotation.reset();
              compareElementAnnotation.isCandidate = true;
              assert candidateAnnotationDirectoryItem != null;
              processFields(compareElementAnnotation, candidateAnnotationDirectoryItem,
                  candidateClassDataItem.getStaticFields());
              processFields(compareElementAnnotation, candidateAnnotationDirectoryItem,
                  candidateClassDataItem.getInstanceFields());
              compareElementAnnotation.isCandidate = false;
              assert annotationDirectoryItem != null;
              processFields(compareElementAnnotation, annotationDirectoryItem,
                  classDataItem.getStaticFields());
              processFields(compareElementAnnotation, annotationDirectoryItem,
                  classDataItem.getInstanceFields());
            }
          }

          /* Method and parameter Annotations */
          {
            if (doCheckAnnotations) {
              compareElementAnnotation.reset();
              compareElementAnnotation.isCandidate = true;
              assert candidateAnnotationDirectoryItem != null;
              processMethods(compareElementAnnotation, candidateAnnotationDirectoryItem,
                  candidateClassDataItem.getDirectMethods());
              processMethods(compareElementAnnotation, candidateAnnotationDirectoryItem,
                  candidateClassDataItem.getVirtualMethods());
              compareElementAnnotation.isCandidate = false;
              assert annotationDirectoryItem != null;
              compareElementAnnotation.classData = classDataItem;
              processMethods(compareElementAnnotation, annotationDirectoryItem,
                  classDataItem.getDirectMethods());
              processMethods(compareElementAnnotation, annotationDirectoryItem,
                  classDataItem.getVirtualMethods());
            }
          }
        } /* check ClassData field */

        classDataItem = null;
        candidateClassDataItem = null;
      } else /* Class */ {
        logger.log(ERROR_LEVEL, "Class {0} NOK: missing", className);

        if (!TOLERATE_MISSING_SYNTHETICS || !isSynthetic(classDefItem.getAccessFlags())) {
          throw new DifferenceFoundException("Class " + className + " was not found in candidate.");
        }
      }
      }
    } /* for each class */

  }

  private void processMethods(@Nonnull CompareElementAnnotation compareElementAnnotation,
      @Nonnull AnnotationDirectoryItem annotationDirectoryItem,
      @Nonnull List<EncodedMethod> methods) {
    for (EncodedMethod method : methods) {
      AnnotationSetItem methodAnnotations =
          annotationDirectoryItem.getMethodAnnotations(method.method);
      if (methodAnnotations != null) {
        compareElementAnnotation.processMethodAnnotations(method.method, methodAnnotations);
      }
      AnnotationSetRefList parameterAnnotations =
          annotationDirectoryItem.getParameterAnnotations(method.method);
      if (parameterAnnotations != null) {
        compareElementAnnotation.processParameterAnnotations(method.method, parameterAnnotations);
      }
    }
  }

  private void processFields(@Nonnull CompareElementAnnotation compareElementAnnotation,
      @Nonnull AnnotationDirectoryItem annotationDirectoryItem,
      @Nonnull List<EncodedField> fields) {
    for (EncodedField field : fields) {
      AnnotationSetItem fieldAnnotations = annotationDirectoryItem.getFieldAnnotations(field.field);
      if (fieldAnnotations != null) {
        compareElementAnnotation.processFieldAnnotations(field.field, fieldAnnotations);
      }
    }
  }

  private void checkClassAnnotations(AnnotationDirectoryItem annotationDirectoryItem,
      AnnotationDirectoryItem candidateAnnotationDirectoryItem,
      CompareElementAnnotation compareElementAnnotationDelegate, String className)
      throws DifferenceFoundException {
  assert annotationDirectoryItem != null;
    assert candidateAnnotationDirectoryItem != null;
    AnnotationSetItem classAnnotations = annotationDirectoryItem.getClassAnnotations();
    AnnotationSetItem candidateClassAnnotations =
        candidateAnnotationDirectoryItem.getClassAnnotations();

    if (classAnnotations == null && candidateClassAnnotations == null) {
      logger.log(DEBUG_LEVEL, "ClassAnnotations of {0} OK: both are null", className);
    } else if (classAnnotations == null || candidateClassAnnotations == null) {
      String errorString =
          ((classAnnotations == null) ? ("reference's is null and not candidate's")
              : ("candidate's is null and not reference's"));

      logger.log(ERROR_LEVEL, "ClassAnnotations of {0} NOK: {1}",
          new Object[] {className, errorString});

        boolean onlyAnonymousMemberClassesAnnot = true;
        if (classAnnotations != null) {
          AnnotationItem[] annots = classAnnotations.getAnnotations();
          for (AnnotationItem annot : annots) {
            TypeIdItem type = annot.getEncodedAnnotation().annotationType;
            boolean isMemberClassesAnnot =
                type.getTypeDescriptor().equals(MEMBER_CLASSES_DESCRIPTOR);
            if (isMemberClassesAnnot) {
              onlyAnonymousMemberClassesAnnot &=
                  checkMemberClassesAnnotationOnlyHasAnonymousOrSynthetics(
                      annot.getEncodedAnnotation());
            } else {
              onlyAnonymousMemberClassesAnnot = false;
            }
            if (!onlyAnonymousMemberClassesAnnot) {
              break;
            }
          }
        }

      if (!onlyAnonymousMemberClassesAnnot) {
        throw new DifferenceFoundException(
            "ClassAnnotations of '" + className + "' do not match: " + errorString);
      } else {
        return;
      }
    } else {
        compareElementAnnotationDelegate.reset();
        compareElementAnnotationDelegate.isCandidate = true;
        compareElementAnnotationDelegate.processGeneric(
            "class " + className,
            candidateClassAnnotations);
        compareElementAnnotationDelegate.isCandidate = false;
        compareElementAnnotationDelegate.processGeneric(
            "class " + className, classAnnotations);
    }
  }

  private static boolean checkMemberClassesAnnotationOnlyHasAnonymousOrSynthetics(
      AnnotationEncodedSubValue encodedAnnotation) {
    boolean result = true;
    for (EncodedValue value : encodedAnnotation.values) {
      for (EncodedValue subvalue : ((ArrayEncodedValue) value).values) {
        TypeIdItem typeId = ((TypeEncodedValue) subvalue).value;
        result &= isAnonymousTypeName(typeId.getTypeDescriptor());
        if (!result) {
          break;
        }
      }
    }
    return result;
  }

  /**
   * Compares annotations for each element (field method) in reference. Please re use instance for
   * performance's sake (call reset() beforehand)
   *
   */
  private static class CompareElementAnnotation  {

    public boolean isCandidate = false;
    @CheckForNull
    public ClassDataItem classData = null;

    @Nonnull
    private final HashMap<String, AnnotationSetItem> candidateElementAnnotations =
        new HashMap<String, AnnotationSetItem>();

    @Nonnull
    private final Logger logger;

    public CompareElementAnnotation(Logger logger) {
      this.logger = logger;
    }

    public void processGeneric(@Nonnull String elementString,
        @Nonnull AnnotationSetItem annotations) throws DifferenceFoundException {

      if (isCandidate) {
        /* fill lookup table */
        candidateElementAnnotations.put(elementString, annotations);
      } else {

        /* reference : compare against lookup table */
        AnnotationSetItem candidateAnnotationsForField =
            candidateElementAnnotations.get(elementString);
        AnnotationItem[] candidatesAnnots =
            (candidateAnnotationsForField != null) ? (candidateAnnotationsForField.getAnnotations())
                : (new AnnotationItem[] {});
        AnnotationItem[] refAnnots = annotations.getAnnotations();

        for (int i = 0; i < refAnnots.length; i++) {
        //for (AnnotationItem annot : refAnnots) {
          AnnotationItem annot = refAnnots[i];

          // if the annotation is a MemberClass annotation that only contains anonymous classes,
          // it may not be in our candidate so we ignore it
          boolean isMemberClassAnnotation = annot
              .getEncodedAnnotation().annotationType.getTypeDescriptor()
              .equals(MEMBER_CLASSES_DESCRIPTOR);
          if (IGNORE_ANONYMOUS_CLASSES && isMemberClassAnnotation) {
            if (checkMemberClassesAnnotationOnlyHasAnonymousOrSynthetics(annot
                .getEncodedAnnotation())) {
              continue;
            }
          }
          boolean isAnnotFound = false;
          for (AnnotationItem candidateAnnot : candidatesAnnots) {
            /* found the same annotation for this element? (based on its type) */
            if (compareTypeIds(annot.getEncodedAnnotation().annotationType,
                candidateAnnot.getEncodedAnnotation().annotationType)) {
              checkEncodedAnnotations(annot.getEncodedAnnotation(),
                  candidateAnnot.getEncodedAnnotation(), elementString);
              isAnnotFound = true;
              break;
            }
          }

          if (!isAnnotFound) {
            if ((!TOLERATE_MISSING_INITS || !elementString.contains("<init>")) &&
                (!TOLERATE_MISSING_CLINITS || !elementString.contains("<clinit>"))) {
            throw new DifferenceFoundException("NOK: for " + elementString
                + ": Could not find annotation "
                + annot.getEncodedAnnotation().annotationType.getTypeDescriptor()
                + " in candidate.");
            }
          }
        }
      }
    }

    private boolean compareValues(EncodedValue encodedValue, EncodedValue candEncodedValue,
        String elementString, String type, String name) throws DifferenceFoundException {
      boolean isEqual = false;
      if (encodedValue.getValueType().value == candEncodedValue.getValueType().value) {
        if (encodedValue instanceof StringEncodedValue) {
          isEqual = ((StringEncodedValue) encodedValue).value.getStringValue()
              .equals(((StringEncodedValue) candEncodedValue).value.getStringValue());
        } else if (encodedValue instanceof TypeEncodedValue) {
          isEqual = compareTypeIds(
              ((TypeEncodedValue) encodedValue).value, ((TypeEncodedValue) candEncodedValue).value);
        } else if (encodedValue instanceof EnumEncodedValue) {
          isEqual = compareFieldIds(
              ((EnumEncodedValue) encodedValue).value, ((EnumEncodedValue) candEncodedValue).value);
        } else if (encodedValue instanceof MethodEncodedValue) {
          isEqual = compareMethodIds(((MethodEncodedValue) encodedValue).value,
              ((MethodEncodedValue) candEncodedValue).value);
        } else if (encodedValue instanceof AnnotationEncodedSubValue) {
          AnnotationEncodedSubValue encodedAnnotation =
              (AnnotationEncodedSubValue) encodedValue;
          AnnotationEncodedSubValue candEncodedAnnotation =
              (AnnotationEncodedSubValue) candEncodedValue;
          boolean sameAnnotationType = compareTypeIds(
              encodedAnnotation.annotationType, candEncodedAnnotation.annotationType);
          if (sameAnnotationType) {
            checkEncodedAnnotations(encodedAnnotation, candEncodedAnnotation, elementString);
            isEqual = true;
          } else {
            throw new AssertionError("sub annotation types do not match");
          }
        } else if (encodedValue instanceof ArrayEncodedSubValue) {
          ArrayEncodedSubValue arrayEncodedValue = (ArrayEncodedSubValue) encodedValue;
          ArrayEncodedSubValue candArrayEncodedValue = (ArrayEncodedSubValue) candEncodedValue;

          List<EncodedValue> refEncodedValues = Arrays.asList(arrayEncodedValue.values);
          List<EncodedValue> candEncodedValues = Arrays.asList(candArrayEncodedValue.values);

          // With system annotations we should not take the order of the values in the encoded array
          // into account because it is synthetic anyway. We only need to avoid "MemberClasses" and
          // "Throws" because they're the only ones that contain encoded arrays.
          boolean doNotCompareWithOrder =
              type.equals(MEMBER_CLASSES_DESCRIPTOR) || type.equals(THROWS_DESCRIPTOR);

          if (doNotCompareWithOrder) {
            isEqual = compareUnorderedEncodedValues(refEncodedValues, candEncodedValues,
                elementString, type, name);
          } else {
            isEqual = compareOrderedEncodedValues(refEncodedValues, candEncodedValues,
                elementString, type, name);
          }

        } else {
          isEqual = encodedValue.compareTo(candEncodedValue) == 0;
        }

        if (!isEqual) {
          // print access flags of InnerClass annotations
          if (type.equals("Ldalvik/annotation/InnerClass;") && name.equals("accessFlags")) {
            int flags = ((IntEncodedValue) encodedValue).value;
            int candFlags = ((IntEncodedValue) candEncodedValue).value;
            logger.log(DEBUG_LEVEL, "Difference in access flags of InnerClass annotations of "
                + elementString + ": ref=0x" + Integer.toHexString(flags) + " - cand=0x"
                + Integer.toHexString(candFlags));
          }

          throw new DifferenceFoundException("NOK: for " + elementString + ":annotation "
              + type
              + ", name = " + name
              + " is present but values differ");
        }
      } else {
        throw new DifferenceFoundException("NOK: for " + elementString + ":annotation "
            + type
            + ", name = " + name
            + ", encoded values have different types");
      }
      return isEqual;
    }

    private boolean compareUnorderedEncodedValues(List<EncodedValue> refEncodedValues,
        List<EncodedValue> candEncodedValues, String elementString, String type, String name)
        throws DifferenceFoundException {
      Collections.sort(refEncodedValues);
      Collections.sort(candEncodedValues);
      return compareOrderedEncodedValues(refEncodedValues, candEncodedValues, elementString, type,
          name);
    }

    private boolean compareOrderedEncodedValues(List<EncodedValue> refEncodedValues,
        List<EncodedValue> candEncodedValues, String elementString, String type, String name)
        throws DifferenceFoundException {
      boolean isEqual = true;
      boolean isMemberClass = type.equals(MEMBER_CLASSES_DESCRIPTOR);
      Iterator<EncodedValue> refEncodedValuesIterator = refEncodedValues.iterator();
      Iterator<EncodedValue> candEncodedValuesIterator = candEncodedValues.iterator();

      while (refEncodedValuesIterator.hasNext()) {
        if (!candEncodedValuesIterator.hasNext()) {
          isEqual = false;
          break;
        }
        EncodedValue refValue = refEncodedValuesIterator.next();

        // our reference may have additional synthetic anonymous classes that we should ignore
        if (isMemberClass) {
          TypeEncodedValue typeRefValue = (TypeEncodedValue) refValue;
          if (isAnonymousTypeName(typeRefValue.value.getTypeDescriptor())) {
            continue;
          }
        }

        EncodedValue candValue = candEncodedValuesIterator.next();
        isEqual = compareValues(refValue, candValue, elementString, type, name);
        if (!isEqual) {
          break;
        }
      }
      if (candEncodedValuesIterator.hasNext()) {
        isEqual = false;
      }
      return isEqual;
    }

    private boolean compareMethodIds(MethodIdItem value, MethodIdItem value2) {
      boolean isEqual = compareTypeIds(value.getContainingClass(), value2.getContainingClass());
      isEqual &= compareProtoIds(value.getPrototype(), value2.getPrototype());
      isEqual &=
          value.getMethodName().getStringValue().equals(value2.getMethodName().getStringValue());
      return isEqual;
    }

    private boolean compareProtoIds(ProtoIdItem value, ProtoIdItem value2) {
      boolean isEqual = false;
      if (value == null && value2 == null) {
        isEqual = true;
      } else if (value != null && value2 != null) {
        if (compareTypeIds(value.getReturnType(), value2.getReturnType())) {
          TypeListItem tyleList = value.getParameters();
          TypeListItem tyleList2 = value2.getParameters();
          if (tyleList == null && tyleList2 == null) {
            isEqual = true;
          } else if (tyleList != null && tyleList2 != null) {
            if (tyleList.getTypeCount() == tyleList2.getTypeCount()) {
              Iterator<TypeIdItem> tyleListIter = tyleList.getTypes().iterator();
              Iterator<TypeIdItem> tyleListIter2 = tyleList2.getTypes().iterator();
              isEqual = true;
              while (isEqual && tyleListIter.hasNext()) {
                isEqual &= compareTypeIds(tyleListIter.next(), tyleListIter2.next());
              }
            }
          }
        }
      }
      return isEqual;
    }

    private boolean compareFieldIds(FieldIdItem value, FieldIdItem value2) {
      boolean isEqual = compareTypeIds(value.getContainingClass(), value2.getContainingClass());
      isEqual &= compareTypeIds(value.getFieldType(), value2.getFieldType());
      isEqual &=
          value.getFieldName().getStringValue().equals(value2.getFieldName().getStringValue());
      return isEqual;
    }

    private boolean compareTypeIds(TypeIdItem annotationType, TypeIdItem annotationType2) {
      return annotationType.getTypeDescriptor().equals(annotationType2.getTypeDescriptor());
    }

    private void checkEncodedAnnotations(AnnotationEncodedSubValue encodedAnnotation,
        AnnotationEncodedSubValue encodedAnnotation2, String elementString)
        throws DifferenceFoundException {
      assert compareTypeIds(encodedAnnotation.annotationType, encodedAnnotation2.annotationType);

      /* check every name and values for this annotation */
      for (int i = 0; i < encodedAnnotation.names.length; i++) {
        StringIdItem name = encodedAnnotation.names[i];
        boolean isAnnotNameFound = false;
        for (int j = 0; !isAnnotNameFound && j < encodedAnnotation2.names.length; j++) {
          StringIdItem candidateName = encodedAnnotation2.names[j];
          if (name.getStringValue().equals(candidateName.getStringValue())) {
            EncodedValue subSubValue = encodedAnnotation.values[i];
            EncodedValue candSubSubValue = encodedAnnotation2.values[j];
            boolean isEqual = compareValues(subSubValue, candSubSubValue, elementString,
                encodedAnnotation.annotationType.getTypeDescriptor(), name.getStringValue());
            if (!isEqual) {
              throw new DifferenceFoundException("NOK: for " + elementString + ":annotation "
                  + encodedAnnotation.annotationType.getTypeDescriptor()
                  + ", value type differs : reference = "
                  + encodedAnnotation.getValueType().toString() + ", candidate = "
                  + encodedAnnotation2.getValueType().toString());
            }
            isAnnotNameFound = true;
            break; // We found the right name, stop searching
          }
        }

        if (!isAnnotNameFound) {
          throw new DifferenceFoundException("NOK: for " + elementString + ": Could not find name "
              + name.getStringValue() + " for annotation "
              + encodedAnnotation.annotationType.getTypeDescriptor() + " in candidate.");
        }
      }
    }

    public void processFieldAnnotations(FieldIdItem field, AnnotationSetItem fieldAnnotations) {
      try {
        processGeneric("field " + field.getFieldString(), fieldAnnotations);
      } catch (DifferenceFoundException e) {
        //TODO
        throw new RuntimeException(e);
      }
    }

    public void processMethodAnnotations(MethodIdItem method, AnnotationSetItem annotations) {
      try {
        String elementString = "method " + method.getMethodString();

        if (isCandidate) {
          /* fill lookup table */
          candidateElementAnnotations.put(elementString, annotations);
        } else {
          assert classData != null;
          boolean synthetic = isSynthetic(getMethodAccessFlags(classData, method));

          /* reference : compare against lookup table */
          AnnotationSetItem candidateAnnotationsForField =
              candidateElementAnnotations.get(elementString);
          AnnotationItem[] candidatesAnnots =
              (candidateAnnotationsForField != null) ? (candidateAnnotationsForField
                  .getAnnotations())
                  : (new AnnotationItem[] {});
          AnnotationItem[] refAnnots = annotations.getAnnotations();

          for (AnnotationItem annot : refAnnots) {
            boolean isAnnotFound = false;
            for (AnnotationItem candidateAnnot : candidatesAnnots) {
              /* found the same annotation for this element? (based on its type) */
              if (compareTypeIds(annot.getEncodedAnnotation().annotationType,
                  candidateAnnot.getEncodedAnnotation().annotationType)) {
                checkEncodedAnnotations(annot.getEncodedAnnotation(),
                    candidateAnnot.getEncodedAnnotation(), elementString);
                isAnnotFound = true;
                break;
              }
            }

            //TODO
            if (!isAnnotFound) {
              if ((!TOLERATE_MISSING_SYNTHETICS || !synthetic) &&
                  (!TOLERATE_MISSING_INITS || !elementString.contains("<init>")) &&
                  (!TOLERATE_MISSING_CLINITS || !elementString.contains("<clinit>"))) {
              throw new DifferenceFoundException("NOK: for " + elementString
                  + ": Could not find annotation "
                  + annot.getEncodedAnnotation().annotationType.getTypeDescriptor()
                  + " in candidate.");
              }
            }
          }
        }
      } catch (DifferenceFoundException e) {
        //TODO
        throw new RuntimeException(e);
      }
    }

    public void processParameterAnnotations(
        MethodIdItem method, AnnotationSetRefList parameterAnnotations) {
      int cptParam = 0;
      for (AnnotationSetItem asi : parameterAnnotations.getAnnotationSets()) {
        try {
          processGeneric("param " + cptParam + " " + method.getMethodString(), asi);
        } catch (DifferenceFoundException e) {
          // TODO
          throw new RuntimeException(e);
        }
      }
    }

    public void reset() {
      isCandidate = false;
      candidateElementAnnotations.clear();
    }
  }

  private static boolean isAnonymousTypeName(String typeName) {
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

  private static int getMethodAccessFlags(ClassDataItem classData, MethodIdItem methodId) {
    for (EncodedMethod encodedMethod : classData.getDirectMethods()) {
      if (encodedMethod.method == methodId) {
        return encodedMethod.accessFlags;
      }
    }
    for (EncodedMethod encodedMethod : classData.getVirtualMethods()) {
      if (encodedMethod.method == methodId) {
        return encodedMethod.accessFlags;
      }
    }
    return -1;
  }
}