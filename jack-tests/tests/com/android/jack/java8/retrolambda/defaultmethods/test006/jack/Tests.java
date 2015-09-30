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
// Copyright © 2013-2015 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package com.android.jack.java8.retrolambda.defaultmethods.test006.jack;

import org.junit.Assert;
import org.junit.Test;

public class Tests {

  private interface SuperOriginal {
    default String foo() {
        return "original";
    }
}

private interface SuperOverridden extends SuperOriginal {
    @Override
    default String foo() {
        return "overridden";
    }
}

  private interface InheritsOriginal extends SuperOriginal {
  }

  private interface InheritsOverridden extends SuperOverridden {
  }

  private interface InheritsOverriddenAndOriginal extends SuperOverridden, InheritsOriginal {
  }

  private interface InheritsOriginalAndOverridden extends InheritsOriginal, SuperOverridden {
  }

  @Test
  public void inheriting_same_default_methods_through_many_parent_interfaces() {
    Assert.assertEquals(new InheritsOriginal() {
      }.foo(), "original");

    Assert.assertEquals(new InheritsOverridden() {
      }.foo(), "overridden");

    Assert.assertEquals(new InheritsOverriddenAndOriginal() {
      }.foo(), "overridden");

    Assert.assertEquals(new InheritsOriginalAndOverridden() {
      }.foo(), "overridden");
  }

}

