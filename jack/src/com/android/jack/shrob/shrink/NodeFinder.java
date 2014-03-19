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

package com.android.jack.shrob.shrink;

import com.android.jack.ir.ast.JNode;
import com.android.jack.shrob.spec.Specification;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Finds all the nodes matching the given specifications.
 * @param <T> the type of the node searched
 */
public class NodeFinder<T extends JNode> {

  @Nonnull
  private final List<T> toSearch;

  private boolean allMatched = true;

  public NodeFinder(@Nonnull List<T> toSearch) {
    this.toSearch = toSearch;
  }

  @Nonnull
  public List<T> find(@Nonnull List<? extends Specification<T>> specs) {
    List<T> found = new ArrayList<T>();
    for (Specification<T> spec : specs) {
      boolean matches = false;
      for (T t : toSearch) {
        if (spec.matches(t)) {
          matches = true;
          if (!found.contains(t)) {
            found.add(t);
          }
        }
      }
      if (!matches) {
        allMatched = false;
      }
    }
    return found;
  }

  public boolean allSpecificationsMatched() {
    return allMatched;
  }
}
