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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This class defines methods that are use to remap type names and paths according to a list of
 * {@link Wildcard} built from patterns found in rule file.
 */
public class PackageRemapper {
  @Nonnull
  private final List<Wildcard> wildcards;

  @Nonnull
  private static final String RESOURCE_SUFFIX = "RESOURCE";
  @Nonnull
  private static final Pattern ARRAY_FOR_NAME_PATTERN =
      Pattern.compile("\\[L[\\p{javaJavaIdentifierPart}\\.]+?;");
  @Nonnull
  private final Map<String, String> typeCache = new HashMap<String, String>();
  @Nonnull
  private final Map<String, String> pathCache = new HashMap<String, String>();
  @Nonnull
  private final Map<Object, String> valueCache = new HashMap<Object, String>();

  public PackageRemapper(@Nonnull List<Wildcard> wildcards) {
    this.wildcards = wildcards;
  }

  static boolean isArrayForName(@Nonnull String value) {
    return ARRAY_FOR_NAME_PATTERN.matcher(value).matches();
  }

  @CheckForNull
  private String map(@Nonnull String key) {
    String s = typeCache.get(key);
    if (s == null) {
      s = replaceHelper(key);
      if (key.equals(s)) {
        s = null;
      }
      typeCache.put(key, s);
    }
    return s;
  }

  @Nonnull
  public String mapPath(@Nonnull String path) {
    String s = pathCache.get(path);
    if (s == null) {
      s = path;
      int slash = s.lastIndexOf('/');
      String end;
      if (slash < 0) {
        end = s;
        s = RESOURCE_SUFFIX;
      } else {
        end = s.substring(slash + 1);
        s = s.substring(0, slash + 1) + RESOURCE_SUFFIX;
      }
      boolean absolute = s.startsWith("/");
      if (absolute) {
        s = s.substring(1);
      }

      s = replaceHelper(s);

      if (absolute) {
        s = "/" + s;
      }
      if (s.indexOf(RESOURCE_SUFFIX) < 0) {
        return path;
      }
      s = s.substring(0, s.length() - RESOURCE_SUFFIX.length()) + end;
      pathCache.put(path, s);
    }
    return s;
  }

  @Nonnull
  public String mapValue(@Nonnull String value) {
    String s = valueCache.get(value);
    if (s == null) {
      s = value;
      if (isArrayForName(s)) {
        String desc1 = s.replace('.', '/');
        String desc2 = mapDesc(desc1);
        if (!desc2.equals(desc1)) {
          return desc2.replace('/', '.');
        }
      } else {
        s = mapPath(s);
        if (s.equals(value)) {
          boolean hasDot = s.indexOf('.') >= 0;
          boolean hasSlash = s.indexOf('/') >= 0;
          if (!(hasDot && hasSlash)) {
            if (hasDot) {
              s = replaceHelper(s.replace('.', '/')).replace('/', '.');
            } else {
              s = replaceHelper(s);
            }
          }
        }
      }
      valueCache.put(value, s);
    }
    return s;
  }

  @Nonnull
  private String replaceHelper(@Nonnull String value) {
    for (Wildcard wildcard : wildcards) {
      String test = wildcard.replace(value);
      if (test != null) {
        return test;
      }
    }
    return value;
  }

  @Nonnull
  private String mapDesc(@Nonnull String desc) {
    if (desc.startsWith("[")) {
      return "[" + mapDesc(desc.substring(1));
    } else if (desc.startsWith("L")) {
      assert desc.endsWith(";");
      String newDesc = map(desc.substring(1, desc.length() - 1));
      if (newDesc != null) {
        return "L" + newDesc + ";";
      }
    }
    return desc;
  }
}
