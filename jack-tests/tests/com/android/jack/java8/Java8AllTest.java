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

package com.android.jack.java8;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * JUnit test for compilation of Java 8 features
 */
@RunWith(Suite.class)
@SuiteClasses(
  value = {
    // PostM tests
    BridgeTestPostM.class,
    DefaultMethodTest.class,
    EcjInterfaceMethodsTest.class,
    EcjLambdaTestPostM.class,
    GwtTestPostM.class,
    LambdaTestPostM.class,
    RetroLambdaTests.class,
    StaticMethodTest.class,

    // PreN tests
    AnnotationTest.class,
    BridgeTestPreN.class,
    EcjLambdaTest.class,
    ExplicitReceiverTest.class,
    GwtTest.class,
    IntersectionTypeTest.class,
    LambdaTest.class,
    MethodRefTest.class,
    TypeInferenceTest.class,
    VariableTest.class,

    // O test
    ParameterNameTest.class
  }
)
public class Java8AllTest {}
