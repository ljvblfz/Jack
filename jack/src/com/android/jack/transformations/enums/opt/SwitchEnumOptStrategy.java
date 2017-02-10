/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.jack.transformations.enums.opt;

import com.android.sched.util.codec.EnumName;
import com.android.sched.util.codec.VariableName;

/**
 * Types of switch enum optimization strategies.
 * 1. feedback (set on by default)
 * 2. always
 * 3. never
 */
@VariableName("strategy")
public enum SwitchEnumOptStrategy {
  // feedback-based optimization: this strategy will be enabled/disabled based on the
  // compile time information collected, e.g., if it is detected that an enum is only
  // used in one/few switch statements, it is useless to optimize it. Potentially enable
  // this strategy will cost more compilation time, but save more dex code
  @EnumName(name = "feedback")
  FEEDBACK(),
  // different from feedback-based optimization, always strategy doesn't collect compile-
  // time information to guide switch enum optimization. It will always enable switch enum
  // optimization no matter the enum is rarely/frequently used. Ideally this strategy will
  // compile code quicker than feedback-based strategy does, but the generated dex may be
  // larger than feedback strategy
  @EnumName(name = "always")
  ALWAYS(),
  // this actually is not real strategy, but we still need it because switch enum
  // optimization is disabled when incremental compilation is triggered
  @EnumName(name = "never")
  NEVER();
}