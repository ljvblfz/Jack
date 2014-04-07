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

package com.android.jack.shrob;

import com.android.jack.JackIOException;
import com.android.jack.Options;
import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JType;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.jack.shrob.proguard.GrammarActions;
import com.android.jack.shrob.shrink.NodeFinder;
import com.android.jack.shrob.spec.ClassSpecification;
import com.android.jack.shrob.spec.FieldSpecification;
import com.android.jack.shrob.spec.Flags;
import com.android.jack.shrob.spec.MethodSpecification;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Produce;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.codec.StreamCodec;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.StreamFile;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Visitor that prints the seeds
 */
@HasKeyId
@Description("Visitor that prints the seeds")
@Produce(SeedFile.class)
@Constraint(need = OriginalNames.class)
public class SeedPrinter implements RunnableSchedulable<JSession> {

  @Nonnull
  public static final PropertyId<StreamFile> SEEDS_OUTPUT_FILE = PropertyId.create(
      "jack.seed.dump.file", "File where the seeds will be printed",
      new StreamCodec(Existence.MAY_EXIST, Permission.WRITE).allowStandard())
      .addDefaultValue("-");

  @Nonnull
  private final PrintStream stream;

  private static final char TYPE_AND_MEMBER_SEPARATOR = ':';

  @Nonnull
  private final Flags flags = ThreadConfig.get(Options.FLAGS);

  public SeedPrinter() {
    StreamFile outputStreamFile = ThreadConfig.get(SEEDS_OUTPUT_FILE);
    try {
      stream = outputStreamFile.getPrintStream();
    } catch (FileNotFoundException e) {
      throw new JackIOException(
          "Seeds output file " + outputStreamFile.getName() + " not found", e);
    }
  }

  private void appendQualifiedName(
      @Nonnull StringBuilder nameBuilder, @Nonnull JType type) {
    nameBuilder.append(GrammarActions.getSourceFormatter().getName(type));
  }

  @Override
  public void run(@Nonnull JSession session) throws Exception {

    for (JDefinedClassOrInterface type : session.getTypesToEmit()) {
      boolean matched = false;
      List<FieldSpecification> matchedFieldSpecs = new ArrayList<FieldSpecification>();
      List<MethodSpecification> matchedMethodSpecs = new ArrayList<MethodSpecification>();
      for (ClassSpecification classSpec : flags.getKeepClassSpecs()) {
        if (classSpec.matches(type)) {
          matched = true;
          matchedFieldSpecs.addAll(classSpec.getFieldSpecs());
          matchedMethodSpecs.addAll(classSpec.getMethodSpecs());
        }
      }
      for (ClassSpecification classSpec : flags.getKeepClassMembersSpecs()) {
        if (classSpec.matches(type)) {
          matchedFieldSpecs.addAll(classSpec.getFieldSpecs());
          matchedMethodSpecs.addAll(classSpec.getMethodSpecs());
        }
      }
      for (ClassSpecification classSpec : flags.getKeepClassesWithMembersSpecs()) {
        if (classSpec.matches(type)) {
          NodeFinder<JField> fieldFinder = new NodeFinder<JField>(type.getFields());
          fieldFinder.find(classSpec.getFieldSpecs());

          NodeFinder<JMethod> methodFinder = new NodeFinder<JMethod>(type.getMethods());
          methodFinder.find(classSpec.getMethodSpecs());

          if (fieldFinder.allSpecificationsMatched() && methodFinder.allSpecificationsMatched()) {
            matched = true;
            matchedFieldSpecs.addAll(classSpec.getFieldSpecs());
            matchedMethodSpecs.addAll(classSpec.getMethodSpecs());
          }
        }
      }
      for (ClassSpecification classSpec : flags.getKeepClassSpecs()) {
        if (classSpec.matches(type)) {
          matched = true;
          matchedFieldSpecs.addAll(classSpec.getFieldSpecs());
          matchedMethodSpecs.addAll(classSpec.getMethodSpecs());
        }
      }

      StringBuilder typeNameBuilder = new StringBuilder();
      appendQualifiedName(typeNameBuilder, type);

      if (matched) {
        stream.println(typeNameBuilder.toString());
      }

      for (JField field : type.getFields()) {
        for (FieldSpecification spec : matchedFieldSpecs) {
          if (spec.matches(field)) {
            StringBuilder fieldNameBuilder = new StringBuilder(typeNameBuilder);
            fieldNameBuilder.append(TYPE_AND_MEMBER_SEPARATOR);
            fieldNameBuilder.append(' ');
            appendQualifiedName(fieldNameBuilder, field.getType());
            fieldNameBuilder.append(' ');
            fieldNameBuilder.append(field.getName());
            stream.println(fieldNameBuilder.toString());
            break;
          }
        }
      }

      for (JMethod method : type.getMethods()) {
        for (MethodSpecification spec : matchedMethodSpecs) {
          if (spec.matches(method)) {
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
            break;
          }
        }
      }
    }
    stream.close();
  }

}
