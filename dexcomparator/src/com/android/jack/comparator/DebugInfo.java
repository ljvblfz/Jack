/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.comparator;

import com.android.jack.dx.dex.file.DebugInfoDecoder;
import com.android.jack.dx.dex.file.DebugInfoDecoder.LocalEntry;
import com.android.jack.dx.dex.file.DebugInfoDecoder.PositionEntry;
import com.android.jack.dx.io.Code;
import com.android.jack.dx.io.DexBuffer;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Debug information for one method.
 */
public class DebugInfo {

  /**
   * The scope of a {@link LocalVar}.
   */
  public static class Interval {
    private int start;
    private int end;
    private int pendingCloseCount;

    public Interval(int start) {
      this.start = start;
      this.end = start;
      pendingCloseCount = 1;
    }

    public Interval(int start, int end) {
      this.start = start;
      this.end = end;
    }

    /**
     * @return the end
     */
    public int getEnd() {
      return end;
    }

    /**
     * @return the start
     */
    public int getStart() {
      return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(int start) {
      augment(start);
      pendingCloseCount++;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(int end) {
      augment(end);
      pendingCloseCount--;
      assert pendingCloseCount >= 0;
    }

    @Override
    public String toString() {
      return "[" + start + ", " + end + "]";
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof Interval) {
        Interval other = (Interval) obj;
        return start == other.start && end == other.end;
      }
      return false;
    }

    @Override
    public int hashCode() {
      return 7529 * start + 11113 * end;
    }

    /**
     * Increase this interval so that it includes the new limit.
     */
    private void augment(int limit) {
      if (this.end < limit) {
        this.end = limit;
      }
      if (this.start > limit) {
        this.start = limit;
      }
    }

    private boolean isClosed() {
      return pendingCloseCount == 0;
    }
  }

  /**
   * Debugging information for a local variable.
   */
  public static class LocalVar {
    @Nonnull
    private final String name;
    @CheckForNull
    private final String typeSignature;

    @Nonnull
    private final Interval scope;

    @Nonnull
    private static final char NON_SOURCE_CONFLICTING_CHAR = '-';

    private LocalVar(@Nonnull String name, @CheckForNull String typeSignature, int start) {
      this.name = name;
      this.typeSignature = typeSignature;
      scope = new Interval(start);
    }

    @Nonnull
    public String getName() {
      return name;
    }

    @CheckForNull
    public String getTypeSignature() {
      return typeSignature;
    }

    @Nonnull
    public Interval getScope() {
      return scope;
    }

    public boolean isSynthetic() {
      return name.indexOf(NON_SOURCE_CONFLICTING_CHAR) != -1;
    }

    @Nonnull
    @Override
    public String toString() {
      return typeSignature + " " + name + " " + scope;
    }

    void addInterval(int start) {
      scope.setStart(start);
    }

    void closeInterval(int end) {
      scope.setEnd(end);
    }
  }

  private static final int NO_INDEX = -1;

  private List<PositionEntry> lines;
  private final HashMap<String, LocalVar> locals = new HashMap<String, DebugInfo.LocalVar>();
  private DexBuffer dex;
  private int sizeInBytes;
  private int debugInfoOffset;

  public DebugInfo(DebugInfoDecoder decoder, DexBuffer dex, Code codeItem, int sizeInbytes) {
    this.dex = dex;
    this.sizeInBytes = sizeInbytes;
    debugInfoOffset = codeItem.getDebugInfoOffset();
    lines = decoder.getPositionList();
    Collections.sort(lines, new Comparator<PositionEntry>() {
      @Override
      public int compare(PositionEntry o1, PositionEntry o2) {
        return o2.address > o1.address ? -1 : (o2.address == o1.address ? 0 : 1);
      }});

    LocalVar[] regs = new LocalVar[codeItem.getRegistersSize()];

    for (LocalEntry localEntry : decoder.getLocals()) {

        int reg = localEntry.reg;
        int line = getLine(localEntry.address - 1);
        if (localEntry.isStart && regs[reg] != null) {
            regs[reg].closeInterval(line);
            regs[reg] = null;
        }

        if (localEntry.nameIndex != NO_INDEX) {

          LocalVar localVar = getOrCreate(localEntry);

          if (localEntry.isStart) {
            localVar.addInterval(line);
            regs[reg] = localVar;
          } else {
            localVar.closeInterval(line);
            regs[reg] = null;
          }
        }
    }

    int endLine = -1;
    for (PositionEntry line : lines) {
      endLine = Math.max(line.line, endLine);
    }
    for (LocalVar localVar : locals.values()) {
        if (!localVar.getScope().isClosed()) {
            localVar.closeInterval(endLine);
        }
    }
  }

  private LocalVar getOrCreate(LocalEntry localEntry) {
    String name = dex.strings().get(localEntry.nameIndex);
    String desc = null;
    if (localEntry.signatureIndex != NO_INDEX) {
      desc = dex.strings().get(localEntry.signatureIndex);
    } else if (localEntry.typeIndex != NO_INDEX) {
      desc = dex.typeNames().get(localEntry.typeIndex);
    }

    String key = name + desc;
    LocalVar local = locals.get(key);
    if (local == null) {
      assert localEntry.isStart;
      local = new LocalVar(name, desc, getLine(localEntry.address));
      locals.put(key, local);
    }
    return local;
  }

  public int getLine(int address) {
    int line = -1;
    for (PositionEntry entry : lines) {
      if (entry.address > address) {
        if (line == -1) {
          line = entry.line;
        }
        break;
      } else {
        line = entry.line;
      }
    }
    return line;
  }

  public Collection<LocalVar> getLocals() {
    return locals.values();
  }

  public LocalVar getLocal(LocalVar other) {
    return getLocal(other.getName(), other.getTypeSignature());
  }

  public LocalVar getLocal(String name, String desc) {
    return locals.get(name + desc);
  }

  public int getSizeInBytes() {
    return sizeInBytes;
  }

  public int getDebugInfoOffset() {
    return debugInfoOffset;
  }
}
