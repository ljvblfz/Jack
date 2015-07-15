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
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JTryStatement;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.jack.shrob.shrink.KeepMarker;
import com.android.jack.transformations.LocalVarCreator;
import com.android.jack.transformations.enums.EnumMappingMarker;
import com.android.jack.transformations.enums.EnumMappingSchedulingSeparator;
import com.android.jack.transformations.enums.OptimizationUtil;
import com.android.jack.transformations.enums.SwitchEnumSupport;
import com.android.jack.transformations.exceptions.TryStatementSchedulingSeparator;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.IntegerPropertyId;

import java.util.Collections;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Add support to optimize enum switch statements. Current {@link SwitchEnumSupport} generates a
 * synthetic method for each class using enum in the switch statement, and this optimization aims
 * at improving {@link SwitchEnumSupport}. Instead of creating synthetic initializer for each user
 * class, this class only generates a single synthetic class declaring synthetic switch map
 * initializers for different enums.
 */
@Description("Add support to optimize enum switch statements.")
@Name("OptimizedSwitchEnumSupport")
@Synchronized
@Constraint(need = {JSwitchStatement.class, JEnumField.class, JEnumLiteral.class,
    OriginalNames.class})

// The meaning of these tags are not very clear, but missing some of them fails assertion, e.g.,
// TryStatementSchedulingSeparator.SeparatorTag.class. The most strange thing is some of these
// class are never used inside Jack except annotation
@Transform(modify = JSwitchStatement.class,
    add = {EnumMappingMarker.class, JNewArray.class, JAsgOperation.NonReusedAsg.class,
      JMethodCall.class, JArrayRef.class, JArrayLength.class, JLocalRef.class, JField.class,
      JMethod.class, JMethodBody.class, JFieldRef.class, JNullLiteral.class, JLocal.class,
      JIfStatement.class, JReturnStatement.class, JBlock.class, JTryStatement.class,
      JIntLiteral.class, JExpressionStatement.class, JNeqOperation.class,
      TryStatementSchedulingSeparator.SeparatorTag.class,
      EnumMappingSchedulingSeparator.SeparatorTag.class},
    remove = {JSwitchStatement.SwitchWithEnum.class, ThreeAddressCodeForm.class})
@Use(value = {LocalVarCreator.class})
@HasKeyId
public class OptimizedSwitchEnumSupport implements RunnableSchedulable<JMethod> {
  /**
   * Threshold specifying the minimal number of classes using enum in switch statements. If this
   * value is more than 0, simple user classes counting strategy will be used. Otherwise
   * observation-based strategy is applied {@link SyntheticClassManager#optimize(JDefinedEnum)}.
   */
  @Nonnull
  public static final IntegerPropertyId OPTIMIZED_ENUM_SWITCH_THRESHOLD = IntegerPropertyId
  .create("jack.optimization.enum.switch.threshold", "Threshold trigger enum optimization")
        .addDefaultValue(0);

  // read specified threshold enabling enum switch optimization defined above
  private final int threshold = ThreadConfig.get(OPTIMIZED_ENUM_SWITCH_THRESHOLD).intValue();

  // switch map filler which will fills synthetic switch map field and initializer
  private final SwitchMapClassFiller classFiller = new SwitchMapClassFiller();

  // support utility provides fundamental APIs to use
  private final OptimizationUtil supportUtil;

  // synthetic class manager
  private final SyntheticClassManager manager;

  /**
   * Constructor. It declares the support utility to use.
   */
  public OptimizedSwitchEnumSupport() {
    supportUtil = new OptimizationUtil(Jack.getSession().getLookup());
    manager = new SyntheticClassManager(supportUtil, threshold);
  }

  @Override
  public synchronized void run(@Nonnull JMethod method) {
    if (Jack.getSession().containsMarker(ShrinkMarker.class)
        && !method.containsMarker(KeepMarker.class)) {
      // if shrinking is enabled and current method is not reachable, don't optimize it since
      // Jack thinks it is dead code will drop it while dumping it to dex file
      return;
    }

    JDefinedClassOrInterface definedClass = method.getEnclosingType();
    // check if the method and enclosing class are both concrete
    if (!(definedClass instanceof JDefinedClass) || definedClass.isExternal() || method.isNative()
        || method.isAbstract()) {
      return;
    }

    // create transformation to instrument the code. Please be noted that at this point, method
    // is reachable
    TransformationRequest transformRequest = new TransformationRequest(definedClass);
    Visitor visitor = new Visitor(transformRequest, (JDefinedClass) definedClass);
    visitor.accept(method);
    // emit the code at this point
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

    /**
     * Transform the traversed switch statement. Create synthetic switch map class if necessary.
     */
    @Override
    public boolean visit(@Nonnull JSwitchStatement switchStmt) {
      // check the type of the switch expression, and only optimize the switch statement if
      // the enum is used as switch expression, e.g.,
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

        // get the synthetic class, create one if it doesn't exist. Note that the class
        // {@link OptimizedSwitchEnumSupport} is executed sequentially, e.g., process one
        // method after another. We don't have to worry too much about concurrency issue
        // in creating synthetic class. The synthetic class related to enum may be null
        // because it may be worthless to optimize it using synthetic class
        JDefinedClass switchMapClass = manager.getOrCreateRelatedSyntheticClass(enumType,
            true /*create class if not found*/);
        if (switchMapClass == null) {
          // at this point, synthetic switch map class is null meaning it is worthless
          // to do optimization. Thus, insert switch map initializer method inside of
          // enclosing class
          switchMapClass = enclosingClass;
        }

        // after the synthetic class is created, fill it with corresponding synthetic
        // fields and methods
        classFiller.fillSwitchMapClass(supportUtil, enumType, switchMapClass);

        // call the utility function to get the method name initializing synthetic switch
        // map. At this point, the synthetic switch map initializer has already be created
        String syntheticInitializerName = OptimizationUtil.getSyntheticSwitchMapInitializerName(
            enumType);

        JMethod syntheticSwitchmapInitializer = switchMapClass.getMethod(
            syntheticInitializerName, supportUtil.getPrimitiveIntType().getArray());

        // create expression representing call to synthetic switch map initializer
        JExpression getSwitchMapInvocExpr = new JMethodCall(switchStmt.getSourceInfo(),
            null /*static method*/, switchMapClass, syntheticSwitchmapInitializer.getMethodId(),
            syntheticSwitchmapInitializer.getType(),
            syntheticSwitchmapInitializer.canBePolymorphic());

        // get the method id of Enum.oridinal()
        JMethod ordinalMethod = supportUtil.getEnumType().getMethod(OptimizationUtil.Ordinal,
            supportUtil.getPrimitiveIntType(), Collections.<JType>emptyList());

        // replace switch statement with switch map
        transformRequest.append(new Replace(switchExpr,
                new JArrayRef(switchStmt.getSourceInfo(), getSwitchMapInvocExpr,
                    new JMethodCall(switchStmt.getSourceInfo(), switchExpr, enumType,
                        ordinalMethod.getMethodId(), ordinalMethod.getType(),
                        ordinalMethod.canBePolymorphic())
                    )
                )
            );
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
        // proceed only when the expression is enum constant
        JEnumLiteral enumLiteral = (JEnumLiteral) caseExpr;
        JDefinedEnum enumType = (JDefinedEnum) enumLiteral.getType();

        // at this point, synthetic method should already be created, get it
        JDefinedClass switchmapClass = manager.getOrCreateRelatedSyntheticClass(enumType,
            false /*not create class if not found*/);
        if (switchmapClass == null) {
          switchmapClass = enclosingClass;
        }

        String syntheticInitializerName = OptimizationUtil.getSyntheticSwitchMapInitializerName(
            enumType);
        JMethod syntheticInitializer = switchmapClass.getMethod(syntheticInitializerName,
            supportUtil.getPrimitiveIntType().getArray());

        // since EnumMappingMarker is attached to each synthetic switch map initializer, retrieve
        // it then map the enum literal to the compile-time ordinal
        EnumMappingMarker ordinalMapping = syntheticInitializer.getMarker(EnumMappingMarker.class);

        // this must hold true otherwise synthetic switch map class creation fails
        if (ordinalMapping == null) {
          throw new AssertionError(
              "No EnumMappingMarker is attached to synthetic switch map initializer:"
                  + syntheticInitializer);
        }

        JFieldId enumFieldId = enumLiteral.getFieldId();

        Map<JFieldId, Integer> ordinalMap = ordinalMapping.getMapping();

        Integer compileTimeOrdinal = ordinalMap.get(enumFieldId);
        if (compileTimeOrdinal == null) {
          // throw exception if the ordinal mapping cannot be found
          throw new AssertionError(
              "CompileTime ordinal is null for the enum field: " + enumLiteral);
        }
        // replace the case statement with
        // case T1 ===> getSwitchMap(Enum.O1.ordinal()) == T1
        Replace replace = new Replace(caseExpr, new JIntLiteral(caseStmt.getSourceInfo(),
            compileTimeOrdinal.intValue()));
        transformRequest.append(replace);
      }
      // keep traversing the rest of statements
      return super.visit(caseStmt);
    }
  }
}