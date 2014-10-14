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
import com.android.jack.classpath.ClasspathTest;
import com.android.jack.enums.EnumTests;
import com.android.jack.error.AnnotationProcessorErrorTest;
import com.android.jack.error.CommandLineErrorTest;
import com.android.jack.error.FileAccessErrorTest;
import com.android.jack.experimenal.incremental.DependenciesTests005;
import com.android.jack.jarjar.JarjarTests;
import com.android.jack.shrob.ObfuscationWithoutMappingTests;
import com.android.jack.tools.merger.MergerAllTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test suite containing all tests (except for regression tests that must be run from the command
 * line).
 */
@RunWith(Suite.class)
@SuiteClasses(value = {
    AnnotationTests.class,
    ArithmeticTests.class,
    ClasspathTest.class,
    DependenciesTests005.class,
    EnumTests.class,
    JarjarTests.class,
    MergerAllTests.class,
    ObfuscationWithoutMappingTests.class,
    AnnotationProcessorErrorTest.class,
    FileAccessErrorTest.class,
    CommandLineErrorTest.class
  })
public class AllTests {
}
