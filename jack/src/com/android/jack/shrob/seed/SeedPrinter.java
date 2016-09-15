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

package com.android.jack.shrob.seed;

import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JType;
import com.android.jack.reporting.ReportableIOException;
import com.android.jack.reporting.Reporter.Severity;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.jack.shrob.proguard.GrammarActions;
import com.android.jack.transformations.ast.removeinit.FieldInitMethod;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Produce;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.codec.WriterFileCodec;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.WriterFilePropertyId;
import com.android.sched.util.file.CannotWriteException;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.stream.CustomPrintWriter;

import java.io.IOException;
import java.util.Iterator;

import javax.annotation.Nonnull;

/**
 * Visitor that prints the seeds
 */
@HasKeyId
@Description("Visitor that prints the seeds")
@Produce(SeedFile.class)
@Constraint(need = {OriginalNames.class, SeedMarker.class}, no = FieldInitMethod.class)
public class SeedPrinter implements RunnableSchedulable<JSession> {

  @Nonnull
  public static final WriterFilePropertyId SEEDS_OUTPUT_FILE = WriterFilePropertyId.create(
      "jack.seed.dump.file", "File where the seeds will be printed",
      new WriterFileCodec(Existence.MAY_EXIST).allowStandardOutputOrError().allowCharset())
      .addDefaultValue("-");

  private static final char TYPE_AND_MEMBER_SEPARATOR = ':';

  private void appendQualifiedName(
      @Nonnull StringBuilder nameBuilder, @Nonnull JType type) {
    nameBuilder.append(GrammarActions.getSourceFormatter().getName(type));
  }

  @Override
  public void run(@Nonnull JSession session) {
    CustomPrintWriter writer = ThreadConfig.get(SEEDS_OUTPUT_FILE).getPrintWriter();
    try {
      for (JDefinedClassOrInterface type : session.getTypesToEmit()) {
        StringBuilder typeNameBuilder = new StringBuilder();
        appendQualifiedName(typeNameBuilder, type);

        if (type.containsMarker(SeedMarker.class)) {
          writer.println(typeNameBuilder.toString());
        }

        for (JField field : type.getFields()) {
          if (field.containsMarker(SeedMarker.class)) {
            StringBuilder fieldNameBuilder = new StringBuilder(typeNameBuilder);
            fieldNameBuilder.append(TYPE_AND_MEMBER_SEPARATOR);
            fieldNameBuilder.append(' ');
            appendQualifiedName(fieldNameBuilder, field.getType());
            fieldNameBuilder.append(' ');
            fieldNameBuilder.append(field.getName());
            writer.println(fieldNameBuilder.toString());
          }
        }

        for (JMethod method : type.getMethods()) {
          if (method.containsMarker(SeedMarker.class)) {
            StringBuilder methodNameBuilder = new StringBuilder(typeNameBuilder);
            methodNameBuilder.append(TYPE_AND_MEMBER_SEPARATOR);
            methodNameBuilder.append(' ');
            if (method instanceof JConstructor) {
              methodNameBuilder.append(method.getEnclosingType().getName());
            } else {
              appendQualifiedName(methodNameBuilder, method.getType());
              methodNameBuilder.append(' ');
              methodNameBuilder.append(method.getName());
            }
            methodNameBuilder.append('(');
            Iterator<JParameter> iterator = method.getParams().iterator();
            while (iterator.hasNext()) {
              JParameter param = iterator.next();
              appendQualifiedName(methodNameBuilder, param.getType());
              if (iterator.hasNext()) {
                methodNameBuilder.append(',');
              }
            }
            methodNameBuilder.append(')');
            writer.println(methodNameBuilder.toString());
          }
        }
      }
    } finally {
      writer.close();
      try {
        writer.throwPendingException();
      } catch (IOException e) {
        session.getReporter().report(Severity.FATAL, new ReportableIOException("Seed",
            new CannotWriteException(ThreadConfig.get(SEEDS_OUTPUT_FILE), e)));
        session.abortEventually();
      }
    }
  }

}
