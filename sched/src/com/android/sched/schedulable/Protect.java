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

package com.android.sched.schedulable;

import com.android.sched.item.MarkerOrComponent;
import com.android.sched.item.TagOrMarkerOrComponent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows to protect the <i>data</i> against specific transformations after the
 * {@link ProcessorSchedulable} it annotates is run.
 * <p>If the protection cannot be maintained due to the scheduling, the transformations of the
 * {@link With} annotations of the {@code unprotect} element are applied and the {@code Feature}s of
 * this {@link ProcessorSchedulable} are considered not supported.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Protect {

  /**
   * {@link TagOrMarkerOrComponent}s that won't be able to be added after this
   * {@link ProcessorSchedulable} is run.
   */
  public Class<? extends TagOrMarkerOrComponent>[] add() default {};

  /**
   * {@link TagOrMarkerOrComponent}s that won't be able to be removed after this
   * {@link ProcessorSchedulable} is run.
   */
  public Class<? extends TagOrMarkerOrComponent>[] remove() default {};

  /**
   * {@link MarkerOrComponent}s that won't be able to be modified after this
   * {@link ProcessorSchedulable} is run.
   */
  public Class<? extends MarkerOrComponent>[] modify() default {};

  /**
   * {@link With} annotations that specify what transformations to do before this
   * {@link ProcessorSchedulable} is run in case the protection cannot be maintained.
   */
  public With[] unprotect() default {};
}
