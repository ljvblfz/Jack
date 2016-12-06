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

package com.android.jack.generic.test002.jack;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class CompletionStage {
}

public class Test002 {

  public void test1() {
    Predicate<Method> isNotStatic = (method) -> (method.getModifiers() & Modifier.STATIC) == 0;
    List<Method> minimalMethods = Stream.of(Object.class, CompletionStage.class)
        .flatMap((klazz) -> Stream.of(klazz.getMethods())).filter(isNotStatic)
        .collect(Collectors.toList());
  }
}
