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

package com.android.jack.shrob.test008.jack;

public class A {
  void m() {

  }

  void keep() {
    try  {
      m();
    } catch(MyException me) {

    }
  }

  void keep2() {
    try  {
      m();
    } catch(MyException me) {
      System.out.println(me);
    }
  }

  void keep3() {
    try  {
      m();
    } catch(MyException me) {
      me.printStackTrace();
    }
  }

  void keep4() throws MyException {

  }
}