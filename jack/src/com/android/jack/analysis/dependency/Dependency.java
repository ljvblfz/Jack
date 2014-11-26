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

package com.android.jack.analysis.dependency;

import com.google.common.io.LineReader;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Tools related to dependencies support.
 */
public abstract class Dependency {

  @Nonnull
  public static final String MAP_SEPARATOR = "#";

  public static final char MAP_VALUE_SEPARATOR = ',';

  public static final char MAP_KEY_VALUE_SEPARATOR = ':';

  private static class LineParser {
    @Nonnull
    private final String line;

    @Nonnegative
    private int lineCharIdx = 0;

    LineParser(@Nonnull String line) {
      this.line = line;
    }

    @CheckForNull
    private String nextToken(char separator) {
      if (lineCharIdx >= line.length()) {
        return null;
      }

      char c;
      StringBuffer token = new StringBuffer();
      while (lineCharIdx < line.length() && (c = line.charAt(lineCharIdx)) != separator) {
        token.append(c);
        lineCharIdx++;
      }
      lineCharIdx++;
      return token.toString();
    }
  }

  @Nonnull
  protected Map<String, Set<String>> readMapOne2Many(@Nonnull LineReader lr)
      throws IOException {
    Map<String, Set<String>> one2many = new HashMap<String, Set<String>>();
    String line;

    while ((line = lr.readLine()) != null && !line.equals(MAP_SEPARATOR)) {
      Set<String> values = new HashSet<String>();
      LineParser lp = new LineParser(line);
      String key = lp.nextToken(MAP_KEY_VALUE_SEPARATOR);
      assert key != null;
      String value = lp.nextToken(MAP_VALUE_SEPARATOR);
      while (value != null) {
        values.add(value);
        value = lp.nextToken(MAP_VALUE_SEPARATOR);
      }
      one2many.put(key, values);
    }

    return one2many;
  }

  protected void writeMapOne2Many(@Nonnull PrintStream ps,
      @Nonnull Map<String, Set<String>> one2many) {
    for (Map.Entry<String, Set<String>> entry : one2many.entrySet()) {
      StringBuffer sb = new StringBuffer();
      sb.append(entry.getKey());
      sb.append(MAP_KEY_VALUE_SEPARATOR);
      Iterator<String> itValues = entry.getValue().iterator();
      while (itValues.hasNext()) {
        sb.append(itValues.next());
        if (itValues.hasNext()) {
          sb.append(MAP_VALUE_SEPARATOR);
        }
      }
      ps.print(sb.toString());
      ps.println();
    }
  }
}
