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

package com.android.jack.shrob.obfuscation.nameprovider;

import com.android.sched.util.codec.ImplementationName;

/**
 * Class generating names in alphabetical order with upper- and lowercase
 */
@ImplementationName(iface = NameProvider.class, name = "mixed-case")
public class MixedCaseAlphabeticalNameProvider extends AlphabeticalNameProvider {

  private static final char BEGIN_LOWERCASE_CHAR = 'a';

  private static final char END_LOWERCASE_CHAR = 'z';

  private static final char BEGIN_UPPERCASE_CHAR = 'A';

  private static final char END_UPPERCASE_CHAR = 'Z';

  @Override
  protected boolean hasNextChar(char c) {
      return c != END_UPPERCASE_CHAR;
  }

  @Override
  protected char nextChar(char c) {
    assert hasNextChar(c);
    if (c == END_LOWERCASE_CHAR) {
      return BEGIN_UPPERCASE_CHAR;
    } else {
      return (char) (c + 1);
    }
  }

  @Override
  protected char getFirstChar() {
    return BEGIN_LOWERCASE_CHAR;
  }
}
