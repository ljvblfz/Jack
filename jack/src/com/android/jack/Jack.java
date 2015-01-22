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
import com.android.jack.incremental.Incremental;
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
import com.android.jack.library.JackLibraryFactory;
import com.android.jack.library.LibraryIOException;
import com.android.jack.library.LibraryReadingException;
import com.android.jack.library.OutputLibrary;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.lookup.JPhantomLookup;
import com.android.jack.meta.LibraryMetaWriter;
import com.android.jack.meta.MetaImporter;
import com.android.jack.meta.MetaReadingException;
import com.android.jack.optimizations.ConstantRefinerAndVariableRemover;
import com.android.jack.optimizations.DefUsesChainsSimplifier;
import com.android.jack.optimizations.ExpressionSimplifier;
import com.android.jack.optimizations.IfWithConstantSimplifier;
import com.android.jack.optimizations.NotSimplifier;
import com.android.jack.optimizations.UnusedDefinitionRemover;
import com.android.jack.optimizations.UseDefsChainsSimplifier;
import com.android.jack.preprocessor.PreProcessor;
import com.android.jack.preprocessor.PreProcessorApplier;
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
import com.android.jack.scheduling.feature.DxLegacy;
import com.android.jack.scheduling.feature.Resources;
import com.android.jack.scheduling.feature.SourceVersion7;
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
import com.android.jack.shrob.obfuscation.remover.FieldKeepNameMarkerRemover;
import com.android.jack.shrob.obfuscation.remover.MethodKeepNameMarkerRemover;
import com.android.jack.shrob.obfuscation.remover.TypeKeepNameMarkerRemover;
import com.android.jack.shrob.obfuscation.remover.TypeOriginalNameMarkerRemover;
import com.android.jack.shrob.obfuscation.resource.AdaptResourceFileContent;
import com.android.jack.shrob.obfuscation.resource.ResourceContentRefiner;
import com.android.jack.shrob.obfuscation.resource.ResourceRefiner;
import com.android.jack.shrob.proguard.GrammarActions;
import com.android.jack.shrob.seed.SeedFile;
import com.android.jack.shrob.seed.SeedFinder;
import com.android.jack.shrob.seed.SeedPrinter;
import com.android.jack.shrob.seed.remover.FieldSeedMarkerRemover;
import com.android.jack.shrob.seed.remover.MethodSeedMarkerRemover;
import com.android.jack.shrob.seed.remover.TypeSeedMarkerRemover;
import com.android.jack.shrob.shrink.FieldShrinker;
import com.android.jack.shrob.shrink.Keeper;
import com.android.jack.shrob.shrink.MethodShrinker;
import com.android.jack.shrob.shrink.ShrinkAndMainDexTracer;
import com.android.jack.shrob.shrink.ShrinkStructurePrinter;
import com.android.jack.shrob.shrink.Shrinking;
import com.android.jack.shrob.shrink.StructurePrinting;
import com.android.jack.shrob.shrink.TypeShrinker;
import com.android.jack.shrob.shrink.remover.FieldKeepMarkerRemover;
import com.android.jack.shrob.shrink.remover.MethodKeepMarkerRemover;
import com.android.jack.shrob.shrink.remover.TypeShrinkMarkerRemover;
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
import com.android.sched.util.config.Config;
import com.android.sched.util.config.ConfigPrinterFactory;
import com.android.sched.util.config.ConfigurationException;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ReflectFactory;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.config.id.ObjectId;
import com.android.sched.util.config.id.ReflectFactoryPropertyId;
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.InputZipFile;
import com.android.sched.util.log.Event;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.vfs.Container;
import com.android.sched.vfs.DirectVFS;
import com.android.sched.vfs.InputVFS;
import com.android.sched.vfs.InputZipVFS;

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
          .addDefaultValue("structure");

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

  /**
   * Runs the jack compiler on source files and generates a dex file.
   *
   * @param options options for the compiler.
   * @throws ConfigurationException thrown from the configuration framework.
   * @throws IllegalOptionsException thrown when an {@code Options} is not valid.
   * @throws NothingToDoException thrown when there is nothing to compile.
   * @throws ProcessException thrown during schedulable execution
   */
  public static void run(@Nonnull Options options)
      throws IllegalOptionsException,
      NothingToDoException,
      ConfigurationException,
      JackUserException, ProcessException {
    boolean assertEnable = false;
    // assertEnable = true if assertion is already enable
    assert true == (assertEnable = true);

    if (options.proguardFlagsFiles != null && !options.proguardFlagsFiles.isEmpty()) {
      if (options.flags == null) {
        options.flags = new Flags();
      }
      for (File proguardFlagsFile : options.proguardFlagsFiles) {
        try {
          GrammarActions.parse(proguardFlagsFile.getPath(), ".", options.flags);
        } catch (RecognitionException e) {
          throw new IllegalOptionsException(
              "Error while parsing " + proguardFlagsFile.getPath() + ":" + e.line, e);
        }
      }

      if (options.flags.optimize()) {
        logger.log(Level.WARNING,
            "Flag '-dontoptimize' not found: Proguard optimizations are not supported");
      }
      if (options.flags.preverify()) {
        logger.log(Level.WARNING,
            "Flag '-dontpreverify' not found: Proguard preverification is not supported");
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

        JSession session = getSession();

        buildSession(options, hooks);

        if (config.get(Options.GENERATE_JACK_LIBRARY).booleanValue()) {
          session.setJackOutputLibrary(session.getInputFilter().getOutputJackLibrary());
        }

        Request request = createInitialRequest();
        request.addFeature(PreProcessor.class);

        request.addFeature(Resources.class);

        JavaVersion sourceVersion = config.get(Options.JAVA_SOURCE_VERSION);
        if (sourceVersion.compareTo(JavaVersion.JAVA_7) >= 0) {
          request.addFeature(SourceVersion7.class);
        }

        if (config.get(Options.ENABLE_COMPILED_FILES_STATISTICS).booleanValue()) {
          request.addFeature(CompiledTypeStats.class);
          request.addFeature(CodeStats.class);
        }

        if (options.hasSanityChecks()) {
          request.addFeature(SanityChecks.class);
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
          if (options.flags.printSeeds()) {
            request.addProduction(SeedFile.class);
          }
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
          if (!options.flags.getKeepParameterNames()) {
            request.addFeature(RemoveParameterName.class);
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

        request.addInitialTagsOrMarkers(getJavaSourceInitialTagSet());
        request.addInitialTagsOrMarkers(getJackFormatInitialTagSet());

        if (config.get(Options.GENERATE_DEX_IN_LIBRARY).booleanValue()) {
          request.addProduction(DexInLibraryProduct.class);
        }

        if (options.out != null || options.outZip != null) {
          request.addProduction(DexFileProduct.class);
          session.addGeneratedFileType(FileType.DEX);
        }

        if (config.get(Options.GENERATE_JAYCE_IN_LIBRARY).booleanValue()) {
            request.addProduction(JayceInLibraryProduct.class);
        }

        if (config.get(Options.GENERATE_DEPENDENCIES_IN_LIBRARY).booleanValue()) {
          request.addProduction(DependencyInLibraryProduct.class);
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

        fillDexPlan(options, planBuilder);
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
              || targetProduction.contains(DexFileProduct.class)
              || (plan.computeFinalTagsOrMarkers(
                  request.getInitialTags()).contains(JackFormatIr.class)
                  && !targetProduction.contains(DexInLibraryProduct.class))
              || (targetProduction.contains(DexInLibraryProduct.class)
                  && targetProduction.contains(JayceInLibraryProduct.class));
        }

        PlanPrinterFactory.getPlanPrinter().printPlan(plan);
        try {
          plan.getScheduleInstance().process(session);
        } finally {
          try {
            OutputLibrary jackOutputLibrary = session.getJackOutputLibrary();
            if (jackOutputLibrary != null) {
              jackOutputLibrary.close();
            }
            //TODO(jack-team): auto-close
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
        event.end();
      }
    } finally {

      hooks.runHooks();
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
  static JSession buildSession(@Nonnull Options options, @Nonnull RunnableHooks hooks)
      throws JackUserException {
    Tracer tracer = TracerFactory.getTracer();

    List<String> ecjArguments = options.ecjArguments;

    JSession session =  getSession();
    try {
      session.setInputFilter(ThreadConfig.get(Options.INPUT_FILTER).create(options, hooks));
    } catch (RuntimeException e) {
      Throwable cause = e.getCause();
      if (cause instanceof JackAbortException) {
        throw (JackAbortException) cause;
      } else {
        throw e;
      }
    }

    try {
      getMetaImporter(options.metaImport, hooks).doImport(session);
    } catch (MetaReadingException e) {
      session.getReporter().report(Severity.FATAL, e);
      throw new JackAbortException(e);
    }

    List<InputJackLibrary> inputJackLibraries = new ArrayList<InputJackLibrary>();
    for (InputLibrary library : session.getInputFilter().getImportedLibrary()) {
      if (library instanceof InputJackLibrary) {
        addPackageLoaderForLibrary(session, ThreadConfig.get(IMPORT_POLICY),
            (InputJackLibrary) library);
        inputJackLibraries.add((InputJackLibrary) library);
        session.addImportedLibrary(library);
      }
    }
    JayceFileImporter jayceImporter = new JayceFileImporter(inputJackLibraries);

    for (InputLibrary library : session.getInputFilter().getClasspath()) {
      if (library instanceof InputJackLibrary) {
        addPackageLoaderForLibrary(session, ThreadConfig.get(CLASSPATH_POLICY),
            (InputJackLibrary) library);
        session.addLibraryOnClasspath(library);
      }
    }

    if (ecjArguments != null) {

      JackBatchCompiler jbc = new JackBatchCompiler(session);

      Event event = tracer.start(JackEventType.ECJ_COMPILATION);

      try {
        if (!jbc.compile(ecjArguments.toArray(new String[ecjArguments.size()]))) {
          throw new FrontendCompilationException("Failed to compile.");
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
      getResourceImporter(options.resImport, hooks).doImport(session);
    } catch (ResourceReadingException e) {
      session.getReporter().report(Severity.FATAL, e);
      throw new JackAbortException(e);
    }

    jayceImporter.doImport(session);

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

    return session;
  }

  @Nonnull
  private static ResourceImporter getResourceImporter(@Nonnull List<File> importedResources,
      @Nonnull RunnableHooks hooks) throws ResourceReadingException {
    List<InputVFS> resourceVDirs = new ArrayList<InputVFS>();
    for (File resourceDir : importedResources) {
      try {
        // Let's assume all of these are directories for now
        InputVFS dir = new DirectVFS(new Directory(resourceDir.getPath(), hooks,
            Existence.MUST_EXIST, Permission.READ, ChangePermission.NOCHANGE));
        resourceVDirs.add(dir);
      } catch (IOException ioException) {
        throw new ResourceReadingException(ioException);
      }
    }
    return new ResourceImporter(resourceVDirs);
  }

  @Nonnull
  private static MetaImporter getMetaImporter(@Nonnull List<File> importedMetas,
      @Nonnull RunnableHooks hooks) throws MetaReadingException {
    List<InputVFS> metaVDirs = new ArrayList<InputVFS>();
    for (File metaDir : importedMetas) {
      try {
        // Let's assume all of these are directories for now
        InputVFS dir = new DirectVFS(new Directory(metaDir.getPath(), hooks,
            Existence.MUST_EXIST, Permission.READ, ChangePermission.NOCHANGE));
        metaVDirs.add(dir);
      } catch (IOException ioException) {
        throw new MetaReadingException(ioException);
      }
    }
    return new MetaImporter(metaVDirs);
  }

  @Nonnull
  private static JayceFileImporter getJayceFileImporter(@Nonnull List<File> jayceImport,
      @Nonnull RunnableHooks hooks, @Nonnull JSession session) throws LibraryReadingException {
    List<InputJackLibrary> inputJackLibraries = new ArrayList<InputJackLibrary>(jayceImport.size());
    ReflectFactory<JaycePackageLoader> factory = ThreadConfig.get(IMPORT_POLICY);
    for (final File jackFile : jayceImport) {
      try {
        InputVFS vDir = wrapAsVDir(jackFile, hooks);
        InputJackLibrary inputJackLibrary = JackLibraryFactory.getInputLibrary(vDir);
        inputJackLibraries.add(inputJackLibrary);
        addPackageLoaderForLibrary(session, factory, inputJackLibrary);
        session.addImportedLibrary(inputJackLibrary);
      } catch (IOException ioException) {
        throw new LibraryReadingException(ioException);
      } catch (LibraryException libException) {
        throw new LibraryReadingException(libException);
      }
    }

    return new JayceFileImporter(inputJackLibraries);
  }

  private static void addPackageLoaderForLibrary(JSession session,
      ReflectFactory<JaycePackageLoader> factory, InputJackLibrary inputJackLibrary) {
    if (inputJackLibrary.containsFileType(FileType.JAYCE)) {
      JaycePackageLoader rootPLoader =
          factory.create(inputJackLibrary, session.getPhantomLookup());
      session.getTopLevelPackage().addLoader(rootPLoader);
    }
  }

  @Nonnull
  public static InputVFS wrapAsVDir(@Nonnull final File dirOrZip,
      @CheckForNull RunnableHooks hooks)
      throws IOException {
    final InputVFS vfs;
    if (dirOrZip.isDirectory()) {
      vfs = new DirectVFS(new Directory(dirOrZip.getPath(), hooks, Existence.MUST_EXIST,
          Permission.READ, ChangePermission.NOCHANGE));
    } else { // zip
      vfs = new InputZipVFS(new InputZipFile(dirOrZip.getPath(), hooks, Existence.MUST_EXIST,
          ChangePermission.NOCHANGE));
    }

    if (hooks != null) {
      hooks.addHook(new Runnable() {
        @Override
        public void run() {
          try {
            vfs.close();
          } catch (IOException e) {
            logger.log(Level.FINE, "Failed to close vfs for '" + dirOrZip + "'.", e);
          }
        }
      });
    }

    return vfs;
  }

  @SuppressWarnings("unused")
  private static void fillJayceToJaycePlan(@Nonnull PlanBuilder<JSession> planBuilder) {
    // Add here transformations we want to apply before writing jayce file
    FeatureSet features = planBuilder.getRequest().getFeatures();
    ProductionSet productions = planBuilder.getRequest().getTargetProductions();

    if (features.contains(SanityChecks.class)) {
      planBuilder.append(TypeDuplicateRemoverChecker.class);
    }

    appendStringRefiners(planBuilder);

    // JarJar
    if (features.contains(Jarjar.class)) {
      planBuilder.append(PackageRenamer.class);
    }

    // Shrob
    appendMultiDexAndShrobStartPlan(planBuilder);
    if (features.contains(Obfuscation.class)) {
      appendObfuscationPlan(planBuilder, features);
    } else {
      planBuilder.append(NameFinalizer.class);
    }
    if (features.contains(SourceFileRenaming.class)) {
      planBuilder.append(SourceFileRenamer.class);
    }
    if (productions.contains(Mapping.class)) {
      planBuilder.append(MappingPrinter.class);
    }
    if (productions.contains(StructurePrinting.class)) {
      planBuilder.append(ShrinkStructurePrinter.class);
    }
    if (features.contains(Shrinking.class) || features.contains(Obfuscation.class)) {
      appendShrobMarkerRemoverPlan(planBuilder);
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

  static void fillDexPlan(@Nonnull Options options, @Nonnull PlanBuilder<JSession> planBuilder) {
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
        SubPlanBuilder<JMethod> methodPlan = typePlan.appendSubPlan(JMethodAdapter.class);
        if (features.contains(CompiledTypeStats.class)) {
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
        if (features.contains(DxLegacy.class)) {
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
          methodPlan2.append(InnerAccessorGenerator.class);
        }
      }
    }

    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan =
          planBuilder.appendSubPlan(ExcludeTypeFromLibAdapter.class);
      SubPlanBuilder<JMethod> methodPlan = typePlan.appendSubPlan(JMethodAdapter.class);
      methodPlan.append(SwitchEnumSupport.class);
    }

    planBuilder.append(InnerAccessorSchedulingSeparator.class);
    planBuilder.append(TryStatementSchedulingSeparator.class);
    planBuilder.append(EnumMappingSchedulingSeparator.class);

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
      if (features.contains(Incremental.class)) {
        typePlan = planBuilder.appendSubPlan(ExcludeTypeFromLibWithBinaryAdapter.class);
      } else {
        typePlan = planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdapter.class);
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
        methodPlan.append(UselessCastRemover.class);
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
        if (!options.keepMethodBody) {
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
      if (features.contains(Incremental.class)) {
        typePlan = planBuilder.appendSubPlan(ExcludeTypeFromLibWithBinaryAdapter.class);
      } else {
        typePlan = planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdapter.class);
      }
      if (productions.contains(DexInLibraryProduct.class)) {
        typePlan.append(DexInLibraryWriter.class);
      }
    }

    if (hasSanityChecks) {
      planBuilder.append(AstChecker.class);
    }
  }

  @SuppressWarnings("unused")
  private static void fillJavaToJaycePlan(@Nonnull PlanBuilder<JSession> planBuilder) {
    Request request = planBuilder.getRequest();
    FeatureSet features = request.getFeatures();
    ProductionSet productions = request.getTargetProductions();
    boolean hasSanityChecks = features.contains(SanityChecks.class);

    // Build the plan
    if (hasSanityChecks) {
      planBuilder.append(TypeDuplicateRemoverChecker.class);
    }
    appendStringRefiners(planBuilder);

    if (features.contains(Jarjar.class)) {
      planBuilder.append(PackageRenamer.class);
    }

    appendMultiDexAndShrobStartPlan(planBuilder);

    if (hasSanityChecks) {
      planBuilder.append(AstChecker.class);
    }
    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan7 =
          planBuilder.appendSubPlan(ExcludeTypeFromLibAdapter.class);
      typePlan7.append(UsedEnumFieldCollector.class);

      {
        SubPlanBuilder<JMethod> methodPlan =
            typePlan7.appendSubPlan(JMethodAdapter.class);
        if (features.contains(CompiledTypeStats.class)) {
          methodPlan.append(MethodStats.class);
        }
      }
      {
        SubPlanBuilder<JField> fieldPlan =
            typePlan7.appendSubPlan(JFieldAdapter.class);
        if (features.contains(CompiledTypeStats.class)) {
          fieldPlan.append(FieldStats.class);
        }
        fieldPlan.append(FieldInitializerRemover.class);
      }
    }
    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan2 =
          planBuilder.appendSubPlan(ExcludeTypeFromLibAdapter.class);
      if (features.contains(DxLegacy.class)) {
        typePlan2.append(VisibilityBridgeAdder.class);
      }
      {
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

          methodPlan2.append(IncDecRemover.class);
          methodPlan2.append(CompoundAssignmentRemover.class);
          methodPlan2.append(ConcatRemover.class);
          methodPlan2.append(InnerAccessorGenerator.class);
        }
      }
    }

    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan =
          planBuilder.appendSubPlan(ExcludeTypeFromLibAdapter.class);
      SubPlanBuilder<JMethod> methodPlan = typePlan.appendSubPlan(JMethodAdapter.class);
      methodPlan.append(SwitchEnumSupport.class);
    }

    planBuilder.append(InnerAccessorSchedulingSeparator.class);
    planBuilder.append(EnumMappingSchedulingSeparator.class);

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
        SubPlanBuilder<JMethod> methodPlan =
            typePlan4.appendSubPlan(JMethodAdapter.class);
        methodPlan.append(FieldInitMethodCallRemover.class);
      }
      typePlan4.append(FieldInitMethodRemover.class);
    }
    planBuilder.append(TryStatementSchedulingSeparator.class);
    if (features.contains(SourceFileRenaming.class)) {
      planBuilder.append(SourceFileRenamer.class);
    }

    if (hasSanityChecks) {
      planBuilder.append(TypeDuplicateRemoverChecker.class);
    }
    if (features.contains(Obfuscation.class)) {
      appendObfuscationPlan(planBuilder, features);
    } else {
      planBuilder.append(NameFinalizer.class);
    }
    if (productions.contains(Mapping.class)) {
      planBuilder.append(MappingPrinter.class);
    }
    if (productions.contains(StructurePrinting.class)) {
      planBuilder.append(ShrinkStructurePrinter.class);
    }
    if (features.contains(Shrinking.class) || features.contains(Obfuscation.class)) {
      appendShrobMarkerRemoverPlan(planBuilder);
    }
  }

  private static void appendShrobMarkerRemoverPlan(@Nonnull PlanBuilder<JSession> planBuilder) {
    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdapter.class);
      typePlan.append(TypeShrinkMarkerRemover.class);
      typePlan.append(TypeKeepNameMarkerRemover.class);
      typePlan.append(TypeOriginalNameMarkerRemover.class);
      typePlan.append(TypeSeedMarkerRemover.class);
      {
        SubPlanBuilder<JMethod> methodPlan = typePlan.appendSubPlan(JMethodAdapter.class);
        methodPlan.append(MethodKeepMarkerRemover.class);
        methodPlan.append(MethodKeepNameMarkerRemover.class);
        methodPlan.append(MethodSeedMarkerRemover.class);
      }
      {
        SubPlanBuilder<JField> fieldPlan = typePlan.appendSubPlan(JFieldAdapter.class);
        fieldPlan.append(FieldKeepMarkerRemover.class);
        fieldPlan.append(FieldKeepNameMarkerRemover.class);
        fieldPlan.append(FieldSeedMarkerRemover.class);
      }
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

  @SuppressWarnings("unused")
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

    appendStringRefiners(planBuilder);

    if (features.contains(Jarjar.class)) {
      planBuilder.append(PackageRenamer.class);
    }

    appendMultiDexAndShrobStartPlan(planBuilder);

    if (hasSanityChecks) {
      planBuilder.append(AstChecker.class);
    }
    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan3 =
          planBuilder.appendSubPlan(ExcludeTypeFromLibWithBinaryAdapter.class);
      {
        {
          SubPlanBuilder<JMethod> methodPlan2 =
              typePlan3.appendSubPlan(JMethodAdapter.class);
          methodPlan2.append(UselessSwitchesRemover.class);
          methodPlan2.append(UselessIfRemover.class);
          if (hasSanityChecks) {
            methodPlan2.append(UselessIfChecker.class);
          }
        }
      }
    }

    if (features.contains(Obfuscation.class)) {
      appendObfuscationPlan(planBuilder, features);
    } else {
      planBuilder.append(NameFinalizer.class);
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
    if (features.contains(SourceFileRenaming.class)) {
      planBuilder.append(SourceFileRenamer.class);
    }
    {
      SubPlanBuilder<JDefinedClassOrInterface> typePlan4 =
          planBuilder.appendSubPlan(ExcludeTypeFromLibWithBinaryAdapter.class);
      typePlan4.append(ClassDefItemBuilder.class);
      typePlan4.append(ClassAnnotationBuilder.class);
      {
        SubPlanBuilder<JMethod> methodPlan3 = typePlan4.appendSubPlan(JMethodAdapter.class);
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
        if (features.contains(CodeStats.class)) {
          methodPlan3.append(BinaryOperationWithCst.class);
        }
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
        if (hasSanityChecks) {
          methodPlan3.append(NumericConversionChecker.class);
        }
      }
    }
    if (productions.contains(Mapping.class)) {
      planBuilder.append(MappingPrinter.class);
    }
    if (productions.contains(StructurePrinting.class)) {
      planBuilder.append(ShrinkStructurePrinter.class);
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
        if (!options.keepMethodBody) {
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
      SubPlanBuilder<JDefinedClassOrInterface> typePlan =
          planBuilder.appendSubPlan(JDefinedClassOrInterfaceAdapter.class);
      if (productions.contains(DexInLibraryProduct.class)) {
        typePlan.append(DexInLibraryWriter.class);
      }
    }

    if (hasSanityChecks) {
      planBuilder.append(AstChecker.class);
    }
  }

  @Nonnull
  private static final String PROPERTIES_FILE = "jack.properties";

  @Nonnull
  public static String getVersionString() {
    String version = "Unknown (problem with " + PROPERTIES_FILE + " resource file)";

    InputStream is = Jack.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
    if (is != null) {
      Properties prop = new Properties();
      try {
        prop.load(is);
        String rawVersion = prop.getProperty("jack.version");
        if (rawVersion != null) {
          version = rawVersion;

          String codeName = prop.getProperty("jack.version.codename");
          if (codeName != null) {
            version += " \'" + codeName + '\'';
          }

          String bid = prop.getProperty("jack.version.buildid", "engineering");
          String sha = prop.getProperty("jack.version.sha");
          if (sha != null) {
            version += " (" + bid + ' ' + sha + ')';
          } else {
            version += " (" + bid + ')';
          }
        }
      } catch (IOException e) {
        // Return default version
      }
    }

    return version;
  }

  @CheckForNull
  public static String getBuildId() {
    String version = null;

    InputStream is = Jack.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
    if (is != null) {
      Properties prop = new Properties();
      try {
        prop.load(is);
        version = prop.getProperty("jack.version.buildid");
      } catch (IOException e) {
        logger.log(Level.WARNING, "Failed to read Jack properties from " + PROPERTIES_FILE, e);
      }
    } else {
      logger.log(Level.WARNING, "Failed to open Jack properties file " + PROPERTIES_FILE);
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
