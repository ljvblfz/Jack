/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.transformations.lambda;

import com.android.sched.util.codec.EnumName;
import com.android.sched.util.codec.VariableName;

/** Defines how to group lambdas into lambda classes */
@VariableName("scope")
public enum LambdaGroupingScope {
  @EnumName(name = "none", description = "one lambda class for each lambda")
  NONE,
  @EnumName(name = "type",
          description = "one lambda class for all lambdas inside a top-level type")
  TYPE,
  @EnumName(name = "package", description = "one lambda class for all lambdas inside a package")
  PACKAGE;
}
