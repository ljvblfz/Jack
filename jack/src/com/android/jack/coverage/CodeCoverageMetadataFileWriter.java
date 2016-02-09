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

import com.android.jack.Options;
import com.android.jack.coverage.ProbeDescription.ProbeLineData;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.formatter.BinarySignatureFormatter;
import com.android.jack.ir.formatter.SourceFormatter;
import com.android.jack.ir.formatter.TypeAndMethodFormatter;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Produce;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.codec.OutputStreamCodec;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.OutputStreamFile;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

/**
 * Writes Jacoco metadata file.
 */
@HasKeyId
@Description("Writes Jacoco metadata file.")
@Constraint(need = CodeCoverageMarker.Complete.class)
@Transform(remove = CodeCoverageMarker.class)
@Produce(CodeCoverageMetadataFile.class)
public class CodeCoverageMetadataFileWriter implements RunnableSchedulable<JSession> {

  @Nonnull
  public static final PropertyId<OutputStreamFile> COVERAGE_METADATA_FILE = PropertyId.create(
      "jack.coverage.metadata.file", "File where the coverage metadata will be emitted",
      new OutputStreamCodec(Existence.MAY_EXIST).allowStandardOutputOrError())
      .requiredIf(Options.CODE_COVERVAGE.getValue().isTrue());

  private static final TypeAndMethodFormatter binaryFormatter =
      BinarySignatureFormatter.getFormatter();
  private static final TypeAndMethodFormatter sourceFormatter = SourceFormatter.getFormatter();

  private static class Visitor extends JVisitor {
    private static final String ONE_TAB = "  ";
    private static final int ONE_TAB_LENGTH = ONE_TAB.length();

    @Nonnull
    private final PrintStream writer;

    @Nonnull
    private final CodeCoverageMarker marker;

    @Nonnull
    private String currentIndent = "";

    public Visitor(@Nonnull PrintStream writer, @Nonnull CodeCoverageMarker marker) {
      this.writer = writer;
      this.marker = marker;
    }

    private static String getMethodDesc(@Nonnull JMethod method) {
      StringBuilder sb = new StringBuilder();
      sb.append('(');
      for (JParameter p : method.getParams()) {
        sb.append(binaryFormatter.getName(p.getType()));
      }
      sb.append(')');
      sb.append(binaryFormatter.getName(method.getType()));
      return sb.toString();
    }

    private void indent() {
      currentIndent = ONE_TAB + currentIndent;
    }

    private void unindent() {
      if (currentIndent.length() < ONE_TAB_LENGTH) {
        throw new IllegalStateException("Cannot decrement indentation");
      }
      currentIndent = currentIndent.substring(ONE_TAB_LENGTH);
    }

    private void println(@Nonnull String str) {
      writer.println(currentIndent + str);
    }

    private static String getSourceFileNameWithoutPath(@Nonnull JDefinedClassOrInterface x) {
      SourceInfo sourceInfo = x.getSourceInfo();
      String sourceFilename = "";
      if (sourceInfo != SourceInfo.UNKNOWN) {
        sourceFilename = sourceInfo.getFileSourceInfo().getFileName();
        int pos = sourceFilename.lastIndexOf(File.separator);
        if (pos != -1) {
          sourceFilename = sourceFilename.substring(pos + 1);
        }
      }
      return sourceFilename;
    }

    @Override
    public boolean visit(@Nonnull JDefinedClassOrInterface x) {
      JClass superClass = x.getSuperClass();
      String superClassName = (superClass != null) ? sourceFormatter.getName(superClass) : "";
      String sourceFilename = getSourceFileNameWithoutPath(x);
      List<ProbeDescription> probes = marker.getProbes();

      // We do not want to include the added coverage init method in the file (it is not
      // instrumented). So we create a copy of the methods list that does not contain this method.
      JMethod coverageInitMethod = marker.getInitMethod();
      assert coverageInitMethod != null;
      List<JMethod> methods = new ArrayList<JMethod>(x.getMethods().size());
      for (JMethod m : x.getMethods()) {
        if (m != coverageInitMethod) {
          methods.add(m);
        }
      }

      indent();
      println("\"id\": " + marker.getClassId() + ",");
      println("\"name\": \"" + binaryFormatter.getName(x) + "\",");
      println("\"superClassName\": \"" + superClassName + "\",");
      println("\"sourceFile\": \"" + sourceFilename + "\",");
      println("\"interfaces\": [");
      indent();
      List<JInterface> interfaces = x.getImplements();
      for (int i = 0, e = interfaces.size(); i < e; ++i) {
        JInterface inf = interfaces.get(i);
        String commaSuffix = (i < e - 1 ? "," : "");
        println("\"" + binaryFormatter.getName(inf) + "\"" + commaSuffix);
      }
      unindent();
      println("],");
      println("\"methods\": [");

      indent();
      for (int i = 0, e = methods.size(); i < e; ++i) {
        JMethod m = methods.get(i);
        println("{");
        indent();
        println("\"id\": " + i + ",");
        println("\"name\": \"" + m.getName() + "\",");
        println("\"desc\": \"" + getMethodDesc(m) + "\"");
        unindent();
        if (i == e - 1) {
          println("}");
        } else {
          println("},");
        }
      }
      unindent();
      println("],");
      println("\"probes\": [");

      indent();
      for (int i = 0, e = probes.size(); i < e; ++i) {
        ProbeDescription p = probes.get(i);
        println("{");
        indent();
        println("\"id\": " + probes.get(i).getProbeId() + ",");
        println("\"method\": " + methods.indexOf(p.getMethod()) + ",");
        println("\"lines\": [");

        indent();
        Map<Integer, ProbeLineData> linesMap = p.getLineToData();
        Iterator<Entry<Integer, ProbeLineData>> keyIterator = linesMap.entrySet().iterator();
        while (keyIterator.hasNext()) {
          Entry<Integer, ProbeLineData> entry = keyIterator.next();
          int line = entry.getKey().intValue();
          ProbeLineData data = entry.getValue();
          println("{");
          indent();
          println("\"line\": " + line + ",");
          int insnsCount = data.getNodesCount();
          int branchCount = data.getBranchesCount();
          println("\"insnCount\": " + insnsCount + ",");
          println("\"branchCount\": " + branchCount);
          unindent();
          if (keyIterator.hasNext()) {
            println("},");
          } else {
            println("}");
          }
        }

        unindent();
        println("]");
        unindent();
        if (i == e - 1) {
          println("}");
        } else {
          println("},");
        }
      }
      unindent();
      println("]");

      unindent();
      return true;
    }
  }

  @Override
  public void run(@Nonnull JSession session) throws Exception {
    OutputStreamFile outputFile = ThreadConfig.get(COVERAGE_METADATA_FILE);
    PrintStream writer = outputFile.getPrintStream();
    try {
      writeMetadata(session, writer);
    } finally {
      writer.close();
    }
  }

  private void writeMetadata(@Nonnull JSession session, @Nonnull PrintStream writer) {
    writer.println('[');
    Iterator<JDefinedClassOrInterface> list = session.getTypesToEmit().iterator();
    boolean first = true;
    while (list.hasNext()) {
      JDefinedClassOrInterface c = list.next();
      CodeCoverageMarker marker = c.removeMarker(CodeCoverageMarker.class);
      if (marker == null) {
        // No code coverage for this class.
        continue;
      }
      if (first) {
        writer.println("{");
        first = false;
      } else {
        writer.println(",{");
      }

      // Write metadata of the class.
      new Visitor(writer, marker).accept(c);

      writer.print('}');
      if (list.hasNext()) {
      } else {
        writer.println();
      }
    }
    writer.println(']');
  }
}
