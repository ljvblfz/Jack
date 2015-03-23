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

import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.Resource;
import com.android.jack.ir.naming.TypeName;
import com.android.jack.ir.naming.TypeName.Kind;
import com.android.jack.lookup.JLookup;
import com.android.jack.lookup.JLookupException;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.jack.shrob.proguard.GrammarActions;
import com.android.jack.shrob.spec.FilterSpecification;
import com.android.jack.util.NamingTools;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.VPath;

import java.io.InputStreamReader;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A {@link RunnableSchedulable} that refines string corresponding to type names in resources.
 */
@Description("Refines string corresponding to type names in resources")
@Constraint(need = OriginalNames.class)
public class ResourceContentRefiner implements RunnableSchedulable<JSession> {

  @Nonnull
  private final JLookup lookup = Jack.getSession().getLookup();

  @CheckForNull
  private final FilterSpecification adaptResourceFileContents =
      ThreadConfig.get(Options.FLAGS).getAdaptResourceFileContents();

  @Override
  public void run(@Nonnull JSession session) throws Exception {
    assert adaptResourceFileContents != null;
    List<Resource> resources = session.getResources();
    for (Resource res : resources) {
      VPath resName = res.getPath();
      if (adaptResourceFileContents.matches(
          resName.getPathAsString(GrammarActions.SHROB_REGEX_PATH_SEPARATOR))) {
        InputStreamReader reader = null;
        InputVFile originalVFile = res.getVFile();
        RefinedVFile refinedVFile = new RefinedVFile(originalVFile);
        int position = 0;
        try {
          reader = new InputStreamReader(originalVFile.getInputStream());
          int c = reader.read();
          while (c != -1) {
            if (Character.isJavaIdentifierStart(c)) {
              // Reading first character of a potential type name
              StringBuilder sb = new StringBuilder();
              sb.append((char) c);
              int startPosition = position;
              c = reader.read();
              position++;
              while (Character.isJavaIdentifierPart(c) || c == '.' || c == '-') {
                // Reading the next characters
                sb.append((char) c);
                c = reader.read();
                position++;
              }
              String signatureName = NamingTools.getTypeSignatureName(sb.toString());
              if (NamingTools.isClassDescriptor(signatureName)) {
                try {
                  JType type = lookup.getType(signatureName);
                  // A matching type was found, the resource and the type will be linked
                  refinedVFile.addRefinedEntry(startPosition, position - 1,
                      new TypeName(Kind.SRC_QN, type));
                } catch (JLookupException e) {
                  // The string was not a valid type, do not replace it.
                }
              }

            }
            c = reader.read();
            position++;
          }
          res.setVFile(refinedVFile);
        } finally {
          if (reader != null) {
            reader.close();
          }
        }
      }
    }
  }

}
