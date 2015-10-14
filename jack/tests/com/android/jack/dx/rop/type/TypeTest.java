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

package com.android.jack.dx.rop.type;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

public class TypeTest {

  @Test
  public void type() {
    Type t = Type.intern("La/b/c;");
    Assert.assertEquals("a/b/c", t.getClassName());
    Assert.assertEquals(1, t.getCategory());
    Assert.assertTrue(t.isCategory1());
    Assert.assertFalse(t.isCategory2());
    Assert.assertTrue(t.isReference());
    Assert.assertFalse(t.isArray());
    Assert.assertEquals(Type.BT_OBJECT, t.getBasicType());
    Assert.assertEquals("La/b/c;", t.getDescriptor());
    Assert.assertEquals("[La/b/c;", t.getArrayType().getDescriptor());
  }

  @Test
  public void arrayType() {
    Type t = Type.intern("[[La/b/c;");
    Assert.assertEquals("[[La/b/c;", t.getClassName());
    Assert.assertEquals("[La/b/c;", t.getComponentType().getClassName());
    Assert.assertEquals("[La/b/c;", t.getComponentType().getDescriptor());
    Assert.assertEquals("La/b/c;", t.getComponentType().getComponentType().getDescriptor());
    Assert.assertEquals("a/b/c", t.getComponentType().getComponentType().getClassName());
    Assert.assertEquals(1, t.getCategory());
    Assert.assertTrue(t.isCategory1());
    Assert.assertFalse(t.isCategory2());
    Assert.assertFalse(t.getComponentType().getComponentType().isCategory2());
    Assert.assertTrue(t.isReference());
    Assert.assertTrue(t.isArray());
    Assert.assertEquals(Type.BT_OBJECT, t.getBasicType());
  }

  @Test
  @Ignore
  public void closure() {
    Type t = Type.intern("\\a/b/c;");
    Assert.assertEquals("a/b/c", t.getClassName());
    Assert.assertEquals(2, t.getCategory());
    Assert.assertFalse(t.isCategory1());
    Assert.assertTrue(t.isCategory2());
    Assert.assertFalse(t.isReference());
    Assert.assertTrue(t.isClosure());
    Assert.assertFalse(t.isArray());
    Assert.assertEquals(Type.BT_CLOSURE, t.getBasicType());
    Assert.assertEquals("\\a/b/c;", t.getDescriptor());
    Assert.assertEquals("[\\a/b/c;", t.getArrayType().getDescriptor());
  }

  @Test
  @Ignore
  public void arrayClosure() {
    Type t = Type.intern("[[\\a/b/c;");
    Assert.assertEquals("[[\\a/b/c;", t.getClassName());
    Assert.assertEquals("[\\a/b/c;", t.getComponentType().getClassName());
    Assert.assertEquals("[\\a/b/c;", t.getComponentType().getDescriptor());
    Assert.assertEquals("\\a/b/c;", t.getComponentType().getComponentType().getDescriptor());
    Assert.assertEquals("a/b/c", t.getComponentType().getComponentType().getClassName());
    Assert.assertEquals(1, t.getCategory());
    Assert.assertTrue(t.isCategory1());
    Assert.assertFalse(t.isCategory2());
    Assert.assertTrue(t.getComponentType().getComponentType().isCategory2());
    Assert.assertTrue(t.getComponentType().getComponentType().isClosure());
    Assert.assertTrue(t.isReference());
    Assert.assertTrue(t.isArray());
    Assert.assertEquals(Type.BT_OBJECT, t.getBasicType());
  }
}
