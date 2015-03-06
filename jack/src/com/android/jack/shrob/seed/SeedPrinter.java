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
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.jack.shrob.proguard.GrammarActions;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Produce;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.codec.OutputStreamCodec;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.OutputStreamFile;

import java.io.PrintStream;
import java.util.Iterator;

import javax.annotation.Nonnull;

/**
 * Visitor that prints the seeds
 */
@HasKeyId
@Description("Visitor that prints the seeds")
@Produce(SeedFile.class)
@Constraint(need = {OriginalNames.class, SeedMarker.class})
public class SeedPrinter implements RunnableSchedulable<JSession> {

  @Nonnull
  public static final PropertyId<OutputStreamFile> SEEDS_OUTPUT_FILE = PropertyId.create(
      "jack.seed.dump.file", "File where the seeds will be printed",
      new OutputStreamCodec(Existence.MAY_EXIST).allowStandardOutputOrError())
      .addDefaultValue("-");

  @Nonnull
  private final PrintStream stream;

  private static final char TYPE_AND_MEMBER_SEPARATOR = ':';

  public SeedPrinter() {
    stream = ThreadConfig.get(SEEDS_OUTPUT_FILE).getPrintStream();
  }

  private void appendQualifiedName(
      @Nonnull StringBuilder nameBuilder, @Nonnull JType type) {
    nameBuilder.append(GrammarActions.getSourceFormatter().getName(type));
  }

  @Override
  public void run(@Nonnull JSession session) throws Exception {

    for (JDefinedClassOrInterface type : session.getTypesToEmit()) {
      StringBuilder typeNameBuilder = new StringBuilder();
      appendQualifiedName(typeNameBuilder, type);

      if (type.containsMarker(SeedMarker.class)) {
        stream.println(typeNameBuilder.toString());
      }

      for (JField field : type.getFields()) {
        if (field.containsMarker(SeedMarker.class)) {
          StringBuilder fieldNameBuilder = new StringBuilder(typeNameBuilder);
          fieldNameBuilder.append(TYPE_AND_MEMBER_SEPARATOR);
          fieldNameBuilder.append(' ');
          appendQualifiedName(fieldNameBuilder, field.getType());
          fieldNameBuilder.append(' ');
          fieldNameBuilder.append(field.getName());
          stream.println(fieldNameBuilder.toString());
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
          stream.println(methodNameBuilder.toString());
        }
      }
    }
    stream.close();
  }

}
