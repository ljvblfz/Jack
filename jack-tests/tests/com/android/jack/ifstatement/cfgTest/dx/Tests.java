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

package com.android.jack.ifstatement.cfgTest.dx;

import org.junit.Assert;
import org.junit.Test;

import com.android.jack.ifstatement.cfgTest.jack.IfCfg;

/**
 * Tests about if statement and cfg builder.
 */
public class Tests {

  @Test
  public void ifWithEndedThen() {
    Assert.assertEquals(1, IfCfg.ifWithEndedThen(true));
    Assert.assertEquals(2, IfCfg.ifWithEndedThen(false));
  }

  @Test
  public void ifWithEndedThenElse() {
    Assert.assertEquals(1, IfCfg.ifWithEndedThenElse(true));
    Assert.assertEquals(2, IfCfg.ifWithEndedThenElse(false));
  }

  @Test
  public void ifWithThen() {
    Assert.assertEquals(2, IfCfg.ifWithThen(true));
    Assert.assertEquals(0, IfCfg.ifWithThen(false));
  }

  @Test
  public void ifWithThenElse() {
    Assert.assertEquals(2, IfCfg.ifWithThenElse(true));
    Assert.assertEquals(3, IfCfg.ifWithThenElse(false));
  }

  @Test
  public void ifThenNested() {
    Assert.assertEquals(4, IfCfg.ifThenNested(1));
    Assert.assertEquals(2, IfCfg.ifThenNested(2));
  }

  @Test
  public void ifElseNested() {
    Assert.assertEquals(4, IfCfg.ifElseNested(1));
    Assert.assertEquals(4, IfCfg.ifElseNested(2));
    Assert.assertEquals(2, IfCfg.ifElseNested(3));
  }

  @Test
  public void ifEmptyThen() {
    Assert.assertEquals(2, IfCfg.ifEmptyThen(1));
    Assert.assertEquals(2, IfCfg.ifEmptyThen(2));
  }

  @Test
  public void ifEmptyElse() {
    Assert.assertEquals(4, IfCfg.ifEmptyElse(1));
    Assert.assertEquals(2, IfCfg.ifEmptyElse(2));
  }

  @Test
  public void ifThenElseNested() {
    Assert.assertEquals(1, IfCfg.ifThenElseNested(1));
    Assert.assertEquals(2, IfCfg.ifThenElseNested(2));
    Assert.assertEquals(3, IfCfg.ifThenElseNested(3));
  }
}
