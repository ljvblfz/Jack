/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.sched.marker;

import com.android.sched.item.Description;
import com.android.sched.item.Items;
import com.android.sched.util.log.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Represents a {@link Marker} with all annotations extracted.
 */
public class ManagedMarker {
  private static final Logger logger = LoggerFactory.getLogger();

  // @Name
  @Nonnull
  private final String name;

  // @Description
  @Nonnull
  private final String description;

  // Source marker
  @Nonnull
  private final Class<? extends Marker> marker;

  // @ValidOn
  @Nonnull
  private Class<? extends AbstractMarkerManager>[] staticValidOn;

  // @DynamicValidOn
  @Nonnull
  private List<InternalDynamicValidOn> dynamicValidOn;

  /**
   * Represents an extracted {@link DynamicValidOn} annotation.
   */
  public static class InternalDynamicValidOn {
    public Class<? extends AbstractMarkerManager> getValidOn() {
      return validOn;
    }

    public Method getMethod() {
      return method;
    }

    private Class<? extends AbstractMarkerManager> validOn;
    private Method method;

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();

      sb.append(validOn);
      sb.append(" ('");
      sb.append(method);
      sb.append("')");

      return new String(sb);
    }
  }

  public ManagedMarker(@Nonnull Class<? extends Marker> marker) throws MarkerNotConformException {
    this.marker = marker;
    this.name = Items.getName(marker);

    // FINDBUGS
    String description = Items.getDescription(marker);
    if (description == null) {
      throw new MarkerNotConformException("Marker '" + marker.getCanonicalName()
          + "' must have a @" + Description.class.getSimpleName());
    }
    this.description = description;

    extractAnnotation(marker);

    logger.log(Level.CONFIG, "{0}", this);
  }

  @Nonnull
  public String getName() {
    return name;
  }

  @Nonnull
  public String getDescription() {
    return description;
  }

  @Nonnull
  public Class<? extends AbstractMarkerManager>[] getStaticValidOn() {
    return staticValidOn.clone();
  }

  @Nonnull
  public List<InternalDynamicValidOn> getDynamicValidOn() {
    return dynamicValidOn;
  }

  public boolean isValidMarker(@Nonnull AbstractMarkerManager marked) {
    for (Class<? extends AbstractMarkerManager> cls : staticValidOn) {
      if (cls.isAssignableFrom(marked.getClass())) {
        return true;
      }
    }

    for (InternalDynamicValidOn dvo : dynamicValidOn) {
      if (dvo.validOn.isAssignableFrom(marked.getClass())) {
        return true;
      }
    }

    return false;
  }

  public boolean isValidMarker(@Nonnull AbstractMarkerManager marked, @Nonnull Marker m) {
    for (Class<? extends AbstractMarkerManager> cls : staticValidOn) {
      if (cls.isAssignableFrom(marked.getClass())) {
        return true;
      }
    }

    for (InternalDynamicValidOn dvo : dynamicValidOn) {
      if (dvo.validOn.isAssignableFrom(marked.getClass())) {
        try {
          return ((Boolean) dvo.method.invoke(m, marked)).booleanValue();
        } catch (IllegalArgumentException e) {
          logger.log(Level.SEVERE, "Program can not be here", e);
          throw new AssertionError(e);
        } catch (IllegalAccessException e) {
          logger.log(Level.SEVERE, "Program can not be here", e);
          throw new AssertionError(e);
        } catch (InvocationTargetException e) {
          logger.log(Level.WARNING, "Method '" + dvo.method + "' threw an exception", e.getCause());

          return false;
        }
      }
    }

    return false;
  }

  @Nonnull
  public Class<? extends Marker> getMarker() {
    return marker;
  }

  @Nonnull
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("Marker '");
    sb.append(name);
    sb.append("' (");
    sb.append(description);
    sb.append("), static valid on [");

    boolean first = true;
    for (Class<? extends AbstractMarkerManager> cls : staticValidOn) {
      if (!first) {
        sb.append(", ");
      } else {
        first = false;
      }

      sb.append(cls.getCanonicalName());
    }
    sb.append("], dynamic valid on [");

    first = true;
    for (InternalDynamicValidOn dvo : dynamicValidOn) {
      if (!first) {
        sb.append(", ");
      } else {
        first = false;
      }

      sb.append(dvo.validOn.getCanonicalName());
      sb.append(" (");
      sb.append(dvo.method);
      sb.append(")");
    }
    sb.append(']');

    return new String(sb);
  }

  @SuppressWarnings("unchecked")
  private void extractAnnotation(@Nonnull Class<? extends Marker> m) {
    ValidOn validOnAnnotation = m.getAnnotation(ValidOn.class);
    if (validOnAnnotation != null) {
      if (validOnAnnotation.value().length == 0) {
        throw new MarkerNotConformException("Annotation @" + ValidOn.class.getSimpleName()
            + " on class '" + m.getCanonicalName() + "' must have at least one parameter");
      }

      staticValidOn = validOnAnnotation.value();
    } else {
      staticValidOn = new Class[0];
    }

    dynamicValidOn = new ArrayList<InternalDynamicValidOn>();
    for (Method method : m.getMethods()) {
      DynamicValidOn dynamicValidOnAnnotation = method.getAnnotation(DynamicValidOn.class);

      if (dynamicValidOnAnnotation != null) {
        if (!method.getReturnType().equals(Boolean.TYPE)) {
          throw new MarkerNotConformException("Annotated method '" + method + "' with @"
              + DynamicValidOn.class.getSimpleName() + " must have a 'boolean' return type");
        }

        if (method.getParameterTypes().length != 1) {
          throw new MarkerNotConformException("Annotated method '" + method + "' with @"
              + DynamicValidOn.class.getSimpleName() + " must have a single parameter");
        }

        if (!AbstractMarkerManager.class.isAssignableFrom(method.getParameterTypes()[0])) {
          throw new MarkerNotConformException("Annotated method '" + method + "' with @"
              + DynamicValidOn.class.getSimpleName() + " must have a parameter assignable from "
              + AbstractMarkerManager.class.getSimpleName());
        }

        for (Class<? extends AbstractMarkerManager> marked : staticValidOn) {
          if (marked.isAssignableFrom(method.getParameterTypes()[0])) {
            throw new MarkerNotConformException("Marker '" + name + "' cannot have both a static @'"
                + ValidOn.class.getName() + " (on class '" + marker.getCanonicalName()
                + "') and a @" + DynamicValidOn.class.getName() + " (on method '" + method + "'");
          }
        }

        for (InternalDynamicValidOn dvo : dynamicValidOn) {
          if (dvo.validOn.isAssignableFrom(method.getParameterTypes()[0])
              || method.getParameterTypes()[0].isAssignableFrom(dvo.validOn)) {
            throw new MarkerNotConformException("Marker '" + name
                + "' could not have two @'" + DynamicValidOn.class.getName() + " ('" + method
                + "' and '" + dvo.method + "')");
          }
        }

        InternalDynamicValidOn dvo = new InternalDynamicValidOn();

        dvo.validOn = (Class<? extends AbstractMarkerManager>) (method.getParameterTypes()[0]);
        dvo.method = method;
        dynamicValidOn.add(dvo);
      }
    }
  }
}
