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

package com.android.sched.item;

import static org.junit.Assert.assertEquals;

import com.android.sched.marker.Marker1;
import com.android.sched.marker.Marker2;
import com.android.sched.marker.Marker3;
import com.android.sched.marker.Marker4;
import com.android.sched.marker.Marker5;
import com.android.sched.marker.Marker6;
import com.android.sched.marker.StaticMarkerManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ItemsTest {

  @Before
  public void setUp() throws Exception {
    StaticMarkerManager.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testGetName() {
    assertEquals(Marker1.NAME, Items.getName(Marker1.class));
    assertEquals(Marker2.NAME, Items.getName(Marker2.class));
    assertEquals(Marker3.NAME, Items.getName(Marker3.class));
    assertEquals(Marker4.NAME, Items.getName(Marker4.class));
    assertEquals(Marker5.NAME, Items.getName(Marker5.class));
    assertEquals(Marker6.NAME, Items.getName(Marker6.class));
  }

  @Test
  public void testGetDescription() {
    assertEquals(Marker1.DESCRIPTION, Items.getDescription(Marker1.class));
    assertEquals(Marker2.DESCRIPTION, Items.getDescription(Marker2.class));
    assertEquals(Marker3.DESCRIPTION, Items.getDescription(Marker3.class));
    assertEquals(Marker4.DESCRIPTION, Items.getDescription(Marker4.class));
    assertEquals(Marker5.DESCRIPTION, Items.getDescription(Marker5.class));
    assertEquals(Marker6.DESCRIPTION, Items.getDescription(Marker6.class));
  }
}
