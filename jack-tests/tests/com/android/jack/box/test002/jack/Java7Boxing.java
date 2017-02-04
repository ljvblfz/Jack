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

package com.android.jack.box.test002.jack;

import org.junit.Assert;
import org.junit.Test;

public class Java7Boxing {

  @Test
  public void test() {
    Assert.assertEquals(1.3f, Java7Boxing.get(1.3f), 0.0001);
  }

  @SuppressWarnings("boxing")
  public static float get(float value) {
    Object f = Float.valueOf(value);
    return (float) f;
  }
}
