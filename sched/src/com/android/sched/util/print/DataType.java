/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.sched.util.print;

import java.util.ResourceBundle;

/**
 * Data type used by {@link DataView}.
 */
public enum DataType {
  /**
   * Dummy type
   */
  NOTHING,
  /**
   * Boolean type ({@link Boolean})
   */
  BOOLEAN,
  /**
   * Number type ({@link Number})
   */
  NUMBER,
  /**
   * Percent type ({@link Double})
   */
  PERCENT,
  /**
   * Quantity type ({@link Long})
   */
  QUANTITY,
  /**
   * Duration type ({@link Long} in nano-seconds)
   */
  DURATION,
  /**
   * String type ({@link Object}, use {@link Object#toString()})
   */
  STRING,
  /**
   * Key bundle type ({@link String}, use {@link ResourceBundle})
   */
  BUNDLE,
  /**
   * Structure type ({@link DataModel})
   */
  STRUCT,
  /**
   * List type ({@link DataModelList})
   */
  LIST
}
