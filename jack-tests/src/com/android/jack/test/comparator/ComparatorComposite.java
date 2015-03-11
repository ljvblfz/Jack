/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.test.comparator;

import com.android.jack.comparator.DifferenceFoundException;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * This {link Comparator} is composed of one or several comparators.
 */
public class ComparatorComposite implements Comparator {

  @Nonnull
  private List<Comparator> comparators = new ArrayList<Comparator>();

  public ComparatorComposite(@Nonnull Comparator... comparators) {
    for (Comparator comparator : comparators) {
      this.comparators.add(comparator);
    }
  }

  @Override
  public void compare() throws DifferenceFoundException, ComparatorException {
    for (Comparator comparator : comparators) {
      comparator.compare();
    }
  }
}
