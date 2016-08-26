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

package com.android.jack.annotation.processor.sample2.processors;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

/**
 * Annotation processor to generate a new source file.
 */
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes("*")
public class SourceAnnotationProcessor extends AbstractProcessor {
  @CheckForNull
  private ProcessingEnvironment env;
  private boolean fileGenerated = false;

  @Override
  public synchronized void init(@Nonnull ProcessingEnvironment env) {
    this.env = env;
  }

  @Override
  public boolean process(@Nonnull Set<? extends TypeElement> annotations, @Nonnull RoundEnvironment roundEnv) {
    assert env != null;
    env.getMessager().printMessage(Kind.NOTE, "SourceAnnotationProcessor.process");

    if (!roundEnv.processingOver()) {
      if (!fileGenerated) {
        try {
          JavaFileObject jfo = env.getFiler()
              .createSourceFile("com.android.jack.annotation.processor.sample2.src.B");
          Writer writer = jfo.openWriter();
          writer.write("package com.android.jack.annotation.processor.sample2.src;"
              + "public class B { public static final class C { } }");
          writer.close();
          fileGenerated = true;
        } catch (IOException e) {
          throw new AssertionError(e);
        }
      }
    }

    return false;
  }
}
