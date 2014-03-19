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

package com.android.jack.shrob.shrink;

import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JNode;
import com.android.sched.item.Description;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/**
 * Fills the marker {@code ExtendingOrImplementingClassMarker} with the list of all classes
 * extending or implementing the marked type
 */
@Description("Fills the marker ExtendingOrImplementingClassMarker with the list of all classes"
    + "extending or implementing the marked type")
@Synchronized
@Transform(add = ExtendingOrImplementingClassMarker.class)
public class ExtendingOrImplementingClassFinder
implements RunnableSchedulable<JDefinedClassOrInterface> {

  private void addToSubClass(
      @Nonnull JDefinedClass subClass, @Nonnull JClassOrInterface superClOrI) {
    ExtendingOrImplementingClassMarker marker =
        ((JNode) superClOrI).getMarker(ExtendingOrImplementingClassMarker.class);
    if (marker == null) {
      marker = new ExtendingOrImplementingClassMarker();
      ((JNode) superClOrI).addMarker(marker);
    }
    marker.addSubClass(subClass);
    if (superClOrI instanceof JDefinedClassOrInterface) {
      if (superClOrI instanceof JDefinedClass) {
        JClass superClass = ((JDefinedClass) superClOrI).getSuperClass();
        if (superClass != null) {
          addToSubClass(subClass, superClass);
        }
      }
      for (JInterface i : ((JDefinedClassOrInterface) superClOrI).getImplements()) {
        addToSubClass(subClass, i);
      }
    }
  }

  @Override
  public synchronized void run(@Nonnull JDefinedClassOrInterface t) throws Exception {
    if (t instanceof JDefinedClass) {
      JDefinedClass definedClass = (JDefinedClass) t;
      JClass superClass = definedClass.getSuperClass();

      if (superClass != null) {
        addToSubClass(definedClass, superClass);
      }

      for (JInterface i : definedClass.getImplements()) {
        addToSubClass(definedClass, i);
      }
    }
  }

}
