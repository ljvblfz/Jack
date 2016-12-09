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

package com.android.jack.frontend.generic.basic.jack;

import java.util.Iterator;

public class Generic<T> {

  private Iterator<T> values;


  public Generic(T arg) {
  }

  public Generic<T> getT(T arg) {
    return new Generic<T>(arg);
  }

  public Generic<Object> getObject(Object arg) {
    return new Generic<Object>(arg);
  }

  public Generic<T> next() {
    return new Generic<T>(values.next());
  }

}
