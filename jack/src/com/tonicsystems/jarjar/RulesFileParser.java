/**
 * Copyright 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tonicsystems.jarjar;

import com.android.sched.util.file.InputStreamFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A parser that returns the list of {@link PatternElement}s contained in a jarjar rule file.
 */
public class RulesFileParser {
  private RulesFileParser() {}

  public static List<PatternElement> parse(InputStreamFile file) throws IOException {
    return parse(new InputStreamReader(file.getInputStream()));
  }

  public static List<PatternElement> parse(String value) throws IOException {
    return parse(new java.io.StringReader(value));
  }

  private static String stripComment(String in) {
    int p = in.indexOf("#");
    return p < 0 ? in : in.substring(0, p);
  }

  public static List<PatternElement> parse(Reader r) throws IOException {
    try {
      List<PatternElement> patterns = new ArrayList<PatternElement>();
      BufferedReader br = new BufferedReader(r);
      int c = 1;
      String line;
      while ((line = br.readLine()) != null) {
        line = stripComment(line);
        if (line.isEmpty()) {
          continue;
        }
        String[] parts = line.split("\\s+");
        if (parts.length < 2) {
          error(c, parts);
        }
        String type = parts[0];
        PatternElement element = null;
        if (type.equals("rule")) {
          if (parts.length < 3) {
            error(c, parts);
          }
          Rule rule = new Rule();
          rule.setResult(parts[2]);
          element = rule;
        } else if (type.equals("zap")) {
          continue;
        } else if (type.equals("keep")) {
          continue;
        } else {
          error(c, parts);
        }
        assert element != null;
        element.setPattern(parts[1]);
        patterns.add(element);
        c++;
      }
      return patterns;
    } finally {
      r.close();
    }
  }

  private static void error(int line, String[] parts) {
    throw new IllegalArgumentException("Error on line " + line + ": " + Arrays.asList(parts));
  }
}
