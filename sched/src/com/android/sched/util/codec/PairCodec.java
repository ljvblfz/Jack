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

package com.android.sched.util.codec;

import com.android.sched.util.codec.PairCodec.Pair;
import com.android.sched.util.config.ChainedException.ChainedExceptionBuilder;
import com.android.sched.util.config.ConfigurationError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to create an instance of {@link Entry}
 */
public class PairCodec<T, U> implements StringCodec<Pair<T, U>>{

  /**
   * A pair.
   */
  public static class Pair<T, U> {
    @Nonnull
    private final T first;
    @Nonnull
    private final U second;

    public Pair(@Nonnull T first, @Nonnull U second) {
      this.first = first;
      this.second = second;
    }

    @Nonnull
    public T getFirst() {
      return first;
    }

    @Nonnull
    public U getSecond() {
      return second;
    }

    @Override
    public final boolean equals(Object obj) {
      if (obj instanceof Pair) {
        return first.equals(((Pair<?, ?>) obj).first) && second.equals(((Pair<?, ?>) obj).second);
      }
      return super.equals(obj);
    }

    @Override
    public final int hashCode() {
      return first.hashCode() ^ second.hashCode();
    }
  }

  @Nonnull
  private final StringCodec<T> keyParser;
  @Nonnull
  private final StringCodec<U> valueParser;

  @Nonnull
  private String separator = "=";

  public PairCodec(@Nonnull StringCodec<T> keyParser, @Nonnull StringCodec<U> valueParser) {
    this.keyParser = keyParser;
    this.valueParser = valueParser;
  }

  @Nonnull
  public PairCodec<T, U> on(@Nonnull String separator) {
    this.separator = separator;

    return this;
  }

  @Nonnull
  public String getSeparator() {
    return separator;
  }

  @Override
  @Nonnull
  public Pair<T, U> parseString(@Nonnull CodecContext context, @Nonnull String string) {
    int endKey = string.indexOf(separator);
    if (endKey == -1) {
      throw new ConfigurationError("Missing '" + separator + "' in '" + string + "'");
    }
    String key = string.substring(0, endKey);
    String valueString = string.substring(endKey + separator.length());
    return new Pair<T, U>(keyParser.parseString(context, key),
        valueParser.parseString(context, valueString));
  }

  @Override
  @CheckForNull
  public Pair<T, U> checkString(@Nonnull CodecContext context,
      @Nonnull String string)
      throws ParsingException {
    ChainedExceptionBuilder<ParsingException> exceptions =
        new ChainedExceptionBuilder<ParsingException>();

    T keyElement = null;
    U valueElement = null;
    String key = null;
    String valueString = null;
    int endKey = string.indexOf(separator);
    if (endKey == -1) {
      exceptions.appendException(new ParsingException("Missing '"
          + separator + "' in '" + string + "'"));
    } else {
      key = string.substring(0, endKey);
      valueString = string.substring(endKey + separator.length());
      try {
        keyElement = keyParser.checkString(context, key);
      } catch (ParsingException e) {
        exceptions.appendException(e);
      }
      try {
        valueElement = valueParser.checkString(context, valueString);
      } catch (ParsingException e) {
        exceptions.appendException(e);
      }
    }

    exceptions.throwIfNecessary();
    // If the two elements are null, do not compute the pair
    if (keyElement == null && valueElement == null) {
      return null;
    } else {
      // Else, complete the pair, and return it
      if (keyElement == null) {
        assert key != null;
        keyElement = keyParser.parseString(context, key);
      }

      if (valueElement == null) {
        assert valueString != null;
        valueElement = valueParser.parseString(context, valueString);
      }

      return new Pair<T, U>(keyElement, valueElement);
    }
  }

  @Override
  @Nonnull
  public String getUsage() {
    StringBuilder sb = new StringBuilder();
    sb.append('<');
    sb.append(keyParser.getVariableName());
    sb.append('>');
    sb.append(separator);
    sb.append('<');
    sb.append(valueParser.getVariableName());
    sb.append("> where <");
    sb.append(keyParser.getVariableName());
    sb.append("> is ");
    sb.append(keyParser.getUsage());
    sb.append(" and where <");
    sb.append(valueParser.getVariableName());
    sb.append("> is ");
    sb.append(valueParser.getUsage());

    return sb.toString();
  }

  @Override
  @Nonnull
  public String getVariableName() {
    return "pair";
  }

  @Override
  @Nonnull
  public List<com.android.sched.util.codec.Parser.ValueDescription> getValueDescriptions() {
    List<com.android.sched.util.codec.Parser.ValueDescription> list =
        new ArrayList<Parser.ValueDescription>();
    list.addAll(keyParser.getValueDescriptions());
    list.addAll(valueParser.getValueDescriptions());
    return list;
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull Pair<T, U> data) {
    StringBuilder sb = new StringBuilder();

    sb.append(keyParser.formatValue(data.getFirst()));
    sb.append(separator);
    sb.append(valueParser.formatValue(data.getSecond()));
    return sb.toString();
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull Pair<T, U> data)
      throws CheckingException {
    ChainedExceptionBuilder<CheckingException> exceptions =
        new ChainedExceptionBuilder<CheckingException>();
    try {
      keyParser.checkValue(context, data.getFirst());
    } catch (CheckingException e) {
      exceptions.appendException(e);
    }
    try {
      valueParser.checkValue(context, data.getSecond());
    } catch (CheckingException e) {
      exceptions.appendException(e);
    }

    exceptions.throwIfNecessary();
  }
}
