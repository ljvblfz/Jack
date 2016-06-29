/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack;

import javax.annotation.Nonnull;

/**
 * Jack {@code Exception} that should be brought to the attention of the user.
 */
@Deprecated
public class JackUserException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public JackUserException() {
    super();
  }

  public JackUserException(@Nonnull String message) {
    super(message);
  }

  public JackUserException(@Nonnull String message, @Nonnull Throwable cause) {
    super(message, cause);
  }

  public JackUserException(@Nonnull Throwable cause) {
    super(cause);
  }
}
