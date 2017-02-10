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

import com.android.jack.backend.dex.rop.RopRegisterManagerTest;
import com.android.jack.ir.ast.JDefinedInterfaceTest;
import com.android.jack.ir.ast.MarkerCollectorTest;
import com.android.jack.ir.impl.ReferenceMapperTest;
import com.android.jack.jayce.v0004.io.EscapeStringTest;
import com.android.jack.optimizations.ExpressionSimplifierTest;
import com.android.jack.optimizations.tailrecursion.TailRecursionTest;
import com.android.jack.preprocessor.PreProcessorTest;
import com.android.jack.reporting.ProblemLevelTest;
import com.android.jack.reporting.ReporterFormatTest;
import com.android.jack.shrob.obfuscation.nameprovider.DictionaryNameProviderTest;
import com.android.jack.tracer.TracingTest;
import com.android.jack.transformations.ast.string.StringSplittingTest;
import com.android.jack.transformations.cast.UselessCastRemoverTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test suite containing all tests (except for regression tests that must be run from the command
 * line).
 */
@RunWith(Suite.class)
@SuiteClasses(value = {
    VersionTest.class,
    com.android.jack.cfg.AllTests.class,
    com.android.jack.frontend.AllTests.class,
    com.android.jack.gwt.AllTests.class,
    com.android.jack.transformations.ast.AllTests.class,
    com.android.jack.util.AllTests.class,
    ConditionalTest.class,
    DictionaryNameProviderTest.class,
    EscapeStringTest.class,
    ExpressionSimplifierTest.class,
    FibonacciThreeAddressTest.class,
    FinallyTest.class,
    JarjarTest.class,
    JDefinedInterfaceTest.class,
    MainTest.class,
    MarkerCollectorTest.class,
    /* MultiDexOverflowTests.class, */
    /* MultiDexTests.class, */
    PreProcessorTest.class,
    ProblemLevelTest.class,
//    ReachingDefsTest.class,
    ReferenceMapperTest.class,
    ReporterFormatTest.class,
    RopRegisterManagerTest.class,
    StaticValuesTest.class,
    StringSplittingTest.class,
    Types.class,
    UnaryTest.class,
    UselessCastRemoverTest.class,
    TracingTest.class,
    TailRecursionTest.class})
public class AllUnitTests {
}
