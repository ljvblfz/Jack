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

package com.android.jack.annotationadder;

import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * {@link Scope} for members of a class.
 */
public class ClassMembersScope implements Scope {

  @Nonnull
  private final JDefinedClassOrInterface root;

  public ClassMembersScope(@Nonnull JDefinedClassOrInterface root) {
    this.root = root;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> Set<T> getNodesByName(@Nonnull Class<T> nodeType, @Nonnull NamePattern name) {
    Collection<Object> list = new HashSet<Object>();
    if (JField.class.isAssignableFrom(nodeType) || nodeType.isAssignableFrom(JField.class)) {
      for (JField field : root.getFields()) {
        if (nodeType.isInstance(field)
            && name.matches(field.getName())) {
          list.add(field);
        }
      }
    } else if (JMethod.class.isAssignableFrom(nodeType)
        || nodeType.isAssignableFrom(JMethod.class)) {
      for (JMethod method : root.getMethods()) {
        if (nodeType.isInstance(method)
            && name.matches(method.getName())) {
          list.add(method);
        }
      }
    } else if (JClassOrInterface.class.isAssignableFrom(nodeType)
        || nodeType.isAssignableFrom(JClassOrInterface.class)) {
      for (JClassOrInterface jcoi : root.getMemberTypes()) {
        if (nodeType.isInstance(jcoi)
            && name.matches(jcoi.getName())) {
          list.add(jcoi);
        }
      }
    }
    return (Set<T>) list;
  }

}
