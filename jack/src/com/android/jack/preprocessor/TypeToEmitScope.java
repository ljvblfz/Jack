/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.preprocessor;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.formatter.TypeFormatter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Global {@link Scope} giving access to all types to emit by their full name.
 */
public class TypeToEmitScope implements Scope {

  @Nonnull
  private final JSession session;

  @Nonnull
  private final TypeFormatter formater = Jack.getUserFriendlyFormatter();

  public TypeToEmitScope(@Nonnull JSession session) {
    this.session = session;
  }

  @SuppressWarnings("unchecked")
  @Override
  @Nonnull
  public <T> Set<T> getNodesByName(@Nonnull Class<T> nodeType, @Nonnull NamePattern name) {
    if (!JDefinedClassOrInterface.class.isAssignableFrom(nodeType)
        && !nodeType.isAssignableFrom(JDefinedClassOrInterface.class)) {
      return Collections.emptySet();
    }
    Collection<JDefinedClassOrInterface> matched = new HashSet<JDefinedClassOrInterface>();
    for (JDefinedClassOrInterface coi : session.getTypesToEmit()) {
      if (nodeType.isInstance(coi) && name.matches(formater.getName(coi))) {
        matched.add(coi);
      }
    }
    return (Set<T>) matched;
  }

}
