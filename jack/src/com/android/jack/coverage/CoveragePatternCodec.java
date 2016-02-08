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

import com.android.jack.util.NamingTools;
import com.android.sched.util.codec.CheckingException;
import com.android.sched.util.codec.CodecContext;
import com.android.sched.util.codec.Parser;
import com.android.sched.util.codec.ParsingException;
import com.android.sched.util.codec.StringCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A {@link StringCodec} that creates a {@link CoveragePattern} containing the original string
 * and the corresponding {@link Pattern} for filtering.
 */
public class CoveragePatternCodec implements StringCodec<CoveragePattern> {
  @Override
  @Nonnull
  public CoveragePattern parseString(@Nonnull CodecContext context, @Nonnull String string) {
    return new CoveragePattern(string);
  }

  @Override
  @CheckForNull
  public CoveragePattern checkString(@Nonnull CodecContext context, @Nonnull String string)
      throws ParsingException {
    if (!NamingTools.isClassSourceName(fakeWildcards(string))) {
      throw new ParsingException("pattern \'" + string + "\' is not valid");
    }
    return parseString(context, string);
  }

  @Override
  @Nonnull
  public String getUsage() {
    return "a fully-qualified java class name (e.g. java.lang.Object)";
  }

  @Override
  @Nonnull
  public List<Parser.ValueDescription> getValueDescriptions() {
    List<Parser.ValueDescription> valueDescriptions = new ArrayList<Parser.ValueDescription>();
    valueDescriptions.add(new ValueDescription("*", "replaces multiple characters"));
    valueDescriptions.add(new ValueDescription("?", "replaces a single character"));
    return valueDescriptions;
  }

  @Override
  @Nonnull
  public String getVariableName() {
    return "pattern";
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull CoveragePattern data) {
    return data.getString();
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull CoveragePattern data)
      throws CheckingException {
    String string = data.getString();
    if (!NamingTools.isClassSourceName(fakeWildcards(string))) {
      throw new CheckingException("pattern \'" + string + "\' is not valid");
    }
  }

  /**
   * Replaces wildcards '*' and '?' with valid characters to fake a real class name.
   *
   * @param string a class name that may contain wildcard characters
   * @return a class name without wildcard characters
   */
  // package visible for testing.
  static String fakeWildcards(@Nonnull String string) {
    return string.replace('*', 'a').replace('?', 'b');
  }
}
