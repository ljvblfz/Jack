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

package com.android.jack.annotation.test019.jack;

import java.io.Serializable;
import java.util.ArrayList;

@Check("Annotated")
public class Annotated<@TypeAnnotation("T") T extends @TypeAnnotation("Runnable") Runnable
        & @TypeAnnotation("Serializable") Serializable>
    extends @TypeAnnotation("ArrayList<Object>") ArrayList<@TypeAnnotation("Object") Object> {
  int i;

  @Check("<init>")
  public Annotated(@TypeAnnotation("int") int arg)
      throws @TypeAnnotation("ClassCastException") ClassCastException {
    @TypeAnnotation("int") int tmp = (@TypeAnnotation("int") byte) arg;
    i = tmp;
  }

  @Check("getInt")
  public int getInt(@TypeAnnotation("int") int arg) {
    @TypeAnnotation("int") int[] array1 = {1};
    int @TypeAnnotation("int[]") [] array2 = {2};
    int @TypeAnnotation("int[][]") [][] array3 = {{3}};
    int[] @TypeAnnotation("Component int []")[] array4 = {{4}};
    return i + arg;
  }

  @Check("getInt2")
  public int getInt2(Object arg) {
    if (arg instanceof @TypeAnnotation("Annotated") Annotated) {
      return 1;
    } else if (new @TypeAnnotation("Object") Object().equals(arg)) {
      return 0;
    } else {
      return 2;
    }
  }

  @Check("getInt3")
  public <@TypeAnnotation("U") U> int getInt3(U arg) {
    if (arg instanceof @TypeAnnotation("Annotated") Annotated) {
      return 1;
    } else if (new @TypeAnnotation("Object") Object().equals(arg)) {
      return 0;
    } else {
      return 2;
    }
  }

}
