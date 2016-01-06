/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.util;

import com.android.jack.analysis.DefinitionMarker;
import com.android.jack.analysis.UseDefsMarker;
import com.android.jack.analysis.UsedVariableMarker;
import com.android.jack.analysis.dfa.reachingdefs.ReachingDefsMarker;
import com.android.jack.cfg.BasicBlock;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JVariableRef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Helpers related to optimizations.
 */
public class OptimizationTools {

  @Nonnull
  public static List<DefinitionMarker> getReachingDefs(@Nonnull BasicBlock bb) {
    ReachingDefsMarker rdm = bb.getMarker(ReachingDefsMarker.class);
    assert rdm != null;
    return rdm.getReachingDefs();
  }

  @Nonnull
  public static List<DefinitionMarker> getReachingDefs(@Nonnull BasicBlock bb,
      @Nonnull JVariable var) {
    List<DefinitionMarker> reachingDefs = new ArrayList<DefinitionMarker>();

    for (DefinitionMarker reachingDef : getReachingDefs(bb)) {
      if (reachingDef.getDefinedVariable() == var) {
        reachingDefs.add(reachingDef);
      }
    }

    return reachingDefs;
  }

  @Nonnull
  public static List<DefinitionMarker> getUsedDefinitions(@Nonnull JVariableRef varRef) {
    UseDefsMarker udm = varRef.getMarker(UseDefsMarker.class);
    assert udm != null;
    return udm.getDefs();
  }

  @Nonnull
  public static List<JVariableRef> getUsedVariables(@Nonnull JStatement stmt) {
    UsedVariableMarker markerOfVarsUsedBys1 = stmt.getMarker(UsedVariableMarker.class);

    if (markerOfVarsUsedBys1 != null) {
      return markerOfVarsUsedBys1.getUsedVariables();
    }

    return Collections.emptyList();
  }
}
