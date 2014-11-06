/*
 * Copyright (C) 2013 The Android Open Source Project
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
import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JArrayType;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JEnum;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JReferenceType;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.formatter.TypeFormatter;
import com.android.jack.lookup.CommonTypes.CommonType;
import com.android.jack.util.NamingTools;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Phantom lookup.
 */
public class JPhantomLookup extends JLookup {

  @Nonnull
  private final Map<String, JReferenceType> typeCache =
      new HashMap<String, JReferenceType>();
  @Nonnull
  private final Map<String, JClass> classCache =
      new HashMap<String, JClass>();
  @Nonnull
  private final Map<String, JEnum> enumCache =
      new HashMap<String, JEnum>();
  @Nonnull
  private final Map<String, JInterface> interfaceCache =
      new HashMap<String, JInterface>();
  @Nonnull
  private final Map<String, JAnnotation> annotationCache =
      new HashMap<String, JAnnotation>();

  @Nonnull
  private final JNodeLookup jackLookup;

  public JPhantomLookup(@Nonnull JNodeLookup jackLookup) {
    super(jackLookup.getTopLevelPackage());
    this.jackLookup = jackLookup;
  }

  /**
   * Find a {@link JType} from his name.
   *
   * @param signature Name of the searched type. The type name must have the following form
   *        Ljava/jang/String;.
   * @return The {@link JType} found.
   */
  @Override
  @Nonnull
  public JType getType(@Nonnull String signature) {
    JType type;
    try {
      type = jackLookup.getType(signature);
      assert !doesCacheContain(typeCache, signature);
    } catch (JLookupException e) {
      synchronized (typeCache) {
        type = typeCache.get(signature);

        if (type == null) {
          int typeNameLength = signature.length();
          assert typeNameLength > 1 : "Invalid signature '" + signature + "'";
          if (signature.charAt(0) == '[') {
            JArrayType array = getArrayType(signature);
            type = array;
            typeCache.put(signature, array);
          } else {
            String[] splitName = splitSignature(signature);
            JPackage pack = getPackage(splitName);
            JClassOrInterface phantom =
                pack.getPhantomClassOrInterface(splitName[splitName.length - 1]);
            typeCache.put(signature, phantom);
            type = phantom;
          }
        }
      }
    }
    return type;
  }

  @Nonnull
  private JPackage getPackage(@Nonnull String[] splitClassOrInterfaceName) {
    JPackage currentPackage = topLevelPackage;
    int packageLength = splitClassOrInterfaceName.length - 1;
    for (int i = 0; i < packageLength; i++) {
      currentPackage = currentPackage.getOrCreateSubPackage(splitClassOrInterfaceName[i]);
    }
    return currentPackage;
  }

  @Nonnull
  private String[] splitSignature(@Nonnull String signature) {
    assert NamingTools.isClassDescriptor(signature);
    String[] splitName = signature.substring(1, signature.length() - 1)
        .split(String.valueOf(JLookup.PACKAGE_SEPARATOR));
    return splitName;
  }

  @Override
  @Nonnull
  public JClass getClass(@Nonnull String signature) {
    JClass type;
    try {
      type = jackLookup.getClass(signature);
      assert !classCache.containsKey(signature);
    } catch (JLookupException e) {
      synchronized (classCache) {
        type = classCache.get(signature);

        if (type == null) {
          String[] splitName = splitSignature(signature);
          JPackage pack = getPackage(splitName);
          type = pack.getPhantomClass(splitName[splitName.length - 1]);
          classCache.put(signature, type);
        }
      }
    }
    return type;
  }

  @Override
  @Nonnull
  public JInterface getInterface(@Nonnull String signature) {
    JInterface type;
    try {
      type = jackLookup.getInterface(signature);
      assert !doesCacheContain(interfaceCache, signature);
    } catch (JLookupException e) {
      synchronized (interfaceCache) {
        type = interfaceCache.get(signature);

        if (type == null) {
          String[] splitName = splitSignature(signature);
          JPackage pack = getPackage(splitName);
          type = pack.getPhantomInterface(splitName[splitName.length - 1]);
          interfaceCache.put(signature, type);
        }
      }
    }
    return type;
  }

  @Override
  @Nonnull
  public JAnnotation getAnnotation(@Nonnull String signature) {
    JAnnotation type;
    try {
      type = jackLookup.getAnnotation(signature);
      assert !doesCacheContain(annotationCache, signature);
    } catch (JLookupException e) {
      synchronized (annotationCache) {
        type = annotationCache.get(signature);

        if (type == null) {
          String[] splitName = splitSignature(signature);
          JPackage pack = getPackage(splitName);
          type = pack.getPhantomAnnotation(splitName[splitName.length - 1]);
          annotationCache.put(signature, type);
        }
      }
    }
    return type;
  }

  @Override
  @Nonnull
  public JEnum getEnum(@Nonnull String signature) {
    JEnum type;
    try {
      type = jackLookup.getEnum(signature);
      assert !doesCacheContain(enumCache, signature);
    } catch (JLookupException e) {
      synchronized (enumCache) {
        type = enumCache.get(signature);
        if (type == null) {
          String[] splitName = splitSignature(signature);
          JPackage pack = getPackage(splitName);
          type = pack.getPhantomEnum(splitName[splitName.length - 1]);
          enumCache.put(signature, type);
        }
      }
    }
    return type;
  }

  @Override
  @Nonnull
  public JClass getClass(@Nonnull CommonType type) {
    try {
      return super.getClass(type);
    } catch (JTypeLookupException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  @Nonnull
  public JInterface getInterface(@Nonnull CommonType type) {
    try {
      return super.getInterface(type);
    } catch (JTypeLookupException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  @Nonnull
  public JType getType(@Nonnull CommonType type) {
    try {
      return super.getType(type);
    } catch (JTypeLookupException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public void clear() {
    synchronized (typeCache) {
      typeCache.clear();
    }
    synchronized (classCache) {
    classCache.clear();
    }
    synchronized (enumCache) {
      enumCache.clear();
    }
    synchronized (interfaceCache) {
      interfaceCache.clear();
    }
    synchronized (annotationCache) {
      annotationCache.clear();
    }
  }

  private boolean doesCacheContain(@Nonnull Map<String, ? extends JReferenceType> cache,
      @Nonnull String signature) {
    synchronized (cache) {
      return cache.containsKey(signature);
    }
  }

  /**
   * Check that given {@link JType} does not conflict with types already known by this lookup.
   * This method is intended as a support for assert when modifying type's name or package.
   *
   * @return always return true, throws an {@link AssertionError} in case of conflict.
   */
  public boolean check(@Nonnull JType checkedType) {
    String signature = Jack.getLookupFormatter().getName(checkedType);
    JType defined;
    try {
      defined = jackLookup.getType(signature);
      if (!defined.isSameType(checkedType)) {
        throw getCheckError(checkedType, defined);
      }
    } catch (JLookupException e) {
      defined = null;
    }

    checkCacheContent(checkedType, signature, defined, JType.class, typeCache);
    checkCacheContent(checkedType, signature, defined, JClass.class, classCache);
    checkCacheContent(checkedType, signature, defined, JEnum.class, enumCache);
    checkCacheContent(checkedType, signature, defined, JInterface.class, interfaceCache);
    checkCacheContent(checkedType, signature, defined, JAnnotation.class, annotationCache);
    return true;
  }

  private void checkCacheContent(@Nonnull JType checkedType,
      @Nonnull String signature,
      @CheckForNull JType defined,
      @Nonnull Class<?> clazz,
      @Nonnull Map<String, ? extends JType> cache) {
    if (!clazz.isInstance(defined)) {
      JType phantom = cache.get(signature);
      if (phantom != null && !phantom.isSameType(checkedType)) {
        throw getCheckError(checkedType, phantom);
      }
    }
  }

  @Nonnull
  private static AssertionError getCheckError(@Nonnull JType checkedType, @Nonnull JType ref) {
    TypeFormatter formatter = Jack.getLookupFormatter();
    return new AssertionError(formatter.getName(checkedType) + " ("
        + checkedType.getClass().getName() + ") does not equal with " + formatter.getName(ref)
        + " (" + ref.getClass().getName() + ")");
  }

  @Override
  @Nonnull
  protected JArrayType getArrayType(@Nonnull String typeName) {
    try {
      return super.getArrayType(typeName);
    } catch (JTypeLookupException e) {
      // should not happen since this is a phantom lookup
      throw new AssertionError(e);
    }
  }
}
