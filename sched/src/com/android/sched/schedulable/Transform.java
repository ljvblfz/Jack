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
 * Annotates a {@link ProcessorSchedulable} and specifies what it does to <i>data</i>.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Transform {

  /**
   * {@link TagOrMarkerOrComponent}s that are added by the {@link ProcessorSchedulable}.
   */
  public Class<? extends TagOrMarkerOrComponent>[] add() default {};

  /**
   * {@link TagOrMarkerOrComponent}s that are removed by the {@link ProcessorSchedulable}.
   */
  public Class<? extends TagOrMarkerOrComponent>[] remove() default {};

  /**
   * {@link MarkerOrComponent}s that are modified by the {@link ProcessorSchedulable}.
   */
  public Class<? extends MarkerOrComponent>[] modify() default {};
}
