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

package com.android.jack.java8.inference.test003.jack;

import java.util.ArrayList;
import java.util.Iterator;

public class Inference {

  public static <T> Iterator<T> iter(Iterable<T> i)
  {
      return i.iterator();
  }

  public static boolean test()
  {
      Iterator<String> it = iter( new ArrayList<>() );
      return it.hasNext();
  }

}

