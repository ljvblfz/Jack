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

package com.android.jack.backend.dex.rop;

import com.android.jack.Options;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.sched.util.RunnableHooks;
import com.android.sched.util.config.ThreadConfig;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.annotation.Nonnull;

public class RopRegisterManagerTest {

  @Nonnull
  private static RunnableHooks hooks;

  @BeforeClass
  public static void setUpClass() throws Exception {
    Options options = new Options();
    hooks = new RunnableHooks();
    options.checkValidity(hooks);
    options.getConfigBuilder(hooks).getCodecContext().setDebug();
    ThreadConfig.setConfig(options.getConfig());
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    hooks.runHooks();
    ThreadConfig.unsetConfig();
  }

  /**
   * Return reg must be created before getting it.
   */
  @Test
  public void ropRegManager001() {
    RopRegisterManager rrm = new RopRegisterManager(
        true /* emitDebugInfo */,
        false /* emitSyntheticDebugInfo */);
    try {
      rrm.getReturnReg(JPrimitiveTypeEnum.BOOLEAN.getType());
      Assert.fail();
    } catch (AssertionError e) {
      // Assertion must be triggered
    }
  }

  /**
   * Return reg must be created before getting it with the same type.
   */
  @Test
  public void ropRegManager002() {
    RopRegisterManager rrm = new RopRegisterManager(
        true /* emitDebugInfo */,
        false /* emitSyntheticDebugInfo */);
    rrm.createReturnReg(JPrimitiveTypeEnum.INT.getType());
    try {
      rrm.getReturnReg(JPrimitiveTypeEnum.BOOLEAN.getType());
      Assert.fail();
    } catch (AssertionError e) {
      // Assertion must be triggered
    }
  }

  /**
   * Return reg must be created before getting it with the same type.
   */
  @Test
  public void ropRegManager003() {
    RopRegisterManager rrm = new RopRegisterManager(
        true /* emitDebugInfo */,
        false /* emitSyntheticDebugInfo */);
    rrm.createReturnReg(JPrimitiveTypeEnum.INT.getType());
    rrm.getReturnReg(JPrimitiveTypeEnum.INT.getType());
  }
}
