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

package com.android.sched.transform;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * List of transformations that can be applied on the JAST.
 */
public class TransformRequest {

  @Nonnull
  private final List<TransformStep> requests = new ArrayList<TransformStep>();

  public TransformRequest() {
  }

  public void append(@Nonnull TransformStep step) {
    requests.add(step);
  }

  /**
   * @Throws {@link UnsupportedOperationException} if a transformation can not be applied.
   */
  public void commit() throws UnsupportedOperationException {
    for (TransformStep step : requests) {
      step.apply();
    }

    requests.clear();
  }

}
