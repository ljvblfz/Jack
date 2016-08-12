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

import com.google.common.base.Joiner;

import com.android.jack.backend.dex.DexFileWriter;
import com.android.jack.backend.dex.MultiDexLegacy;
import com.android.jack.backend.dex.compatibility.AndroidCompatibilityChecker;
import com.android.jack.backend.dex.rop.CodeItemBuilder;
import com.android.jack.config.id.Arzon;
import com.android.jack.config.id.Carnac;
import com.android.jack.config.id.JavaVersionPropertyId;
import com.android.jack.incremental.InputFilter;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.library.DumpInLibrary;
import com.android.jack.library.InputLibrary;
import com.android.jack.library.LibraryPathPropertyId;
import com.android.jack.library.PrebuiltCompatibility;
import com.android.jack.meta.MetaImporter;
import com.android.jack.plugin.JackPluginJarCodec;
import com.android.jack.plugin.NotJackPluginException;
import com.android.jack.plugin.PluginManager;
import com.android.jack.plugin.PluginNotFoundException;
import com.android.jack.reporting.Reportable;
import com.android.jack.reporting.Reportable.ProblemLevel;
import com.android.jack.reporting.Reporter;
import com.android.jack.reporting.Reporter.Severity;
import com.android.jack.resource.ResourceImporter;
import com.android.jack.shrob.obfuscation.MappingPrinter;
import com.android.jack.shrob.obfuscation.NameProviderFactory;
import com.android.jack.shrob.obfuscation.Renamer;
import com.android.jack.shrob.obfuscation.SourceFileRenamer;
import com.android.jack.shrob.obfuscation.annotation.AnnotationRemover;
import com.android.jack.shrob.obfuscation.annotation.ParameterAnnotationRemover;
import com.android.jack.shrob.seed.SeedPrinter;
import com.android.jack.shrob.spec.Flags;
import com.android.jack.transformations.lambda.LambdaGroupingScope;
import com.android.jack.transformations.renamepackage.PackageRenamer;
import com.android.jack.util.AndroidApiLevel;
import com.android.jack.util.AndroidApiLevelCodec;
import com.android.jack.util.ClassNameCodec;
import com.android.jack.util.args4j.JackEnumOptionHandler;
import com.android.jack.util.filter.Filter;
import com.android.sched.reflections.ReflectionFactory;
import com.android.sched.util.RunnableHooks;
import com.android.sched.util.SubReleaseKind;
import com.android.sched.util.codec.CaseInsensitiveDirectFSCodec;
import com.android.sched.util.codec.CodecContext;
import com.android.sched.util.codec.DirectDirOutputVFSCodec;
import com.android.sched.util.codec.DirectoryCodec;
import com.android.sched.util.codec.EnumName;
import com.android.sched.util.codec.InputFileOrDirectoryCodec;
import com.android.sched.util.codec.ListCodec;
import com.android.sched.util.codec.PairCodec;
import com.android.sched.util.codec.PairCodec.Pair;
import com.android.sched.util.codec.PairListToMapCodecConverter;
import com.android.sched.util.codec.ParsingException;
import com.android.sched.util.codec.ReaderFileOrDirectoryCodec;
import com.android.sched.util.codec.StringValueCodec;
import com.android.sched.util.codec.VariableName;
import com.android.sched.util.codec.ZipFSCodec;
import com.android.sched.util.codec.ZipOutputVFSCodec;
import com.android.sched.util.config.Config;
import com.android.sched.util.config.ConfigurationException;
import com.android.sched.util.config.GatherConfigBuilder;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.PropertyIdException;
import com.android.sched.util.config.category.Private;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.config.id.EnumPropertyId;
import com.android.sched.util.config.id.ImplementationPropertyId;
import com.android.sched.util.config.id.ListPropertyId;
import com.android.sched.util.config.id.MessageDigestPropertyId;
import com.android.sched.util.config.id.ObjectId;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.config.id.ReflectFactoryPropertyId;
import com.android.sched.util.file.CannotChangePermissionException;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileAlreadyExistsException;
import com.android.sched.util.file.FileOrDirectory;
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.FileUtils;
import com.android.sched.util.file.Files;
import com.android.sched.util.file.InputJarFile;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.OutputZipFile.Compression;
import com.android.sched.util.file.WriterFile;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.location.NoLocation;
import com.android.sched.util.location.StringLocation;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.tracer.StatsTracerFtl;
import com.android.sched.vfs.Container;
import com.android.sched.vfs.OutputVFSPropertyId;
import com.android.sched.vfs.VFSPropertyId;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.ExplicitBooleanOptionHandler;
import org.kohsuke.args4j.spi.MapOptionHandler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Jack command line options Bean
 */
@HasKeyId
public class Options {

  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  private static class DeprecatedVerbosity implements Reportable {
    @Nonnull
    private final VerbosityLevel verbosity;

    private DeprecatedVerbosity(@Nonnull VerbosityLevel verbosity) {
      this.verbosity = verbosity;
    }

    @Override
    @Nonnull
    public String getMessage() {
      return "Verbosity level '" + verbosity.name().toLowerCase() + "' is deprecated";
    }

    @Override
    @Nonnull
    public ProblemLevel getDefaultProblemLevel() {
      return ProblemLevel.WARNING;
    }

  }

  /**
   * Assertion policies
   */
  @VariableName("policy")
  public enum AssertionPolicy {
    @EnumName(name = "always", description = "always check assert statements")
    ALWAYS,
    @EnumName(name = "never", description = "remove assert statements")
    NEVER,
    @EnumName(name = "runtime", description = "check according to runtime configuration")
    RUNTIME;
  }

  @Nonnull
  public static final EnumPropertyId<AssertionPolicy> ASSERTION_POLICY =
      EnumPropertyId.create(
              "jack.assert.policy",
              "Assert statement policy",
              AssertionPolicy.class)
          .addDefaultValue(AssertionPolicy.RUNTIME)
          .ignoreCase()
          .addCategory(DumpInLibrary.class)
          .addCategory(PrebuiltCompatibility.class)
          .addCategory(Carnac.class);

  @Nonnull
  public static final BooleanPropertyId INCREMENTAL_MODE = BooleanPropertyId
      .create("jack.incremental", "Enable incremental mode")
      .addDefaultValue(Boolean.FALSE).addCategory(DumpInLibrary.class);

  @Nonnull
  public static final ReflectFactoryPropertyId<InputFilter> INPUT_FILTER = ReflectFactoryPropertyId
      .create("jack.input.filter", "Inputs filter", InputFilter.class)
      .addDefaultValue("no-filter").addArgType(Options.class);

  @Nonnull
  public static final JavaVersionPropertyId JAVA_SOURCE_VERSION =
      JavaVersionPropertyId.create("jack.java.source.version", "Java source version")
          .addDefaultValue("1.7").addCategory(Arzon.class).addCategory(DumpInLibrary.class);

  @Nonnull
  public static final BooleanPropertyId LAMBDA_TO_ANONYMOUS_CONVERTER = BooleanPropertyId
      .create("jack.lambda.anonymous", "Enable lambda support with an anonymous class")
      .addDefaultValue(Boolean.TRUE)
      .addCategory(DumpInLibrary.class)
      .addCategory(PrebuiltCompatibility.class);

  @Nonnull
  public static final EnumPropertyId<LambdaGroupingScope> LAMBDA_GROUPING_SCOPE = EnumPropertyId
      .create(
          "jack.lambda.grouping-scope", "Defines the scope for lambda grouping",
          LambdaGroupingScope.class)
      .ignoreCase()
      .addDefaultValue(LambdaGroupingScope.NONE)
      .requiredIf(LAMBDA_TO_ANONYMOUS_CONVERTER.getValue().isTrue())
      .addCategory(DumpInLibrary.class)
      .addCategory(PrebuiltCompatibility.class)
      .addCategory(Private.class);

  @Nonnull
  public static final MessageDigestPropertyId LAMBDA_NAME_DIGEST_ALGO = MessageDigestPropertyId
      .create("jack.lambda.name.digest.algo", "Digest algorithm use for lambda class name")
      .requiredIf(LAMBDA_TO_ANONYMOUS_CONVERTER.getValue().isTrue()).addDefaultValue("SHA");

  @Nonnull
  public static final BooleanPropertyId LAMBDA_MERGE_INTERFACES = BooleanPropertyId
      .create("jack.lambda.merge-interfaces",
          "Allows merging functional interfaces")
      .addDefaultValue(Boolean.FALSE)
      .requiredIf(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getValue().isTrue())
      .addCategory(DumpInLibrary.class)
      .addCategory(PrebuiltCompatibility.class)
      .addCategory(Private.class);

  @Nonnull
  public static final BooleanPropertyId LAMBDA_SIMPLIFY_STATELESS = BooleanPropertyId
      .create("jack.lambda.simplify-stateless",
          "Simplifies stateless lambda to use single-instance implementation")
      .addDefaultValue(Boolean.FALSE)
      .requiredIf(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getValue().isTrue())
      .addCategory(DumpInLibrary.class)
      .addCategory(PrebuiltCompatibility.class)
      .addCategory(Private.class);

  @Nonnull
  public static final BooleanPropertyId GENERATE_JACK_LIBRARY = BooleanPropertyId.create(
      "jack.library", "Generate jack library").addDefaultValue(Boolean.FALSE);

  @Nonnull
  public static final BooleanPropertyId GENERATE_DEX_FILE =
      BooleanPropertyId.create("jack.dex", "Generate dex file").addDefaultValue(Boolean.FALSE)
          .addCategory(DumpInLibrary.class);

  /**
   * property used to specify the kind of switch enum optimization that is enabled. See(@link
   * SwitchEnumOptStrategy)
   */
  @Nonnull
  public static final EnumPropertyId<SwitchEnumOptStrategy> OPTIMIZED_ENUM_SWITCH =
      EnumPropertyId.create(
              "jack.optimization.enum.switch", "Optimize enum switch", SwitchEnumOptStrategy.class)
          .addDefaultValue(SwitchEnumOptStrategy.NEVER)
          .ignoreCase()
          .addCategory(DumpInLibrary.class)
          .addCategory(PrebuiltCompatibility.class);

  @Nonnull
  public static final BooleanPropertyId GENERATE_DEX_IN_LIBRARY = BooleanPropertyId
      .create("jack.library.dex", "Generate dex files in library").addDefaultValue(Boolean.TRUE)
      .requiredIf(GENERATE_JACK_LIBRARY.getValue().isTrue());

  @Nonnull
  public static final BooleanPropertyId USE_PREBUILT_FROM_LIBRARY =
      BooleanPropertyId.create("jack.library.prebuilt.use", "Use prebuilt files from library")
          .addDefaultValue(Boolean.TRUE)
          .requiredIf(
              GENERATE_DEX_FILE.getValue().isTrue().or(GENERATE_JACK_LIBRARY.getValue().isTrue()))
      .addCategory(DumpInLibrary.class);

  @Nonnull
  public static final BooleanPropertyId GENERATE_JAYCE_IN_LIBRARY = BooleanPropertyId
      .create("jack.library.jayce", "Generate Jayce files in library")
      .addDefaultValue(Boolean.FALSE).addCategory(Private.class)
      .requiredIf(GENERATE_JACK_LIBRARY.getValue().isTrue());

  @Nonnull
  public static final BooleanPropertyId GENERATE_DEPENDENCIES_IN_LIBRARY = BooleanPropertyId
      .create("jack.library.dependencies", "Generate Dependency files in library")
      .addDefaultValue(Boolean.FALSE).addCategory(Private.class)
      .requiredIf(GENERATE_JACK_LIBRARY.getValue().isTrue());

  @Nonnull
  public static final
      BooleanPropertyId GENERATE_LIBRARY_FROM_INCREMENTAL_FOLDER = BooleanPropertyId.create(
          "jack.library.from-incremental-folder",
          "Generate a jack library from the incremental folder").addDefaultValue(Boolean.FALSE)
          .addCategory(Private.class);

  @Nonnull
  public static final EnumPropertyId<Container> DEX_OUTPUT_CONTAINER_TYPE =
      EnumPropertyId.create("jack.dex.output.container", "Output container type", Container.class)
          .ignoreCase().requiredIf(GENERATE_DEX_FILE.getValue().isTrue());

  @Nonnull
  public static final EnumPropertyId<Container> LIBRARY_OUTPUT_CONTAINER_TYPE = EnumPropertyId
      .create("jack.library.output.container", "Library output container type", Container.class)
      .ignoreCase().requiredIf(GENERATE_JACK_LIBRARY.getValue().isTrue());

  @Nonnull
  public static final VFSPropertyId LIBRARY_OUTPUT_ZIP = VFSPropertyId
      .create("jack.library.output.zip", "Output zip archive for library",
          new ZipFSCodec(Existence.MAY_EXIST, Compression.UNCOMPRESSED).setInfoString("output-lib"))
      .requiredIf(GENERATE_JACK_LIBRARY.getValue().isTrue()
          .and(LIBRARY_OUTPUT_CONTAINER_TYPE.is(Container.ZIP))
          .or(GENERATE_LIBRARY_FROM_INCREMENTAL_FOLDER.getValue().isTrue()));

  @Nonnull
  public static final VFSPropertyId LIBRARY_OUTPUT_DIR =
      VFSPropertyId.create(
              "jack.library.output.dir",
              "Output folder for library",
              new CaseInsensitiveDirectFSCodec(Existence.MUST_EXIST).setInfoString("output-lib"))
          .withoutAutoAction()
          .requiredIf(
              GENERATE_JACK_LIBRARY
                  .getValue()
                  .isTrue()
                  .and(LIBRARY_OUTPUT_CONTAINER_TYPE.is(Container.DIR)));

  @Nonnull
  public static final OutputVFSPropertyId DEX_OUTPUT_DIR = OutputVFSPropertyId.create(
      "jack.dex.output.dir", "Output folder for dex",
      new DirectDirOutputVFSCodec(Existence.MUST_EXIST).setInfoString("output-dex"))
      .withoutAutoAction().requiredIf(
      DEX_OUTPUT_CONTAINER_TYPE.is(Container.DIR));

  @Nonnull
  public static final OutputVFSPropertyId DEX_OUTPUT_ZIP = OutputVFSPropertyId.create(
      "jack.dex.output.zip", "Output zip archive for dex",
      new ZipOutputVFSCodec(Existence.MAY_EXIST).setInfoString("output-dex")).requiredIf(
      DEX_OUTPUT_CONTAINER_TYPE.is(Container.ZIP));

  @Nonnull
  public static final LibraryPathPropertyId IMPORTED_LIBRARIES =
      new LibraryPathPropertyId("jack.library.import", "Libraries to import", "imported-lib")
          .withoutAutoAction()
          .addDefaultValue(Collections.<InputLibrary>emptyList());

  @Nonnull
  public static final LibraryPathPropertyId CLASSPATH =
      new LibraryPathPropertyId("jack.classpath", "Classpath", "classpath-lib")
          .withoutAutoAction()
          .addDefaultValue(Collections.<InputLibrary>emptyList());

  @Nonnull
  public static final BooleanPropertyId ENABLE_COMPILED_FILES_STATISTICS = BooleanPropertyId.create(
      "jack.statistic.source", "Enable compiled files statistics").addDefaultValue(
      Boolean.FALSE);

  @Nonnull
  public static final BooleanPropertyId ANNOTATION_PROCESSOR_ENABLED =
      BooleanPropertyId.create(
        "jack.annotation-processor", "Enable annotation processors")
        .addDefaultValue(true).addCategory(DumpInLibrary.class);

  @Nonnull
  public static final BooleanPropertyId USE_WHOLE_DEX_PREBUILT =
      BooleanPropertyId.create(
        "jack.library.prebuilt.whole", "When using prebuilt, use each dex prebuilt as a whole")
        .addCategory(Private.class)
        .addDefaultValue(true);

  @Option(name = "--version", usage = "display version")
  private boolean version;

  @Option(name = "--help", usage = "display help")
  private boolean help;

  @Option(name = "--help-properties", usage = "display properties list")
  private boolean helpProperties;

  @Option(name = "-D", metaVar = "<property>=<value>",
      usage = "set value for the given property (repeatable)",
      handler = MapOptionHandler.class)
  @Nonnull
  private final Map<String, String> properties = new HashMap<String, String>();

  @Option(name = "-A", metaVar = "<option>=<value>",
      usage = "set option for annotation processors (repeatable)",
      handler = MapOptionHandler.class)
  @CheckForNull
  private Map<String, String> annotationProcessorOption;

  @Nonnull
  public static final PropertyId<Map<String, String>> ANNOTATION_PROCESSOR_OPTIONS = PropertyId
      .create(
          "jack.annotation-processor.options",
          "Options for annotation processors",
          new PairListToMapCodecConverter<String, String>(new ListCodec<Pair<String, String>>(
              new PairCodec<String, String>(new StringValueCodec(
                  "an annotation processor option name", "option"), new StringValueCodec(
                  "an annotation processor option value", "value"))).setMin(0))).addDefaultValue(
          Collections.<String, String>emptyMap());

  private final File propertiesFile = null;

  /**
   * Jack verbosity level.
   * Note: The implementation of {@link ProblemLevel} assumes that the ordinal values of
   * {@link VerbosityLevel} are ordered from the highest severity to the lowest.
   */
  @VariableName("level")
  public enum VerbosityLevel {
    @EnumName(name = "error")
    ERROR("error"),
    @EnumName(name = "warning")
    WARNING("warning"),
    @EnumName(name = "info")
    INFO("info"),
    @EnumName(name = "debug", hide = true)
    @Deprecated DEBUG("debug"),
    @EnumName(name = "trace", hide = true)
    @Deprecated TRACE("trace");

    @Nonnull
    private final String id;

    VerbosityLevel(@Nonnull String id) {
      this.id = id;
    }

    public String getId() {
      return id;
    }
  }

  /**
   * Types of switch enum optimization strategies.
   * 1. feedback (set on by default)
   * 2. always
   * 3. never
   */
  @VariableName("strategy")
  public enum SwitchEnumOptStrategy {
    // feedback-based optimization: this strategy will be enabled/disabled based on the
    // compile time information collected, e.g., if it is detected that an enum is only
    // used in one/few switch statements, it is useless to optimize it. Potentially enable
    // this strategy will cost more compilation time, but save more dex code
    @EnumName(name = "feedback")
    FEEDBACK(),
    // different from feedback-based optimization, always strategy doesn't collect compile-
    // time information to guide switch enum optimization. It will always enable switch enum
    // optimization no matter the enum is rarely/frequently used. Ideally this strategy will
    // compile code quicker than feedback-based strategy does, but the generated dex may be
    // larger than feedback strategy
    @EnumName(name = "always")
    ALWAYS(),
    // this actually is not real strategy, but we still need it because switch enum
    // optimization is disabled when incremental compilation is triggered
    @EnumName(name = "never")
    NEVER();
  }

  @Nonnull
  public static final EnumPropertyId<VerbosityLevel> VERBOSITY_LEVEL = EnumPropertyId.create(
      "jack.verbose.level", "Verbosity level", VerbosityLevel.class)
      .addDefaultValue(VerbosityLevel.WARNING);

  @Option(name = "--verbose", usage = "set verbosity (default: warning)",
      handler = JackEnumOptionHandler.class)
  private VerbosityLevel verbose = VerbosityLevel.WARNING;

  /**
   * Folder used for incremental data.
   */
  @Option(name = "--incremental-folder", usage = "directory used for incremental data",
      metaVar = "<DIRECTORY>")
  private File incrementalFolder = null;

  @Option(name = "--output-dex", usage = "output dex files and resources to the directory",
      metaVar = "<DIRECTORY>")
  private File out = null;

  /**
   * Output to this zip file.
   */
  @Option(name = "--output-dex-zip", metaVar = "<FILE>")
  private File outZip = null;

  /**
   * Output jack library to this folder.
   */
  private File libraryOutDir = null;

  @Option(name = "--output-jack", usage = "output jack library file", metaVar = "<FILE>")
  private File libraryOutZip = null;

  @Option(name = "--config-jarjar", usage = "use jarjar rules files (default: none)",
      metaVar = "<FILE>")
  private List<File> jarjarRulesFiles = new ArrayList<File>(0);

  @Option(name = "--import", usage = "import the given file into the output (repeatable)",
      metaVar = "<FILE>")
  protected List<File> importedLibraries = new ArrayList<File>();

  @Option(name = "--import-resource",
      usage = "import the given directory into the output as resource files (repeatable)",
      metaVar = "<DIRECTORY>")
  private List<File> resImport = new ArrayList<File>();

  @Option(name = "--import-meta",
      usage = "import the given directory into the output as meta-files (repeatable)",
      metaVar = "<DIRECTORY>")
  private List<File> metaImport = new ArrayList<File>();

  @Option(name = "--config-proguard",
      usage = "use a proguard flags file (default: none) (repeatable)",
      metaVar = "<FILE>")
  protected List<File> proguardFlagsFiles = null;

  /**
   * Enable/disable compiler sanity checks.
   */
  @Option(name = "--sanity-checks",
      handler = ExplicitBooleanOptionHandler.class, metaVar = "[on | off]")
  private boolean sanityChecks =
      Jack.getVersion().getSubReleaseKind() == SubReleaseKind.ENGINEERING;
  @Nonnull
  public static final BooleanPropertyId SANITY_CHECKS =
      BooleanPropertyId.create("jack.sanity-checks", "Compiler sanity checks")
          .addCategory(DumpInLibrary.class)
          .addDefaultValue(Jack.getVersion().getSubReleaseKind() == SubReleaseKind.ENGINEERING);

  /**
   * Enable tracer and output into this dir (.html).
   */
  @Option(name = "--tracer-dir",
      metaVar = "<DIRECTORY>")
  private File tracerDir;

  /*
   * Annotation processor
   */

  @Option(name = "--processorpath", usage = "annotation processor classpath", metaVar = "<PATH>")
  @CheckForNull
  private String processorPath;

  @Option(name = "--processor", usage = "annotation processor class names",
      metaVar = "<NAME>[,<NAME>...]")
  @CheckForNull
  private String processor;

  @Nonnull
  public static final BooleanPropertyId ANNOTATION_PROCESSOR_MANUAL =
      BooleanPropertyId.create(
        "jack.annotation-processor.manual", "run only specified annotation processors")
        .addDefaultValue(false).addCategory(DumpInLibrary.class);

  @Nonnull
  public static final ListPropertyId<String> ANNOTATION_PROCESSOR_MANUAL_LIST =
      new ListPropertyId<String>("jack.annotation-processor.manual.list",
          "Annotation processor class names", new ClassNameCodec()).minElements(0)
              .requiredIf(ANNOTATION_PROCESSOR_MANUAL.getValue().isTrue());

  @Nonnull
  public static final PropertyId<Directory> ANNOTATION_PROCESSOR_SOURCE_OUTPUT_DIR =
    PropertyId.create(
      "jack.annotation-processor.source.output",
      "Output folder for sources generated by annotation processors",
      new DirectoryCodec(Existence.MUST_EXIST, Permission.WRITE | Permission.READ));

  @Nonnull
  public static final PropertyId<Directory> ANNOTATION_PROCESSOR_CLASS_OUTPUT_DIR =
    PropertyId.create(
      "jack.annotation-processor.class.output",
      "Output folder for classes generated by annotation processors",
      new DirectoryCodec(Existence.MUST_EXIST, Permission.WRITE | Permission.READ));

  @Nonnull
  public static final BooleanPropertyId ANNOTATION_PROCESSOR_PATH =
      BooleanPropertyId.create(
        "jack.annotation-processor.path",
        "Use annotation processor classpath for annotation processor loading")
        .addDefaultValue(false);

  @Nonnull
  public static final ListPropertyId<FileOrDirectory> ANNOTATION_PROCESSOR_PATH_LIST =
      new ListPropertyId<FileOrDirectory>("jack.annotation-processor.path.list",
          "Annotation processor classpath", new InputFileOrDirectoryCodec()).on(File.pathSeparator)
          .minElements(0).requiredIf(ANNOTATION_PROCESSOR_PATH.getValue().isTrue());

  /*
   *
   */

  @Nonnull
  public static final BooleanPropertyId USE_DEFAULT_LIBRARIES = BooleanPropertyId.create(
      "jack.classpath.default-libraries", "Use default libraries as first classpath entries")
      .addDefaultValue(Boolean.TRUE).addCategory(DumpInLibrary.class);

  @Nonnull
  public static final ListPropertyId<FileOrDirectory> SOURCE_PATH =
      new ListPropertyId<FileOrDirectory>("jack.source.path",
          "Source path", new InputFileOrDirectoryCodec()).on(File.pathSeparator)
          .minElements(0).addDefaultValue(Collections.<FileOrDirectory>emptyList());

  @Nonnull
  @Option(name = "-cp", aliases = "--classpath", usage = "set classpath", metaVar = "<PATH>")
  protected String classpath = "";

  // This is a trick to document @<FILE>, but it has no real link to ecjArguments
  @Argument(usage = "read command line from file", metaVar = "@<FILE>")
  @CheckForNull
  protected List<File> inputSources;

  @Nonnull
  public static final ListPropertyId<FileOrDirectory> SOURCES =
      new ListPropertyId<FileOrDirectory>("jack.source", "Sources to compile",
          new ReaderFileOrDirectoryCodec()).on(File.pathSeparator).minElements(0)
          .addDefaultValue(Collections.<FileOrDirectory>emptyList());

  @Nonnull
  private final List<String> ecjExtraArguments = new ArrayList<String>();

  @Option(name = "-g", usage = "emit debug infos")
  private Boolean emitLocalDebugInfo;

  /**
   * Available mode for the multidex feature
   */
  public enum MultiDexKind {
    NONE,
    NATIVE,
    LEGACY
  }

  @Option(name = "--multi-dex",
      usage = "whether to split code into multiple dex files (default: none)",
      handler = JackEnumOptionHandler.class)
  private MultiDexKind multiDexKind = MultiDexKind.NONE;

  @Nonnull
  public static final BooleanPropertyId OPTIMIZE_INNER_CLASSES_ACCESSORS = BooleanPropertyId.create(
      "jack.optimization.inner-class.accessors",
      "Avoid creating synthethic accessors for outer class private fields and methods")
      .addDefaultValue(Boolean.FALSE).addCategory(DumpInLibrary.class);

  @Nonnull
  public static final BooleanPropertyId OPTIMIZE_TAIL_RECURSION =
      BooleanPropertyId.create("jack.optimization.tail-recursion", "Optimize tail recursive calls")
          .addDefaultValue(Boolean.FALSE)
          .addCategory(DumpInLibrary.class)
          .addCategory(PrebuiltCompatibility.class);

  @Nonnull
  public static final BooleanPropertyId EMIT_LOCAL_DEBUG_INFO =
      BooleanPropertyId.create(
              "jack.dex.debug.vars", "Emit local variable debug info into generated dex")
          .addDefaultValue(Boolean.FALSE)
          .addCategory(DumpInLibrary.class)
          .addCategory(PrebuiltCompatibility.class);

  @Nonnull
  public static final BooleanPropertyId EMIT_LINE_NUMBER_DEBUG_INFO =
      BooleanPropertyId.create(
              "jack.dex.debug.lines", "Emit line number debug info into generated dex")
          .addDefaultValue(Boolean.TRUE)
          .addCategory(DumpInLibrary.class)
          .addCategory(PrebuiltCompatibility.class);

  @Nonnull
  public static final BooleanPropertyId EMIT_SOURCE_FILE_DEBUG_INFO =
      BooleanPropertyId.create(
              "jack.dex.debug.source", "Emit source file debug info into generated dex")
          .addDefaultValue(Boolean.TRUE)
          .addCategory(DumpInLibrary.class)
          .addCategory(PrebuiltCompatibility.class);

  @Nonnull
  public static final PropertyId<AndroidApiLevel> ANDROID_MIN_API_LEVEL = PropertyId
      .create("jack.android.min-api-level", "Minimum Android API level compatibility",
          new AndroidApiLevelCodec())
      .addDefaultValue(new AndroidApiLevel(AndroidApiLevel.ReleasedLevel.B))
      .addCategory(DumpInLibrary.class)
      .addCategory(Carnac.class)
      .addCategory(new PrebuiltCompatibility() {
        @Override
        public boolean isCompatible(@Nonnull Config config, @Nonnull String valueFromLibrary)
            throws ParsingException {
          AndroidApiLevel levelFromLib = config.parseAs(valueFromLibrary, ANDROID_MIN_API_LEVEL);
          AndroidApiLevel levelFromConf = config.get(ANDROID_MIN_API_LEVEL);

          if (levelFromLib.isReleasedLevel() && levelFromConf.isReleasedLevel()) {
            return levelFromLib.getReleasedLevel() <= levelFromConf.getReleasedLevel();
          } else if (!levelFromLib.isReleasedLevel() && !levelFromConf.isReleasedLevel()) {
            return levelFromLib.getProvisionalLevel() == levelFromConf.getProvisionalLevel();
          } else {
            return false;
          }
        }
      });

  @Nonnull
  public static final BooleanPropertyId DROP_METHOD_BODY = BooleanPropertyId.create(
      "jack.internal.dropmethodbody", "Drop method bodies when they are no longer useful")
      .addDefaultValue(Boolean.TRUE);

  @Nonnull
  public static final BooleanPropertyId SHRINKING_ENABLED =
      BooleanPropertyId.create("jack.shrob.shrink", "Enable shrinking feature")
      .addDefaultValue(false).addCategory(DumpInLibrary.class);

  @Nonnull
  public static final BooleanPropertyId OBFUSCATION_ENABLED =
      BooleanPropertyId.create("jack.shrob.obfuscate", "Enable obfuscation feature")
      .addDefaultValue(false).addCategory(DumpInLibrary.class);

  @CheckForNull
  protected Flags flags = null;

  @Nonnull
  public static final ObjectId<Flags> FLAGS = new ObjectId<Flags>("jack.shrob.flags", Flags.class);

  @Nonnull
  public static final BooleanPropertyId USE_MIXED_CASE_CLASSNAME =
      BooleanPropertyId
          .create("jack.obfuscation.mixedcaseclassname",
              "Use mixed case class name when obfuscating")
          .addDefaultValue(Boolean.FALSE).addCategory(DumpInLibrary.class);

  @Nonnull
  public static final BooleanPropertyId EMIT_CLASS_FILES =
      BooleanPropertyId.create("jack.class-file", "Generate class files")
      .addDefaultValue(false);

  @Nonnull
  public static final PropertyId<Directory> EMIT_CLASS_FILES_FOLDER =
    PropertyId.create(
      "jack.class-file.output.dir",
      "Output folder for class files",
      new DirectoryCodec(Existence.MUST_EXIST, Permission.WRITE | Permission.READ))
    .requiredIf(EMIT_CLASS_FILES.getValue().isTrue());

  @SuppressWarnings("unchecked")
  @Nonnull
  public static final ImplementationPropertyId<Filter<JMethod>> METHOD_FILTER =
      (ImplementationPropertyId<Filter<JMethod>>) (Object) ImplementationPropertyId.create(
          "jack.internal.filter.method", "Define which filter will be used for methods",
          Filter.class).addDefaultValue("all-methods");

  @CheckForNull
  private OutputStream reporterStream = null;

  /*
   * CodecContext
   */

  @CheckForNull
  private File workingDirectory = null;

  @CheckForNull
  private PrintStream standardError = null;

  @CheckForNull
  private PrintStream standardOutput = null;

  public void setWorkingDirectory(@Nonnull File workingDirectory) {
    this.workingDirectory = workingDirectory;
  }

  public void setStandardError(@Nonnull PrintStream standardError) {
    this.standardError = standardError;
  }

  public void setStandardOutput(@Nonnull PrintStream standardOutput) {
    this.standardOutput = standardOutput;
  }

  @CheckForNull
  private Charset defaultCharset = null;

  public void setDefaultCharset(@Nonnull Charset charset) {
    this.defaultCharset = charset;
  }

  @CheckForNull
  private CodecContext codecContext = null;

  @Nonnull
  private CodecContext getCodecContext() throws IllegalOptionsException {
    if (codecContext == null) {
      codecContext = new CodecContext();

      if (workingDirectory != null) {
        try {
          codecContext.setWorkingDirectory(workingDirectory);
        } catch (NotDirectoryException e) {
          throw new IllegalOptionsException(e.getMessage(), e);
        } catch (WrongPermissionException e) {
          throw new IllegalOptionsException(e.getMessage(), e);
        } catch (NoSuchFileException e) {
          throw new IllegalOptionsException(e.getMessage(), e);
        }
      }

      if (standardError != null) {
        codecContext.setStandardError(standardError);
      }

      if (standardOutput != null) {
        codecContext.setStandardOutput(standardOutput);
      }

      if (defaultCharset != null) {
        codecContext.setDefaultCharset(defaultCharset);
      }

      if (sanityChecks) {
        codecContext.setDebug();
      }
    }

    assert codecContext != null;
    return codecContext;
  }

  /*
   * Plugin Manager
   */

  @Option(name = "--list-plugins", usage = "display all available plugins")
  private boolean listPlugins;

  @Option(name = "--pluginpath", usage = "jack plugin classpath", metaVar = "<PATH>")
  @Nonnull
  private String pluginPath = "";

  @Option(name = "--plugin", usage = "jack plugin names", metaVar = "<NAME>[,<NAME>...] (unique)")
  @Nonnull
  private String pluginNames = "";

  public void setPluginPath(@CheckForNull String pluginPath) {
    if (pluginPath == null) {
      this.pluginPath = "";
    } else {
      this.pluginPath = pluginPath;
    }
  }

  public void setPluginNames(@Nonnull String pluginNames) {
    this.pluginNames = pluginNames;
  }

  @CheckForNull
  private PluginManager pluginManager = null;

  @Nonnull
  public static final ListCodec<InputJarFile> PLUGIN_PATH_CODEC =
      new ListCodec<InputJarFile>(new JackPluginJarCodec()).setSeparator(File.pathSeparator);

  @Nonnull
  public static final ListCodec<String> PLUGIN_NAMES_CODEC =
      new ListCodec<String>(new StringValueCodec("a Jack plugin name", "plugin"))
          .setSeparator(",")
          .ensureUnicity();

  public void ensurePluginManager()
      throws IllegalOptionsException {
    if (pluginManager == null) {
      List<InputJarFile> jars;
      try {
        jars = PLUGIN_PATH_CODEC.checkString(getCodecContext(), pluginPath);
        if (jars == null) {
          jars = PLUGIN_PATH_CODEC.parseString(getCodecContext(), pluginPath);
        }
        pluginManager = new PluginManager();
        try {
          for (InputJarFile jar : jars) {
            pluginManager.addPlugin(new URL[]{jar.getFile().toURI().toURL()});
          }
        } catch (NotJackPluginException | MalformedURLException e) {
          throw new AssertionError(e);
        }
      } catch (ParsingException e) {
        throw new IllegalOptionsException("option --pluginpath: " + e.getMessage(), e);
      }

      try {
        List<String> names = PLUGIN_NAMES_CODEC.checkString(getCodecContext(), pluginNames);
        if (names == null) {
          names = PLUGIN_NAMES_CODEC.parseString(getCodecContext(), pluginNames);
        }
        for (String name : names) {
          try {
            assert pluginManager != null;
            pluginManager.loadPlugin(name);
          } catch (PluginNotFoundException e) {
            throw new IllegalOptionsException("option --plugin: " + e.getMessage(), e);
          }
        }
      } catch (ParsingException e) {
        throw new IllegalOptionsException("option --plugin: " + e.getMessage(), e);

      }
    }
  }

  @Nonnull
  public PluginManager getPluginManager() {
    assert pluginManager != null;
    return pluginManager;
  }

  /*
   *
   */

  public void setVerbosityLevel(@Nonnull VerbosityLevel verbose) {
    this.verbose = verbose;
  }

  public boolean askForVersion() {
    return version;
  }

  public boolean askForHelp() {
    return help;
  }

  public boolean askForPropertiesHelp() {
    return helpProperties;
  }

  public boolean askForPluginsList() {
    return listPlugins;
  }

  public void setInputSources(@Nonnull Collection<File> inputSources) {
    this.inputSources = new ArrayList<File>(inputSources);
  }

  public void setOutputDir(File out) {
    this.out = out;
  }

  public void setOutputZip(File out) {
    this.outZip = out;
  }

  @CheckForNull
  private Config config = null;

  @Nonnull
  public Config getConfig() {
    assert config != null;

    return config;
  }

  @Nonnull
  public GatherConfigBuilder getDefaultConfigBuilder()
      throws IOException, IllegalOptionsException {
    ensurePluginManager();

    GatherConfigBuilder configBuilder = new GatherConfigBuilder(sanityChecks,
        getPluginManager().getReflectionManager(ReflectionFactory.getManager()));
    assert codecContext != null; // ensured by ensurePluginManager()
    configBuilder.setCodecContext(codecContext);
    String resourceName = "/config.properties";

    InputStream is = Main.class.getResourceAsStream(resourceName);
    if (is != null) {
      try {
        configBuilder.load(is, new StringLocation("resource " + resourceName));
      } finally {
        is.close();
      }
    }

    return configBuilder;
  }

  @Nonnull
  public GatherConfigBuilder getConfigBuilder(@Nonnull RunnableHooks hooks)
      throws IllegalOptionsException {

    GatherConfigBuilder configBuilder;

    if (propertiesFile != null) {
      if (!propertiesFile.exists()) {
        throw new IllegalOptionsException(
            "The specified config file '" + propertiesFile.getPath() + "' does not exist.");
      }

      if (!propertiesFile.isFile()) {
        throw new IllegalOptionsException(
            "The specified config file '" + propertiesFile.getPath() + "' is not a file.");
      }

      if (!propertiesFile.canRead()) {
        throw new IllegalOptionsException(
            "The specified config file '" + propertiesFile.getPath() + "' cannot be read.");
      }

      ensurePluginManager();
      configBuilder = new GatherConfigBuilder(sanityChecks,
          getPluginManager().getReflectionManager(ReflectionFactory.getManager()));
      configBuilder.setCodecContext(getCodecContext());

      try {
        InputStream is = new BufferedInputStream(new FileInputStream(propertiesFile));
        try {
          configBuilder.load(is, new FileLocation(propertiesFile));
        } finally {
          is.close();
        }
      } catch (FileNotFoundException e) {
        // Already check
        throw new AssertionError();
      } catch (IOException e) {
        throw new IllegalOptionsException(
            "The specified config file '" + propertiesFile.getPath() + "' cannot be read.",
            e.getCause());
      }
    } else {
      try {
        configBuilder = getDefaultConfigBuilder();
      } catch (IOException e) {
        throw new IllegalOptionsException(e.getMessage(), e);
      }
    }


    configBuilder.pushDefaultLocation(new StringLocation("Options"));

    configBuilder.set(VERBOSITY_LEVEL, verbose);

    if (reporterStream != null) {
      configBuilder.set(Reporter.REPORTER_WRITER,
          new WriterFile(reporterStream, new NoLocation()));
    }

    if (!jarjarRulesFiles.isEmpty()) {
      configBuilder.set(PackageRenamer.JARJAR_ENABLED, true);
      String sep = PackageRenamer.JARJAR_FILES.getCodec().getSeparator();
      configBuilder.setString(PackageRenamer.JARJAR_FILES, Joiner.on(sep).join(jarjarRulesFiles));
      configBuilder.set(Options.USE_PREBUILT_FROM_LIBRARY, false);
      logger.log(Level.WARNING,
          "Prebuilts from libraries are not used due to usage of jarjar");
    }

    if (processor != null) {
      configBuilder.set(ANNOTATION_PROCESSOR_MANUAL, true);
      configBuilder.setString(ANNOTATION_PROCESSOR_MANUAL_LIST, processor);
    }
    configBuilder.set(ANNOTATION_PROCESSOR_SOURCE_OUTPUT_DIR, createTempDir(hooks));
    Directory annotationProcessorOutputClasses = createTempDir(hooks);
    configBuilder.set(ANNOTATION_PROCESSOR_CLASS_OUTPUT_DIR, annotationProcessorOutputClasses);
    addResource(annotationProcessorOutputClasses.getFile());
    if (processorPath != null) {
      configBuilder.set(ANNOTATION_PROCESSOR_PATH, true);
      configBuilder.setString(ANNOTATION_PROCESSOR_PATH_LIST, processorPath);
    }

    if (annotationProcessorOption != null) {
      configBuilder.set(ANNOTATION_PROCESSOR_OPTIONS, annotationProcessorOption);
    }

    if (!resImport.isEmpty()) {
      configBuilder.setString(ResourceImporter.IMPORTED_RESOURCES,
          Joiner.on(File.pathSeparator).join(resImport));
    }

    if (!metaImport.isEmpty()) {
      configBuilder.setString(MetaImporter.IMPORTED_META,
          Joiner.on(File.pathSeparator).join(metaImport));
    }

    if (inputSources != null && !inputSources.isEmpty()) {
      configBuilder.setString(SOURCES, Joiner.on(File.pathSeparator).join(inputSources));
    }

    if (emitLocalDebugInfo != null) {
      configBuilder.set(EMIT_LOCAL_DEBUG_INFO, emitLocalDebugInfo);
    }

    configBuilder.pushDefaultLocation(new StringLocation("proguard flags"));

    configBuilder.set(SHRINKING_ENABLED, flags != null && flags.shrink());
    configBuilder.set(OBFUSCATION_ENABLED, flags != null && flags.obfuscate());

    if (flags != null) {
      if (flags.obfuscate() || flags.shrink()) {
        configBuilder.set(Options.USE_WHOLE_DEX_PREBUILT, false);
        logger
            .log(
                Level.WARNING,
                "Prebuilts from libraries are not use due to usage of shrinking or obfuscation");
      }

      if (flags.obfuscate()) { // keepAttribute only makes sense when obfuscating
        configBuilder.set(Options.USE_PREBUILT_FROM_LIBRARY, false);
        boolean emitRuntimeInvisibleAnnotation = flags.keepAttribute("RuntimeInvisibleAnnotations");
        configBuilder.set(
            AnnotationRemover.EMIT_SOURCE_RETENTION_ANNOTATION, emitRuntimeInvisibleAnnotation);
        configBuilder.set(
            AnnotationRemover.EMIT_CLASS_RETENTION_ANNOTATION, emitRuntimeInvisibleAnnotation);
        configBuilder.set(AnnotationRemover.EMIT_RUNTIME_RETENTION_ANNOTATION,
            flags.keepAttribute("RuntimeVisibleAnnotations"));

        boolean emitRuntimeInvisibleParameterAnnotation =
            flags.keepAttribute("RuntimeInvisibleParameterAnnotations");
        configBuilder.set(
            ParameterAnnotationRemover.EMIT_SOURCE_RETENTION_PARAMETER_ANNOTATION,
            emitRuntimeInvisibleParameterAnnotation);
        configBuilder.set(
            ParameterAnnotationRemover.EMIT_CLASS_RETENTION_PARAMETER_ANNOTATION,
            emitRuntimeInvisibleParameterAnnotation);
        configBuilder.set(
            ParameterAnnotationRemover.EMIT_RUNTIME_RETENTION_PARAMETER_ANNOTATION,
            flags.keepAttribute("RuntimeVisibleParameterAnnotations"));

        configBuilder.set(EMIT_LINE_NUMBER_DEBUG_INFO, flags.keepAttribute("LineNumberTable"));
        configBuilder.set(EMIT_LOCAL_DEBUG_INFO, flags.keepAttribute("LocalVariableTable"));
      }

      configBuilder.set(Options.FLAGS, flags);
      configBuilder.set(
          Options.USE_MIXED_CASE_CLASSNAME, flags.getUseMixedCaseClassName());
      configBuilder.set(Renamer.USE_UNIQUE_CLASSMEMBERNAMES,
          flags.getUseUniqueClassMemberNames());

      File mapping = flags.getObfuscationMapping();
      if (mapping != null) {
        configBuilder.set(Renamer.USE_MAPPING, true);
        configBuilder.setString(Renamer.MAPPING_FILE, mapping.getPath());
      } else {
        configBuilder.set(Renamer.USE_MAPPING, false);
      }

      File seeds = flags.getSeedsFile();
      if (seeds != null) {
        configBuilder.setString(SeedPrinter.SEEDS_OUTPUT_FILE, seeds.getPath());
      }

      File dictionary = flags.getObfuscationDictionary();
      if (dictionary != null) {
        configBuilder.set(Renamer.USE_OBFUSCATION_DICTIONARY, true);
        configBuilder.setString(Renamer.OBFUSCATION_DICTIONARY, dictionary.getPath());
      } else {
        configBuilder.set(Renamer.USE_OBFUSCATION_DICTIONARY, false);
      }

      File classDictionary = flags.getClassObfuscationDictionary();
      if (classDictionary != null) {
        configBuilder.set(Renamer.USE_CLASS_OBFUSCATION_DICTIONARY, true);
        configBuilder.setString(Renamer.CLASS_OBFUSCATION_DICTIONARY,
            classDictionary.getPath());
      } else {
        configBuilder.set(Renamer.USE_CLASS_OBFUSCATION_DICTIONARY, false);
      }

      File packageDictionary = flags.getPackageObfuscationDictionary();
      if (packageDictionary != null) {
        configBuilder.set(Renamer.USE_PACKAGE_OBFUSCATION_DICTIONARY, true);
        configBuilder.setString(
            Renamer.PACKAGE_OBFUSCATION_DICTIONARY, packageDictionary.getPath());
      } else {
        configBuilder.set(Renamer.USE_PACKAGE_OBFUSCATION_DICTIONARY, false);
      }
      configBuilder.set(MappingPrinter.MAPPING_OUTPUT_ENABLED, flags.printMapping());
      File outputmapping = flags.getOutputMapping();
      if (outputmapping != null) {
        configBuilder.setString(MappingPrinter.MAPPING_OUTPUT_FILE,
            outputmapping.getPath());
      }

      if (flags.getUseMixedCaseClassName()) {
        configBuilder.setString(NameProviderFactory.NAMEPROVIDER, "mixed-case");
      }

      String packageForRenamedClasses = flags.getPackageForRenamedClasses();
      if (packageForRenamedClasses != null) {
        configBuilder.set(Renamer.REPACKAGE_CLASSES, true);
        configBuilder.set(Renamer.PACKAGE_FOR_RENAMED_CLASSES, packageForRenamedClasses);
        if (flags.getPackageForFlatHierarchy() != null) {
          throw new IllegalOptionsException("Flatten package and repackage classes cannot be used"
              + " simultaneously");
        }
      } else {
        configBuilder.set(Renamer.REPACKAGE_CLASSES, false);
      }

      String packageForRenamedPackages = flags.getPackageForFlatHierarchy();
      if (packageForRenamedPackages != null) {
        configBuilder.set(Renamer.FLATTEN_PACKAGE, true);
        configBuilder.set(Renamer.PACKAGE_FOR_RENAMED_PACKAGES, packageForRenamedPackages);
      } else {
        configBuilder.set(Renamer.FLATTEN_PACKAGE, false);
      }

      String renameSourceFileAttribute = flags.getRenameSourceFileAttribute();
      if (renameSourceFileAttribute != null) {
        configBuilder.set(SourceFileRenamer.RENAME_SOURCEFILE, true);
        configBuilder.set(SourceFileRenamer.NEW_SOURCEFILE_NAME,
            new File(renameSourceFileAttribute));
      } else {
        configBuilder.set(SourceFileRenamer.RENAME_SOURCEFILE, false);
      }
    }

    configBuilder.popDefaultLocation();

    if (importedLibraries != null) {
      configBuilder.setString(
          IMPORTED_LIBRARIES, Joiner.on(File.pathSeparator).join(importedLibraries));
    }

    if (classpath != null) {
      configBuilder.setString(CLASSPATH, classpath);
    }

    if (libraryOutZip != null) {
      configBuilder.setString(LIBRARY_OUTPUT_ZIP, libraryOutZip.getPath());
      configBuilder.set(LIBRARY_OUTPUT_CONTAINER_TYPE, Container.ZIP);
      configBuilder.set(GENERATE_JACK_LIBRARY, true);
      configBuilder.set(GENERATE_JAYCE_IN_LIBRARY, true);
      configBuilder.set(GENERATE_DEPENDENCIES_IN_LIBRARY, true);
    } else if (libraryOutDir != null) {
      configBuilder.setString(LIBRARY_OUTPUT_DIR, libraryOutDir.getPath());
      configBuilder.set(LIBRARY_OUTPUT_CONTAINER_TYPE, Container.DIR);
      configBuilder.set(GENERATE_JACK_LIBRARY, true);
      configBuilder.set(GENERATE_JAYCE_IN_LIBRARY, true);
      configBuilder.set(GENERATE_DEPENDENCIES_IN_LIBRARY, true);
    } else {
      configBuilder.setString(Options.LIBRARY_OUTPUT_DIR, createTempDir(hooks).getPath());
      configBuilder.set(LIBRARY_OUTPUT_CONTAINER_TYPE, Container.DIR);
      configBuilder.set(GENERATE_JACK_LIBRARY, true);
    }

    switch (multiDexKind) {
      case NATIVE:
        configBuilder.setString(DexFileWriter.DEX_WRITING_POLICY, "multidex");
        break;
      case LEGACY:
        configBuilder.setString(DexFileWriter.DEX_WRITING_POLICY, "multidex");
        configBuilder.set(MultiDexLegacy.MULTIDEX_LEGACY, true);
        break;
      case NONE:
        break;
      default:
        throw new AssertionError("Unsupported multi dex kind: '" + multiDexKind.name() + "'");
    }



    if (outZip != null) {
      configBuilder.setString(DEX_OUTPUT_ZIP, outZip.getPath());
      configBuilder.set(DEX_OUTPUT_CONTAINER_TYPE, Container.ZIP);
      configBuilder.set(GENERATE_DEX_FILE, true);
      configBuilder.set(AndroidCompatibilityChecker.CHECK_COMPATIBILITY, true);
    } else if (out != null) {
      configBuilder.setString(DEX_OUTPUT_DIR, out.getPath());
      configBuilder.set(DEX_OUTPUT_CONTAINER_TYPE, Container.DIR);
      configBuilder.set(GENERATE_DEX_FILE, true);
      configBuilder.set(AndroidCompatibilityChecker.CHECK_COMPATIBILITY, true);
    }

    // use a variable to keep record of whether incremental compilation is enabled or not,
    // because we cannot check the value through configBuilder
    boolean isIncrementalEnabled = false;
    if (incrementalFolder != null) {
      if (multiDexKind == MultiDexKind.LEGACY) {
        logger.log(Level.WARNING,
            "Incremental mode is disabled due to multi-dex legacy mode");
      } else if (flags != null && (flags.shrink() || flags.obfuscate())) {
        logger.log(Level.WARNING,
            "Incremental mode is disabled due to usage of shrinking or obfuscation");
      } else if (!jarjarRulesFiles.isEmpty()) {
        logger.log(Level.WARNING,
            "Incremental mode is disabled due to usage of jarjar");
      } else if (properties.containsKey(SOURCE_PATH.getName())) {
        logger.log(Level.WARNING,
            "Incremental mode is disabled due to usage of source path");
      } else {
        configBuilder.set(Options.INCREMENTAL_MODE, true);
        configBuilder.setString(Options.INPUT_FILTER, "incremental");
        configBuilder.set(Options.GENERATE_JACK_LIBRARY, true);
        configBuilder.set(GENERATE_JAYCE_IN_LIBRARY, true);
        configBuilder.set(GENERATE_DEPENDENCIES_IN_LIBRARY, true);
        configBuilder.setString(Options.LIBRARY_OUTPUT_CONTAINER_TYPE, "dir");
        configBuilder.setString(Options.LIBRARY_OUTPUT_DIR, incrementalFolder.getPath());
        if (libraryOutZip != null) {
          configBuilder.set(GENERATE_LIBRARY_FROM_INCREMENTAL_FOLDER, true);
        }
        isIncrementalEnabled = true;
      }
    }

    if (tracerDir != null) {
      configBuilder.setString(TracerFactory.TRACER, "html");
      configBuilder.setString(StatsTracerFtl.TRACER_DIR, tracerDir.getPath());
    }

    configBuilder.set(SANITY_CHECKS, sanityChecks);

    configBuilder.popDefaultLocation();

    configBuilder.setString(CLASSPATH, classpath);

    for (Entry<String, String> entry : properties.entrySet()) {
      configBuilder.setString(entry.getKey(), entry.getValue(), new StringLocation("-D option"));
    }

    if (isIncrementalEnabled) {
      // if the incremental compilation is enabled, the switch enum optimization cannot
      // be enabled because it will generates non-deterministic code. This has to be done after
      // -D options are set
      configBuilder.set(OPTIMIZED_ENUM_SWITCH.getName(), SwitchEnumOptStrategy.NEVER);
    }

    configBuilder.processEnvironmentVariables("JACK_CONFIG_");
    configBuilder.setHooks(hooks);

    return configBuilder;
  }

  public void checkValidity(@Nonnull RunnableHooks hooks)
      throws IllegalOptionsException, ConfigurationException {
    ecjExtraArguments.clear();
    config = getConfigBuilder(hooks).build();

    // FINDBUGS
    Config config = this.config;
    assert config != null;

    // FINDBUGS
    assert config != null;

    // Check ecj arguments
    if (inputSources != null) {
      if (config.get(VERBOSITY_LEVEL) == VerbosityLevel.ERROR) {
        ecjExtraArguments.add(0, "-nowarn");
      }
      ecjExtraArguments.add("-source");
      ecjExtraArguments.add(config.get(Options.JAVA_SOURCE_VERSION).toString());

      if (!config.get(Options.ANNOTATION_PROCESSOR_ENABLED).booleanValue()) {
        ecjExtraArguments.add("-proc:none");
      }

      if (config.get(Options.EMIT_CLASS_FILES).booleanValue()) {
        ecjExtraArguments.add("-d");
        ecjExtraArguments.add(config.get(Options.EMIT_CLASS_FILES_FOLDER).getPath());
      }
    }

    // Check Jack arguments
    if (config.get(CodeItemBuilder.EMIT_SYNTHETIC_LOCAL_DEBUG_INFO).booleanValue()
        && !config.get(Options.EMIT_LOCAL_DEBUG_INFO).booleanValue()) {
      throw new PropertyIdException(CodeItemBuilder.EMIT_SYNTHETIC_LOCAL_DEBUG_INFO,
          new NoLocation(),
          "Impossible to emit synthetic debug info when not emitting debug info");
    }

    if (verbose == VerbosityLevel.DEBUG || verbose == VerbosityLevel.TRACE) {
      config.get(Reporter.REPORTER).report(Severity.NON_FATAL, new DeprecatedVerbosity(verbose));
    }
  }

  public void setJayceOutputDir(@Nonnull File outputDir) {
    libraryOutDir = outputDir;
  }

  public void setJayceOutputZip(@Nonnull File outputZip) {
    libraryOutZip = outputZip;
  }

  public void setImportedLibraries(@Nonnull List<File> importedLibraries) {
    this.importedLibraries = importedLibraries;
  }

  @CheckForNull
  public Flags getFlags() {
    return flags;
  }

  public void setFlags(@Nonnull Flags flags) {
    this.flags = flags;
  }

  public void applyShrobFlags() {
    assert flags != null;
    List<File> inJars = flags.getInJars();
    if (inJars.size() > 0) {
      importedLibraries = new ArrayList<File>(inJars.size());
      importedLibraries.addAll(inJars);
    }
    List<File> outJars = flags.getOutJars();
    if (outJars.size() > 0) {
      File outJar = outJars.get(0);
      if (outJar.isDirectory()) {
        libraryOutDir = outJar;
      } else {
        libraryOutZip = outJar;
      }
    }
    String libraryJars = flags.getLibraryJars();
    if (libraryJars != null) {
      if (classpath.isEmpty()) {
        classpath = libraryJars;
      } else {
        classpath += File.pathSeparatorChar + libraryJars;
      }
    }
  }

  public void setClasspath(@CheckForNull String classpath) {
    if (classpath == null) {
      this.classpath = "";
    } else {
      this.classpath = classpath;
    }
  }

  public void setMultiDexKind(@Nonnull MultiDexKind multiDexKind) {
    this.multiDexKind = multiDexKind;
  }

  public void addProguardFlagsFile(@Nonnull File flags) {
    if (proguardFlagsFiles == null) {
      proguardFlagsFiles = new ArrayList<File>();
    }
    proguardFlagsFiles.add(flags);
  }

  public void addProperty(@Nonnull String propertyName, @Nonnull String propertyValue) {
    properties.put(propertyName, propertyValue);
  }

  @Nonnull
  List<String> getEcjExtraArguments() {
    return ecjExtraArguments;
  }

  public void setProguardFlagsFile(@Nonnull List<File> proguardFlagsFiles) {
    this.proguardFlagsFiles = proguardFlagsFiles;
  }

  public void setJarjarRulesFiles(@Nonnull List<File> jarjarRulesFiles) {
    this.jarjarRulesFiles = jarjarRulesFiles;
  }

  public void disableDxOptimizations() {
    properties.put(CodeItemBuilder.DEX_OPTIMIZE.getName(), "false");
  }

  public void setSanityChecks(boolean sanityChecks) {
    this.sanityChecks = sanityChecks;
  }

  public void setIncrementalFolder(@Nonnull File incrementalFolder) {
    this.incrementalFolder = incrementalFolder;
  }

  public void addResource(@Nonnull File resource) {
    resImport.add(resource);
  }

  public void setResourceDirs(@Nonnull List<File> resourceDirs) {
    resImport = new ArrayList<File>(resourceDirs);
  }

  public void setMetaDirs(@Nonnull List<File> metaDirs) {
    metaImport = metaDirs;
  }

  public void setReporterStream(@Nonnull OutputStream reporterStream) {
    this.reporterStream = reporterStream;
  }

  public List<File> getProguardFlagsFile() {
    List<File> proguardFlagsFileFromWorkingDir = new ArrayList<File>(proguardFlagsFiles.size());
    for (File proguardFlagsFile : proguardFlagsFiles) {
      if (workingDirectory != null && !proguardFlagsFile.isAbsolute()) {
        proguardFlagsFileFromWorkingDir.add(
            new File(workingDirectory, proguardFlagsFile.getPath()));
      } else {
        proguardFlagsFileFromWorkingDir.add(proguardFlagsFile);
      }
    }
    return proguardFlagsFileFromWorkingDir;
  }

  @Nonnull
  private static Directory createTempDir(
      @Nonnull RunnableHooks hooks) {
    try {
      File tmp = Files.createTempDir("jack-");
      Directory dir = new Directory(tmp.getPath(), hooks, Existence.MUST_EXIST, Permission.WRITE,
          ChangePermission.NOCHANGE);
      hooks.addHook(new TempDirDeleter(dir));
      return dir;
    } catch (CannotChangePermissionException | CannotCreateFileException
        | FileAlreadyExistsException | NotDirectoryException | NoSuchFileException
        | WrongPermissionException e) {
      throw new JackUserException(e);
    }
  }

  private static class TempDirDeleter implements Runnable {

    @Nonnull
    private final Directory dir;

    public TempDirDeleter(@Nonnull Directory dir) {
      this.dir = dir;
    }

    @Override
    public void run() {
      try {
        FileUtils.deleteDir(dir.getFile());
      } catch (IOException e) {
        throw new JackIOException(
            "Failed to delete temporary " + dir.getLocation().getDescription(), e);
      }
    }
  }
}
