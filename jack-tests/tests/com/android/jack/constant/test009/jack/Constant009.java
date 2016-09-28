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

package com.android.jack.constant.test009.jack;

public class Constant009 {

  int i=-1;
  long j=123456789123456789L;

  public long getAndIncLong0() {
    return ((false ? (j--) :  ( 1761670604)) >>> -1468536474L);
  }

  public long getAndIncInt0() {
    return ((false ? (i--) :  ( 1761670604)) >>> -1468536474L);
  }

  public long getAndIncLong1() {
    return ((true ? (j--) :  ( 1761670604)) >>> -1468536474L);
  }

  public long getAndIncInt1() {
    return ((true ? (i--) :  ( 1761670604)) >>> -1468536474L);
  }

  public long getAndIncLong2() {
    return (true ? j-- :  (false ? 1868855630L : -1810748389L)) << Long.MAX_VALUE;
  }

  public long getAndIncInt2() {
    return (true ? i-- :  (false ? 1868855630L : -1810748389L)) << Long.MAX_VALUE;
  }
}
