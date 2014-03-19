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

package com.android.jack.tryfinally.finally002.jack;

public class Data {

  public static int get001(int i) {
    int result = 0;
    try {
      alwaysThrow();
    } catch (Exception e) {
      try {
        try {
          try {
            try {
              canThrow(i);
            } finally {
              result += 3;
            }
          } catch (NullPointerException npe) {
            result += 5;
          }
        } catch (OutOfMemoryError oom) {
          result += 7;
        }
      } catch (TestException te) {
        result += 11;
      }
    }
    return result;
  }

  public static int get002(int i) {
    int result = 0;
    try {
      try {
        alwaysThrow();
      } catch (Exception e) {
        try {
          alwaysThrow();
        } catch (Exception e2) {
          try {
            alwaysThrow();
          } catch (Exception e3) {
            try {
              alwaysThrow();
            } catch (Exception e4) {
              try {
                alwaysThrow();
              } catch (Exception e5) {
                try {
                  canThrow(i);
                } finally {
                  result += 3;
                }
                result += 5;
              }
              result += 7;

            }
            result += 11;

          }
          result += 17;

        }
        result += 23;

      }
    } catch (Exception e) {
      result += 29;
    }
    return result;
  }

  public static int get003(int i) {
    int result = 0;
    try {
        try {
          alwaysThrow();
        } catch (Exception e2) {
          try {
            alwaysThrow();
          } catch (Exception e3) {
              try {
                alwaysThrow();
              } catch (Exception e5) {
                result += 1;
              } finally {
                try {
                  canThrow(i);
                } finally {
                  result += 3;
                }
                result += 5;

            }
            result += 11;

          }
          result += 17;

        }
     } catch (Exception e) {
      result += 29;
    }
    return result;
  }

  @SuppressWarnings("finally")
  public static int get004() {
    try {
      throw new ArrayIndexOutOfBoundsException();
    } catch (ArrayIndexOutOfBoundsException e) {
      try {
        throw new ArrayIndexOutOfBoundsException();
      } catch (ArrayIndexOutOfBoundsException e1) {
        try {
          return 1;
        } finally {
          throw new NullPointerException();
        }
      }
    } catch (NullPointerException e) {
      return 2;
    }
  }

  @SuppressWarnings("finally")
  public static int get005() {
    try {
      throw new ArrayIndexOutOfBoundsException();
    } catch (ArrayIndexOutOfBoundsException e) {
      try {
        throw new ArrayIndexOutOfBoundsException();
      } catch (ArrayIndexOutOfBoundsException e1) {
        try {
          return 1;
        } finally {
          throw new NullPointerException();
        }
      }
    } catch (NullPointerException e) {
      return 2;
    } finally {
      return 3;
    }
  }

  @SuppressWarnings("finally")
  public static int get005Bis() {
    try {
      throw new ArrayIndexOutOfBoundsException();
    } catch (ArrayIndexOutOfBoundsException e) {
      try {
        throw new ArrayIndexOutOfBoundsException();
      } catch (ArrayIndexOutOfBoundsException e1) {
        try {
          return 1;
        } finally {
          throw new NullPointerException();
        }
      }
    } catch (NullPointerException e) {
      return 2;
    } finally {
      return 3;
    }
  }

  @SuppressWarnings("finally")
  public static int get006() {
    try {
      return 1;
    } finally {
      try {
        return 2;
      } finally {
        try {
          return 3;
        } finally {
          throw new NullPointerException();
        }
      }
    }
  }

  public static int field007;

  @SuppressWarnings("finally")
  public static int get007() {
    field007 = 0;
    try {
      return 1;
    }
    catch (NullPointerException e) {
      field007 = 4;
      return 4;
    }
    catch (ArrayIndexOutOfBoundsException e) {
      try {
        return 2;
      } finally {
        try {
          return 3;
        } finally {
          throw new NullPointerException();
        }
      }
    }
  }

  private static void canThrow(int i) throws TestException {
    switch (i) {
      case 1 :
        throw new NullPointerException();
      case 2 :
        throw new OutOfMemoryError();
      case 3 :
        throw new TestException();
    }
  }
  private static void alwaysThrow() throws TestException {
    throw new TestException();
  }
}
