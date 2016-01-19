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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldId;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JPhantomClassOrInterface;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.reporting.Reportable;
import com.android.jack.reporting.Reporter.Severity;
import com.android.jack.shrob.proguard.GrammarActions;
import com.android.jack.shrob.seed.SeedMarker;
import com.android.jack.shrob.spec.Flags;
import com.android.sched.item.Description;
import com.android.sched.marker.MarkerManager;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;

import javax.annotation.Nonnull;

/**
 * Marks all classes and members that will not be renamed when obfuscating.
 */
@Description("Marks all classes and members that will not be renamed when obfuscating.")
@Transform(add = KeepNameMarker.class)
public class NameKeeper implements RunnableSchedulable<JPackage> {
  @Nonnull
  private static final Joiner typeNameJoiner = Joiner.on(", ");

  private static class KeepedPackageBecauseOfPhantom implements Reportable {
    @Nonnull
    private final JPackage pack;

    public KeepedPackageBecauseOfPhantom(@Nonnull JPackage pack) {
      this.pack = pack;
    }

    @Override
    @Nonnull
    public String getMessage() {
      return "Obfuscation: force to keep package name '"
          + Jack.getUserFriendlyFormatter().getName(pack)
          + "' due to unknown referenced types "
          + typeNameJoiner.join(
              Iterables.transform(pack.getPhantoms(),
                  new Function<JPhantomClassOrInterface, String>() {
                @Override
                public String apply(JPhantomClassOrInterface arg0) {
                  return Jack.getUserFriendlyFormatter().getName(arg0);
                }
              }));
    }

    @Override
    @Nonnull
    public ProblemLevel getDefaultProblemLevel() {
      return ProblemLevel.WARNING;
    }
  }

  private class Visitor extends JVisitor {

    @Override
    public boolean visit(@Nonnull JPackage pack) {
      if (!pack.getPhantoms().isEmpty()) {
        Jack.getSession().getReporter().report(Severity.NON_FATAL,
            new KeepedPackageBecauseOfPhantom(pack));
        keepName(pack);
      } else if (flags.getKeepPackageNames() != null) {
        if (!isMarked(pack)) {
          if (flags.getKeepPackageNames()
              .matches(GrammarActions.getSourceFormatter().getName(pack))) {
            keepName(pack);
          }
        }
      }

      // Overload visit of package to force loading of sub-classes.
      for (JDefinedClassOrInterface subType : pack.getTypes()) {
        subType.traverse(this);
      }
      return false;
   }

    @Override
    public boolean visit(@Nonnull JDefinedClassOrInterface clOrI) {
      if (clOrI.isExternal()) {
        keepName(clOrI);
        for (JMethod m : clOrI.getMethods()) {
          keepName(m);
        }
        return false;
      } else {
        SeedMarker marker = clOrI.getMarker(SeedMarker.class);
        if (marker != null && !marker.getModifier().allowObfuscation()) {
          keepName(clOrI);
        }
        for (JField field : clOrI.getFields()) {
          marker = field.getMarker(SeedMarker.class);
          if (marker != null && !marker.getModifier().allowObfuscation()) {
            keepName(field);
          }
        }

        for (JMethod method : clOrI.getMethods()) {
          marker = method.getMarker(SeedMarker.class);
          if (marker != null && !marker.getModifier().allowObfuscation()) {
            keepName(method);
          }
        }
        return super.visit(clOrI);
      }
    }

    @Override
    public boolean visit(@Nonnull JMethod m) {
      if (JMethod.isClinit(m) || m instanceof JConstructor) {
        keepName(m);
      }
      return false;
    }
  }

  private boolean markIfNecessary(@Nonnull MarkerManager node) {
    synchronized (node) {
      if (!isMarked(node)) {
        node.addMarker(new KeepNameMarker());
        return true;
      }
      return false;
    }
  }

  private boolean isMarked(@Nonnull MarkerManager node) {
    synchronized (node) {
      return node.containsMarker(KeepNameMarker.class);
    }
  }

  private void keepName(@Nonnull JPackage pack) {
    if (markIfNecessary(pack)) {
      JPackage enclosingPackage = pack.getEnclosingPackage();
      if (enclosingPackage != null) {
        keepName(enclosingPackage);
      }
    }
  }

  private void keepName(@Nonnull JDefinedClassOrInterface type) {
    if (markIfNecessary(type)) {
      keepName(type.getEnclosingPackage());

      for (JField field : type.getFields()) {
        SeedMarker marker = field.getMarker(SeedMarker.class);
        if (marker != null && !marker.getModifier().allowObfuscation()) {
          keepName(field);
        }
      }

      for (JMethod method : type.getMethods()) {
        SeedMarker marker = method.getMarker(SeedMarker.class);
        if (marker != null && !marker.getModifier().allowObfuscation()) {
          keepName(method);
        }
      }
    }
  }

  private void keepName(JMethod method) {
    JMethodIdWide methodId = method.getMethodIdWide();
    markIfNecessary(methodId);
  }

  private void keepName(JField field) {
    JFieldId fieldId = field.getId();
    markIfNecessary(fieldId);
  }

  @Override
  public void run(@Nonnull JPackage pack) throws Exception {
    new Visitor().accept(pack);
  }

  @Nonnull
  protected final Tracer tracer = TracerFactory.getTracer();

  @Nonnull
  private final Flags flags = ThreadConfig.get(Options.FLAGS);
}
