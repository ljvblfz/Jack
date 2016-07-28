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

import com.android.jack.ir.ast.*;
import com.android.sched.item.Description;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.*;

import javax.annotation.Nonnull;

/**
 * Compute statistics about classes and interfaces.
 */
@Description("Compute statistics about classes and interfaces")
@Support(ClassStats.class)
public class ClassStatsComputer implements RunnableSchedulable<JDefinedClassOrInterface> {

  @Nonnull
  private static final StatisticId<Counter> CLASSES_COUNT =
          new StatisticId<>("jack.sample.stats.class.total-count",
                  "Number of classes being compiled",
                  CounterImpl.class, Counter.class);

  @Nonnull
  private static final StatisticId<Percent> INTERFACE_CLASSES =
          new StatisticId<>("jack.sample.stats.class.interface-classes",
                  "Number of interface classes",
                  PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Percent> ENUM_CLASSES =
          new StatisticId<>("jack.sample.stats.class.enum-classes",
                  "Number of enum classes",
                  PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Percent> ANNOTATION_CLASSES =
          new StatisticId<>("jack.sample.stats.class.annotation-classes",
                  "Number of annotation classes",
                  PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Percent> ABSTRACT_CLASSES =
          new StatisticId<>("jack.sample.stats.class.abstract-classes",
                  "Number of abstract classes",
                  PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Percent> FINAL_CLASSES =
          new StatisticId<>("jack.sample.stats.class.final-classes",
                  "Number of final classes",
                  PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Percent> PUBLIC_CLASSES =
          new StatisticId<>("jack.sample.stats.class.public-classes",
                  "Number of public classes",
                  PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Percent> PROTECTED_CLASSES =
          new StatisticId<>("jack.sample.stats.class.protected-classes",
                  "Number of protected classes",
                  PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Percent> PRIVATE_CLASSES =
          new StatisticId<>("jack.sample.stats.class.private-classes",
                  "Number of private classes",
                  PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Percent> PACKAGE_CLASSES =
          new StatisticId<>("jack.sample.stats.class.package-classes",
                  "Number of package classes",
                  PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Percent> SYNTHETIC_CLASSES =
          new StatisticId<>("jack.sample.stats.class.synthetic-classes",
                  "Number of synthetic classes",
                  PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Sample> ANNOTATIONS_COUNT =
          new StatisticId<>("jack.sample.stats.class.annotations-count",
                  "Number of class annotations",
                  SampleImpl.class, Sample.class);

  @Nonnull
  private static final StatisticId<Sample> FIELD_COUNT =
          new StatisticId<>("jack.sample.stats.class.field-count",
                  "Number of fields in a class",
                  SampleImpl.class, Sample.class);

  @Nonnull
  private static final StatisticId<Sample> METHODS_COUNT =
          new StatisticId<>("jack.sample.stats.class.method-count",
                  "Number of methods in a class",
                  SampleImpl.class, Sample.class);

  @Nonnull
  private static final StatisticId<Sample> HIERARCHY_DEPTH =
          new StatisticId<>("jack.sample.stats.class.hierarchy-depth",
                  "Depth of super class hierarchy of a class",
                  SampleImpl.class, Sample.class);

  @Nonnull
  private static final StatisticId<Sample> IMPLEMENTS_COUNT =
          new StatisticId<>("jack.sample.stats.class.implements-count",
                  "Number of interfaces implemented by a class",
                  SampleImpl.class, Sample.class);

  /**
   * Getting the Tracer is an expensive operation. Therefore it is recommended to do that once
   * and cache the result in an instance field.
   */
  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  @Override
  public void run(@Nonnull JDefinedClassOrInterface clOrI) {
    /*
     * Contrary to the Tracer, a statistic MUST NOT be cached. Indeed the
     * Tracer.getStatistic method may return different objects.
     */
    tracer.getStatistic(CLASSES_COUNT).incValue();

    tracer.getStatistic(PUBLIC_CLASSES).add(clOrI.isPublic());
    tracer.getStatistic(PROTECTED_CLASSES).add(clOrI.isProtected());
    tracer.getStatistic(PRIVATE_CLASSES).add(clOrI.isPrivate());
    tracer.getStatistic(PACKAGE_CLASSES).add(
            !clOrI.isPublic() && !clOrI.isProtected() && !clOrI.isPrivate());
    tracer.getStatistic(ABSTRACT_CLASSES).add(clOrI.isAbstract());
    tracer.getStatistic(FINAL_CLASSES).add(clOrI.isFinal());
    tracer.getStatistic(SYNTHETIC_CLASSES).add(clOrI.isSynthetic());
    tracer.getStatistic(INTERFACE_CLASSES).add(clOrI instanceof JInterface);
    tracer.getStatistic(ENUM_CLASSES).add(clOrI instanceof JEnum);
    tracer.getStatistic(ANNOTATION_CLASSES).add(clOrI instanceof JAnnotationType);
    tracer.getStatistic(ANNOTATIONS_COUNT).add(clOrI.getAnnotations().size());
    tracer.getStatistic(FIELD_COUNT).add(clOrI.getFields().size());
    tracer.getStatistic(METHODS_COUNT).add(clOrI.getMethods().size());

    // Sample statistics allow to measure an average, a minimum and a maximum. Minimum and maximum
    // can also be marked by an Object which will be represented using its toString method.
    tracer.getStatistic(HIERARCHY_DEPTH).add(clOrI.getHierarchy().size(), clOrI);
    tracer.getStatistic(IMPLEMENTS_COUNT).add(clOrI.getImplements().size(), clOrI);

  }
}
