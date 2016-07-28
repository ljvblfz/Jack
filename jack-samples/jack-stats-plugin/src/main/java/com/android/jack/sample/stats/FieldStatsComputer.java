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
import com.android.jack.ir.ast.JField;
import com.android.jack.transformations.EmptyClinit;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.*;

import javax.annotation.Nonnull;

/**
 * Computes statistics about fields.
 */
@Description("Compute stats about fields")
@Support(FieldStats.class)
public class FieldStatsComputer implements RunnableSchedulable<JField> {

  @Nonnull
  private static final StatisticId<Percent> FINAL_FIELDS =
          new StatisticId<>("jack.sample.stats.field.final-fields",
                  "Number of final fields",
                  PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Percent> PACKAGE_FIELDS =
          new StatisticId<>("jack.sample.stats.field.package-fields",
                  "Number of package fields",
                  PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Percent> PRIVATE_FIELDS =
          new StatisticId<>("jack.sample.stats.field.private-fields",
                  "Number of private fields",
                  PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Percent> PROTECTED_FIELDS =
          new StatisticId<>("jack.sample.stats.field.protected-fields",
                  "Number of protected fields",
                  PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Percent> PUBLIC_FIELDS =
          new StatisticId<>("jack.sample.stats.field.public-fields",
                  "Number of public fields",
                  PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Percent> STATIC_FIELDS =
          new StatisticId<>("jack.sample.stats.field.static-fields",
                  "Number of static fields",
                  PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Percent> VOLATILE_FIELDS =
          new StatisticId<>("jack.sample.stats.field.volatile-fields",
                  "Number of volatile fields",
                  PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Percent> SYNTHETIC_FIELDS =
          new StatisticId<>("jack.sample.stats.field.synthetic-fields",
                  "Number of synthetic fields",
                  PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Sample> ANNOTATIONS_COUNT =
          new StatisticId<>("jack.sample.stats.field.annotations-count",
                  "Number of field annotations",
                  SampleImpl.class, Sample.class);

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  @Override
  public void run(@Nonnull JField field) {
    // Percent stats
    tracer.getStatistic(PUBLIC_FIELDS).add(field.isPublic());
    tracer.getStatistic(PROTECTED_FIELDS).add(field.isProtected());
    tracer.getStatistic(PRIVATE_FIELDS).add(field.isPrivate());
    tracer.getStatistic(PACKAGE_FIELDS).add(
            !field.isPublic() && !field.isProtected() && !field.isPrivate());
    tracer.getStatistic(STATIC_FIELDS).add(field.isStatic());
    tracer.getStatistic(FINAL_FIELDS).add(field.isFinal());
    tracer.getStatistic(SYNTHETIC_FIELDS).add(field.isSynthetic());
    tracer.getStatistic(VOLATILE_FIELDS).add(field.isVolatile());

    // Sample stats
    tracer.getStatistic(ANNOTATIONS_COUNT).add(field.getAnnotations().size(), field);
  }
}
