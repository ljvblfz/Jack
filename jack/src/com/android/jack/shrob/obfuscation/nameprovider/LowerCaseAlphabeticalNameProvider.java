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
 * Class generating names in alphabetical order using only lower case
 */
@ImplementationName(iface = NameProvider.class, name = "lower-case")
public class LowerCaseAlphabeticalNameProvider extends AlphabeticalNameProvider {

  private static final char BEGIN_LOWERCASE_CHAR = 'a';

  private static final char END_LOWERCASE_CHAR = 'z';

  @Override
  protected boolean hasNextChar(char c) {
    return c != END_LOWERCASE_CHAR;
  }

  @Override
  protected char nextChar(char c) {
    assert hasNextChar(c);
    return (char) (c + 1);
  }

  @Override
  protected char getFirstChar() {
    return BEGIN_LOWERCASE_CHAR;
  }
}
