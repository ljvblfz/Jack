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

import com.android.jack.ir.ast.JAbstractMethodBody;
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
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JThis;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.ir.ast.JTryStatement;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.lookup.JMethodLookupException;
import com.android.jack.transformations.LocalVarCreator;
import com.android.jack.transformations.enums.EnumMappingMarker;
import com.android.jack.transformations.enums.OptimizationUtil;
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

import javax.annotation.Nonnull;

/**
 * Input: the input of this class is the enum. Because the switch map of an enum
 * is shared by all the classes using this enum, the field and initializer could
 * be generated based on a given enum
 *
 * Output: The output of this class is the synthetic class which includes the
 * following parts of code
 *
 * <li> 1. class structure. Its super class is Ljava/lang/Object; This class must
 * be emit so that it could be used in the future. </li>
 *
 * <li> 2. methods. Two methods are required: switch map initializer and init. </li>
 * <br> init could simply contains two statements
 * {@code
 *   public init() {
 *     super.init();
 *     return;
 *   }
 * }
 *
 * <br> switch map initializer is of type:
 * {@code -getA_B_EnumSwitchesValues() {...} }
 *
 * It is defined as synchronized method because it may be accessed in parallel.
 *
 * <li> 3. switch map field. This field is set as a field of this class, e.g., private
 * static synthetic int[] -A_B_EnumSwitchesValues. It follows the pattern of A/B/Enum is the
 * full class name for Enum, where A and B are packages' name. </li>
 */
public class SwitchMapClassFiller {
  // support util which provides several basic APIs for current compilation
  private OptimizationUtil supportUtil;

  // the enum type for which synthetic class is created
  private JDefinedEnum enumType;

  // the synthetic switch map class
  private JDefinedClass switchMapClass;

  // the synthetic switch map initializer corresponding to exactly a int[] field
  private JMethod syntheticSwitchMapInitializer;

  // the instance init method of synthetic switch map class
  private JMethod initMethod;

  // the synthetic switch map int[] field corresponding to exactly a initializer
  private JField syntheticSwitchMapField;

  public SwitchMapClassFiller() {}

  /**
   * Emit code of switch map int[] field, switch map initializer and instance initializer.
   * which includes:
   * <li> 1. init </li>
   * <li> 2. switch map initializer </li>
   * <li> 3. switch map field </li>
   * @param supportUtil The class provides basic APIs needed by current compilation
   * @param enumType The enum type
   * @param switchMapClass The synthetic switch map class to which the code will be emit
   *
   * @throws RuntimeException if this process fails
   */
  public synchronized void fillSwitchMapClass(
      @Nonnull OptimizationUtil supportUtil, @Nonnull JDefinedEnum enumType,
      @Nonnull JDefinedClass switchMapClass) {
    // set the input before generating the synthetic class
    this.enumType = enumType;
    this.supportUtil = supportUtil;
    this.switchMapClass = switchMapClass;

    TransformationRequest transformRequest = new TransformationRequest(switchMapClass);

    if (SyntheticClassManager.isSyntheticSwitchMapClass(switchMapClass)) {
      // add <init> if it is synthetic switch map class. It provides way to create
      // instance of this class
      createInstanceInitIfNotExists(transformRequest);
    }

    // add switch map initializer
    if (!addSyntheticInitializerIfNotExists(transformRequest)) {
      // fail the execution if adding synthetic initializer fails
      throw new AssertionError("Fails adding synthetic switch map initializer to class: "
          + switchMapClass);
    }

    // flush all the modified code
    transformRequest.commit();
  }

  /**
   * Add the synthetic switch map field to the synthetic class.
   * @param transformRequest Transformation request used to add/delete/modify code
   *
   * @return The created field
   */
  private JField addSyntheticField(@Nonnull TransformationRequest transformRequest) {
    String fieldName = OptimizationUtil.getSyntheticSwitchMapFieldName(enumType);
    List<JField> switchMapFields = switchMapClass.getFields(fieldName);
    if (!switchMapFields.isEmpty()) {
      throw new AssertionError("Pre-defined synthetic field: " + fieldName);
    }
    // if such field is never defined before, initialize it
    JField syntheticMapField = createSyntheticField();
    // add this field to synthetic class
    transformRequest.append(new AppendField(switchMapClass, syntheticMapField));
    return syntheticMapField;
  }

  /**
   * Create synthetic switch map field. At this time it hasn't been added into class yet.
   *
   * @return The created synthetic field
   */
  private JField createSyntheticField() {
    String fieldName = OptimizationUtil.getSyntheticSwitchMapFieldName(enumType);
    return new JField(SourceInfo.UNKNOWN, fieldName, switchMapClass,
        supportUtil.getPrimitiveIntType().getArray(),
        JModifier.PRIVATE | JModifier.STATIC | JModifier.SYNTHETIC);
  }

  /**
   * Create instance {@code <init>} method for the given class. As constructor, it should
   * also needs to invoke the super(). This method is added into switch map class as side
   * affect.
   * @param transformRequest Transformation request used to add/delete/modify code
   *
   * @return The instance init method
   */
  private JMethod createInstanceInitIfNotExists(@Nonnull TransformationRequest transformRequest) {
    try {
      initMethod = switchMapClass.getMethod(NamingTools.INIT_NAME, supportUtil
          .getPrimitiveVoidType());
    } catch (JMethodLookupException ex) {
      // create the empty constructor
      initMethod = new JConstructor(SourceInfo.UNKNOWN, switchMapClass, JModifier.PUBLIC);

      // set method body and block appropriately
      JBlock block = new JBlock(SourceInfo.UNKNOWN);
      JMethodBody body = new JMethodBody(SourceInfo.UNKNOWN, block);
      OptimizationUtil.setMethodBody(initMethod, body);

      // call to super class, as the super class is java/lang/Object
      JMethod superConstruct = supportUtil.getObjectType().getMethod(NamingTools.INIT_NAME,
          supportUtil.getPrimitiveVoidType());

      // get this local reference
      JThis thisLocal = initMethod.getThis();
      if (thisLocal == null) {
        throw new AssertionError("Cannot get 'this' local of method: " + initMethod);
      }
      JThisRef thisRef = new JThisRef(SourceInfo.UNKNOWN, thisLocal);

      // create method call to super class default constructor
      JMethodCall superCall = new JMethodCall(SourceInfo.UNKNOWN, thisRef,
          supportUtil.getObjectType(), superConstruct.getMethodId(),
          supportUtil.getPrimitiveVoidType(), superConstruct.canBePolymorphic());
      transformRequest.append(new AppendStatement(block, superCall.makeStatement()));

      // add a simple return void statement into the block
      transformRequest.append(new AppendStatement(block, new JReturnStatement(SourceInfo.UNKNOWN,
          null)));

      // add this method to the synthetic class
      transformRequest.append(new AppendMethod(switchMapClass, initMethod));
    }
    return initMethod;
  }

  /**
   * Add synthetic switch map method which initializes switch map.
   * @param transformRequest Transformation request used to add/delete/modify code
   *
   * @return true if it is created successfully, otherwise false
   */
  private boolean addSyntheticInitializerIfNotExists(@Nonnull TransformationRequest
      transformRequest) {
    // search for the correct name of initializer
    String methodName = OptimizationUtil.getSyntheticSwitchMapInitializerName(enumType);
    try {
      switchMapClass.getMethod(methodName, supportUtil.getPrimitiveIntType().getArray());
    } catch (JMethodLookupException ex) {
      // insert the switch map field into enum
      syntheticSwitchMapField = addSyntheticField(transformRequest);
      // if this method cannot be identified, create the method first
      syntheticSwitchMapInitializer = createSyntheticSwitchMapInitializer(transformRequest);
      // create ordinal mapping if this method is not defined yet. This information
      // is attached to the synthetic initializer and supposed to be immutable through
      // this compilation. Never use a packed switch statement when the enum switch map
      // is stored inside synthetic map class
      boolean isPackedSwitchStmt = !SyntheticClassManager.isSyntheticSwitchMapClass(switchMapClass);
      createOrdinalMapping(isPackedSwitchStmt);
      // add statements into this method
      if (!fillSyntheticSwitchMapInitializer(transformRequest)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Create the mapping relationship between static enum field to a random integer called
   * compile-time ordinal.
   * @param packedSwitch Specify if packed switch statement is needed
   */
  private void createOrdinalMapping(boolean packedSwitch) {
    if (syntheticSwitchMapInitializer.containsMarker(EnumMappingMarker.class)) {
      // the ordinal mapping should not be defined before
      throw new AssertionError(
          "EnumMappingMarker has already been attached to synthetic switch map initializer: "
              + syntheticSwitchMapInitializer);
    }
    int packedCompileTimeOrdinal = 1;
    int unpackedCompileTimeOrdinal = 1;
    Set<JFieldId> usedEnumFields = null;
    if (packedSwitch) {
      UsedEnumField usedFieldMarker = switchMapClass.getMarker(UsedEnumField.class);
      if (usedFieldMarker != null) {
        usedEnumFields = usedFieldMarker.getEnumFields();
        unpackedCompileTimeOrdinal = usedEnumFields.size() + 1;
      }
    }
    // calculate which enum member (fields) the class is using
    EnumMappingMarker mappingMarker = new EnumMappingMarker();
    for (JField enumField : enumType.getFields()) {
      if (!(enumField instanceof JEnumField)) {
        // if it is not final enum field, ignore it
        continue;
      }
      // compile time ordinal starts from 1
      JFieldId enumFieldId = enumField.getId();
      if (usedEnumFields == null) {
        // if usedEnumFields is unknown, we cannot use packed switch
        mappingMarker.addMapping(enumFieldId, unpackedCompileTimeOrdinal++);
      } else if (usedEnumFields.contains(enumField.getId())) {
        // at this point, usedEnumFields is known, assign continuous ordinal
        mappingMarker.addMapping(enumFieldId, packedCompileTimeOrdinal++);
      } else {
        // at this point, usedEnumFields is known, but current field is not used
        // thus, we unpacked ordinal instead
        mappingMarker.addMapping(enumFieldId, unpackedCompileTimeOrdinal++);
      }
    }
    syntheticSwitchMapInitializer.addMarker(mappingMarker);
  }

  /**
   * Create the synthetic method corresponding with the type of enum. At this point,
   * the method has no statement.
   * @param transformRequest Transformation request used to add/delete/modify code
   *
   * @return The created synthetic switch map method
   */
  private JMethod createSyntheticSwitchMapInitializer(@Nonnull TransformationRequest
      transformRequest) {
    // search for the correct name of initializer
    String syntheticMethodName = OptimizationUtil.getSyntheticSwitchMapInitializerName(enumType);
    JMethod syntheticMapInitializer = null;
    try {
      syntheticMapInitializer = switchMapClass.getMethod(syntheticMethodName,
          supportUtil.getPrimitiveIntType().getArray());

    } catch (JMethodLookupException e) {
    }
    if (syntheticMapInitializer != null) {
      throw new AssertionError("synthetic switch map method: " + syntheticMethodName
          + " already exists");
    }

    // create synthetic switch map initializer
    syntheticMapInitializer = new JMethod(SourceInfo.UNKNOWN, new JMethodId(
        syntheticMethodName, MethodKind.STATIC), switchMapClass,
        supportUtil.getPrimitiveIntType().getArray(),
        JModifier.PUBLIC | JModifier.STATIC | JModifier.SYNTHETIC);
    // add this new method into AST
    transformRequest.append(new AppendMethod(switchMapClass, syntheticMapInitializer));
    return syntheticMapInitializer;
  }

  /**
   * Fill statements into the newly created synthetic switch map initializer.
   * @param transformRequest Transformation request used to add/delete/modify code
   *
   * @return true if this process is successful, otherwise false
   */
  private boolean fillSyntheticSwitchMapInitializer(@Nonnull TransformationRequest
      transformRequest) {
    if (syntheticSwitchMapInitializer.getBody() != null) {
      throw new AssertionError("Body already be added into synthetic switch map initializer: "
         + syntheticSwitchMapInitializer.getBody());
    }
    // create body block
    JBlock bodyBlock = new JBlock(SourceInfo.UNKNOWN);
    JMethodBody methodBody = new JMethodBody(SourceInfo.UNKNOWN, bodyBlock);
    OptimizationUtil.setMethodBody(syntheticSwitchMapInitializer, methodBody);

    // create transformation request with this method. They will be used to create locals
    LocalVarCreator localVarCreator = new LocalVarCreator(syntheticSwitchMapInitializer, "tac");

    /**create the outer most if-else statement
     * {@code
     *  int[] switchmap = field reference to switch map
     *  if (switchmap != null) {
     *    return switchmap;
     *  }
     *  else {
     *    switchmap = new int[enum.values().length];
     *    switchmap[Enum1.ordinal()] = compileTimeOrdinal1;
     *    switchmap[Enum2.ordinal()] = compileTimeOrdinal2;
     *    ...
     *    return switchmap;
     *  }
     * }
     */
    // load the static field into local variable
    JExpression checkNullExpr = JBinaryOperation.create(SourceInfo.UNKNOWN, JBinaryOperator.NEQ,
        new JFieldRef(SourceInfo.UNKNOWN, null /*static field*/, syntheticSwitchMapField.getId(),
            switchMapClass), new JNullLiteral(SourceInfo.UNKNOWN));

    // if field already was created return it
    JBlock thenBlock = new JBlock(SourceInfo.UNKNOWN);
    transformRequest.append(new AppendStatement(thenBlock, new JReturnStatement(SourceInfo.UNKNOWN,
        new JFieldRef(SourceInfo.UNKNOWN, null /*static field*/, syntheticSwitchMapField.getId(),
            switchMapClass))));

    // add statement and update AST
    JStatement ifStmt = new JIfStatement(SourceInfo.UNKNOWN, checkNullExpr, thenBlock, null
        /*no else block*/);
    transformRequest.append(new AppendStatement(bodyBlock, ifStmt));

    // otherwise, create switch map initializing function to initialize it
    createSwitchMapInitializerBlock(transformRequest, localVarCreator);
    return true;
  }

  /**
   * Emit code for the block in synthetic method, which will initialize the switch map.
   * @param transformRequest Transformation request used to add/delete/modify code
   * @param localVarCreator JLocal creator
   *
   * @return The block initialize synthetic switch map int[] field
   */
  private JBlock createSwitchMapInitializerBlock(
      @Nonnull TransformationRequest transformRequest,
      @Nonnull LocalVarCreator localVarCreator) {
    // create else block
    JAbstractMethodBody body = syntheticSwitchMapInitializer.getBody();
    if (!(body instanceof JMethodBody)) {
      // synthetic method does not have method body
      throw new AssertionError("Method body is not initialized inside of method: "
          + syntheticSwitchMapInitializer);
    }
    JBlock block = ((JMethodBody) body).getBlock();
    // initialize switch map int array
    EnumMappingMarker enumMappingMarker = syntheticSwitchMapInitializer.getMarker(
        EnumMappingMarker.class);
    if (enumMappingMarker == null) {
      // enum mapping marker is not set yet
      throw new AssertionError("EnumMappingMarker is not attached to switch map initializer: "
          + syntheticSwitchMapInitializer);
    }
    Map<JFieldId, Integer> enumFieldsMap = enumMappingMarker.getMapping();
    JStatement newSwitchmapArrayStmt = createSwitchmapArrayStatement(localVarCreator,
        transformRequest, block, new JIntLiteral(SourceInfo.UNKNOWN, enumFieldsMap.size()));
    JLocal switchmapLocal = OptimizationUtil.getLhs(newSwitchmapArrayStmt);

    createPutStaticFieldStatement(transformRequest, block, switchmapLocal);

    // fill the switch map array
    createStatementsInitializingSwitchMapArrayField(transformRequest, localVarCreator, block,
        switchmapLocal);

    // add the return statement in the end
    transformRequest.append(new AppendStatement(block, new JReturnStatement(SourceInfo.UNKNOWN,
        new JLocalRef(SourceInfo.UNKNOWN, switchmapLocal))));

    return block;
  }

  /**
   * Initialize the synthetic switch map int[] field.
   * @param transformRequest Transformation utility
   * @param localVarCreator JLocal creator
   * @param block Current block to emit code to
   * @param switchmapLocal Switch map int[] local
   *
   * @return The block of instructions initialize switch map int[] field
   */
  private JBlock createStatementsInitializingSwitchMapArrayField(
      @Nonnull TransformationRequest transformRequest,
      @Nonnull LocalVarCreator localVarCreator,
      @Nonnull JBlock block,
      @Nonnull JLocal switchmapLocal) {
    // Use for loop to find the field name and map corresponding relationship between enum
    // name to compile-time ordinal
    EnumMappingMarker enumMappingMarker = syntheticSwitchMapInitializer.getMarker(
        EnumMappingMarker.class);
    if (enumMappingMarker == null) {
      // enum mapping marker is not set yet
      throw new AssertionError(
          "EnumMappingMarker is not attached to synthetic switch map initializer: "
              + syntheticSwitchMapInitializer);
    }
    Map<JFieldId, Integer> enumFieldsMap = enumMappingMarker.getMapping();
    // for each field inside of map, emit code to explicitly map it to compile time ordinal
    for (Map.Entry<JFieldId, Integer> enumFieldEntry : enumFieldsMap.entrySet()) {
      JBlock tryBlock = new JBlock(SourceInfo.UNKNOWN);
      JCatchBlock noSuchFieldExCatchBlock = createCatchBlock(supportUtil.getNoSuchFieldErrorType());
      // get field and compile time ordinal which has been calculated already
      JFieldId enumFieldId = enumFieldEntry.getKey();
      Integer compileTimeOrdinal = enumFieldEntry.getValue();

      // switchmap[staticOrdinal] = compileTimeOrdinal
      JExpression enumFieldExpr = new JFieldRef(SourceInfo.UNKNOWN, null /*static field*/,
          enumFieldId, enumType);
      JMethod ordinalMethod = supportUtil.getEnumType().getMethod(OptimizationUtil.Ordinal,
          supportUtil.getPrimitiveIntType());
      JExpression invocOrdinalExpr = new JMethodCall(SourceInfo.UNKNOWN, enumFieldExpr, enumType,
          ordinalMethod.getMethodId(), JPrimitiveTypeEnum.INT.getType(),
          ordinalMethod.canBePolymorphic());

      transformRequest.append(new AppendStatement(tryBlock, JBinaryOperation.create(
          SourceInfo.UNKNOWN, JBinaryOperator.ASG,
          new JArrayRef(SourceInfo.UNKNOWN, new JLocalRef(SourceInfo.UNKNOWN, switchmapLocal),
              invocOrdinalExpr),
          new JIntLiteral(SourceInfo.UNKNOWN, compileTimeOrdinal.intValue())).makeStatement()));

      transformRequest.append(new AppendStatement(block, new JTryStatement(SourceInfo.UNKNOWN,
          Collections.<JStatement>emptyList(), tryBlock, Lists.newArrayList(
              noSuchFieldExCatchBlock), null)));
    }
    return block;
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
   * @return The statement initializing the array of certain length
   */
  private JStatement createSwitchmapArrayStatement(
      @Nonnull LocalVarCreator localVarCreator, @Nonnull TransformationRequest transformRequest,
      @Nonnull JBlock block, @Nonnull JExpression capacityExpr) {
    // new-array v0, v0, [I
    List<JExpression> dims = Lists.newArrayList();
    dims.add(capacityExpr);
    JExpression newArrayExpr = JNewArray.createWithDims(SourceInfo.UNKNOWN, supportUtil
        .getPrimitiveIntType().getArray(), dims);

    JLocal intArrayLocal = OptimizationUtil.newLocal(localVarCreator, transformRequest, supportUtil
        .getPrimitiveIntType().getArray());

    JStatement newArrayStmt = new JExpressionStatement(SourceInfo.UNKNOWN, JBinaryOperation.create(
        SourceInfo.UNKNOWN, JBinaryOperator.ASG, new JLocalRef(SourceInfo.UNKNOWN, intArrayLocal),
        newArrayExpr));

    transformRequest.append(new AppendStatement(block, newArrayStmt));
    return newArrayStmt;
  }

  /**
   * Emit code for statement putting switch map local to synthetic field, e.g.,
   * switchmap = r0;
   *
   * @param transformRequest Transformation utility
   * @param block Current block
   * @param switchmapLocal Switch map local
   *
   * @return The statement put switch map local into synthetic field
   */
  private JStatement createPutStaticFieldStatement(@Nonnull TransformationRequest transformRequest,
      @Nonnull JBlock block, @Nonnull JLocal switchmapLocal) {
    JFieldId fieldId = syntheticSwitchMapField.getId();
    JStatement putStaticFieldStmt = new JExpressionStatement(
        SourceInfo.UNKNOWN, JBinaryOperation.create(SourceInfo.UNKNOWN, JBinaryOperator.ASG,
            new JFieldRef(SourceInfo.UNKNOWN, null /*static field*/, fieldId, switchMapClass),
            new JLocalRef(SourceInfo.UNKNOWN, switchmapLocal))
        );
    transformRequest.append(new AppendStatement(block, putStaticFieldStmt));
    return putStaticFieldStmt;
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
    // create exception local inside of catch block
    JLocal catchLocal = new JLocal(SourceInfo.UNKNOWN, "ex", exceptionType,
            JModifier.SYNTHETIC, (JMethodBody) syntheticSwitchMapInitializer.getBody());

    // create catch block
    JCatchBlock catchBlock = new JCatchBlock(SourceInfo.UNKNOWN,
        Collections.<JClass>singletonList(exceptionType), catchLocal);

    return catchBlock;
  }
}