/*
 * Copyright (C) 2014 The Android Open Source Project
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

import com.google.common.base.Joiner;

import com.android.sched.util.codec.Selector;
import com.android.sched.util.config.ConfigChecker;
import com.android.sched.util.config.PropertyIdException;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Abstract class representing an expression of type {@link Class<? extends T>}.
 *
 * @param <T> type of the base class.
 */
@SuppressWarnings("javadoc")
public abstract class ClassExpression<T> extends Expression {
  public abstract Class<? extends T> eval(@Nonnull ConfigChecker checker)
      throws PropertyIdException, PropertyNotRequiredException;

  @Nonnull
  private final Selector<T> selector;

  protected ClassExpression(@Nonnull Selector<T> selector) {
    this.selector = selector;
  }

  @Nonnull
  public BooleanExpression isImplementedBy(@Nonnull final Class<? extends T> type) {
    return new BooleanExpression() {
      @Override
      public boolean eval(@Nonnull ConfigChecker checker)
          throws PropertyIdException, PropertyNotRequiredException {
        return type == ClassExpression.this.eval(checker);
      }

      @Override
      @Nonnull
      public String getDescription() {
        return format(ClassExpression.this.getDescription(), true, selector.getName(type));
      }

      @Override
      @Nonnull
      public String getCause(@Nonnull ConfigChecker checker) throws PropertyIdException {
        try {
          return format(ClassExpression.this.getCause(checker), eval(checker),
              selector.getName(ClassExpression.this.eval(checker)));
        } catch (PropertyNotRequiredException e) {
          return e.getMessage();
        }
      }
    };
  }

  @Nonnull
  public BooleanExpression isNotImplementedBy(@Nonnull final Class<? extends T> type) {
    return new BooleanExpression() {
      @Override
      public boolean eval(@Nonnull ConfigChecker checker)
          throws PropertyIdException, PropertyNotRequiredException {
        return type != ClassExpression.this.eval(checker);
      }


      @Override
      @Nonnull
      public String getDescription() {
        return format(ClassExpression.this.getDescription(), false, selector.getName(type));
      }

      @Override
      @Nonnull
      public String getCause(@Nonnull ConfigChecker checker) throws PropertyIdException {
        try {
          return format(ClassExpression.this.getCause(checker), !eval(checker),
              selector.getName(ClassExpression.this.eval(checker)));
        } catch (PropertyNotRequiredException e) {
          return e.getMessage();
        }
      }
    };
  }

  @Nonnull
  public BooleanExpression isSubClassOf(@Nonnull final Class<? extends T> type) {
    return new BooleanExpression() {
      @Override
      public boolean eval(@Nonnull ConfigChecker checker)
          throws PropertyIdException, PropertyNotRequiredException {
        return type.isAssignableFrom(ClassExpression.this.eval(checker));
      }

      @Override
      @Nonnull
      public String getDescription() {
        return format(ClassExpression.this.getDescription(), true, selector.getNames(type));
      }

      @Override
      @Nonnull
      public String getCause(@Nonnull ConfigChecker checker) throws PropertyIdException {
        try {
          return format(ClassExpression.this.getCause(checker), eval(checker),
              selector.getName(ClassExpression.this.eval(checker)));
        } catch (PropertyNotRequiredException e) {
          return e.getMessage();
        }
      }
    };
  }

  @Nonnull
  public BooleanExpression isNotSubClassOf(@Nonnull final Class<? extends T> type) {
    return new BooleanExpression() {
      @Override
      public boolean eval(@Nonnull ConfigChecker checker)
          throws PropertyIdException, PropertyNotRequiredException {
        return !type.isAssignableFrom(ClassExpression.this.eval(checker));
      }

      @Override
      @Nonnull
      public String getDescription() {
        return format(ClassExpression.this.getDescription(), false, selector.getNames(type));
      }

      @Override
      @Nonnull
      public String getCause(@Nonnull ConfigChecker checker) throws PropertyIdException {
        try {
          return format(
              ClassExpression.this.getCause(checker), !eval(checker), selector.getName(type));
        } catch (PropertyNotRequiredException e) {
          return e.getMessage();
        }
      }
    };
  }

  @Nonnull
  private String format (@Nonnull String left, boolean set, @Nonnull String right) {
    StringBuilder sb = new StringBuilder();

    sb.append(left);
    sb.append(" is ");
    if (!set) {
      sb.append("not ");
    }
    sb.append("set to '");
    sb.append(right);
    sb.append('\'');

    return sb.toString();
  }

  @Nonnull
  private String format (@Nonnull String left, boolean set, @Nonnull List<String> rights) {
    StringBuilder sb = new StringBuilder();

    sb.append(left);
    sb.append(" is ");
    if (!set) {
      sb.append("not ");
    }
    sb.append("set to ");
    if (rights.size() > 1) {
      sb.append("one of {");
      Joiner.on(',').appendTo(sb, rights);
      sb.append('}');
    } else if (rights.size() == 1) {
      sb.append('\'');
      sb.append(rights.get(0));
      sb.append('\'');
    } else {
      sb.append("nothing");
    }

    return sb.toString();
  }
}