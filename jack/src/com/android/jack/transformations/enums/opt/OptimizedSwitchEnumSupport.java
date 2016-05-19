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

import com.android.jack.Jack;
import com.android.jack.ir.ast.JArrayLength;
import com.android.jack.ir.ast.JArrayRef;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JCaseStatement;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedEnum;
import com.android.jack.ir.ast.JEnumField;
import com.android.jack.ir.ast.JEnumLiteral;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldId;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JIntLiteral;
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JNeqOperation;
import com.android.jack.ir.ast.JNewArray;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JTryStatement;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.scheduling.filter.SourceTypeFilter;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.jack.transformations.LocalVarCreator;
import com.android.jack.transformations.ast.inner.InnerAccessorGeneratorSchedulingSeparator;
import com.android.jack.transformations.enums.EnumMappingMarker;
import com.android.jack.transformations.enums.EnumMappingSchedulingSeparator;
import com.android.jack.transformations.enums.SwitchEnumSupport;
import com.android.jack.transformations.exceptions.TryStatementSchedulingSeparator;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.ExclusiveAccess;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;

import java.util.Collections;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Add support to optimize enum switch statements. Current {@link SwitchEnumSupport} generates a
 * synthetic method for each class using enum in the switch statement, and this optimization aims
 * at improving it. Instead of creating synthetic initializer for each user class, this class only
 * generates a single synthetic class which only declares one synthetic switch map initializers
 * for each enum.
 */
@Description("Add support to optimize enum switch statements.")
@Name("OptimizedSwitchEnumSupport")
@Synchronized
@Constraint(
 need = {JSwitchStatement.class, JEnumField.class, JEnumLiteral.class, OriginalNames.class})

@Transform(
 modify = {JSwitchStatement.class},
 add = {EnumMappingMarker.class, JNewArray.class, JAsgOperation.NonReusedAsg.class,
     JMethodCall.class, JArrayRef.class, JArrayLength.class, JLocalRef.class, JField.class,
     JMethod.class, JMethodBody.class, JFieldRef.class, JNullLiteral.class, JLocal.class,
     JIfStatement.class, JReturnStatement.class, JBlock.class, JTryStatement.class,
     JIntLiteral.class, JExpressionStatement.class, JNeqOperation.class, JDefinedClass.class,
     TryStatementSchedulingSeparator.SeparatorTag.class,
     EnumMappingSchedulingSeparator.SeparatorTag.class,
     InnerAccessorGeneratorSchedulingSeparator.SeparatorSwitchEnumSupportTag.class},
 remove = {JSwitchStatement.SwitchWithEnum.class, ThreeAddressCodeForm.class})
@Use(value = {LocalVarCreator.class})
@Filter(SourceTypeFilter.class)
// This schedulable modifies a class (that it added in a previous run) that it is not visiting.
@ExclusiveAccess(JSession.class)
public class OptimizedSwitchEnumSupport implements RunnableSchedulable<JMethod> {
  // switch map filler which will fills synthetic switch map field and initializer
  @Nonnull
  private final SwitchMapClassFiller classFiller = new SwitchMapClassFiller();

  @Nonnull
  private final OptimizationUtil supportUtil;

  // synthetic class manager which is responsible for creating synthetic class
  @Nonnull
  private final SyntheticClassManager manager;

  public OptimizedSwitchEnumSupport() {
    supportUtil = new OptimizationUtil();
    manager = new SyntheticClassManager(supportUtil);
  }

  @Override
  public synchronized void run(@Nonnull JMethod method) {
    JDefinedClassOrInterface definedClass = method.getEnclosingType();
    if (!(definedClass instanceof JDefinedClass) || method.isNative() || method.isAbstract()) {
      return;
    }

    TransformationRequest transformRequest = new TransformationRequest(definedClass);
    Visitor visitor = new Visitor(transformRequest, (JDefinedClass) definedClass);
    visitor.accept(method);
    transformRequest.commit();
  }

  private class Visitor extends JVisitor {
    @Nonnull
    private final TransformationRequest transformRequest;

    // enclosing class in which the traversal starts from
    @Nonnull
    private final JDefinedClass enclosingClass;

    /**
     * Constructor.
     * @param transformRequest The transformation request used to add/delete/modify code
     * @param definedClass The enclosing class from where traversal starts
     */
    public Visitor(
        @Nonnull TransformationRequest transformRequest, @Nonnull JDefinedClass definedClass) {
      this.transformRequest = transformRequest;
      this.enclosingClass = definedClass;
    }

    @Override
    public boolean visit(@Nonnull JSwitchStatement switchStmt) {
      // check the type of the switch expression, and optimize the switch statement e.g.,
      // Enum e;
      // switch(e) {
      //   case Enum.O2: {...}
      //   case Enum.o2: {...}
      //   ...
      // }
      JExpression switchExpr = switchStmt.getExpr();
      JType switchExprType = switchExpr.getType();

      if (switchExprType instanceof JDefinedEnum) {
        JDefinedEnum enumType = (JDefinedEnum) switchExprType;

        // get the synthetic class, create one if it doesn't exist and it is worth to do that
        JDefinedClass switchMapClass =
            getSwitchMapClass(enumType, true /*create class if not found*/);
        if (switchMapClass == null) {
          // at this point, synthetic switch map class is null meaning it is worthless
          // to do optimization. Thus, insert switch map initializer method inside of
          // enclosing class
          switchMapClass = enclosingClass;
        }

        // after the synthetic class is created, fill it with corresponding synthetic
        // fields and methods
        classFiller.fillSwitchMapClass(enumType, switchMapClass);

        // call the utility function to get the method name initializing synthetic switch map
        String syntheticInitializerName =
            OptimizationUtil.getSyntheticSwitchMapInitializerName(enumType);

        JMethod syntheticInitializer = switchMapClass.getMethod(
            syntheticInitializerName, JPrimitiveTypeEnum.INT.getType().getArray());

        // create expression representing call to synthetic switch map initializer
        JExpression getSwitchMapInvocExpr =
            new JMethodCall(switchStmt.getSourceInfo(), null /*static method*/, switchMapClass,
                syntheticInitializer.getMethodIdWide(), syntheticInitializer.getType(),
                syntheticInitializer.canBePolymorphic());

        JMethod ordinalMethod =
            Jack.getSession().getLookup().getClass("Ljava/lang/Enum;").
            getMethod("ordinal", JPrimitiveTypeEnum.INT.getType(), Collections.<JType>emptyList());

        transformRequest.append(
            new Replace(switchExpr, new JArrayRef(switchStmt.getSourceInfo(), getSwitchMapInvocExpr,
                    new JMethodCall(switchStmt.getSourceInfo(), switchExpr, enumType,
                        ordinalMethod.getMethodIdWide(), ordinalMethod.getType(),
                        ordinalMethod.canBePolymorphic()))));
      }
      // keep traversing because case statement needs transformation as well
      return super.visit(switchStmt);
    }

    /**
     * Transform the traversed case statement. Replace it with compile time ordinal.
     */
    @Override
    public boolean visit(@Nonnull JCaseStatement caseStmt) {
      JLiteral caseExpr = caseStmt.getExpr();

      if (caseExpr instanceof JEnumLiteral) {
        // proceed only when the expression is enum literals
        JEnumLiteral enumLiteral = (JEnumLiteral) caseExpr;
        JDefinedEnum enumType = (JDefinedEnum) enumLiteral.getType();

        JDefinedClass switchmapClass =
            getSwitchMapClass(enumType, false /*not create class if not found*/);

        if (switchmapClass == null) {
          switchmapClass = enclosingClass;
        }

        String syntheticInitializerName =
            OptimizationUtil.getSyntheticSwitchMapInitializerName(enumType);

        JMethod syntheticInitializer = switchmapClass.getMethod(
            syntheticInitializerName, JPrimitiveTypeEnum.INT.getType().getArray());

        // because a EnumMappingMarker is attached to each synthetic switch map initializer,
        // retrieve it then map the enum literal to the compile-time ordinal
        EnumMappingMarker ordinalMapping = syntheticInitializer.getMarker(EnumMappingMarker.class);

        // this must hold true otherwise synthetic switch map class creation fails
        assert ordinalMapping != null;

        JFieldId enumFieldId = enumLiteral.getFieldId();

        Map<JFieldId, Integer> ordinalMap = ordinalMapping.getMapping();

        Integer compileTimeOrdinal = ordinalMap.get(enumFieldId);
        assert compileTimeOrdinal != null;
        // replace the case statement with
        // case T1 ===> -getXYZSwitchesValues()[T1.ordinal()]
        Replace replace = new Replace(caseExpr,
            new JIntLiteral(caseStmt.getSourceInfo(), compileTimeOrdinal.intValue()));
        transformRequest.append(replace);
      }
      return super.visit(caseStmt);
    }

    /**
     * Get the switch map class related to given type. If the enclosing class already
     * defines switch map initializer, don't optimize it because it already exists in
     * enclosing class
     *
     * @param enumType The enum type
     * @param createIfNotExist True means synthetic class will be created if not found
     *
     * @return synthetic switch map class
     */
    @CheckForNull
    private JDefinedClass getSwitchMapClass(
        @Nonnull JDefinedEnum enumType, boolean createIfNotExist) {
      String syntheticFieldName = OptimizationUtil.getSyntheticSwitchMapFieldName(enumType);
      for (JField field : enclosingClass.getFields()) {
        // if enclosing class already contains switch map field, don't optimizing it
        if (supportUtil.isSyntheticSwitchMapField(field)
            && syntheticFieldName.equals(field.getName())) {
          return enclosingClass;
        }
      }

      return manager.getOrCreateSyntheticClass(enumType, createIfNotExist);
    }
  }
}