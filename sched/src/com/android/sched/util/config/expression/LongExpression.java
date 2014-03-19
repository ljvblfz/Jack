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

package com.android.sched.util.config.expression;

import com.android.sched.util.config.ConfigChecker;
import com.android.sched.util.config.PropertyIdException;

import javax.annotation.Nonnull;

/**
 * Abstract class representing an expression of type {@link Long}.
 */
public abstract class LongExpression extends Expression {
  public abstract long eval(@Nonnull ConfigChecker checker)
      throws PropertyIdException, PropertyNotRequiredException;

  @Nonnull
  public BooleanExpression isEqual(@Nonnull final LongExpression right) {
    return new BooleanExpression() {
      @Override
      public boolean eval(@Nonnull ConfigChecker checker)
          throws PropertyIdException, PropertyNotRequiredException {
        return LongExpression.this.eval(checker) == right.eval(checker);
      }

      @Override
      @Nonnull
      public String getDescription() {
        return LongExpression.this.getDescription() + " is equal to " + right.getDescription();
      }

      @Override
      @Nonnull
      public String getCause(@Nonnull ConfigChecker checker) throws PropertyIdException {
        try {
          return format(LongExpression.this.getCause(checker), eval(checker),
              right.getCause(checker), LongExpression.this.eval(checker), right.eval(checker));
        } catch (PropertyNotRequiredException e) {
          return e.getMessage();
        }
      }
    };
  }

  @Nonnull
  public BooleanExpression isNotEqual(@Nonnull final LongExpression right) {
    return new BooleanExpression() {
      @Override
      public boolean eval(@Nonnull ConfigChecker checker)
          throws PropertyIdException, PropertyNotRequiredException {
        return LongExpression.this.eval(checker) != right.eval(checker);
      }

      @Override
      @Nonnull
      public String getDescription() {
        return LongExpression.this.getDescription() + " is not equal to " + right.getDescription();
      }

      @Override
      @Nonnull
      public String getCause(@Nonnull ConfigChecker checker) throws PropertyIdException {
        try {
          return format(LongExpression.this.getCause(checker), eval(checker),
              right.getCause(checker), LongExpression.this.eval(checker), right.eval(checker));
        } catch (PropertyNotRequiredException e) {
          return e.getMessage();
        }
      }
    };
  }

  @Nonnull
  public BooleanExpression isGreater(@Nonnull final LongExpression right) {
    return new BooleanExpression() {
      @Override
      public boolean eval(@Nonnull ConfigChecker checker)
          throws PropertyIdException, PropertyNotRequiredException {
        return LongExpression.this.eval(checker) > right.eval(checker);
      }

      @Override
      @Nonnull
      public String getDescription() {
        return LongExpression.this.getDescription() + " is greater than " + right.getDescription();
      }

      @Override
      @Nonnull
      public String getCause(@Nonnull ConfigChecker checker) throws PropertyIdException {
        try {
          return format(LongExpression.this.getCause(checker), eval(checker),
              right.getCause(checker), LongExpression.this.eval(checker), right.eval(checker));
        } catch (PropertyNotRequiredException e) {
          return e.getMessage();
        }
      }
    };
  }

  @Nonnull
  public BooleanExpression isGreaterOrEqual(@Nonnull final LongExpression right) {
    return new BooleanExpression() {
      @Override
      public boolean eval(@Nonnull ConfigChecker checker)
          throws PropertyIdException, PropertyNotRequiredException {
        return LongExpression.this.eval(checker) >= right.eval(checker);
      }

      @Override
      @Nonnull
      public String getDescription() {
        return LongExpression.this.getDescription() + " is greater or equal than "
            + right.getDescription();
      }

      @Override
      @Nonnull
      public String getCause(@Nonnull ConfigChecker checker) throws PropertyIdException {
        try {
          return format(LongExpression.this.getCause(checker), eval(checker),
              right.getCause(checker), LongExpression.this.eval(checker), right.eval(checker));
        } catch (PropertyNotRequiredException e) {
          return e.getMessage();
        }
      }
    };
  }

  @Nonnull
  public BooleanExpression isLower(@Nonnull final LongExpression right) {
    return new BooleanExpression() {
      @Override
      public boolean eval(@Nonnull ConfigChecker checker)
          throws PropertyIdException, PropertyNotRequiredException {
        return LongExpression.this.eval(checker) < right.eval(checker);
      }

      @Override
      @Nonnull
      public String getDescription() {
        return LongExpression.this.getDescription() + " is lower than " + right.getDescription();
      }

      @Override
      @Nonnull
      public String getCause(@Nonnull ConfigChecker checker) throws PropertyIdException {
        try {
          return format(LongExpression.this.getCause(checker), eval(checker),
              right.getCause(checker), LongExpression.this.eval(checker), right.eval(checker));
        } catch (PropertyNotRequiredException e) {
          return e.getMessage();
        }
      }
    };
  }

  @Nonnull
  public BooleanExpression isLowerOrEqual(@Nonnull final LongExpression right) {
    return new BooleanExpression() {
      @Override
      public boolean eval(@Nonnull ConfigChecker checker)
          throws PropertyIdException, PropertyNotRequiredException {
        return LongExpression.this.eval(checker) <= right.eval(checker);
      }

      @Override
      @Nonnull
      public String getDescription() {
        return LongExpression.this.getDescription() + " is lower or equal than "
            + right.getDescription();
      }

      @Override
      @Nonnull
      public String getCause(@Nonnull ConfigChecker checker) throws PropertyIdException {
        try {
          return format(LongExpression.this.getCause(checker), eval(checker),
              right.getCause(checker), LongExpression.this.eval(checker), right.eval(checker));
        } catch (PropertyNotRequiredException e) {
          return e.getMessage();
        }
      }
    };
  }

  @Nonnull
  public static LongExpression getConstant(final long value) {
    return new LongExpression() {
      @Override
      public long eval(@Nonnull ConfigChecker checker) {
        return value;
      }

      @Override
      @Nonnull
      public String getDescription() {
        return Long.toString(value);
      }

      @Override
      @Nonnull
      public String getCause(@Nonnull ConfigChecker checker) {
        return Long.toString(value);
      }
    };
  }

  @Nonnull
  private String format(
      @Nonnull String left, boolean eval, @Nonnull String right, long lhs, long rhs) {
    StringBuilder sb = new StringBuilder();
    if (!eval) {
      long tmp = lhs;
      lhs = rhs;
      rhs = tmp;
    }

    sb.append(left);
    if (lhs < rhs) {
      sb.append(" is lower than ");
    } else if (lhs > rhs) {
      sb.append(" is greater than ");
    } else {
      sb.append(" is equal to ");
    }
    sb.append(right);

    return sb.toString();
  }
}