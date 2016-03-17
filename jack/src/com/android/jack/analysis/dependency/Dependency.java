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

import com.google.common.base.Joiner;
import com.google.common.io.LineReader;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Tools related to dependencies support.
 */
public abstract class Dependency {

  @Nonnull
  public static final String DEPENDENCY_FILE_EXTENSION = ".dep";

  @Nonnull
  protected static final String END_OF_MAP = "#";

  private static final char LIST_VALUE_SEPARATOR = ',';

  private static final char MAP_VALUE_SEPARATOR = ',';

  @Nonnull
  private static final Joiner mapValueJoiner = Joiner.on(MAP_VALUE_SEPARATOR);

  @Nonnull
  private static final Joiner listValueJoiner = Joiner.on(LIST_VALUE_SEPARATOR).useForNull("");

  /**
   * Line parser
   */
  private static class LineParser {
    @Nonnull
    private final String line;

    @Nonnegative
    private int lineCharIdx = 0;

    public LineParser(@Nonnull String line) {
      this.line = line;
    }

    public boolean hasNextToken() {
      if (lineCharIdx >= line.length()) {
        return false;
      }
      return true;
    }

    @Nonnull
    public String nextToken(char separator) {
      if (lineCharIdx >= line.length()) {
        throw new NoSuchElementException();
      }

      int nextSeparatorIndex = line.indexOf(separator, lineCharIdx);
      if (nextSeparatorIndex == -1) {
        String result = line.substring(lineCharIdx, line.length());
        lineCharIdx = line.length() + 1;
        return result;
      } else {
        String result = line.substring(lineCharIdx, nextSeparatorIndex);
        lineCharIdx = nextSeparatorIndex + 1;
        return result;
      }
    }
  }

  @Nonnull
  protected Map<String, Set<String>> readMapOne2Many(@Nonnull LineReader lr)
      throws IOException {
    Map<String, Set<String>> one2many = new HashMap<String, Set<String>>();
    String line;

    while ((line = lr.readLine()) != null && !line.equals(END_OF_MAP)) {
      Set<String> values = new HashSet<String>();
      String key = line;
      line = lr.readLine();
      assert line != null;
      LineParser lp = new LineParser(line);
      while (lp.hasNextToken()) {
        values.add(lp.nextToken(MAP_VALUE_SEPARATOR));
      }
      one2many.put(key, values);
    }

    return one2many;
  }

  /**
   * Maps are written as below into dependency files:
   * key1
   * value1, ...
   * key2
   * value1, ...
   * key3
   * blank line if no value
   * #
   *
   * We do not longer generate key and values on the same line, because one map contains file path
   * as key and it allows to avoid conflict between the key/values separator and character allowed
   * into file path.
   */
  protected void writeMapOne2Many(@Nonnull PrintStream ps,
      @Nonnull Map<String, Set<String>> one2many) {
    for (Map.Entry<String, Set<String>> entry : one2many.entrySet()) {
      ps.print(entry.getKey());
      ps.println();
      StringBuffer sb = new StringBuffer();
      sb.append(mapValueJoiner.join(entry.getValue().iterator()));
      ps.print(sb.toString());
      ps.println();
    }
  }

  protected void writeList(@Nonnull PrintStream ps, @Nonnull List<String> list) {
    ps.print(listValueJoiner.join(list.iterator()));
  }

  @Nonnull
  protected List<String> readList(@Nonnull LineReader lr) throws IOException {
    List<String> digestValues = new ArrayList<String>();
    String line = lr.readLine();

    LineParser lp = new LineParser(line);
    while (lp.hasNextToken()) {
      digestValues.add(lp.nextToken(LIST_VALUE_SEPARATOR));
    }

    return digestValues;
  }

  public abstract void read(@Nonnull Readable readable) throws IOException;
}
