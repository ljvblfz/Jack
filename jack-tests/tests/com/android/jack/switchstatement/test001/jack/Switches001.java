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

package com.android.jack.switchstatement.test001.jack;

/**
 * Switch tests.
 */
public class Switches001 {

  public static int switch001(int a) {
    switch (a) {
      case 1:
        return 1;
      case 2:
        return 2;
      default:
        return 3;
    }
  }

  public int switch002(int i) {
    switch (i) {
      case -1:
        return 2;
      case 10:
      case 15:
        return 20;
      default:
        return -1;
    }
  }

  public int switch003(int i) {
    switch (i) {
      case -1:
        return 1;
      case 15:
        return 2;
    }
    return -1;
  }

  public int switch004(int i, int j) {
    switch (i) {
      case 1:
        switch (j) {
          case 10:
            return 11;
          case 20:
            return 12;
          default:
            break;
        }
      case 2:
        switch (j) {
          case 10:
            return 21;
          case 20:
            return 22;
          default:
            break;
        }
      default:
        return -1;
    }
  }

  public int switch005(int i) {
    switch (i) {
      case 1: {
        return 2;
      }
      case 3: {
        return 4;
      }
      default: {
        break;
      }
    }
    return -1;
  }

  public int switch006(int i) {
    switch (i) {
      case 3: {
        return 3;
      }
      case 1: {
        return 1;
      }
      case 2: {
        return 2;
      }
      default: {
        break;
      }
    }
    return -1;
  }

  public int switch007(int i) {
    int result = 0;
    switch (i) {
      case 3:
      case 1: {
        result = result + 2;
        break;
      }
      case 2: {
        return 2;
      }
      default: {
        break;
      }
    }
    result = result + 2;
    return result;
  }

  public int switch008(int i) {
    int result = 0;
    switch (i) {
      case 3:
      case 1: {
        result = result + 2;
        switch (i) {
          case 3:
            result = result + 2;
            break;
          case 1:
        }
        result = result + 2;
        break;
      }
      case 2: {
        return 2;
      }
      default: {
        break;
      }
    }
    result = result + 2;
    return result;
  }

  public char switch009(char c) {
    switch (c) {
      case '#':
        return '#';
      case 'A':
        return 'A';
    }
    return ' ';
  }

  public int switch010(byte c) {
    switch (c) {
      case (byte) 1:
        return 1;
      case (short) 2:
        return 2;
    }
    return 3;
  }

  public int switch011(int c) {
    switch (c) {
      default:
        return 2;
    }
  }

  public int switch012(int c) {
    switch (c) {
    }
    return 1;
  }

  public int switch013(int c) {
    switch (c) {
      default:
    }
    return 1;
  }

  public int switch014(int c) {
    switch (c) {
      default:
      break;
    }
    return 1;
  }

  public int switch015(int c) {
    switch (c) {
      default:
        if (c == 5) {
          break;
        }
        break;
    }
    return 1;
  }

  public int switch016(int c) {
    switch (c) {
      default:
        if (c == 1) {
          switch (c) {
            case 1:
              return 1;
            case 3 :
              break;
            default:
              break;
          }
          break;
        }
        break;
    }
    return 2;
  }

  public int switch017(int c) {
    switch (c) {
      default:
        if (c == 1) {
          int lv = c;
          while (true) {
            lv += 5;
            break;
          }
          return lv;
        }
        break;
    }
    return 2;
  }

  public int switch018(int c) {
    labelSwitch: switch (c) {
      default:
        if (c == 1) {
          int lv = c;
          while (true) {
            lv += 5;
            break;
          }
          switch (c) {
            case 1:
              break labelSwitch;
            default:
              break;
          }
          return 1;
        }
        break;
    }
    return 2;
  }
}
