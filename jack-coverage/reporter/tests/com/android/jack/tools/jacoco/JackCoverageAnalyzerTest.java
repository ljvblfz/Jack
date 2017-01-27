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
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import javax.annotation.Nonnull;

public class JackCoverageAnalyzerTest {
  private static final long ID = 12345L;
  private static final String CLASS_NAME = "foo/bar/MyClass";
  private static final String OBFUSCATED_CLASS_NAME = "a/A";
  private static final String METHOD_DECLARATION = "void oldMethod(foo.bar.MyClass)";
  private static final String METHOD_SIGNATURE = "oldMethod(Lfoo/bar/MyClass;)V";
  private static final String OBFUSCATED_METHOD_NAME = "newMethod";
  private static final String OBFUSCATED_METHOD_SIGNATURE = "newMethod(La/A;)V";
  private static final String SUPER_CLASS_NAME = "java/lang/Object";
  private static final boolean[] PROBES = {true, false, true, false};

  @Test
  public void testAnalysis() throws IOException {
    byte[] buffer = createSimpleJsonBuffer(CLASS_NAME, METHOD_SIGNATURE);

    ICoverageVisitor visitor = new ICoverageVisitor() {
      @Override
      public void visitCoverage(IClassCoverage arg0) {
        // Check class
        Assert.assertEquals(ID, arg0.getId());
        Assert.assertEquals(CLASS_NAME, arg0.getName());
        Assert.assertEquals(SUPER_CLASS_NAME, arg0.getSuperName());

        // Check method
        Assert.assertEquals(1, arg0.getMethods().size());
        IMethodCoverage methodCoverage = arg0.getMethods().iterator().next();
        Assert.assertNotNull(methodCoverage);
        String[] nameAndDesc = splitSignatureInNameAndDesc(METHOD_SIGNATURE);
        Assert.assertEquals(nameAndDesc[0], methodCoverage.getName());
        Assert.assertEquals(nameAndDesc[1], methodCoverage.getDesc());
      }
    };

    ExecutionDataStore executionDataStore = createExecutionDataStore(CLASS_NAME);
    JackCoverageAnalyzer jackCoverageAnalyzer =
        new JackCoverageAnalyzer(executionDataStore, visitor, null /* no mapping file */);

    ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
    try {
      jackCoverageAnalyzer.analyze(bais);
    } finally {
      bais.close();
    }
  }

  @Test
  public void testAnalysisWithIdentityMappingFile() throws IOException {
    byte[] buffer = createSimpleJsonBuffer(CLASS_NAME, METHOD_SIGNATURE);

    ICoverageVisitor visitor = new ICoverageVisitor() {
      @Override
      public void visitCoverage(IClassCoverage arg0) {
        Assert.assertEquals(ID, arg0.getId());
        Assert.assertEquals(CLASS_NAME, arg0.getName());
        Assert.assertEquals(SUPER_CLASS_NAME, arg0.getSuperName());
      }
    };

    MappingFileLoader loader = MappingFileLoaderTest.loadMappingFile(
        String.format("%s -> %s:",
            NamingUtils.binaryNameToFqName(CLASS_NAME), NamingUtils.binaryNameToFqName(CLASS_NAME))
    );

    ExecutionDataStore executionDataStore = createExecutionDataStore(CLASS_NAME);
    JackCoverageAnalyzer jackCoverageAnalyzer =
        new JackCoverageAnalyzer(executionDataStore, visitor, loader);

    ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
    try {
      jackCoverageAnalyzer.analyze(bais);
    } finally {
      bais.close();
    }
  }

  /**
   * Simulate shrinking where the class is not in the mapping file
   */
  @Test
  public void testAnalysisWithClassShrinking() throws IOException {
    byte[] buffer = createSimpleJsonBuffer(CLASS_NAME, METHOD_SIGNATURE);

    ICoverageVisitor visitor = new ICoverageVisitor() {
      @Override
      public void visitCoverage(IClassCoverage arg0) {
        // The only class in the file should be ignored because it does not appear in the mapping
        // file
        Assert.fail("Unexpected visit: class is shrunk");
      }
    };

    MappingFileLoader loader = MappingFileLoaderTest.loadMappingFile(
        String.format("Foo -> Foo:")
    );

    ExecutionDataStore executionDataStore = createExecutionDataStore(CLASS_NAME);
    JackCoverageAnalyzer jackCoverageAnalyzer =
        new JackCoverageAnalyzer(executionDataStore, visitor, loader);

    ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
    try {
      jackCoverageAnalyzer.analyze(bais);
    } finally {
      bais.close();
    }
  }

  /**
   * Simulate shrinking where the method is not in the mapping file
   */
  @Test
  public void testAnalysisWithMethodShrinking() throws IOException {
    byte[] buffer = createSimpleJsonBuffer(CLASS_NAME, METHOD_SIGNATURE);

    ICoverageVisitor visitor = new ICoverageVisitor() {
      @Override
      public void visitCoverage(IClassCoverage arg0) {
        Assert.assertEquals(ID, arg0.getId());
        Assert.assertEquals(CLASS_NAME, arg0.getName());
        Assert.assertEquals(SUPER_CLASS_NAME, arg0.getSuperName());
        // We must not find the method.
        Assert.assertTrue(arg0.getMethods().isEmpty());
      }
    };

    String classBinaryName = NamingUtils.binaryNameToFqName(CLASS_NAME);
    MappingFileLoader loader = MappingFileLoaderTest.loadMappingFile(
        String.format("%s -> %s:", classBinaryName, classBinaryName),
        String.format("void nonExistentMethod() -> nonExistentMethod")
    );

    ExecutionDataStore executionDataStore = createExecutionDataStore(CLASS_NAME);
    JackCoverageAnalyzer jackCoverageAnalyzer =
        new JackCoverageAnalyzer(executionDataStore, visitor, loader);

    ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
    try {
      jackCoverageAnalyzer.analyze(bais);
    } finally {
      bais.close();
    }
  }

  /**
   * Simulate obfuscated class.
   */
  @Test
  public void testAnalysisWithClassObfuscation() throws IOException {
    byte[] buffer = createSimpleJsonBuffer(OBFUSCATED_CLASS_NAME, METHOD_SIGNATURE);

    ICoverageVisitor visitor = new ICoverageVisitor() {
      @Override
      public void visitCoverage(IClassCoverage arg0) {
        Assert.assertEquals(ID, arg0.getId());
        Assert.assertEquals(CLASS_NAME, arg0.getName());
        Assert.assertEquals(SUPER_CLASS_NAME, arg0.getSuperName());
      }
    };

    MappingFileLoader loader = MappingFileLoaderTest.loadMappingFile(
        String.format("%s -> %s:",
            NamingUtils.binaryNameToFqName(CLASS_NAME),
            NamingUtils.binaryNameToFqName(OBFUSCATED_CLASS_NAME))
    );

    ExecutionDataStore executionDataStore = createExecutionDataStore(OBFUSCATED_CLASS_NAME);
    JackCoverageAnalyzer jackCoverageAnalyzer =
        new JackCoverageAnalyzer(executionDataStore, visitor, loader);

    ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
    try {
      jackCoverageAnalyzer.analyze(bais);
    } finally {
      bais.close();
    }
  }

  /**
   * Simulate obfuscated class.
   */
  @Test
  public void testAnalysisWithMethodObfuscation() throws IOException {
    byte[] buffer = createSimpleJsonBuffer(OBFUSCATED_CLASS_NAME, OBFUSCATED_METHOD_SIGNATURE);

    ICoverageVisitor visitor = new ICoverageVisitor() {
      @Override
      public void visitCoverage(IClassCoverage arg0) {
        Assert.assertEquals(ID, arg0.getId());
        Assert.assertEquals(CLASS_NAME, arg0.getName());
        Assert.assertEquals(SUPER_CLASS_NAME, arg0.getSuperName());

        // Check method
        Assert.assertEquals(1, arg0.getMethods().size());
        IMethodCoverage methodCoverage = arg0.getMethods().iterator().next();
        Assert.assertNotNull(methodCoverage);
        String[] nameAndDesc = splitSignatureInNameAndDesc(METHOD_SIGNATURE);
        Assert.assertEquals(nameAndDesc[0], methodCoverage.getName());
        Assert.assertEquals(nameAndDesc[1], methodCoverage.getDesc());
      }
    };

    MappingFileLoader loader = MappingFileLoaderTest.loadMappingFile(
        String.format("%s -> %s:",
            NamingUtils.binaryNameToFqName(CLASS_NAME),
            NamingUtils.binaryNameToFqName(OBFUSCATED_CLASS_NAME)),
        String.format("%s -> %s",
            METHOD_DECLARATION, OBFUSCATED_METHOD_NAME)
    );

    ExecutionDataStore executionDataStore = createExecutionDataStore(OBFUSCATED_CLASS_NAME);
    JackCoverageAnalyzer jackCoverageAnalyzer =
        new JackCoverageAnalyzer(executionDataStore, visitor, loader);

    ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
    try {
      jackCoverageAnalyzer.analyze(bais);
    } finally {
      bais.close();
    }
  }

  @Nonnull
  private ExecutionDataStore createExecutionDataStore(@Nonnull String className) {
    ExecutionDataStore executionDataStore = new ExecutionDataStore();
    ExecutionData executionData = executionDataStore.get(Long.valueOf(ID), className,
        PROBES.length);
    for (int i = 0, e = PROBES.length; i < e; ++i) {
      executionData.getProbes()[i] = PROBES[i];
    }
    return executionDataStore;
  }

  // Create the minimal JSON for the class.
  @Nonnull
  private byte[] createSimpleJsonBuffer(@Nonnull String className, @Nonnull String methodSignature)
      throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    JsonWriter w = new JsonWriter(new OutputStreamWriter(baos, "UTF-8"));
    try {
      w.beginObject();
      w.name(JackCoverageAnalyzer.JSON_VERSION_ATTRIBUTE)
          .value(JackCoverageAnalyzer.CURRENT_VERSION);
      w.name(JackCoverageAnalyzer.JSON_DATA_ATTRIBUTE).beginArray();
      w.beginObject();
      w.name("id").value(ID);
      w.name("name").value(className);
      w.name("superClassName").value(SUPER_CLASS_NAME);
      w.name("methods").beginArray();
      // Add method
      {
        String[] nameAndDesc = splitSignatureInNameAndDesc(methodSignature);
        w.beginObject();
        w.name("id").value(0);
        w.name("name").value(nameAndDesc[0]);
        w.name("desc").value(nameAndDesc[1]);
        w.endObject();
      }
      w.endArray();
      w.name("probes").beginArray();
      for (int i = 0, e = PROBES.length; i < e; ++i) {
        w.beginObject();
        w.name("id").value(i);
        w.name("method").value(0);
        w.endObject();
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

  @Nonnull
  private static String[] splitSignatureInNameAndDesc(@Nonnull String methodSignature) {
    int separatorPos = methodSignature.indexOf('(');
    assert separatorPos > 0;
    String methodName = methodSignature.substring(0, separatorPos);
    String methodDesc = methodSignature.substring(separatorPos);
    return new String[] { methodName, methodDesc };
  }
}
