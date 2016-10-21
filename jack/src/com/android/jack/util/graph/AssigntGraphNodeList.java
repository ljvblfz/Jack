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

package com.android.jack.util.graph;

import javax.annotation.Nonnull;

/**
 * This class should probably be a schedulable that runs on CFG IR. Since we don't exactly know what
 * that looks like we are just going to leave it as a static helper for the unit tests.
 */
public class AssigntGraphNodeList {
  @SuppressWarnings("unchecked")
  public static void assignNodeList(@SuppressWarnings("rawtypes") @Nonnull IGraph graph) {
    assert graph.getMarker(NodeListMarker.class) == null;
    graph.addMarker(new NodeListMarker(graph.getNodes()));
  }
}