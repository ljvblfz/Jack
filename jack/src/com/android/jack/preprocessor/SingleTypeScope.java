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
import com.android.jack.ir.ast.JType;

import java.util.Collections;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * {@link Scope} containing a single type.
 */
public class SingleTypeScope implements Scope {

  @Nonnull
  private final JType element;

  public SingleTypeScope(@Nonnull JType element) {
    this.element = element;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> Set<T> getNodesByName(@Nonnull Class<T> nodeType, @Nonnull NamePattern name) {
    if (nodeType.isInstance(element)
        && name.matches(Jack.getUserFriendlyFormatter().getName(element))) {
      assert nodeType.isInstance(element);
      return (Set<T>) Collections.singleton(element);
    } else {
      return Collections.emptySet();
    }
  }

  @Nonnull
  public JType getElement() {
    return element;
  }

}
