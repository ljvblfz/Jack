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
import com.android.jack.ir.ast.JAnnotationType;
import com.android.jack.ir.ast.JArrayType;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JEnum;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JReferenceType;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.ast.MissingJTypeLookupException;
import com.android.jack.ir.formatter.TypeFormatter;
import com.android.jack.lookup.CommonTypes.CommonType;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Phantom lookup.
 */
public class JPhantomLookup extends JLookup {

  private abstract static class PhantomAdapter <T extends JReferenceType>
    implements Adapter<T> {

    @Override
    @Nonnull
    public JPackage getPackage(@Nonnull JPackage pack, @Nonnull String simpleName) {
      return pack.getOrCreateSubPackage(simpleName);
    }

    @Nonnull
    public abstract T getDefined(@Nonnull String signature);
  }

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
  private final Map<String, JAnnotationType> annotationCache =
      new HashMap<String, JAnnotationType>();

  @Nonnull
  private final PhantomAdapter<JReferenceType> coiAdapter =
  new PhantomAdapter<JReferenceType>() {
    @Nonnull
    @Override
    public Map<String, JReferenceType> getCache() {
      return typeCache;
    }

    @Nonnull
    @Override
    public JReferenceType getType(@Nonnull JPackage pack, @Nonnull String simpleName) {
      return pack.getPhantomClassOrInterface(simpleName);
    }

    @Override
    @Nonnull
    public JReferenceType getDefined(@Nonnull String signature) {
      throw new UnsupportedOperationException();
    }
  };

  @Nonnull
  private final PhantomAdapter<JClass> classAdapter = new PhantomAdapter<JClass>() {
    @Nonnull
    @Override
    public Map<String, JClass> getCache() {
      return classCache;
    }

    @Nonnull
    @Override
    public JClass getType(@Nonnull JPackage pack, @Nonnull String simpleName) {
      return pack.getPhantomClass(simpleName);
    }

    @Override
    @Nonnull
    public JClass getDefined(@Nonnull String signature) {
      return jackLookup.getClass(signature);
    }
  };

  @Nonnull
  private final PhantomAdapter<JEnum> enumAdapter = new PhantomAdapter<JEnum>() {
    @Nonnull
    @Override
    public Map<String, JEnum> getCache() {
      return enumCache;
    }

    @Nonnull
    @Override
    public JEnum getType(@Nonnull JPackage pack, @Nonnull String simpleName) {
      return pack.getPhantomEnum(simpleName);
    }

    @Override
    @Nonnull
    public JEnum getDefined(@Nonnull String signature) {
      return jackLookup.getEnum(signature);
    }
  };

  @Nonnull
  private final PhantomAdapter<JInterface> interfaceAdapter = new PhantomAdapter<JInterface>() {
    @Nonnull
    @Override
    public Map<String, JInterface> getCache() {
      return interfaceCache;
    }

    @Nonnull
    @Override
    public JInterface getType(@Nonnull JPackage pack, @Nonnull String simpleName) {
      return pack.getPhantomInterface(simpleName);
    }

    @Override
    @Nonnull
    public JInterface getDefined(@Nonnull String signature) {
      return jackLookup.getInterface(signature);
    }
  };

  @Nonnull
  private final PhantomAdapter<JAnnotationType> annotationAdapter =
      new PhantomAdapter<JAnnotationType>() {
    @Nonnull
    @Override
    public Map<String, JAnnotationType> getCache() {
      return annotationCache;
    }

    @Nonnull
    @Override
    public JAnnotationType getType(@Nonnull JPackage pack, @Nonnull String simpleName) {
      return pack.getPhantomAnnotationType(simpleName);
    }

    @Override
    @Nonnull
    public JAnnotationType getDefined(@Nonnull String signature) {
      return jackLookup.getAnnotationType(signature);
    }
  };

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
      try {
        type = getType(signature, coiAdapter);
      } catch (MissingJTypeLookupException t) {
        throw new AssertionError(signature);
      }
    }
    return type;
  }

  @Override
  protected <T extends JReferenceType> T getNonArrayType(
      @Nonnull String signature,
      @Nonnull Adapter<T> adapter) {
    Map<String, T> cache = adapter.getCache();
    T type;
    try {
      type = ((PhantomAdapter<T>) adapter).getDefined(signature);
      assert !doesCacheContain(cache, signature);
    } catch (JLookupException e) {
      try {
        type = super.getNonArrayType(signature, adapter);
      } catch (MissingJTypeLookupException t) {
        throw new AssertionError(signature);
      }
    }
    return type;
  }

  @Override
  @Nonnull
  public JClass getClass(@Nonnull String signature) {
    return getNonArrayType(signature, classAdapter);
  }

  @Override
  @Nonnull
  public JInterface getInterface(@Nonnull String signature) {
    return getNonArrayType(signature, interfaceAdapter);
  }

  @Override
  @Nonnull
  public JAnnotationType getAnnotationType(@Nonnull String signature) {
    return getNonArrayType(signature, annotationAdapter);
  }

  @Override
  @Nonnull
  public JEnum getEnum(@Nonnull String signature) {
    return getNonArrayType(signature, enumAdapter);
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

  private void clear() {
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

  @Override
  public void removeType(@Nonnull JType type) {
    clear();
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
    checkCacheContent(checkedType, signature, defined, JAnnotationType.class, annotationCache);
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
  protected JArrayType findArrayType(@Nonnull String typeName) {
    try {
      return super.findArrayType(typeName);
    } catch (JTypeLookupException e) {
      // should not happen since this is a phantom lookup
      throw new AssertionError(e);
    }
  }
}
