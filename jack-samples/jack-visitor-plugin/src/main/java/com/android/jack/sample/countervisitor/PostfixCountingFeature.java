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

package com.android.jack.sample.countervisitor;

import com.android.sched.item.Description;
import com.android.sched.item.Feature;

/**
 * A feature that allows to count the number of postfix operations.
 * <p>
 * This feature is supported by the {@link PostfixCounter} schedulable. As any feature, it is
 * annotated with the {@link Description} annotation.
 */
@Description("Counter feature")
public interface PostfixCountingFeature extends Feature {}
