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

package com.android.jack.lookup;

import com.android.jack.Jack;
import com.android.jack.ir.ast.IncompatibleJTypeLookupException;
import com.android.jack.ir.ast.JArrayType;
import com.android.jack.ir.ast.JDefinedAnnotation;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedEnum;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JNullType;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JPackageLookupException;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.ast.MissingJTypeLookupException;
import com.android.jack.util.NamingTools;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Percent;
import com.android.sched.util.log.stats.PercentImpl;
import com.android.sched.util.log.stats.StatisticId;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Jack lookup.
 */
public class JNodeLookup extends JLookup {
  @Nonnull
  public static final StatisticId<Percent> SUCCESS_LOOKUP = new StatisticId<Percent>(
      "jack.lookup.success", "Lookup requests returning a JDefinedClassOrInterface",
      PercentImpl.class, Percent.class);

  @Nonnull
  private final Map<String, JType> types = new HashMap<String, JType>();

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  /**
   * Initialize lookup.
   */
  public JNodeLookup(@Nonnull JPackage topLevelPackage) {
    super(topLevelPackage);
    init();
  }

  @Nonnull
  public JPackage getTopLevelPackage() {
    return topLevelPackage;
  }

  /**
   * Return true if the given name correspond to a package defined in compilations paths.
   */
  public boolean isPackageOnPath(@Nonnull String packageName) {
    try {
      return getPackage(packageName).isOnPath();
    } catch (JPackageLookupException e) {
      return false;
    }
  }



  @Override
  @Nonnull
  public JType getType(@Nonnull String typeName) throws JTypeLookupException {
    Percent statistic = tracer.getStatistic(SUCCESS_LOOKUP);
    statistic.addFalse();
    synchronized (types) {
      JType result = types.get(typeName);

      if (result == null) {
        int typeNameLength = typeName.length();
        assert typeNameLength > 1 : "Invalid signature or missing primitive type '" + typeName
        + "'";
        if (typeName.charAt(0) == '[') {
          JArrayType arrayType = getArrayType(typeName);
          types.put(typeName, arrayType);
          return arrayType;
        }

        assert NamingTools.isClassDescriptor(typeName) : "Invalid signature '" + typeName + "'";

        int separatorIndex = typeName.lastIndexOf(JLookup.PACKAGE_SEPARATOR);
        JPackage currentPackage;
        String simpleName;
        if (separatorIndex == -1) {
          currentPackage = topLevelPackage;
          simpleName = typeName.substring(1, typeNameLength - 1);
        } else {
          try {
            currentPackage = getPackage(typeName.substring(1, separatorIndex));
            simpleName = typeName.substring(separatorIndex + 1, typeNameLength - 1);
          } catch (JPackageLookupException e) {
            throw new MissingJTypeLookupException(typeName);
          }
        }
        result = currentPackage.getType(simpleName);
        types.put(typeName, result);
      }
      statistic.removeFalse();
      statistic.addTrue();
      return result;
    }
  }

  @Override
  @Nonnull
  public JDefinedClass getClass(@Nonnull String typeName) throws JTypeLookupException {
    JType type = getType(typeName);
    if (type instanceof JDefinedClass) {
      return (JDefinedClass) type;
    } else {
      throw new IncompatibleJTypeLookupException(type, JDefinedEnum.class);
    }
  }

  @Override
  @Nonnull
  public JDefinedInterface getInterface(@Nonnull String typeName) throws JTypeLookupException {
    JType type = getType(typeName);
    if (type instanceof JDefinedInterface) {
      return (JDefinedInterface) type;
    } else {
      throw new IncompatibleJTypeLookupException(type, JDefinedInterface.class);
    }
  }

  private void addType(@Nonnull JType type) {
    types.put(Jack.getLookupFormatter().getName(type), type);
  }

  @Override
  @Nonnull
  public JDefinedAnnotation getAnnotation(@Nonnull String typeName) throws JTypeLookupException {
    JType type = getType(typeName);
    if (type instanceof JDefinedAnnotation) {
      return (JDefinedAnnotation) type;
    } else {
      throw new IncompatibleJTypeLookupException(type, JDefinedAnnotation.class);
    }
  }

  @Override
  @Nonnull
  public JDefinedEnum getEnum(@Nonnull String typeName) throws JTypeLookupException {
    JType type = getType(typeName);
    if (type instanceof JDefinedEnum) {
      return (JDefinedEnum) type;
    } else {
      throw new IncompatibleJTypeLookupException(type, JDefinedEnum.class);
    }
  }

  @Override
  public void clear() {
    synchronized (types) {
      types.clear();
      init();
    }
  }

  private void init() {
    // By default, add primitive types in order to be able to lookup them.
    addType(JPrimitiveTypeEnum.VOID.getType());
    addType(JPrimitiveTypeEnum.BOOLEAN.getType());
    addType(JPrimitiveTypeEnum.BYTE.getType());
    addType(JPrimitiveTypeEnum.CHAR.getType());
    addType(JPrimitiveTypeEnum.SHORT.getType());
    addType(JPrimitiveTypeEnum.INT.getType());
    addType(JPrimitiveTypeEnum.FLOAT.getType());
    addType(JPrimitiveTypeEnum.DOUBLE.getType());
    addType(JPrimitiveTypeEnum.LONG.getType());
    addType(JNullType.INSTANCE);
  }

  @Nonnull
  private JPackage getPackage(@Nonnull String packageName)
      throws JPackageLookupException {
    assert !packageName.contains(".");
    JPackage currentPackage = topLevelPackage;
    Iterator<String> iterator = packageBinaryNameSplitter.split(packageName).iterator();
    while (iterator.hasNext()) {
      String name = iterator.next();
      currentPackage = currentPackage.getSubPackage(name);
    }
    assert Jack.getLookupFormatter().getName(currentPackage).equals(packageName);
    return currentPackage;
  }
}
