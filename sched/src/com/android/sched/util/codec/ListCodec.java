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

package com.android.sched.util.codec;

import com.android.sched.util.config.ChainedException.ChainedExceptionBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to create an instance of {@link List}
 *
 * @param <T> Element type of the list
 */
public class ListCodec<T> implements StringCodec<List<T>> {
  @Nonnull
  private final StringCodec<T> parser;

  @Nonnegative
  private int min;
  @Nonnegative
  private int max;
  @Nonnull
  private String regexp;
  @Nonnull
  private String separator;
  private boolean unicity = false;

  public ListCodec(@Nonnull StringCodec<T> parser) {
    this.separator = ",";
    this.regexp = Pattern.quote(separator);
    this.parser = parser;
    this.min = 1;
    this.max = Integer.MAX_VALUE;
  }

  public ListCodec<T> setSeparator(@Nonnull String separator) {
    this.separator = separator;
    this.regexp = Pattern.quote(separator);

    return this;
  }

  @Nonnull
  public String getSeparator() {
    return separator;
  }

  public ListCodec<T> setMin(@Nonnegative int min) {
    assert min < max;
    assert min >= 0;

    this.min = min;

    return this;
  }

  public ListCodec<T> setMax(@Nonnegative int max) {
    assert min < max;
    assert max >  0;

    this.max = max;

    return this;
  }

  public ListCodec<T> ensureUnicity() {
    this.unicity = true;

    return this;
  }

  @Override
  @Nonnull
  public List<T> parseString(@Nonnull CodecContext context, @Nonnull String string) {
    string = string.trim();
    if (string.isEmpty()) {
      return Collections.emptyList();
    }

    String[] values = string.split(regexp);
    List<T>  list   = new ArrayList<T>(values.length);

    for (String v : values) {
      list.add(parser.parseString(context, v.trim()));
    }

    return list;
  }

  @Override
  @CheckForNull
  public List<T> checkString(@Nonnull CodecContext context, @Nonnull String string)
      throws ParsingException  {
    ChainedExceptionBuilder<ParsingException> exceptions =
        new ChainedExceptionBuilder<ParsingException>();

    String[] values = string.trim().split(regexp);
    int size = values.length;

    if (size < min) {
      exceptions.appendException(new ParsingException(
          "The minimal number of element in the list must be " + min + " but is " + values.length));
    }

    // Manage empty list
    if (size == 1 && values[0].isEmpty()) {
      return Collections.emptyList();
    }

    if (size > max) {
      exceptions.appendException(new ParsingException(
          "The maximal number of element in the list must be " + max + " but is " + values.length));
    }

    int index = 0;
    List<T> list = new ArrayList<T>(values.length);
    for (String v : values) {
      try {
        T element = parser.checkString(context, v.trim());
        if (element == null) {
          if (unicity) {
            element = parser.parseString(context, v.trim());
          } else {
            // If one element is null, do not compute the list
            list = null;
          }
        }

        if (list != null) {
          if (list.contains(element)) {
            exceptions.appendException(new ListParsingException(index,
                "'" + v + "' is redundant with element #" + (list.indexOf(element) + 1)));
          }

          list.add(element);
        }
      } catch (ParsingException e) {
        exceptions.appendException(new ListParsingException(index, e));
      }

      index++;
    }

    exceptions.throwIfNecessary();

    return list;
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull List<T> list)
      throws CheckingException {
    ChainedExceptionBuilder<CheckingException> exceptions =
        new ChainedExceptionBuilder<CheckingException>();

    int size = list.size();

    if (size < min) {
      exceptions.appendException(new CheckingException(
          "The minimal number of element in the list must be " + min + " but is " + size));
    }

    if (size > max) {
      exceptions.appendException(new CheckingException(
          "The maximal number of element in the list must be " + max + " but is " + size));
    }

    List<T> uniq = new ArrayList<>(list.size());
    ListIterator<T> iter = list.listIterator();
    while (iter.hasNext()) {
      int index = iter.nextIndex();
      T element = iter.next();
      try {
        parser.checkValue(context, element);
        if (unicity) {
          if (uniq.contains(element)) {
            exceptions.appendException(
                new ListCheckingException(index, "'" + parser.formatValue(element)
                    + "' is redundant with element #" + list.indexOf(element)));
          }
          uniq.add(element);
        }
      } catch (CheckingException e) {
        exceptions.appendException(new ListCheckingException(index, e));
      }
    }

    exceptions.throwIfNecessary();
  }

  @Override
  @Nonnull
  public String getUsage() {
    StringBuilder sb = new StringBuilder();
    String var = parser.getVariableName();

    if (min > 0) {
      sb.append('<').append(var).append("-1").append('>');

      if (min > 2) {
        sb.append(separator);
        sb.append("<...>");
      }

      if (min > 1) {
        sb.append(separator);
        sb.append('<').append(var).append('-').append(min).append('>');
      }
    }

    if (min < max) {
      StringBuilder end = new StringBuilder();

      sb.append('[');
      end.append(']');
      if (min > 0) {
        sb.append(separator);
      }
      sb.append('<').append(var).append('-').append(min + 1).append('>');

      if (max - min > 2) {
        sb.append('[');
        end.append(']');
        sb.append(separator);
        sb.append("...");
      }

      if (max - min  > 1) {
        sb.append('[');
        end.append(']');
        sb.append(separator);
        if (max == Integer.MAX_VALUE) {
          sb.append('<').append(var).append("-n").append('>');
        } else {
          sb.append('<').append(var).append('-').append(max).append('>');
        }
      }

      sb.append(end);
    }

    sb.append(" where <").append(var).append("-i> is ");
    sb.append(parser.getUsage());
    if (unicity) {
      sb.append(" and unique");
    }

    return sb.toString();
  }

  @Override
  @Nonnull
  public String getVariableName() {
    return "list";
  }

  @Override
  @Nonnull
  public List<ValueDescription> getValueDescriptions() {
    return parser.getValueDescriptions();
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull List<T> list) {
    StringBuilder sb = new StringBuilder();

    boolean first = true;
    for (T element : list) {
      if (first) {
        first = false;
      } else {
        sb.append(separator);
      }

      sb.append(parser.formatValue(element));
    }

    return sb.toString();
  }
}