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

package com.android.jack.ir.ast;

import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Description;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Base class for any defined reference type.
 */
@Description("Base class for any defined reference type.")
public abstract class JDefinedReferenceType extends JReferenceTypeCommon {

  /**
   * This type's implemented interfaces.
   */
  @Nonnull
  protected List<JInterface> superInterfaces = new ArrayList<JInterface>();

  public JDefinedReferenceType(@Nonnull SourceInfo info, @Nonnull String name) {
    super(info, name);
  }

  boolean implementsInterface(@Nonnull JInterface jinterface) {
    for (JInterface interf : getImplements()) {
      if (interf.isSameType(jinterface)) {
        return true;
      } else if (interf instanceof JDefinedInterface) {
        if (((JDefinedInterface) interf).implementsInterface(jinterface)) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Returns implemented or extended interfaces. Returns an empty list if this
   * type implements no interfaces.
   */
  @Nonnull
  public List<JInterface> getImplements() {
    return superInterfaces;
  }
}
