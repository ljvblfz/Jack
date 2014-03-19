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

package com.android.jack.annotation.test006.jack;






import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReflectAnnotationsTest2 {

  public Object consInnerNamed;
  public List<String> genField;

  public ReflectAnnotationsTest2() {
    /* local, not anonymous, not member */
    class ConsInnerNamed {
        public void m() {
        }
    }
    consInnerNamed = new ConsInnerNamed();
  }

  public void foo() {
    /* anonymous, not local, not member */
    SuperInterface1 c = new SuperInterface1() {
      SuperClass f = new SuperClass();
    };
  }

  public Generic2<String> getGeneric() throws OutOfMemoryError, AssertionError{
    return new Generic2<String>();
  }

  public void genMeht(List<String> l){}

  abstract class Generic1<K, V> extends HashMap<K,V> implements Map<K,V> {}
  private class Generic2<K>  extends Generic1<K, String> {}

}
