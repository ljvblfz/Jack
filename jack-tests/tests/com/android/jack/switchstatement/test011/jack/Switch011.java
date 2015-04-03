/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.switchstatement.test011.jack;

import com.android.jack.switchstatement.test011.jack.sub.Token;

public class Switch011 {

  public static com.android.jack.switchstatement.test011.jack.Token getToken() {
    return com.android.jack.switchstatement.test011.jack.Token.UN;
  }

  public static Token test(Token t) {
    if (t != null) {
      switch (t) {
        case ONE:
          return Token.ONE;
        case TWO:
          return Token.TWO;
        case THREE:
          return Token.THREE;
      }
    }
    com.android.jack.switchstatement.test011.jack.Token token = getToken();
    switch (token) {
      case UN:
        return Token.ONE;
      case DEUX:
        return Token.TWO;
      case TROIS:
        return Token.THREE;
      default:
        throw new AssertionError();
    }
  }
}
