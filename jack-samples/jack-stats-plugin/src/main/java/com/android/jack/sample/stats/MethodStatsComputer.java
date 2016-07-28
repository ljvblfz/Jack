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

package com.android.jack.sample.stats;

import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.scheduling.filter.SourceTypeFilter;
import com.android.jack.transformations.EmptyClinit;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.*;

import javax.annotation.Nonnull;

/**
 * Computes statistics about methods
 */
@Description("Compute stats about methods")
@Constraint(no = EmptyClinit.class)
@Support(MethodStats.class)
public class MethodStatsComputer implements RunnableSchedulable<JMethod> {

  @Nonnull
  private static final StatisticId<Percent> CLINIT_METHODS =
          new StatisticId<>("jack.sample.stats.method.class-initializer-methods",
                  "Number of class initializer methods",
                  PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Percent> CONSTRUCTOR_METHODS =
          new StatisticId<>("jack.sample.stats.method.constructor-methods",
                  "Number of constructor methods",
                  PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Percent> ABSTRACT_METHODS =
          new StatisticId<>("jack.sample.stats.method.abstract-methods",
                  "Number of abstract methods",
                  PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Percent> FINAL_METHODS =
          new StatisticId<>("jack.sample.stats.method.final-methods",
                  "Number of final methods",
                  PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Percent> NATIVE_METHODS =
          new StatisticId<>("jack.sample.stats.method.native-methods",
                  "Number of native methods",
                  PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Percent> PACKAGE_METHODS =
          new StatisticId<>("jack.sample.stats.method.package-methods",
                  "Number of package methods",
                  PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Percent> PRIVATE_METHODS =
          new StatisticId<>("jack.sample.stats.method.private-methods",
                  "Number of private methods",
                  PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Percent> PROTECTED_METHODS =
          new StatisticId<>("jack.sample.stats.method.protected-methods",
                  "Number of protected methods",
                  PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Percent> PUBLIC_METHODS =
          new StatisticId<>("jack.sample.stats.method.public-methods",
                  "Number of public methods",
                  PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Percent> STATIC_METHODS =
          new StatisticId<>("jack.sample.stats.method.static-methods",
                  "Number of static methods",
                  PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Percent> SYNCHRONIZED_METHODS =
          new StatisticId<>("jack.sample.stats.method.synchronized-methods",
                  "Number of synchronized methods",
                  PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Percent> SYNTHETIC_METHODS =
          new StatisticId<>("jack.sample.stats.method.synthetic-methods",
                  "Number of synthetic methods",
                  PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Sample> PARAMETERS_COUNT =
          new StatisticId<>("jack.sample.stats.method.parameters-count",
                  "Number of method parameters",
                  SampleImpl.class, Sample.class);

  @Nonnull
  private static final StatisticId<Sample> ANNOTATIONS_COUNT =
          new StatisticId<>("jack.sample.stats.method.annotations-count",
                  "Number of method annotations",
                  SampleImpl.class, Sample.class);

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  @Override
  public void run(@Nonnull JMethod method) {
    // Percent stats
    tracer.getStatistic(PUBLIC_METHODS).add(method.isPublic());
    tracer.getStatistic(PROTECTED_METHODS).add(method.isProtected());
    tracer.getStatistic(PRIVATE_METHODS).add(method.isPrivate());
    tracer.getStatistic(PACKAGE_METHODS).add(
            !method.isPublic() && !method.isProtected() && !method.isPrivate());
    tracer.getStatistic(NATIVE_METHODS).add(method.isNative());
    tracer.getStatistic(ABSTRACT_METHODS).add(method.isAbstract());
    tracer.getStatistic(STATIC_METHODS).add(method.isStatic());
    tracer.getStatistic(FINAL_METHODS).add(method.isFinal());
    tracer.getStatistic(SYNCHRONIZED_METHODS).add(method.isSynchronized());
    tracer.getStatistic(SYNTHETIC_METHODS).add(method.isSynthetic());
    tracer.getStatistic(CONSTRUCTOR_METHODS).add(method instanceof JConstructor);
    tracer.getStatistic(CLINIT_METHODS).add(JMethod.isClinit(method));

    // Sample stats
    tracer.getStatistic(PARAMETERS_COUNT).add(method.getParams().size(), method);
    tracer.getStatistic(ANNOTATIONS_COUNT).add(method.getAnnotations().size(), method);
  }
}
