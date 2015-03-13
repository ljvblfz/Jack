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

package com.android.sched.build;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * Annotation processor to generate dedicated resources for {@code SchedDiscover} and
 * {@code AnnotationProcessorReflectionManager}.
 */
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes("*")
public class SchedAnnotationProcessor extends AbstractProcessor {
  @CheckForNull
  private ProcessingEnvironment env;

  private static class AbortException extends Exception {
    private static final long serialVersionUID = 1L;
  }

  private enum Items {
    KEY_ID("com.android.sched.util.config.id.KeyId"),
    HASKEYID("com.android.sched.util.config.HasKeyId") {
      @Override
      public void check(@Nonnull ProcessingEnvironment env, @Nonnull Element element)
          throws AbortException {
        TypeMirror keyIdType = env.getTypeUtils().erasure(Items.KEY_ID.getTypeMirror());

        boolean noKeyId = true;
        for (Element enclosedElement : element.getEnclosedElements()) {
          if (enclosedElement.getKind() == ElementKind.FIELD
              && env.getTypeUtils().isSubtype(env.getTypeUtils().erasure(enclosedElement.asType()),
                  keyIdType)) {
            noKeyId = false;

            if (!enclosedElement.getModifiers().contains(Modifier.STATIC)) {
              env.getMessager().printMessage(
                  Kind.WARNING, "KeyId should be declared static", enclosedElement);
            }

            if (!enclosedElement.getModifiers().contains(Modifier.FINAL)) {
              env.getMessager().printMessage(
                  Kind.ERROR, "KeyId must be declared final", enclosedElement);
            }
          }
        }

        if (noKeyId) {
          env.getMessager().printMessage(Kind.ERROR, "Type does not contains KeyId", element);
        }
      }
    },
    DESCRIPTION("com.android.sched.item.Description"),
    MARKER("com.android.sched.marker.Marker"),
    FEATURE("com.android.sched.item.Feature"),
    PRODUCTION("com.android.sched.item.Production"),
    TOMOC("com.android.sched.item.TagOrMarkerOrComponent"),
    SCHEDULABLE("com.android.sched.schedulable.Schedulable"),
    VARIABLE_NAME("com.android.sched.util.codec.VariableName"),
    IMPLEMENTATION_NAME("com.android.sched.util.codec.ImplementationName");

    @Nonnull
    private final String fqName;
    @CheckForNull
    private TypeElement typeElement;

    @CheckForNull
    private static ProcessingEnvironment env;

    static void init(@Nonnull ProcessingEnvironment env) {
      Items.env = env;
      reset();
    }

    static void reset() {
      for (Items item : Items.values()) {
        item.typeElement = null;
      }
    }

    private Items(@Nonnull String fqName) {
      this.fqName = fqName;
    }

    @Nonnull
    public String getFQName() {
      return fqName;
    }

    @Nonnull
    public TypeElement getTypeElement() throws AbortException {
      assert env != null;

      if (typeElement == null) {
        typeElement = env.getElementUtils().getTypeElement(fqName);
        if (typeElement == null) {
          env.getMessager().printMessage(Kind.ERROR, "Can not get element type '" + fqName + "'");
          throw new AbortException();
        }
      }

      return typeElement;
    }

    @Nonnull
    public TypeMirror getTypeMirror() throws AbortException {
      return getTypeElement().asType();
    }

    @SuppressWarnings("unused")
    public void check(@Nonnull ProcessingEnvironment env, @Nonnull Element element)
        throws AbortException {}
  }

  @CheckForNull
  private DataProcessor data;

  private static class DataProcessor extends SchedDiscover {
    @Nonnull
    private final ProcessingEnvironment env;

    private DataProcessor(@Nonnull ProcessingEnvironment env) {
      this.env = env;
    }

    private void add(@Nonnull String sup, @Nonnull TypeElement element) {
      add(sup, env.getElementUtils().getBinaryName(element).toString());
    }

    private void add(@Nonnull String sup, @Nonnull TypeElement element, @Nonnull String extra) {
      add(sup, env.getElementUtils().getBinaryName(element).toString(), extra);
    }

    private void remove(@Nonnull String sup, @Nonnull TypeElement element) {
      remove(sup, env.getElementUtils().getBinaryName(element).toString());
    }

    @Override
    public void readResource(@Nonnull BufferedReader reader) throws IOException {
      super.readResource(reader);

      for (Set<SchedData> set : map.values()) {
        Iterator<SchedData> iter = set.iterator();
        while (iter.hasNext()) {
          String name = iter.next().getName();

          TypeElement te = env.getElementUtils().getTypeElement(name);
          if (te == null) {
            iter.remove();
          }
        }
      }
    }

    @Override
    public void writeResource(@Nonnull Writer writer) throws IOException {
      writeResource(writer, SchedAnnotationProcessor.class.getCanonicalName());
    }
  }

  @Override
  public synchronized void init(ProcessingEnvironment env) {
    this.env = env;

    this.data = new DataProcessor(env);
    try {
      FileObject fo =
          env.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", data.getResourceName());
      BufferedReader reader = new BufferedReader(new InputStreamReader(fo.openInputStream()));
      try {
        data.readResource(reader);
      } finally {
        reader.close();
      }
    } catch (IOException e) {
      // Best effort
    }

    Items.init(env);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      // Dump resource file
      assert data != null;
      assert env != null;

      try {
        OutputStream os = env.getFiler()
            .createResource(StandardLocation.CLASS_OUTPUT, "", data.getResourceName())
            .openOutputStream();
        Writer writer = new OutputStreamWriter(os);
        try {
          data.writeResource(writer);
        } finally {
          writer.close();
        }
      } catch (IOException e) {
        env.getMessager().printMessage(Kind.ERROR,
            "Can not write resource file for '" + data.getResourceName() + "': " + e.getMessage());
      }
    } else {
      // Process annotations
      try {
        processAnnotations(annotations, roundEnv);
      } catch (AbortException e) {
      }
    }

    return false;
  }

  private void processAnnotations(@Nonnull Set<? extends TypeElement> annotations,
      @Nonnull RoundEnvironment roundEnv) throws AbortException {
    assert env != null;

    // Reset cached Items
    Items.reset();

    //
    // @Description
    //

    for (Element element : getElementsAnnotatedWith(roundEnv, Items.DESCRIPTION)) {
      assert data != null;

      TypeMirror type = element.asType();

      if (type.getKind() == TypeKind.DECLARED) {
        for (Items item : Items.values()) {
          assert env != null;
          if (env.getTypeUtils().isAssignable(type, item.getTypeMirror())) {
            item.check(env, element);
            data.add(item.getFQName(), (TypeElement) element);
          } else {
            data.remove(item.getFQName(), (TypeElement) element);
          }
        }
      }
    }

    //
    // @HasKeyId
    //

    for (Element element : getElementsAnnotatedWith(roundEnv, Items.HASKEYID)) {
      assert data != null;

      TypeMirror type = element.asType();

      if (type.getKind() == TypeKind.DECLARED) {
        Items.HASKEYID.check(env, element);
        data.add(Items.HASKEYID.getFQName(), (TypeElement) element);
      }
    }

    //
    // @ImplementationName
    //

    for (Element element : getElementsAnnotatedWith(roundEnv, Items.IMPLEMENTATION_NAME)) {
      assert env != null;
      assert data != null;

      TypeMirror elementType = element.asType();

      if (elementType.getKind() == TypeKind.DECLARED) {
        for (AnnotationMirror am : element.getAnnotationMirrors()) {
          if (env.getTypeUtils().isSameType(am.getAnnotationType(),
              Items.IMPLEMENTATION_NAME.getTypeMirror())) {
            // Found annotation
            AnnotationValue name = null;
            TypeMirror iface = null;

            // Search for attributes
            for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : am
                .getElementValues().entrySet()) {
              String attributeName = entry.getKey().getSimpleName().toString();

              if (attributeName.equals("name")) {
                // Found @Implementation.name()
                name = entry.getValue();
              }

              if (attributeName.equals("iface")) {
                // Found @Implementation.iface()
                iface = (TypeMirror) entry.getValue().getValue();
              }
            }

            if (iface != null && name != null) {
              String ifaceName = iface.toString();

              data.remove(ifaceName, (TypeElement) element);

              // Search for duplicate name
              for (com.android.sched.build.SchedDiscover.SchedData elt : data.get(ifaceName)) {
                if (name.getValue().equals(elt.getExtra())) {
                  env.getMessager().printMessage(Kind.ERROR,
                      "Same name '" + name.getValue() + "' on '" + elt.getName() + "'", element,
                      am, name);
                }
              }

              // Check inheritance
              if (!env.getTypeUtils().isAssignable(element.asType(), iface)) {
                env.getMessager().printMessage(Kind.ERROR,
                    "Must extends or implements '" + ifaceName + "'", element);
              }

              data.add(ifaceName, (TypeElement) element, name.getValue().toString());
            } else {
              env.getMessager().printMessage(
                  Kind.ERROR,
                  "Wrong @" + Items.IMPLEMENTATION_NAME.getFQName()
                      + " annotation, must have 'iface' and 'name' attributes");
            }
          }
        }
      }
    }
  }

  @Nonnull
  private Set<? extends Element> getElementsAnnotatedWith(@Nonnull RoundEnvironment roundEnv,
      @Nonnull Items item) throws AbortException {
    assert env != null;

    return roundEnv.getElementsAnnotatedWith(item.getTypeElement());
  }
}
