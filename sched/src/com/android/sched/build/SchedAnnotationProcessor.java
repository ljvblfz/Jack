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

import com.android.sched.item.Description;
import com.android.sched.item.Feature;
import com.android.sched.item.Production;
import com.android.sched.item.TagOrMarkerOrComponent;
import com.android.sched.marker.Marker;
import com.android.sched.schedulable.Schedulable;
import com.android.sched.util.codec.ImplementationName;
import com.android.sched.util.config.HasKeyId;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
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
import javax.lang.model.element.ExecutableElement;
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

  private enum Items {
    HASKEYID(HasKeyId.class),
    MARKER(Marker.class),
    FEATURE(Feature.class),
    PRODUCTION(Production.class),
    TOMOC(TagOrMarkerOrComponent.class),
    SCHEDULABLE(Schedulable.class);

    @Nonnull
    private final Class<?> cls;

    private Items(@Nonnull Class<?> cls) {
      this.cls = cls;
    }

    @Nonnull
    public String getFQName() {
      return cls.getCanonicalName();
    }

    public void check(@Nonnull Element element, @Nonnull ProcessingEnvironment env) {}
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
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
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
      assert env != null;

      TypeMirror[] types = new TypeMirror[Items.values().length];

      for (Items item : Items.values()) {
        TypeElement element = env.getElementUtils().getTypeElement(item.getFQName());
        if (element != null) {
          assert types != null;
          types[item.ordinal()] = element.asType();
        } else {
          env.getMessager().printMessage(
              Kind.ERROR, "Can not get element type '" + Marker.class.getCanonicalName() + "'");
        }
      }

      //
      // @Description
      //

      for (Element element : getElementsAnnotatedWith(roundEnv, Description.class)) {
        assert data != null;

        TypeMirror type = element.asType();

        if (type.getKind() == TypeKind.DECLARED) {
          for (Items item : Items.values()) {
            assert types != null;
            TypeMirror sup = types[item.ordinal()];

            assert env != null;
            if (sup != null && env.getTypeUtils().isAssignable(type, sup)) {
              item.check(element, env);
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

      for (Element element : getElementsAnnotatedWith(roundEnv, HasKeyId.class)) {
        assert data != null;

        TypeMirror type = element.asType();

        if (type.getKind() == TypeKind.DECLARED) {
          data.add(Items.HASKEYID.getFQName(), (TypeElement) element);
        }
      }

      //
      // @ImplementationName
      //

      for (Element element : getElementsAnnotatedWith(roundEnv, ImplementationName.class)) {
        assert env != null;
        assert data != null;

        TypeMirror elementType = element.asType();

        if (elementType.getKind() == TypeKind.DECLARED) {
          TypeMirror implementationNameType = env.getElementUtils()
              .getTypeElement(ImplementationName.class.getCanonicalName()).asType();

          for (AnnotationMirror am : element.getAnnotationMirrors()) {
            if (env.getTypeUtils().isSameType(am.getAnnotationType(), implementationNameType)) {
              // Found annotation
              AnnotationValue name = null;
              TypeMirror iface = null;

              // Search for attributes
              for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                  am.getElementValues().entrySet()) {
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
                  env.getMessager().printMessage(
                      Kind.ERROR, "Must extends or implements '" + ifaceName + "'", element);
                }

                data.add(ifaceName, (TypeElement) element, name.getValue().toString());
              } else {
                env.getMessager().printMessage(Kind.ERROR, "Wrong @"
                    + ImplementationName.class.getCanonicalName()
                    + " annotation, must have 'iface' and 'name' attributes");
              }
            }
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
