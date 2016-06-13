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

package com.android.jack.frontend.test018.jack;

import java.util.List;

interface I<T> {
}

class U {

}

public class Test002 {
  private static List<I<? super U>> getPredicates(M... finders) {
    return null;
  }

  public static M allOf(final M... finders) {
    return new M(A.allOf(getPredicates(finders)));
  }
}
