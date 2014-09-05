/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.ir.ast;

import com.android.jack.Options;
import com.android.jack.frontend.ParentSetter;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.load.NopClassOrInterfaceLoader;
import com.android.jack.scheduling.marker.collector.SubTreeMarkersCollector;
import com.android.sched.util.RunnableHooks;
import com.android.sched.util.config.ThreadConfig;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.annotation.Nonnull;

public class MarkerCollectorTest {

  @Nonnull
  private final JParameter param;

  public MarkerCollectorTest() {
    JPackage p = new JPackage("test", new JSession(), null);
    JDefinedClass classTest = new JDefinedClass(SourceInfo.UNKNOWN, "Test", JModifier.PUBLIC, p,
        NopClassOrInterfaceLoader.INSTANCE);
    JMethod method =
        new JMethod(SourceInfo.UNKNOWN, new JMethodId("test", MethodKind.STATIC), classTest,
            JPrimitiveTypeEnum.VOID.getType(), JModifier.PUBLIC | JModifier.STATIC);
    param =
        new JParameter(SourceInfo.UNKNOWN, "p", JPrimitiveTypeEnum.BOOLEAN.getType(), 0, method);
  }

  @BeforeClass
  public static void setUp() throws Exception {
    MarkerCollectorTest.class.getClassLoader().setDefaultAssertionStatus(true);
    Options options = new Options();
    RunnableHooks hooks = new RunnableHooks();
    options.checkValidity(hooks);
    options.getConfigBuilder(hooks).setDebug();
    ThreadConfig.setConfig(options.getConfig());
  }


  @Test
  public void markerCollector001() {
    // p = 1
    JBinaryOperation binOp1 = JBinaryOperation
        .create(SourceInfo.UNKNOWN, JBinaryOperator.ASG, new JParameterRef(
            SourceInfo.UNKNOWN, param), new JIntLiteral(SourceInfo.UNKNOWN, 1));
    binOp1.addMarker(new M1());

    // p + (p = 1)
    JParameterRef pref = new JParameterRef(SourceInfo.UNKNOWN, param);
    JBinaryOperation binOp2 =
        JBinaryOperation.create(SourceInfo.UNKNOWN, JBinaryOperator.ADD, pref, binOp1);

    // p = p + (p = 1)
    JBinaryOperation binOp =
        JBinaryOperation.create(SourceInfo.UNKNOWN, JBinaryOperator.ASG, new JParameterRef(
            SourceInfo.UNKNOWN, param), binOp2);

    ParentSetter ps = new ParentSetter();
    ps.accept(binOp);

    SubTreeMarkersCollector<M1> M1MarkersCollector =
        new SubTreeMarkersCollector<M1>(SubTreeM1Markers.class);

    Assert.assertEquals(1, binOp2.getSubTreeMarkers(M1MarkersCollector).size());
    Assert.assertEquals(1, binOp.getSubTreeMarkers(M1MarkersCollector).size());
    Assert.assertEquals(0, binOp1.getSubTreeMarkersOnPreviousSibling(M1MarkersCollector).size());
    Assert.assertEquals(0, binOp1.getSubTreeMarkersOnNextSibling(M1MarkersCollector).size());
    Assert.assertEquals(1, binOp1.getSubTreeMarkers(M1MarkersCollector).size());
    Assert.assertEquals(1, binOp.getSubTreeMarkers(M1MarkersCollector).size());
    Assert.assertEquals(0, binOp.getSubTreeMarkersOnPreviousSibling(M1MarkersCollector).size());
    Assert.assertEquals(0, binOp.getSubTreeMarkersOnNextSibling(M1MarkersCollector).size());
  }

  @Test
  public void markerCollector002() {
    // binOp1: p = 1
    JBinaryOperation binOp1 = JBinaryOperation
        .create(SourceInfo.UNKNOWN, JBinaryOperator.ASG, new JParameterRef(
            SourceInfo.UNKNOWN, param), new JIntLiteral(SourceInfo.UNKNOWN, 1));
    binOp1.addMarker(new M1());

    // binOp2: p + binOp1
    JParameterRef pref = new JParameterRef(SourceInfo.UNKNOWN, param);
    JBinaryOperation binOp2 =
        JBinaryOperation.create(SourceInfo.UNKNOWN, JBinaryOperator.ADD, pref, binOp1);

    // binOp3: p = 1
    JBinaryOperation binOp3 = JBinaryOperation
        .create(SourceInfo.UNKNOWN, JBinaryOperator.ASG, new JParameterRef(
            SourceInfo.UNKNOWN, param), new JIntLiteral(SourceInfo.UNKNOWN, 1));
    binOp3.addMarker(new M1());

    // binOp4: p + binOp3
    JParameterRef pref2 = new JParameterRef(SourceInfo.UNKNOWN, param);
    JBinaryOperation binOp4 =
        JBinaryOperation.create(SourceInfo.UNKNOWN, JBinaryOperator.ADD, pref2, binOp3);

    // p = binOp2 + binOp4
    JParameterRef pref3 = new JParameterRef(SourceInfo.UNKNOWN, param);
    JBinaryOperation binOp =
        JBinaryOperation.create(SourceInfo.UNKNOWN, JBinaryOperator.ASG, pref3,
            JBinaryOperation.create(SourceInfo.UNKNOWN, JBinaryOperator.ADD, binOp2, binOp4));

    ParentSetter ps = new ParentSetter();
    ps.accept(binOp);

    SubTreeMarkersCollector<M1> M1MarkersCollector =
        new SubTreeMarkersCollector<M1>(SubTreeM1Markers.class);

    Assert.assertEquals(0, pref.getSubTreeMarkersOnPreviousSibling(M1MarkersCollector).size());
    Assert.assertEquals(1, pref.getSubTreeMarkersOnNextSibling(M1MarkersCollector).size());
    Assert.assertEquals(1, binOp1.getSubTreeMarkers(M1MarkersCollector).size());
    Assert.assertEquals(1, binOp2.getSubTreeMarkers(M1MarkersCollector).size());
    Assert.assertEquals(1, binOp2.getSubTreeMarkersOnNextSibling(M1MarkersCollector).size());
    Assert.assertEquals(0, binOp2.getSubTreeMarkersOnPreviousSibling(M1MarkersCollector).size());
    Assert.assertEquals(2, binOp.getSubTreeMarkers(M1MarkersCollector).size());
    Assert.assertEquals(0, binOp.getSubTreeMarkersOnPreviousSibling(M1MarkersCollector).size());
    Assert.assertEquals(0, binOp.getSubTreeMarkersOnNextSibling(M1MarkersCollector).size());
  }

  @Test
  public void markerCollector003() {
    // binOp1: p = 1
    JBinaryOperation binOp1 = JBinaryOperation
        .create(SourceInfo.UNKNOWN, JBinaryOperator.ASG, new JParameterRef(
            SourceInfo.UNKNOWN, param), new JIntLiteral(SourceInfo.UNKNOWN, 1));
    binOp1.addMarker(new M1());

    // binOp2: p + binOp1
    JParameterRef pref = new JParameterRef(SourceInfo.UNKNOWN, param);
    JBinaryOperation binOp2 =
        JBinaryOperation.create(SourceInfo.UNKNOWN, JBinaryOperator.ADD, pref, binOp1);

    // binOp3: p = 1
    JBinaryOperation binOp3 = JBinaryOperation
        .create(SourceInfo.UNKNOWN, JBinaryOperator.ASG, new JParameterRef(
            SourceInfo.UNKNOWN, param), new JIntLiteral(SourceInfo.UNKNOWN, 1));
    binOp3.addMarker(new M1());

    // binOp4: p + binOp3
    JParameterRef pref2 = new JParameterRef(SourceInfo.UNKNOWN, param);
    JBinaryOperation binOp4 =
        JBinaryOperation.create(SourceInfo.UNKNOWN, JBinaryOperator.ADD, pref2, binOp3);
    binOp4.addMarker(new M2());

    // p = binOp2 + binOp4
    JParameterRef pref3 = new JParameterRef(SourceInfo.UNKNOWN, param);
    JBinaryOperation binOp =
        JBinaryOperation.create(SourceInfo.UNKNOWN, JBinaryOperator.ASG, pref3,
            JBinaryOperation.create(SourceInfo.UNKNOWN, JBinaryOperator.ADD, binOp2, binOp4));

    ParentSetter ps = new ParentSetter();
    ps.accept(binOp);

    SubTreeMarkersCollector<M1> M1MarkersCollector =
        new SubTreeMarkersCollector<M1>(SubTreeM1Markers.class);

    SubTreeMarkersCollector<M2> M2MarkersCollector =
        new SubTreeMarkersCollector<M2>(SubTreeM2Markers.class);

    Assert.assertEquals(2, binOp.getSubTreeMarkers(M1MarkersCollector).size());
    Assert.assertEquals(1, binOp.getSubTreeMarkers(M2MarkersCollector).size());
    Assert.assertEquals(0, binOp3.getSubTreeMarkers(M2MarkersCollector).size());
    Assert.assertEquals(0, binOp2.getSubTreeMarkers(M2MarkersCollector).size());
  }

  @Test
  public void markerCollector004() {
    // binOp1: p = 1
    JBinaryOperation binOp1 = JBinaryOperation
        .create(SourceInfo.UNKNOWN, JBinaryOperator.ASG, new JParameterRef(
            SourceInfo.UNKNOWN, param), new JIntLiteral(SourceInfo.UNKNOWN, 1));
    binOp1.addMarker(new M1());

    // binOp2: p + binOp1
    JParameterRef pref = new JParameterRef(SourceInfo.UNKNOWN, param);
    JBinaryOperation binOp2 =
        JBinaryOperation.create(SourceInfo.UNKNOWN, JBinaryOperator.ADD, pref, binOp1);

    // binOp3: p = 1
    JBinaryOperation binOp3 = JBinaryOperation
        .create(SourceInfo.UNKNOWN, JBinaryOperator.ASG, new JParameterRef(
            SourceInfo.UNKNOWN, param), new JIntLiteral(SourceInfo.UNKNOWN, 1));
    binOp3.addMarker(new M1());

    // binOp4: p + binOp3
    JParameterRef pref2 = new JParameterRef(SourceInfo.UNKNOWN, param);
    JBinaryOperation binOp4 =
        JBinaryOperation.create(SourceInfo.UNKNOWN, JBinaryOperator.ADD, pref2, binOp3);
    binOp4.addMarker(new M2());

    // p = binOp2 + binOp4
    JParameterRef pref3 = new JParameterRef(SourceInfo.UNKNOWN, param);
    JBinaryOperation binOp =
        JBinaryOperation.create(SourceInfo.UNKNOWN, JBinaryOperator.ASG, pref3,
            JBinaryOperation.create(SourceInfo.UNKNOWN, JBinaryOperator.ADD, binOp2, binOp4));

    ParentSetter ps = new ParentSetter();
    ps.accept(binOp);

    SubTreeMarkersCollector<M1> M1MarkersCollector =
        new SubTreeMarkersCollector<M1>(SubTreeM1Markers.class);

    SubTreeMarkersCollector<M2> M2MarkersCollector =
        new SubTreeMarkersCollector<M2>(SubTreeM2Markers.class);

    Assert.assertEquals(1, binOp4.getSubTreeMarkers(M1MarkersCollector).size());
    Assert.assertEquals(1, binOp4.getSubTreeMarkersOnPreviousSibling(M1MarkersCollector).size());
    Assert.assertEquals(0, binOp4.getSubTreeMarkersOnPreviousSibling(M2MarkersCollector).size());
  }

  @Test
  public void markerCollector005() {
    // binOp1: p == 1
    JBinaryOperation binOp1 = JBinaryOperation
        .create(SourceInfo.UNKNOWN, JBinaryOperator.EQ, new JParameterRef(
            SourceInfo.UNKNOWN, param), new JIntLiteral(SourceInfo.UNKNOWN, 1));
    binOp1.addMarker(new M1());

 // binOp2: p + 2
    JParameterRef pref = new JParameterRef(SourceInfo.UNKNOWN, param);
    JBinaryOperation binOp2 =
        JBinaryOperation.create(SourceInfo.UNKNOWN, JBinaryOperator.ADD, pref, new JIntLiteral(
            SourceInfo.UNKNOWN, 2));

    // binOp3: p = 1
    JBinaryOperation binOp3 = JBinaryOperation
        .create(SourceInfo.UNKNOWN, JBinaryOperator.ASG, new JParameterRef(
            SourceInfo.UNKNOWN, param), new JIntLiteral(SourceInfo.UNKNOWN, 1));
    binOp3.addMarker(new M1());

    // if (binOp1) { binOP2 } else { binOp3 }
    JIfStatement ifStmt =
        new JIfStatement(SourceInfo.UNKNOWN, binOp1, binOp2.makeStatement(),
            binOp3.makeStatement());

    ParentSetter ps = new ParentSetter();
    ps.accept(ifStmt);

    SubTreeMarkersCollector<M1> M1MarkersCollector =
        new SubTreeMarkersCollector<M1>(SubTreeM1Markers.class);

    Assert.assertEquals(1, ifStmt.getSubTreeMarkers(M1MarkersCollector).size());
  }
}
