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

package com.android.jack.lookup;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JMemberLookupException;
import com.android.jack.ir.ast.JReferenceType;

import javax.annotation.Nonnull;

/**
 * An {@code Exception} meaning that the lookup of a method failed.
 */
public abstract class JMethodLookupException extends JMemberLookupException {

  private static final long serialVersionUID = 1L;

  protected JMethodLookupException(
      @Nonnull JReferenceType declaringClass) {
    super(declaringClass);
  }

  @Override
  @Nonnull
  public String getMessage() {
    return "Failed to lookup method " + Jack.getUserFriendlyFormatter().getName(getDeclaringClass())
        + '.' + getMethodString();
  }

  @Nonnull
  protected abstract String getMethodString();
}
