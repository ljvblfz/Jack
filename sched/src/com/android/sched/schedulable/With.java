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

import com.android.sched.item.TagOrMarker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates the {@code unprotect} element of a {@link Protect} annotation and specifies which
 * {@link TagOrMarker}s to add or remove.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface With {

  /**
   * The {@link TagOrMarker}s to add
   */
  public Class<? extends TagOrMarker>[] add() default {};

  /**
   * The {@link TagOrMarker}s to remove
   */
  public Class<? extends TagOrMarker>[] remove() default {};
}
