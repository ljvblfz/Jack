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

package com.android.sched.item;

import com.android.sched.item.onlyfor.Internal;
import com.android.sched.item.onlyfor.OnlyFor;

/**
 * A property of the <i>data</i> at a certain point in the {@code Plan}. Can be added or
 * removed.
 */
@OnlyFor(Internal.class)
public interface Tag extends TagOrMarker, TagOrMarkerOrComponent {
}
