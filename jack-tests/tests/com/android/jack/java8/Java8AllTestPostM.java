/*
 * Copyright (C) 2016 The Android Open Source Project
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


import com.android.jack.test.junit.JackTestRunner;
import com.android.jack.test.junit.MinRuntimeVersion;
import com.android.jack.test.junit.RuntimeVersion;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

/**
 * JUnit tests for compilation of Java 8 features which require a post M runtime.
 */
@RunWith(JackTestRunner.class)
@SuiteClasses(value = {
    BridgeTestPostM.class,
    DefaultMethodTest.class,
    EcjInterfaceMethodsTest.class,
    EcjLambdaTestPostM.class,
    GwtTestPostM.class,
    RetroLambdaTests.class,
    StaticMethodTest.class,
    })
@MinRuntimeVersion(RuntimeVersion.N)
public class Java8AllTestPostM {

}
