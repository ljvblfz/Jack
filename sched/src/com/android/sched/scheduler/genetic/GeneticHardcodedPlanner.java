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

package com.android.sched.scheduler.genetic;

import com.google.common.base.Joiner;

import com.android.sched.item.Component;
import com.android.sched.scheduler.FitnessPlanCandidate;
import com.android.sched.scheduler.GroupPlanCandidate;
import com.android.sched.scheduler.Planner;
import com.android.sched.scheduler.Request;
import com.android.sched.util.codec.ImplementationName;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.IntegerPropertyId;
import com.android.sched.util.config.id.ProbabilityPropertyId;
import com.android.sched.util.log.Event;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;

import org.uncommons.maths.number.AdjustableNumberGenerator;
import org.uncommons.maths.random.Probability;
import org.uncommons.maths.random.XORShiftRNG;
import org.uncommons.watchmaker.framework.CachingFitnessEvaluator;
import org.uncommons.watchmaker.framework.EvolutionEngine;
import org.uncommons.watchmaker.framework.EvolutionObserver;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;
import org.uncommons.watchmaker.framework.FitnessEvaluator;
import org.uncommons.watchmaker.framework.GenerationalEvolutionEngine;
import org.uncommons.watchmaker.framework.PopulationData;
import org.uncommons.watchmaker.framework.SelectionStrategy;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.selection.TournamentSelection;
import org.uncommons.watchmaker.framework.termination.ElapsedTime;
import org.uncommons.watchmaker.framework.termination.Stagnation;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 *
 * @param <T> the root <i>data</i> type
 */
@ImplementationName(iface = Planner.class, name = "genetic")
@HasKeyId
public class GeneticHardcodedPlanner<T extends Component> extends GeneticPlanner<T> {
  @Nonnull
  private static final ProbabilityPropertyId ADD_RUNNER = ProbabilityPropertyId.create(
      "sched.genetic.add", "Probability to add a runner").addDefaultValue("0.15");

  @Nonnull
  private static final ProbabilityPropertyId ADD_PRE_RUNNER = ProbabilityPropertyId.create(
      "sched.genetic.add.pre",
      "Probability to add a runner before another one in order to satisfied it")
      .addDefaultValue("0.90");

  @Nonnull
  private static final ProbabilityPropertyId ADD_POST_RUNNER = ProbabilityPropertyId.create(
      "sched.genetic.add.post",
      "Probability to add a satisfied runner after a satisfied one").addDefaultValue("0.30");

  @Nonnull
  private static final ProbabilityPropertyId REMOVE_RUNNER = ProbabilityPropertyId.create(
      "sched.genetic.remove", "Probability to remove a runner")
      .addDefaultValue("0.50");

  @Nonnull
  private static final
      ProbabilityPropertyId REMOVE_UNSATISFIED_RUNNER = ProbabilityPropertyId.create(
          "sched.genetic.remove.unsatisfied",
          "Probability to remove an unsatisfied runner").addDefaultValue("0.60");

  @Nonnull
  private static final ProbabilityPropertyId MOVE_RUNNER = ProbabilityPropertyId.create(
      "sched.genetic.move.satisfied", "Probability to move a satisfied runner")
      .addDefaultValue("0.60");

  private static final ProbabilityPropertyId SELECTION_PRESSURE = ProbabilityPropertyId.create(
      "sched.genetic.selection.pressure", "Selection pressure")
      .addDefaultValue("0.50").withMin(0.5);

  @Nonnull
  private static final IntegerPropertyId POPULATION_SIZE = IntegerPropertyId.create(
      "sched.genetic.population", "Size of the population")
      .addDefaultValue("10").withMin(1);

  @Nonnull
  private static final IntegerPropertyId ELITE_COUNT = IntegerPropertyId.create(
      "sched.genetic.elite", "Size of the elite population").addDefaultValue("2").
      withMin(0);

  @Nonnull
  private static final IntegerPropertyId STAGNATION = IntegerPropertyId.create(
      "sched.genetic.stagnation", "Number of times a population stagnates before stopping")
      .addDefaultValue("1000").withMin(1);

  @Nonnull
  private static final IntegerPropertyId MAX_DURATION = IntegerPropertyId.create(
      "sched.genetic.duration",
      "Maximum time (in milliseconds) to spend before stopping")
      .addDefaultValue("60000").withMin(0);

  @CheckForNull
  private static Random rng = null;

  @Nonnull
  private final Logger logger = LoggerFactory.getLogger();
  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  @Override
  @Nonnull
  protected GroupPlanCandidate<T> buildPlanCandidate(
      @Nonnull Request request, @Nonnull Class<T> rootRunOn) {
    if (rng == null) {
      Event event = tracer.start(GeneticEventType.RANDOM_INIT);
      try {
        logger.log(Level.FINER, "Initializing random generator");
        rng = new XORShiftRNG();
      } finally {
        event.end();
      }
    }

    //
    // Build engine
    //

    FitnessEvaluator<GroupPlanCandidate<T>> evaluator =
        new CachingFitnessEvaluator<GroupPlanCandidate<T>>(new PlanEvaluator<T>());
    PlanFactory<T> factory = new PlanFactory<T>(request, rootRunOn);
    ArrayList<EvolutionaryOperator<GroupPlanCandidate<T>>> operators =
        new ArrayList<EvolutionaryOperator<GroupPlanCandidate<T>>>();

    operators.add(new AddRunnerMutation<T>(new AdjustableNumberGenerator<Probability>(
        new Probability(ThreadConfig.get(ADD_RUNNER).floatValue())), request, rootRunOn));
    operators.add(new AddPreRunnerMutation<T>(new AdjustableNumberGenerator<Probability>(
        new Probability(ThreadConfig.get(ADD_PRE_RUNNER).floatValue())), request));
    operators.add(new AddPostRunnerMutation<T>(new AdjustableNumberGenerator<Probability>(
        new Probability(ThreadConfig.get(ADD_POST_RUNNER).floatValue())), request));
    operators.add(new RemoveRunnerMutation<T>(new AdjustableNumberGenerator<Probability>(
        new Probability(ThreadConfig.get(REMOVE_RUNNER).floatValue())), request, rootRunOn));
    operators.add(new RemoveUnsatisfiedRunnerMutation<T>(new AdjustableNumberGenerator<Probability>(
        new Probability(ThreadConfig.get(REMOVE_UNSATISFIED_RUNNER).floatValue()))));
    operators.add(new MoveRunnerMutation<T>(new AdjustableNumberGenerator<Probability>(
        new Probability(ThreadConfig.get(MOVE_RUNNER).floatValue())), request));

    SelectionStrategy<Object> selection = new TournamentSelection(new AdjustableNumberGenerator<
        Probability>(new Probability(ThreadConfig.get(SELECTION_PRESSURE).floatValue())));

    EvolutionEngine<GroupPlanCandidate<T>> engine =
        new GenerationalEvolutionEngine<GroupPlanCandidate<T>>(factory,
            new EvolutionPipeline<GroupPlanCandidate<T>>(operators), evaluator, selection, rng);

    if (logger.isLoggable(Level.FINEST)) {
      engine.addEvolutionObserver(new EvolutionObserver<FitnessPlanCandidate<T>>() {
        @Override
        public void populationUpdate(PopulationData<? extends FitnessPlanCandidate<T>> population) {
          logger.log(Level.FINE, "Candidate fitness: {0}, plan: {1}", new Object[] {
              String.valueOf(population.getBestCandidateFitness()),
              Joiner.on(", ").join(population.getBestCandidate())});
        }
      });
    } else if (logger.isLoggable(Level.FINE)) {
      engine.addEvolutionObserver(new EvolutionObserver<FitnessPlanCandidate<T>>() {
        private long iter = 0;
        @Override
        public void populationUpdate(PopulationData<? extends FitnessPlanCandidate<T>> population) {
          if (iter++ % 100 == 0) {
            logger.log(Level.FINE, "Candidate plan: {0}", population.getBestCandidate());
          }
        }
      });
    }

    Event event = tracer.start(GeneticEventType.ENGINE);
    try {
      GroupPlanCandidate<T> planCandidate = engine.evolve(
          ThreadConfig.get(POPULATION_SIZE).intValue(), ThreadConfig.get(ELITE_COUNT).intValue(),
          new Stagnation(ThreadConfig.get(STAGNATION).intValue(), true),
          new ElapsedTime(ThreadConfig.get(MAX_DURATION).intValue()));

      logger.log(Level.FINE, "Winner plan: {0}", planCandidate);

      return planCandidate;
    } finally {
      event.end();
    }
  }
}
