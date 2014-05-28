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
import java.io.Writer;
import java.lang.annotation.Annotation;
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
import javax.tools.JavaFileObject;

/**
 * Annotation processor to generate a new source file.
 */
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes("*")
public class SourceAnnotationProcessor extends AbstractProcessor {
  @CheckForNull
  private ProcessingEnvironment env;

  @Override
  public synchronized void init(ProcessingEnvironment env) {
    this.env = env;
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (!roundEnv.processingOver()) {
      //
      // @SourceErrorAnnotationTest
      //

      for (Element element : getElementsAnnotatedWith(roundEnv, SourceErrorAnnotationTest.class)) {
        TypeMirror type = element.asType();

        if (type.getKind() == TypeKind.DECLARED) {
          TypeElement classElement = (TypeElement) element;
          try {
            assert env != null;
            JavaFileObject jfo =
                env.getFiler().createSourceFile(classElement.getQualifiedName() + "Duplicated");
            Writer writer = jfo.openWriter();

            writer.write("Bad java source.");
            writer.close();
          } catch (IOException e) {
            throw new AssertionError(e);
          }
        }
      }

      //
      // @SourceAnnotationTest
      //

      for (Element element : getElementsAnnotatedWith(roundEnv, SourceAnnotationTest.class)) {
        TypeMirror type = element.asType();

        if (type.getKind() == TypeKind.DECLARED) {
          TypeElement classElement = (TypeElement) element;
          try {
            assert env != null;
            JavaFileObject jfo =
                env.getFiler().createSourceFile(classElement.getQualifiedName() + "Duplicated");
            Writer writer = jfo.openWriter();
            writer.write("public class " + classElement.getSimpleName() + "Duplicated {}");
            writer.close();
          } catch (IOException e) {
            throw new AssertionError(e);
          }
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
