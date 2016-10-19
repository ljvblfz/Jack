/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack;

import com.android.jack.annotation.AnnotationTests;
import com.android.jack.arithmetic.ArithmeticTests;
import com.android.jack.array.ArrayTests;
import com.android.jack.assertion.AssertionTests;
import com.android.jack.assign.AssignTests;
import com.android.jack.box.BoxTests;
import com.android.jack.bridge.BridgeTests;
import com.android.jack.cast.CastTests;
import com.android.jack.comparison.ComparisonTests;
import com.android.jack.conditional.ConditionalTests;
import com.android.jack.constant.ConstantTests;
import com.android.jack.dx.DxTests;
import com.android.jack.enums.EnumsTests;
import com.android.jack.external.ExternalTests;
import com.android.jack.fibonacci.FibonacciTests;
import com.android.jack.field.FieldTests;
import com.android.jack.flow.FlowTests;
import com.android.jack.ifstatement.IfstatementTests;
import com.android.jack.init.InitTests;
import com.android.jack.inner.InnerTests;
import com.android.jack.invoke.InvokeTests;
import com.android.jack.newarray.NewarrayTests;
import com.android.jack.opcodes.OpcodesTests;
import com.android.jack.optimizations.exprsimplifier.ExprsimplifierTests;
import com.android.jack.optimizations.ifwithconstantsimplifier.IfWithConstantSimplifierTests;
import com.android.jack.optimizations.notsimplifier.NotsimplifierTests;
import com.android.jack.optimizations.tailrecursion.TailRecursionTests;
import com.android.jack.optimizations.usedef.UseDefTests;
import com.android.jack.order.OrderTests;
import com.android.jack.reflect.ReflectTests;
import com.android.jack.returnstatement.ReturnstatementTests;
import com.android.jack.shrob.ShrobRuntimeTests;
import com.android.jack.string.StringTests;
import com.android.jack.switchstatement.SwitchstatementTests;
import com.android.jack.synchronize.SynchronizeTests;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.Runtime;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.threeaddress.ThreeaddressTests;
import com.android.jack.throwstatement.ThrowstatementTests;
import com.android.jack.trycatch.TrycatchTests;
import com.android.jack.tryfinally.TryfinallyTests;
import com.android.jack.unary.UnaryTests;
import com.android.jack.verify.VerifyTests;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class RegressionTests {

  public RuntimeTest[] tests = {
      new AnnotationTests(),
      new ArithmeticTests(),
      new ArrayTests(),
      new AssertionTests(),
      new AssignTests(),
      new BoxTests(),
      new BridgeTests(),
      new CastTests(),
      new ComparisonTests(),
      new ConditionalTests(),
      new ConstantTests(),
      new DxTests(),
      new EnumsTests(),
      new ExprsimplifierTests(),
      new ExternalTests(),
      new FibonacciTests(),
      new FieldTests(),
      new FlowTests(),
      new IfstatementTests(),
      new IfWithConstantSimplifierTests(),
      new InitTests(),
      new InnerTests(),
      new InvokeTests(),
      new NewarrayTests(),
      new NotsimplifierTests(),
      new OpcodesTests(),
      new OrderTests(),
      new ReflectTests(),
      new ReturnstatementTests(),
      new ShrobRuntimeTests(),
      new StringTests(),
      new SwitchstatementTests(),
      new SynchronizeTests(),
      new TailRecursionTests(),
      new ThreeaddressTests(),
      new ThrowstatementTests(),
      new TrycatchTests(),
      new TryfinallyTests(),
      new UnaryTests(),
      new UseDefTests(),
      new VerifyTests(),
  };



  @Test
  @Runtime
  public void runRegressionTests() throws Exception {
    List<RuntimeTestInfo> rtTestInfos = new ArrayList<RuntimeTestInfo>();

    for (RuntimeTest test : tests) {
      for (RuntimeTestInfo testInfos : test.getRuntimeTestInfos()) {
        rtTestInfos.add(testInfos);
      }
    }
    new RuntimeTestHelper(rtTestInfos.toArray(new RuntimeTestInfo[rtTestInfos.size()]))
        .compileAndRunTest();
  }

}
