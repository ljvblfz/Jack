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

package com.android.jack.transformations.enums;

import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.config.id.Private;
import com.android.jack.ir.ast.JArrayLength;
import com.android.jack.ir.ast.JArrayRef;
import com.android.jack.ir.ast.JArrayType;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JBinaryOperator;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JCaseStatement;
import com.android.jack.ir.ast.JCatchBlock;
import com.android.jack.ir.ast.JClass;
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
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JNeqOperation;
import com.android.jack.ir.ast.JNewArray;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JTryStatement;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.formatter.BinaryQualifiedNameFormatter;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.lookup.JLookup;
import com.android.jack.lookup.JMethodLookupException;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.jack.transformations.LocalVarCreator;
import com.android.jack.transformations.exceptions.TryStatementSchedulingSeparator;
import com.android.jack.transformations.request.AppendField;
import com.android.jack.transformations.request.AppendMethod;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.NamingTools;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.Synchronized;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.BooleanPropertyId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Add support for partial recompilation for enum used into switches.
 */
@Description("Add support for partial recompilation for enum used into switches.")
@Name("SwitchEnumSupport")
@Synchronized
@Constraint(need = {JSwitchStatement.class, JEnumField.class, JEnumLiteral.class,
    SwitchEnumSupport.UsedEnumField.class, OriginalNames.class})
@Transform(modify = JSwitchStatement.class, add = {EnumMappingMarker.class,
    JNewArray.class, JAsgOperation.NonReusedAsg.class, JMethodCall.class, JArrayRef.class,
    JArrayLength.class, JLocalRef.class, JField.class,
    JMethod.class, JMethodBody.class, JFieldRef.class, JNullLiteral.class, JLocal.class,
    JIfStatement.class, JReturnStatement.class, JBlock.class, JTryStatement.class,
    JIntLiteral.class, JExpressionStatement.class,
    JNeqOperation.class, TryStatementSchedulingSeparator.SeparatorTag.class,
    EnumMappingSchedulingSeparator.SeparatorTag.class},
    remove = {JSwitchStatement.SwitchWithEnum.class, ThreeAddressCodeForm.class})
@Use(value = {LocalVarCreator.class})
@HasKeyId
public class SwitchEnumSupport implements RunnableSchedulable<JMethod> {

  @Nonnull
  public static final BooleanPropertyId SORT_ENUM_FIELD = BooleanPropertyId.create(
      "jack.internal.switch-enumfield.sort",
      "Generate determinist code to initialize constant array indexed by ordinal "
      + "value of enum field")
      .addDefaultValue(Boolean.TRUE).withCategory(Private.get());

  private final JType noSuchFieldErrorType =
      Jack.getSession().getPhantomLookup().getType("Ljava/lang/NoSuchFieldError;");

  /**
   * Enum fields used into switch.
   */
  @Description("Enum fields used into switch")
  @ValidOn(JDefinedClass.class)
  public static class UsedEnumField implements Marker {
    @Nonnull
    private final Set<JFieldId> enumFields;

    public UsedEnumField(@Nonnull Set<JFieldId> enumFields) {
      this.enumFields = enumFields;
    }

    @Nonnull
    public Set<JFieldId> getEnumFields() {
      return enumFields;
    }

    @Override
    public Marker cloneIfNeeded() {
      return this;
    }
  }

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  private class Visitor extends JVisitor {

    private final boolean sortEnumField = ThreadConfig.get(SORT_ENUM_FIELD).booleanValue();

    private static final String ORDINAL = "ordinal";

    @Nonnull
    private final TransformationRequest tr;

    @Nonnull
    private final JDefinedClassOrInterface currentClass;

    @Nonnull
    private final JLookup lookup;

    @CheckForNull
    private Set<JFieldId> usedEnumFields;

    public Visitor(@Nonnull TransformationRequest tr,
        @Nonnull JDefinedClassOrInterface currentClass) {
      this.tr = tr;
      this.currentClass = currentClass;
      this.lookup = Jack.getSession().getPhantomLookup();
    }

    @Override
    public boolean visit(@Nonnull JMethod method) {
      if (method.getEnclosingType() instanceof JDefinedClass) {
        UsedEnumField uef = method.getEnclosingType().getMarker(UsedEnumField.class);
        assert uef != null;
        usedEnumFields = uef.getEnumFields();
      }
      return super.visit(method);
    }

    @Override
    public boolean visit(@Nonnull JSwitchStatement switchStmt) {
      JExpression expr = switchStmt.getExpr();

      JType exprType = expr.getType();

      if (exprType instanceof JDefinedEnum) {
          JDefinedEnum enumType = (JDefinedEnum) exprType;

          JMethod getEnumSwitchValues = getSwitchValuesMethod(enumType);

          // Replace enum access by $SwitchesValues[x.ordinal()]
          JMethodId methodId = getEnumSwitchValues.getMethodId();
          JExpression callSwitchValues = new JMethodCall(
              switchStmt.getSourceInfo(), null, getEnumSwitchValues.getEnclosingType(),
              methodId, getEnumSwitchValues.getType(),
              methodId.canBeVirtual());

        JMethodId ordinalMethodId = enumType.getOrCreateMethodId(
            ORDINAL, Collections.<JType>emptyList(), MethodKind.INSTANCE_VIRTUAL);

          tr.append(new Replace(expr, new JArrayRef(SourceInfo.UNKNOWN, callSwitchValues,
            new JMethodCall(SourceInfo.UNKNOWN, expr, enumType, ordinalMethodId,
                JPrimitiveTypeEnum.INT.getType(), ordinalMethodId.canBeVirtual()))));
      }
      return super.visit(switchStmt);
    }


    @Override
    public boolean visit(@Nonnull JCaseStatement caseStmt) {
      JLiteral caseExpr = caseStmt.getExpr();

      if (caseExpr != null && caseExpr instanceof JEnumLiteral) {
        JEnumLiteral literal = (JEnumLiteral) caseExpr;

        JMethod getEnumSwitchValues =
            getSwitchValuesMethod((JDefinedEnum) literal.getType());

        EnumMappingMarker emm = getEnumSwitchValues.getMarker(EnumMappingMarker.class);
        assert emm != null;

        Integer enumSwitchValue = emm.getMapping().get(literal.getFieldId());
        assert enumSwitchValue != null;

        tr.append(new Replace(caseExpr,
            new JIntLiteral(caseStmt.getSourceInfo(), enumSwitchValue.intValue())));
      }

      return super.visit(caseStmt);
    }

    @Nonnull
    private JMethod getSwitchValuesMethod(@Nonnull JDefinedEnum enumType) {
      SourceInfo dbgInfo = SourceInfo.UNKNOWN;
      String enumName = BinaryQualifiedNameFormatter.getFormatter().getName(enumType);
      String methodName =
          NamingTools.getStrictNonSourceConflictingName("get" + enumName + "SwitchesValues");

      String fieldName =
          NamingTools.getStrictNonSourceConflictingName(enumName + "SwitchesValues");

      JArrayType switchValuesArrayType = JPrimitiveTypeEnum.INT.getType().getArray();
      JArrayType enumArrayType = enumType.getArray();

      JMethod getEnumSwitchValues;

      try {
        getEnumSwitchValues = currentClass.getMethod(methodName, switchValuesArrayType);
      } catch (JMethodLookupException e) {
        TransformationRequest localTr = new TransformationRequest(currentClass);

        // Create $[EnumName]switchesValues field
        JField enumSwitchValues =
            new JField(dbgInfo, fieldName, currentClass, switchValuesArrayType,
                JModifier.PRIVATE | JModifier.STATIC | JModifier.SYNTHETIC);
        localTr.append(new AppendField(currentClass, enumSwitchValues));

        // Create method $getEnumSwitchesValues
        getEnumSwitchValues =
            new JMethod(dbgInfo, new JMethodId(methodName, MethodKind.STATIC),
                currentClass, switchValuesArrayType,
                JModifier.PRIVATE | JModifier.STATIC | JModifier.SYNTHETIC);
        localTr.append(new AppendMethod(currentClass, getEnumSwitchValues));

        JBlock bodyBlock = new JBlock(dbgInfo);
        JMethodBody body = new JMethodBody(dbgInfo, bodyBlock);
        getEnumSwitchValues.setBody(body);
        LocalVarCreator lvc = new LocalVarCreator(getEnumSwitchValues, "es");

        // if ($switchesValues != null) return ($switchesValues);
        JFieldId enumSwitchValuesId = enumSwitchValues.getId();
        JExpression checkNull =
            JBinaryOperation.create(dbgInfo, JBinaryOperator.NEQ, new JFieldRef(dbgInfo,
                null /* instance */, enumSwitchValuesId, currentClass),
                new JNullLiteral(dbgInfo));
        JBlock thenBlock = new JBlock(dbgInfo);
        thenBlock.addStmt(new JReturnStatement(dbgInfo,
            new JFieldRef(dbgInfo, null /* instance */, enumSwitchValuesId, currentClass)));
        bodyBlock.addStmt(new JIfStatement(dbgInfo, checkNull, thenBlock, null /* elseStmt */));

        JLocal arrayVar = lvc.createTempLocal(switchValuesArrayType, dbgInfo, localTr);

        JMethod valuesMethod;
        try {
          valuesMethod = enumType.getMethod("values", enumArrayType);
        } catch (JMethodLookupException e1) {
          // A valid enum must have a values() method
          throw new AssertionError(e1);
        }

        // int[] array = new int[enum.values().length]
        JMethodId valuesId = valuesMethod.getMethodId();
        JExpression valuesLength = new JArrayLength(dbgInfo, new JMethodCall(dbgInfo,
            null /* instance */, enumType, valuesId, valuesMethod.getType(),
            valuesId.canBeVirtual()));
        List<JExpression> dimensions = new ArrayList<JExpression>();
        dimensions.add(valuesLength);
        bodyBlock.addStmt(new JAsgOperation(dbgInfo, new JLocalRef(dbgInfo, arrayVar),
            JNewArray.createWithDims(dbgInfo, switchValuesArrayType, dimensions)).makeStatement());


        // 0 is used to represent unknown value.
        int usedEnumFieldCstValue = 1;
        assert usedEnumFields != null;
        // +1 due to the fact that numbering of used fields does not start to 0 but 1, otherwise
        // the numbering of used fields and unused fields could be overlap.
        int unusedEnumFieldCstValue = usedEnumFields.size() + 1;
        EnumMappingMarker emm = new EnumMappingMarker();

        List<JField> enumFields = enumType.getFields();
        if (sortEnumField) {
          Collections.sort(enumFields, new Comparator<JField>() {
            @Override
            public int compare(JField o1, JField o2) {
              return o1.getName().compareTo(o2.getName());
            }
          });
        }
        for (JField enumField : enumFields) {

          if (!(enumField instanceof JEnumField)) {
            continue;
          }

          // try { array[enumField.ordinal()] = constant; } catch (NoSuchFieldError ex) {}
          JBlock tryBlock = new JBlock(dbgInfo);

          JLocal exVar = new JLocal(dbgInfo, "ex", noSuchFieldErrorType, JModifier.SYNTHETIC, body);

          List<JCatchBlock> catchBlock = new ArrayList<JCatchBlock>(1);
          catchBlock.add(new JCatchBlock(dbgInfo, Collections
              .singletonList((JClass) noSuchFieldErrorType), exVar));

          bodyBlock.addStmt(new JTryStatement(dbgInfo,
              Collections.<JStatement>emptyList(),
              tryBlock,
              catchBlock,
              null /* finallyBlock */));

          JFieldId enumFieldId = enumField.getId();
          JExpression enumFieldAccess =
              new JFieldRef(dbgInfo, null /* instance */, enumFieldId, enumType);
          JMethodId ordinalMethodId = enumType.getOrCreateMethodId(
              ORDINAL, Collections.<JType>emptyList(), MethodKind.INSTANCE_VIRTUAL);
          JExpression callOrdinal =
              new JMethodCall(dbgInfo, enumFieldAccess, enumType, ordinalMethodId,
                  JPrimitiveTypeEnum.INT.getType(),
                  ordinalMethodId.canBeVirtual());

          int constant;
          assert usedEnumFields != null;
          if (usedEnumFields.contains(enumFieldId)) {
            constant = usedEnumFieldCstValue;
            usedEnumFieldCstValue++;
          } else {
            constant = unusedEnumFieldCstValue;
            unusedEnumFieldCstValue++;
          }

          tryBlock.addStmt(new JAsgOperation(dbgInfo, new JArrayRef(dbgInfo, new JLocalRef(dbgInfo,
              arrayVar), callOrdinal), new JIntLiteral(dbgInfo, constant)).makeStatement());

          emm.addMapping(enumFieldId, constant);
        }

        getEnumSwitchValues.addMarker(emm);
        bodyBlock.addStmt(new JAsgOperation(dbgInfo, new JFieldRef(dbgInfo, null /* instance */,
            enumSwitchValuesId, currentClass), new JLocalRef(dbgInfo, arrayVar)).makeStatement());
        bodyBlock.addStmt(new JReturnStatement(dbgInfo, new JLocalRef(dbgInfo, arrayVar)));

        localTr.commit();
      }

      return (getEnumSwitchValues);
    }
  }

  @Override
  public synchronized void run(@Nonnull JMethod method) throws Exception {
    JDefinedClassOrInterface enclosingType = method.getEnclosingType();

    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    TransformationRequest tr = new TransformationRequest(enclosingType);
    Visitor visitor = new Visitor(tr, enclosingType);
    visitor.accept(method);
    tr.commit();
  }

}