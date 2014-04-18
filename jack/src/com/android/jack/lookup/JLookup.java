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


import com.google.common.base.Splitter;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JArrayType;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JEnum;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.lookup.CommonTypes.CommonType;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * {@link JLookup} allows to lookup {@link JType} from signature.
 */
public abstract class JLookup {

  @Nonnull
  protected static final Splitter packageBinaryNameSplitter =
    Splitter.on(JLookup.PACKAGE_SEPARATOR);

  @Nonnull
  private final CommonTypesCache commonTypesCache = new CommonTypesCache(this);

  @Nonnull
  protected final JPackage topLevelPackage;

  public static final char PACKAGE_SEPARATOR = '/';

  protected JLookup(@Nonnull JPackage topLevelPackage) {
    this.topLevelPackage = topLevelPackage;
  }

  @Nonnull
  public JPackage getOrCreatePackage(@Nonnull String packageName) {
    assert !packageName.contains(".");
    JPackage currentPackage = topLevelPackage;
    if (!packageName.isEmpty()) {
      for (String name : packageBinaryNameSplitter.split(packageName)) {
        currentPackage = currentPackage.getOrCreateSubPackage(name);
      }
    }
    assert Jack.getLookupFormatter().getName(currentPackage).equals(packageName);

    return currentPackage;
  }

  /**
   * Find a {@link JType} from his name.
   *
   * @param typeName Name of the searched type. The type name must have the following form
   *        Ljava/jang/String;.
   * @return The {@link JType} found.
   */
  @Nonnull
  public abstract JType getType(@Nonnull String typeName) throws JTypeLookupException;

  @Nonnull
  public abstract JClass getClass(@Nonnull String typeName) throws JTypeLookupException;

  @Nonnull
  public abstract JEnum getEnum(@Nonnull String typeName) throws JTypeLookupException;

  @Nonnull
  public abstract JInterface getInterface(@Nonnull String typeName) throws JTypeLookupException;

  @Nonnull
  public abstract JAnnotation getAnnotation(@Nonnull String signature) throws JTypeLookupException;

  public abstract void clear();

  @Nonnull
  public JClass getClass(@Nonnull CommonType type) throws JTypeLookupException {
    return commonTypesCache.getClass(type);
  }

  @Nonnull
  public JInterface getInterface(@Nonnull CommonType type) throws JTypeLookupException {
    return commonTypesCache.getInterface(type);
  }

  @Nonnull
  public JType getType(@Nonnull CommonType type) throws JTypeLookupException {
     return commonTypesCache.getType(type);
  }

  /**
   * Find a {@link JArrayType} from its leaf type and dimension.
   * @param leafType Requested leaf type
   * @param dimension Dimension for this array.
   * @return An instance of a {@code leafType} JArrayType of dimension {@code dimension}
   */
  @Nonnull
  public JArrayType getArrayType(@Nonnull JType leafType, @Nonnegative int dimension) {
    assert dimension > 0;
    JType array = leafType;
    for (int i = 0; i < dimension; i++) {
      array = array.getArray();
    }
    return (JArrayType) array;
  }

  @Nonnull
  protected JArrayType getArrayType(@Nonnull String typeName) throws JTypeLookupException {
    int typeNameLength = typeName.length();
    assert typeNameLength > 0 && typeName.charAt(0) == '[';

    int dim = 0;
    do {
      dim++;
      assert dim < typeNameLength;
    } while (typeName.charAt(dim) == '[');

    return getArrayType(getType(typeName.substring(dim)), dim);
  }

}
