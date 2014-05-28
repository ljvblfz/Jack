/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.errorhandling.annotationprocessor;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;
import javax.tools.StandardLocation;

/**
 * Annotation processor generating a dedicated resource file called {@code rscGeneratedFile}.
 */
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes("*")
public class ResourceAnnotationProcessor extends AbstractProcessor {
  @CheckForNull
  private ProcessingEnvironment env;

  @Nonnull
  private final List<String> data = new ArrayList<String>();

  @Nonnull
  public final static String FILENAME = "rscGeneratedFile";

  @Override
  public synchronized void init(ProcessingEnvironment env) {
    this.env = env;
    try {
      env.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", FILENAME);
    } catch (IOException e) {
      // Best effort
    }
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      try {
        assert env != null;

        OutputStream os = env.getFiler()
            .createResource(StandardLocation.CLASS_OUTPUT, "", FILENAME)
            .openOutputStream();
        Writer writer = new OutputStreamWriter(os);
        try {
          for (String val : data) {
            writer.write(val);
            writer.write("\n");
          }
        } finally {
          writer.close();
        }
      } catch (IOException e) {
        env.getMessager().printMessage(Kind.ERROR,
            "Can not write resource file for '" + FILENAME + "': " + e.getMessage());
      }
    } else {
      //
      // @ResourceAnnotationTest
      //

      for (Element element : getElementsAnnotatedWith(roundEnv, ResourceAnnotationTest.class)) {
        assert data != null;

        TypeMirror type = element.asType();

        if (type.getKind() == TypeKind.DECLARED) {
          data.add(ResourceAnnotationTest.class.getCanonicalName());
          assert env != null;
          data.add(env.getElementUtils().getBinaryName((TypeElement) element).toString());

        }
      }
    }

    return false;
  }

  @Nonnull
  private Set<? extends Element> getElementsAnnotatedWith(@Nonnull RoundEnvironment roundEnv,
      @Nonnull Class<? extends Annotation> cls) {
    assert env != null;

    String name = cls.getCanonicalName();
    assert name != null;

    TypeElement element = env.getElementUtils().getTypeElement(name);
    assert element != null;

    return roundEnv.getElementsAnnotatedWith(element);
  }
}
