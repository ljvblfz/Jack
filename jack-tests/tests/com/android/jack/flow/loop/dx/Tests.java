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

package com.android.jack.flow.loop.dx;

import com.android.jack.flow.loop.jack.InfiniteLoop;
import com.android.jack.flow.loop.jack.LoopDoWhileBreak;
import com.android.jack.flow.loop.jack.LoopDoWhileContinue;
import com.android.jack.flow.loop.jack.LoopDoWhileLiteral;
import com.android.jack.flow.loop.jack.LoopForBreak;
import com.android.jack.flow.loop.jack.LoopForContinue;
import com.android.jack.flow.loop.jack.LoopWhileBreak;
import com.android.jack.flow.loop.jack.LoopWhileContinue;
import com.android.jack.flow.loop.jack.NoMoreStatementBug;

import junit.framework.Assert;

import org.junit.Test;

public class Tests {

  @Test
  public void loopForBreak() {
    LoopForBreak l = new LoopForBreak();
    Assert.assertEquals(5, l.loop());
  }
  @Test
  public void loopForBreakWithLabel() {
    LoopForBreak l = new LoopForBreak();
    Assert.assertEquals(5, l.loopWithLabel());
  }
  @Test
  public void loopForBreakWithUnusedLabel() {
    LoopForBreak l = new LoopForBreak();
    Assert.assertEquals(10, l.loopWithUnusedLabel());
  }
  @Test
  public void loopDoWhileBreak() {
    LoopDoWhileBreak l = new LoopDoWhileBreak();
    Assert.assertEquals(5, l.loop());
  }
  @Test
  public void loopDoWhileBreakWithLabel() {
    LoopDoWhileBreak l = new LoopDoWhileBreak();
    Assert.assertEquals(5, l.loopWithLabel());
  }
  @Test
  public void loopDoWhileFalse() {
    LoopDoWhileLiteral l = new LoopDoWhileLiteral();
    Assert.assertEquals(2, l.doWhileFalse());
  }
  @Test
  public void loopDoWhileTrue() {
    LoopDoWhileLiteral l = new LoopDoWhileLiteral();
    Assert.assertEquals(128, l.doWhileTrue());
  }
  @Test
  public void loopDoWhileBreakWithUnusedLabel() {
    LoopDoWhileBreak l = new LoopDoWhileBreak();
    Assert.assertEquals(10, l.loopWithUnusedLabel());
  }
  @Test
  public void loopWhileBreak() {
    LoopWhileBreak l = new LoopWhileBreak();
    Assert.assertEquals(5, l.loop());
  }
  @Test
  public void loopWhileBreakWithLabel() {
    LoopWhileBreak l = new LoopWhileBreak();
    Assert.assertEquals(5, l.loopWithLabel());
  }
  @Test
  public void loopWhileBreakWithUnusedLabel() {
    LoopWhileBreak l = new LoopWhileBreak();
    Assert.assertEquals(10, l.loopWithUnusedLabel());
  }
  @Test
  public void loopWhileContinue() {
    LoopWhileContinue l = new LoopWhileContinue();
    Assert.assertEquals(40, l.loop());
  }
  @Test
  public void loopWhileContinueWithLabel() {
    LoopWhileContinue l = new LoopWhileContinue();
    Assert.assertEquals(40, l.loopWithLabel());
  }
  @Test
  public void loopWhileContinueWithUnusedLabel() {
    LoopWhileContinue l = new LoopWhileContinue();
    Assert.assertEquals(45, l.loopWithUnusedLabel());
  }
  @Test
  public void loopForContinue() {
    LoopForContinue l = new LoopForContinue();
    Assert.assertEquals(40, l.loop());
  }
  @Test
  public void loopForContinueWithLabel() {
    LoopForContinue l = new LoopForContinue();
    Assert.assertEquals(40, l.loopWithLabel());
  }

  @Test
  public void loopForContinueWithUnusedLabel() {
    LoopForContinue l = new LoopForContinue();
    Assert.assertEquals(45, l.loopWithUnusedLabel());
  }
  @Test
  public void loopDoWhileContinue() {
    LoopDoWhileContinue l = new LoopDoWhileContinue();
    Assert.assertEquals(40, l.loop());
  }
  @Test
  public void loopDoWhileContinueWithLabel() {
    LoopDoWhileContinue l = new LoopDoWhileContinue();
    Assert.assertEquals(40, l.loopWithLabel());
  }
  @Test
  public void loopDoWhileContinueWithUnusedLabel() {
    LoopDoWhileContinue l = new LoopDoWhileContinue();
    Assert.assertEquals(45, l.loopWithUnusedLabel());
  }
  @Test
  public void noMoreStatementBug() {
    NoMoreStatementBug l = new NoMoreStatementBug();
    Assert.assertEquals(40, l.loop());
  }

  @Test
  public void infiniteLoop001() {
    Assert.assertEquals(5, InfiniteLoop.infiniteLoop001());
  }

  @Test
  public void infiniteLoop002() {
    Assert.assertEquals(5, InfiniteLoop.infiniteLoop002());
  }

  @Test
  public void infiniteLoop003() {
    Assert.assertEquals(5, InfiniteLoop.infiniteLoop003());
  }

  @Test
  public void infiniteLoop004() {
    Assert.assertEquals(5, InfiniteLoop.infiniteLoop004());
  }

  @Test
  public void infiniteLoop005() {
    Assert.assertEquals(5, InfiniteLoop.infiniteLoop005());
  }

  @Test
  public void infiniteLoop006() {
    Assert.assertEquals(5, InfiniteLoop.infiniteLoop006());
  }
}
