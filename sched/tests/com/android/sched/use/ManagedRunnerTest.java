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

package com.android.sched.use;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.android.sched.feature.Feature1;
import com.android.sched.feature.Feature2;
import com.android.sched.marker.Marker1;
import com.android.sched.marker.Marker2;
import com.android.sched.marker.Marker3;
import com.android.sched.marker.Marker4;
import com.android.sched.marker.Marker5;
import com.android.sched.marker.Marker6;
import com.android.sched.marker.MarkerOk3;
import com.android.sched.marker.MarkerOk4;
import com.android.sched.production.Production1;
import com.android.sched.production.Production2;
import com.android.sched.scheduler.Component0;
import com.android.sched.scheduler.Component1;
import com.android.sched.scheduler.Component2;
import com.android.sched.scheduler.FeatureSet;
import com.android.sched.scheduler.ManagedRunnable;
import com.android.sched.scheduler.ProductionSet;
import com.android.sched.scheduler.Scheduler;
import com.android.sched.scheduler.TagOrMarkerOrComponentSet;
import com.android.sched.tag.Tag1;
import com.android.sched.tag.Tag2;
import com.android.sched.tag.Tag3;
import com.android.sched.tag.Tag4;
import com.android.sched.use.input.Runner1;

import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

public class ManagedRunnerTest {
  @Before
  public void setUp() {
    ManagedRunnable.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void testRunner1() {
    ManagedRunnable runner = new ManagedRunnable(Runner1.class);
    Scheduler scheduler = Scheduler.getScheduler();

    TagOrMarkerOrComponentSet tags = scheduler.createTagOrMarkerOrComponentSet();
    FeatureSet features = scheduler.createFeatureSet();
    ProductionSet productions = scheduler.createProductionSet();

    tags.clear();
    tags.add(Marker3.class);
    tags.add(Marker2.class);
    assertEquals(tags, runner.getAddedTags());

    tags.clear();
    tags.add(Tag2.class);
    tags.add(Tag3.class);
    assertEquals(tags, runner.getRemovedTags());

    tags.clear();
    tags.add(Component0.class);
    tags.add(Component2.class);
    assertEquals(tags, runner.getModifiedTags());

    productions.clear();
    productions.add(Production1.class);
    productions.add(Production2.class);
    assertEquals(productions, runner.getProductions());

    features.clear();
    features.add(Feature2.class);
    features.add(Feature1.class);
    assertEquals(features, runner.getSupportedFeatures());

    tags.clear();
    tags.add(Marker4.class);
    tags.add(MarkerOk3.class);
    tags.add(Tag3.class);
    assertEquals(tags, runner.getDefaultNeededTags());

    tags.clear();
    tags.add(Marker3.class);
    tags.add(MarkerOk4.class);
    assertEquals(tags, runner.getDefaultUnsupportedTags());

    tags.clear();
    tags.add(Marker4.class);
    tags.add(Marker1.class);
    assertEquals(tags, runner.getProtectAddingTags());

    tags.clear();
    tags.add(Tag1.class);
    tags.add(Tag4.class);
    assertEquals(tags, runner.getProtectRemovingTags());

    tags.clear();
    tags.add(Component1.class);
    tags.add(Component2.class);
    assertEquals(tags, runner.getProtectModifyingTags());

    tags.clear();
    tags.add(Marker6.class);
    tags.add(Marker2.class);
    assertEquals(tags, runner.getUnprotectByAddingTags());

    tags.clear();
    tags.add(Marker1.class);
    assertEquals(tags, runner.getUnprotectByRemovingTags());

    Iterator<FeatureSet> iter = runner.getOptionalFeatures().iterator();
    assertTrue(iter.hasNext());

    features.clear();
    features.add(Feature1.class);
    assertEquals(features, iter.next());

    tags.clear();
    tags.add(Marker5.class);
    tags.add(Marker6.class);
    assertEquals(tags, runner.getNeededTags(features));

    tags.clear();
    assertEquals(tags, runner.getUnsupportedTags(features));

    assertFalse(iter.hasNext());
  }
}
