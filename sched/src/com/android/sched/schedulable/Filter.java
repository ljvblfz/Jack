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

import com.android.sched.item.Component;
import com.android.sched.item.Feature;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a {@link RunnableSchedulable} and specifies which {@link Filter} the
 * {@code RunnableSchedulable} needs.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Filter {
  /**
   * The {@link ComponentFilter}s the {@link RunnableSchedulable} need.
   */
  public Class<? extends ComponentFilter<? extends Component>>[] value();
  /**
   * All {@link Feature} the filter need to be taken into account.
   */
  public Class<? extends Feature>[] ifAll() default {};
  /**
   * {@link Feature}s that disable the filter if one is present.
   */
  public Class<? extends Feature>[] unlessOne() default {};
}
