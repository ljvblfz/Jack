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

package com.android.jack.frontend.generic.test003.jack;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

interface Data<T, S extends BaseStream<T, S>> extends Iterable<T> {
  public interface type<T> extends Data<T, Stream<T>> { }
}

public class Test003 {

  public void test1(Data.type<Integer> data) {
    List<Integer> asImmutableList = call(data, s -> s.collect(Collectors.toList()));
  }

  protected <T, R, S_IN extends BaseStream<T, S_IN>> R call(Data<T, S_IN> data,
      Function<S_IN, R> terminalF) {
    return null;
  }
}
