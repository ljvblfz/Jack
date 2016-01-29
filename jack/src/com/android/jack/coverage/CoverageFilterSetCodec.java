/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.coverage;

import com.android.sched.util.codec.CheckingException;
import com.android.sched.util.codec.CodecContext;
import com.android.sched.util.codec.ListCodec;
import com.android.sched.util.codec.ParsingException;
import com.android.sched.util.codec.StringCodec;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A {@link StringCodec} that creates a {@link CoverageFilterSet} from a list of class names.
 */
public class CoverageFilterSetCodec implements StringCodec<CoverageFilterSet> {
  @Nonnull
  private final ListCodec<CoveragePattern> parser;

  public CoverageFilterSetCodec() {
    parser = new ListCodec<CoveragePattern>(new CoveragePatternCodec());
    parser.setMin(0);
  }

  @Override
  @Nonnull
  public CoverageFilterSet parseString(@Nonnull CodecContext context, @Nonnull String string) {
    List<CoveragePattern> patterns = parser.parseString(context, string);
    return createFromPatterns(patterns);
  }

  @Override
  @CheckForNull
  public CoverageFilterSet checkString(@Nonnull CodecContext context, @Nonnull String string)
      throws ParsingException {
    List<CoveragePattern> patterns = parser.checkString(context, string);
    return createFromPatterns(patterns);
  }

  private static CoverageFilterSet createFromPatterns(
      @Nonnull List<? extends CoveragePattern> patterns) {
    CoverageFilterSet filterSet = new CoverageFilterSet();
    for (CoveragePattern pattern : patterns) {
      filterSet.addPattern(pattern);
    }
    return filterSet;
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
  public String getVariableName() {
    return parser.getVariableName();
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull CoverageFilterSet data) {
    return parser.formatValue(data.getPatterns());
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull CoverageFilterSet data)
      throws CheckingException {
    parser.checkValue(context, data.getPatterns());
  }
}
