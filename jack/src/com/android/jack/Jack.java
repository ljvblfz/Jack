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

package com.android.jack;

import com.android.jack.Options.Container;
import com.android.jack.analysis.DefinitionMarkerAdder;
import com.android.jack.analysis.DefinitionMarkerRemover;
import com.android.jack.analysis.UsedVariableAdder;
import com.android.jack.analysis.UsedVariableRemover;
import com.android.jack.analysis.defsuses.DefUsesAndUseDefsChainComputation;
import com.android.jack.analysis.defsuses.DefUsesAndUseDefsChainRemover;
import com.android.jack.analysis.defsuses.UseDefsChecker;
import com.android.jack.analysis.dfa.reachingdefs.ReachingDefinitions;
import com.android.jack.analysis.dfa.reachingdefs.ReachingDefinitionsRemover;
import com.android.jack.backend.ResourceWriter;
import com.android.jack.backend.dex.ClassAnnotationBuilder;
import com.android.jack.backend.dex.ClassDefItemBuilder;
import com.android.jack.backend.dex.DexFileBuilder;
import com.android.jack.backend.dex.DexFilePreparer;
import com.android.jack.backend.dex.DexFileWriter;
import com.android.jack.backend.dex.DexZipWriter;
import com.android.jack.backend.dex.EncodedFieldBuilder;
import com.android.jack.backend.dex.EncodedMethodBuilder;
import com.android.jack.backend.dex.FieldAnnotationBuilder;
import com.android.jack.backend.dex.FieldInitializerRemover;
import com.android.jack.backend.dex.MethodAnnotationBuilder;
import com.android.jack.backend.dex.MethodBodyRemover;
import com.android.jack.backend.dex.annotations.ClassAnnotationSchedulingSeparator;
import com.android.jack.backend.dex.annotations.DefaultValueAnnotationAdder;
import com.android.jack.backend.dex.annotations.ReflectAnnotationsAdder;
import com.android.jack.backend.dex.rop.CodeItemBuilder;
import com.android.jack.backend.jayce.JackFormatProduct;
import com.android.jack.backend.jayce.JayceFileImporter;
import com.android.jack.backend.jayce.JayceSingleTypeWriter;
import com.android.jack.cfg.CfgBuilder;
import com.android.jack.cfg.CfgMarkerRemover;
import com.android.jack.config.id.JavaVersionPropertyId.JavaVersion;
import com.android.jack.frontend.FrontendCompilationException;
import com.android.jack.frontend.MethodIdDuplicateRemover;
import com.android.jack.frontend.MethodIdMerger;
import com.android.jack.frontend.TypeDuplicateRemoverChecker;
import com.android.jack.frontend.VirtualMethodsMarker;
import com.android.jack.frontend.java.JackBatchCompiler;
import com.android.jack.ir.JackFormatIr;
import com.android.jack.ir.JavaSourceIr;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.formatter.InternalFormatter;
import com.android.jack.ir.formatter.TypePackageAndMethodFormatter;
import com.android.jack.ir.formatter.UserFriendlyFormatter;
import com.android.jack.jayce.JaycePackageLoader;
import com.android.jack.load.ComposedPackageLoader;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.lookup.JPhantomLookup;
import com.android.jack.optimizations.ConstantRefinerAndVariableRemover;
import com.android.jack.optimizations.DefUsesChainsSimplifier;
import com.android.jack.optimizations.ExpressionSimplifier;
import com.android.jack.optimizations.IfWithConstantSimplifier;
import com.android.jack.optimizations.NotSimplifier;
import com.android.jack.optimizations.UnusedDefinitionRemover;
import com.android.jack.optimizations.UseDefsChainsSimplifier;
import com.android.jack.scheduling.adapter.JDefinedClassOrInterfaceAdaptor;
import com.android.jack.scheduling.adapter.JFieldAdaptor;
import com.android.jack.scheduling.adapter.JMethodAdaptor;
import com.android.jack.scheduling.adapter.JPackageAdapter;
import com.android.jack.scheduling.feature.DexNonZipOutput;
import com.android.jack.scheduling.feature.DexZipOutput;
import com.android.jack.scheduling.feature.DxLegacy;
import com.android.jack.scheduling.feature.JackFileOutput;
import com.android.jack.scheduling.feature.Resources;
import com.android.jack.scheduling.feature.SourceVersion7;
import com.android.jack.scheduling.tags.DexFileProduct;
import com.android.jack.shrob.SeedFile;
import com.android.jack.shrob.SeedPrinter;
import com.android.jack.shrob.obfuscation.Mapping;
import com.android.jack.shrob.obfuscation.MappingPrinter;
import com.android.jack.shrob.obfuscation.NameFinalizer;
import com.android.jack.shrob.obfuscation.NameKeeper;
import com.android.jack.shrob.obfuscation.Obfuscation;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.jack.shrob.obfuscation.Renamer;
import com.android.jack.shrob.obfuscation.annotation.FieldAnnotationRemover;
import com.android.jack.shrob.obfuscation.annotation.MethodAnnotationRemover;
import com.android.jack.shrob.obfuscation.annotation.ParameterAnnotationRemover;
import com.android.jack.shrob.obfuscation.annotation.TypeAnnotationRemover;
import com.android.jack.shrob.obfuscation.remover.FieldKeepNameMarkerRemover;
import com.android.jack.shrob.obfuscation.remover.MethodKeepNameMarkerRemover;
import com.android.jack.shrob.obfuscation.remover.TypeKeepNameMarkerRemover;
import com.android.jack.shrob.obfuscation.remover.TypeOriginalNameMarkerRemover;
import com.android.jack.shrob.proguard.GrammarActions;
import com.android.jack.shrob.shrink.ExtendingOrImplementingClassFinder;
import com.android.jack.shrob.shrink.FieldShrinker;
import com.android.jack.shrob.shrink.Keeper;
import com.android.jack.shrob.shrink.MethodShrinker;
import com.android.jack.shrob.shrink.Shrinking;
import com.android.jack.shrob.shrink.TypeAndMemberLister;
import com.android.jack.shrob.shrink.TypeAndMemberListing;
import com.android.jack.shrob.shrink.TypeShrinker;
import com.android.jack.shrob.shrink.remover.FieldKeepMarkerRemover;
import com.android.jack.shrob.shrink.remover.MethodKeepMarkerRemover;
import com.android.jack.shrob.shrink.remover.TypeShrinkMarkerRemover;
import com.android.jack.shrob.spec.Flags;
import com.android.jack.statistics.BinaryOperationWithCst;
import com.android.jack.transformations.AssertionTransformer;
import com.android.jack.transformations.AssertionTransformerSchedulingSeparator;
import com.android.jack.transformations.EmptyClinitRemover;
import com.android.jack.transformations.FieldInitializer;
import com.android.jack.transformations.Jarjar;
import com.android.jack.transformations.SanityChecks;
import com.android.jack.transformations.UnusedLocalRemover;
import com.android.jack.transformations.VisibilityBridgeAdder;
import com.android.jack.transformations.ast.BooleanTestTransformer;
import com.android.jack.transformations.ast.CompoundAssignmentRemover;
import com.android.jack.transformations.ast.ConcatRemover;
import com.android.jack.transformations.ast.ExpressionStatementLegalizer;
import com.android.jack.transformations.ast.ImplicitBlocks;
import com.android.jack.transformations.ast.ImplicitBlocksChecker;
import com.android.jack.transformations.ast.IncDecRemover;
import com.android.jack.transformations.ast.InitInNewArrayRemover;
import com.android.jack.transformations.ast.MultiDimensionNewArrayRemover;
import com.android.jack.transformations.ast.NestedAssignRemover;
import com.android.jack.transformations.ast.NumericConversionChecker;
import com.android.jack.transformations.ast.PrimitiveClassTransformer;
import com.android.jack.transformations.ast.RefAsStatementRemover;
import com.android.jack.transformations.ast.SynchronizeTransformer;
import com.android.jack.transformations.ast.TryWithResourcesTransformer;
import com.android.jack.transformations.ast.TypeLegalizer;
import com.android.jack.transformations.ast.inner.InnerAccessorAdder;
import com.android.jack.transformations.ast.inner.InnerAccessorGenerator;
import com.android.jack.transformations.ast.inner.InnerAccessorSchedulingSeparator;
import com.android.jack.transformations.ast.removeinit.FieldInitMethodCallRemover;
import com.android.jack.transformations.ast.removeinit.FieldInitMethodRemover;
import com.android.jack.transformations.ast.splitnew.SplitNewInstance;
import com.android.jack.transformations.ast.splitnew.SplitNewInstanceChecker;
import com.android.jack.transformations.ast.string.FieldGenericSignatureSplitter;
import com.android.jack.transformations.ast.string.FieldStringLiteralRefiner;
import com.android.jack.transformations.ast.string.MethodGenericSignatureSplitter;
import com.android.jack.transformations.ast.string.MethodStringLiteralRefiner;
import com.android.jack.transformations.ast.string.ReflectionStringLiteralRefiner;
import com.android.jack.transformations.ast.string.TypeGenericSignatureSplitter;
import com.android.jack.transformations.ast.string.TypeStringLiteralRefiner;
import com.android.jack.transformations.ast.switches.SwitchStringSupport;
import com.android.jack.transformations.ast.switches.UselessCaseChecker;
import com.android.jack.transformations.ast.switches.UselessCaseRemover;
import com.android.jack.transformations.ast.switches.UselessSwitchesRemover;
import com.android.jack.transformations.booleanoperators.ConditionalAndOrRemover;
import com.android.jack.transformations.booleanoperators.ConditionalAndOrRemoverChecker;
import com.android.jack.transformations.cast.UselessCastRemover;
import com.android.jack.transformations.debug.LineDebugInfo;
import com.android.jack.transformations.debug.ThisRefDebugInfoAdder;
import com.android.jack.transformations.enums.EnumMappingMarkerRemover;
import com.android.jack.transformations.enums.EnumMappingSchedulingSeparator;
import com.android.jack.transformations.enums.SwitchEnumSupport;
import com.android.jack.transformations.enums.UsedEnumFieldCollector;
import com.android.jack.transformations.enums.UsedEnumFieldMarkerRemover;
import com.android.jack.transformations.exceptions.ExceptionRuntimeValueAdder;
import com.android.jack.transformations.exceptions.TryCatchRemover;
import com.android.jack.transformations.exceptions.TryStatementSchedulingSeparator;
import com.android.jack.transformations.finallyblock.FinallyRemover;
import com.android.jack.transformations.flow.FlowNormalizer;
import com.android.jack.transformations.flow.FlowNormalizerSchedulingSeparator;
import com.android.jack.transformations.parent.DeclaredTypePackageChecker;
import com.android.jack.transformations.parent.PackageChecker;
import com.android.jack.transformations.parent.ParentSetterChecker;
import com.android.jack.transformations.renamepackage.PackageRenamer;
import com.android.jack.transformations.rop.cast.RopCastLegalizer;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeBuilder;
import com.android.jack.transformations.uselessif.UselessIfChecker;
import com.android.jack.transformations.uselessif.UselessIfRemover;
import com.android.jack.util.collect.UnmodifiableCollections;
import com.android.sched.scheduler.FeatureSet;
import com.android.sched.scheduler.IllegalRequestException;
import com.android.sched.scheduler.Plan;
import com.android.sched.scheduler.PlanBuilder;
import com.android.sched.scheduler.PlanNotFoundException;
import com.android.sched.scheduler.PlanPrinterFactory;
import com.android.sched.scheduler.ProductionSet;
import com.android.sched.scheduler.Request;
import com.android.sched.scheduler.Scheduler;
import com.android.sched.scheduler.SubPlanBuilder;
import com.android.sched.scheduler.TagOrMarkerOrComponentSet;
import com.android.sched.util.RunnableHooks;
import com.android.sched.util.config.Config;
import com.android.sched.util.config.ConfigPrinterFactory;
import com.android.sched.util.config.ConfigurationException;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ReflectFactory;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.ObjectId;
import com.android.sched.util.config.id.ReflectFactoryPropertyId;
import com.android.sched.util.log.Event;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.vfs.InputVDir;
import com.android.sched.vfs.direct.InputDirectDir;
import com.android.sched.vfs.direct.OutputDirectDir;
import com.android.sched.vfs.zip.InputZipArchive;
import com.android.sched.vfs.zip.OutputZipRootVDir;

import org.antlr.runtime.RecognitionException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
/**
 * Executable class to run the jack compiler.
 */
@HasKeyId
public abstract class Jack {
  static {
    LoggerFactory.loadLoggerConfiguration(Jack.class, "/initial.logging.properties");
  }

  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();
  @Nonnull
  private static final TypePackageAndMethodFormatter lookupFormatter =
      InternalFormatter.getFormatter();
  @Nonnull
  private static final TypePackageAndMethodFormatter userFriendlyFormatter =
      UserFriendlyFormatter.getFormatter();

  @Nonnull
  public static final ObjectId<JSession> SESSION =
      new ObjectId<JSession>("jack.session", JSession.class);


  // Compilation configuration kept in a static field to avoid ThreadConfig overhead
  @CheckForNull
  private static UnmodifiableCollections unmodifiableCollections;

  @Nonnull
  private static final
      ReflectFactoryPropertyId<JaycePackageLoader> CLASSPATH_POLICY =
        ReflectFactoryPropertyId.create(
          "jack.internal.jayce.loader.classpath.policy",
          "Hint on default load policy for classpath entries",
          JaycePackageLoader.class)
          .addArgType(InputVDir.class).addArgType(JPhantomLookup.class).bypassAccessibility()
          .addDefaultValue("structure");

  @Nonnull
  private static final
      ReflectFactoryPropertyId<JaycePackageLoader> IMPORT_POLICY = ReflectFactoryPropertyId.create(
          "jack.internal.jayce.loader.import.policy",
          "Hint on default load policy for import entries",
          JaycePackageLoader.class)
          .addArgType(InputVDir.class).addArgType(JPhantomLookup.class).bypassAccessibility()
          .addDefaultValue("full");

  @Nonnull
  public static JSession getSession() {
    return ThreadConfig.get(Jack.SESSION);
  }

  @Nonnull
  public static UnmodifiableCollections getUnmodifiableCollections() {
    if (unmodifiableCollections == null) {
      unmodifiableCollections =
          ThreadConfig.get(UnmodifiableCollections.UNMODIFIABLE_COLLECTION).create();
    }
    assert unmodifiableCollections != null; //FINDBUGS
    return unmodifiableCollections;
  }

  /**
   * Runs the jack compiler on source files and generates a dex file.
   *
   * @param options options for the compiler.
   * @throws ConfigurationException
   * @throws IllegalOptionsException
   * @throws NothingToDoException
   */
  public static void run(@Nonnull Options options)
      throws IllegalOptionsException, NothingToDoException, ConfigurationException {
    boolean assertEnable = false;
    // assertEnable = true if assertion is already enable
    assert true == (assertEnable = true);

    if (options.proguardFlagsFiles != null && !options.proguardFlagsFiles.isEmpty()) {
      if (options.flags == null) {
        options.flags = new Flags();
      }
      for (File proguardFlagsFile : options.proguardFlagsFiles) {
        try {
          GrammarActions.parse(proguardFlagsFile.getAbsolutePath(), ".", options.flags);
        } catch (RecognitionException e) {
          throw new IllegalOptionsException(
              "Error while parsing " + proguardFlagsFile.getPath() + ":" + e.line, e);
        }
      }
      options.applyShrobFlags();
    }


    RunnableHooks hooks = new RunnableHooks();
    try {
      options.checkValidity(hooks);

      Config config = options.getConfig();
      ThreadConfig.setConfig(config);
      ConfigPrinterFactory.getConfigPrinter().printConfig(config);
      Event event = TracerFactory.getTracer().start(JackEventType.JACK_RUN);
      try {
        if (options.hasSanityChecks() != assertEnable) {
          logger.log(Level.INFO, "Jack assertion status overriden by sanity checks option");
        }

        ClassLoader classLoader = Jack.class.getClassLoader();
        classLoader.clearAssertionStatus();
        classLoader.setDefaultAssertionStatus(options.hasSanityChecks());
        logger.log(Level.INFO, "Jack sanity checks {0}",
            (options.hasSanityChecks() ? "enabled" : "disabled"));

        JSession session = buildSession(options, hooks);
        Request request = createInitialRequest();

        request.addFeature(Resources.class);

        JavaVersion sourceVersion = config.get(Options.JAVA_SOURCE_VERSION);
        if (sourceVersion.compareTo(JavaVersion.JAVA_7) >= 0) {
          request.addFeature(SourceVersion7.class);
        }

        if (options.hasSanityChecks()) {
          request.addFeature(SanityChecks.class);
        }
        if (config.get(Options.EMIT_LINE_NUMBER_DEBUG_INFO).booleanValue()) {
          request.addFeature(LineDebugInfo.class);
        }
        if (options.jarjarRulesFile != null) {
          request.addFeature(Jarjar.class);
        }
        if (options.dxLegacy) {
          request.addFeature(DxLegacy.class);
        }
        if (options.flags != null) {
          if (options.flags.shrink()) {
            request.addFeature(Shrinking.class);
          }
          if (options.flags.obfuscate()) {
            request.addFeature(Obfuscation.class);
          }
          if (options.flags.printMapping()) {
            request.addProduction(Mapping.class);
          }
          if (options.flags.printSeeds()) {
            request.addProduction(SeedFile.class);
          }
        }
        if (config.get(TypeAndMemberLister.TYPE_AND_MEMBER_LISTING).booleanValue()) {
          request.addProduction(TypeAndMemberListing.class);
        }

        if (options.outputToZip()) {
          request.addFeature(DexZipOutput.class);
        } else {
          request.addFeature(DexNonZipOutput.class);
        }

        if (config.get(Options.GENERATE_JACK_FILE).booleanValue()) {
          request.addFeature(JackFileOutput.class);
        }

        if (options.ecjArguments == null) {
          request.addInitialTagsOrMarkers(getJackFormatInitialTagSet());
        } else {
          request.addInitialTagsOrMarkers(getJavaSourceInitialTagSet());
          if (config.get(Options.GENERATE_JACK_FILE).booleanValue()) {
            request.addInitialTagsOrMarkers(getJackFormatInitialTagSet());
            request.addProduction(JackFormatProduct.class);
          }
        }

        if (options.jayceOutDir != null || options.jayceOutZip != null) {
          request.addProduction(JackFormatProduct.class);
        } else {
          assert options.out != null || options.outZip != null;
          request.addProduction(DexFileProduct.class);
        }

        ProductionSet targetProduction = request.getTargetProductions();
        FeatureSet features = request.getFeatures();
        PlanBuilder<JSession> planBuilder;
        try {
          planBuilder = request.getPlanBuilder(JSession.class);
        } catch (IllegalRequestException e) {
          throw new AssertionError(e);
        }

        if (targetProduction.contains(JackFormatProduct.class)
            && !targetProduction.contains(DexFileProduct.class)) {
          if (options.ecjArguments == null) {
            fillJayceToJaycePlan(options, planBuilder);
          } else {
            fillJavaToJaycePlan(options, planBuilder);
          }
          SubPlanBuilder<JDefinedClassOrInterface> typePlan =
              planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdaptor.class);
          typePlan.append(JayceSingleTypeWriter.class);

          if (features.contains(Resources.class)) {
            SubPlanBuilder<JPackage> packagePlan = planBuilder.appendSubPlan(JPackageAdapter.class);
            packagePlan.append(ResourceWriter.class);
          }
        } else if (options.ecjArguments == null) {
          assert targetProduction.contains(DexFileProduct.class);
          fillJayceToDexPlan(options, planBuilder);
          if (features.contains(DexZipOutput.class)) {
            planBuilder.append(DexZipWriter.class);
          } else {
            planBuilder.append(DexFileWriter.class);
          }
        } else {
          assert targetProduction.contains(DexFileProduct.class);
          fillDexPlan(options, planBuilder);
          if (features.contains(DexZipOutput.class)) {
            planBuilder.append(DexZipWriter.class);
          } else {
            planBuilder.append(DexFileWriter.class);
          }
        }

        Plan<JSession> plan;
        try {
          // Try to build an automatic plan ...
          try {
            plan = request.buildPlan(JSession.class);
          } catch (PlanNotFoundException e) {
            throw new AssertionError(e);
          } catch (IllegalRequestException e) {
            throw new AssertionError(e);
          }
        } catch (UnsupportedOperationException e) {
          // ... but use a manual one if not supported
          plan = planBuilder.getPlan();

          assert !targetProduction.contains(JackFormatProduct.class)
              || targetProduction.contains(DexFileProduct.class)
              || plan.computeFinalTagsOrMarkers(request.getInitialTags()).contains(
                  JackFormatIr.class);
        }

        if (config.get(Options.GENERATE_JACK_FILE).booleanValue()) {
          Container outputContainer = config.get(Options.JACK_OUTPUT_CONTAINER_TYPE);
          if (outputContainer == Container.DIR) {
            session.setOutputVDir(new OutputDirectDir(config.get(Options.JACK_FILE_OUTPUT_DIR)));
          } else if (outputContainer == Container.ZIP) {
            try {
              final OutputZipRootVDir vDir =
                  new OutputZipRootVDir(config.get(Options.JACK_FILE_OUTPUT_ZIP));
              final File jayceOutZip = options.jayceOutZip;
              hooks.addHook(new Runnable() {
                @Override
                public void run() {
                  try {
                    vDir.close();
                  } catch (IOException e) {
                    logger.log(Level.WARNING,
                        "Failed to close zip for '" + jayceOutZip.getAbsolutePath() + "'.", e);
                  }
                }
              });
              session.setOutputVDir(vDir);
            } catch (IOException e) {
              throw new JackFileException(
                  "Error initializing jack output zip: " + options.jayceOutZip.getAbsolutePath(),
                  e);
            }
          }
        }

        PlanPrinterFactory.getPlanPrinter().printPlan(plan);
        try {
          plan.getScheduleInstance().process(session);
        } catch (Exception e) {
          throw new AssertionError(e);
        }
      } finally {
        event.end();
      }
    } finally {
      hooks.runHooks();
      ThreadConfig.unsetConfig();
    }
  }

  @Nonnull
  static Request createInitialRequest() {
    Scheduler scheduler = Scheduler.getScheduler();
    Request request = scheduler.createScheduleRequest();

    request.addSchedulables(scheduler.getAllSchedulable());
    return request;
  }

  @Nonnull
  public static TagOrMarkerOrComponentSet getJavaSourceInitialTagSet() {
    Scheduler scheduler = Scheduler.getScheduler();
    TagOrMarkerOrComponentSet set = scheduler.createTagOrMarkerOrComponentSet();
    set.add(JavaSourceIr.class);
    set.add(OriginalNames.class);
    return set;
  }

  @Nonnull
  private static TagOrMarkerOrComponentSet getJackFormatInitialTagSet() {
    Scheduler scheduler = Scheduler.getScheduler();
    TagOrMarkerOrComponentSet set = scheduler.createTagOrMarkerOrComponentSet();
    set.add(JackFormatIr.class);
    set.add(OriginalNames.class);
    return set;
  }

  @Nonnull
  static JSession buildSession(@Nonnull Options options, @Nonnull RunnableHooks hooks)
      throws JackIOException {

    Tracer tracer = TracerFactory.getTracer();

    List<String> ecjArguments = options.ecjArguments;

    JSession session =  getSession();

    ComposedPackageLoader rootPackageLoader = session.getTopLevelLoader();

    JPhantomLookup phantomLookup = session.getPhantomLookup();
    JayceFileImporter jayceImporter =
        getJayceFileImporter(options.jayceImport, rootPackageLoader, phantomLookup, hooks);
    putInJackClasspath(options.getBootclasspath(), rootPackageLoader, phantomLookup, hooks);
    putInJackClasspath(options.getClasspath(), rootPackageLoader, phantomLookup, hooks);

    if (ecjArguments != null) {
      String bootclasspathOption = "-bootclasspath";
      String classpathOption = "-classpath";
      String classpathShortOption = "-cp";

      int bootclasspathIndex = ecjArguments.indexOf(bootclasspathOption);
      if (bootclasspathIndex != -1) {
        String previousBootclasspath = ecjArguments.get(bootclasspathIndex + 1);
        ecjArguments.set(bootclasspathIndex + 1, previousBootclasspath + File.pathSeparatorChar
            + JackBatchCompiler.JACK_LOGICAL_PATH_ENTRY);
      } else {
        ecjArguments.add(bootclasspathOption);
        ecjArguments.add(JackBatchCompiler.JACK_LOGICAL_PATH_ENTRY);
      }

      JackBatchCompiler jbc = new JackBatchCompiler(session, jayceImporter);

      Event event = tracer.start(JackEventType.ECJ_COMPILATION);

      try {
        if (!jbc.compile(ecjArguments.toArray(new String[ecjArguments.size()]))) {
          throw new FrontendCompilationException("Failed to compile.");
        }
      } finally {
        event.end();
      }
    }

    jayceImporter.doImport(session);

    Event eventIdMerger = tracer.start(JackEventType.METHOD_ID_MERGER);

    try {
      JClass javaLangObject = phantomLookup.getClass(CommonTypes.JAVA_LANG_OBJECT);
      MethodIdMerger merger = new MethodIdMerger(javaLangObject);
      for (JType type : session.getTypesToEmit()) {
        merger.accept(type);
      }
      JVisitor remover = new VirtualMethodsMarker.Remover(javaLangObject);
      for (JType type : session.getTypesToEmit()) {
        remover.accept(type);
      }
    } finally {
      eventIdMerger.end();
    }

    MethodIdDuplicateRemover methodIdDupRemover = new MethodIdDuplicateRemover();
    methodIdDupRemover.accept(session);

    return session;
  }

  @Nonnull
  private static JayceFileImporter getJayceFileImporter(@Nonnull List<File> jayceImport,
      @Nonnull ComposedPackageLoader rootPackageLoader, @Nonnull JPhantomLookup phantomLookup,
      @Nonnull RunnableHooks hooks) {
    List<InputVDir> jackFilesToImport = new ArrayList<InputVDir>(jayceImport.size());
    ReflectFactory<JaycePackageLoader> factory = ThreadConfig.get(IMPORT_POLICY);
    for (final File jackFile : jayceImport) {
      try {
        InputVDir vDir = wrapAsVDir(jackFile, hooks);
        jackFilesToImport.add(vDir);
        // add to classpath
        JaycePackageLoader rootPLoader = factory.create(vDir, phantomLookup);
        rootPackageLoader.appendLoader(rootPLoader);
      } catch (IOException ioException) {
        throw new JackFileException("Error importing jack container: " + jackFile.getAbsolutePath(),
            ioException);
      }
    }
    return new JayceFileImporter(jackFilesToImport);
  }

  private static void putInJackClasspath(@Nonnull List<File> jackFiles,
      @Nonnull ComposedPackageLoader rootPackageLoader,
      @Nonnull JPhantomLookup phantomJNodeLookup,
      @Nonnull RunnableHooks hooks) {
    ReflectFactory<JaycePackageLoader> factory = ThreadConfig.get(CLASSPATH_POLICY);
    for (final File jackFile : jackFiles) {
      try {
        InputVDir vDir = wrapAsVDir(jackFile, hooks);
        JaycePackageLoader rootPLoader = factory.create(vDir, phantomJNodeLookup);
        rootPackageLoader.appendLoader(rootPLoader);
      } catch (IOException ioException) {
        // Ignore bad entry
        logger.log(Level.WARNING, "Bad classpath entry ignored: {0}",
            jackFile.getAbsolutePath());
      }
    }
  }

  @Nonnull
  private static InputVDir wrapAsVDir(@Nonnull final File dirOrZip,
      @Nonnull RunnableHooks hooks) throws IOException {
    InputVDir dir;
    if (dirOrZip.isDirectory()) {
      dir = new InputDirectDir(dirOrZip);
    } else { // zip
      final InputZipArchive zipArchive = new InputZipArchive(dirOrZip);
      dir = zipArchive;
      hooks.addHook(new Runnable() {
        @Override
        public void run() {
          try {
            zipArchive.close();
          } catch (IOException e) {
            logger.log(Level.FINE, "Failed to close zip for '" + dirOrZip + "'.", e);
          }
        }
      });
    }
    return dir;
  }

  private static void fillJayceToJaycePlan(
      @Nonnull Options options, @Nonnull PlanBuilder<JSession> planBuilder) {
    // Add here transformations we want to apply before writing .jack file
    FeatureSet features = planBuilder.getRequest().getFeatures();
    ProductionSet productions = planBuilder.getRequest().getTargetProductions();

    if (features.contains(SanityChecks.class)) {
      planBuilder.append(TypeDuplicateRemoverChecker.class);
    }

    // JarJar
    if (features.contains(Jarjar.class)) {
      planBuilder.append(PackageRenamer.class);
    }

    // Shrob
    if (features.contains(Shrinking.class) || features.contains(Obfuscation.class)) {
      appendStringRefiningPlan(planBuilder);
    }

    if (productions.contains(SeedFile.class)) {
      planBuilder.append(SeedPrinter.class);
    }
    if (features.contains(Shrinking.class)) {
      appendShrinkingPlan(planBuilder);
    }
    if (features.contains(Obfuscation.class)) {
      appendObfuscationPlan(planBuilder);
    } else {
      planBuilder.append(NameFinalizer.class);
    }
    if (productions.contains(Mapping.class)) {
      planBuilder.append(MappingPrinter.class);
    }
    if (productions.contains(TypeAndMemberListing.class)) {
      planBuilder.append(TypeAndMemberLister.class);
    }
    if (features.contains(Shrinking.class) || features.contains(Obfuscation.class)) {
      appendShrobMarkerRemoverPlan(planBuilder);
    }
  }

  static void fillDexPlan(@Nonnull Options options, @Nonnull PlanBuilder<JSession> planBuilder) {
    FeatureSet features = planBuilder.getRequest().getFeatures();
    ProductionSet productions = planBuilder.getRequest().getTargetProductions();
    boolean hasSanityChecks = features.contains(SanityChecks.class);

    // Build the plan
    if (hasSanityChecks) {
      planBuilder.append(TypeDuplicateRemoverChecker.class);
    }

    if (features.contains(Jarjar.class)) {
      planBuilder.append(PackageRenamer.class);
    }

    if (hasSanityChecks) {
      planBuilder.append(ParentSetterChecker.class);
    }
    planBuilder.append(DexFileBuilder.class);

    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdaptor.class);
      {
        SubPlanBuilder<JMethod> methodPlan = typePlan.appendSubPlan(JMethodAdaptor.class);
        if (features.contains(LineDebugInfo.class)) {
          methodPlan.append(ThisRefDebugInfoAdder.class);
        }
        if (features.contains(SourceVersion7.class)) {
          methodPlan.append(TryWithResourcesTransformer.class);
        }
      }
      {
        SubPlanBuilder<JField> fieldPlan = typePlan.appendSubPlan(JFieldAdaptor.class);
        fieldPlan.append(FieldInitializerRemover.class);
      }
    }

    if (features.contains(Shrinking.class) || features.contains(Obfuscation.class)) {
      appendStringRefiningPlan(planBuilder);
    }

    if (productions.contains(SeedFile.class)) {
      planBuilder.append(SeedPrinter.class);
    }
    if (features.contains(Shrinking.class)) {
      appendShrinkingPlan(planBuilder);
    }
    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdaptor.class);
      typePlan.append(UsedEnumFieldCollector.class);
    }
    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan2 =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdaptor.class);
      {
        if (features.contains(DxLegacy.class)) {
          typePlan2.append(VisibilityBridgeAdder.class);
        }
        SubPlanBuilder<JMethod> methodPlan =
            typePlan2.appendSubPlan(JMethodAdaptor.class);
        methodPlan.append(NotSimplifier.class);
        methodPlan.append(AssertionTransformer.class);
      }
    }
    planBuilder.append(AssertionTransformerSchedulingSeparator.class);
    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan3 =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdaptor.class);

      {
        {
          SubPlanBuilder<JField> fieldPlan =
              typePlan3.appendSubPlan(JFieldAdaptor.class);
          fieldPlan.append(FieldInitializer.class);
        }
        {
          SubPlanBuilder<JMethod> methodPlan2 =
              typePlan3.appendSubPlan(JMethodAdaptor.class);
          methodPlan2.append(ImplicitBlocks.class);
          if (hasSanityChecks) {
            methodPlan2.append(ImplicitBlocksChecker.class);
          }
          if (hasSanityChecks) {
            methodPlan2.append(UselessIfChecker.class);
          }
          methodPlan2.append(IncDecRemover.class);
          methodPlan2.append(CompoundAssignmentRemover.class);
          methodPlan2.append(ConcatRemover.class);
          methodPlan2.append(InnerAccessorGenerator.class);
        }
      }
    }

    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdaptor.class);
      SubPlanBuilder<JMethod> methodPlan = typePlan.appendSubPlan(JMethodAdaptor.class);
      methodPlan.append(SwitchEnumSupport.class);
    }

    planBuilder.append(InnerAccessorSchedulingSeparator.class);
    planBuilder.append(TryStatementSchedulingSeparator.class);
    planBuilder.append(EnumMappingSchedulingSeparator.class);

    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan4 =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdaptor.class);
      typePlan4.append(InnerAccessorAdder.class);
      typePlan4.append(UsedEnumFieldMarkerRemover.class);
      {
        SubPlanBuilder<JMethod> methodPlan =
            typePlan4.appendSubPlan(JMethodAdaptor.class);
        methodPlan.append(FlowNormalizer.class);
        if (features.contains(SourceVersion7.class)) {
          methodPlan.append(SwitchStringSupport.class);
        }
        methodPlan.append(EnumMappingMarkerRemover.class);
        methodPlan.append(EmptyClinitRemover.class);
      }

      typePlan4.append(FlowNormalizerSchedulingSeparator.class);
      {
        SubPlanBuilder<JMethod> methodPlan3 =
            typePlan4.appendSubPlan(JMethodAdaptor.class);
        methodPlan3.append(FieldInitMethodCallRemover.class);

        typePlan4.append(FieldInitMethodRemover.class);
        if (features.contains(JackFileOutput.class)) {
          typePlan4.append(JayceSingleTypeWriter.class);
        }
      }
    }
    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan4 =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdaptor.class);

      {
        SubPlanBuilder<JMethod> methodPlan = typePlan4.appendSubPlan(JMethodAdaptor.class);
        methodPlan.append(DefaultValueAnnotationAdder.class);
        methodPlan.append(ConditionalAndOrRemover.class);
        if (hasSanityChecks) {
          methodPlan.append(ConditionalAndOrRemoverChecker.class);
        }
        methodPlan.append(BooleanTestTransformer.class);
        methodPlan.append(SplitNewInstance.class);
        if (hasSanityChecks) {
          methodPlan.append(SplitNewInstanceChecker.class);
        }
        methodPlan.append(MultiDimensionNewArrayRemover.class);
        methodPlan.append(InitInNewArrayRemover.class);
        methodPlan.append(PrimitiveClassTransformer.class);
        methodPlan.append(SynchronizeTransformer.class);
        methodPlan.append(NestedAssignRemover.class);
        methodPlan.append(TypeLegalizer.class);
        methodPlan.append(RopCastLegalizer.class);
        methodPlan.append(UselessCastRemover.class);
        methodPlan.append(BinaryOperationWithCst.class);
        methodPlan.append(UselessCaseRemover.class);
        methodPlan.append(UselessSwitchesRemover.class);
        if (hasSanityChecks) {
          methodPlan.append(UselessCaseChecker.class);
        }
        methodPlan.append(FinallyRemover.class);
        methodPlan.append(ExceptionRuntimeValueAdder.class);
        methodPlan.append(DefinitionMarkerAdder.class);
        methodPlan.append(ThreeAddressCodeBuilder.class);
        methodPlan.append(DefinitionMarkerRemover.class);
        methodPlan.append(TryCatchRemover.class);
        methodPlan.append(ExpressionStatementLegalizer.class);
        methodPlan.append(NumericConversionChecker.class);
      }
    }
    if (features.contains(Obfuscation.class)) {
      appendObfuscationPlan(planBuilder);
    } else {
      planBuilder.append(NameFinalizer.class);
    }
    if (productions.contains(Mapping.class)) {
      planBuilder.append(MappingPrinter.class);
    }
    if (productions.contains(TypeAndMemberListing.class)) {
      planBuilder.append(TypeAndMemberLister.class);
    }
    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdaptor.class);
      typePlan.append(ReflectAnnotationsAdder.class);
    }
    planBuilder.append(ClassAnnotationSchedulingSeparator.class);
    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdaptor.class);
      typePlan.append(ClassDefItemBuilder.class);
      typePlan.append(ClassAnnotationBuilder.class);
    }

    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan5 =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdaptor.class);
      {
        SubPlanBuilder<JMethod> methodPlan4 =
            typePlan5.appendSubPlan(JMethodAdaptor.class);
        methodPlan4.append(RefAsStatementRemover.class);
        methodPlan4.append(CfgBuilder.class);
        methodPlan4.append(DefinitionMarkerAdder.class);
        methodPlan4.append(ReachingDefinitions.class);
        methodPlan4.append(UsedVariableAdder.class);
        methodPlan4.append(DefUsesAndUseDefsChainComputation.class);
        if (hasSanityChecks) {
          methodPlan4.append(UseDefsChecker.class);
        }
        methodPlan4.append(ConstantRefinerAndVariableRemover.class);
        methodPlan4.append(UseDefsChainsSimplifier.class);
        methodPlan4.append(DefUsesChainsSimplifier.class);
        // Instructions are removed by DefUsesChainsSimplifier thus rebuild the cfg.
        methodPlan4.append(CfgMarkerRemover.class);
        methodPlan4.append(CfgBuilder.class);
        methodPlan4.append(UnusedDefinitionRemover.class);
        methodPlan4.append(RefAsStatementRemover.class);
        methodPlan4.append(CfgMarkerRemover.class);
        methodPlan4.append(CfgBuilder.class);
        methodPlan4.append(IfWithConstantSimplifier.class);
        methodPlan4.append(UnusedLocalRemover.class);
        methodPlan4.append(DefUsesAndUseDefsChainRemover.class);
        methodPlan4.append(DefinitionMarkerRemover.class);
        methodPlan4.append(UsedVariableRemover.class);
        methodPlan4.append(ReachingDefinitionsRemover.class);
        methodPlan4.append(ExpressionSimplifier.class);
        methodPlan4.append(UselessIfRemover.class);
        methodPlan4.append(CfgMarkerRemover.class);
        methodPlan4.append(CfgBuilder.class);
        methodPlan4.append(CodeItemBuilder.class);
        methodPlan4.append(CfgMarkerRemover.class);
        methodPlan4.append(EncodedMethodBuilder.class);
        methodPlan4.append(MethodAnnotationBuilder.class);
        if (!options.keepMethodBody) {
          methodPlan4.append(MethodBodyRemover.class);
        }
        {
          SubPlanBuilder<JField> fieldPlan2 =
              typePlan5.appendSubPlan(JFieldAdaptor.class);
          fieldPlan2.append(EncodedFieldBuilder.class);
          fieldPlan2.append(FieldAnnotationBuilder.class);
        }
      }
    }

    if (hasSanityChecks) {
      planBuilder.append(ParentSetterChecker.class);
      {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdaptor.class);
      typePlan.append(DeclaredTypePackageChecker.class);
      }
      {
        SubPlanBuilder<JPackage> packagePlan = planBuilder.appendSubPlan(JPackageAdapter.class);
        packagePlan.append(PackageChecker.class);
      }
    }

    planBuilder.append(DexFilePreparer.class);
  }

  private static void fillJavaToJaycePlan(
      @Nonnull Options options, @Nonnull PlanBuilder<JSession> planBuilder) {
    Request request = planBuilder.getRequest();
    FeatureSet features = request.getFeatures();
    ProductionSet productions = request.getTargetProductions();
    boolean hasSanityChecks = features.contains(SanityChecks.class);

    // Build the plan
    if (hasSanityChecks) {
      planBuilder.append(TypeDuplicateRemoverChecker.class);
    }

    if (features.contains(Jarjar.class)) {
      planBuilder.append(PackageRenamer.class);
    }

    if (features.contains(Shrinking.class) || features.contains(Obfuscation.class)) {
      appendStringRefiningPlan(planBuilder);
    }

    if (productions.contains(SeedFile.class)) {
      planBuilder.append(SeedPrinter.class);
    }
    if (features.contains(Shrinking.class)) {
      appendShrinkingPlan(planBuilder);
    }

    if (hasSanityChecks) {
      planBuilder.append(ParentSetterChecker.class);
    }
    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan7 =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdaptor.class);
      typePlan7.append(UsedEnumFieldCollector.class);

      {
        SubPlanBuilder<JMethod> methodPlan =
            typePlan7.appendSubPlan(JMethodAdaptor.class);
        if (features.contains(LineDebugInfo.class)) {
          methodPlan.append(ThisRefDebugInfoAdder.class);
        }
      }
      {
        SubPlanBuilder<JField> fieldPlan =
            typePlan7.appendSubPlan(JFieldAdaptor.class);
        fieldPlan.append(FieldInitializerRemover.class);
      }
    }
    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan2 =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdaptor.class);
      if (features.contains(DxLegacy.class)) {
        typePlan2.append(VisibilityBridgeAdder.class);
      }
      {
        SubPlanBuilder<JMethod> methodPlan =
            typePlan2.appendSubPlan(JMethodAdaptor.class);
        methodPlan.append(NotSimplifier.class);
        methodPlan.append(AssertionTransformer.class);
        if (features.contains(SourceVersion7.class)) {
          methodPlan.append(TryWithResourcesTransformer.class);
        }
      }
    }
    planBuilder.append(AssertionTransformerSchedulingSeparator.class);
    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan3 =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdaptor.class);
      {
        {
          SubPlanBuilder<JField> fieldPlan =
              typePlan3.appendSubPlan(JFieldAdaptor.class);
          fieldPlan.append(FieldInitializer.class);
        }
        {
          SubPlanBuilder<JMethod> methodPlan2 =
              typePlan3.appendSubPlan(JMethodAdaptor.class);
          methodPlan2.append(ImplicitBlocks.class);
          if (hasSanityChecks) {
            methodPlan2.append(ImplicitBlocksChecker.class);
          }

          methodPlan2.append(IncDecRemover.class);
          methodPlan2.append(CompoundAssignmentRemover.class);
          methodPlan2.append(ConcatRemover.class);
          methodPlan2.append(InnerAccessorGenerator.class);
        }
      }
    }

    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdaptor.class);
      SubPlanBuilder<JMethod> methodPlan = typePlan.appendSubPlan(JMethodAdaptor.class);
      methodPlan.append(SwitchEnumSupport.class);
    }

    planBuilder.append(InnerAccessorSchedulingSeparator.class);
    planBuilder.append(EnumMappingSchedulingSeparator.class);

    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan4 =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdaptor.class);
      typePlan4.append(InnerAccessorAdder.class);
      typePlan4.append(UsedEnumFieldMarkerRemover.class);
      {
        SubPlanBuilder<JMethod> methodPlan =
            typePlan4.appendSubPlan(JMethodAdaptor.class);
        methodPlan.append(EnumMappingMarkerRemover.class);
        methodPlan.append(FlowNormalizer.class);
        methodPlan.append(EmptyClinitRemover.class);
      }
      typePlan4.append(FlowNormalizerSchedulingSeparator.class);
      {
        SubPlanBuilder<JMethod> methodPlan =
            typePlan4.appendSubPlan(JMethodAdaptor.class);
        methodPlan.append(FieldInitMethodCallRemover.class);
      }
      typePlan4.append(FieldInitMethodRemover.class);
    }
    planBuilder.append(TryStatementSchedulingSeparator.class);

    if (hasSanityChecks) {
      planBuilder.append(TypeDuplicateRemoverChecker.class);
    }
    if (features.contains(Obfuscation.class)) {
      appendObfuscationPlan(planBuilder);
    } else {
      planBuilder.append(NameFinalizer.class);
    }
    if (productions.contains(Mapping.class)) {
      planBuilder.append(MappingPrinter.class);
    }
    if (productions.contains(TypeAndMemberListing.class)) {
      planBuilder.append(TypeAndMemberLister.class);
    }
    if (features.contains(Shrinking.class) || features.contains(Obfuscation.class)) {
      appendShrobMarkerRemoverPlan(planBuilder);
    }
  }

  private static void appendStringRefiningPlan(@Nonnull PlanBuilder<JSession> planBuilder) {
    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdaptor.class);
      typePlan.append(TypeGenericSignatureSplitter.class);
      typePlan.append(TypeStringLiteralRefiner.class);
      {
        SubPlanBuilder<JMethod> methodPlan =
            typePlan.appendSubPlan(JMethodAdaptor.class);
        methodPlan.append(MethodGenericSignatureSplitter.class);
        methodPlan.append(ReflectionStringLiteralRefiner.class);
        methodPlan.append(MethodStringLiteralRefiner.class);
      }
      {
        SubPlanBuilder<JField> fieldPlan =
            typePlan.appendSubPlan(JFieldAdaptor.class);
        fieldPlan.append(FieldGenericSignatureSplitter.class);
        fieldPlan.append(FieldStringLiteralRefiner.class);
      }
    }
  }

  private static void appendShrobMarkerRemoverPlan(@Nonnull PlanBuilder<JSession> planBuilder) {
    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan4 =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdaptor.class);
      typePlan4.append(TypeShrinkMarkerRemover.class);
      typePlan4.append(TypeKeepNameMarkerRemover.class);
      typePlan4.append(TypeOriginalNameMarkerRemover.class);
      {
        SubPlanBuilder<JMethod> methodPlan = typePlan4.appendSubPlan(JMethodAdaptor.class);
        methodPlan.append(MethodKeepMarkerRemover.class);
        methodPlan.append(MethodKeepNameMarkerRemover.class);
      }
      {
        SubPlanBuilder<JField> fieldPlan = typePlan4.appendSubPlan(JFieldAdaptor.class);
        fieldPlan.append(FieldKeepMarkerRemover.class);
        fieldPlan.append(FieldKeepNameMarkerRemover.class);
      }
    }
  }

  private static void appendShrinkingPlan(@Nonnull PlanBuilder<JSession> planBuilder) {
    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdaptor.class);
      typePlan.append(ExtendingOrImplementingClassFinder.class);
    }
    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdaptor.class);
      typePlan.append(Keeper.class);
    }
    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdaptor.class);
      typePlan.append(TypeShrinker.class);
      {
        SubPlanBuilder<JMethod> methodPlan = typePlan.appendSubPlan(JMethodAdaptor.class);
        methodPlan.append(MethodShrinker.class);
      }
      {
        SubPlanBuilder<JField> fieldPlan = typePlan.appendSubPlan(JFieldAdaptor.class);
        fieldPlan.append(FieldShrinker.class);
      }
    }
  }

  private static void appendObfuscationPlan(@Nonnull PlanBuilder<JSession> planBuilder) {
    {
      SubPlanBuilder<JPackage> packagePlan =
          planBuilder.appendSubPlan(JPackageAdapter.class);
      packagePlan.append(NameKeeper.class);
    }
    planBuilder.append(Renamer.class);
    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdaptor.class);
      typePlan.append(TypeAnnotationRemover.class);
      {
        SubPlanBuilder<JField> fieldPlan = typePlan.appendSubPlan(JFieldAdaptor.class);
        fieldPlan.append(FieldAnnotationRemover.class);
      }
      {
        SubPlanBuilder<JMethod> methodPlan = typePlan.appendSubPlan(JMethodAdaptor.class);
        methodPlan.append(MethodAnnotationRemover.class);
        methodPlan.append(ParameterAnnotationRemover.class);
      }
    }
  }

  private static void fillJayceToDexPlan(
      @Nonnull Options options, @Nonnull PlanBuilder<JSession> planBuilder) {
    Request request = planBuilder.getRequest();
    FeatureSet features = request.getFeatures();
    ProductionSet productions = request.getTargetProductions();
    boolean hasSanityChecks = features.contains(SanityChecks.class);

    // Build the plan
    if (hasSanityChecks) {
      planBuilder.append(TypeDuplicateRemoverChecker.class);
    }

    if (features.contains(Jarjar.class)) {
      planBuilder.append(PackageRenamer.class);
    }

    if (features.contains(Shrinking.class) || features.contains(Obfuscation.class)) {
      appendStringRefiningPlan(planBuilder);
    }

    if (productions.contains(SeedFile.class)) {
      planBuilder.append(SeedPrinter.class);
    }
    if (features.contains(Shrinking.class)) {
      appendShrinkingPlan(planBuilder);
    }

    if (hasSanityChecks) {
      planBuilder.append(ParentSetterChecker.class);
    }
    planBuilder.append(DexFileBuilder.class);
    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan3 =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdaptor.class);
      {
        {
          SubPlanBuilder<JMethod> methodPlan2 =
              typePlan3.appendSubPlan(JMethodAdaptor.class);
          methodPlan2.append(UselessSwitchesRemover.class);
          methodPlan2.append(UselessIfRemover.class);
          if (hasSanityChecks) {
            methodPlan2.append(UselessIfChecker.class);
          }
          methodPlan2.append(DefaultValueAnnotationAdder.class);
        }
      }
    }

    if (features.contains(Obfuscation.class)) {
      appendObfuscationPlan(planBuilder);
    } else {
      planBuilder.append(NameFinalizer.class);
    }
    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdaptor.class);
      typePlan.append(ReflectAnnotationsAdder.class);
    }
    planBuilder.append(ClassAnnotationSchedulingSeparator.class);
    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan4 =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdaptor.class);
      typePlan4.append(ClassDefItemBuilder.class);
      typePlan4.append(ClassAnnotationBuilder.class);
      {
        SubPlanBuilder<JMethod> methodPlan3 = typePlan4.appendSubPlan(JMethodAdaptor.class);
        methodPlan3.append(ConditionalAndOrRemover.class);
        if (hasSanityChecks) {
          methodPlan3.append(ConditionalAndOrRemoverChecker.class);
        }
        methodPlan3.append(BooleanTestTransformer.class);
        methodPlan3.append(SplitNewInstance.class);
        if (hasSanityChecks) {
          methodPlan3.append(SplitNewInstanceChecker.class);
        }
        methodPlan3.append(MultiDimensionNewArrayRemover.class);
        methodPlan3.append(InitInNewArrayRemover.class);
        methodPlan3.append(PrimitiveClassTransformer.class);
        methodPlan3.append(SynchronizeTransformer.class);
        methodPlan3.append(NestedAssignRemover.class);
        methodPlan3.append(TypeLegalizer.class);
        methodPlan3.append(RopCastLegalizer.class);
        methodPlan3.append(UselessCastRemover.class);
        methodPlan3.append(BinaryOperationWithCst.class);
        methodPlan3.append(UselessCaseRemover.class);
        methodPlan3.append(UselessSwitchesRemover.class);
        if (hasSanityChecks) {
          methodPlan3.append(UselessCaseChecker.class);
        }
        methodPlan3.append(FinallyRemover.class);
        methodPlan3.append(ExceptionRuntimeValueAdder.class);
        methodPlan3.append(DefinitionMarkerAdder.class);
        methodPlan3.append(ThreeAddressCodeBuilder.class);
        methodPlan3.append(DefinitionMarkerRemover.class);
        methodPlan3.append(TryCatchRemover.class);
        methodPlan3.append(ExpressionStatementLegalizer.class);
        methodPlan3.append(NumericConversionChecker.class);
      }
    }
    if (productions.contains(Mapping.class)) {
      planBuilder.append(MappingPrinter.class);
    }
    if (productions.contains(TypeAndMemberListing.class)) {
      planBuilder.append(TypeAndMemberLister.class);
    }

    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan5 =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdaptor.class);
      {
        SubPlanBuilder<JMethod> methodPlan4 =
            typePlan5.appendSubPlan(JMethodAdaptor.class);
        methodPlan4.append(RefAsStatementRemover.class);
        methodPlan4.append(CfgBuilder.class);
        methodPlan4.append(DefinitionMarkerAdder.class);
        methodPlan4.append(ReachingDefinitions.class);
        methodPlan4.append(UsedVariableAdder.class);
        methodPlan4.append(DefUsesAndUseDefsChainComputation.class);
        if (hasSanityChecks) {
          methodPlan4.append(UseDefsChecker.class);
        }
        methodPlan4.append(ConstantRefinerAndVariableRemover.class);
        methodPlan4.append(UseDefsChainsSimplifier.class);
        methodPlan4.append(DefUsesChainsSimplifier.class);
        // Instructions are removed by DefUsesChainsSimplifier thus rebuild the cfg.
        methodPlan4.append(CfgMarkerRemover.class);
        methodPlan4.append(CfgBuilder.class);
        methodPlan4.append(UnusedDefinitionRemover.class);
        methodPlan4.append(RefAsStatementRemover.class);
        methodPlan4.append(CfgMarkerRemover.class);
        methodPlan4.append(CfgBuilder.class);
        methodPlan4.append(IfWithConstantSimplifier.class);
        methodPlan4.append(UnusedLocalRemover.class);
        methodPlan4.append(DefUsesAndUseDefsChainRemover.class);
        methodPlan4.append(DefinitionMarkerRemover.class);
        methodPlan4.append(UsedVariableRemover.class);
        methodPlan4.append(ReachingDefinitionsRemover.class);
        methodPlan4.append(ExpressionSimplifier.class);
        methodPlan4.append(UselessIfRemover.class);
        methodPlan4.append(CfgMarkerRemover.class);
        methodPlan4.append(CfgBuilder.class);
        methodPlan4.append(CodeItemBuilder.class);
        methodPlan4.append(CfgMarkerRemover.class);
        methodPlan4.append(EncodedMethodBuilder.class);
        methodPlan4.append(MethodAnnotationBuilder.class);
        if (!options.keepMethodBody) {
          methodPlan4.append(MethodBodyRemover.class);
        }
        {
          SubPlanBuilder<JField> fieldPlan2 =
              typePlan5.appendSubPlan(JFieldAdaptor.class);
          fieldPlan2.append(EncodedFieldBuilder.class);
          fieldPlan2.append(FieldAnnotationBuilder.class);
        }
      }
    }

    if (hasSanityChecks) {
      planBuilder.append(ParentSetterChecker.class);
    }

    planBuilder.append(DexFilePreparer.class);
  }

  @Nonnull
  public static String getVersionString() {
    String version = "Unknown (no resource file)";

    InputStream is = Jack.class.getClassLoader().getResourceAsStream("jack.properties");
    if (is != null) {
      Properties prop = new Properties();
      String noVersion = "Unknown (no jack.version entry)";
      try {
        prop.load(is);
        version = prop.getProperty("jack.version", noVersion);
      } catch (IOException e) {
        version = noVersion;
      }
    }

    return version;
  }

  @Nonnull
  public static TypePackageAndMethodFormatter getLookupFormatter() {
    return lookupFormatter;
  }

  @Nonnull
  public static TypePackageAndMethodFormatter getUserFriendlyFormatter() {
    return userFriendlyFormatter;
  }
}
