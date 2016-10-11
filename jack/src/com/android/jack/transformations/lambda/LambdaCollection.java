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

package com.android.jack.transformations.lambda;

import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JLambda;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.formatter.BinaryQualifiedNameFormatter;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.load.NopClassOrInterfaceLoader;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.util.NamingTools;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Counter;
import com.android.sched.util.log.stats.CounterImpl;
import com.android.sched.util.log.stats.StatisticId;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Lambdas collection of the current session. Groups lambdas by
 * implemented interfaces, capture signatures and scopes.
 *
 * Builds a set of lambda groups and classes representing them.
 *
 * Note: adding a lambda is a concurrent operation.
 */
@Transform(add = { JDefinedClass.class, LambdaGroupMarker.class })
public final class LambdaCollection {
  @Nonnull
  public static final StatisticId<Counter> LAMBDA_GROUP_CLASSES_CREATED =
      new StatisticId<>(
          "jack.lambda.group-classes-created",
          "Lambda group classes created",
          CounterImpl.class, Counter.class);

  /**
   * Prefix for lambda group class names
   *
   * '$Lambda$' prefix is mandatory for IntelliJ to be able to skip methods that
   * are not synthetics when using 'step into' during a debug session, otherwise
   * 'step into' stops on a method that is not the lambda body but the method that
   * calls the lambda body.
   */
  @Nonnull
  public static final String LAMBDA_GROUP_CLASS_NAME_PREFIX = "$Lambda$";

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  @Nonnull
  private final JClass javaLangObject =
      Jack.getSession().getPhantomLookup().getClass(CommonTypes.CommonType.OBJECT);
  private final boolean mergeInterfaces =
      ThreadConfig.get(Options.LAMBDA_MERGE_INTERFACES).booleanValue();
  @Nonnull
  private final LambdaGroupingScope groupingScope =
      ThreadConfig.get(Options.LAMBDA_GROUPING_SCOPE);

  @Nonnull
  private final ConcurrentHashMap<Key, ConcurrentHashMap<String, JLambda>> lambdaClassSets =
      new ConcurrentHashMap<>();

  private static final class Key implements Comparable<Key> {
    @Nonnull
    final LambdaCaptureSignature captureSignature;
    /** The package the lambda group class to be created in */
    @Nonnull
    final JPackage pkg;
    /** Unique key being used for equality checks and comparisons */
    @Nonnull
    final String key;

    Key(@Nonnull JPackage pkg,
        @Nonnull LambdaCaptureSignature captureSignature,
        @Nonnull String scopeId,
        @Nonnull String interfaceSignatureId) {
      this.captureSignature = captureSignature;
      this.pkg = pkg;

      // Generate 'key'
      String captureSignatureId = captureSignature.getUniqueId();
      this.key = captureSignatureId + ";;" + interfaceSignatureId + ";;" + scopeId;
    }

    @Override
    public final int hashCode() {
      return key.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
      return this == obj ||
          (obj instanceof Key && this.key.equals(((Key) obj).key));
    }

    @Override
    public int compareTo(@Nonnull Key other) {
      return this.key.compareTo(other.key);
    }
  }

  /**
   * LambdaId must be a kind of a stable lambda identifier to be used to sort
   * lambdas for getting a lambda order stable across multiple compilations.
   *
   * NOTE: thread safe
   */
  void addLambda(@Nonnull JDefinedClassOrInterface currentType,
      @Nonnull String lambdaId, @Nonnull JLambda lambda) {
    getOrCreateForLambda(lambdaId, lambda, currentType).put(lambdaId, lambda);
  }

  @Nonnull
  private ConcurrentHashMap<String, JLambda> getOrCreateForLambda(
      @Nonnull String lambdaId, @Nonnull JLambda lambda,
      @Nonnull JDefinedClassOrInterface currentType) {
    Key key = createKey(lambdaId, lambda, currentType);

    ConcurrentHashMap<String, JLambda> classes = lambdaClassSets.get(key);
    if (classes != null) {
      return classes;
    }

    classes = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, JLambda> existing = lambdaClassSets.putIfAbsent(key, classes);
    return existing == null ? classes : existing;
  }

  @Nonnull
  private Key createKey(@Nonnull String lambdaId, @Nonnull JLambda lambda,
      @Nonnull JDefinedClassOrInterface currentType) {

    String interfaceSignatureId =
        mergeInterfaces ? "" : LambdaInterfaceSignature.forLambda(lambda).getUniqueId();

    String scopeId;
    switch (groupingScope) {
      case NONE:
        // We create a separate group for each lambda,
        // using lambda id as a stable key for the scope
        scopeId = lambdaId;
        break;

      case TYPE:
        while (currentType.getEnclosingType() != null) {
          currentType = (JDefinedClassOrInterface) currentType.getEnclosingType();
        }
        scopeId = BinaryQualifiedNameFormatter
            .getFormatter().getName(currentType);
        break;

      case PACKAGE:
        scopeId = BinaryQualifiedNameFormatter
            .getFormatter().getName(currentType.getEnclosingPackage());
        break;

      default:
        throw new AssertionError();
    }

    return new Key(currentType.getEnclosingPackage(),
        LambdaCaptureSignature.forLambda(lambda), scopeId, interfaceSignatureId);
  }

  /**
   * For collected lambda groups creates an empty class representing the group.
   *
   * Created classes will have the following structure:
   * <pre>
   * public synthetic final class $Lambda$XYZ {
   *   // Members to be added in LambdaGroupClassFinalizer
   * }
   * </pre>
   */
  void createLambdaClassGroups(@Nonnull JSession session) {
    int nextId = 0;
    // Create a map sorted by key to ensure a stable order
    // of the created lambda group classes
    TreeMap<Key, ConcurrentHashMap<String, JLambda>> sorted = new TreeMap<>(lambdaClassSets);
    for (Map.Entry<Key, ConcurrentHashMap<String, JLambda>> entry : sorted.entrySet()) {
      // Create a group class to represent lambda group
      JDefinedClass groupClass = createGroupClass(nextId++, session, entry.getKey().pkg);

      LambdaGroup lambdaGroup = new LambdaGroup(
          entry.getValue(), groupClass, entry.getKey().captureSignature);

      groupClass.addMarker(new LambdaGroupMarker(lambdaGroup));
    }
  }

  @Nonnull
  private JDefinedClass createGroupClass(
      @Nonnegative int id, @Nonnull JSession session, @Nonnull JPackage pkg) {
    // Create a class
    JDefinedClass groupClass = new JDefinedClass(SourceInfo.UNKNOWN,
        NamingTools.getNonSourceConflictingName(LAMBDA_GROUP_CLASS_NAME_PREFIX + id),
        JModifier.FINAL | JModifier.SYNTHETIC,
        pkg, NopClassOrInterfaceLoader.INSTANCE);
    groupClass.setSuperClass(javaLangObject);

    session.addTypeToEmit(groupClass);
    tracer.getStatistic(LAMBDA_GROUP_CLASSES_CREATED).incValue();
    return groupClass;
  }
}
