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
import com.android.sched.item.Items;
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
  @Nonnegative
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
    try (Event event = tracer.open(SchedEventType.ANALYZER)) {
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
    }
  }

  private void ensureTagsAtIndex(@Nonnegative int atIdx) {
    assert atIdx >= 0;
    assert atIdx < plan.size();

    if (atIdx > currentTagValidityIdx) {
      for (int idx = currentTagValidityIdx + 1; idx <= atIdx; idx++) {
        ((PlanConstructor<?>.DecoratedRunner) plan.get(idx))
            .updateBeforeTags(plan.get(idx - 1).getAfterTags());
      }

      currentTagValidityIdx = atIdx;
    }
  }

  private void ensureConstraintsAtIndex(@Nonnegative int atIdx) {
    assert atIdx >= 0;
    assert atIdx < plan.size() - 1;

    if (atIdx < constraintTagValidityIdx) {
      for (int idx = Math.min(constraintTagValidityIdx - 1, plan.size() - 2); idx >= atIdx; idx--) {
        plan.get(idx).updateNeedToAdd(plan.get(idx + 1).getNeedToAdd());
        plan.get(idx).updateNeedToRemove(plan.get(idx + 1).getNeedToRemove());
      }

      constraintTagValidityIdx = atIdx;
    }
  }

  @Override
  public boolean isValid() {
    ensureConstraintsAtIndex(0);

    return request.getInitialTags().containsAll(plan.get(0).getNeedToAdd())
        && request.getInitialTags().containsNone(plan.get(0).getNeedToRemove())
        && missingProductions.isEmpty();
  }

  public boolean isProductionValid(@Nonnull ManagedRunnable runner) {
    return missingProductions.containsAll(runner.getProductions());
  }

  public ProductionSet getSuperfluousProductions(@Nonnull ManagedRunnable runner) {
    return (ProductionSet) runner.getProductions().clone().removeAll(missingProductions);
  }

  public boolean isConstraintValid(@Nonnegative int index, @Nonnull ManagedRunnable runner) {
    // Skip initial state
    index++;
    // Take state from previous
    index--;
    ensureTagsAtIndex(index);
    return runner.isCompatible(features, plan.get(index).getAfterTags());
  }

  @SuppressWarnings("unchecked")
  public boolean isConstraintValid(@Nonnegative int index) {
    // Skip initial state
    index++;
    // Take state from previous
    index--;
    ensureTagsAtIndex(index);
    return ((PlanConstructor<T>.DecoratedRunner) (plan.get(index + 1))).getRunner()
        .isCompatible(features, plan.get(index).getAfterTags());
  }

  @SuppressWarnings("unchecked")
  public ManagedRunnable getRunnerAt(@Nonnegative int index) {
    // Skip initial state
    index++;
    return ((PlanConstructor<T>.DecoratedRunner) (plan.get(index))).getRunner();
  }

  public void insert(@Nonnegative int index, @Nonnull ManagedRunnable runner) {
    assert isConstraintValid(index, runner);

    // Skip initial state
    index++;

    plan.add(index, new DecoratedRunner(runner));
    currentTagValidityIdx = Math.min(index - 1, currentTagValidityIdx);
    constraintTagValidityIdx = Math.max(index + 1, constraintTagValidityIdx);
    missingProductions.removeAll(runner.getProductions());
  }

  public void remove(@Nonnegative int index) {
    // Skip initial state
    index++;

    @SuppressWarnings("unchecked")
    DecoratedRunner dr = (PlanConstructor<T>.DecoratedRunner) plan.remove(index);
    currentTagValidityIdx = Math.min(index - 1, currentTagValidityIdx);
    constraintTagValidityIdx = Math.max(index, constraintTagValidityIdx);
    missingProductions.addAll(dr.getRunner().getProductions());
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

  // STOPSHIP Check
  @Override
  @Nonnull
  public PlanBuilder<T> getPlanBuilder() throws IllegalRequestException {
    try (Event event = tracer.open(SchedEventType.PLANBUILDER)) {
      // STOPSHIP Replace Stack
      Stack<Class<? extends Component>> runOn = new Stack<Class<? extends Component>>();
      Stack<SubPlanBuilder<? extends Component>> adapters =
          new Stack<SubPlanBuilder<? extends Component>>();
      Stack<Class<? extends Component>> exclusiveAccess = new Stack<Class<? extends Component>>();

      runOn.push(rootRunOn);
      adapters.push(request.getPlanBuilder(rootRunOn));
      exclusiveAccess.push(null);

      Iterator<Decorated> iter = plan.iterator();
      // Skip the initial state
      Decorated decorated = iter.next();
      ManagedRunnable oldRunner = null;
      while ((decorated = iter.next()) instanceof PlanConstructor.DecoratedRunner) {
        ManagedRunnable runner = ((PlanConstructor<?>.DecoratedRunner) decorated).getRunner();
        Class<? extends Component> accessComponent = runner.getAccess();
        Class<? extends Component> exclusiveComponent = runner.getExclusiveAccess();
        if (request.getVisitors().containsAdapters(exclusiveComponent, accessComponent)) {
          accessComponent = exclusiveComponent;
        }
        logger.log(Level.FINER, "Runner {0} run on {1}",
            new Object[] {runner.getName(), Items.getName(runner.getRunOn())});
        logger.log(Level.FINER, "Runner {0} has acces on {1}",
            new Object[] {runner.getName(), Items.getName(accessComponent)});
        logger.log(Level.FINER, "Runner {0} has exclusive acces on {1}",
            new Object[] {runner.getName(), Items.getName(exclusiveComponent)});

        // Pop adapters for runOn
        if (runOn.peek() != runner.getRunOn() && runOn.contains(runner.getRunOn())) {
          logger.log(Level.FINE, "Pop adapters before {0} to run on {1}",
              new Object[] {runner.getName(), Items.getName(runner.getRunOn())});

          while (runOn.peek() != runner.getRunOn()) {
            runOn.pop();
            adapters.pop();
            exclusiveAccess.pop();
            logger.log(Level.FINEST, "Pop adapter to be on {0}", Items.getName(runOn.peek()));
          }
        }

        // Manage exclusive access before
        if (runOn.peek() != exclusiveComponent && runOn.contains(exclusiveComponent)) {
          logger.log(Level.FINE, "Pop adapters before {0} to gain exclusive access to {1}",
              new Object[] {runner.getName(), Items.getName(exclusiveComponent)});
          while (runOn.peek() != exclusiveComponent) {
            runOn.pop();
            adapters.pop();
            exclusiveAccess.pop();
            logger.log(Level.FINEST, "Pop adapter to be on {0}", Items.getName(runOn.peek()));
          }
        }

        // Manage access
        Class<? extends Component> currentExclusiveAccess = exclusiveAccess.peek();
        if (currentExclusiveAccess != null &&
            accessComponent != currentExclusiveAccess &&
            request.getVisitors().containsAdapters(accessComponent, currentExclusiveAccess)) {
          logger.log(Level.FINE,
              "Pop adapters before {0} to gain access to {1} after exclusive access {2}",
              new Object[] {runner.getName(), Items.getName(accessComponent),
                  Items.getName(currentExclusiveAccess)});
          while (accessComponent != currentExclusiveAccess
              && runOn.contains(currentExclusiveAccess)) {
            runOn.pop();
            adapters.pop();
            exclusiveAccess.pop();
            logger.log(Level.FINEST, "Pop adapter to be on {0}", Items.getName(runOn.peek()));

            if (exclusiveAccess.peek() != null &&
                exclusiveAccess.peek() != currentExclusiveAccess &&
                request.getVisitors().containsAdapters(exclusiveAccess.peek(),
                    currentExclusiveAccess) &&
                exclusiveAccess.peek() != accessComponent &&
                request.getVisitors().containsAdapters(accessComponent, exclusiveAccess.peek())) {
              currentExclusiveAccess = exclusiveAccess.peek();
              logger.log(Level.FINE,
                  "Pop adapters before {0} to gain access to {1} after exclusive access {2}",
                  new Object[] {runner.getName(), Items.getName(accessComponent),
                      Items.getName(currentExclusiveAccess)});
            }
          }
        }

        // Push adapters to runOn
        if (runner.getRunOn() != runOn.peek()) {
          while (!runOn.isEmpty()) {
            if (request.getVisitors().containsAdapters(runOn.peek(), runner.getRunOn())) {
              logger.log(Level.FINE, "Push adapters before {0} to run on {1}",
                  new Object[] {runner.getName(), Items.getName(runner.getRunOn())});

              for (ManagedVisitor visitor : request.getVisitors().getAdapter(runOn.peek(),
                  runner.getRunOn())) {
                runOn.push(visitor.getRunOnAfter());
                adapters.push(adapters.peek().appendSubPlan(visitor));
                exclusiveAccess.push(null);
                logger.log(Level.FINEST, "Push adapter to be on {0}", Items.getName(runOn.peek()));
              }

              break;
            }

            logger.log(Level.FINE, "Pop adapters before {0}", runner.getName());
            runOn.pop();
            adapters.pop();
            exclusiveAccess.pop();
            logger.log(Level.FINEST, "Pop adapter to be on {0}", Items.getName(runOn.peek()));
          }
        }

        // Append the runner
        adapters.peek().append(runner);
        exclusiveAccess.pop();
        exclusiveAccess.push(exclusiveComponent);

        // Manage exclusive access after
        if (runOn.peek() != exclusiveComponent && runOn.contains(exclusiveComponent)) {
          logger.log(Level.FINE, "Pop adapters after {0} to keep exclusive access to {1}",
              new Object[] {runner.getName(), Items.getName(exclusiveComponent)});
          while (runOn.peek() != exclusiveComponent) {
            runOn.pop();
            adapters.pop();
            exclusiveAccess.pop();
            logger.log(Level.FINEST, "Pop adapter to be on {0}", Items.getName(runOn.peek()));
          }
        }

        // Manage adding and deleting component
        int level = runOn.size();
        String reason = null;
        for (Class<? extends TagOrMarkerOrComponent> tag : runner.getAddedTags()) {
          for (int i = 0; i < level; i++) {
            if (runOn.get(i).isAssignableFrom(tag)) {
              level = i;
              if (logger.isLoggable(Level.FINE)) {
                reason = "Pop adapters after " + runner.getName() + " to come back to "
                    + Items.getName(runOn.get(level - 1)) + " because it adds "
                    + Items.getName(tag);
              }
              break;
            }
          }
        }
        for (Class<? extends TagOrMarkerOrComponent> tag : runner.getRemovedTags()) {
          for (int i = 0; i < level; i++) {
            if (runOn.get(i).isAssignableFrom(tag)) {
              level = i;
              if (logger.isLoggable(Level.FINE)) {
                reason = "Pop adapters after " + runner.getName() + " to come back to "
                    + Items.getName(runOn.get(level - 1)) + " because it removes "
                    + Items.getName(tag);
              }
              break;
            }
          }
        }

        if (reason != null) {
          logger.log(Level.FINE, reason);
        }
        while (adapters.size() > level) {
            runOn.pop();
            adapters.pop();
            exclusiveAccess.pop();
            logger.log(Level.FINEST, "Pop adapter to be on {0}", Items.getName(runOn.peek()));
        }
      }

      // Pop the end
      if (runOn.peek() != rootRunOn) {
        logger.log(Level.FINE, "Pop adapters to end on {0}", Items.getName(rootRunOn));
        while (runOn.peek() != rootRunOn) {
          adapters.pop();
          runOn.pop();
          exclusiveAccess.pop();
          logger.log(Level.FINEST, "Pop adapter to be on {0}", Items.getName(runOn.peek()));
        }
      }

      @SuppressWarnings("unchecked")
      PlanBuilder<T> pb = (PlanBuilder<T>) adapters.pop();
      return pb;
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
