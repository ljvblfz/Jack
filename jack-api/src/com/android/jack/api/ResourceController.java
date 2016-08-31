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

package com.android.jack.api;

import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Resource controller of a {@link JackProvider}.
 */
public interface ResourceController {
  /**
   * Resource category
   */
  public enum Category {
    /**
     * Memory (heap)
     */
    MEMORY,
    /**
     * Disk
     */
    DISK,
    /**
     * Memory (code)
     */
    CODE;
  }

  /**
   * Kind of Impact
   */
  public enum Impact {
    /**
     * Compilation performance
     */
    PERFORMANCE,
    /**
     * Compilation latency
     */
    LATENCY;
  }

  /**
   * Clean system resources used by {@link JackProvider} according to impacts the caller is able to
   * undergo during subsequent compilations. The action of this method is best effort, no guarantee
   * is made about the result. See {{@link #getSupportedCategories()} and
   * {@link #getSupportedImpacts()} to know which categories and impact the current JackProvider
   * knows.
   *
   * @param categories set of categories to clean
   * @param impacts set of allowed impacts on subsequent compilations
   */
  void clean(@Nonnull Set<Category> categories, @Nonnull Set<Impact> impacts);

  /**
   * @return the set of supported {@link Category}
   */
  Set<Category> getSupportedCategories();

  /**
   * @return the set of supported {@link Impact}
   */
  Set<Impact> getSupportedImpacts();
}
