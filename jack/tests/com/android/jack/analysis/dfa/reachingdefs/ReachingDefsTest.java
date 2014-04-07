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

package com.android.jack.analysis.dfa.reachingdefs;

import com.android.jack.Main;
import com.android.jack.Options;
import com.android.jack.TestTools;
import com.android.jack.analysis.DefinitionMarker;
import com.android.jack.cfg.BasicBlock;
import com.android.jack.cfg.ControlFlowGraph;
import com.android.jack.cfg.SwitchBasicBlock;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JIntLiteral;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.util.filter.SignatureMethodFilter;
import com.android.sched.util.codec.ImplementationName;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.annotation.Nonnull;

/**
 * JUnit test for reaching definitions.
 */
public class ReachingDefsTest {

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @ImplementationName(iface = ReachingDefinitionsChecker.class, name = "test001Checker")
  public static class Test001ReachingDefinitionsChecker implements ReachingDefinitionsChecker {

    @Override
    public void check(@Nonnull JMethod method) {
      ControlFlowGraph cfg = method.getMarker(ControlFlowGraph.class);
      assert cfg != null;

      for (BasicBlock bb : cfg.getNodes()) {
        if (bb instanceof SwitchBasicBlock) {
          Assert.assertEquals(4, bb.getSuccessors().size());
          for (BasicBlock succ : bb.getSuccessors()) {
            ReachingDefsMarker rdm = succ.getMarker(ReachingDefsMarker.class);
            assert rdm != null;
            boolean foundResultDef = false;
            for (DefinitionMarker dm : rdm.getReachingDefs()) {
              if (dm.hasValue()) {
                JAsgOperation asg = (JAsgOperation) dm.getDefinition();
                if (asg.getLhs() instanceof JVariableRef
                    && ((JVariableRef) asg.getLhs()).getTarget().getName().equals("result")
                    && asg.getRhs() instanceof JIntLiteral) {
                  foundResultDef = true;
                }
              }
            }
            Assert.assertTrue(foundResultDef);
          }
        }
      }
    }
  }

  @Test
  public void testDfa001() throws Exception {
    Options options = TestTools.buildCommandLineArgs(TestTools
        .getJackTestsWithJackFolder("analysis/dfa/reachingdefs/test001"));
    options.addProperty(Options.METHOD_FILTER.getName(), "method-with-signature");
    options.addProperty(SignatureMethodFilter.METHOD_SIGNATURE_FILTER.getName(),
        "dfaWithSwitch(I)I");
    options.addProperty(ReachingDefinitions.REACHING_DEFS_CHECKER.getName(), "test001Checker");
    TestTools.runCompilation(options);
  }
}
