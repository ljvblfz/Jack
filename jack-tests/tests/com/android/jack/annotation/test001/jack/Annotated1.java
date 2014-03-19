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

package com.android.jack.annotation.test001.jack;

@Annotation9(OneEnum.C)
@Annotation7(Annotation1.class)
@Annotation1
@Annotation2
@Annotation3(annotationValue1 = @Annotation1)
@Annotation4
@Annotation5
@Annotation6(annotationValue1 = {@Annotation1, @Annotation1, @Annotation1()},
annotationValue2 = {@Annotation2(1), @Annotation2(5), @Annotation2(Constants.C3), @Annotation2})
@Annotation8({1, 2 ,4 , (byte) 255})
public class Annotated1 {

  @Annotation1
  private int field;

  @Deprecated
  public void method() {

  }

  public int method2(int a, @Annotation1 int b) {
    @Annotation5
    int result = 0;
    for (@Annotation4 int i = 0; i < 3; i++) {
      result ++;
    }

    new Runnable() {

      @Override
      public void run() {
      }
    };
    return result;
  }

  @Annotation1
  private static class Annotated2 {

    @Annotation1
    private int field;

    @Deprecated
    public void method() {

    }

    public int method2(int a, @Annotation1 int b) {
      @Annotation5
      int result = 0;
      for (@Annotation4 int i = 0; i < 3; i++) {
        result ++;
      }
      return result;
    }
  }

  @Annotation11(int.class)
  public void method3() {

  }
}
