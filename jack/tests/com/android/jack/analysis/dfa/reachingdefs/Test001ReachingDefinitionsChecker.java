package com.android.jack.analysis.dfa.reachingdefs;

import com.android.jack.analysis.DefinitionMarker;
import com.android.jack.cfg.BasicBlock;
import com.android.jack.cfg.ControlFlowGraph;
import com.android.jack.cfg.SwitchBasicBlock;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JIntLiteral;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JVariableRef;
import com.android.sched.util.codec.ImplementationName;

import junit.framework.Assert;

import javax.annotation.Nonnull;

@ImplementationName(iface = ReachingDefinitionsChecker.class, name = "test001Checker")
public class Test001ReachingDefinitionsChecker implements ReachingDefinitionsChecker {

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