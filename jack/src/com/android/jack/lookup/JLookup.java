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
import com.android.jack.ir.ast.JAnnotationType;
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

  /**
   * A {@link Splitter} that splits a package name based on the package separator character
   */
  @Nonnull
  protected static final Splitter packageBinaryNameSplitter =
    Splitter.on(JLookup.PACKAGE_SEPARATOR);

  @Nonnull
  private final CommonTypesCache commonTypesCache = new CommonTypesCache(this);

  /**
   * The {@link JPackage} representing the top level package
   */
  @Nonnull
  protected final JPackage topLevelPackage;

  @Nonnull
  private final Map<String, JPackage> packages = new HashMap<String, JPackage>();

  /**
   * The character used as package separator.
   */
  public static final char PACKAGE_SEPARATOR = '/';

  /**
   * Constructor specifying the top level {@link JPackage} instance
   *
   * @param topLevelPackage the top level package
   */
  protected JLookup(@Nonnull JPackage topLevelPackage) {
    this.topLevelPackage = topLevelPackage;
  }

  /**
   * Returns the {@link JPackage} denoted by the given {@code packageName}. The name of
   * the package must be of the form a/b/c.
   *
   * @param packageName the name of the package to lookup
   * @return the corresponding {@link JPackage}
   * @throws JPackageLookupException if the package does not exist
   */
  @Nonnull
  public JPackage getPackage(@Nonnull String packageName) throws JPackageLookupException {
    assert packageName.indexOf(NamingTools.PACKAGE_SOURCE_SEPARATOR) == -1;
    JPackage currentPackage = topLevelPackage;
    if (!packageName.isEmpty()) {
      for (String name : packageBinaryNameSplitter.split(packageName)) {
        currentPackage = currentPackage.getSubPackage(name);
      }
    }
    assert Jack.getLookupFormatter().getName(currentPackage).equals(packageName);

    return currentPackage;
  }


  /**
   * Returns the {@link JPackage} denoted by the given {@code packageName}. The package is created
   * if it does not exist yet. The name of the package must be of the form a/b/c.
   *
   * @param packageName the name of the package to lookup or create
   * @return the corresponding {@link JPackage}
   */
  @Nonnull
  public JPackage getOrCreatePackage(@Nonnull String packageName) {
    assert packageName.indexOf(NamingTools.PACKAGE_SOURCE_SEPARATOR) == -1;
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
   * Finds a {@link JType} for the given name
   *
   * @param typeSignature the signature of the searched type (of the form
   *        {@code Ljava/jang/String;})
   * @return the {@link JType} found
   * @throws JTypeLookupException if there is no {@link JType} for the given name
   */
  @Nonnull
  public abstract JType getType(@Nonnull String typeSignature) throws JTypeLookupException;

  /**
   * Finds a {@link JClass} for the given name
   *
   * @param typeSignature the type signature of the searched class (of the form
   *        {@code Ljava/jang/String;})
   * @return the {@link JClass} found
   * @throws JTypeLookupException if there is no {@link JClass} for the given name
   */
  @Nonnull
  public abstract JClass getClass(@Nonnull String typeSignature) throws JTypeLookupException;

  /**
   * Finds a {@link JEnum} for the given name
   *
   * @param typeSignature the signature of the searched enum (of the form
   *        {@code Ljava/jang/String;})
   * @return the {@link JEnum} found
   * @throws JTypeLookupException if there is no {@link JEnum} for the given name
   */
  @Nonnull
  public abstract JEnum getEnum(@Nonnull String typeSignature) throws JTypeLookupException;

  /**
   * Finds a {@link JInterface} for the given name
   *
   * @param typeSignature the signature of the searched interface (of the form
   * {@code Ljava/jang/String;})
   * @return the {@link JInterface} found
   * @throws JTypeLookupException if there is no {@link JInterface} for the given name
   */
  @Nonnull
  public abstract JInterface getInterface(@Nonnull String typeSignature)
      throws JTypeLookupException;

  /**
   * Finds a {@link JAnnotationType} for the given {@code signature}
   *
   * @param signature the signature of the annotation type (like 'Lfoo/FooAnnotation;')
   * @return the corresponding {@link JAnnotationType}
   * @throws JTypeLookupException if no annotation type matches that signature
   */
  @Nonnull
  public abstract JAnnotationType getAnnotationType(@Nonnull String signature)
      throws JTypeLookupException;

  /**
   * Removes the given {@link JType} from this lookup context.
   *
   * @param type the type to remove
   */
  public abstract void removeType(@Nonnull JType type);

  /**
   * Finds a {@link JClass} for the given common type.
   *
   * @param type the common type to lookup
   * @return an instance of {@link JClass}
   * @throws JTypeLookupException if there is no {@link JClass} for this type.
   */
  @Nonnull
  public JClass getClass(@Nonnull CommonType type) throws JTypeLookupException {
    return commonTypesCache.getClass(type);
  }

  /**
   * Finds a {@link JInterface} for the given common type.
   *
   * @param type the common type to lookup
   * @return an instance of {@link JInterface}
   * @throws JTypeLookupException if there is no {@link JInterface} for this type.
   */
  @Nonnull
  public JInterface getInterface(@Nonnull CommonType type) throws JTypeLookupException {
    return commonTypesCache.getInterface(type);
  }

  /**
   * Finds a {@link JType} for the given common type.
   *
   * @param type the common type to lookup
   * @return an instance of {@link JType}
   * @throws JTypeLookupException if there is no {@link JType} for this type.
   */
  @Nonnull
  public JType getType(@Nonnull CommonType type) throws JTypeLookupException {
     return commonTypesCache.getType(type);
  }

  /**
   * Finds a {@link JArrayType} from its leaf type and dimension.
   *
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
    }

    if (type == null) {
      int typeNameLength = signature.length();
      assert typeNameLength > 1 : "Invalid signature '" + signature + "'";
      if (signature.charAt(0) == '[') {
        type = (T) findArrayType(signature);
      } else {
        type = findClassOrInterface(signature, adapter);
      }
      synchronized (cache) {
        // Model already ensures unicity of types, so the worst that could happen here would be to
        // store the exact same type that is already stored.
        assert cache.get(signature) == null || cache.get(signature) == type;
        cache.put(signature, type);
      }
    }
    return type;
  }

  /**
   * Finds a {@link JArrayType} for the given type name.
   *
   * @param typeSignature the signature of the array type (of the form
   *        {@code '[Lfoo/Bar;'}, multiple '[' for multi-dimensional arrays)
   * @return an instance of {@link JArrayType} for this type name
   * @throws JTypeLookupException if there is no {@link JArrayType} for this type name
   */
  @Nonnull
  protected JArrayType findArrayType(@Nonnull String typeSignature) throws JTypeLookupException {
    int typeNameLength = typeSignature.length();
    assert typeNameLength > 0 && typeSignature.charAt(0) == '[';

    int dim = 0;
    do {
      dim++;
      assert dim < typeNameLength;
    } while (typeSignature.charAt(dim) == '[');

    return getArrayType(getType(typeSignature.substring(dim)), dim);
  }

  /**
   * Finds a non-array {@link JReferenceType} for the given signature and adapter.
   *
   * @param <T> the expected {@link JReferenceType}
   * @param signature the signature of the type of the form {@code Ljava/lang/String;}
   * @param adapter an {@link Adapter} for this lookup
   * @return the corresponding reference type.
   * @throws MissingJTypeLookupException if there is no non-array type for this signature
   */
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

  /**
   * Finds a {@link JPackage} for the given package name and adapter.
   *
   * @param packageName the name of the package (of the form {@code 'a/b/c'})
   * @param adapter an {@link Adapter} for this lookup
   * @return an instance of {@link JPackage}
   * @throws JPackageLookupException if there is no corresponding {@link JPackage}
   */
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

  /**
   * Finds a class or interface for the given signature and adapter.
   *
   * @param signature the type signature. It is of the form 'Ljava/lang/String;'
   * @param adapter an {@link Adapter} for this lookup
   * @return a class or interface, instance of a subclass of {@link JType}
   * @throws MissingJTypeLookupException if there is no corresponding class or interface
   */
  @Nonnull
  private <T extends JType> T findClassOrInterface(@Nonnull String signature,
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
