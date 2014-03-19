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

package com.android.jack.ir.ast;

/**
 * Distinguish 3 kinds of methods: {@link #STATIC}, {@link #INSTANCE_VIRTUAL} and
 * {@link #INSTANCE_NON_VIRTUAL}.
 */
public enum MethodKind {
  STATIC,
  /**
   * Non virtual instance. Private instance method and constructor are {@link #INSTANCE_NON_VIRTUAL}
   */
  INSTANCE_NON_VIRTUAL,
  INSTANCE_VIRTUAL;
}