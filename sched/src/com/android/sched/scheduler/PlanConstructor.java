/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.sched.scheduler;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

import com.android.sched.item.Component;
import com.android.sched.item.TagOrMarkerOrComponent;
import com.android.sched.util.findbugs.SuppressFBWarnings;
import com.android.sched.util.log.Event;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.log.SchedEventType;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A {@link PlanCandidate}.
 */
public class PlanConstructor<T extends Component>  implements PlanCandidate<T> {
  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  private static class Decorated {
    @CheckForNull
    protected TagOrMarkerOrComponentSet afterTags;
    @CheckForNull
    protected TagOrMarkerOrComponentSet needToAdd;
    @CheckForNull
    protected TagOrMarkerOrComponentSet needToRemove;

    protected Decorated() {
    }

    public Decorated(@Nonnull TagOrMarkerOrComponentSet initial) {
      this.afterTags = initial.clone();
    }

    public Decorated(@Nonnull TagOrMarkerOrComponentSet needed,
        @Nonnull TagOrMarkerOrComponentSet forbidden) {
      this.needToAdd = needed.clone();
      this.needToRemove = forbidden.clone();
    }

    @Nonnull
    public TagOrMarkerOrComponentSet getAfterTags() {
      assert afterTags != null;
      return afterTags;
    }

    @Nonnull
    public TagOrMarkerOrComponentSet getNeedToAdd() {
      assert needToAdd != null;
      return needToAdd;
    }

    @Nonnull
    public TagOrMarkerOrComponentSet getNeedToRemove() {
      assert needToRemove != null;
      return needToRemove;
    }

    public boolean updateNeedToAdd(@Nonnull TagOrMarkerOrComponentSet neededAfter) {
      if (needToAdd == null) {
        needToAdd = neededAfter.clone();
        needToAdd.clear();
      }

      TagOrMarkerOrComponentSet set = neededAfter.clone();

      try {
        return needToAdd.equals(set);
      } finally {
        needToAdd = set;
      }
    }

    public boolean updateNeedToRemove(@Nonnull TagOrMarkerOrComponentSet forbiddenAfter) {
      if (needToRemove == null) {
        needToRemove = forbiddenAfter.clone();
        forbiddenAfter.clear();
      }

      TagOrMarkerOrComponentSet set = forbiddenAfter.clone();

      try {
        return needToRemove.equals(set);
      } finally {
        needToRemove = set;
      }
    }

    @Override
    @Nonnull
    public String toString() {
      return ((afterTags != null) ? "<initial>" : "<final>") + ", needToAdd: " + getNeedToAdd()
          + ", needToRemove: " + getNeedToRemove() + ", afterTags: "
          + ((afterTags != null) ? getAfterTags() : "<none>");
    }
  }

  private class DecoratedRunner extends Decorated {
    @Nonnull
    private final ManagedRunnable runner;

    public DecoratedRunner(@Nonnull ManagedRunnable runner) {
      this.runner = runner;
    }

    @Nonnull
    public ManagedRunnable getRunner() {
      return runner;
    }

    public boolean updateBeforeTags(@Nonnull TagOrMarkerOrComponentSet before) {
      if (afterTags == null) {
        afterTags = before.clone();
        afterTags.clear();
      }

      TagOrMarkerOrComponentSet set = before.clone();
      set.addAll(runner.getAddedTags());
      set.removeAll(runner.getRemovedTags());

      try {
        return afterTags.equals(set);
      } finally {
        afterTags = set;
      }
    }

    @Override
    public boolean updateNeedToAdd(@Nonnull TagOrMarkerOrComponentSet needToAddAfter) {
      if (needToAdd == null) {
        needToAdd = needToAddAfter.clone();
        needToAdd.clear();
      }

      TagOrMarkerOrComponentSet set = needToAddAfter.clone();
      set.removeAll(runner.getAddedTags());
      set.addAll(runner.getNeededTags(features));

      try {
        return needToAdd.equals(set);
      } finally {
        needToAdd = set;
      }
    }

    @Override
    public boolean updateNeedToRemove(@Nonnull TagOrMarkerOrComponentSet needToRemoveAfter) {
      if (needToRemove == null) {
        needToRemove = needToRemoveAfter.clone();
        needToRemove.clear();
      }

      TagOrMarkerOrComponentSet set = needToRemoveAfter.clone();
      set.removeAll(runner.getRemovedTags());
      set.addAll(runner.getUnsupportedTags(features));

      try {
        return needToRemove.equals(set);
      } finally {
        needToRemove = set;
      }
    }

    @Override
    @Nonnull
    public String toString() {
      return runner.getName() + ", needToAdd: " + getNeedToAdd() + ", needToRemove: "
          + getNeedToRemove() + ", afterTags: " + getAfterTags();
    }
  }

  @Nonnull
  private final List<Decorated> plan;

  @Nonnegative
  private int currentTagValidityIdx = 0;
  // STOPSHIP Use this
  @SuppressWarnings("unused")
  private int constraintTagValidityIdx = Integer.MAX_VALUE;

  @Nonnull
  private final ProductionSet missingProductions;

  @Nonnull
  private final FeatureSet features;

  @Nonnull
  private final Request request;
  @Nonnull
  private final Class<T> rootRunOn;

  public PlanConstructor(@Nonnull Request request,
                         @Nonnull Class<T> rootRunOn,
                         @Nonnull PlanBuilder<T> builder) {
    this(request, rootRunOn, builder.getRunners());
  }

  public PlanConstructor(@Nonnull PlanConstructor<T> analyzer,
                         @Nonnull List<ManagedRunnable> plan) {
    this(analyzer.request, analyzer.rootRunOn, plan);
  }

  PlanConstructor(@Nonnull Request request,
                  @Nonnull Class<T> rootRunOn,
                  @Nonnull List<ManagedRunnable> plan) {
    Event event = tracer.start(SchedEventType.ANALYZER);

    try {
      this.request = request;
      this.rootRunOn = rootRunOn;
      this.features = request.getFeatures();
      this.missingProductions = request.getTargetProductions();

      this.plan = new LinkedList<Decorated>();
      this.plan.add(new Decorated(request.getInitialTags()));
      for (ManagedRunnable runner : plan) {
        this.plan.add(new DecoratedRunner(runner));
        this.missingProductions.removeAll(runner.getProductions());
      }
      this.plan.add(new Decorated(request.getTargetIncludeTags(), request.getTargetExcludeTags()));
    } finally {
      event.end();
    }
  }

  private void ensureTagsAtIndex(@Nonnegative int atIdx) {
    // assert atIdx > 0;
    assert atIdx <= plan.size();

    for (int idx = currentTagValidityIdx + 1; idx <= atIdx; idx++) {
      ((PlanConstructor<?>.DecoratedRunner) plan.get(idx))
          .updateBeforeTags(plan.get(idx - 1).getAfterTags());
    }

    currentTagValidityIdx = atIdx;
  }

  private void ensureConstraintsAtIndex(@Nonnegative int atIdx) {
    assert atIdx > 0;
    assert atIdx <= plan.size();

    for (int idx = plan.size() - 2; idx >= 0; idx--) {
      plan.get(idx).updateNeedToAdd(plan.get(idx + 1).getNeedToAdd());
      plan.get(idx).updateNeedToRemove(plan.get(idx + 1).getNeedToRemove());
    }

    constraintTagValidityIdx = atIdx;
  }

  @Override
  public boolean isValid() {
    ensureConstraintsAtIndex(0);

    return plan.get(0).getNeedToAdd().containsAll(request.getInitialTags())
        && plan.get(0).getNeedToRemove().containsNone(request.getInitialTags())
        && missingProductions.isEmpty();
  }

  public boolean isProductionValid(@Nonnull ManagedRunnable runner) {
    return runner.getProductions().containsAll(missingProductions);
  }

  public boolean isConstraintValid(@Nonnegative int index, @Nonnull ManagedRunnable runner) {
    // Skip initial state
    index++;
    // Take state from previous
    index--;
    ensureTagsAtIndex(index);
    return runner.isCompatible(features, plan.get(index).getAfterTags());
  }

  public void insert(@Nonnegative int index, @Nonnull ManagedRunnable runner) {
    assert isConstraintValid(index, runner);

    // Skip initial state
    index++;

    plan.add(index, new DecoratedRunner(runner));
    currentTagValidityIdx = index - 1;
    constraintTagValidityIdx = index;
  }

  public void remove(@Nonnegative int index) {
    // Skip initial state
    index++;

    plan.remove(index);
    currentTagValidityIdx = index - 1;
    constraintTagValidityIdx = index;
  }

  @Override
  @Nonnull
  public
  String getDescription() {
    try {
      return getPlanBuilder().getDescription();
    } catch (IllegalRequestException e) {
      return "Unknown";
    }
  }

  @Override
  @Nonnull
  public String getDetailedDescription() {
    try {
      return getPlanBuilder().getDetailedDescription();
    } catch (IllegalRequestException e) {
      return "Unknown";
    }
  }

  @Override
  @Nonnull
  public PlanBuilder<T> getPlanBuilder() throws IllegalRequestException {
    Event event = tracer.start(SchedEventType.PLANBUILDER);

    try {
      Stack<Class<? extends Component>> runOn = new Stack<Class<? extends Component>>();
      Stack<SubPlanBuilder<? extends Component>> adapters =
          new Stack<SubPlanBuilder<? extends Component>>();

      runOn.push(rootRunOn);
      adapters.push(request.getPlanBuilder(rootRunOn));

      Iterator<Decorated> iter = plan.iterator();
      // Skip the initial state
      Decorated decorated = iter.next();
      ManagedRunnable oldRunner = null;
      while ((decorated = iter.next()) instanceof PlanConstructor.DecoratedRunner) {
        ManagedRunnable runner = ((PlanConstructor<?>.DecoratedRunner) decorated).getRunner();

        // Manage exclusive access at the entry
        // STOPSHIP To Be Rewritten from ...
        if (runner.needsExclusiveAccess()) {
          Class<? extends Component> component = runner.getExclusiveAccess();
          assert component != null;

          if (logger.isLoggable(Level.FINE)
              && !request.getVisitors().containsAdapters(runOn.peek(), component)) {
            logger.log(Level.FINE, "Pop adapters before {0} to gain exclusive access to {1}",
                new Object[] {runner.getName(), component.getName()});
          }
          while (!request.getVisitors().containsAdapters(runOn.peek(), component)) {
            runOn.pop();
            adapters.pop();
          }
        }
        // STOPSHIP ... Until here

        // Manage adapters
        while (!runOn.isEmpty()) {
          if (runOn.contains(runner.getRunOn())) {
            while (runOn.peek() != runner.getRunOn()) {
              runOn.pop();
              adapters.pop();
            }

            break;
          }

          if (request.getVisitors().containsAdapters(runOn.peek(), runner.getRunOn())) {
            for (ManagedVisitor visitor : request.getVisitors().getAdapter(runOn.peek(),
                runner.getRunOn())) {
              runOn.push(visitor.getRunOnAfter());
              adapters.push(adapters.peek().appendSubPlan(visitor));
            }

            break;
          }
          runOn.pop();
          adapters.pop();
        }

        // Append the runner
        adapters.peek().append(runner);

        // Manage exclusive access at the exit
        // STOPSHIP To Be Rewritten from ...
        if (runner.needsExclusiveAccess()) {
          Class<? extends Component> component = runner.getExclusiveAccess();
          assert component != null;

          if (logger.isLoggable(Level.FINE)
              && !request.getVisitors().containsAdapters(runOn.peek(), component)) {
            logger.log(Level.FINE, "Pop adapters after {0} to keep exclusive access to {1}",
                new Object[] {runner.getName(), component.getName()});
          }
          while (!request.getVisitors().containsAdapters(runOn.peek(), component)) {
            runOn.pop();
            adapters.pop();
          }
        }

        // Manage adding and deleting component
        int level = runOn.size();
        Class<? extends TagOrMarkerOrComponent> reason = null;
        for (Class<? extends TagOrMarkerOrComponent> tag : runner.getAddedTags()) {
          for (int i = 0; i < level; i++) {
            if (runOn.get(i).isAssignableFrom(tag)) {
              level = i;
              reason = runOn.get(level - 1);
              break;
            }
          }
        }
        for (Class<? extends TagOrMarkerOrComponent> tag : runner.getRemovedTags()) {
          for (int i = 0; i < level; i++) {
            if (runOn.get(i).isAssignableFrom(tag)) {
              level = i;
              reason = runOn.get(level - 1);
              break;
            }
          }
        }

        if (reason != null) {
          logger.log(Level.FINE, "Pop adapters after {0} to come back to {1}",
              new Object[] {runner.getName(), reason.getName()});
          while (adapters.size() > level) {
            runOn.pop();
            adapters.pop();
          }
        }
        // STOPSHIP ... Until here
      }

      while (runOn.peek() != rootRunOn) {
        adapters.pop();
        runOn.pop();
      }

      @SuppressWarnings("unchecked")
      PlanBuilder<T> pb = (PlanBuilder<T>) adapters.pop();
      return pb;
    } finally {
      event.end();
    }
  }

  @Override
  @Nonnull
  public String toString () {
    ensureTagsAtIndex(getSize());
    ensureConstraintsAtIndex(1);

    return plan.toString();
  }

  @Override
  @Nonnegative
  public int getSize() {
    return plan.size() - 2;
  }

  @Override
  @Nonnull
  public Iterator<ManagedRunnable> iterator() {
    Iterator<Decorated> iter = plan.iterator();
    iter.next();

    return Iterators.limit(Iterators.transform(iter, new Function<Decorated, ManagedRunnable>(){
      // FINDBUGS: Only first and last element of plan are not DecoratedRunner, iter.next skip the
      //           first and Iterators.limit skip the last.
      @SuppressFBWarnings("BC_UNCONFIRMED_CAST")
      @Override
      public ManagedRunnable apply(@Nonnull Decorated decorated) {
        return ((PlanConstructor<?>.DecoratedRunner) decorated).getRunner();
      }}), getSize());
  }
}
