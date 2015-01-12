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

import com.google.common.io.Files;

import com.android.jack.backend.dex.DexFileWriter;
import com.android.jack.backend.dex.FieldInitializerRemover;
import com.android.jack.backend.dex.MultiDexLegacy;
import com.android.jack.backend.dex.rop.CodeItemBuilder;
import com.android.jack.config.id.Arzon;
import com.android.jack.config.id.JavaVersionPropertyId;
import com.android.jack.config.id.Private;
import com.android.jack.incremental.InputFilter;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.shrob.obfuscation.MappingPrinter;
import com.android.jack.shrob.obfuscation.NameProviderFactory;
import com.android.jack.shrob.obfuscation.Renamer;
import com.android.jack.shrob.obfuscation.SourceFileRenamer;
import com.android.jack.shrob.obfuscation.annotation.AnnotationRemover;
import com.android.jack.shrob.obfuscation.annotation.ParameterAnnotationRemover;
import com.android.jack.shrob.seed.SeedPrinter;
import com.android.jack.shrob.spec.Flags;
import com.android.jack.transformations.renamepackage.PackageRenamer;
import com.android.jack.util.filter.AllMethods;
import com.android.jack.util.filter.Filter;
import com.android.sched.util.RunnableHooks;
import com.android.sched.util.codec.DirectDirInputOutputVDirCodec;
import com.android.sched.util.codec.DirectDirOutputVDirCodec;
import com.android.sched.util.codec.ZipInputOutputVDirCodec;
import com.android.sched.util.codec.ZipOutputVDirCodec;
import com.android.sched.util.config.Config;
import com.android.sched.util.config.ConfigPrinterFactory;
import com.android.sched.util.config.ConfigurationException;
import com.android.sched.util.config.GatherConfigBuilder;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.config.id.EnumPropertyId;
import com.android.sched.util.config.id.ImplementationPropertyId;
import com.android.sched.util.config.id.ObjectId;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.config.id.ReflectFactoryPropertyId;
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.FileUtils;
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.location.StringLocation;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.tracer.StatsTracerFtl;
import com.android.sched.vfs.Container;
import com.android.sched.vfs.DirectVFS;
import com.android.sched.vfs.InputOutputVFS;
import com.android.sched.vfs.OutputVFS;

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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
/**
 * Jack command line options Bean
 */
@HasKeyId
public class Options {

  @Nonnull
  public static final BooleanPropertyId INCREMENTAL_MODE = BooleanPropertyId
      .create("jack.incremental", "Enable incremental mode")
      .addDefaultValue(Boolean.FALSE);

  @Nonnull
  public static final ReflectFactoryPropertyId<InputFilter> INPUT_FILTER = ReflectFactoryPropertyId
      .create("jack.input.filter", "Inputs filter", InputFilter.class)
      .addDefaultValue("no-filter").addArgType(Options.class);

  @Nonnull
  public static final JavaVersionPropertyId JAVA_SOURCE_VERSION = JavaVersionPropertyId
      .create("jack.java.source.version", "Java source version").addDefaultValue("1.7")
      .withCategory(Arzon.get());

  @Nonnull
  public static final BooleanPropertyId GENERATE_JACK_LIBRARY = BooleanPropertyId.create(
      "jack.library", "Generate jack library").addDefaultValue(Boolean.FALSE);

  @Nonnull
  public static final BooleanPropertyId GENERATE_DEX_FILE = BooleanPropertyId
      .create("jack.dex", "Generate dex file").addDefaultValue(Boolean.FALSE);

  @Nonnull
  public static final BooleanPropertyId GENERATE_DEX_IN_LIBRARY = BooleanPropertyId
      .create("jack.library.dex", "Generate dex files in library").addDefaultValue(Boolean.TRUE)
      .requiredIf(GENERATE_JACK_LIBRARY.getValue().isTrue());

  @Nonnull
  public static final BooleanPropertyId GENERATE_JAYCE_IN_LIBRARY = BooleanPropertyId
      .create("jack.library.jayce", "Generate Jayce files in library")
      .addDefaultValue(Boolean.FALSE).withCategory(Private.get())
      .requiredIf(GENERATE_JACK_LIBRARY.getValue().isTrue());

  @Nonnull
  public static final BooleanPropertyId GENERATE_DEPENDENCIES_IN_LIBRARY = BooleanPropertyId
      .create("jack.library.dependencies", "Generate Dependency files in library")
      .addDefaultValue(Boolean.FALSE).withCategory(Private.get())
      .requiredIf(GENERATE_JACK_LIBRARY.getValue().isTrue());

  @Nonnull
  public static final EnumPropertyId<Container> DEX_OUTPUT_CONTAINER_TYPE = EnumPropertyId.create(
      "jack.dex.output.container", "Output container type", Container.values())
      .ignoreCase().requiredIf(GENERATE_DEX_FILE.getValue().isTrue());

  @Nonnull
  public static final EnumPropertyId<Container> LIBRARY_OUTPUT_CONTAINER_TYPE = EnumPropertyId
      .create("jack.library.output.container", "Library output container type", Container.values())
      .ignoreCase().requiredIf(GENERATE_JACK_LIBRARY.getValue().isTrue());

  @Nonnull
  public static final PropertyId<InputOutputVFS> LIBRARY_OUTPUT_ZIP = PropertyId.create(
      "jack.library.output.zip", "Output zip archive for library",
      new ZipInputOutputVDirCodec(Existence.MAY_EXIST)).requiredIf(GENERATE_JACK_LIBRARY.getValue()
      .isTrue().and(LIBRARY_OUTPUT_CONTAINER_TYPE.is(Container.ZIP)));

  @Nonnull
  public static final PropertyId<InputOutputVFS> LIBRARY_OUTPUT_DIR = PropertyId.create(
      "jack.library.output.dir", "Output folder for library",
      new DirectDirInputOutputVDirCodec(Existence.MAY_EXIST)).requiredIf(GENERATE_JACK_LIBRARY
      .getValue().isTrue().and(LIBRARY_OUTPUT_CONTAINER_TYPE.is(Container.DIR)));

  @Nonnull
  public static final PropertyId<OutputVFS> DEX_OUTPUT_DIR = PropertyId.create(
      "jack.dex.output.dir", "Output folder for dex",
      new DirectDirOutputVDirCodec(Existence.MUST_EXIST)).requiredIf(
      DEX_OUTPUT_CONTAINER_TYPE.is(Container.DIR));

  @Nonnull
  public static final PropertyId<OutputVFS> DEX_OUTPUT_ZIP = PropertyId.create(
      "jack.dex.output.zip", "Output zip archive for dex",
      new ZipOutputVDirCodec(Existence.MAY_EXIST)).requiredIf(
      DEX_OUTPUT_CONTAINER_TYPE.is(Container.ZIP));

  @Nonnull
  public static final BooleanPropertyId ENABLE_COMPILED_FILES_STATISTICS = BooleanPropertyId.create(
      "jack.statistic.source", "Enable compiled files statistics").addDefaultValue(
      Boolean.FALSE);

  @Option(name = "--version", usage = "display version")
  protected boolean version;

  @Option(name = "--help", usage = "display help")
  protected boolean help;

  @Option(name = "--help-properties", usage = "display properties list")
  protected boolean helpProperties;

  protected boolean dumpProperties;

  @Option(name = "-D", metaVar = "<property>=<value>",
      usage = "set value for the given property (repeatable)",
      handler = MapOptionHandler.class)
  @Nonnull
  protected final Map<String, String> properties = new HashMap<String, String>();

  protected final File propertiesFile = null;

  /**
   * Jack verbosity level
   */
  public enum VerbosityLevel {
    ERROR("error"), WARNING("warning"), INFO("info"), DEBUG("debug"), TRACE("trace");

    @Nonnull
    private final String id;

    VerbosityLevel(@Nonnull String id) {
      this.id = id;
    }

    public String getId() {
      return id;
    }
  }

  @Option(name = "--verbose", usage = "set verbosity (default: warning)",
      metaVar = "[error | warning | info | debug | trace]")
  protected VerbosityLevel verbose = VerbosityLevel.WARNING;

  @Option(name = "--disable-automatic-full-rebuild")
  protected boolean disableAutomaticFullRebuild = false;

  @Option(name = "--incremental-folder", usage = "Folder used for incremental data",
      metaVar = "FILE")
  protected File incrementalFolder = null;

  @Option(name = "--output-dex", usage = "output dex file(s) to this folder",
      metaVar = "DIRECTORY")
  protected File out = null;

  @Option(name = "--output-dex-zip", usage = "output to this zip file", metaVar = "FILE")
  protected File outZip = null;

  @Option(name = "--output-jack-dir", usage = "output jack library to this folder",
      metaVar = "DIRECTORY")
  protected File libraryOutDir = null;

  @Option(name = "--output-jack", usage = "output jack library file", metaVar = "FILE")
  protected File libraryOutZip = null;

  @Option(name = "--config-jarjar", usage = "use this jarjar rules file (default: none)",
      metaVar = "FILE")
  protected File jarjarRulesFile = null;

  @Option(name = "--import", usage = "import the given file into the output (repeatable)",
      metaVar = "FILE")
  protected List<File> jayceImport = new ArrayList<File>();

  @Option(name = "--import-resource",
      usage = "import the given directory into the output as resource files (repeatable)",
      metaVar = "DIRECTORY")
  protected List<File> resImport = new ArrayList<File>();

  @Option(name = "--import-meta",
      usage = "import the given directory into the output as meta-files (repeatable)",
      metaVar = "DIRECTORY")
  protected List<File> metaImport = new ArrayList<File>();

  @Option(name = "--dx-legacy", usage = "keep generation close to dx (default: on)",
      handler = ExplicitBooleanOptionHandler.class, metaVar = "[on | off]")
  protected boolean dxLegacy = true;

  @Option(name = "--runtime-legacy",
      usage = "keep generation compatible with older runtime (default: on)",
      handler = ExplicitBooleanOptionHandler.class, metaVar = "[on | off]")
  protected boolean runtimeLegacy = true;

  @Option(name = "--config-proguard", usage = "use these proguard flags file (default: none)",
      metaVar = "FILE")
  protected List<File> proguardFlagsFiles = null;

  @Option(name = "--sanity-checks", usage = "enable/disable compiler sanity checks (default: on)",
      handler = ExplicitBooleanOptionHandler.class, metaVar = "[on | off]")
  protected boolean sanityChecks = true;
  @Nonnull
  public static final BooleanPropertyId SANITY_CHECKS = BooleanPropertyId.create(
      "jack.sanitychecks", "enable/disable compiler sanity checks")
      .addDefaultValue(Boolean.TRUE);

  @Option(name = "--tracer-dir", usage = "enable tracer and output into this dir (.html)",
      metaVar = "DIRECTORY")
  protected File tracerDir;

  @Option(name = "--graph-file")
  protected File graphFile;

  @Option(name = "-cp", aliases = "--classpath", usage = "classpath", metaVar = "PATH")
  protected String classpath = null;

  @Argument
  protected List<String> ecjArguments;

  @Nonnull
  private static final String ECJ_HELP_ARG = "-help";

  @Option(name = "-g", usage = "emit debug infos")
  protected boolean emitLocalDebugInfo = false;

  /**
   * Available mode for the multidex feature
   */
  public enum MultiDexKind {
    NONE,
    NATIVE,
    LEGACY
  }

  @Option(name = "--multi-dex",
      usage = "whether to split code into multiple dex (default: none)",
      metaVar = "[none | native | legacy]")
  protected MultiDexKind multiDexKind = MultiDexKind.NONE;

  @Nonnull
  public static final BooleanPropertyId EMIT_LOCAL_DEBUG_INFO = BooleanPropertyId.create(
      "jack.dex.debug.vars", "Emit local variable debug info into generated dex")
      .addDefaultValue(Boolean.FALSE);

  @Nonnull
  public static final BooleanPropertyId EMIT_JACK_FLAG = BooleanPropertyId.create(
      "jack.internal.jackflag", "Emit jack flag into generated dex")
      .addDefaultValue(Boolean.FALSE);

  protected boolean emitSyntheticDebugInfo = false;

  @Nonnull
  public static final BooleanPropertyId EMIT_LINE_NUMBER_DEBUG_INFO = BooleanPropertyId.create(
      "jack.dex.debug.lines", "Emit line number debug info into generated dex")
      .addDefaultValue(Boolean.TRUE);

  @Nonnull
  public static final BooleanPropertyId EMIT_SOURCE_FILE_DEBUG_INFO = BooleanPropertyId.create(
      "jack.dex.debug.source", "Emit source file debug info into generated dex")
      .addDefaultValue(Boolean.TRUE);

  protected boolean keepMethodBody = false;

  @Nonnull
  public static final BooleanPropertyId SHROB_ENABLED =
      BooleanPropertyId.create("jack.shrob", "Enable shrink and obfuscation features")
      .addDefaultValue(false);

  @CheckForNull
  protected Flags flags = null;

  @Nonnull
  public static final ObjectId<Flags> FLAGS = new ObjectId<Flags>("jack.shrob.flags", Flags.class);

  @Nonnull
  public static final BooleanPropertyId USE_MIXED_CASE_CLASSNAME = BooleanPropertyId.create(
      "jack.obfuscation.mixedcaseclassname",
      "Use mixed case class name when obfuscating").addDefaultValue(Boolean.FALSE);

  @Nonnull
  protected Filter<JMethod> filter = new AllMethods();

  @SuppressWarnings("unchecked")
  @Nonnull
  public static final ImplementationPropertyId<Filter<JMethod>> METHOD_FILTER =
      (ImplementationPropertyId<Filter<JMethod>>) (Object) ImplementationPropertyId.create(
          "jack.internal.filter.method", "Define which filter will be used for methods",
          Filter.class).addDefaultValue("all-methods");

  public VerbosityLevel getVerbosityLevel() {
    return verbose;
  }

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

  public boolean askForEcjHelp() {
    return ecjArguments != null && ecjArguments.contains(ECJ_HELP_ARG);
  }

  @Nonnull
  public File getOutputDir() {
    return out;
  }

  public void setOutputDir(File out) {
    this.out = out;
  }

  public void setOutputZip(File out) {
    this.outZip = out;
  }

  boolean hasSanityChecks() {
    return sanityChecks;
  }

  @CheckForNull
  public String getClasspathAsString() {
    return classpath;
  }

  @Nonnull
  public List<File> getClasspath() {
    return getFilesFromPathString(classpath);
  }

  @Nonnull
  private List<File> getFilesFromPathString(@CheckForNull String pathString) {
    List<File> classpath = new ArrayList<File>();
    if (pathString != null && !pathString.isEmpty()) {
      String[] paths = pathString.split(File.pathSeparator);
      for (String path : paths) {
        classpath.add(new File(path));
      }
    }
    return classpath;
  }

  @CheckForNull
  private Config config = null;

  @Nonnull
  public Config getConfig() {
    assert config != null;

    return config;
  }

  @Nonnull
  public GatherConfigBuilder getDefaultConfigBuilder() throws IOException {
    GatherConfigBuilder configBuilder = new GatherConfigBuilder();
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

      configBuilder = new GatherConfigBuilder();
      if (sanityChecks) {
        configBuilder.setDebug();
      }

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

    if (jarjarRulesFile != null) {
      configBuilder.set(PackageRenamer.JARJAR_FILE, jarjarRulesFile);
    }

    configBuilder.pushDefaultLocation(new StringLocation("proguard flags"));

    if (flags != null) {
      configBuilder.set(SHROB_ENABLED, true);
      configBuilder.set(AnnotationRemover.EMIT_RUNTIME_INVISIBLE_ANNOTATION,
          flags.keepAttribute("RuntimeInvisibleAnnotations"));
      configBuilder.set(AnnotationRemover.EMIT_RUNTIME_VISIBLE_ANNOTATION,
          flags.keepAttribute("RuntimeVisibleAnnotations"));
      configBuilder.set(ParameterAnnotationRemover.EMIT_RUNTIME_VISIBLE_PARAMETER_ANNOTATION,
          flags.keepAttribute("RuntimeVisibleParameterAnnotations"));
      configBuilder.set(ParameterAnnotationRemover.EMIT_RUNTIME_INVISIBLE_PARAMETER_ANNOTATION,
          flags.keepAttribute("RuntimeInvisibleParameterAnnotations"));
      configBuilder.set(EMIT_LINE_NUMBER_DEBUG_INFO,
          flags.keepAttribute("LineNumberTable"));
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

    configBuilder.set(EMIT_LOCAL_DEBUG_INFO, emitLocalDebugInfo);
    configBuilder.set(
        CodeItemBuilder.EMIT_SYNTHETIC_LOCAL_DEBUG_INFO, emitSyntheticDebugInfo);

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
      configBuilder.set(GENERATE_JACK_LIBRARY, true);
      configBuilder.set(LIBRARY_OUTPUT_CONTAINER_TYPE, Container.DIR);
      configBuilder.set(Options.LIBRARY_OUTPUT_DIR,
          new DirectVFS(createTempDirForTypeDexFiles(hooks)));
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
    } else if (out != null) {
      configBuilder.setString(DEX_OUTPUT_DIR, out.getPath());
      configBuilder.set(DEX_OUTPUT_CONTAINER_TYPE, Container.DIR);
      configBuilder.set(GENERATE_DEX_FILE, true);
    }
    configBuilder.set(FieldInitializerRemover.CLASS_AS_INITIALVALUE, !dxLegacy);
    configBuilder.set(
        FieldInitializerRemover.STRING_AS_INITIALVALUE_OF_OBJECT, !runtimeLegacy);

    if (incrementalFolder != null) {
      if (multiDexKind == MultiDexKind.LEGACY) {
        LoggerFactory.getLogger().log(Level.INFO,
            "Incremental mode is disable due to multi-dex legacy mode");
      } else {
        configBuilder.set(Options.INCREMENTAL_MODE, true);
        configBuilder.setString(Options.INPUT_FILTER.getName(), "incremental");
        configBuilder.set(Options.GENERATE_JACK_LIBRARY, true);
        configBuilder.set(GENERATE_JAYCE_IN_LIBRARY, true);
        configBuilder.set(GENERATE_DEPENDENCIES_IN_LIBRARY, true);
        configBuilder.setString(Options.LIBRARY_OUTPUT_CONTAINER_TYPE.getName(), "dir");
        configBuilder.setString(Options.LIBRARY_OUTPUT_DIR.getName(), incrementalFolder.getPath());
      }
    }

    if (tracerDir != null) {
      configBuilder.setString(TracerFactory.TRACER, "html");
      configBuilder.setString(StatsTracerFtl.TRACER_DIR, tracerDir.getPath());
    }

    configBuilder.set(SANITY_CHECKS, sanityChecks);

    if (dumpProperties) {
      configBuilder.setString(ConfigPrinterFactory.CONFIG_PRINTER, "properties-file");
    }

    configBuilder.popDefaultLocation();

    for (Entry<String, String> entry : properties.entrySet()) {
      configBuilder.setString(entry.getKey(), entry.getValue(), new StringLocation("-D option"));
    }

    configBuilder.processEnvironmentVariables("JACK_CONFIG_");
    configBuilder.setHooks(hooks);

    return configBuilder;
  }

  public void checkValidity(@Nonnull RunnableHooks hooks)
      throws IllegalOptionsException, NothingToDoException, ConfigurationException {
    config = getConfigBuilder(hooks).build();

    LoggerFactory.loadLoggerConfiguration(
        this.getClass(), "/" + getVerbosityLevel().getId() + ".jack.logging.properties");

    // Check ecj arguments
    if (ecjArguments != null) {
      if (getVerbosityLevel() == VerbosityLevel.ERROR) {
        ecjArguments.add(0, "-nowarn");
      }
      ecjArguments.add(0, "-source");
      assert config != null;
      ecjArguments.add(1, config.get(Options.JAVA_SOURCE_VERSION).toString());

      // TODO(jplesot) Rework to avoid re-instantiate compiler. Use our own command line parser.
      org.eclipse.jdt.internal.compiler.batch.Main compiler =
          new org.eclipse.jdt.internal.compiler.batch.Main(
              new PrintWriter(System.out), new PrintWriter(System.err),
              false /* exit */, null /* options */
              , null /* compilationProgress */
          );

      try {
        compiler.configure(ecjArguments.toArray(new String[ecjArguments.size()]));
        if (!compiler.proceed && incrementalFolder == null) {
          throw new NothingToDoException();
        }
        compiler.getLibraryAccess().cleanup();
      } catch (IllegalArgumentException e) {
        throw new IllegalOptionsException(e.getMessage(), e);
      }
    }

    // Check Jack arguments
    if (emitSyntheticDebugInfo && !emitLocalDebugInfo) {
      throw new IllegalOptionsException(
          "Impossible to emit synthetic debug info when not emitting debug info");
    }

    if (libraryOutDir != null) {
      if (!libraryOutDir.exists()) {
        throw new IllegalOptionsException("The specified output folder '"
            + libraryOutDir.getPath() + "' for jayce files does not exist.");
      }

      if (!libraryOutDir.canWrite()) {
        throw new IllegalOptionsException("The specified output folder '"
            + libraryOutDir.getPath() + "' for jayce files cannot be written to.");
      }
    }

    if (jarjarRulesFile != null) {
      if (!jarjarRulesFile.exists()) {
        throw new IllegalOptionsException("The specified rules file '"
            + jarjarRulesFile.getPath() + "' for package renaming does not exist.");
      }

      if (!jarjarRulesFile.canRead()) {
        throw new IllegalOptionsException("The specified rules file '"
            + jarjarRulesFile.getPath() + "' for package renaming cannot be read.");
      }
    }
  }

  public void setJayceOutputDir(@Nonnull File outputDir) {
    libraryOutDir = outputDir;
  }

  public void setJayceOutputZip(@Nonnull File outputZip) {
    libraryOutZip = outputZip;
  }

  public void setJayceImports(@Nonnull List<File> imports) {
    jayceImport = imports;
  }

  public boolean outputToZip() {
    return outZip != null || libraryOutZip != null;
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
      jayceImport = new ArrayList<File>(inJars.size());
      jayceImport.addAll(inJars);
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
      if (classpath == null) {
        classpath = libraryJars;
      } else {
        classpath += File.pathSeparatorChar + libraryJars;
      }
    }
  }

  public void setClasspath(String classpath) {
    this.classpath = classpath;
  }

  public void addProguardFlagsFile(@Nonnull File flags) {
    if (proguardFlagsFiles == null) {
      proguardFlagsFiles = new ArrayList<File>();
    }
    proguardFlagsFiles.add(flags);
  }

  public void addJayceImport(@Nonnull File importFile) {
    jayceImport.add(importFile);
  }

  public void addProperty(@Nonnull String propertyName, @Nonnull String propertyValue) {
    properties.put(propertyName, propertyValue);
  }

  @Nonnull
  public List<String> getEcjArguments() {
    if (ecjArguments == null) {
      return Collections.emptyList();
    }

    return ecjArguments;
  }

  public void setEcjArguments(@Nonnull List<String> ecjArguments) {
    this.ecjArguments = ecjArguments;
  }

  public void setProguardFlagsFile(@Nonnull List<File> proguardFlagsFiles) {
    this.proguardFlagsFiles = proguardFlagsFiles;
  }

  @CheckForNull
  public File getJarjarRulesFile() {
    return jarjarRulesFile;
  }

  public void setJarjarRulesFile(@Nonnull File jarjarRulesFile) {
    this.jarjarRulesFile = jarjarRulesFile;
  }

  public void setNameProvider(@Nonnull String nameProvider) {
    properties.put(NameProviderFactory.NAMEPROVIDER.getName(), nameProvider);
  }

  public void enableDxOptimizations() {
    properties.put(CodeItemBuilder.DEX_OPTIMIZE.getName(), "true");
  }

  public void disableDxOptimizations() {
    properties.put(CodeItemBuilder.DEX_OPTIMIZE.getName(), "false");
  }

  @Nonnull
  public List<File> getJayceImport() {
    return jayceImport;
  }

  @CheckForNull
  public File getIncrementalFolder() {
    return incrementalFolder;
  }

  public void setIncrementalFolder(@Nonnull File incrementalFolder) {
    this.incrementalFolder = incrementalFolder;
  }

  public boolean isAutomaticFullRebuildEnabled() {
    return !disableAutomaticFullRebuild;
  }

  public void addResource(@Nonnull File resource) {
    resImport.add(resource);
  }

  @Nonnull
  private static Directory createTempDirForTypeDexFiles(
      @Nonnull RunnableHooks hooks) {
    try {
      File tmp = Files.createTempDir();
      Directory dir = new Directory(tmp.getPath(), hooks, Existence.MUST_EXIST, Permission.WRITE,
          ChangePermission.NOCHANGE);
      hooks.addHook(new TypeDexDirDeleter(dir));
      return dir;
    } catch (IOException e) {
      throw new JackUserException(e);
    }
  }

  private static class TypeDexDirDeleter extends Thread {

    @Nonnull
    private final Directory dir;

    public TypeDexDirDeleter(@Nonnull Directory dir) {
      this.dir = dir;
    }

    @Override
    public void run() {
      try {
        FileUtils.deleteDir(dir.getFile());
      } catch (IOException e) {
        throw new JackIOException("Failed to delete temporary directory " + dir, e);
      }
    }
  }
}
