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

package com.android.jack.coverage;

import com.android.jack.ir.ast.JMethod;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * This class represents a Jacoco probe. A probe covers a sequence of instructions in a method.
 */
public class ProbeDescription {
  /**
   * The probe information for a line.
   */
  public static class ProbeLineData {
    private int nodesCount;
    private int branchesCount;

    public int getNodesCount() {
      return nodesCount;
    }

    public int getBranchesCount() {
      return branchesCount;
    }
  }

  @Nonnegative
  private final int probeId;

  @Nonnull
  private final JMethod method;

  @Nonnull
  private final Map<Integer, ProbeLineData> lineToData =
      new HashMap<Integer, ProbeDescription.ProbeLineData>();

  public ProbeDescription(@Nonnegative int probeId, @Nonnull JMethod method) {
    this.probeId = probeId;
    this.method = method;
  }

  @Nonnegative
  public int getProbeId() {
    return probeId;
  }

  @Nonnull
  public JMethod getMethod() {
    return method;
  }

  @Nonnull
  public Map<Integer, ProbeLineData> getLineToData() {
    return lineToData;
  }

  public void incrementLine(
      @Nonnegative int line, @Nonnegative int nodesCount, boolean branchNode) {
    ProbeLineData probeData = lineToData.get(Integer.valueOf(line));
    if (probeData == null) {
      probeData = new ProbeLineData();
      lineToData.put(Integer.valueOf(line), probeData);
    }
    if (branchNode) {
      probeData.branchesCount += nodesCount;
    } else {
      probeData.nodesCount += nodesCount;
    }
  }

  @Override
  public String toString() {
    return "Probe " + probeId;
  }
}
