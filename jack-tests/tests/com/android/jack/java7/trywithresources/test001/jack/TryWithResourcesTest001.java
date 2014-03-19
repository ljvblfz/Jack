/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.java7.trywithresources.test001.jack;

import com.android.jack.java7.trywithresources.test001.jack.autocloseable.AutoCloseable001;
import com.android.jack.java7.trywithresources.test001.jack.autocloseable.AutoCloseable002;

public class TryWithResourcesTest001 {

  public static boolean m() throws Exception {
    int result = 0;
    Observer o = new Observer();
    try (AutoCloseable001 ac1 = new AutoCloseable001(o)) {
    }
    return o.isClosed;
  }

  public static boolean m2() throws Exception {
    int result = 0;
    Observer o = new Observer();
    try (AutoCloseable002 ac2 = new AutoCloseable002()) {

    }
    return o.isClosed;
  }

}
