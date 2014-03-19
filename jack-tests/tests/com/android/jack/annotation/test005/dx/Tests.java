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

package com.android.jack.annotation.test005.dx;

import com.android.jack.annotation.test005.jack.PackageAnnotation;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;


public class Tests {

  @Test
  public void refTest001() {
    checkPackage(com.android.jack.annotation.test005.dx.sub.Dummy.class.getPackage());
    checkPackage("com.android.jack.annotation005.dx.sub");
  }

  @Test
  @Ignore("An empty package is not \"observable\"")
  public void refTest002() {
    checkPackage("com.android.jack.annotation005.dx.empty");
  }

  @Test
  public void test001() {
    checkPackage(com.android.jack.annotation.test005.jack.sub.Dummy.class.getPackage());
    checkPackage("com.android.jack.annotation005.jack.sub");
  }

  @Test
  @Ignore("An empty package is not \"observable\"")
  public void test002() {
    checkPackage("com.android.jack.annotation005.jack.empty");
  }

  void checkPackage(String packageName) {
    Package empty = Package.getPackage(packageName);
    checkPackage(empty);
  }

  void checkPackage(Package empty) {
    PackageAnnotation annotation = empty.getAnnotation(PackageAnnotation.class);
    Assert.assertNotNull(annotation);
  }
}
