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

import com.android.jack.dx.dex.file.DexFile;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JSession;
import com.android.sched.item.ComposedOf;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.Tag;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * A marker which contains a {@code DexFile} instances.
 */
@Description("A marker which contains a DexFile instances.")
@ValidOn(JSession.class)
public final class DexFileMarker implements Marker {

  @Nonnull
  private final Map<JDefinedClassOrInterface, DexFile> dexFilePerType =
      new HashMap<JDefinedClassOrInterface, DexFile>();

  /**
   * This tag means that the {@code DexFile} contained into the {@Code DexFileMarker} is complete
   * and could be used or dumped into a file.
   */
  @Description("The DexFile contained into the DexFileMarker is complete.")
  @ComposedOf({DexFileMarker.class, Field.class, Method.class, ClassAnnotation.class,
    FieldAnnotation.class, MethodAnnotation.class})
  @Name("DexFileMarker.Complete")
  public static final class Complete implements Tag {
  }

  /**
   * This tag means that the {@code DexFile} contained into the {@Code DexFileMarker} contains
   * fields.
   */
  @Description("The DexFile contained into the DexFileMarker has fields.")
  @Name("DexFileMarker.Field")
  public static final class Field implements Tag {
  }

  /**
   * This tag means that the {@code DexFile}s contained into the {@Code DexFileMarker} contains
   * methods.
   */
  @Description("The DexFile contained into the DexFileMarker has methods.")
  @Name("DexFileMarker.Method")
  public static final class Method implements Tag {
  }

  /**
   * This tag means that the {@code DexFile}s contained into the {@Code DexFileMarker} contains
   * annotations on methods.
   */
  @Description("The DexFile contained into the DexFileMarker has annotations on methods.")
  @Name("DexFileMarker.MethodAnnotation")
  public static final class MethodAnnotation implements Tag {
  }

  /**
   * This tag means that the {@code DexFile}s contained into the {@Code DexFileMarker} contains
   * annotations on fields.
   */
  @Description("The DexFile contained into the DexFileMarker has annotations on fields.")
  @Name("DexFileMarker.FieldAnnotation")
  public static final class FieldAnnotation implements Tag {
  }

  /**
   * This tag means that the {@code DexFile}s contained into the {@Code DexFileMarker} contains
   * annotations on classes.
   */
  @Description("The DexFile contained into the DexFileMarker has annotations on class.")
  @Name("DexFileMarker.ClassAnnotation")
  public static final class ClassAnnotation implements Tag {
  }

  public DexFileMarker() {
  }

  public void addDexFilePerType(@Nonnull JDefinedClassOrInterface type, @Nonnull DexFile dexFile) {
    dexFilePerType.put(type, dexFile);
  }

  @Nonnull
  public DexFile getDexFileOfType(@Nonnull JDefinedClassOrInterface type) {
    DexFile dexFile = dexFilePerType.get(type);
    assert dexFile != null;
    return dexFile;
  }

  @Override
  @Nonnull
  public Marker cloneIfNeeded() {
    return this;
  }
}
