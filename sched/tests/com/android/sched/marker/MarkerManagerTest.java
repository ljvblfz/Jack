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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class MarkerManagerTest {

  MarkerManager[] markeds;
  Class<? extends Marker>[] markers;
  boolean[][] compatible;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() throws Exception {
    MarkerManager.class.getClassLoader().setDefaultAssertionStatus(true);

    new MarkerManager();

    markeds = new MarkerManager[] {new MarkedA(), new MarkedB(), new MarkedC(), new MarkedB1()};
    markers = (Class<? extends Marker>[]) new Class<?>[] {
        Marker1.class, Marker2.class, Marker3.class};
    compatible = new boolean[markeds.length][markers.length];

    compatible[0][0] = true;
    compatible[1][1] = true;
    compatible[3][1] = true;
    compatible[0][2] = true;
    compatible[1][2] = true;
    compatible[3][2] = true;
  }

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testAddMarker() throws InstantiationException, IllegalAccessException {

    //
    // Check addMarker and NotValidMarkerException
    //
    for (int i = 0; i < markeds.length; i++) {
      for (int j = 0; j < markers.length; j++) {
        try {
          markeds[i].addMarker(markers[j].newInstance());
          assertTrue(compatible[i][j]);
        } catch (AssertionError e) {
          assertFalse(compatible[i][j]);
        }
      }
    }

    //
    // Check DuplicateMarkerException
    //
    for (int i = 0; i < markeds.length; i++) {
      for (int j = 0; j < markers.length; j++) {
        if (compatible[i][j]) {
          try {
            markeds[i].addMarker(markers[j].newInstance());
            fail("Must throw AssertionError");
          } catch (AssertionError e) {
            // Normal Exception
          }
        }
      }
    }
  }

  @Test
  public void testAddAllMarker() throws InstantiationException, IllegalAccessException {
    Marker[][] add = new Marker[markeds.length][markers.length];
    List<Marker> list = new ArrayList<Marker>();

    //
    // Populate markers
    //
    for (int i = 0; i < markeds.length; i++) {
      list.clear();

      for (int j = 0; j < markers.length; j++) {
        if (compatible[i][j]) {
          add[i][j] = markers[j].newInstance();
          list.add(add[i][j]);
        }
      }

      markeds[i].addAllMarker(list);
    }

    //
    // Check
    //
    for (int i = 0; i < markeds.length; i++) {
      for (int j = 0; j < markers.length; j++) {
        if (compatible[i][j]) {
          assertSame(add[i][j], markeds[i].getMarker(markers[j]));
        } else {
          try {
            markeds[i].getMarker(markers[j]);
            fail();
          } catch (AssertionError e) {
            // Normal Exception
          }
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetAllMarkers() throws InstantiationException, IllegalAccessException {
    List<Marker>[] lists = new ArrayList[markeds.length];

    //
    // Populate markers
    //
    for (int i = 0; i < markeds.length; i++) {
      lists[i] = new ArrayList<Marker>();

      for (int j = 0; j < markers.length; j++) {
        if (compatible[i][j]) {
          Marker marker = markers[j].newInstance();
          lists[i].add(marker);
          markeds[i].addMarker(marker);
        }
      }
    }

    //
    // Check
    //
    for (int i = 0; i < markeds.length; i++) {
      assertTrue(markeds[i].getAllMarkers().containsAll(lists[i]));
      assertTrue(lists[i].containsAll(markeds[i].getAllMarkers()));
    }
  }

  @Test
  public void testGetMarker() throws InstantiationException, IllegalAccessException {
    Marker[][] add = new Marker[markeds.length][markers.length];

    //
    // Populate markers
    //
    for (int i = 0; i < markeds.length; i++) {
      for (int j = 0; j < markers.length; j++) {
        if (compatible[i][j]) {
          add[i][j] = markers[j].newInstance();
          markeds[i].addMarker(add[i][j]);
        }
      }
    }

    //
    // Check
    //
    for (int i = 0; i < markeds.length; i++) {
      for (int j = 0; j < markers.length; j++) {
        if (compatible[i][j]) {
          assertSame(add[i][j], markeds[i].getMarker(markers[j]));
        } else {
          try {
            markeds[i].getMarker(markers[j]);
            fail();
          } catch (AssertionError e) {
            // Normal Exception
          }
        }
      }
    }
  }

  @Test
  public void testContainsMarker() throws InstantiationException, IllegalAccessException {
    //
    // Populate markers
    //
    for (int i = 0; i < markeds.length; i++) {
      for (int j = 0; j < markers.length; j++) {
        if (compatible[i][j]) {
          markeds[i].addMarker(markers[j].newInstance());
        }
      }
    }

    //
    // Check
    //
    for (int i = 0; i < markeds.length; i++) {
      for (int j = 0; j < markers.length; j++) {
        if (compatible[i][j]) {
          assertEquals(Boolean.valueOf(compatible[i][j]),
              Boolean.valueOf(markeds[i].containsMarker(markers[j])));
        } else {
          try {
            markeds[i].containsMarker(markers[j]);
            fail();
          } catch (AssertionError e) {
            // Normal Exception
          }
        }
      }
    }
  }

  @Test
  public void testRemoveMarker() throws InstantiationException, IllegalAccessException {
    //
    // Populate markers
    //
    for (int i = 0; i < markeds.length; i++) {
      for (int j = 0; j < markers.length; j++) {
        if (compatible[i][j]) {
          markeds[i].addMarker(markers[j].newInstance());
        }
      }
    }

    //
    // Check
    //
    for (int i = 0; i < markeds.length; i++) {
      for (int j = 0; j < markers.length; j++) {
        if (compatible[i][j]) {
          markeds[i].removeMarker(markers[j]);
          assertFalse(markeds[i].containsMarker(markers[j]));
        } else {
          try {
            markeds[i].removeMarker(markers[j]);
            fail();
          } catch (AssertionError e) {
            // Normal Exception
          }
        }
      }
    }
  }
}
