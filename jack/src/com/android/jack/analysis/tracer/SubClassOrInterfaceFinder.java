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

package com.android.jack.analysis.tracer;

import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JSession;
import com.android.jack.shrob.obfuscation.SubClassOrInterfaceMarker;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Access;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/**
 * Fills the marker {@code SubClassOrInterfaceMarker} with the list of all classes
 * and interfaces extending or implementing the marked type
 */
@Description("Fills the marker SubClassOrInterfaceMarker with the list of all classes and " +
    "interfaces extending or implementing the marked type")
@Transform(add = SubClassOrInterfaceMarker.class)
// Visit hierarchy.
@Access(JSession.class)
public class SubClassOrInterfaceFinder implements RunnableSchedulable<JPackage> {

  private void addToSubClass(
      @Nonnull JDefinedClassOrInterface subClass, @Nonnull JClassOrInterface superClOrI) {
    JNode castedSuperClOrI = (JNode) superClOrI;
    SubClassOrInterfaceMarker marker = castedSuperClOrI.getMarker(SubClassOrInterfaceMarker.class);
    if (marker == null) {
      SubClassOrInterfaceMarker newMarker = new SubClassOrInterfaceMarker();
      marker = castedSuperClOrI.addMarkerIfAbsent(newMarker);
      if (marker == null) {
        marker = newMarker;
      }
    }
    assert marker != null;
    marker.addSubClassOrInterface(subClass);
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
  public void run(@Nonnull JPackage pack) {
    for (JClassOrInterface type : pack.getTypes()) {

      if (type instanceof JDefinedClassOrInterface) {
        JDefinedClassOrInterface definedType = (JDefinedClassOrInterface) type;
        if (type instanceof JDefinedClass) {
          JDefinedClass definedClass = (JDefinedClass) type;
          JClass superClass = definedClass.getSuperClass();

          if (superClass != null) {
            addToSubClass(definedClass, superClass);
          }
        }
        for (JInterface i : definedType.getImplements()) {
          addToSubClass(definedType, i);
        }
      }
    }
  }

}
