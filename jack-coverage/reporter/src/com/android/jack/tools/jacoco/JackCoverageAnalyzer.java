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

import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageVisitor;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.internal.analysis.ClassCoverageImpl;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.jacoco.core.internal.analysis.MethodCoverageImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Code coverage report analyzer.
 */
public class JackCoverageAnalyzer {
  @Nonnull
  public static final String CURRENT_VERSION = "1.0";

  @Nonnull
  public static final String JSON_VERSION_ATTRIBUTE = "version";

  @Nonnull
  public static final String JSON_DATA_ATTRIBUTE = "data";

  @Nonnull
  private final ExecutionDataStore executionDataStore;

  @Nonnull
  private final ICoverageVisitor coverageVisitor;

  @CheckForNull
  private final MappingFileLoader mappingFileLoader;

  @CheckForNull
  private MappingFileLoader.ClassMapping currentClassMapping = null;

  /**
   * Count the real number of probes in the code, taking shrinking into account.
   */
  @Nonnegative
  private int currentClassProbesCount;

  /**
   * Constructs a {@link JackCoverageAnalyzer}.
   *
   * @param executionDataStore a {@link ExecutionDataStore} containing runtime coverage information.
   * @param coverageVisitor a {@link ICoverageVisitor} notified of each coverage element created
   *        during the analysis.
   */
  public JackCoverageAnalyzer(@Nonnull ExecutionDataStore executionDataStore,
      @Nonnull ICoverageVisitor coverageVisitor,
      @CheckForNull MappingFileLoader mappingFileLoader) {
    this.executionDataStore = executionDataStore;
    this.coverageVisitor = coverageVisitor;
    this.mappingFileLoader = mappingFileLoader;
  }

  /**
   * Analyzes a coverage description file.
   *
   * @param coverageDescriptionFile a coverage description file to be analyzed
   * @throws IOException in case of file error
   */
  public void analyze(@Nonnull File coverageDescriptionFile) throws IOException {
    InputStream inputStream = new FileInputStream(coverageDescriptionFile);
    try {
      analyze(inputStream);
    } finally {
      inputStream.close();
    }
  }

  /**
   * Analyzes a coverage description input stream.
   *
   * @param inputStream a coverage description input stream
   * @throws IOException in case of read error
   */
  public void analyze(@Nonnull InputStream inputStream) throws IOException {
    JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream));
    readMetadata(jsonReader);
  }

  private void checkVersion(@CheckForNull String version) {
    if (version == null) {
      throw new JsonParseException("Missing 'version' attribute before coverage metadadata");
    }
    String[] parts = version.split(Pattern.quote("."));
    if (parts.length != 2) {
      throw new JsonParseException("Version number format must be x.y");
    }
    if (!version.equals(CURRENT_VERSION)) {
      throw new JsonParseException("Unknown version " + version);
    }
  }

  private void readMetadata(@Nonnull JsonReader jsonReader) throws IOException {
    jsonReader.beginObject();

    String version = null;
    while (jsonReader.hasNext()) {
      String attributeName = jsonReader.nextName();
      if (attributeName.equals(JSON_VERSION_ATTRIBUTE)) {
        // Reads the version so we can parse the JSON accordingly.
        version = jsonReader.nextString();
      } else if (attributeName.equals(JSON_DATA_ATTRIBUTE)) {
        checkVersion(version);
        readClasses(jsonReader);
      } else {
        jsonReader.skipValue();
      }
    }

    jsonReader.endObject();
  }

  private void readClasses(@Nonnull JsonReader jsonReader) throws IOException {
    jsonReader.beginArray();
    while (jsonReader.hasNext()) {
      IClassCoverage classCoverage = readClass(jsonReader);
      if (classCoverage != null) {
        coverageVisitor.visitCoverage(classCoverage);
      }
    }
    jsonReader.endArray();
  }

  /**
   * Returns {@link IClassCoverage} object parsed from class information, or null if the class
   * was shrunk (according to the mapping file).
   *
   * @param jsonReader a reader
   * @return {@link IClassCoverage} instance or null
   * @throws IOException if an error occurred during file parsing
   */
  @CheckForNull
  private IClassCoverage readClass(@Nonnull JsonReader jsonReader) throws IOException {
    long id = 0;
    String className = null;
    String sourceFile = null;
    String superClassName = null;
    List<IMethodCoverage> methods = new ArrayList<IMethodCoverage>();
    List<ProbeDescription> probes = new ArrayList<ProbeDescription>();
    List<String> interfaces = new ArrayList<String>();

    currentClassProbesCount = 0;

    jsonReader.beginObject();
    while (jsonReader.hasNext()) {
      String attributeName = jsonReader.nextName();
      if ("id".equals(attributeName)) {
        id = jsonReader.nextLong();
      } else if ("name".equals(attributeName)) {
        className = jsonReader.nextString();
        if (mappingFileLoader != null) {
          currentClassMapping = mappingFileLoader.getClassMapping(className);
        }
      } else if ("sourceFile".equals(attributeName)) {
        sourceFile = jsonReader.nextString();
      } else if ("superClassName".equals(attributeName)) {
        superClassName = jsonReader.nextString();
      } else if ("interfaces".equals(attributeName)) {
        readInterfaces(jsonReader, interfaces);
      } else if ("methods".equals(attributeName)) {
        readMethods(jsonReader, methods);
      } else if ("probes".equals(attributeName)) {
        readProbes(jsonReader, probes, methods);
      } else {
        throw new JsonParseException("Unknown attribute \"" + attributeName + "\"");
      }
    }
    jsonReader.endObject();

    // Check mandatory attributes.
    if (id == 0) {
      throw new JsonParseException("Missing 'id' attribute");
    }
    if (className == null) {
      throw new JsonParseException("Missing 'name' attribute");
    }
    if (superClassName == null) {
      throw new JsonParseException("Missing 'superClassName' attribute");
    }

    final ExecutionData executionData = executionDataStore.get(id);
    boolean noMatch;
    if (executionData != null) {
      noMatch = false;
      // Check there is no id collision.
      executionData.assertCompatibility(id, className, currentClassProbesCount);
    } else {
      noMatch = executionDataStore.contains(className);
    }

    // Support shrinking and obfuscation.
    if (currentClassMapping != null) {
      className = currentClassMapping.getOriginalClassName();
      assert className != null;
    } else if (mappingFileLoader != null) {
      // We did not find the class in the mapping file: it must have been shrunk so ignore it.
      return null;
    }

    // Build the class coverage.
    String[] interfacesArray = interfaces.toArray(new String[0]);
    ClassCoverageImpl c = new ClassCoverageImpl(className, id, noMatch,
        NamingUtils.binaryNameToSignature(className), superClassName, interfacesArray);
    c.setSourceFileName(sourceFile);

    // Update methods with probes.
    final boolean[] executionProbes = executionData != null ? executionData.getProbes() : null;
    for (ProbeDescription probe : probes) {
      final int probeIndex = probe.id;
      final boolean active = (executionProbes != null && executionProbes[probeIndex]);
      final MethodCoverageImpl methodCoverage = probe.method;
      for (ProbeDescription.Line line : probe.lines) {
        ICounter instructionCounter;
        ICounter branchCounter;
        if (active) {
          instructionCounter = CounterImpl.getInstance(0, line.instructionsCount);
          branchCounter = CounterImpl.getInstance(0, line.branchesCount);
        } else {
          instructionCounter = CounterImpl.getInstance(line.instructionsCount, 0);
          branchCounter = CounterImpl.getInstance(line.branchesCount, 0);
        }
        methodCoverage.increment(instructionCounter, branchCounter, line.line);
      }
    }

    // Now methods have been updated with probes, add them to the class coverage.
    for (IMethodCoverage method : methods) {
      c.addMethod(method);
    }

    return c;
  }

  private void readInterfaces(@Nonnull JsonReader jsonReader, @Nonnull List<String> interfaces)
      throws IOException {
    jsonReader.beginArray();
    while (jsonReader.hasNext()) {
      interfaces.add(jsonReader.nextString());
    }
    jsonReader.endArray();
  }

  private void readProbes(@Nonnull JsonReader jsonReader,
      @Nonnull List<ProbeDescription> probes, @Nonnull List<? extends IMethodCoverage> methods)
      throws IOException {
    jsonReader.beginArray();
    while (jsonReader.hasNext()) {
      ProbeDescription probe = readProbe(jsonReader, methods);
      if (probe != null) {
        probes.add(probe);
      }
    }
    jsonReader.endArray();
  }

  /**
   * Returns a {@link ProbeDescription} object representing the probe being parsed, or null if the
   * method associated with the probe was shrunk.
   *
   * @param jsonReader a reader
   * @param methods the list of methods in the current class
   * @return a {@link ProbeDescription} instance or null
   * @throws IOException if an error occurred during file parsing
   */
  @CheckForNull
  private ProbeDescription readProbe(@Nonnull JsonReader jsonReader,
      @Nonnull List<? extends IMethodCoverage> methods) throws IOException {
    ++currentClassProbesCount;
    ProbeDescription probe = new ProbeDescription();
    jsonReader.beginObject();
    while (jsonReader.hasNext()) {
      String attributeName = jsonReader.nextName();
      if ("id".equals(attributeName)) {
        probe.setId(jsonReader.nextInt());
      } else if ("method".equals(attributeName)) {
        int methodId = jsonReader.nextInt();
        for (IMethodCoverage mc : methods) {
          if (((JackMethodCoverage) mc).getId() == methodId) {
            probe.setMethod((MethodCoverageImpl) mc);
            break;
          }
        }
      } else if ("lines".equals(attributeName)) {
        readLines(jsonReader, probe);
      } else {
        throw new JsonParseException("Unknown attribute \"" + attributeName + "\"");
      }
    }
    jsonReader.endObject();

    if (probe.method != null) {
      return probe;
    } else {
      // Ignore shrob if there is no matching method (due to shrinking)
      return null;
    }
  }

  private static void readLines(@Nonnull JsonReader jsonReader, @Nonnull ProbeDescription probe)
      throws IOException {
    jsonReader.beginArray();
    while (jsonReader.hasNext()) {
      jsonReader.beginObject();
      int line = -1;
      int instructionsCount = -1;
      int branchesCount = -1;
      while (jsonReader.hasNext()) {
        String attributeName = jsonReader.nextName();
        if ("line".equals(attributeName)) {
          line = jsonReader.nextInt();
        } else if ("insnCount".equals(attributeName)) {
          instructionsCount = jsonReader.nextInt();
        } else if ("branchCount".equals(attributeName)) {
          branchesCount = jsonReader.nextInt();
        } else {
          throw new JsonParseException("Unknown attribute \"" + attributeName + "\"");
        }
      }
      probe.addLine(line, instructionsCount, branchesCount);
      jsonReader.endObject();
    }
    jsonReader.endArray();
  }

  private void readMethods(@Nonnull JsonReader jsonReader,
      @Nonnull List<IMethodCoverage> methods) throws IOException {
    jsonReader.beginArray();
    while (jsonReader.hasNext()) {
      IMethodCoverage methodCoverage = readMethod(jsonReader);
      if (methodCoverage != null) {
        methods.add(methodCoverage);
      }
    }
    jsonReader.endArray();
  }

  /**
   * Returns a {@link IMethodCoverage} object representing the method being parsed, or null if this
   * method was shrunk.
   *
   * @param jsonReader a reader
   * @return a {@link IMethodCoverage} instance or null
   * @throws IOException if an error occurred during file parsing
   */
  @CheckForNull
  private IMethodCoverage readMethod(@Nonnull JsonReader jsonReader) throws IOException {
    int id = -1;
    String name = null;
    String desc = null;
    String signature = null;

    jsonReader.beginObject();
    while (jsonReader.hasNext()) {
      String attributeName = jsonReader.nextName();
      if ("id".equals(attributeName)) {
        id = jsonReader.nextInt();
      } else if ("name".equals(attributeName)) {
        name = jsonReader.nextString();
      } else if ("desc".equals(attributeName)) {
        desc = jsonReader.nextString();
      } else if ("signature".equals(attributeName)) {
        signature = jsonReader.nextString();
      } else {
        throw new JsonParseException("Unknown attribute \"" + attributeName + "\"");
      }
    }
    jsonReader.endObject();

    if (currentClassMapping != null) {
      assert mappingFileLoader != null;
      // We have a mapping file and the class has not been shrunk. Let's see if this method was
      // shrunk or obfuscated.
      String methodSignature = name + desc;
      String oldMethodSignature = currentClassMapping.getOriginalMethodSignature(methodSignature);
      if (oldMethodSignature == null) {
        // No corresponding method: method was shrunk
        return null;
      } else {
        // Method may have been obfuscated.
        int methodNameEndPos = oldMethodSignature.indexOf('(');
        assert methodNameEndPos > 0;
        name = oldMethodSignature.substring(0, methodNameEndPos);
        desc = oldMethodSignature.substring(methodNameEndPos);
      }
    }
    return new JackMethodCoverage(id, name, desc, signature);
  }
}
