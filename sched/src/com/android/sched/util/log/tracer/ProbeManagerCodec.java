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

package com.android.sched.util.log.tracer;

import com.android.sched.util.codec.CodecContext;
import com.android.sched.util.codec.ImplementationSelector;
import com.android.sched.util.codec.ListCodec;
import com.android.sched.util.codec.ParsingException;
import com.android.sched.util.codec.StringCodec;
import com.android.sched.util.log.tracer.probe.Probe;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to create an instance of {@link ProbeManager}
 */
public class ProbeManagerCodec implements StringCodec<ProbeManager> {
  @Nonnull
  private final ListCodec<Probe> parser;

  public ProbeManagerCodec() {
    parser = new ListCodec<Probe>("probe", new ImplementationSelector<Probe>(Probe.class))
        .setMin(1);
  }

  @Override
  @CheckForNull
  public ProbeManager checkString(@Nonnull CodecContext context, @Nonnull String string)
      throws ParsingException {
    List<Probe> probes = parser.checkString(context, string);
    if (probes != null) {
      ProbeManagerBuilder builder = new ProbeManagerBuilder();

      for (Probe probe : probes) {
        builder.add(probe);
      }

      return builder.build();
    } else {
      return null;
    }
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull ProbeManager data) {
  }

  @Override
  @Nonnull
  public ProbeManager parseString(@Nonnull CodecContext context, @Nonnull String string) {
    List<Probe> probes = parser.parseString(context, string);
    ProbeManagerBuilder builder = new ProbeManagerBuilder();

    for (Probe probe : probes) {
      builder.add(probe);
    }

    return builder.build();
  }

  @Override
  @Nonnull
  public String getUsage() {
    return parser.getUsage();
  }

  @Override
  @Nonnull
  public List<com.android.sched.util.codec.Parser.ValueDescription> getValueDescriptions() {
    return parser.getValueDescriptions();
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull ProbeManager data) {
    return parser.formatValue(data.getProbes());
  }
}
