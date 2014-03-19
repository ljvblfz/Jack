/*
 * Copyright 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.android.jack.ir;



import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Describes where a SourceInfo's node came from. This class currently includes
 * only physical origin information, but could be extended to provide support
 * for source-Module and -Generators.
 *
 * TODO(gwt): make this package-protected?
 */
public class SourceOrigin implements SourceInfo {

  private static final long serialVersionUID = 1L;

  private static class SourceOriginPos extends SourceOrigin {

    private static final long serialVersionUID = 1L;

    private final int endCol;
    private final int startCol;

    private SourceOriginPos(@Nonnull String location, @Nonnegative int startLine,
        @Nonnegative int endLine, int startCol, int endCol) {
      super(location, startLine, endLine);
      this.startCol = startCol;
      this.endCol = endCol;
    }

    @Override
    public int getEndColumn() {
      return endCol;
    }

    @Override
    public int getStartColumn() {
      return startCol;
    }

    // super.equals and hashCode call getStartColumn() and getEndColumn(),
    // so there is no need to implement them in this subclass
    // FINDBUGS
    @Override
    public boolean equals(Object o) {
      return super.equals(o);
    }

    // FINDBUGS
    @Override
    public int hashCode() {
      return super.hashCode();
    }
  }

  @Nonnull
  public static final SourceOrigin UNKNOWN = new SourceOrigin("Unknown", 0, 0) {
    private static final long serialVersionUID = 1L;

    private Object readResolve() {
      return UNKNOWN;
    }
  };

  /**
   * Cache to reuse recently-created origins. This is very useful for JS nodes,
   * since {@code com.google.gwt.dev.js.JsParser} currently only provides line
   * numbers rather than character positions, so we get a lot of reuse there. We
   * get barely any reuse in the Java AST. Synchronized since several threads
   * could operate on it at once during parallel optimization phases.
   */
  private static final Map<SourceOrigin, SourceOrigin> CANONICAL_SOURCE_ORIGINS = Collections
      .synchronizedMap(new LinkedHashMap<SourceOrigin, SourceOrigin>(150, 0.75f, true) {
        private static final long serialVersionUID = 1L;

        @Override
        protected boolean removeEldestEntry(Entry<SourceOrigin, SourceOrigin> eldest) {
          return size() > 100;
        }
      });

  /**
   * Creates SourceOrigin nodes.
   */
  public static SourceOrigin create(int startCol, int endCol,
      @Nonnegative int startLine, @Nonnegative int endLine, @Nonnull String fileName) {
    if (startCol < 0 && endCol < 0) {
      return create(startLine, endLine, fileName);
    }

    return new SourceOriginPos(fileName, startLine, endLine, startCol, endCol);
  }

  /**
   * Creates SourceOrigin nodes. This factory method will attempt to provide
   * canonicalized instances of SourceOrigin objects.
   */
  public static SourceOrigin create(
      @Nonnegative int startLine, @Nonnegative int endLine, @Nonnull String fileName) {

    SourceOrigin newInstance = new SourceOrigin(fileName, startLine, endLine);
    SourceOrigin canonical = CANONICAL_SOURCE_ORIGINS.get(newInstance);

    assert canonical == null || (newInstance != canonical && newInstance.equals(canonical));

    if (canonical != null) {
      return canonical;
    } else {
      CANONICAL_SOURCE_ORIGINS.put(newInstance, newInstance);
      return newInstance;
    }
  }

  // TODO(gwt): Add Module and Generator tracking
  @Nonnull
  private final String fileName;
  @Nonnegative
  private final int startLine;
  @Nonnegative
  private final int endLine;

  private SourceOrigin(
      @Nonnull String location, @Nonnegative int startLine, @Nonnegative int endLine) {
    this.fileName = StringInterner.get().intern(location);
    this.startLine = startLine;
    this.endLine = endLine;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SourceOrigin)) {
      return false;
    }
    SourceOrigin other = (SourceOrigin) o;
    return startLine == other.startLine && getEndColumn() == other.getEndColumn()
        && getStartColumn() == other.getStartColumn() && fileName.equals(other.fileName);
  }

  @Override
  public int getEndColumn() {
    return -1;
  }

  @Override
  @Nonnull
  public String getFileName() {
    return fileName;
  }

  @Override
  @Nonnull
  public SourceOrigin getOrigin() {
    return this;
  }

  @Override
  @Nonnegative
  public int getStartLine() {
    return startLine;
  }

  @Override
  public int getStartColumn() {
    return -1;
  }

  @Override
  public int hashCode() {
    return 2 + 13 * fileName.hashCode() + 17 * startLine + 29 * getStartColumn() +
        31 * getEndColumn();
  }

  @Override
  @Nonnull
  public SourceInfo makeChild() {
    return this;
  }

  @Override
  public SourceInfo makeChild(SourceOrigin origin) {
    return origin;
  }

  @Override
  public String toString() {
    return getFileName() + '(' + getStartLine() + ')';
  }

  @Override
  @Nonnegative
  public int getEndLine() {
    return endLine;
  }
}