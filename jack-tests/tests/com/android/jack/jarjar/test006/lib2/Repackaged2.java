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

package com.android.jack.jarjar.test006.lib2;

import com.android.jack.jarjar.test006.lib1.Repackaged;
import com.android.jack.jarjar.test006.lib1.RepackagedAnnotation;
import com.android.jack.jarjar.test006.lib1.RepackagedInterface;

@RepackagedAnnotation
public class Repackaged2 implements RepackagedInterface {

  public int get() {
    return Repackaged.get();
  }

  @Override
  public int getInterface() {
    switch (Repackaged.getEnum()) {
      case A:
        return 2;
      case B:
        return 1;
      default:
        throw new AssertionError();
    }
  }

  public void checkInterface() {
    if (!RepackagedInterface.class.isAssignableFrom(getClass())) {
      throw new AssertionError();
    }
  }

  public void checkAnnotation() {
    if (getClass().getAnnotation(RepackagedAnnotation.class) == null) {
      throw new AssertionError();
    }
  }

}
