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

package com.android.jack.bridge.test009.jack;

import com.android.jack.bridge.test009.jack.sub.PublicExtendsPackage;


public class Caller {

  public void call() {
    PublicExtendsPackage obj = new PublicExtendsPackage() {
      @Override
      public void put(boolean key) {
      }
    };

    obj.put(1.0f);
    obj.put("", "toto");
    obj.put(2l);
    obj.put(true);
  }
}
