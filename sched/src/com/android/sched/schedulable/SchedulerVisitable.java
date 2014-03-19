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

import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/**
 * Abstracts the idea that a class can be visited.
 *
 * @param <T> the type of the visitor
 */
public interface SchedulerVisitable<T> {

  /**
   * Must implement the visit of the instance of type T in each subclass. See design pattern
   * visitor.
   *
   * <pre>
   * public void visit(@Nonnull T visitor, @Nonnull TransformRequest tranformRequest)
   *        throws Exception {
   *   visitor.visit(this, transformRequest);
   * }
   * </pre>
   *
   * @throws Exception if any Exception is thrown during the visit
   */
  public void visit(@Nonnull T visitor, @Nonnull TransformRequest transformRequest)
      throws Exception;
}
