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

package com.android.sched.marker;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

public class ManagedMarkerTest {
  @Before
  public void setUp() {
    StaticMarkerManager.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void testConstructor() {
    testMarkerError(MarkerError1.class);
    testMarkerError(MarkerError2.class);
    testMarkerOk(MarkerOk3.class);
    testMarkerError(MarkerError3.class);
    testMarkerOk(MarkerOk4.class);
    testMarkerError(MarkerError4.class);
    testMarkerError(MarkerError5.class);
    testMarkerError(MarkerError6.class);
    testMarkerError(MarkerError7.class);
    testMarkerError(MarkerError8.class);
  }

  private void testMarkerOk(Class<? extends Marker> marker) {
    try {
      new ManagedMarker(marker);
    } catch (MarkerNotConformException e) {
      fail();
    }
  }

  private void testMarkerError(Class<? extends Marker> marker) {
    try {
      new ManagedMarker(marker);
      fail();
    } catch (MarkerNotConformException e) {
      // Normal Exception
    }
  }
}
