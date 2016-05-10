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

package com.android.jack.shrob.obfuscation.nameprovider;

import com.android.sched.util.codec.ImplementationName;

import javax.annotation.Nonnull;

/**
 * This providers returns always the same new name corresponding to an oldName
 * This is used for test purposes, to compare the output with a reference
 */
@ImplementationName(iface = NameProvider.class, name = "rot13")
public class Rot13NameProvider implements NameProvider {

  private static final char BEGIN_LOWERCASE_CHAR = 'a';

  private static final char END_LOWERCASE_CHAR = 'z';

  private static final char BEGIN_UPPERCASE_CHAR = 'A';

  private static final char END_UPPERCASE_CHAR = 'Z';

  private static final int OFFSET = 13;

  protected char nextChar(char c) {
    if (c >= BEGIN_LOWERCASE_CHAR && c <= END_LOWERCASE_CHAR) {
      c += OFFSET;
      if (c > END_LOWERCASE_CHAR) {
        c -= (END_LOWERCASE_CHAR - BEGIN_LOWERCASE_CHAR);
      }
    } else if (c >= BEGIN_UPPERCASE_CHAR && c <= END_UPPERCASE_CHAR) {
      c += OFFSET;
      if (c > END_UPPERCASE_CHAR) {
        c -= (END_UPPERCASE_CHAR - BEGIN_UPPERCASE_CHAR);
      }
    } else {
      throw new AssertionError();
    }
    return c;
  }

  protected boolean isTransformable(char c) {
    return (c >= BEGIN_LOWERCASE_CHAR && c <= END_LOWERCASE_CHAR)
        || (c >= BEGIN_UPPERCASE_CHAR && c <= END_UPPERCASE_CHAR);
  }

  @Override
  @Nonnull
  public String getNewName(@Nonnull String oldName) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < oldName.length() ; i++)  {
      char c = oldName.charAt(i);
      if (isTransformable(c)) {
        sb.append(nextChar(c));
      } else if (c == '(' || c == ')') {
        sb.append('_');
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  @Override
  public boolean hasAlternativeName(@Nonnull String oldName) {
    return false;
  }

}
