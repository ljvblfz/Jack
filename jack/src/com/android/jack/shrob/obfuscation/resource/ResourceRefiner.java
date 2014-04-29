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
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.ast.Resource;
import com.android.jack.ir.naming.CompositeName;
import com.android.jack.ir.naming.TypeName;
import com.android.jack.ir.naming.TypeName.Kind;
import com.android.jack.lookup.JLookup;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.jack.shrob.spec.FilterSpecification;
import com.android.jack.shrob.spec.Flags;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;

/**
 * A {@link RunnableSchedulable} that refines resource names when necessary.
 */
@Description("Refines resource names when necessary")
@Constraint(need = OriginalNames.class)
public class ResourceRefiner implements RunnableSchedulable<JSession>{

  @Nonnull
  private final Flags flags = ThreadConfig.get(Options.FLAGS);

  @Override
  public void run(@Nonnull JSession session) throws Exception {
    FilterSpecification adaptResourceFileNames = flags.getAdaptResourceFileNames();
    if (adaptResourceFileNames != null) {
      JLookup lookup = session.getLookup();
      for (Resource res : session.getResources()) {
        String resName = res.getName().toString();
        if (adaptResourceFileNames.matches(resName)) {
          int index = resName.indexOf('.');
          if (index != -1) {
            String typeName = resName.substring(0, index);
            String extension = resName.substring(index, resName.length());
            try {
              JType type = lookup.getType(typeName);
              res.setName(
                  new CompositeName(new TypeName(Kind.BINARY_QN, type), extension));
              assert res.getName().toString().equals(resName);
            } catch (JTypeLookupException e) {
              // Ignored
            }
          }
        }
      }
    }
  }
}
