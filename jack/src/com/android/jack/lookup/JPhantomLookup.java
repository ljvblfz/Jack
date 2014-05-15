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
import com.android.jack.lookup.CommonTypes.CommonType;
import com.android.jack.util.NamingTools;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

/**
 * Phantom lookup.
 */
public class JPhantomLookup extends JLookup {

  @Nonnull
  private final Map<String, JReferenceType> typeCache =
      new ConcurrentHashMap<String, JReferenceType>();
  @Nonnull
  private final Map<String, JClass> classCache =
      new ConcurrentHashMap<String, JClass>();
  @Nonnull
  private final Map<String, JEnum> enumCache =
      new ConcurrentHashMap<String, JEnum>();
  @Nonnull
  private final Map<String, JInterface> interfaceCache =
      new ConcurrentHashMap<String, JInterface>();
  @Nonnull
  private final Map<String, JAnnotation> annotationCache =
      new ConcurrentHashMap<String, JAnnotation>();

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
   * @throws JTypeLookupException
   */
  @Override
  @Nonnull
  public JType getType(@Nonnull String signature) throws JTypeLookupException {
    JType type;
    try {
      type = jackLookup.getType(signature);
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
    typeCache.clear();
    classCache.clear();
    enumCache.clear();
    interfaceCache.clear();
    annotationCache.clear();
  }
}
