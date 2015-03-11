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
import com.android.jack.shrob.ListingComparator;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * This {@link Comparator} is used to compare shrob mappings.
 */
public class ComparatorMapping extends ComparatorFile {

  public ComparatorMapping(@Nonnull File candidate, @Nonnull File reference) {
    super(candidate, reference);
  }

  @Override
  public void compare() throws DifferenceFoundException, ComparatorException {
    try {
      ListingComparator.compare(reference, candidate);
    } catch (IOException e) {
      throw new ComparatorException(e);
    }
  }
}
