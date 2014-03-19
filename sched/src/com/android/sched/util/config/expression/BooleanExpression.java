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
import com.android.sched.util.config.MissingPropertyException;
import com.android.sched.util.config.PropertyIdException;

import javax.annotation.Nonnull;

/**
 * Abstract class representing an expression of type {@link Boolean}.
 */
public abstract class BooleanExpression extends Expression {
  public abstract boolean eval(@Nonnull ConfigChecker checker)
      throws PropertyIdException, PropertyNotRequiredException;

  @Nonnull
  public BooleanExpression isEqual(@Nonnull final BooleanExpression right) {
    return new BooleanExpression() {
      @Override
      public boolean eval(@Nonnull ConfigChecker checker)
          throws PropertyIdException, PropertyNotRequiredException {
        return BooleanExpression.this.eval(checker) == right.eval(checker);
      }

      @Override
      @Nonnull
      public String getDescription() {
        return format(BooleanExpression.this.getDescription(), true, right.getDescription());
      }

      @Override
      @Nonnull
      public String getCause(@Nonnull ConfigChecker checker) throws PropertyIdException {
        try {
          return format(
              BooleanExpression.this.getCause(checker), eval(checker), right.getCause(checker));
        } catch (PropertyNotRequiredException e) {
          return e.getMessage();
        }
      }
    };
  }


  @Nonnull
  public BooleanExpression isNotEqual(@Nonnull final BooleanExpression right) {
    return new BooleanExpression() {
      @Override
      public boolean eval(@Nonnull ConfigChecker checker)
          throws PropertyIdException, PropertyNotRequiredException {
        return BooleanExpression.this.eval(checker) != right.eval(checker);
      }

      @Override
      @Nonnull
      public String getDescription() {
        return format(BooleanExpression.this.getDescription(), false, right.getDescription());
      }

      @Override
      @Nonnull
      public String getCause(@Nonnull ConfigChecker checker) throws PropertyIdException {
        try {
          return format(
              BooleanExpression.this.getCause(checker), !eval(checker), right.getCause(checker));
        } catch (PropertyNotRequiredException e) {
          return e.getMessage();
        }
      }
    };
  }

  @Nonnull
  public BooleanExpression or(@Nonnull final BooleanExpression right) {
    return new BooleanExpression() {
      @Override
      public boolean eval(@Nonnull ConfigChecker checker)
          throws PropertyIdException, PropertyNotRequiredException {
        try {
          if (BooleanExpression.this.eval(checker)) {
            return true;
          }
        } catch (MissingPropertyException e) {
          // Try right part
        }

        return right.eval(checker);
      }

      @Override
      @Nonnull
      public String getDescription() {
        return BooleanExpression.this.getDescription() + " or " + right.getDescription();
      }

      @Override
      @Nonnull
      public String getCause(@Nonnull ConfigChecker checker) throws PropertyIdException {
        try {
          if (BooleanExpression.this.eval(checker)) {
            return BooleanExpression.this.getCause(checker);
          }
        } catch (PropertyNotRequiredException e) {
          // Try right part
        }

        try {
          if (right.eval(checker)) {
            return right.getCause(checker);
          } else {
            return BooleanExpression.this.getCause(checker) + " and " + right.getCause(checker);
          }
        } catch (PropertyNotRequiredException e) {
          return e.getMessage();
        }
      }
    };
  }

  @Nonnull
  public BooleanExpression and(@Nonnull final BooleanExpression right) {
    return new BooleanExpression() {
      @Override
      public boolean eval(@Nonnull ConfigChecker checker)
          throws PropertyIdException, PropertyNotRequiredException {
        return BooleanExpression.this.eval(checker) && right.eval(checker);
      }

      @Override
      @Nonnull
      public String getDescription() {
        return BooleanExpression.this.getDescription() + " and " + right.getDescription();
      }

      @Override
      @Nonnull
      public String getCause(@Nonnull ConfigChecker checker) throws PropertyIdException {
        try {
          if (BooleanExpression.this.eval(checker) && right.eval(checker)) {
            return BooleanExpression.this.getCause(checker) + " and " + right.getCause(checker);
          } else if (!BooleanExpression.this.eval(checker)) {
            return BooleanExpression.this.getCause(checker);
          } else {
            return right.getCause(checker);
          }
        } catch (PropertyNotRequiredException e) {
          return e.getMessage();
        }
      }
    };
  }

  @Nonnull
  public BooleanExpression isTrue() {
    return new BooleanExpression() {
      @Override
      public boolean eval(@Nonnull ConfigChecker checker)
          throws PropertyIdException, PropertyNotRequiredException {
        return BooleanExpression.this.eval(checker);
      }

      @Override
      @Nonnull
      public String getDescription() {
        return format(BooleanExpression.this.getDescription(), true);
      }

      @Override
      @Nonnull
      public String getCause(@Nonnull ConfigChecker checker) throws PropertyIdException {
        try {
          return format(BooleanExpression.this.getCause(checker), eval(checker));
        } catch (PropertyNotRequiredException e) {
          return e.getMessage();
        }
      }
    };
  }

  @Nonnull
  public BooleanExpression isFalse() {
    return new BooleanExpression() {
      @Override
      public boolean eval(@Nonnull ConfigChecker checker)
          throws PropertyIdException, PropertyNotRequiredException {
        return !BooleanExpression.this.eval(checker);
      }

      @Override
      @Nonnull
      public String getDescription() {
        return format(BooleanExpression.this.getDescription(), false);
      }

      @Override
      @Nonnull
      public String getCause(@Nonnull ConfigChecker checker) throws PropertyIdException {
        try {
          return format(BooleanExpression.this.getCause(checker), !eval(checker));
        } catch (PropertyNotRequiredException e) {
          return e.getMessage();
        }
      }
    };
  }

  @Nonnull
  private String format (@Nonnull String left, boolean equal, @Nonnull String right) {
    StringBuilder sb = new StringBuilder();

    sb.append(left);
    sb.append(" is ");
    if (!equal) {
      sb.append("not ");
    }
    sb.append("equal to ");
    sb.append(right);

    return sb.toString();
  }


  @Nonnull
  private String format (@Nonnull String left, boolean eval) {
    StringBuilder sb = new StringBuilder();

    sb.append(left);
    sb.append(" is set to ");
    sb.append(eval ? "true" : "false");

    return sb.toString();
  }
}