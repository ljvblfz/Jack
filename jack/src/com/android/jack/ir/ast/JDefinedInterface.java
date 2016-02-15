/*
 * Copyright 2007 Google Inc.
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
import com.android.jack.load.ClassOrInterfaceLoader;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.lookup.JMethodWithReturnLookupException;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import java.util.Iterator;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Java interface type definition.
 */
@Description("Java interface type definition")
public class JDefinedInterface extends JDefinedClassOrInterface implements JInterface {

  private static class SamNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;
  }

  public JDefinedInterface(@Nonnull SourceInfo info, @Nonnull String name, int modifier,
      @Nonnull JPackage enclosingPackage, @Nonnull ClassOrInterfaceLoader loader) {
    super(info, name, modifier, enclosingPackage, loader);
    assert JModifier.isInterface(modifier);
    assert JModifier.isAbstract(modifier);
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      if (visitor.needLoading()) {
        loader.ensureFields(this);
        loader.ensureMethods(this);
        loader.ensureAnnotations(this);
      }
      visitor.accept(fields);
      visitor.accept(methods);
      visitor.accept(annotations);
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    for (JField field : fields) {
      field.traverse(schedule);
    }
    for (JMethod method : methods) {
      method.traverse(schedule);
    }
    for (JAnnotation annotation : annotations) {
      annotation.traverse(schedule);
    }
  }

  @Override
  protected void transform(@Nonnull JNode existingNode, @CheckForNull JNode newNode,
      @Nonnull Transformation transformation) throws UnsupportedOperationException {
    if (!transform(fields, existingNode, (JField) newNode, transformation)) {
      if (!transform(methods, existingNode, (JMethod) newNode, transformation)) {
        super.transform(existingNode, newNode, transformation);
      }
    }
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }

  @Override
  public boolean canBeSafelyUpcast(@Nonnull JReferenceType castTo) {
    if (isTrivialCast(castTo)
        || (castTo instanceof JInterface && this.implementsInterface((JInterface) castTo))) {
      return true;
    }

    return false;
  }

  @CheckForNull
  public JMethod getSingleAbstractMethod() {
    JClass jlo = Jack.getSession().getLookup().getClass(CommonTypes.JAVA_LANG_OBJECT);

    if (!(jlo instanceof JDefinedClass)) {
      return null;
    }

    JMethod samMethod = null;

    try {
      samMethod = getSamMethod(jlo);
    } catch (SamNotFoundException e) {
    }

    return samMethod;
  }

  @Nonnull
  private JMethod getSamMethod(@Nonnull JClass jlo) throws SamNotFoundException {
    JMethod samMethod = null;
    for (JMethod mth : getMethods()) {
      if (!mth.isAbstract()) {
        continue;
      }
      try {
        JMethod jloMth = ((JDefinedClass) jlo).getMethod(mth.getName(), mth.getType(),
            mth.getMethodIdWide().getParamTypes());
        if (jloMth.isPublic() && !jloMth.isStatic()) {
          // Do not take overriding of jlo public non static methods
          continue;
        }
      } catch (JMethodWithReturnLookupException e) {
        // Ok
      }
      if (samMethod == null) {
        samMethod = mth;
      } else {
        throw new SamNotFoundException();
      }
    }

    for (JInterface jInterface : getImplements()) {

      if (jInterface instanceof JDefinedInterface) {
        JMethod newSamMethod = ((JDefinedInterface) jInterface).getSamMethod(jlo);
        if (samMethod == null) {
          samMethod = newSamMethod;
        } else {
          if (newSamMethod != null && !methodAreEquals(samMethod, newSamMethod)) {
            throw new SamNotFoundException();
          }
        }
      } else {
        throw new SamNotFoundException();
      }
    }

    return samMethod;
  }

  private boolean methodAreEquals(@Nonnull JMethod mth1, @Nonnull JMethod mth2) {
    if (!(mth1.getName().equals(mth2.getName()))) {
      return false;
    }

    List<JParameter> mth1Params = mth1.getParams();
    List<JParameter> mth2Params = mth2.getParams();

    if (mth1Params.size() != mth2Params.size()) {
      return false;
    }

    Iterator<JParameter> otherParams = mth1Params.iterator();
    for (JParameter param : mth2Params) {
      if (!param.getType().isSameType(otherParams.next().getType())) {
        return false;
      }
    }

    return true;
  }

  public boolean isSingleAbstractMethodType() {
    return getSingleAbstractMethod() != null;
  }
}
