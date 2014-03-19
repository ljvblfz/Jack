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

package com.android.jack.constant.clazz.dx;

import com.android.jack.constant.clazz.jack.Data;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Tests class.
 */
public class Tests {

  @Test
  public void getPrimitiveVoidClass() {
    Assert.assertTrue(Data.getPrimitiveVoidClass().getName().equals("void"));
  }

  @Test
  public void getPrimitiveByteClass() {
    Assert.assertTrue(Data.getPrimitiveByteClass().getName().equals("byte"));
  }

  @Test
  public void getPrimitiveBooleanClass() {
    Assert.assertTrue(Data.getPrimitiveBooleanClass().getName().equals("boolean"));
  }

  @Test
  public void getPrimitiveCharClass() {
    Assert.assertTrue(Data.getPrimitiveCharClass().getName().equals("char"));
  }

  @Test
  public void getPrimitiveShortClass() {
    Assert.assertTrue(Data.getPrimitiveShortClass().getName().equals("short"));
  }

  @Test
  public void getPrimitiveIntClass() {
    Assert.assertTrue(Data.getPrimitiveIntClass().getName().equals("int"));
  }

  @Test
  public void getPrimitiveFloatClass() {
    Assert.assertTrue(Data.getPrimitiveFloatClass().getName().equals("float"));
  }

  @Test
  public void getPrimitiveLongClass() {
    Assert.assertTrue(Data.getPrimitiveLongClass().getName().equals("long"));
  }

  @Test
  public void getPrimitiveDoubleClass() {
    Assert.assertTrue(Data.getPrimitiveDoubleClass().getName().equals("double"));
  }

  @Test
  public void getObjectVoidClass() {
    System.out.println(Data.getObjectVoidClass().getName());
    Assert.assertTrue(Data.getObjectVoidClass().getName().equals("java.lang.Void"));
  }
}
