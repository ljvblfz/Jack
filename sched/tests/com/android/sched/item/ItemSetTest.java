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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.android.sched.scheduler.Scheduler;
import com.android.sched.scheduler.TagOrMarkerOrComponentSet;
import com.android.sched.tag.Tag1;
import com.android.sched.tag.Tag1and3and5;
import com.android.sched.tag.Tag2;
import com.android.sched.tag.Tag3;
import com.android.sched.tag.Tag4;
import com.android.sched.tag.Tag5;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ItemSetTest {

  TagOrMarkerOrComponentSet ts;
  TagOrMarkerOrComponentSet tsAnB;
  TagOrMarkerOrComponentSet tsA;
  TagOrMarkerOrComponentSet tsB;
  Class<? extends Tag>[] tAnB;
  Class<? extends Tag>[] tA;
  Class<? extends Tag>[] tB;

  Scheduler scheduler;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() throws Exception {
    ItemSet.class.getClassLoader().setDefaultAssertionStatus(true);
    scheduler = Scheduler.getScheduler();

    ts = scheduler.createTagOrMarkerOrComponentSet();

    tAnB = (Class<? extends Tag>[]) new Class<?>[] {
        Tag1.class, Tag2.class, Tag3.class, Tag4.class, Tag5.class};
    tA = (Class<? extends Tag>[]) new Class<?>[] {Tag1.class, Tag3.class, Tag5.class};
    tB = (Class<? extends Tag>[]) new Class<?>[] {Tag2.class, Tag4.class};

    tsAnB = scheduler.createTagOrMarkerOrComponentSet();
    for (Class<? extends Tag> tag : tAnB) {
      tsAnB.add(tag);
    }

    tsA = scheduler.createTagOrMarkerOrComponentSet();
    for (Class<? extends Tag> tag : tA) {
      tsA.add(tag);
    }

    tsB = scheduler.createTagOrMarkerOrComponentSet();
    for (Class<? extends Tag> tag : tB) {
      tsB.add(tag);
    }
  }

  @After
  public void tearDown() throws Exception {
    ts = null;
    tsAnB = null;
    tsA = null;
    tsB = null;
  }

  @Test
  public void testAddAndRemove() {
    // Set of one element (TagOne)
    ts.add(Tag1.class);
    ts.remove(Tag2.class);
    ts.add(Tag1.class);
    ts.remove(Tag1.class);
    Assert.assertTrue(ts.isEmpty());

    // Set of set A
    for (Class<? extends Tag> tag : tA) {
      ts.add(tag);
    }

    Assert.assertEquals(tA.length, ts.getSize());
    for (Class<? extends Tag> tag : tB) {
      ts.remove(tag);
    }

    Assert.assertEquals(tA.length, ts.getSize());
    for (Class<? extends Tag> tag : tA) {
      ts.add(tag);
    }

    Assert.assertEquals(tA.length, ts.getSize());
    for (Class<? extends Tag> tag : tA) {
      int sz = ts.getSize();

      ts.remove(tag);
      Assert.assertEquals(sz - 1, ts.getSize());
    }

    // ComposedOf is ok
    ts.add(Tag1and3and5.class);
    Assert.assertEquals(tA.length, ts.getSize());
    for (Class<? extends Tag> tag : tA) {
      int sz = ts.getSize();
      ts.add(tag);
      Assert.assertEquals(sz, ts.getSize());

      ts.remove(tag);
      Assert.assertEquals(sz - 1, ts.getSize());
    }
  }

  @Test
  public void testConstructors() {
    // Empty
    Assert.assertEquals(0, ts.getSize());

    // With tsAnB

    ts = new TagOrMarkerOrComponentSet(tsAnB);

    Assert.assertEquals(tAnB.length, ts.getSize());
    for (Class<? extends Tag> tag : tAnB) {
      int sz = ts.getSize();

      ts.remove(tag);
      Assert.assertEquals(sz - 1, ts.getSize());
    }

    // With tsA

    ts = new TagOrMarkerOrComponentSet(tsA);

    Assert.assertEquals(tA.length, ts.getSize());
    for (Class<? extends Tag> tag : tA) {
      int sz = ts.getSize();

      ts.remove(tag);
      Assert.assertEquals(sz - 1, ts.getSize());
    }

    // With tsB

    ts = new TagOrMarkerOrComponentSet(tsB);

    Assert.assertEquals(tB.length, ts.getSize());
    for (Class<? extends Tag> tag : tB) {
      int sz = ts.getSize();

      ts.remove(tag);
      Assert.assertEquals(sz - 1, ts.getSize());
    }
  }

  @Test
  public void testAddAll() {
    ts.addAll(tsA);
    Assert.assertEquals(tsA.getSize(), ts.getSize());
    for (Class<? extends Tag> tag : tA) {
      int sz = ts.getSize();
      ts.add(tag);
      Assert.assertEquals(sz, ts.getSize());

      ts.remove(tag);
      Assert.assertEquals(sz - 1, ts.getSize());
    }
  }

  @Test
  public void testRemoveAll() {
    ts.addAll(tsAnB);

    ts.removeAll(tsA);
    Assert.assertEquals(tsB.getSize(), ts.getSize());

    ts.removeAll(tsA);
    Assert.assertEquals(tsB.getSize(), ts.getSize());

    ts.removeAll(tsB);
    Assert.assertEquals(0, ts.getSize());

    ts.addAll(tsA);
    ts.removeAll(tsAnB);
    Assert.assertEquals(0, ts.getSize());
  }

  @Test
  public void testContains() {
    ts.addAll(tsA);

    for (Class<? extends Tag> tag : tA) {
      assertTrue(ts.contains(tag));
    }

    for (Class<? extends Tag> tag : tB) {
      assertFalse(ts.contains(tag));
    }
  }

  @Test
  public void testContainsAll() {
    // tsAnB equal tsAnB

    ts.addAll(tsAnB);
    assertTrue(ts.containsAll(tsAnB));
    assertTrue(tsAnB.containsAll(ts));

    // tsA not equal tsB

    assertFalse(tsA.containsAll(tsB));
    assertFalse(tsB.containsAll(tsA));

    //

    for (Class<? extends Tag> tag : tAnB) {
      ts.remove(tag);
      assertTrue(tsAnB.containsAll(ts));
    }

    ts.addAll(tsAnB);
    for (Class<? extends Tag> tag : tAnB) {
      ts.remove(tag);
      assertFalse(ts.containsAll(tsAnB));
    }
  }

  @Test
  public void testContainsOne() {
    ts.addAll(tsAnB);
    assertTrue(ts.containsOne(tsAnB));
    assertTrue(tsAnB.containsOne(ts));

    assertFalse(tsA.containsOne(tsB));
    assertFalse(tsB.containsOne(tsA));

    assertTrue(tsAnB.containsOne(tsA));
    assertTrue(tsA.containsOne(tsAnB));
    assertTrue(tsAnB.containsOne(tsB));
    assertTrue(tsB.containsOne(tsAnB));
  }
}
