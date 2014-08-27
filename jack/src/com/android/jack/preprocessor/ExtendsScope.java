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
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClassOrInterface;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * {@link Scope} for an extends rule.
 */
public class ExtendsScope implements Scope {

  @Nonnull
  private final JDefinedClassOrInterface classOrInterface;

  public ExtendsScope(@Nonnull JDefinedClassOrInterface classOrInterface) {
    this.classOrInterface = classOrInterface;
  }

  /* The cast is unchecked but the casted value is checked by Class.isInstance */
  @SuppressWarnings("unchecked")
  @Override
  public <T> Set<T> getNodesByName(@Nonnull Class<T> nodeType,
      @Nonnull NamePattern name) {
    if (!JClassOrInterface.class.isAssignableFrom(nodeType)
        && !nodeType.isAssignableFrom(JClassOrInterface.class)) {
      return Collections.<T>emptySet();
    }

    Collection<JClassOrInterface> matched = new HashSet<JClassOrInterface>();
    for (JClassOrInterface jcoi : classOrInterface.getHierarchy()) {
      if (nodeType.isInstance(jcoi)
          && name.matches(Jack.getUserFriendlyFormatter().getName(jcoi))) {
        matched.add(jcoi);
      }
    }
    return (Set<T>) matched;
  }

}
