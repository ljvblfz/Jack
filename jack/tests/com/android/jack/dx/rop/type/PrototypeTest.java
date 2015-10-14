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

import org.junit.Test;

public class PrototypeTest {

  @Test
  public void prototype001() {
    Prototype proto = Prototype.intern("(\\a/b/c;I)V");
    Assert.assertEquals("(\\a/b/c;I)V", proto.getDescriptor());
    Assert.assertEquals(2, proto.getParameterTypes().size());
    Assert.assertEquals(2, proto.getParameterTypes().get(0).getCategory());
    Assert.assertTrue(proto.getParameterTypes().get(1).getFrameType().isPrimitive());
    Assert.assertTrue(proto.getParameterTypes().get(0).getFrameType().isClosure());
  }

  @Test
  public void prototype002() {
    Prototype proto = Prototype.intern("(\\a/b/c;Lc/d/e;)V");
    Assert.assertEquals("(\\a/b/c;Lc/d/e;)V", proto.getDescriptor());
    Assert.assertEquals(2, proto.getParameterTypes().size());
    Assert.assertEquals(2, proto.getParameterTypes().get(0).getCategory());
    Assert.assertTrue(proto.getParameterTypes().get(0).getFrameType().isClosure());
    Assert.assertTrue(proto.getParameterTypes().get(1).getFrameType().isReference());
  }

  @Test
  public void prototype003() {
    Prototype proto = Prototype.intern("(Lc/d/e;)\\a/b/c;");
    Assert.assertEquals("(Lc/d/e;)\\a/b/c;", proto.getDescriptor());
    Assert.assertEquals(1, proto.getParameterTypes().size());
    Assert.assertEquals(1, proto.getParameterTypes().get(0).getCategory());
    Assert.assertEquals(2, proto.getReturnType().getCategory());
    Assert.assertTrue(proto.getReturnType().getFrameType().isClosure());
  }
}
