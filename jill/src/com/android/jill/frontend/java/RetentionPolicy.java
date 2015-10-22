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

package com.android.jill.frontend.java;



/**
 * Annotation retention policy
 */
public enum RetentionPolicy {
  /**
   *  @see RetentionPolicy#SOURCE
   */
  SOURCE,
  /**
   *  @see RetentionPolicy#CLASS
   */
  CLASS,
  /**
   *  @see RetentionPolicy#RUNTIME
   */
  RUNTIME,
  /**
   * Retention policy is unknown, it is useful for annotation literal where only root
   * annotation have a retention policy, others annotation literal have unknown retention.
   */
  UNKNOWN
}
