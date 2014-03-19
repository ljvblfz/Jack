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

package com.android.jack.java7.trywithresources.test002.jack;

import com.android.jack.java7.trywithresources.test002.jack.autocloseable.AutoCloseable001;
import com.android.jack.java7.trywithresources.test002.jack.autocloseable.AutoCloseable002;
import com.android.jack.java7.trywithresources.test002.jack.autocloseable.AutoCloseable002b;
import com.android.jack.java7.trywithresources.test002.jack.autocloseable.AutoCloseable003;
import com.android.jack.java7.trywithresources.test002.jack.exception.E1;
import com.android.jack.java7.trywithresources.test002.jack.exception.E2;
import com.android.jack.java7.trywithresources.test002.jack.exception.E3;

public class TryWithResourcesTest002 {

  public static boolean m() {
    boolean result = false;
    Observer o = new Observer();
    try {
      try (AutoCloseable002 ac2 = new AutoCloseable002(o)) {
      }
    } catch (Exception e) {
      result = !o.isClosed
          && e instanceof E2
          && e.getSuppressed().length == 0;
    }
    return result;
  }

  public static boolean m2() {
    boolean result = false;
    Observer o = new Observer();
    try {
      try (AutoCloseable001 ac1 = new AutoCloseable001(o)) {
        throw new E1();
      }
    } catch (Exception e) {
      result = o.isClosed
          && e instanceof E1
          && e.getSuppressed().length == 0;
    }
    return result;
  }

  public static boolean m3() {
    boolean result = false;
    Observer o = new Observer();
    try {
      try (AutoCloseable002 ac2 = new AutoCloseable002(o)) {
        throw new E1();
      }
    } catch (Exception e) {
      result = !o.isClosed
          && e instanceof E1
          && e.getSuppressed().length == 1
          && e.getSuppressed()[0] instanceof E2;
    }
    return result;
  }

  public static boolean m4() {
    boolean result = false;
    Observer o1 = new Observer();
    Observer o2 = new Observer();
    try {
      try (
          AutoCloseable001 ac1 = new AutoCloseable001(o1);
          AutoCloseable002 ac2 = new AutoCloseable002(o2);
          ) {
        throw new E1();
      }
    } catch (Exception e) {
      result = o1.isClosed
          && !o2.isClosed
          && e instanceof E1
          && e.getSuppressed().length == 1
          && e.getSuppressed()[0] instanceof E2;
    }
    return result;
  }

  public static boolean m5() {
    boolean result = false;
    Observer o = new Observer();
    try {
      try (AutoCloseable001 ac1 = new AutoCloseable001(o)) {
        throw new E1();
      } catch (E1 e) {
        result = o.isClosed
            && e.getSuppressed().length == 0;
      }
    } catch (Exception e) {
      result = false;
    }
    return result;
  }

  public static boolean m6() {
    boolean result = false;
    Observer o = new Observer();
    try {
      try (AutoCloseable002 ac2 = new AutoCloseable002(o)) {

      } catch (E1 e) {
        result = false;
      }
    } catch (Exception e) {
      result = !o.isClosed
          && e instanceof E2
          && e.getSuppressed().length == 0;
    }
    return result;
  }

  public static boolean m7() {
    boolean result = false;
    Observer o = new Observer();
    try {
      try (AutoCloseable003 ac3 = new AutoCloseable003(o)) {
        throw new E1();
      } catch (E1 e) {
        result = false;
      }
    } catch (Exception e) {
      result = !o.isClosed
          && e instanceof E3
          && e.getSuppressed().length == 0;
    }
    return result;
  }


  public static boolean m8() {
    boolean result = false;
    Observer o = new Observer();
    try {
      try (AutoCloseable003 ac3 = new AutoCloseable003(o)) {
        throw new E1();
      } catch (E1 e) {
        result = false;
      }
    } catch (Exception e) {
      result = !o.isClosed
          && e instanceof E3
          && e.getSuppressed().length == 0;
    }
    return result;
  }

  public static boolean m9() {
    boolean result = false;
    Observer o1 = new Observer();
    Observer o2 = new Observer();
    try (AutoCloseable001 ac1 = new AutoCloseable001(o1)) {
      try {
        try (AutoCloseable003 ac3 = new AutoCloseable003(o2)) {
          throw new E1();
        } catch (E1 e) {
          result = false;
        }
      } catch (Exception e) {
        result = !o2.isClosed
            && e instanceof E3
            && e.getSuppressed().length == 0;
      }
    } catch (Exception e) {
      result = false;
    }
    result &= o1.isClosed;
    return result;
  }

  public static boolean m10() {
    boolean result = false;
    Observer o1 = new Observer();
    Observer o2 = new Observer();
    try {
      try (
          AutoCloseable001 ac1 = new AutoCloseable001(o1);
          AutoCloseable003 ac3 = new AutoCloseable003(o2);
          ) {
        throw new E1();
      }
    } catch (Exception e) {
      result = o1.isClosed
          && !o2.isClosed
          && e instanceof E3
          && e.getSuppressed().length == 0;
    }
    return result;
  }

  public static boolean m11() {
    boolean result = false;
    Observer o1 = new Observer();
    Observer o2 = new Observer();
    try {
      try (
          AutoCloseable002 ac2 = new AutoCloseable002(o1);
          AutoCloseable003 ac3 = new AutoCloseable003(o2);
          ) {
        throw new E1();
      }
    } catch (Exception e) {
      result = !o1.isClosed
          && !o2.isClosed
          && e instanceof E3
          && e.getSuppressed().length == 1
          && e.getSuppressed()[0] instanceof E2;
    }
    return result;
  }

  public static boolean m12() {
    boolean result = false;
    Observer o1 = new Observer();
    Observer o2 = new Observer();
    try {
      try (
          AutoCloseable002 ac2 = new AutoCloseable002(o1);
          AutoCloseable002 ac3 = new AutoCloseable002(o2);
          ) {
        throw new E1();
      }
    } catch (Exception e) {
      result = !o1.isClosed
          && !o2.isClosed
          && e instanceof E1
          && e.getSuppressed().length == 2
          && e.getSuppressed()[0] instanceof E2
          && e.getSuppressed()[1] instanceof E2;
    }
    return result;
  }

  public static boolean m13() {
    boolean result = false;
    Observer o1 = new Observer();
    Observer o2 = new Observer();
    Observer o3 = new Observer();
    try {
      try (
          AutoCloseable001 ac1 = new AutoCloseable001(o1);
          AutoCloseable002 ac2 = new AutoCloseable002(o2);
          AutoCloseable002b ac2b = new AutoCloseable002b(o3);
          ) {
        // No exception
      }
    } catch (Exception e) {
      result = o1.isClosed
          && !o2.isClosed
          && !o3.isClosed
          && e instanceof E3
          && e.getSuppressed().length == 1
          && e.getSuppressed()[0] instanceof E2;
    }
    return result;
  }

}
