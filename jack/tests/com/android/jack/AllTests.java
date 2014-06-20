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

package com.android.jack;

import com.android.jack.analysis.dfa.reachingdefs.ReachingDefsTest;
import com.android.jack.backend.dex.rop.RopRegisterManagerTest;
import com.android.jack.errorhandling.ErrorHandlingAllTests;
import com.android.jack.experimental.incremental.DependencyAllTests;
import com.android.jack.ir.ast.MarkerCollectorTest;
import com.android.jack.jayce.v0002.io.EscapeStringTest;
import com.android.jack.optimizations.ExpressionSimplifierTest;
import com.android.jack.optimizations.UselessVariableCopyTest;
import com.android.jack.transformations.ast.string.StringSplittingTest;
import com.android.jack.transformations.cast.UselessCastRemoverTest;
import com.android.jack.transformations.flow.CompileFlowTest;
import com.android.jack.util.FileUtilsTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test suite containing all tests (except for regression tests that must be run from the command
 * line).
 */
@RunWith(Suite.class)
@SuiteClasses(value = {
    AnnotationTest.class,
    ArithmeticTest.class,
    ArrayTest.class,
    AssertionTest.class,
    AssignmentTest.class,
    BoxTest.class,
    BridgeTest.class,
    CastTest.class,
    com.android.jack.cfg.AllTests.class,
    com.android.jack.compile.androidtree.AllTests.class,
    com.android.jack.frontend.AllTests.class,
    com.android.jack.gwt.AllTests.class,
    com.android.jack.java7.AllTest.class,
    com.android.jack.shrob.AllTests.class,
    com.android.jack.transformations.ast.AllTests.class,
    ComparisonTest.class,
    CompileFlowTest.class,
    CompileTimeTest.class,
    ConcatTest.class,
    ConditionalTest.class,
    ConstantTest.class,
    DependencyAllTests.class,
    DxTest.class,
    EnumTest.class,
    ErrorTest.class,
    ErrorHandlingAllTests.class,
    EscapeStringTest.class,
    ExpressionSimplifierTest.class,
    FibonacciThreeAddressTest.class,
    FieldAccessTest.class,
    FieldTest.class,
    FileConflictTest.class,
    FileUtilsTest.class,
    FinallyTest.class,
    FlowTest.class,
    GenericTest.class,
    IfTest.class,
    ImplicitCastTest.class,
    ImportTest.class,
    InnerTest.class,
    InvokesTest.class,
    JarjarTest.class,
    MainTest.class,
    MarkerCollectorTest.class,
    NoClasspathTest.class,
    NoPackageTest.class,
    OpcodesTest.class,
    OrderTest.class,
    ReachingDefsTest.class,
    ResourceTest.class,
    ReturnTest.class,
    RopRegisterManagerTest.class,
    StaticValuesTest.class,
    StringSplittingTest.class,
    SwitchesTest.class,
    SynchronizeTest.class,
    ThreeAddressTest.class,
    ThrowsTest.class,
    ToolchainTest.class,
    TryCatchTest.class,
    Types.class,
    UnaryTest.class,
    UselessCastRemoverTest.class,
    UselessVariableCopyTest.class,
    WithPhantomTest.class,
    ClasspathTest.class})
public class AllTests {
}
