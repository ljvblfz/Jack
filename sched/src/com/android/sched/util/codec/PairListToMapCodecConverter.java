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

package com.android.sched.util.codec;

import com.android.sched.util.config.ChainedException.ChainedExceptionBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

/**
 * A {@link ConvertCodec} allowing to convert list of {@link Entry} into a map.
 */
public class PairListToMapCodecConverter<T, U> extends
    ConvertCodec<List<Entry<T, U>>, Map<T, U>> {
  public PairListToMapCodecConverter(StringCodec<List<Entry<T, U>>> codec) {
    super(codec);
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull Map<T, U> data) {
    return codec.formatValue(new ArrayList<Map.Entry<T, U>>(data.entrySet()));
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull Map<T, U> data)
      throws CheckingException {
    codec.checkValue(context, new ArrayList<Map.Entry<T, U>>(data.entrySet()));
  }

  @Override
  @Nonnull
  protected Map<T, U> convert(@Nonnull List<Entry<T, U>> list) throws ParsingException {
    ChainedExceptionBuilder<ParsingException> exceptions =
        new ChainedExceptionBuilder<ParsingException>();
    Map<T, U> map = new HashMap<T, U>(Math.round(list.size() / 0.75f), 0.75f);

    int index = 1;
    for (Entry<T, U> entry : list) {
      if (!map.containsKey(entry.getKey())) {
        map.put(entry.getKey(), entry.getValue());
      } else {
        exceptions.appendException(new ListParsingException(index, "Value for '"
            + entry.getKey() + "' is already defined"));
      }
      index++;
    }
    exceptions.throwIfNecessary();

    return map;
  }
}