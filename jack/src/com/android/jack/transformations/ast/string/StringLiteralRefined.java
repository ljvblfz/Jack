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

package com.android.jack.transformations.ast.string;

import com.android.sched.item.ComposedOf;
import com.android.sched.item.Description;
import com.android.sched.item.Tag;

/**
 * A tag that indicates that all string literals corresponding to a type have been
 * refined.
 */
@Description("Indicates that all string literals corresponding to a type have been"
    + " refined")
@ComposedOf(value = {StringLiteralRefined.Field.class, StringLiteralRefined.Method.class,
    StringLiteralRefined.Type.class})
public class StringLiteralRefined implements Tag {
  /**
   * A tag that indicates that all string literals in fields corresponding to a type
   * have been refined.
   */
  @Description(
      "Indicates that all string literals in fields corresponding to a type have "
      + "been refined")
  public static class Field implements Tag {

  }

  /**
   * A tag that indicates that all string literals in methods corresponding to a type
   * have been refined.
   */
  @Description(
      "Indicates that all string literals in methods corresponding to a type have " +
      "been refined")
  public static class Method implements Tag {

  }

  /**
   * A tag that indicates that all string literals in types corresponding to a type
   * have been refined.
   */
  @Description(
      "Indicates that all string literals in types corresponding to a type have " +
      "been refined")
  public static class Type implements Tag {

  }
}
