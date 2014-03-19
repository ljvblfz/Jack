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

import com.android.sched.item.TagOrMarkerOrComponent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A constraint that annotates a {@link ProcessorSchedulable} and that needs be to be fulfilled
 * before the {code RunnableSchedulable} can be run.
 * <p>
 * {@link TagOrMarkerOrComponent}s can either be needed or fordidden by the
 * {@code RunnableSchedulable}.
 * <p>
 * It can also be used in an {@code add} element of a {@link ToSupport} annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Constraint {

  /**
   * {@link TagOrMarkerOrComponent}s that are needed by the {@link ProcessorSchedulable}.
   */
  public Class<? extends TagOrMarkerOrComponent>[] need() default {};

  /**
   * {@link TagOrMarkerOrComponent}s that are fordidden by the {@link ProcessorSchedulable}.
   */
  public Class<? extends TagOrMarkerOrComponent>[] no() default {};
}
