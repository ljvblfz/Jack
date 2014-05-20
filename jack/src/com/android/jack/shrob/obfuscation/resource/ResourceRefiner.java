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

package com.android.jack.shrob.obfuscation.resource;

import com.android.jack.Options;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JPackageLookupException;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.ast.Resource;
import com.android.jack.ir.naming.CompositeName;
import com.android.jack.ir.naming.PackageName;
import com.android.jack.ir.naming.TypeName;
import com.android.jack.ir.naming.TypeName.Kind;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.jack.shrob.spec.FilterSpecification;
import com.android.jack.shrob.spec.Flags;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.vfs.VPath;

import java.util.Iterator;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A {@link RunnableSchedulable} that refines resource names when necessary.
 */
@Description("Refines resource names when necessary")
@Constraint(need = OriginalNames.class)
public class ResourceRefiner implements RunnableSchedulable<JSession>{

  @Nonnull
  private final Flags flags = ThreadConfig.get(Options.FLAGS);

  private static final char BINARY_QN_SEPARATOR = '/';

  private static final char SHROB_PATH_SEPARATOR = '/';

  @CheckForNull
  private CharSequence getResourceRefinedName(@Nonnull VPath resPath,
      @Nonnull JPackage topLevelPackage) {
    JPackage currentPackage = topLevelPackage;
    Iterator<String> iterator = resPath.split().iterator();
    String name = null;
    while (iterator.hasNext()) {
      name = iterator.next();
      if (iterator.hasNext()) {
        // All but the last entry can only be matched with a package
        try {
          currentPackage = currentPackage.getSubPackage(name);
        } catch (JPackageLookupException e) {
          // The package does not exist, no need to search further.
          break;
        }
      } else {
        // The last split entry is the resource name which can only be matched with a type
        int index = name.indexOf('.');
        if (index != -1) {
          String typeName = name.substring(0, index);
          String extension = name.substring(index);
          try {
            JType type = currentPackage.getType(typeName);
            CompositeName refinedName =
                new CompositeName(new TypeName(Kind.BINARY_QN, type), extension);
            return refinedName;
          } catch (JTypeLookupException typeException) {
            // The type does not exist, no need to search further.
          }
        }
      }
    }
    // No type refining was possible, currentPackage is the deepest package found that matches the
    // beginning of the resource name and the iterator points to the renaming name parts of the
    // resource.

    if (currentPackage == topLevelPackage) {
      // No package was found, no need to refine.
      return null;
    }

    // Construct remaining resource name
    StringBuilder sb = new StringBuilder();
    sb.append(BINARY_QN_SEPARATOR);
    assert name != null;
    sb.append(name);
    while (iterator.hasNext()) {
      sb.append(BINARY_QN_SEPARATOR);
      sb.append(iterator.next());
    }
    CompositeName refinedName = new CompositeName(
        new PackageName(com.android.jack.ir.naming.PackageName.Kind.BINARY_QN, currentPackage),
        sb.toString());
    return refinedName;
  }

  @Override
  public void run(@Nonnull JSession session) throws Exception {
    FilterSpecification adaptResourceFileNames = flags.getAdaptResourceFileNames();
    if (adaptResourceFileNames != null) {
      for (Resource res : session.getResources()) {
        VPath resName = res.getPath();
        if (adaptResourceFileNames.matches(resName.getPathAsString(SHROB_PATH_SEPARATOR))) {
          CharSequence refinedName = getResourceRefinedName(resName, session.getTopLevelPackage());
          if (refinedName != null) {
            VPath vPath = new VPath(refinedName, BINARY_QN_SEPARATOR);
            assert vPath.equals(resName);
            res.setPath(vPath);
          }
        }
      }
    }
  }
}
