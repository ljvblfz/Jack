/*
* Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.jayce.v0003.nodes;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.formatter.TypeAndMethodFormatter;
import com.android.jack.jayce.DeclaredTypeNode;
import com.android.jack.jayce.MethodNode;
import com.android.jack.jayce.NodeLevel;
import com.android.jack.jayce.v0003.NNode;
import com.android.jack.jayce.v0003.io.ExportSession;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Base class for any declared type.
 */
public abstract class NDeclaredType extends NNode implements HasSourceInfo, DeclaredTypeNode {

  @CheckForNull
  protected NodeLevel level;

  @Nonnull
  public List<NMethod> methods = Collections.emptyList();
  @Nonnull
  private final TypeAndMethodFormatter lookupFormatter = Jack.getLookupFormatter();

  @Override
  @Nonnull
  public abstract JDefinedClassOrInterface exportAsJast(@Nonnull ExportSession exportSession);

  @Override
  @Nonnull
  public NodeLevel getLevel() {
    assert level != null;
    return level;
  }

  @Override
  @Nonnull
  public MethodNode getMethodNode(@Nonnull JMethod jMethod) {
    if (jMethod instanceof JConstructor) {
      for (NMethod nMethod : methods) {
        if (nMethod.name == null
            && isSameArgTypeList(jMethod, nMethod)) {
          assert nMethod instanceof NConstructor;
          return nMethod;
        }
      }
    } else {
      for (NMethod nMethod : methods) {
        if (jMethod.getName().equals(nMethod.name)
            && lookupFormatter.getName(jMethod.getType()).equals(nMethod.returnType)
            && isSameArgTypeList(jMethod, nMethod)) {
          return nMethod;
        }
      }
    }
    throw new IllegalArgumentException("getMethodNode can be called only with JMethod argument" +
        " created by the receiver DeclaredTypeNode. " + jMethod + " was not created by " + this);
  }

  private boolean isSameArgTypeList(@Nonnull JMethod jMethod, @Nonnull NMethod nMethod) {
    List<JParameter> jParams = jMethod.getParams();
    List<NParameter> nParams = nMethod.parameters;
    if (jParams.size() != nParams.size()) {
      return false;
    }
    Iterator<JParameter> jParamIterator = jParams.iterator();
    for (NParameter nParam : nParams) {
      JParameter jParam = jParamIterator.next();
      if (!lookupFormatter.getName(jParam.getType()).equals(nParam.type)) {
        return false;
      }
    }
    return true;
  }

}
