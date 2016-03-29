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

package com.android.jack.shrob.obfuscation;

import com.google.common.collect.Iterators;

import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JPhantomClassOrInterface;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * {@code Marker} that contains all the subclasses and subinterfaces of a type.
 */
@ValidOn({JDefinedClassOrInterface.class, JPhantomClassOrInterface.class})
@Description("Contains all the subclasses and subinterfaces of a type.")
public class SubClassOrInterfaceMarker implements Marker, Iterable<JDefinedClassOrInterface> {

  @Nonnull
  private final Set<JDefinedClass> subClasses = new HashSet<JDefinedClass>();

  @Nonnull
  private final Set<JDefinedInterface> subInterfaces = new HashSet<JDefinedInterface>();

  public void addSubClass(@Nonnull JDefinedClass subClass) {
    subClasses.add(subClass);
  }

  public void addSubInterface(@Nonnull JDefinedInterface subInterface) {
    subInterfaces.add(subInterface);
  }

  @Nonnull
  public Set<JDefinedClass> getSubClasses() {
    return subClasses;
  }

  @Nonnull
  public Set<JDefinedInterface> getSubInterfaces() {
    return subInterfaces;
  }

  @Override
  public Marker cloneIfNeeded() {
    return this;
  }

  @Override
  public Iterator<JDefinedClassOrInterface> iterator() {
    return Iterators.concat(subClasses.iterator(), subInterfaces.iterator());
  }

  public void addSubClassOrInterface(@Nonnull JDefinedClassOrInterface type) {
    if (type instanceof JDefinedClass) {
      addSubClass((JDefinedClass) type);
    } else {
      addSubInterface((JDefinedInterface) type);
    }
  }
}