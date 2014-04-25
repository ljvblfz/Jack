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

package com.android.jack.shrob.obfuscation;

import com.android.jack.JackIOException;
import com.android.jack.ir.ast.HasName;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JFieldId;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.shrob.obfuscation.nameprovider.DictionaryNameProvider;
import com.android.jack.shrob.obfuscation.nameprovider.MappingNameProvider;
import com.android.jack.shrob.obfuscation.nameprovider.NameProvider;
import com.android.jack.shrob.obfuscation.nameprovider.UniqueNameProvider;
import com.android.sched.marker.MarkerManager;
import com.android.sched.util.codec.DefaultFactorySelector;
import com.android.sched.util.config.DefaultFactory;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.PropertyId;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Factory creating the {@link NameProvider}s
 */
@HasKeyId
public class NameProviderFactory {

  @Nonnull
  public static final PropertyId<DefaultFactory<NameProvider>> NAMEPROVIDER = PropertyId.create(
      "jack.obfuscation.nameprovider", "Define which nameprovider to use",
      new DefaultFactorySelector<NameProvider>(NameProvider.class)).addDefaultValue("lower-case");

  @CheckForNull
  private final File obfuscationDictionary;

  @CheckForNull
  private final File classObfuscationDictionary;

  @CheckForNull
  private final File packageObfuscationDictionary;

  @CheckForNull
  private NameProvider globalFieldNameProvider;

  @CheckForNull
  private NameProvider globalMethodNameProvider;

  @Nonnull
  private final DefaultFactory<NameProvider> defaultNameProviderFactory;

  public NameProviderFactory(
      @CheckForNull File obfuscationDictionary,
      @CheckForNull File classObfuscationDictionary,
      @CheckForNull File packageObfuscationDictionary) {
    this.obfuscationDictionary = obfuscationDictionary;
    this.classObfuscationDictionary = classObfuscationDictionary;
    this.packageObfuscationDictionary = packageObfuscationDictionary;
    this.defaultNameProviderFactory = ThreadConfig.get(NAMEPROVIDER);
  }

  @Nonnull
  private NameProvider getNameProvider(@CheckForNull File dictionary) throws JackIOException {
    NameProvider defaultNameProvider = defaultNameProviderFactory.create();
    if (dictionary != null) {
      return new DictionaryNameProvider(dictionary, defaultNameProvider);
    }
    return defaultNameProvider;
  }

  private void fillExistingName(@Nonnull Collection<? extends HasName> namedElements,
      @Nonnull Collection<String> existingNames) {
    for (HasName namedElement : namedElements) {
      if (!Renamer.mustBeRenamed((MarkerManager) namedElement)) {
        existingNames.add(Renamer.getKey(namedElement));
      }
    }
  }

  @Nonnull
  public NameProvider getPackageNameProvider(@Nonnull Collection<JPackage> packages)
      throws JackIOException {
    Set<String> existingNames = new HashSet<String>();
    fillExistingName(packages, existingNames);
    return new UniqueNameProvider(getNameProvider(packageObfuscationDictionary), existingNames);
  }

  @Nonnull
  public NameProvider getClassNameProvider(@Nonnull Collection<? extends JClassOrInterface> types) {
    Set<String> existingNames = new HashSet<String>();
    fillExistingName(types, existingNames);
    return new UniqueNameProvider(getNameProvider(classObfuscationDictionary), existingNames);
  }

  @Nonnull
  public NameProvider getFieldNameProvider(@Nonnull Collection<JFieldId> fieldIds)
      throws JackIOException {
    NameProvider provider;
    if (globalFieldNameProvider != null) {
      provider = globalFieldNameProvider;
    } else {
      Set<String> existingNames = new HashSet<String>();
      fillExistingName(fieldIds, existingNames);
      provider = new UniqueNameProvider(getNameProvider(obfuscationDictionary), existingNames);
    }
    return provider;
  }

  @Nonnull
  public NameProvider getMethodNameProvider(@Nonnull Collection<JMethodId> methodIds)
      throws JackIOException {
    NameProvider provider;
    if (globalMethodNameProvider != null) {
      provider = globalMethodNameProvider;
    } else {
      Set<String> existingNames = new HashSet<String>();
      fillExistingName(methodIds, existingNames);
      provider = new UniqueNameProvider(getNameProvider(obfuscationDictionary), existingNames);
    }
    return provider;
  }

  public void createGlobalFieldNameProvider(
      @Nonnull Map<String, String> existingNames, @Nonnull Collection<JFieldId> fieldIds)
      throws JackIOException {
    for (JFieldId fid : fieldIds) {
      if (!Renamer.mustBeRenamed(fid)) {
        existingNames.put(Renamer.getKey(fid), fid.getName());
      }
    }
    globalFieldNameProvider =
        new MappingNameProvider(getNameProvider(obfuscationDictionary), existingNames);
  }

  public void createGlobalMethodNameProvider(
      @Nonnull Map<String, String> existingNames, @Nonnull Collection<JMethodId> methodIds)
      throws JackIOException {
    for (JMethodId mid : methodIds) {
      if (!Renamer.mustBeRenamed(mid)) {
        existingNames.put(Renamer.getKey(mid), mid.getName());
      }
    }
    globalMethodNameProvider =
        new MappingNameProvider(getNameProvider(obfuscationDictionary), existingNames);
  }
}
