/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.reflect.test001.jack;

public class Reflect001 {

  private class $1I{}

  private interface I{}

  private class $A$B$C {}

  private class C{}

  private interface $1{}

  private interface ${}

  public String getSimpleNameInner1() {
    return C.class.getSimpleName();
  }

  public String getSimpleNameInner2() {
    return new C().getClass().getSimpleName();
  }

  public String getSimpleNameInner3() {
    return $A$B$C.class.getSimpleName();
  }

  public String getSimpleNameInner4() {
    return new $A$B$C().getClass().getSimpleName();
  }

  public String getSimpleNameInner5() {
    return $1I.class.getSimpleName();
  }

  public String getSimpleNameInner6() {
    return new $1I().getClass().getSimpleName();
  }

  public String getSimpleNameInner7() {
    return $1.class.getSimpleName();
  }

  public String getSimpleNameInner8() {
    return $.class.getSimpleName();
  }

  public String getSimpleNameAnonymous1() {
    return new $(){}.getClass().getSimpleName();
  }

  public String getSimpleNameAnonymous2() {
    return new $1(){}.getClass().getSimpleName();
  }

  public boolean isAnonymousInner1() {
    return C.class.isAnonymousClass();
  }

  public boolean isAnonymousInner2() {
    return new C().getClass().isAnonymousClass();
  }

  public boolean isAnonymousInner3() {
    return $A$B$C.class.isAnonymousClass();
  }

  public boolean isAnonymousInner4() {
    return new $A$B$C().getClass().isAnonymousClass();
  }

  public boolean isAnonymousInner5() {
    return new I(){}.getClass().isAnonymousClass();
  }

  public boolean isAnonymousInner6() {
    return new $1(){}.getClass().isAnonymousClass();
  }

  public boolean isAnonymousInner7() {
    return new $(){}.getClass().isAnonymousClass();
  }

  public String getSimpleNameLocal1() {
    class $1I{}

    return $1I.class.getSimpleName();
  }

  public String getSimpleNameLocal2() {
    class $1I{}

    $1I i = new $1I(){};
    return i.getClass().getSimpleName();
  }

  public String getSimpleNameLocal3() {
    class I{}

    return I.class.getSimpleName();
  }

  public String getSimpleNameLocal4() {
    class I{}

    I i = new I(){};
    return i.getClass().getSimpleName();
  }

  public boolean isAnonymousLocal1() {
    class $1I{}

    return $1I.class.isAnonymousClass();
  }

  public boolean isAnonymousLocal2() {
    class $1I{}

    $1I i = new $1I(){};
    return i.getClass().isAnonymousClass();
  }

  public boolean isAnonymousLocal3() {
    class I{}

    return I.class.isAnonymousClass();
  }

  public boolean isAnonymousLocal4() {
    class I{}

    I i = new I(){};
    return i.getClass().isAnonymousClass();
  }
}
