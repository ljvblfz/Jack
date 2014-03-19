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

package com.android.jack.java7.exceptions.test002.jack;

/**
 * Test multi-catch.
 */
public class ExceptionTest {

  public class Except0 extends Exception {
    private static final long serialVersionUID = 1L;

    public Except0(String message) {
      super(message);
    }
  }

  public class Except1 extends Except0 {
    private static final long serialVersionUID = 1L;

    public Except1() {
      super("Except1");
    }
  }

  public class Except2 extends Except0 {
    private static final long serialVersionUID = 1L;

    public Except2() {
      super("Except2");
    }
  }


  public class Except3 extends Except0 {
    private static final long serialVersionUID = 1L;

    public Except3() {
      super("Error");
    }
  }

  public String except001(int choose) {
    try {
      switch (choose) {
        case 1: {
          throw new Except1();
        }
        case 2: {
          throw new Except2();
        }
        case 3: {
          throw new Except3();
        }
        default: {
          return "No except";
        }
      }
    } catch (Except1 | Except2 e) {
      return e.getMessage();
    } catch (Except3 e) {
      return "Except3";
    }
  }
}
