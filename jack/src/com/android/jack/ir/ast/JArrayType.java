/*
 * Copyright 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.android.jack.ir.ast;

import com.android.jack.Jack;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.lookup.JPhantomLookup;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/**
 * Instances are shared.
 */
@Description("Java array type")
public class JArrayType extends JDefinedReferenceType {

  private int dims = 0;
  private JType elementType;
  private JType leafType = null;

  JArrayType(JType elementType) {
    super(SourceInfo.UNKNOWN, elementType.getName() + "[]");
    this.elementType = elementType;
    JPhantomLookup lookup = Jack.getSession().getPhantomLookup();
    addImplements(lookup.getInterface(CommonTypes.JAVA_IO_SERIALIZABLE));
    addImplements(lookup.getInterface(CommonTypes.JAVA_LANG_CLONEABLE));
    setSuperClass(lookup.getClass(CommonTypes.JAVA_LANG_OBJECT));
  }

  public int getDims() {
    if (dims == 0) {
      dims = 1;
      if (elementType instanceof JArrayType) {
        dims += ((JArrayType) elementType).getDims();
      }
    }
    return dims;
  }

  public JType getElementType() {
    return elementType;
  }

  public void setElementType(@Nonnull JType elementType) {
    this.elementType = elementType;
  }

  public JType getLeafType() {
    if (leafType == null) {
      if (elementType instanceof JArrayType) {
        leafType = ((JArrayType) elementType).getLeafType();
      } else {
        leafType = elementType;
      }
    }
    return leafType;
  }

  public void resetLeafType() {
    leafType = null;
  }

  @Override
  public boolean isExternal() {
    return elementType.isExternal();
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }

  @Override
  public boolean canBeSafelyUpcast(@Nonnull JReferenceType castTo) {
    if (isTrivialCast(castTo)
        || (castTo instanceof JInterface && implementsInterface((JInterface) castTo))) {
      return true;
    }

    if (castTo instanceof JArrayType) {
      JType castedToElementType = ((JArrayType) castTo).getElementType();
      if (elementType instanceof JReferenceType
          && castedToElementType instanceof JReferenceType) {
        return ((JReferenceType) elementType).canBeSafelyUpcast(
            (JReferenceType) castedToElementType);
      }
    }

    return false;
  }

  @Override
  public final boolean isSameType(@Nonnull JType type) {
    if (type instanceof JArrayType) {
      return getDims() == ((JArrayType) type).getDims()
          && getLeafType().isSameType(((JArrayType) type).getLeafType());
    } else {
      return false;
    }
  }

  @Override
  public void checkValidity() {
   throw new AssertionError();
  }
}
