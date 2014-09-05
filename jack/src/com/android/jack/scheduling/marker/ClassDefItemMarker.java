/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.scheduling.marker;

import com.android.jack.dx.dex.file.ClassDefItem;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.sched.item.ComposedOf;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.Tag;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import javax.annotation.Nonnull;

/**
 * A marker which contains a {@link ClassDefItem} instance.
 */
@Description("A marker which contains a ClassDefItem instance.")
@ValidOn(JDefinedClassOrInterface.class)
public final class ClassDefItemMarker implements Marker {
  /**
   * This tag means that the {@link ClassDefItem} contained in the {@link ClassDefItemMarker} is
   * complete and could be used or dumped into a file.
   */
  @Description("The ClassDefItem contained in the ClassDefItemMarker is complete.")
  @ComposedOf({ClassDefItemMarker.class,
      ClassDefItemMarker.Field.class,
      ClassDefItemMarker.Method.class,
      ClassDefItemMarker.ClassAnnotation.class,
      ClassDefItemMarker.FieldAnnotation.class,
      ClassDefItemMarker.MethodAnnotation.class})
  @Name("ClassDefItemMarker.Complete")
  public static final class Complete implements Tag {
  }

  /**
   * This tag means that the {@link ClassDefItem} contained in the {@link ClassDefItemMarker}
   * contains fields.
   */
  @Description("The ClassDefItem contained in the ClassDefItemMarker has fields.")
  @Name("ClassDefItemMarker.Field")
  public static final class Field implements Tag {
  }

  /**
   * This tag means that the {@link ClassDefItem} contained in the {@link ClassDefItemMarker}
   * contains methods.
   */
  @Description("The ClassDefItem contained in the ClassDefItemMarker has methods.")
  @Name("ClassDefItemMarker.Method")
  public static final class Method implements Tag {
  }

  /**
   * This tag means that the {@link ClassDefItem} contained in the {@link ClassDefItemMarker}
   * contains annotations on methods.
   */
  @Description("The ClassDefItem contained in the ClassDefItemMarker has annotations on methods.")
  @Name("ClassDefItemMarker.MethodAnnotation")
  public static final class MethodAnnotation implements Tag {
  }

  /**
   * This tag means that the {@link ClassDefItem} contained in the {@link ClassDefItemMarker}
   * contains annotations on fields.
   */
  @Description("The ClassDefItem contained in the ClassDefItemMarker has annotations on fields.")
  @Name("ClassDefItemMarker.FieldAnnotation")
  public static final class FieldAnnotation implements Tag {
  }

  /**
   * This tag means that the {@link ClassDefItem} contained in the {@link ClassDefItemMarker}
   * contains annotations on classes.
   */
  @Description("The ClassDefItem contained in the ClassDefItemMarker has annotations on class.")
  @Name("ClassDefItemMarker.ClassAnnotation")
  public static final class ClassAnnotation implements Tag {
  }

  @Nonnull
  private final ClassDefItem classDefItem;

  public ClassDefItemMarker(@Nonnull ClassDefItem classDefItem) {
    this.classDefItem = classDefItem;
  }

  @Nonnull
  public ClassDefItem getClassDefItem() {
    return classDefItem;
  }

  @Override
  @Nonnull
  public Marker cloneIfNeeded() {
    return this;
  }

}
