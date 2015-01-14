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
import com.android.jack.ir.ast.JPackageLookupException;
import com.android.jack.ir.ast.JReferenceType;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.ast.MissingJTypeLookupException;
import com.android.jack.lookup.CommonTypes.CommonType;
import com.android.jack.util.NamingTools;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * {@link JLookup} allows to lookup {@link JType} from signature.
 */
public abstract class JLookup {

  /**
   * Adapter for the specificities of different lookup kind.
   */
  protected static interface Adapter<T extends JType> {
    @Nonnull
    Map<String, T> getCache();

    @Nonnull
    T getType(@Nonnull JPackage pack, @Nonnull String simpleName)
        throws MissingJTypeLookupException;

    @Nonnull
    JPackage getPackage(@Nonnull JPackage pack, @Nonnull String simpleName)
        throws JPackageLookupException;
  }

  @Nonnull
  protected static final Splitter packageBinaryNameSplitter =
    Splitter.on(JLookup.PACKAGE_SEPARATOR);

  @Nonnull
  private final CommonTypesCache commonTypesCache = new CommonTypesCache(this);

  @Nonnull
  protected final JPackage topLevelPackage;

  @Nonnull
  private final Map<String, JPackage> packages = new HashMap<String, JPackage>();

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

  @SuppressWarnings("unchecked")
  @Nonnull
  protected <T extends JType> T getType(@Nonnull String signature,
      @Nonnull Adapter<T> adapter) throws MissingJTypeLookupException {
    Map<String, T> cache = adapter.getCache();
    T type;
    synchronized (cache) {
      type = cache.get(signature);

      if (type == null) {
        int typeNameLength = signature.length();
        assert typeNameLength > 1 : "Invalid signature '" + signature + "'";
        if (signature.charAt(0) == '[') {
          type = (T) findArrayType(signature);
        } else {
          type = findClassOrInterface(signature, adapter);
        }
        cache.put(signature, type);
     }
    }
    return type;
  }

  @Nonnull
  protected JArrayType findArrayType(@Nonnull String typeName) throws JTypeLookupException {
    int typeNameLength = typeName.length();
    assert typeNameLength > 0 && typeName.charAt(0) == '[';

    int dim = 0;
    do {
      dim++;
      assert dim < typeNameLength;
    } while (typeName.charAt(dim) == '[');

    return getArrayType(getType(typeName.substring(dim)), dim);
  }

  protected <T extends JReferenceType> T getNonArrayType(
      @Nonnull String signature,
      @Nonnull Adapter<T> adapter) throws MissingJTypeLookupException {
    Map<String, T> cache = adapter.getCache();
    T type;
    synchronized (cache) {
      type = cache.get(signature);

      if (type == null) {
        type = findClassOrInterface(signature, adapter);
        cache.put(signature, type);
      }
    }
    return type;
  }

  @Nonnull
  protected JPackage getPackage(@Nonnull String packageName,
      @Nonnull Adapter<? extends JType> adapter)
      throws JPackageLookupException {

    synchronized (packages) {
      JPackage found = packages.get(packageName);
      if (found == null) {
        assert NamingTools.isPackageBinaryName(packageName);
        int separatorIndex = packageName.lastIndexOf(JLookup.PACKAGE_SEPARATOR);
        JPackage parent;
        String simplePackageName;
        if (separatorIndex == -1) {
          parent = topLevelPackage;
          simplePackageName = packageName;
        } else {
          parent = getPackage(packageName.substring(0, separatorIndex), adapter);
          simplePackageName = packageName.substring(separatorIndex + 1);
        }
        found = adapter.getPackage(parent, simplePackageName);
        packages.put(packageName, found);
      }
      return found;
    }
  }

  @Nonnull
  private <T extends JType, U extends Throwable> T findClassOrInterface(@Nonnull String signature,
      @Nonnull Adapter<T> adapter) throws MissingJTypeLookupException {

    assert NamingTools.isClassDescriptor(signature) : "Invalid signature '" + signature + "'";

    int typeNameLength = signature.length();
    int separatorIndex = signature.lastIndexOf(JLookup.PACKAGE_SEPARATOR);
    JPackage currentPackage;
    String simpleName;
    if (separatorIndex == -1) {
      currentPackage = topLevelPackage;
      simpleName = signature.substring(1, typeNameLength - 1);
    } else {
      try {
        currentPackage = getPackage(signature.substring(1, separatorIndex), adapter);
        simpleName = signature.substring(separatorIndex + 1, typeNameLength - 1);
      } catch (JPackageLookupException p) {
        throw new MissingJTypeLookupException(signature);
      }
    }
    return adapter.getType(currentPackage, simpleName);
  }

}
