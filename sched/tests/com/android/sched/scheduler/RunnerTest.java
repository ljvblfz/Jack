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

package com.android.sched.scheduler;

import com.android.sched.item.Feature;
import com.android.sched.item.Production;
import com.android.sched.item.TagOrMarkerOrComponent;

import org.junit.Assert;

public class RunnerTest {
  private static TagOrMarkerOrComponentSet tags;
  private static ProductionSet  productions;
  private static FeatureSet     features;


  public void add(Class<? extends TagOrMarkerOrComponent> tag) {
    tags.add(tag);
  }

  public void remove(Class<? extends TagOrMarkerOrComponent> tag) {
    tags.remove(tag);
  }

  public void need(Class<? extends TagOrMarkerOrComponent> tag) {
    Assert.assertTrue(tags.contains(tag));
  }

  public void no(Class<? extends TagOrMarkerOrComponent> tag) {
    Assert.assertFalse(tags.contains(tag));
  }

  public void need(Class<? extends Feature> feature, Class<? extends TagOrMarkerOrComponent> tag) {
    if (features.contains(feature)) {
      Assert.assertTrue(tags.contains(tag));
    }
  }

  public void no(Class<? extends Feature> feature, Class<? extends TagOrMarkerOrComponent> tag) {
    if (features.contains(feature)) {
      Assert.assertFalse(tags.contains(tag));
    }
  }

  public void produce(Class<? extends Production> production) {
    productions.add(production);
  }

  public static void reset() {
    tags = null;
    productions = null;
    features = null;
  }

  public static void init(Scheduler scheduler, Request request) {
    reset();

    tags = scheduler.createTagOrMarkerOrComponentSet();
    productions = scheduler.createProductionSet();
    features = scheduler.createFeatureSet();

    tags.addAll(request.getInitialTags());
    features.addAll(request.getFeatures());
  }

  public static void done(Request request) {
    Assert.assertTrue(
        "Request productions " + request.getTargetProductions() + ", but have " + productions,
        productions.containsAll(request.getTargetProductions()));
    Assert.assertTrue("Request tags " + request.getTargetIncludeTags() + ", but have " + tags,
        tags.containsAll(request.getTargetIncludeTags()));
    Assert.assertTrue("Request no tags " + request.getTargetExcludeTags() + ", but have " + tags,
        tags.containsNone(request.getTargetExcludeTags()));
  }
}
