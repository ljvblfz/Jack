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

package com.android.jack.field.instance005.jack;

public class InstanceField {
  public int f01;
  public final int f02;
  public final int f03  = 3;

  int f11;
  final int f12;
  final int f13  = 13;

  protected int f21;
  protected final int f22;
  protected final int f23  = 23;

  @SuppressWarnings("unused")
  private int f31;
  @SuppressWarnings("unused")
  private final int f32;
  @SuppressWarnings("unused")
  private final int f33  = 33;


  {
    f02=2;
    f12=12;
    f22=22;
    f01++;
    f32=32;
 }
}
