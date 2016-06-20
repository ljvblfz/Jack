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

package com.android.jack.optimizations.valuepropagation.test001;

class A {
  int initialized1_reassigned_with_same_value = 1001;
  int initialized2_reassigned_with_different_value = 1;
  int initialized3_definitely_reassigned_with_different_value = 77;

  int not_initialized1_definitely_assigned_different_value;
  int not_initialized2_definitely_assigned_same_value;
  int not_initialized3_partially_assigned_default_value;
  int not_initialized4_partially_assigned_different_value;
  int not_initialized5_not_assigned;

  A(int i) {
    if (i > 0) {
      initialized1_reassigned_with_same_value = 1001;
      initialized3_definitely_reassigned_with_different_value = 202;
      not_initialized1_definitely_assigned_different_value = 3;
      not_initialized2_definitely_assigned_same_value = 333;
    } else {
      initialized2_reassigned_with_different_value = 2;
      initialized3_definitely_reassigned_with_different_value = 202;
      not_initialized1_definitely_assigned_different_value = 3;
      not_initialized2_definitely_assigned_same_value = 333;
    }

    for (int k = 0; k < i; k++) {
      not_initialized3_partially_assigned_default_value = 0;
      not_initialized4_partially_assigned_different_value = 1;
    }
  }

  A(long i) {
    if (i == 0) {
      initialized3_definitely_reassigned_with_different_value = 202;
      not_initialized1_definitely_assigned_different_value = 3;
      not_initialized2_definitely_assigned_same_value = 333;
      not_initialized3_partially_assigned_default_value = 0;
      not_initialized4_partially_assigned_different_value = 1;
    } else {
      not_initialized3_partially_assigned_default_value = 0;
      not_initialized4_partially_assigned_different_value = 1;
      if (i > 1) {
        initialized3_definitely_reassigned_with_different_value = 202;
        not_initialized1_definitely_assigned_different_value = 3;
        not_initialized2_definitely_assigned_same_value = 333;
      } else {
        initialized3_definitely_reassigned_with_different_value = 202;
        not_initialized1_definitely_assigned_different_value = 33;
        not_initialized2_definitely_assigned_same_value = 333;
      }
    }
  }

  int use(int result) {
    result += initialized1_reassigned_with_same_value;
    result += initialized2_reassigned_with_different_value;
    result += initialized3_definitely_reassigned_with_different_value;
    result += not_initialized1_definitely_assigned_different_value;
    result += not_initialized2_definitely_assigned_same_value;
    result += not_initialized3_partially_assigned_default_value;
    result += not_initialized4_partially_assigned_different_value;
    result += not_initialized5_not_assigned;
    return result;
  }
}
