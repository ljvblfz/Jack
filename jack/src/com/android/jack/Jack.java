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

import com.android.jack.Options.SwitchEnumOptStrategy;
import com.android.jack.analysis.DefinitionMarkerAdder;
import com.android.jack.analysis.DefinitionMarkerRemover;
import com.android.jack.analysis.UsedVariableAdder;
import com.android.jack.analysis.UsedVariableRemover;
import com.android.jack.analysis.defsuses.DefUsesAndUseDefsChainComputation;
import com.android.jack.analysis.defsuses.DefUsesAndUseDefsChainRemover;
import com.android.jack.analysis.defsuses.UseDefsChecker;
import com.android.jack.analysis.dependency.DependencyInLibraryProduct;
import com.android.jack.analysis.dependency.file.FileDependenciesCollector;
import com.android.jack.analysis.dependency.file.FileDependenciesInLibraryWriter;
import com.android.jack.analysis.dependency.library.LibraryDependenciesInLibraryWriter;
import com.android.jack.analysis.dependency.type.TypeDependenciesCollector;
import com.android.jack.analysis.dependency.type.TypeDependenciesInLibraryWriter;
import com.android.jack.analysis.dfa.reachingdefs.ReachingDefinitions;
import com.android.jack.analysis.dfa.reachingdefs.ReachingDefinitionsRemover;
import com.android.jack.analysis.tracer.ExtendingOrImplementingClassFinder;
import com.android.jack.backend.ResourceWriter;
import com.android.jack.backend.dex.ClassAnnotationBuilder;
import com.android.jack.backend.dex.ClassDefItemBuilder;
import com.android.jack.backend.dex.DexFileProduct;
import com.android.jack.backend.dex.DexFileWriter;
import com.android.jack.backend.dex.DexInLibraryProduct;
import com.android.jack.backend.dex.DexInLibraryWriter;
import com.android.jack.backend.dex.EncodedFieldBuilder;
import com.android.jack.backend.dex.EncodedMethodBuilder;
import com.android.jack.backend.dex.FieldAnnotationBuilder;
import com.android.jack.backend.dex.FieldInitializerRemover;
import com.android.jack.backend.dex.MainDexCollector;
import com.android.jack.backend.dex.MainDexTracer;
import com.android.jack.backend.dex.MethodAnnotationBuilder;
import com.android.jack.backend.dex.MethodBodyRemover;
import com.android.jack.backend.dex.MultiDexAnnotationsFinder;
import com.android.jack.backend.dex.MultiDexLegacy;
import com.android.jack.backend.dex.annotations.ClassAnnotationSchedulingSeparator;
import com.android.jack.backend.dex.annotations.DefaultValueAnnotationAdder;
import com.android.jack.backend.dex.annotations.ReflectAnnotationsAdder;
import com.android.jack.backend.dex.multidex.legacy.AnnotatedFinder;
import com.android.jack.backend.dex.multidex.legacy.RuntimeAnnotationFinder;
import com.android.jack.backend.dex.rop.CodeItemBuilder;
import com.android.jack.backend.jayce.JayceFileImporter;
import com.android.jack.backend.jayce.JayceInLibraryProduct;
import com.android.jack.backend.jayce.JayceInLibraryWriter;
import com.android.jack.cfg.CfgBuilder;
import com.android.jack.cfg.CfgMarkerRemover;
import com.android.jack.config.id.JavaVersionPropertyId.JavaVersion;
import com.android.jack.frontend.FrontendCompilationException;
import com.android.jack.frontend.MethodIdDuplicateRemover;
import com.android.jack.frontend.MethodIdMerger;
import com.android.jack.frontend.TypeDuplicateRemoverChecker;
import com.android.jack.frontend.VirtualMethodsMarker;
import com.android.jack.frontend.java.JackBatchCompiler;
import com.android.jack.frontend.java.JackBatchCompiler.TransportExceptionAroundEcjError;
import com.android.jack.frontend.java.JackBatchCompiler.TransportJUEAroundEcjError;
import com.android.jack.incremental.GenerateLibraryFromIncrementalFolder;
import com.android.jack.incremental.Incremental;
import com.android.jack.incremental.InputFilter;
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
import com.android.jack.ir.sourceinfo.SourceInfoCreation;
import com.android.jack.jayce.JaycePackageLoader;
import com.android.jack.library.FileType;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.library.InputLibrary;
import com.android.jack.library.LibraryIOException;
import com.android.jack.library.LibraryReadingException;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.lookup.JPhantomLookup;
import com.android.jack.meta.LibraryMetaWriter;
import com.android.jack.meta.MetaImporter;
import com.android.jack.optimizations.ConstantRefinerAndVariableRemover;
import com.android.jack.optimizations.DefUsesChainsSimplifier;
import com.android.jack.optimizations.ExpressionSimplifier;
import com.android.jack.optimizations.IfWithConstantSimplifier;
import com.android.jack.optimizations.NotSimplifier;
import com.android.jack.optimizations.UnusedDefinitionRemover;
import com.android.jack.optimizations.UseDefsChainsSimplifier;
import com.android.jack.optimizations.tailrecursion.TailRecursionOptimization;
import com.android.jack.optimizations.tailrecursion.TailRecursionOptimizer;
import com.android.jack.preprocessor.PreProcessor;
import com.android.jack.preprocessor.PreProcessorApplier;
import com.android.jack.reporting.Reporter;
import com.android.jack.reporting.Reporter.Severity;
import com.android.jack.resource.LibraryResourceWriter;
import com.android.jack.resource.ResourceImporter;
import com.android.jack.resource.ResourceReadingException;
import com.android.jack.scheduling.adapter.ExcludeTypeFromLibAdapter;
import com.android.jack.scheduling.adapter.ExcludeTypeFromLibWithBinaryAdapter;
import com.android.jack.scheduling.adapter.JDefinedClassOrInterfaceAdapter;
import com.android.jack.scheduling.adapter.JFieldAdapter;
import com.android.jack.scheduling.adapter.JMethodAdapter;
import com.android.jack.scheduling.adapter.JPackageAdapter;
import com.android.jack.scheduling.feature.CompiledTypeStats;
import com.android.jack.scheduling.feature.DropMethodBody;
import com.android.jack.scheduling.feature.Resources;
import com.android.jack.scheduling.feature.SourceVersion7;
import com.android.jack.scheduling.feature.VisibilityBridge;
import com.android.jack.shrob.obfuscation.Mapping;
import com.android.jack.shrob.obfuscation.MappingPrinter;
import com.android.jack.shrob.obfuscation.NameFinalizer;
import com.android.jack.shrob.obfuscation.NameKeeper;
import com.android.jack.shrob.obfuscation.Obfuscation;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.jack.shrob.obfuscation.RemoveSourceFile;
import com.android.jack.shrob.obfuscation.Renamer;
import com.android.jack.shrob.obfuscation.SourceFileRemover;
import com.android.jack.shrob.obfuscation.SourceFileRenamer;
import com.android.jack.shrob.obfuscation.SourceFileRenaming;
import com.android.jack.shrob.obfuscation.annotation.AnnotationDefaultValueRemover;
import com.android.jack.shrob.obfuscation.annotation.FieldAnnotationRemover;
import com.android.jack.shrob.obfuscation.annotation.FieldGenericSignatureRemover;
import com.android.jack.shrob.obfuscation.annotation.LineNumberRemover;
import com.android.jack.shrob.obfuscation.annotation.LocalVariableGenericSignatureRemover;
import com.android.jack.shrob.obfuscation.annotation.MethodAnnotationRemover;
import com.android.jack.shrob.obfuscation.annotation.MethodGenericSignatureRemover;
import com.android.jack.shrob.obfuscation.annotation.ParameterAnnotationRemover;
import com.android.jack.shrob.obfuscation.annotation.ParameterNameRemover;
import com.android.jack.shrob.obfuscation.annotation.RemoveAnnotationDefaultValue;
import com.android.jack.shrob.obfuscation.annotation.RemoveEnclosingMethod;
import com.android.jack.shrob.obfuscation.annotation.RemoveEnclosingType;
import com.android.jack.shrob.obfuscation.annotation.RemoveGenericSignature;
import com.android.jack.shrob.obfuscation.annotation.RemoveLineNumber;
import com.android.jack.shrob.obfuscation.annotation.RemoveLocalVariableGenericSignature;
import com.android.jack.shrob.obfuscation.annotation.RemoveParameterName;
import com.android.jack.shrob.obfuscation.annotation.RemoveThrownException;
import com.android.jack.shrob.obfuscation.annotation.ThrownExceptionRemover;
import com.android.jack.shrob.obfuscation.annotation.TypeAnnotationRemover;
import com.android.jack.shrob.obfuscation.annotation.TypeEnclosingMethodRemover;
import com.android.jack.shrob.obfuscation.annotation.TypeEnclosingTypeRemover;
import com.android.jack.shrob.obfuscation.annotation.TypeGenericSignatureRemover;
import com.android.jack.shrob.obfuscation.resource.AdaptResourceFileContent;
import com.android.jack.shrob.obfuscation.resource.ResourceContentRefiner;
import com.android.jack.shrob.obfuscation.resource.ResourceRefiner;
import com.android.jack.shrob.proguard.GrammarActions;
import com.android.jack.shrob.seed.SeedFile;
import com.android.jack.shrob.seed.SeedFinder;
import com.android.jack.shrob.seed.SeedPrinter;
import com.android.jack.shrob.shrink.FieldShrinker;
import com.android.jack.shrob.shrink.Keeper;
import com.android.jack.shrob.shrink.MethodShrinker;
import com.android.jack.shrob.shrink.ShrinkAndMainDexTracer;
import com.android.jack.shrob.shrink.ShrinkStructurePrinter;
import com.android.jack.shrob.shrink.Shrinking;
import com.android.jack.shrob.shrink.StructurePrinting;
import com.android.jack.shrob.shrink.TypeShrinker;
import com.android.jack.shrob.spec.Flags;
import com.android.jack.statistics.BinaryOperationWithCst;
import com.android.jack.statistics.CodeStats;
import com.android.jack.statistics.FieldStats;
import com.android.jack.statistics.MethodStats;
import com.android.jack.transformations.AssertionTransformer;
import com.android.jack.transformations.AssertionTransformerSchedulingSeparator;
import com.android.jack.transformations.EmptyClinitRemover;
import com.android.jack.transformations.FieldInitializer;
import com.android.jack.transformations.Jarjar;
import com.android.jack.transformations.OptimizedSwitchEnumFeedbackFeature;
import com.android.jack.transformations.OptimizedSwitchEnumNonFeedbackFeature;
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
import com.android.jack.transformations.ast.inner.AvoidSynthethicAccessors;
import com.android.jack.transformations.ast.inner.InnerAccessorAdder;
import com.android.jack.transformations.ast.inner.InnerAccessorGenerator;
import com.android.jack.transformations.ast.inner.InnerAccessorSchedulingSeparator;
import com.android.jack.transformations.ast.inner.MethodCallDispatchAdjuster;
import com.android.jack.transformations.ast.inner.OptimizedInnerAccessorGenerator;
import com.android.jack.transformations.ast.inner.OptimizedInnerAccessorSchedulingSeparator;
import com.android.jack.transformations.ast.inner.ReferencedOuterFieldsExposer;
import com.android.jack.transformations.ast.removeinit.FieldInitMethodCallRemover;
import com.android.jack.transformations.ast.removeinit.FieldInitMethodRemover;
import com.android.jack.transformations.ast.splitnew.SplitNewInstance;
import com.android.jack.transformations.ast.splitnew.SplitNewInstanceChecker;
import com.android.jack.transformations.ast.string.FieldGenericSignatureSplitter;
import com.android.jack.transformations.ast.string.FieldStringLiteralRefiner;
import com.android.jack.transformations.ast.string.MethodGenericSignatureSplitter;
import com.android.jack.transformations.ast.string.MethodStringLiteralRefiner;
import com.android.jack.transformations.ast.string.ReflectionStringLiteralRefiner;
import com.android.jack.transformations.ast.string.SimpleNameRefiner;
import com.android.jack.transformations.ast.string.TypeGenericSignatureSplitter;
import com.android.jack.transformations.ast.string.TypeStringLiteralRefiner;
import com.android.jack.transformations.ast.switches.SwitchStringSupport;
import com.android.jack.transformations.ast.switches.UselessCaseChecker;
import com.android.jack.transformations.ast.switches.UselessCaseRemover;
import com.android.jack.transformations.ast.switches.UselessSwitchesRemover;
import com.android.jack.transformations.booleanoperators.ConditionalAndOrRemover;
import com.android.jack.transformations.booleanoperators.ConditionalAndOrRemoverChecker;
import com.android.jack.transformations.cast.UselessCastRemover;
import com.android.jack.transformations.enums.EnumMappingMarkerRemover;
import com.android.jack.transformations.enums.EnumMappingSchedulingSeparator;
import com.android.jack.transformations.enums.SwitchEnumSupport;
import com.android.jack.transformations.enums.UsedEnumFieldCollector;
import com.android.jack.transformations.enums.UsedEnumFieldMarkerRemover;
import com.android.jack.transformations.enums.opt.OptimizedSwitchEnumSupport;
import com.android.jack.transformations.enums.opt.SwitchEnumUsageCollector;
import com.android.jack.transformations.exceptions.ExceptionRuntimeValueAdder;
import com.android.jack.transformations.exceptions.TryCatchRemover;
import com.android.jack.transformations.exceptions.TryStatementSchedulingSeparator;
import com.android.jack.transformations.finallyblock.FinallyRemover;
import com.android.jack.transformations.flow.FlowNormalizer;
import com.android.jack.transformations.flow.FlowNormalizerSchedulingSeparator;
import com.android.jack.transformations.parent.AstChecker;
import com.android.jack.transformations.parent.TypeAstChecker;
import com.android.jack.transformations.renamepackage.PackageRenamer;
import com.android.jack.transformations.rop.cast.RopCastLegalizer;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeBuilder;
import com.android.jack.transformations.typedef.TypeDefRemover;
import com.android.jack.transformations.typedef.TypeDefRemover.RemoveTypeDef;
import com.android.jack.transformations.uselessif.UselessIfChecker;
import com.android.jack.transformations.uselessif.UselessIfRemover;
import com.android.jack.util.collect.UnmodifiableCollections;
import com.android.sched.scheduler.FeatureSet;
import com.android.sched.scheduler.IllegalRequestException;
import com.android.sched.scheduler.Plan;
import com.android.sched.scheduler.PlanBuilder;
import com.android.sched.scheduler.PlanNotFoundException;
import com.android.sched.scheduler.PlanPrinterFactory;
import com.android.sched.scheduler.ProcessException;
import com.android.sched.scheduler.ProductionSet;
import com.android.sched.scheduler.Request;
import com.android.sched.scheduler.Scheduler;
import com.android.sched.scheduler.SubPlanBuilder;
import com.android.sched.scheduler.TagOrMarkerOrComponentSet;
import com.android.sched.util.RunnableHooks;
import com.android.sched.util.Version;
import com.android.sched.util.config.Config;
import com.android.sched.util.config.ConfigPrinterFactory;
import com.android.sched.util.config.ConfigurationException;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ReflectFactory;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.config.id.ObjectId;
import com.android.sched.util.config.id.ReflectFactoryPropertyId;
import com.android.sched.util.log.Event;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.vfs.Container;

import org.antlr.runtime.RecognitionException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Executable class to run the jack compiler.
 */
@HasKeyId
public abstract class Jack {

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
          .addArgType(InputJackLibrary.class)
          .addArgType(JPhantomLookup.class)
          .bypassAccessibility()
          .addDefaultValue("structure");

  @Nonnull
  private static final
      ReflectFactoryPropertyId<JaycePackageLoader> IMPORT_POLICY = ReflectFactoryPropertyId.create(
          "jack.internal.jayce.loader.import.policy",
          "Hint on default load policy for import entries",
          JaycePackageLoader.class)
          .addArgType(InputJackLibrary.class)
          .addArgType(JPhantomLookup.class)
          .bypassAccessibility()
          .addDefaultValue("type");

  @Nonnull
  public static final BooleanPropertyId STRICT_CLASSPATH = BooleanPropertyId.create(
      "jack.classpath.strict", "Do not ignore missing or malformed class path entries")
      .addDefaultValue(Boolean.FALSE);

  @Nonnull
  public static JSession getSession() {
    return ThreadConfig.get(Jack.SESSION);
  }

  @Nonnull
  public static String getEmitterId() {
    return "jack";
  }

  @Nonnull
  public static UnmodifiableCollections getUnmodifiableCollections() {
    if (unmodifiableCollections == null) {
      unmodifiableCollections =
          ThreadConfig.get(UnmodifiableCollections.UNMODIFIABLE_COLLECTION).create();
    }
    assert unmodifiableCollections != null; // FINDBUGS
    return unmodifiableCollections;
  }

  public static void checkAndRun(@Nonnull Options options)
      throws IllegalOptionsException,
      ConfigurationException,
      JackUserException, ProcessException {
    RunnableHooks hooks = new RunnableHooks();
    try {
      check(options, hooks);
      run(options, hooks);
    } finally {
      hooks.runHooks();
    }
  }

  public static void check(@Nonnull Options options, @Nonnull RunnableHooks hooks)
      throws IllegalOptionsException, ConfigurationException {

    if (options.proguardFlagsFiles != null && !options.proguardFlagsFiles.isEmpty()) {
      if (options.flags == null) {
        options.flags = new Flags();
      }
      for (File proguardFlagsFile : options.getProguardFlagsFile()) {
        try {
          assert options.flags != null;
          GrammarActions.parse(proguardFlagsFile.getPath(), ".", options.flags);
        } catch (RecognitionException e) {
          throw new IllegalOptionsException(
              "Error while parsing '" + e.input.getSourceName() + "':" + e.line, e);
        }
      }

      options.applyShrobFlags();
    }

    options.checkValidity(hooks);

    Config config = options.getConfig();


    boolean sanityChecks = config.get(Options.SANITY_CHECKS).booleanValue();

    logger.log(Level.INFO, "Jack sanity checks {0}",
        (sanityChecks ? "enabled" : "disabled"));
  }

  /**
   * Runs the jack compiler.
   * @param options options for the compiler.
   * @param hooks hooks that allow to attach actions that should be run after calling this method
   * @throws JackUserException thrown to report information to the user
   * @throws ProcessException thrown during schedulable execution
   */
  public static void run(@Nonnull Options options, @Nonnull RunnableHooks hooks)
      throws JackUserException, ProcessException {

    Event event = null;

    try {
      Config config = options.getConfig();
      ThreadConfig.setConfig(config);

      event = TracerFactory.getTracer().start(JackEventType.JACK_RUN);

      ConfigPrinterFactory.getConfigPrinter().printConfig(config);

      JSession session = getSession();

      try {
        buildSession(session, options, hooks);

        if (config.get(Options.GENERATE_JACK_LIBRARY).booleanValue()) {
          InputFilter inputFilter = session.getInputFilter();
          assert inputFilter != null;
          session.setJackOutputLibrary(inputFilter.getOutputJackLibrary());
        }

        Request request = createInitialRequest();
        request.addFeature(PreProcessor.class);

        request.addFeature(Resources.class);

        JavaVersion sourceVersion = config.get(Options.JAVA_SOURCE_VERSION);
        if (sourceVersion.compareTo(JavaVersion.JAVA_7) >= 0) {
          request.addFeature(SourceVersion7.class);
        }

        if (config.get(Options.DROP_METHOD_BODY).booleanValue()) {
          request.addFeature(DropMethodBody.class);
        }

        if (config.get(Options.ENABLE_COMPILED_FILES_STATISTICS).booleanValue()) {
          request.addFeature(CompiledTypeStats.class);
          request.addFeature(CodeStats.class);
        }

        if (config.get(Options.SANITY_CHECKS).booleanValue()) {
          request.addFeature(SanityChecks.class);
        }
        if (config.get(PackageRenamer.JARJAR_ENABLED).booleanValue()) {
          request.addFeature(Jarjar.class);
        }
        if (config.get(VisibilityBridgeAdder.VISIBILITY_BRIDGE).booleanValue()) {
          request.addFeature(VisibilityBridge.class);
        }
        if (options.flags != null) {
          if (options.flags.shrink()) {
            request.addFeature(Shrinking.class);
          }
          if (options.flags.obfuscate()) {
            request.addFeature(Obfuscation.class);

            if (!options.flags.keepAttribute("EnclosingMethod")) {
              request.addFeature(RemoveEnclosingMethod.class);
            }
            if (!options.flags.keepAttribute("InnerClasses")) {
              request.addFeature(RemoveEnclosingType.class);
            }
            if (!options.flags.keepAttribute("Signature")) {
              request.addFeature(RemoveGenericSignature.class);
            }
            if (!options.flags.keepAttribute("AnnotationDefault")) {
              request.addFeature(RemoveAnnotationDefaultValue.class);
            }
            if (!options.flags.keepAttribute("LocalVariableTypeTable")) {
              request.addFeature(RemoveLocalVariableGenericSignature.class);
            }
            if (!options.flags.keepAttribute("Exceptions")) {
              request.addFeature(RemoveThrownException.class);
            }
            if (!options.flags.keepAttribute("SourceFile")) {
              request.addFeature(RemoveSourceFile.class);
            }
            if (!options.flags.keepAttribute("LineNumberTable")) {
              request.addFeature(RemoveLineNumber.class);
            }
            if (!options.flags.getKeepParameterNames()
                && !options.flags.keepAttribute("LocalVariableTable")) {
              request.addFeature(RemoveParameterName.class);
            }
          }
          if (options.flags.printSeeds()) {
            request.addProduction(SeedFile.class);
          }
          if (options.flags.getRenameSourceFileAttribute() != null) {
            request.addFeature(SourceFileRenaming.class);
          }
          if (options.flags.getAdaptResourceFileContents() != null) {
            request.addFeature(AdaptResourceFileContent.class);
          }
        }
        if (config.get(MappingPrinter.MAPPING_OUTPUT_ENABLED).booleanValue()) {
          request.addProduction(Mapping.class);
        }
        if (config.get(ShrinkStructurePrinter.STRUCTURE_PRINTING).booleanValue()) {
          request.addProduction(StructurePrinting.class);
        }
        if (config.get(MultiDexLegacy.MULTIDEX_LEGACY).booleanValue()) {
          request.addFeature(MultiDexLegacy.class);
        }
        if (config.get(Options.INCREMENTAL_MODE).booleanValue()) {
          request.addFeature(Incremental.class);
        }
        if (config.get(Options.GENERATE_LIBRARY_FROM_INCREMENTAL_FOLDER).booleanValue()) {
          request.addFeature(GenerateLibraryFromIncrementalFolder.class);
        }

        if (config.get(Options.OPTIMIZE_INNER_CLASSES_ACCESSORS).booleanValue()) {
          request.addFeature(AvoidSynthethicAccessors.class);
        }

        if (config.get(Options.OPTIMIZE_TAIL_RECURSION).booleanValue()) {
          request.addFeature(TailRecursionOptimization.class);
        }

        request.addInitialTagsOrMarkers(getJavaSourceInitialTagSet());
        request.addInitialTagsOrMarkers(getJackFormatInitialTagSet());

        if (config.get(Options.GENERATE_DEX_IN_LIBRARY).booleanValue()) {
          request.addProduction(DexInLibraryProduct.class);
        }

        if (config.get(Options.GENERATE_DEX_FILE).booleanValue()) {
          request.addProduction(DexFileProduct.class);
          session.addGeneratedFileType(FileType.DEX);
        }

        if (config.get(Options.OPTIMIZED_ENUM_SWITCH) == SwitchEnumOptStrategy.FEEDBACK) {
          request.addFeature(OptimizedSwitchEnumFeedbackFeature.class);
        } else if (config.get(Options.OPTIMIZED_ENUM_SWITCH) == SwitchEnumOptStrategy.ALWAYS) {
          request.addFeature(OptimizedSwitchEnumNonFeedbackFeature.class);
        }

        if (config.get(Options.GENERATE_JAYCE_IN_LIBRARY).booleanValue()) {
          request.addProduction(JayceInLibraryProduct.class);
        }

        if (config.get(Options.GENERATE_DEPENDENCIES_IN_LIBRARY).booleanValue()) {
          request.addProduction(DependencyInLibraryProduct.class);
        }

        if (config.get(TypeDefRemover.REMOVE_TYPEDEF).booleanValue()) {
          request.addFeature(TypeDefRemover.RemoveTypeDef.class);
        }

        ProductionSet targetProduction = request.getTargetProductions();
        FeatureSet features = request.getFeatures();
        PlanBuilder<JSession> planBuilder;
        try {
          planBuilder = request.getPlanBuilder(JSession.class);
        } catch (IllegalRequestException e) {
          throw new AssertionError(e);
        }

        planBuilder.append(PreProcessorApplier.class);

        fillDexPlan(planBuilder);
        if (targetProduction.contains(DexFileProduct.class)) {
          planBuilder.append(DexFileWriter.class);
        }

        if (features.contains(Resources.class)) {
          if (targetProduction.contains(DexFileProduct.class)) {
            planBuilder.append(ResourceWriter.class);
          }
          if (targetProduction.contains(JayceInLibraryProduct.class)) {
            planBuilder.append(LibraryResourceWriter.class);
          }
        }

        if (targetProduction.contains(JayceInLibraryProduct.class)) {
          planBuilder.append(LibraryMetaWriter.class);
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

          assert !targetProduction.contains(JayceInLibraryProduct.class)
              || targetProduction.contains(DexFileProduct.class) || (plan.computeFinalTagsOrMarkers(
                  request.getInitialTags()).contains(JackFormatIr.class)
                  && !targetProduction.contains(DexInLibraryProduct.class)) || (
                  targetProduction.contains(DexInLibraryProduct.class)
                  && targetProduction.contains(JayceInLibraryProduct.class));
        }

        PlanPrinterFactory.getPlanPrinter().printPlan(plan);

        plan.getScheduleInstance().process(session);
      } finally {
        try {
          InputFilter inputFilter = session.getInputFilter();
          if (inputFilter != null) {
            inputFilter.getOutputJackLibrary().close();
          }

          // TODO(jack-team): auto-close
          if (config.get(Options.GENERATE_DEX_FILE).booleanValue()
              && config.get(Options.DEX_OUTPUT_CONTAINER_TYPE) == Container.ZIP) {
            config.get(Options.DEX_OUTPUT_ZIP).close();
          }
        } catch (LibraryIOException e) {
          throw new AssertionError(e);
        } catch (IOException e) {
          throw new AssertionError(e);
        }
      }
    } finally {
      if (event != null) {
        event.end();
      }
      ThreadConfig.unsetConfig();
    }
  }

  @Nonnull
  public static Request createInitialRequest() {
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
    set.add(SourceInfoCreation.class);
    return set;
  }

  @Nonnull
  private static TagOrMarkerOrComponentSet getJackFormatInitialTagSet() {
    Scheduler scheduler = Scheduler.getScheduler();
    TagOrMarkerOrComponentSet set = scheduler.createTagOrMarkerOrComponentSet();
    set.add(JackFormatIr.class);
    set.add(OriginalNames.class);
    set.add(SourceInfoCreation.class);
    return set;
  }

  @Nonnull
  static JSession buildSession(@Nonnull Options options,
      @Nonnull RunnableHooks hooks) throws JackUserException {
    JSession session = getSession();
    buildSession(session, options, hooks);
    return session;
  }

  @Nonnull
  private static void buildSession(@Nonnull JSession session, @Nonnull Options options,
      @Nonnull RunnableHooks hooks) throws JackUserException {
    Tracer tracer = TracerFactory.getTracer();

    session.setHooks(hooks);

    session.setReporter(ThreadConfig.get(Reporter.REPORTER));

    Config config = ThreadConfig.getConfig();

    InputFilter inputFilter;
    try {
      inputFilter = config.get(Options.INPUT_FILTER).create(options);
      session.setInputFilter(inputFilter);
    } catch (RuntimeException e) {
      Throwable cause = e.getCause();
      if (cause instanceof JackAbortException) {
        throw (JackAbortException) cause;
      } else if (cause instanceof JackUserException) {
        throw (JackUserException) cause;
      } else {
        throw e;
      }
    }

    new MetaImporter(config.get(MetaImporter.IMPORTED_META)).doImport(session);

    List<InputJackLibrary> inputJackLibraries = new ArrayList<InputJackLibrary>();
    for (InputLibrary library : inputFilter.getImportedLibrary()) {
      if (library instanceof InputJackLibrary) {
        addPackageLoaderForLibrary(session, config.get(IMPORT_POLICY),
            (InputJackLibrary) library);
        inputJackLibraries.add((InputJackLibrary) library);
        session.addImportedLibrary(library);
      }
    }
    JayceFileImporter jayceImporter = new JayceFileImporter(inputJackLibraries);

    for (InputLibrary library : inputFilter.getClasspath()) {
      if (library instanceof InputJackLibrary) {
        addPackageLoaderForLibrary(session, config.get(CLASSPATH_POLICY),
            (InputJackLibrary) library);
        session.addLibraryOnClasspath(library);
      }
    }

    Set<String> fileNamesToCompile = inputFilter.getFileNamesToCompile();
    if (!fileNamesToCompile.isEmpty()) {

      JackBatchCompiler jbc = new JackBatchCompiler(session);

      Event event = tracer.start(JackEventType.ECJ_COMPILATION);

      List<String> ecjExtraArguments = options.getEcjExtraArguments();
      List<String> ecjArguments = new ArrayList<String>(
          ecjExtraArguments.size() + fileNamesToCompile.size());
      ecjArguments.addAll(ecjExtraArguments);
      ecjArguments.addAll(fileNamesToCompile);

      try {
        if (!jbc.compile(ecjArguments.toArray(new String[ecjArguments.size()]))) {
          throw new FrontendCompilationException("Failed to compile");
        }
      } catch (TransportExceptionAroundEcjError e) {
        throw e.getCause();
      } catch (TransportJUEAroundEcjError e) {
        throw e.getCause();
      } finally {
        event.end();
      }
    }

    try {
      new ResourceImporter(config.get(ResourceImporter.IMPORTED_RESOURCES)).doImport(session);
    } catch (ResourceReadingException e) {
      session.getReporter().report(Severity.FATAL, e);
      throw new JackAbortException(e);
    }

    try {
      jayceImporter.doJayceImport(session);
      jayceImporter.doResourceImport(session);
    } catch (LibraryReadingException e) {
      session.getReporter().report(Severity.FATAL, e);
      throw new JackAbortException(e);
    }

    if (options.flags != null && (options.flags.shrink() || options.flags.obfuscate())) {
      Event eventIdMerger = tracer.start(JackEventType.METHOD_ID_MERGER);

      try {
        JClass javaLangObject = session.getPhantomLookup().getClass(CommonTypes.JAVA_LANG_OBJECT);
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
      methodIdDupRemover.accept(session.getTypesToEmit());
    }
  }

  private static void addPackageLoaderForLibrary(JSession session,
      ReflectFactory<JaycePackageLoader> factory, InputJackLibrary inputJackLibrary) {
    if (inputJackLibrary.containsFileType(FileType.JAYCE)) {
      JaycePackageLoader rootPLoader =
          factory.create(inputJackLibrary, session.getPhantomLookup());
      session.getTopLevelPackage().addLoader(rootPLoader);
    }
  }


  private static void appendMultiDexAndShrobStartPlan(@Nonnull PlanBuilder<JSession> planBuilder) {
    ProductionSet productions = planBuilder.getRequest().getTargetProductions();
    FeatureSet features = planBuilder.getRequest().getFeatures();
    boolean isShrinking = features.contains(Shrinking.class);
    boolean isMultiDexWithConstraints = features.contains(MultiDexLegacy.class);
    if (!(isShrinking || features.contains(Obfuscation.class)
        || isMultiDexWithConstraints || productions.contains(SeedFile.class))) {
      // nothing to do
      return;
    }

    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdapter.class);
      if (isShrinking || features.contains(Obfuscation.class)
          || productions.contains(SeedFile.class)) {
        typePlan.append(SeedFinder.class);
        if (productions.contains(SeedFile.class)) {
          planBuilder.append(SeedPrinter.class);
        }
      }

      if (isMultiDexWithConstraints) {
        typePlan.append(MultiDexAnnotationsFinder.class);
        typePlan.append(RuntimeAnnotationFinder.class);
        typePlan.append(AnnotatedFinder.class);
      }

      if (isMultiDexWithConstraints || isShrinking) {
        typePlan.append(ExtendingOrImplementingClassFinder.class);

      }
    }

    if (isShrinking) {
      {
        SubPlanBuilder<JDefinedClassOrInterface> typePlan =
            planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdapter.class);
        Request request = planBuilder.getRequest();
        if (isMultiDexWithConstraints &&
            request.getTargetProductions().contains(DexFileProduct.class)) {
          typePlan.append(ShrinkAndMainDexTracer.class);
        } else {
          typePlan.append(Keeper.class);
        }
      }
      {
        SubPlanBuilder<JDefinedClassOrInterface> typePlan =
            planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdapter.class);
        typePlan.append(TypeShrinker.class);
        {
          SubPlanBuilder<JMethod> methodPlan = typePlan.appendSubPlan(JMethodAdapter.class);
          methodPlan.append(MethodShrinker.class);
        }
        {
          SubPlanBuilder<JField> fieldPlan = typePlan.appendSubPlan(JFieldAdapter.class);
          fieldPlan.append(FieldShrinker.class);
        }
      }
    } else if (isMultiDexWithConstraints) {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdapter.class);
      typePlan.append(MainDexTracer.class);
    }

    if (isMultiDexWithConstraints) {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdapter.class);
      typePlan.append(MainDexCollector.class);
    }
  }

  private static void appendStringRefiners(@Nonnull PlanBuilder<JSession> planBuilder) {
    FeatureSet features = planBuilder.getRequest().getFeatures();
    boolean isShrinking = features.contains(Shrinking.class);
    if (isShrinking || features.contains(Obfuscation.class) || features.contains(Jarjar.class)) {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdapter.class);
      typePlan.append(TypeGenericSignatureSplitter.class);
      typePlan.append(TypeStringLiteralRefiner.class);
      typePlan.append(SimpleNameRefiner.class);
      {
        SubPlanBuilder<JMethod> methodPlan =
            typePlan.appendSubPlan(JMethodAdapter.class);
        methodPlan.append(MethodGenericSignatureSplitter.class);
        methodPlan.append(ReflectionStringLiteralRefiner.class);
        methodPlan.append(MethodStringLiteralRefiner.class);
      }
      {
        SubPlanBuilder<JField> fieldPlan =
            typePlan.appendSubPlan(JFieldAdapter.class);
        fieldPlan.append(FieldGenericSignatureSplitter.class);
        fieldPlan.append(FieldStringLiteralRefiner.class);
      }
    }
  }

  static void fillDexPlan(@Nonnull PlanBuilder<JSession> planBuilder) {
    FeatureSet features = planBuilder.getRequest().getFeatures();
    ProductionSet productions = planBuilder.getRequest().getTargetProductions();
    boolean hasSanityChecks = features.contains(SanityChecks.class);

    // TODO(jack-team): Remove this hack
    boolean preDexing = !getSession().getImportedLibraries().isEmpty();
    for (InputLibrary il : getSession().getImportedLibraries()) {
      if (!il.containsFileType(FileType.DEX)) {
        preDexing = false;
      }
    }
    if (features.contains(Jarjar.class) || features.contains(Obfuscation.class)
        || features.contains(Shrinking.class)) {
      for (InputLibrary il : getSession().getImportedLibraries()) {
        ((InputJackLibrary) il).fileTypes.remove(FileType.DEX);
      }
    }
    logger.log(Level.INFO, "Jack pre-dexing is " + (preDexing ? "enabled" : "disabled"));

    // Build the plan
    if (hasSanityChecks) {
      planBuilder.append(TypeDuplicateRemoverChecker.class);
    }

    if (features.contains(RemoveTypeDef.class)) {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdapter.class);
      typePlan.append(TypeDefRemover.class);
    }

    appendStringRefiners(planBuilder);

    if (features.contains(Jarjar.class)) {
      planBuilder.append(PackageRenamer.class);
    }

    if (hasSanityChecks) {
      planBuilder.append(AstChecker.class);
    }

    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan =
          planBuilder.appendSubPlan(ExcludeTypeFromLibAdapter.class);
      {
        if (features.contains(CompiledTypeStats.class)) {
          SubPlanBuilder<JMethod> methodPlan = typePlan.appendSubPlan(JMethodAdapter.class);
          methodPlan.append(MethodStats.class);
        }
      }
      {
        SubPlanBuilder<JField> fieldPlan = typePlan.appendSubPlan(JFieldAdapter.class);
        if (features.contains(CompiledTypeStats.class)) {
          fieldPlan.append(FieldStats.class);
        }
        fieldPlan.append(FieldInitializerRemover.class);
      }
    }

    appendMultiDexAndShrobStartPlan(planBuilder);

    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan =
          planBuilder.appendSubPlan(ExcludeTypeFromLibAdapter.class);
      typePlan.append(UsedEnumFieldCollector.class);
    }
    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan2 =
          planBuilder.appendSubPlan(ExcludeTypeFromLibAdapter.class);
      {
        if (features.contains(VisibilityBridge.class)) {
          typePlan2.append(VisibilityBridgeAdder.class);
        }
        SubPlanBuilder<JMethod> methodPlan =
            typePlan2.appendSubPlan(JMethodAdapter.class);
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
          planBuilder.appendSubPlan(ExcludeTypeFromLibAdapter.class);

      {
        {
          SubPlanBuilder<JField> fieldPlan =
              typePlan3.appendSubPlan(JFieldAdapter.class);
          fieldPlan.append(FieldInitializer.class);
        }
        {
          SubPlanBuilder<JMethod> methodPlan2 =
              typePlan3.appendSubPlan(JMethodAdapter.class);
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
          if (features.contains(AvoidSynthethicAccessors.class)) {
            methodPlan2.append(OptimizedInnerAccessorGenerator.class);
          } else {
            methodPlan2.append(InnerAccessorGenerator.class);
          }
        }
      }
    }

    if (features.contains(OptimizedSwitchEnumFeedbackFeature.class)) {
      // add one more traversal at compile-time to collect the usage for each enum,
      // figure out how many classes use enum in switch statement.
      // this step is enabled only when feedback-based optimization is enabled
      SubPlanBuilder<JDefinedClassOrInterface> typePlan = planBuilder.appendSubPlan(
          ExcludeTypeFromLibAdapter.class);
      SubPlanBuilder<JMethod> methodPlan = typePlan.appendSubPlan(JMethodAdapter.class);
      methodPlan.append(SwitchEnumUsageCollector.class);
    }

    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan = planBuilder.appendSubPlan(
          ExcludeTypeFromLibAdapter.class);
      SubPlanBuilder<JMethod> methodPlan = typePlan.appendSubPlan(JMethodAdapter.class);
      if (features.contains(OptimizedSwitchEnumFeedbackFeature.class)
          || features.contains(OptimizedSwitchEnumNonFeedbackFeature.class)) {
        methodPlan.append(OptimizedSwitchEnumSupport.class);
      } else {
        methodPlan.append(SwitchEnumSupport.class);
      }
    }

    if ((features.contains(OptimizedSwitchEnumFeedbackFeature.class)
        || features.contains(OptimizedSwitchEnumNonFeedbackFeature.class))
        && hasSanityChecks) {
      // check the validity of instrumentation if switch enum optimization and
      // hasSanityCheck are both set
      planBuilder.append(AstChecker.class);
    }

    planBuilder.append(InnerAccessorSchedulingSeparator.class);
    planBuilder.append(TryStatementSchedulingSeparator.class);
    planBuilder.append(EnumMappingSchedulingSeparator.class);

    if (features.contains(AvoidSynthethicAccessors.class)) {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan =
          planBuilder.appendSubPlan(ExcludeTypeFromLibAdapter.class);
      typePlan.append(ReferencedOuterFieldsExposer.class);

      planBuilder.append(OptimizedInnerAccessorSchedulingSeparator.class);

      SubPlanBuilder<JDefinedClassOrInterface> typePlan2 =
          planBuilder.appendSubPlan(ExcludeTypeFromLibAdapter.class);
      SubPlanBuilder<JMethod> methodPlan =
          typePlan2.appendSubPlan(JMethodAdapter.class);
      methodPlan.append(MethodCallDispatchAdjuster.class);
    }

    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan4 =
          planBuilder.appendSubPlan(ExcludeTypeFromLibAdapter.class);
      typePlan4.append(InnerAccessorAdder.class);
      typePlan4.append(UsedEnumFieldMarkerRemover.class);
      {
        SubPlanBuilder<JMethod> methodPlan =
            typePlan4.appendSubPlan(JMethodAdapter.class);
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
            typePlan4.appendSubPlan(JMethodAdapter.class);
        methodPlan3.append(FieldInitMethodCallRemover.class);
        if (features.contains(TailRecursionOptimization.class)) {
          methodPlan3.append(TailRecursionOptimizer.class);
        }
      }
      typePlan4.append(FieldInitMethodRemover.class);
    }

    if (features.contains(Obfuscation.class)) {
      appendObfuscationPlan(planBuilder, features);
    } else {
      planBuilder.append(NameFinalizer.class);
    }
    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan;
      // Jayce files must be copied into output library in incremental library mode or in non
      // incremental mode
      if (features.contains(GenerateLibraryFromIncrementalFolder.class)
          || !features.contains(Incremental.class)) {
        typePlan = planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdapter.class);
      } else {
        typePlan = planBuilder.appendSubPlan(ExcludeTypeFromLibWithBinaryAdapter.class);
      }
      if (productions.contains(JayceInLibraryProduct.class)) {
        typePlan.append(JayceInLibraryWriter.class);
      }
    }

    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan =
          planBuilder.appendSubPlan(ExcludeTypeFromLibAdapter.class);
      if (productions.contains(DependencyInLibraryProduct.class)) {
        typePlan.append(TypeDependenciesCollector.class);
        typePlan.append(FileDependenciesCollector.class);
      }
    }

    if (features.contains(SourceFileRenaming.class)) {
      planBuilder.append(SourceFileRenamer.class);
    }
    {
      // After this point {@link JDcoiExcludeJackFileAdapter} must not be used since
      // schedulables are not executed into the Java to Jayce plan.
      SubPlanBuilder<JDefinedClassOrInterface> typePlan4 =
          planBuilder.appendSubPlan(ExcludeTypeFromLibWithBinaryAdapter.class);
      {
        SubPlanBuilder<JMethod> methodPlan = typePlan4.appendSubPlan(JMethodAdapter.class);
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
        if (features.contains(CodeStats.class)) {
          methodPlan.append(BinaryOperationWithCst.class);
        }
        methodPlan.append(UselessCaseRemover.class);
        methodPlan.append(UselessSwitchesRemover.class);
        if (hasSanityChecks) {
          methodPlan.append(UselessCaseChecker.class);
        }
        methodPlan.append(FinallyRemover.class);
        methodPlan.append(ExceptionRuntimeValueAdder.class);
        methodPlan.append(DefinitionMarkerAdder.class);
        methodPlan.append(ThreeAddressCodeBuilder.class);
        methodPlan.append(UselessCastRemover.class);
        methodPlan.append(DefinitionMarkerRemover.class);
        methodPlan.append(TryCatchRemover.class);
        methodPlan.append(ExpressionStatementLegalizer.class);
        if (hasSanityChecks) {
          methodPlan.append(NumericConversionChecker.class);
        }
      }
    }

    if (productions.contains(DependencyInLibraryProduct.class)) {
      planBuilder.append(TypeDependenciesInLibraryWriter.class);
      planBuilder.append(FileDependenciesInLibraryWriter.class);
      planBuilder.append(LibraryDependenciesInLibraryWriter.class);
    }

    if (productions.contains(Mapping.class)) {
      planBuilder.append(MappingPrinter.class);
    }
    if (productions.contains(StructurePrinting.class)) {
      planBuilder.append(ShrinkStructurePrinter.class);
    }
    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan =
          planBuilder.appendSubPlan(ExcludeTypeFromLibWithBinaryAdapter.class);
      typePlan.append(ReflectAnnotationsAdder.class);
      {
        SubPlanBuilder<JMethod> methodPlan = typePlan.appendSubPlan(JMethodAdapter.class);
        methodPlan.append(DefaultValueAnnotationAdder.class);
      }
    }
    planBuilder.append(ClassAnnotationSchedulingSeparator.class);
    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan =
          planBuilder.appendSubPlan(ExcludeTypeFromLibWithBinaryAdapter.class);
      typePlan.append(ClassDefItemBuilder.class);
      typePlan.append(ClassAnnotationBuilder.class);
    }

    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan5 =
          planBuilder.appendSubPlan(ExcludeTypeFromLibWithBinaryAdapter.class);
      {
        SubPlanBuilder<JMethod> methodPlan4 =
            typePlan5.appendSubPlan(JMethodAdapter.class);
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
        if (features.contains(DropMethodBody.class)) {
          methodPlan4.append(MethodBodyRemover.class);
        }
        {
          SubPlanBuilder<JField> fieldPlan2 =
              typePlan5.appendSubPlan(JFieldAdapter.class);
          fieldPlan2.append(EncodedFieldBuilder.class);
          fieldPlan2.append(FieldAnnotationBuilder.class);
        }
      }
      if (hasSanityChecks) {
        typePlan5.append(TypeAstChecker.class);
      }
    }

    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan;
      // Jayce files must be copied into output library in incremental library mode or in non
      // incremental mode
      if (features.contains(GenerateLibraryFromIncrementalFolder.class)
          || !features.contains(Incremental.class)) {
        typePlan = planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdapter.class);
      } else {
        typePlan = planBuilder.appendSubPlan(ExcludeTypeFromLibWithBinaryAdapter.class);
      }
      if (productions.contains(DexInLibraryProduct.class)) {
        typePlan.append(DexInLibraryWriter.class);
      }
    }

    if (hasSanityChecks) {
      planBuilder.append(AstChecker.class);
    }
  }

  private static void appendObfuscationPlan(@Nonnull PlanBuilder<JSession> planBuilder,
      @Nonnull FeatureSet features) {
    {
      SubPlanBuilder<JPackage> packagePlan = planBuilder.appendSubPlan(JPackageAdapter.class);
      packagePlan.append(NameKeeper.class);
    }
    planBuilder.append(ResourceRefiner.class);
    if (features.contains(AdaptResourceFileContent.class)) {
      planBuilder.append(ResourceContentRefiner.class);
    }
    planBuilder.append(Renamer.class);
    if (features.contains(RemoveSourceFile.class)) {
      planBuilder.append(SourceFileRemover.class);
    }
    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdapter.class);
      typePlan.append(TypeAnnotationRemover.class);
      if (features.contains(RemoveEnclosingMethod.class)) {
        typePlan.append(TypeEnclosingMethodRemover.class);
      }
      if (features.contains(RemoveEnclosingType.class)) {
        typePlan.append(TypeEnclosingTypeRemover.class);
      }
      if (features.contains(RemoveGenericSignature.class)) {
        typePlan.append(TypeGenericSignatureRemover.class);
      }
      if (features.contains(RemoveLineNumber.class)) {
        typePlan.append(LineNumberRemover.class);
      }
      {
        SubPlanBuilder<JField> fieldPlan = typePlan.appendSubPlan(JFieldAdapter.class);
        fieldPlan.append(FieldAnnotationRemover.class);
        if (features.contains(RemoveGenericSignature.class)) {
          fieldPlan.append(FieldGenericSignatureRemover.class);
        }
      }
      {
        SubPlanBuilder<JMethod> methodPlan = typePlan.appendSubPlan(JMethodAdapter.class);
        methodPlan.append(MethodAnnotationRemover.class);
        methodPlan.append(ParameterAnnotationRemover.class);
        if (features.contains(RemoveGenericSignature.class)) {
          methodPlan.append(MethodGenericSignatureRemover.class);
        }
        if (features.contains(RemoveLocalVariableGenericSignature.class)) {
          methodPlan.append(LocalVariableGenericSignatureRemover.class);
        }
        if (features.contains(RemoveAnnotationDefaultValue.class)) {
          methodPlan.append(AnnotationDefaultValueRemover.class);
        }
        if (features.contains(RemoveThrownException.class)) {
          methodPlan.append(ThrownExceptionRemover.class);
        }
        if (features.contains(RemoveParameterName.class)) {
          methodPlan.append(ParameterNameRemover.class);
        }
      }
    }
  }

  @CheckForNull
  private static Version version = null;

  @Nonnull
  public static Version getVersion() {
    if (version == null) {
      try {
        version = new Version("jack", Jack.class.getClassLoader());
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Failed to open Jack version file", e);
        throw new AssertionError();
      }
    }

    assert version != null;
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
