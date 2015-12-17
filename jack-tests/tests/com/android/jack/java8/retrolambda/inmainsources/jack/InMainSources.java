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
// Copyright © 2013-2015 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package com.android.jack.java8.retrolambda.inmainsources.jack;

import java.util.concurrent.Callable;

public class InMainSources {

    public static int callLambda() throws Exception {
        Callable<Integer> lambda = () -> 42;
        return lambda.call();
    }

    public interface Interface {
        default String defaultMethod() {
            return "default";
        }
    }

    public static class Implementer implements Interface {
    }

    public static class Overrider implements Interface {
        @Override
        public String defaultMethod() {
            return "overridden";
        }
    }
}