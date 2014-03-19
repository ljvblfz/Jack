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

package com.android.jack.tryfinally.finallyblock.jack;

public class Nested {

  public static void throwException() {
    throw new RuntimeException();
  }

  public static void doNotThrowException() {
    return;
  }

  public static int inTry1() {
    int a = 1;
    try {
      a = a * 2;
      doNotThrowException();
      a = a * 23;
      try {
        a = a * 7;
        doNotThrowException();
        a = a * 19;
      } catch (Exception e) {
        a = a * 11;
      } finally {
        a = a * 13;
      }
      a = a * 17;
    } catch (Exception e) {
      a = a * 3;
    } finally {
      a = a * 5;
    }
    return a;
  }

  public static int inTry2() {
    int a = 1;
    try {
      a = a * 2;
      doNotThrowException();
      a = a * 23;
      try {
        a = a * 7;
        throwException();
        a = a * 19;
      } catch (Exception e) {
        a = a * 11;
      } finally {
        a = a * 13;
      }
      a = a * 17;
    } catch (Exception e) {
      a = a * 3;
    } finally {
      a = a * 5;
    }
    return a;
  }

  public static int inTry3() {
    int a = 1;
    try {
      a = a * 2;
      doNotThrowException();
      a = a * 23;
      try {
        a = a * 7;
        throwException();
        a = a * 19;
      } finally {
        a = a * 13;
      }
      a = a * 17;
    } catch (Exception e) {
      a = a * 3;
    } finally {
      a = a * 5;
    }
    return a;
  }

  public static int inTry4() {
    int a = 1;
    try {
      a = a * 2;
      doNotThrowException();
      a = a * 23;
      try {
        a = a * 7;
        throwException();
        a = a * 29;
      } catch (Exception e) {
        a = a * 11;
        throwException();
        a = a * 19;
      } finally {
        a = a * 13;
      }
      a = a * 17;
    } catch (Exception e) {
      a = a * 3;
    } finally {
      a = a * 5;
    }
    return a;
  }

  public static int inTry5() {
    int a = 1;
    try {
      a = a * 2;
      doNotThrowException();
      a = a * 23;
      try {
        a = a * 7;
        doNotThrowException();
        a = a * 29;
      } catch (Exception e) {
        a = a * 11;
      } finally {
        a = a * 13;
        throwException();
        a = a * 19;
      }
      a = a * 17;
    } catch (Exception e) {
      a = a * 3;
    } finally {
      a = a * 5;
    }
    return a;
  }

  public static int inTry6() {
    int a = 1;
    try {
      a = a * 2;
      doNotThrowException();
      a = a * 23;
      try {
        a = a * 7;
        throwException();
        a = a * 29;
      } catch (Exception e) {
        a = a * 11;
      } finally {
        a = a * 13;
        throwException();
        a = a * 19;
      }
      a = a * 17;
    } catch (Exception e) {
      a = a * 3;
    } finally {
      a = a * 5;
    }
    return a;
  }

  public static int inCatch1() {
    int a = 1;
    try {
      a = a * 2;
      doNotThrowException();
      a = a * 19;
    } catch (Exception e) {
      a = a * 3;
      try {
        a = a * 7;
      } catch (Exception e2) {
        a = a * 11;
      } finally {
        a = a * 13;
      }
      a = a * 17;
    } finally {
      a = a * 5;
    }
    return a;
  }

  public static int inCatch2() {
    int a = 1;
    try {
      a = a * 2;
      throwException();
      a = a * 19;
    } catch (Exception e) {
      a = a * 3;
      try {
        a = a * 7;
      } catch (Exception e2) {
        a = a * 11;
      } finally {
        a = a * 13;
      }
      a = a * 17;
    } finally {
      a = a * 5;
    }
    return a;
  }

  public static int inCatch3() {
    int a = 1;
    try {
      a = a * 2;
      throwException();
      a = a * 23;
    } catch (Exception e) {
      a = a * 3;
      try {
        a = a * 7;
        throwException();
        a = a * 19;
      } catch (Exception e2) {
        a = a * 11;
      } finally {
        a = a * 13;
      }
      a = a * 17;
    } finally {
      a = a * 5;
    }
    return a;
  }

  public static int inCatch4() {
    int a = 1;
    try {
      try {
        a = a * 2;
        throwException();
        a = a * 23;
      } catch (Exception e) {
        a = a * 3;
        try {
          a = a * 7;
          throwException();
          a = a * 29;
        } catch (Exception e2) {
          a = a * 11;
          throwException();
          a = a * 19;
        } finally {
          a = a * 13;
        }
        a = a * 17;
      } finally {
        a = a * 5;
      }
    } catch (Exception e3) {
      a = a * 31;
    }
    return a;
  }

  public static int inCatch5() {
    int a = 1;
    try {
      try {
        a = a * 2;
        throwException();
        a = a * 23;
      } catch (Exception e) {
        a = a * 3;
        try {
          a = a * 7;
        } catch (Exception e2) {
          a = a * 11;
        } finally {
          a = a * 13;
          throwException();
          a = a * 19;
        }
        a = a * 17;
      } finally {
        a = a * 5;
      }
    } catch (Exception e3) {
      a = a * 31;
    }
    return a;
  }

  public static int inCatch6() {
    int a = 1;
    try {
      try {
        a = a * 2;
        throwException();
        a = a * 23;
      } catch (Exception e) {
        a = a * 3;
        try {
          a = a * 7;
          throwException();
          a = a * 29;
        } catch (Exception e2) {
          a = a * 11;
        } finally {
          a = a * 13;
          throwException();
          a = a * 19;
        }
        a = a * 17;
      } finally {
        a = a * 5;
      }
    } catch (Exception e3) {
      a = a * 31;
    }
    return a;
  }

  public static int inFinally1() {
    int a = 1;
    try {
      a = a * 2;
      doNotThrowException();
      a = a * 23;
    } catch (Exception e) {
      a = a * 3;
    } finally {
      a = a * 5;
      try {
        a = a * 7;
        doNotThrowException();
        a = a * 19;
      } catch (Exception e) {
        a = a * 11;
      } finally {
        a = a * 13;
      }
      a = a * 17;
    }
    return a;
  }

  public static int inFinally2() {
    int a = 1;
    try {
      a = a * 2;
      doNotThrowException();
      a = a * 23;
    } catch (Exception e) {
      a = a * 3;
    } finally {
      a = a * 5;
      try {
        a = a * 7;
        throwException();
        a = a * 19;
      } catch (Exception e) {
        a = a * 11;
      } finally {
        a = a * 13;
      }
      a = a * 17;
    }
    return a;
  }

  public static int inFinally3() {
    int a = 1;
    try {
      try {
        a = a * 2;
        doNotThrowException();
        a = a * 23;
      } catch (Exception e) {
        a = a * 3;
      } finally {
        a = a * 5;
        try {
          a = a * 7;
          throwException();
          a = a * 19;
        } catch (Exception e) {
          a = a * 11;
          throwException();
          a = a * 29;
        } finally {
          a = a * 13;
        }
        a = a * 17;
      }
    } catch (Exception e3) {
      a = a * 31;
    }
    return a;
  }

  public static int inFinally4() {
    int a = 1;
    try {
      try {
        a = a * 2;
        doNotThrowException();
        a = a * 23;
      } catch (Exception e) {
        a = a * 3;
      } finally {
        a = a * 5;
        try {
          a = a * 7;
          doNotThrowException();
          a = a * 19;
        } catch (Exception e) {
          a = a * 11;
        } finally {
          a = a * 13;
          throwException();
          a = a * 29;
        }
        a = a * 17;
      }
    } catch (Exception e3) {
      a = a * 31;
    }
    return a;
  }

  public static int inFinally5() {
    int a = 1;
    try {
      try {
        a = a * 2;
        doNotThrowException();
        a = a * 23;
      } catch (Exception e) {
        a = a * 3;
      } finally {
        a = a * 5;
        try {
          a = a * 7;
          throwException();
          a = a * 19;
        } catch (Exception e) {
          a = a * 11;
        } finally {
          a = a * 13;
          throwException();
          a = a * 29;
        }
        a = a * 17;
      }
    } catch (Exception e3) {
      a = a * 31;
    }
    return a;
  }

  public static int inFinally6() {
    int a = 1;
    try {
      a = a * 2;
      doNotThrowException();
      a = a * 23;
    } catch (Exception e) {
      a = a * 3;
    } finally {
      a = a * 5;
      try {
        a = a * 7;
        doNotThrowException();
        a = a * 19;
      } catch (Exception e) {
        a = a * 11;
      } finally {
        a = a * 13;
      }
      a = a * 17;
    }
    return a;
  }

  public static int inFinally7() {
    int a = 1;
    try {
      a = a * 2;
      doNotThrowException();
      a = a * 23;
    } catch (Exception e) {
      a = a * 3;
    } finally {
      a = a * 5;
      try {
        a = a * 7;
        throwException();
        a = a * 19;
      } catch (Exception e) {
        a = a * 11;
      } finally {
        a = a * 13;
      }
      a = a * 17;
    }
    return a;
  }

  public static int inFinally8() {
    int a = 1;
    try {
      try {
        a = a * 2;
        doNotThrowException();
        a = a * 23;
      } catch (Exception e) {
        a = a * 3;
      } finally {
        a = a * 5;
        try {
          a = a * 7;
          throwException();
          a = a * 19;
        } catch (Exception e) {
          a = a * 11;
          throwException();
          a = a * 29;
        } finally {
          a = a * 13;
        }
        a = a * 17;
      }
    } catch (Exception e3) {
      a = a * 31;
    }
    return a;
  }

  public static int inFinally9() {
    int a = 1;
    try {
      try {
        a = a * 2;
        doNotThrowException();
        a = a * 23;
      } catch (Exception e) {
        a = a * 3;
      } finally {
        a = a * 5;
        try {
          a = a * 7;
          doNotThrowException();
          a = a * 19;
        } catch (Exception e) {
          a = a * 11;
        } finally {
          a = a * 13;
          throwException();
          a = a * 29;
        }
        a = a * 17;
      }
    } catch (Exception e3) {
      a = a * 31;
    }
    return a;
  }

  public static int inFinally10() {
    int a = 1;
    try {
      try {
        a = a * 2;
        doNotThrowException();
        a = a * 23;
      } catch (Exception e) {
        a = a * 3;
      } finally {
        a = a * 5;
        try {
          a = a * 7;
          throwException();
          a = a * 19;
        } catch (Exception e2) {
          a = a * 11;
        } finally {
          a = a * 13;
          throwException();
          a = a * 29;
        }
        a = a * 17;
      }
    } catch (Exception e3) {
      a = a * 31;
    }
    return a;
  }

  public static int inFinally11() {
    int result = 0;
    try {
      result += 1;
    } finally {
      result += 1;
      try {
        result += 1;
      } finally {
        try {
          result += 1;
        } finally {
          result += 1;
          try {
            result += 1;
          } finally {
            result += 1;
          }
        }
      }
    }
    return result;
  }

}
