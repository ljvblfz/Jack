/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.jack.java8.gwt.test009.jack;

import org.junit.Test;


import junit.framework.Assert;

public class Java8Test {


  interface Lambda2<String> {
    boolean run(String a, String b);
  }

  interface Lambda3<String> {
    boolean run(String a);
  }


  class AcceptsLambda<T> {
    public boolean accept2(Lambda2<String> foo) {
      return foo.run("a", "b");
    }
    public boolean accept3(Lambda3<String> foo) {
      return foo.run("hello");
    }
  }

  @Test
  public void testImplicitQualifierReferenceBinding() throws Exception {
    Assert.assertFalse(new AcceptsLambda<String>().accept2(String::equalsIgnoreCase));
    Assert.assertTrue(new AcceptsLambda<String>().accept3("hello world"::contains));
  }
}
