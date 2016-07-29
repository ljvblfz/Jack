/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.shrob.obfuscation;

import com.google.common.base.Strings;

import com.android.jack.config.id.Arzon;
import com.android.jack.ir.ast.HasName;
import com.android.jack.ir.ast.JArrayType;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.reporting.ReportableIOException;
import com.android.jack.reporting.Reporter.Severity;
import com.android.jack.shrob.proguard.GrammarActions;
import com.android.sched.item.Description;
import com.android.sched.marker.LocalMarkerManager;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Optional;
import com.android.sched.schedulable.Produce;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.ToSupport;
import com.android.sched.util.codec.WriterFileCodec;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.config.id.WriterFilePropertyId;
import com.android.sched.util.file.CannotWriteException;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.WriterFile;
import com.android.sched.util.stream.CustomPrintWriter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.annotation.Nonnull;

/**
 * Visitor that prints the mapping
 */
@HasKeyId
@Description("Visitor that prints the mapping")
@Produce(Mapping.class)
@Constraint(need = FinalNames.class)
@Optional(@ToSupport(
    feature = Obfuscation.class, add = @Constraint(need = OriginalNameMarker.class)))
public class MappingPrinter implements RunnableSchedulable<JSession> {

  @Nonnull
  public static final BooleanPropertyId MAPPING_OUTPUT_ENABLED = BooleanPropertyId
      .create("jack.obfuscation.mapping.dump", "Print the obfuscation mapping")
      .addDefaultValue(false).addCategory(Arzon.class);

  @Nonnull
  public static final WriterFilePropertyId MAPPING_OUTPUT_FILE = WriterFilePropertyId
      .create("jack.obfuscation.mapping.dump.file", "File where the mapping will be emitted",
          new WriterFileCodec(Existence.MAY_EXIST).allowStandardOutputOrError().allowCharset())
      .addDefaultValue("-").addCategory(Arzon.class);

  @Nonnull
  private final WriterFile file = ThreadConfig.get(MAPPING_OUTPUT_FILE);

  private static final String SEPARATOR = " -> ";

  private static final char PACKAGE_SEPARATOR = '.';

  private static class Visitor extends JVisitor {
    @Nonnull
    private final PrintWriter writer;

    public Visitor(@Nonnull PrintWriter writer) {
      this.writer = writer;
    }

    private void appendOriginalQualifiedName(
        @Nonnull StringBuilder nameBuilder, @Nonnull JPackage pack) {
      JPackage enclosingPackage;
      OriginalPackageMarker marker = pack.getMarker(OriginalPackageMarker.class);
      if (marker != null) {
        enclosingPackage = marker.getOriginalEnclosingPackage();
      } else {
        enclosingPackage = pack.getEnclosingPackage();
      }
      if (enclosingPackage != null && !enclosingPackage.isTopLevelPackage()) {
        appendOriginalQualifiedName(nameBuilder, enclosingPackage);
        nameBuilder.append(PACKAGE_SEPARATOR);
      }
      appendOriginalName(nameBuilder, pack);
    }

    private void appendOriginalQualifiedName(
        @Nonnull StringBuilder nameBuilder, @Nonnull JClassOrInterface type) {
      JPackage enclosingPackage;
      OriginalPackageMarker marker = ((JNode) type).getMarker(OriginalPackageMarker.class);
      if (marker != null) {
        enclosingPackage = marker.getOriginalEnclosingPackage();
      } else {
        enclosingPackage = type.getEnclosingPackage();
      }
      assert enclosingPackage != null;
      appendOriginalQualifiedName(nameBuilder, enclosingPackage);
      if (!enclosingPackage.isTopLevelPackage()) {
        nameBuilder.append(PACKAGE_SEPARATOR);
      }
      appendOriginalName(nameBuilder, type);
    }

    private void appendOriginalName(@Nonnull StringBuilder nameBuilder, @Nonnull HasName node) {
      OriginalNameMarker marker = ((LocalMarkerManager) node).getMarker(OriginalNameMarker.class);
      if (marker != null) {
        nameBuilder.append(marker.getOriginalName());
      } else {
        nameBuilder.append(node.getName());
      }
    }

    private void appendOriginalQualifiedName(
        @Nonnull StringBuilder nameBuilder, @Nonnull HasName node) {
      if (node instanceof JArrayType) {
        JArrayType arrayType = (JArrayType) node;
        appendOriginalQualifiedName(nameBuilder, arrayType.getLeafType());
        nameBuilder.append(Strings.repeat("[]", arrayType.getDims()));
      } else if (node instanceof JDefinedClassOrInterface) {
        appendOriginalQualifiedName(nameBuilder, (JClassOrInterface) node);
      } else if (node instanceof JType) {
        nameBuilder.append(node.getName());
      } else {
        appendOriginalName(nameBuilder, node);
      }
    }

    @Override
    public boolean visit(@Nonnull JDefinedClassOrInterface type) {
      StringBuilder info = new StringBuilder();
      appendOriginalQualifiedName(info, type);
      info.append(SEPARATOR);
      info.append(GrammarActions.getSourceFormatter().getName(type));
      info.append(':');
      writer.println(info);

      return super.visit(type);
    }

    @Override
    public boolean visit(@Nonnull JField field) {
      StringBuilder info = new StringBuilder().append("    ");
      appendOriginalQualifiedName(info, field.getType());
      info.append(' ');
      appendOriginalName(info, field.getId());
      info.append(SEPARATOR);
      info.append(field.getName());
      writer.println(info);

      return super.visit(field);
    }

    @Override
    public boolean visit(@Nonnull JMethod method) {
      StringBuilder info = new StringBuilder().append("    ");
      appendOriginalQualifiedName(info, method.getType());
      info.append(' ');
      appendOriginalName(info, method.getMethodIdWide());
      info.append('(');
      Iterator<JParameter> iterator = method.getParams().iterator();
      while (iterator.hasNext()) {
        JParameter param = iterator.next();
        appendOriginalQualifiedName(info, param.getType());
        if (iterator.hasNext()) {
          info.append(',');
        }
      }
      info.append(')');
      info.append(SEPARATOR);
      info.append(method.getName());
      writer.println(info);

      return super.visit(method);
    }
  }

  @Override
  public void run(@Nonnull JSession session) {
    CustomPrintWriter writer = file.getPrintWriter();
    try {
      Visitor visitor = new Visitor(writer);
      visitor.accept(session.getTypesToEmit());
    } finally {
      writer.close();
      try {
        writer.throwPendingException();
      } catch (IOException e) {
        session.getReporter().report(Severity.FATAL, new ReportableIOException("Mapping",
            new CannotWriteException(ThreadConfig.get(MAPPING_OUTPUT_FILE), e)));
        session.abortEventually();
      }
    }
  }
}
