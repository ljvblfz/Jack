/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.transformations.enums.opt;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.android.jack.Jack;
import com.android.jack.ir.ast.FieldKind;
import com.android.jack.ir.ast.JArrayLength;
import com.android.jack.ir.ast.JArrayRef;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JBinaryOperator;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JCatchBlock;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedEnum;
import com.android.jack.ir.ast.JEnumField;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldId;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JIntLiteral;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JNewArray;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JThis;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.ir.ast.JTryStatement;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.library.TypeInInputLibraryLocation;
import com.android.jack.lookup.JMethodLookupException;
import com.android.jack.transformations.LocalVarCreator;
import com.android.jack.transformations.enums.EnumMappingMarker;
import com.android.jack.transformations.enums.SwitchEnumSupport.UsedEnumField;
import com.android.jack.transformations.request.AppendField;
import com.android.jack.transformations.request.AppendMethod;
import com.android.jack.transformations.request.AppendStatement;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.NamingTools;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Input: the input of this class is the enum. Because the switch map of an enum
 * is shared by all the classes using this enum, the field and initializer could
 * be generated based on a given type of enum.
 *
 * Output: The output of this class is the synthetic class which includes the
 * following parts of code
 *
 * <li>
 * 1. class structure. Its super class is Ljava/lang/Object; This class may
 * be emit so that it could be used in the future (no need to generate this class
 * if there exists already).
 * </li>
 *
 * <li>
 * 2. methods. Two methods are required: switch map initializer and instance init.
 * </li>
 * <br> instance init method will be added if the enclosing class is synthetic class
 * <pre><code>
 *   public init() {
 *     super.init();
 *     return;
 *   }
 * }
 * </code></pre>
 *
 * <br> switch map initializer is of type: public static int[] -getA-B-EnumSwitchesValues(),
 * where A and B are the package and sub-package of Enum.
 *
 * <li>
 * 3. switch map field. This field is set as a field of this class, e.g., private static
 * synthetic int[] -A-B-EnumSwitchesValues.
 * </li>
 */
public class SwitchMapClassFiller {
  // the enum type for which synthetic class will be created
  @CheckForNull
  private JDefinedEnum enumType;

  // the synthetic switch map class
  @CheckForNull
  private JDefinedClass switchMapClass;

  // the synthetic switch map initializer
  @CheckForNull
  private JMethod syntheticSwitchMapInitializer;

  // the synthetic switch map field
  @CheckForNull
  private JField syntheticSwitchMapField;

  @Nonnull
  private final JSession session = Jack.getSession();

  public SwitchMapClassFiller() {}

  /**
   * Emit code for:
   * <li> 1. instance init if this class is synthetic class </li>
   * <li> 2. switch map initializer </li>
   * <li> 3. switch map field </li>
   * @param enumType the enum type
   * @param switchMapClass the synthetic switch map class to which the code will be emit
   *
   */
  public synchronized void fillSwitchMapClass(
      @Nonnull JDefinedEnum enumType,
      @Nonnull JDefinedClass switchMapClass) {
    this.enumType = enumType;
    this.switchMapClass = switchMapClass;

    TransformationRequest transformRequest = new TransformationRequest(switchMapClass);

    if (switchMapClass.getName().startsWith(
        SyntheticClassManager.SyntheticSwitchmapClassNamePrefix)) {
      // add <init> if it is synthetic switch map class. It provides way to create
      // instance of this class
      createInstanceInitIfNotExists(transformRequest);
    }

    // create or update switch map initializer
    createOrUpdateSyntheticInitializer(transformRequest);

    transformRequest.commit();
  }

  /**
   * Create instance init method for the given class. As constructor, this method should
   * also needs to invoke the super().
   *
   * @param transformRequest Transformation request used to add/delete/modify code
   *
   */
  private void createInstanceInitIfNotExists(@Nonnull TransformationRequest transformRequest) {
    assert switchMapClass != null;
    try {
      switchMapClass.getMethod(NamingTools.INIT_NAME, JPrimitiveTypeEnum.VOID.getType());
    } catch (JMethodLookupException ex) {
      // the instance init method of synthetic switch map class
      JMethod initMethod = new JConstructor(SourceInfo.UNKNOWN, switchMapClass, JModifier.PUBLIC);

      // set method body and block
      JBlock block = new JBlock(SourceInfo.UNKNOWN);
      JMethodBody body = new JMethodBody(SourceInfo.UNKNOWN, block);
      initMethod.setBody(body);
      body.updateParents(initMethod);

      // call the instance init of super class
      JDefinedClass objectClass = session.getLookup().getClass("Ljava/lang/Object;");
      JMethod superConstruct =
          objectClass.getMethod(NamingTools.INIT_NAME, JPrimitiveTypeEnum.VOID.getType());

      JThis thisLocal = initMethod.getThis();
      assert thisLocal != null;
      JThisRef thisRef = new JThisRef(SourceInfo.UNKNOWN, thisLocal);

      // create method call to super class default constructor
      JMethodCall superCall =
          new JMethodCall(SourceInfo.UNKNOWN, thisRef, objectClass, superConstruct.getMethodId(),
          JPrimitiveTypeEnum.VOID.getType(), superConstruct.canBePolymorphic());
      transformRequest.append(new AppendStatement(block, superCall.makeStatement()));

      transformRequest.append(
          new AppendStatement(block, new JReturnStatement(SourceInfo.UNKNOWN, null)));

      transformRequest.append(new AppendMethod(switchMapClass, initMethod));
    }
  }

  /**
   * Add synthetic switch map method which initializes switch map int array field.
   *
   * @param transformRequest Transformation request used to add/delete/modify code
   */
  private void createOrUpdateSyntheticInitializer(@Nonnull TransformationRequest transformRequest) {
    assert enumType != null;
    assert switchMapClass != null;
    assert enumType.containsMarker(EnumFieldMarker.class);

    String methodName = OptimizationUtil.getSyntheticSwitchMapInitializerName(enumType);
    try {
      syntheticSwitchMapInitializer =
          switchMapClass.getMethod(methodName, JPrimitiveTypeEnum.INT.getType().getArray());

      String fieldName = OptimizationUtil.getSyntheticSwitchMapFieldName(enumType);

      JFieldId syntheticSwitchMapFieldId = switchMapClass.getFieldId(
          fieldName, JPrimitiveTypeEnum.INT.getType().getArray(), FieldKind.STATIC);

      assert syntheticSwitchMapFieldId != null;

      syntheticSwitchMapField = syntheticSwitchMapFieldId.getField();
    } catch (JMethodLookupException ex) {
      // if the switch map initializer is not declared before, define it and corresponding
      // switch map field at this point
      String fieldName = OptimizationUtil.getSyntheticSwitchMapFieldName(enumType);

      syntheticSwitchMapField = new JField(SourceInfo.UNKNOWN, fieldName, switchMapClass,
          JPrimitiveTypeEnum.INT.getType().getArray(),
          JModifier.PRIVATE | JModifier.STATIC | JModifier.SYNTHETIC);

      transformRequest.append(new AppendField(switchMapClass, syntheticSwitchMapField));
      // create synthetic switch map initializer
      syntheticSwitchMapInitializer =
          new JMethod(SourceInfo.UNKNOWN, new JMethodId(methodName, MethodKind.STATIC),
              switchMapClass, JPrimitiveTypeEnum.INT.getType().getArray(),
              JModifier.PUBLIC | JModifier.STATIC | JModifier.SYNTHETIC);

      transformRequest.append(new AppendMethod(switchMapClass, syntheticSwitchMapInitializer));
    }

    if (!syntheticSwitchMapInitializer.containsMarker(EnumMappingMarker.class)) {
      // 1. if the switch map class is not the user class, make sure all the enum literals are
      // sorted by alphabetic order because we want to ensure the switch map array is always
      // the same across different user classes.
      //
      // 2. if the switch map class is the user class, we can use packed switch statement
      //
      // as the document points out, this is one side-affect of our optimization, it replace
      // packed switch with sparse switch and potentially it will hurt the runtime performance.
      // Through the experiment performed, the performance panic is less than 5%
      createOrdinalMapping(!switchMapClass.getName().
          startsWith(SyntheticClassManager.SyntheticSwitchmapClassNamePrefix));

      assert switchMapClass != null;
      if (!(switchMapClass.getLocation() instanceof TypeInInputLibraryLocation)) {
        assert syntheticSwitchMapInitializer != null;
        syntheticSwitchMapInitializer.setBody(null);
        fillSyntheticSwitchMapInitializer(transformRequest);
      }
    }
  }

  /**
   * Create the mapping relationship between static enum field to a random integer called
   * compile-time ordinal.
   * @param packedSwitch will packed switch statement be applied
   */
  private void createOrdinalMapping(boolean packedSwitch) {
    assert enumType != null;
    assert switchMapClass != null;
    assert syntheticSwitchMapInitializer != null;
    assert !syntheticSwitchMapInitializer.containsMarker(EnumMappingMarker.class);
    assert enumType.containsMarker(EnumFieldMarker.class);

    int packedCompileTimeOrdinal = 1;
    int unpackedCompileTimeOrdinal = 1;
    Set<JFieldId> usedEnumFields = Sets.newHashSet();
    EnumFieldMarker enumFieldMarker = enumType.getMarker(EnumFieldMarker.class);

    if (packedSwitch && switchMapClass.containsMarker(UsedEnumField.class)) {
      // if packed switch statement is enabled, try packed switch approach
      UsedEnumField usedFieldMarker = switchMapClass.getMarker(UsedEnumField.class);
      assert usedFieldMarker != null;
      usedEnumFields.addAll(usedFieldMarker.getEnumFields());
      unpackedCompileTimeOrdinal = usedEnumFields.size() + 1;
    } else {
      // if the packed switch statement is disabled, we have to make sure the dynamic ordinal
      // is assigned incrementally by the enum fields alphabetical order
      enumFieldMarker.sortEnumFields();
    }

    EnumMappingMarker mappingMarker = new EnumMappingMarker();
    syntheticSwitchMapInitializer.addMarker(mappingMarker);
    for (JEnumField enumField : enumFieldMarker.getEnumFields()) {
      JFieldId enumFieldId = enumField.getId();
      if (usedEnumFields.contains(enumField.getId())) {
        // because this field is used, assign the packed ordinal to it
        mappingMarker.addMapping(enumFieldId, packedCompileTimeOrdinal++);
      } else {
        // because current field is not used, assign unpacked ordinal to it
        mappingMarker.addMapping(enumFieldId, unpackedCompileTimeOrdinal++);
      }
    }
  }

  /**
   * Fill statements into the newly created synthetic switch map initializer.
   *
   * @param transformRequest Transformation request used to add/delete/modify code
   */
  private void fillSyntheticSwitchMapInitializer(
      @Nonnull TransformationRequest transformRequest) {
    assert switchMapClass != null;
    assert syntheticSwitchMapField != null;
    assert syntheticSwitchMapInitializer != null;
    assert syntheticSwitchMapInitializer.getBody() == null;

    JBlock bodyBlock = new JBlock(SourceInfo.UNKNOWN);
    JMethodBody methodBody = new JMethodBody(SourceInfo.UNKNOWN, bodyBlock);
    syntheticSwitchMapInitializer.setBody(methodBody);
    methodBody.updateParents(syntheticSwitchMapInitializer);

    LocalVarCreator localVarCreator = new LocalVarCreator(syntheticSwitchMapInitializer, "loc");

    /**
     * int[] switchmap = field reference to switch map
     * if (switchmap != null) {
     *   return switchmap;
     * }
     * switchmap = new int[enum.values().length];
     * switchmap[Enum1.ordinal()] = compileTimeOrdinal1;
     * switchmap[Enum2.ordinal()] = compileTimeOrdinal2;
     * ...
     * return switchmap;
     */
    JExpression checkNullExpr = JBinaryOperation.create(SourceInfo.UNKNOWN, JBinaryOperator.NEQ,
        new JFieldRef(SourceInfo.UNKNOWN, null /*static field*/, syntheticSwitchMapField.getId(),
            switchMapClass),
        new JNullLiteral(SourceInfo.UNKNOWN));

    // if field is not null, then return it
    JBlock thenBlock = new JBlock(SourceInfo.UNKNOWN);
    transformRequest.append(new AppendStatement(thenBlock, new JReturnStatement(SourceInfo.UNKNOWN,
        new JFieldRef(SourceInfo.UNKNOWN, null /*static field*/, syntheticSwitchMapField.getId(),
            switchMapClass))));

    JStatement ifStmt = new JIfStatement(SourceInfo.UNKNOWN, checkNullExpr, thenBlock, null
        /*no else block*/);
    transformRequest.append(new AppendStatement(bodyBlock, ifStmt));

    // if the switch map field is null, create switch map initializing function to initialize it
    createSwitchMapInitializerBlock(transformRequest, localVarCreator, bodyBlock);
  }

  /**
   * Emit code for the block in synthetic method, which will initialize the switch map.
   * @param transformRequest transformation request used to add/delete/modify code
   * @param localVarCreator local creator
   * @param initializerMethodBlock synthetic switch map initializer method body block
   */
  private void createSwitchMapInitializerBlock(
      @Nonnull TransformationRequest transformRequest,
      @Nonnull LocalVarCreator localVarCreator,
      @Nonnull JBlock initializerMethodBlock) {
    assert enumType != null;

    JMethod valuesMethod = enumType.getMethod("values", enumType.getArray());

    JExpression valuesLength = new JArrayLength(
        SourceInfo.UNKNOWN, new JMethodCall(SourceInfo.UNKNOWN, null /* instance */, enumType,
            valuesMethod.getMethodId(), valuesMethod.getType(), valuesMethod.canBePolymorphic()));

    JLocal switchmapLocal = createSwitchmapArrayStatement(
        localVarCreator, transformRequest, initializerMethodBlock, valuesLength);

    // put the switch map object back to the field
    assert syntheticSwitchMapField != null;
    assert switchMapClass != null;
    JStatement putStaticFieldStmt = new JExpressionStatement(SourceInfo.UNKNOWN,
        JBinaryOperation.create(SourceInfo.UNKNOWN, JBinaryOperator.ASG,
            new JFieldRef(SourceInfo.UNKNOWN, null /*static field*/,
                syntheticSwitchMapField.getId(), switchMapClass),
            new JLocalRef(SourceInfo.UNKNOWN, switchmapLocal)));
    transformRequest.append(new AppendStatement(initializerMethodBlock, putStaticFieldStmt));

    createStatementsInitializingSwitchMapArrayField(
        transformRequest, localVarCreator, initializerMethodBlock, switchmapLocal);

    transformRequest.append(new AppendStatement(initializerMethodBlock, new JReturnStatement(
        SourceInfo.UNKNOWN, new JLocalRef(SourceInfo.UNKNOWN, switchmapLocal))));
  }

  /**
   * Initialize the synthetic switch map int[] field.
   * @param transformRequest Transformation utility
   * @param localVarCreator JLocal creator
   * @param block Current block to emit code to
   * @param switchmapLocal Switch map int[] local
   */
  private void createStatementsInitializingSwitchMapArrayField(
      @Nonnull TransformationRequest transformRequest,
      @Nonnull LocalVarCreator localVarCreator,
      @Nonnull JBlock block,
      @Nonnull JLocal switchmapLocal) {
    assert syntheticSwitchMapInitializer != null;

    // Use for loop to find the field name, and build the corresponding relationship between
    // enum ordinal and compile-time ordinal
    EnumMappingMarker enumMappingMarker =
        syntheticSwitchMapInitializer.getMarker(EnumMappingMarker.class);

    assert enumMappingMarker != null;
    Map<JFieldId, Integer> enumFieldsMap = enumMappingMarker.getMapping();

    // for each field inside of map, emit code to explicitly map it to compile time ordinal
    for (Map.Entry<JFieldId, Integer> enumFieldEntry : enumFieldsMap.entrySet()) {
      JBlock tryBlock = new JBlock(SourceInfo.UNKNOWN);
      JCatchBlock noSuchFieldExCatchBlock =
          createCatchBlock(session.getLookup().getClass("Ljava/lang/NoSuchFieldError;"));
      // get field and compile time ordinal which has been calculated already
      JFieldId enumFieldId = enumFieldEntry.getKey();
      Integer compileTimeOrdinal = enumFieldEntry.getValue();

      assert enumType != null;
      // switchmap[staticOrdinal] = compileTimeOrdinal
      JExpression enumFieldExpr =
          new JFieldRef(SourceInfo.UNKNOWN, null /*static field*/, enumFieldId, enumType);

      JMethod ordinalMethod = session.getLookup().getClass("Ljava/lang/Enum;").
          getMethod("ordinal", JPrimitiveTypeEnum.INT.getType());

      JExpression invocOrdinalExpr =
          new JMethodCall(SourceInfo.UNKNOWN, enumFieldExpr, enumType, ordinalMethod.getMethodId(),
              JPrimitiveTypeEnum.INT.getType(), ordinalMethod.canBePolymorphic());

      transformRequest.append(new AppendStatement(tryBlock,
          JBinaryOperation.create(SourceInfo.UNKNOWN, JBinaryOperator.ASG,
            new JArrayRef(SourceInfo.UNKNOWN, new JLocalRef(SourceInfo.UNKNOWN, switchmapLocal),
                invocOrdinalExpr),
            new JIntLiteral(SourceInfo.UNKNOWN, compileTimeOrdinal.intValue()))
            .makeStatement()));

      transformRequest.append(new AppendStatement(block,
          new JTryStatement(SourceInfo.UNKNOWN, Collections.<JStatement>emptyList(), tryBlock,
              Lists.newArrayList(noSuchFieldExCatchBlock), null)));
    }
  }

  /**
   * Emit code for statement initializing the array of certain length, e.g.,
   * r0 = new int[capacity];
   *
   * @param localVarCreator JLocal creator
   * @param transformRequest Transformation request used to add/delete/modify code
   * @param block Current block
   * @param capacityExpr The expression indicating the initial capacity of array
   *
   * @return the switch map int array local variable
   */
  private JLocal createSwitchmapArrayStatement(
      @Nonnull LocalVarCreator localVarCreator,
      @Nonnull TransformationRequest transformRequest,
      @Nonnull JBlock block,
      @Nonnull JExpression capacityExpr) {
    List<JExpression> dims = Lists.newArrayList(capacityExpr);

    JExpression newArrayExpr = JNewArray.createWithDims(
        SourceInfo.UNKNOWN, JPrimitiveTypeEnum.INT.getType().getArray(), dims);

    JLocal switchMapLocal = localVarCreator.createTempLocal(
        JPrimitiveTypeEnum.INT.getType().getArray(), SourceInfo.UNKNOWN, transformRequest);

    JStatement newArrayStmt = new JExpressionStatement(
        SourceInfo.UNKNOWN, JBinaryOperation.create(SourceInfo.UNKNOWN, JBinaryOperator.ASG,
            new JLocalRef(SourceInfo.UNKNOWN, switchMapLocal), newArrayExpr));

    transformRequest.append(new AppendStatement(block, newArrayStmt));

    return switchMapLocal;
  }

  /**
   * Emit code for the catch block given an exception type and enclosing method, e.g.,
   * catch(exceptionType e) {
   * }
   *
   * @param exceptionType The type of exception
   *
   * @return The catch block
   */
  private JCatchBlock createCatchBlock(@Nonnull JDefinedClass exceptionType) {
    assert syntheticSwitchMapInitializer != null;

    JLocal catchLocal = new JLocal(SourceInfo.UNKNOWN, "ex", exceptionType,
        JModifier.SYNTHETIC, (JMethodBody) syntheticSwitchMapInitializer.getBody());

    JCatchBlock catchBlock = new JCatchBlock(
        SourceInfo.UNKNOWN, Collections.<JClass>singletonList(exceptionType), catchLocal);
    return catchBlock;
  }
}