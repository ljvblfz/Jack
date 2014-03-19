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

package com.android.jack.util.graph;

import javax.annotation.Nonnull;

/**
 * Exception representing a problem during {@link Graph} construction.
 */
public class GraphException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public GraphException() {
    super();
  }

  public GraphException(@Nonnull String message) {
    super(message);
  }

  public GraphException(@Nonnull String message, @Nonnull Throwable cause) {
    super(message, cause);
  }

  public GraphException(@Nonnull Throwable cause) {
    super(cause);
  }
}
