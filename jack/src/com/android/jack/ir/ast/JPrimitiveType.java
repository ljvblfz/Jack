/*
 * Copyright 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.android.jack.ir.ast;


import com.android.jack.Jack;
import com.android.jack.ir.StringInterner;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.ir.types.JFloatingPointType;
import com.android.jack.ir.types.JIntegralType32;
import com.android.jack.ir.types.JIntegralType64;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.lookup.CommonTypes.CommonType;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/**
 * Base class for all Java primitive types.
 */
@Description("Java primitive types")
public abstract class JPrimitiveType extends JNode implements JType {

  /**
   * This enum represents all primitive types.
   */
  public enum JPrimitiveTypeEnum {
    BOOLEAN(new JPrimitiveType.JBooleanType()),
    BYTE(new JPrimitiveType.JByteType()),
    CHAR(new JPrimitiveType.JCharType()),
    DOUBLE(new JPrimitiveType.JDoubleType()),
    FLOAT(new JPrimitiveType.JFloatType()),
    INT(new JPrimitiveType.JIntType()),
    LONG(new JPrimitiveType.JLongType()),
    SHORT(new JPrimitiveType.JShortType()),
    VOID(new JPrimitiveType.JVoidType());

    @Nonnull
    private final JPrimitiveType type;

    private JPrimitiveTypeEnum(@Nonnull JPrimitiveType type) {
      this.type = type;
    }

    @Nonnull
    public JPrimitiveType getType() {
      return type;
    }
  }

  @Nonnull
  protected final String name;

  private JPrimitiveType(@Nonnull String name) {
    super(SourceInfo.UNKNOWN);
    this.name = StringInterner.get().intern(name);
  }

  @Override
  public final boolean isSameType(@Nonnull JType type) {
    return this == type;
  }

  @Nonnull
  // Section Unary Numeric Promotion (JLS-7 5.6.1)
  public static JType getUnaryPromotion(@Nonnull JType argType) {
    JType promotedType;

    if (JPrimitiveTypeEnum.BYTE.getType().isEquivalent(argType)
        || JPrimitiveTypeEnum.CHAR.getType().isEquivalent(argType)
        || JPrimitiveTypeEnum.SHORT.getType().isEquivalent(argType)
        || JPrimitiveTypeEnum.INT.getType().isEquivalent(argType)) {
      promotedType = JPrimitiveTypeEnum.INT.getType();
    } else if (JPrimitiveTypeEnum.FLOAT.getType().isEquivalent(argType)) {
      promotedType = JPrimitiveTypeEnum.FLOAT.getType();
    } else if (JPrimitiveTypeEnum.LONG.getType().isEquivalent(argType)) {
      promotedType = JPrimitiveTypeEnum.LONG.getType();
    } else if (JPrimitiveTypeEnum.DOUBLE.getType().isEquivalent(argType)) {
      promotedType = JPrimitiveTypeEnum.DOUBLE.getType();
    } else {
      throw new AssertionError();
    }

    return promotedType;
  }

  @Nonnull
  // Section Binary Numeric Promotion (JLS-7 5.6.2)
  public static JType getBinaryPromotionType(@Nonnull JType lhsType, @Nonnull JType rhsType) {
    JType promotedType;

    assert JPrimitiveTypeEnum.BYTE.getType().isEquivalent(lhsType)
        || JPrimitiveTypeEnum.CHAR.getType().isEquivalent(lhsType)
        || JPrimitiveTypeEnum.SHORT.getType().isEquivalent(lhsType)
        || JPrimitiveTypeEnum.INT.getType().isEquivalent(lhsType)
        || JPrimitiveTypeEnum.FLOAT.getType().isEquivalent(lhsType)
        || JPrimitiveTypeEnum.LONG.getType().isEquivalent(lhsType)
        || JPrimitiveTypeEnum.DOUBLE.getType().isEquivalent(lhsType);

    assert JPrimitiveTypeEnum.BYTE.getType().isEquivalent(rhsType)
        || JPrimitiveTypeEnum.CHAR.getType().isEquivalent(rhsType)
        || JPrimitiveTypeEnum.SHORT.getType().isEquivalent(rhsType)
        || JPrimitiveTypeEnum.INT.getType().isEquivalent(rhsType)
        || JPrimitiveTypeEnum.FLOAT.getType().isEquivalent(rhsType)
        || JPrimitiveTypeEnum.LONG.getType().isEquivalent(rhsType)
        || JPrimitiveTypeEnum.DOUBLE.getType().isEquivalent(rhsType);

    if (lhsType.isSameType(JPrimitiveTypeEnum.DOUBLE.getType())
        || rhsType.isSameType(JPrimitiveTypeEnum.DOUBLE.getType())
        || CommonTypes.isCommonType(CommonTypes.JAVA_LANG_DOUBLE, lhsType)
        || CommonTypes.isCommonType(CommonTypes.JAVA_LANG_DOUBLE, rhsType)) {
      promotedType = JPrimitiveTypeEnum.DOUBLE.getType();
    } else if (lhsType.isSameType(JPrimitiveTypeEnum.FLOAT.getType())
        || rhsType.isSameType(JPrimitiveTypeEnum.FLOAT.getType())
        || CommonTypes.isCommonType(CommonTypes.JAVA_LANG_FLOAT, lhsType)
        || CommonTypes.isCommonType(CommonTypes.JAVA_LANG_FLOAT, rhsType)) {
      promotedType = JPrimitiveTypeEnum.FLOAT.getType();
    } else if (lhsType.isSameType(JPrimitiveTypeEnum.LONG.getType())
        || rhsType.isSameType(JPrimitiveTypeEnum.LONG.getType())
        || CommonTypes.isCommonType(CommonTypes.JAVA_LANG_LONG, lhsType)
        || CommonTypes.isCommonType(CommonTypes.JAVA_LANG_LONG, rhsType)) {
      promotedType = JPrimitiveTypeEnum.LONG.getType();
    } else {
      promotedType = JPrimitiveTypeEnum.INT.getType();
    }
    return promotedType;
  }

  @Override
  public boolean isExternal() {
    return false;
  }

  @Override
  public String getName() {
    return name;
  }

  @Nonnull
  public final JClass getWrapperType() {
    return Jack.getSession().getPhantomLookup().getClass(getWrapperCommonType());
  }

  public boolean isWrapperType(@Nonnull JType candidate) {
    return Jack.getSession().getPhantomLookup().getClass(getWrapperCommonType())
        .isSameType(candidate);
  }

  @Nonnull
  public abstract JPrimitiveTypeEnum getPrimitiveTypeEnum();

  public boolean isEquivalent(JType type) {
    return this == type || isWrapperType(type);
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
  }

  @Override
  @Nonnull
  public JArrayType getArray() {
    return Jack.getSession().getArrayOf(getPrimitiveTypeEnum());
  }

  @Nonnull
  abstract CommonType getWrapperCommonType();

  /**
   * Java boolean type
   */
  @Description("Java boolean type")
  public static class JBooleanType extends JPrimitiveType {
    private JBooleanType() {
      super("boolean");
    }

    @Override
    @Nonnull
    public JPrimitiveTypeEnum getPrimitiveTypeEnum() {
      return JPrimitiveTypeEnum.BOOLEAN;
    }

    @Override
    public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
        throws Exception {
      visitor.visit(this, transformRequest);
    }

    @Nonnull
    @Override
    public JExpression createDefaultValue(@Nonnull SourceInfo sourceInfo) {
      return new JBooleanLiteral(sourceInfo, false);
    }

    @Override
    @Nonnull
    CommonType getWrapperCommonType() {
      return CommonTypes.JAVA_LANG_BOOLEAN;
    }

  }

  /**
   * Java byte type
   */
  @Description("Java byte type")
  public static class JByteType extends JPrimitiveType implements JIntegralType32 {
    private JByteType() {
      super("byte");
    }

    @Override
    @Nonnull
    public JPrimitiveTypeEnum getPrimitiveTypeEnum() {
      return JPrimitiveTypeEnum.BYTE;
    }

    @Override
    public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
        throws Exception {
      visitor.visit(this, transformRequest);
    }

    @Nonnull
    @Override
    public JExpression createDefaultValue(@Nonnull SourceInfo sourceInfo) {
      return new JByteLiteral(sourceInfo, (byte) 0);
    }

    @Override
    @Nonnull
    CommonType getWrapperCommonType() {
      return CommonTypes.JAVA_LANG_BYTE;
    }

    @Override
    public boolean isValidValue(int value) {
      return (Byte.MIN_VALUE <= value) && (value <= Byte.MAX_VALUE);
    }
  }

  /**
   * Java char type
   */
  @Description("Java char type")
  public static class JCharType extends JPrimitiveType implements JIntegralType32 {
    private JCharType() {
      super("char");
    }

    @Override
    @Nonnull
    public JPrimitiveTypeEnum getPrimitiveTypeEnum() {
      return JPrimitiveTypeEnum.CHAR;
    }

    @Override
    public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
        throws Exception {
      visitor.visit(this, transformRequest);
    }

    @Nonnull
    @Override
    public JExpression createDefaultValue(@Nonnull SourceInfo sourceInfo) {
      return new JCharLiteral(sourceInfo, (char) 0);
    }

    @Override
    @Nonnull
    CommonType getWrapperCommonType() {
      return CommonTypes.JAVA_LANG_CHAR;
    }

    @Override
    public boolean isValidValue(int value) {
      return (Character.MIN_VALUE <= value) && (value <= Character.MAX_VALUE);
    }
  }

  /**
   * Java double type
   */
  @Description("Java double type")
  public static class JDoubleType extends JPrimitiveType implements JFloatingPointType {
    private JDoubleType() {
      super("double");
    }

    @Override
    @Nonnull
    public JPrimitiveTypeEnum getPrimitiveTypeEnum() {
      return JPrimitiveTypeEnum.DOUBLE;
    }

    @Override
    public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
        throws Exception {
      visitor.visit(this, transformRequest);
    }

    @Nonnull
    @Override
    public JExpression createDefaultValue(@Nonnull SourceInfo sourceInfo) {
      return new JDoubleLiteral(sourceInfo, 0.0);
    }

    @Override
    @Nonnull
    CommonType getWrapperCommonType() {
      return CommonTypes.JAVA_LANG_DOUBLE;
    }
  }

  /**
   * Java float type
   */
  @Description("Java float type")
  public static class JFloatType extends JPrimitiveType implements JFloatingPointType {
    private JFloatType() {
      super("float");
    }

    @Override
    @Nonnull
    public JPrimitiveTypeEnum getPrimitiveTypeEnum() {
      return JPrimitiveTypeEnum.FLOAT;
    }

    @Override
    public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
        throws Exception {
      visitor.visit(this, transformRequest);
    }

    @Nonnull
    @Override
    public JExpression createDefaultValue(@Nonnull SourceInfo sourceInfo) {
      return new JFloatLiteral(sourceInfo, 0.0f);
    }

    @Override
    @Nonnull
    CommonType getWrapperCommonType() {
      return CommonTypes.JAVA_LANG_FLOAT;
    }
  }

  /**
   * Java int type
   */
  @Description("Java int type")
  public static class JIntType extends JPrimitiveType implements JIntegralType32 {
    private JIntType() {
      super("int");
    }

    @Override
    @Nonnull
    public JPrimitiveTypeEnum getPrimitiveTypeEnum() {
      return JPrimitiveTypeEnum.INT;
    }

    @Override
    public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
        throws Exception {
      visitor.visit(this, transformRequest);
    }

    @Nonnull
    @Override
    public JExpression createDefaultValue(@Nonnull SourceInfo sourceInfo) {
      return new JIntLiteral(sourceInfo, 0);
    }

    @Override
    @Nonnull
    CommonType getWrapperCommonType() {
      return CommonTypes.JAVA_LANG_INTEGER;
    }

    @Override
    public boolean isValidValue(int value) {
      return true;
    }
  }

  /**
   * Java long type
   */
  @Description("Java long type")
  public static class JLongType extends JPrimitiveType implements JIntegralType64 {
    private JLongType() {
      super("long");
    }

    @Override
    @Nonnull
    public JPrimitiveTypeEnum getPrimitiveTypeEnum() {
      return JPrimitiveTypeEnum.LONG;
    }

    @Override
    public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
        throws Exception {
      visitor.visit(this, transformRequest);
    }

    @Nonnull
    @Override
    public JExpression createDefaultValue(@Nonnull SourceInfo sourceInfo) {
      return new JLongLiteral(sourceInfo, 0L);
    }

    @Override
    @Nonnull
    CommonType getWrapperCommonType() {
      return CommonTypes.JAVA_LANG_LONG;
    }
  }

  /**
   * Java short type
   */
  @Description("Java short type")
  public static class JShortType extends JPrimitiveType implements JIntegralType32 {
    private JShortType() {
      super("short");
    }

    @Override
    @Nonnull
    public JPrimitiveTypeEnum getPrimitiveTypeEnum() {
      return JPrimitiveTypeEnum.SHORT;
    }

    @Override
    public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
        throws Exception {
      visitor.visit(this, transformRequest);
    }

    @Nonnull
    @Override
    public JExpression createDefaultValue(@Nonnull SourceInfo sourceInfo) {
      return new JShortLiteral(sourceInfo, (short) 0);
    }

    @Override
    @Nonnull
    CommonType getWrapperCommonType() {
      return CommonTypes.JAVA_LANG_SHORT;
    }

    @Override
    public boolean isValidValue(int value) {
      return (Short.MIN_VALUE <= value) && (value <= Short.MAX_VALUE);
    }
  }

  /**
   * Java void type
   */
  @Description("Java void type")
  public static class JVoidType extends JPrimitiveType {
    private JVoidType() {
      super("void");
    }

    @Override
    @Nonnull
    public JPrimitiveTypeEnum getPrimitiveTypeEnum() {
      return JPrimitiveTypeEnum.VOID;
    }

    @Override
    public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
        throws Exception {
      visitor.visit(this, transformRequest);
    }

    @Nonnull
    @Override
    public JExpression createDefaultValue(@Nonnull SourceInfo sourceInfo) {
      throw new AssertionError();
    }

    @Override
    @Nonnull
    CommonType getWrapperCommonType() {
      return CommonTypes.JAVA_LANG_VOID;
    }

    @Override
    @Nonnull
    public JArrayType getArray() {
      // Array of void does not exist.
      throw new AssertionError();
    }
  }
}
