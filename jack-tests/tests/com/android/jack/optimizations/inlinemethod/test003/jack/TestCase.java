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

package com.android.jack.optimizations.inlinemethod.test003.jack;

import com.android.jack.annotations.ForceInline;

public class TestCase {

  public static class C1 {
    public int publicField = 0;

    public void fieldSetter(int x) {
      publicField = x + 1;
    }

    public int fieldGetter() {
      return publicField;
    }

    @ForceInline
    public int inlinedFieldGetter() {
      return publicField;
    }

    @ForceInline
    public int inlineMePrivateAccess(int x) {
      publicField = x - 1;
      return publicField;
    }

    @ForceInline
    public int inlineMeAccessors(int x) {
      fieldSetter(x);
      return fieldGetter();
    }
  }

  public static class S {
    public C1 c1 = new C1();

    public int callInlineMeDirectAccess(int x) {
      return c1.inlineMePrivateAccess(x);
    }

    public int callInlineMeAccessosr(int x) {
      return c1.inlineMeAccessors(x);
    }

    public int callInlineMeInlinedGetter(int x) {
      c1.inlineMeAccessors(x);
      return c1.inlinedFieldGetter();
    }
  }
}
