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
import com.android.jack.ir.ast.JDefinedAnnotationType;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedEnum;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JNullType;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JPackageLookupException;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Percent;
import com.android.sched.util.log.stats.PercentImpl;
import com.android.sched.util.log.stats.StatisticId;

import java.util.HashMap;
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

  @Nonnull
  private final Adapter<JType> adapter =
    new Adapter<JType>() {
    @Nonnull
    @Override
    public Map<String, JType> getCache() {
      return types;
    }

    @Nonnull
    @Override
    public JType getType(@Nonnull JPackage pack, @Nonnull String simpleName)
        throws JTypeLookupException {
      return pack.getType(simpleName);
    }

    @Override
    @Nonnull
    public JPackage getPackage(@Nonnull JPackage pack, @Nonnull String simpleName)
        throws JPackageLookupException {
      return pack.getSubPackage(simpleName);
    }
  };

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
      return getPackage(packageName, adapter).isOnPath();
    } catch (JPackageLookupException e) {
      return false;
    }
  }



  @Override
  @Nonnull
  public JType getType(@Nonnull String typeName) throws JTypeLookupException {

    Percent statistic = tracer.getStatistic(SUCCESS_LOOKUP);
    statistic.addFalse();
    JType result = getType(typeName, adapter);
    statistic.removeFalse();
    statistic.addTrue();
    return result;
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
  public JDefinedAnnotationType getAnnotationType(@Nonnull String typeName)
      throws JTypeLookupException {
    JType type = getType(typeName);
    if (type instanceof JDefinedAnnotationType) {
      return (JDefinedAnnotationType) type;
    } else {
      throw new IncompatibleJTypeLookupException(type, JDefinedAnnotationType.class);
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

  private void clear() {
    synchronized (types) {
      types.clear();
      init();
    }
  }

  @Override
  public void removeType(@Nonnull JType type) {
    clear();
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
}
