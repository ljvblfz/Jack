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

package com.android.jack.tools.jacoco;

import com.google.gson.stream.JsonWriter;

import junit.framework.Assert;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICoverageVisitor;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class JackCoverageAnalyzerTest {
  private static final long ID = 12345L;
  private static final String NAME = "foo/bar/MyClass";
  private static final String SUPER_CLASS_NAME = "java/lang/Object";
  private static final boolean[] PROBES = {true, false, true, false};

  @Test
  public void testAnalysis() throws IOException {
    byte[] buffer = createJsonBuffer();

    ICoverageVisitor visitor = new ICoverageVisitor() {
      @Override
      public void visitCoverage(IClassCoverage arg0) {
        Assert.assertEquals(ID, arg0.getId());
        Assert.assertEquals(NAME, arg0.getName());
        Assert.assertEquals(SUPER_CLASS_NAME, arg0.getSuperName());
      }
    };

    ExecutionDataStore executionDataStore = createExecutionDataStore();
    JackCoverageAnalyzer jackCoverageAnalyzer =
        new JackCoverageAnalyzer(executionDataStore, visitor);

    ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
    try {
      jackCoverageAnalyzer.analyze(bais);
    } finally {
      bais.close();
    }
  }

  private ExecutionDataStore createExecutionDataStore() {
    ExecutionDataStore executionDataStore = new ExecutionDataStore();
    ExecutionData executionData = executionDataStore.get(Long.valueOf(ID), NAME, PROBES.length);
    for (int i = 0, e = PROBES.length; i < e; ++i) {
      executionData.getProbes()[i] = PROBES[i];
    }
    return executionDataStore;
  }

  // Create the minimal JSON for the class.
  private byte[] createJsonBuffer() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    JsonWriter w = new JsonWriter(new OutputStreamWriter(baos, "UTF-8"));
    try {
      w.beginObject();
      w.name(JackCoverageAnalyzer.JSON_VERSION_ATTRIBUTE)
          .value(JackCoverageAnalyzer.CURRENT_VERSION);
      w.name(JackCoverageAnalyzer.JSON_DATA_ATTRIBUTE).beginArray();
      w.beginObject();
      w.name("id").value(ID);
      w.name("name").value(NAME);
      w.name("superClassName").value(SUPER_CLASS_NAME);
      w.name("methods").beginArray().endArray();
      w.name("probes").beginArray();
      for (int i = 0, e = PROBES.length; i < e; ++i) {
        w.beginObject().name("id").value(i).endObject();
      }
      w.endArray();
      w.endObject();
      w.endArray();
      w.endObject();
      w.flush();
    } finally {
      w.close();
    }

    return baos.toByteArray();
  }
}
