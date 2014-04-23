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

import com.android.jack.backend.dex.FieldInitializerRemover;
import com.android.jack.backend.dex.annotations.DefaultValueAnnotationAdder;
import com.android.jack.backend.dex.annotations.ReflectAnnotationsAdder;
import com.android.jack.backend.dex.rop.CodeItemBuilder;
import com.android.jack.config.id.JavaVersionPropertyId;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.shrob.SeedPrinter;
import com.android.jack.shrob.obfuscation.MappingPrinter;
import com.android.jack.shrob.obfuscation.NameProviderFactory;
import com.android.jack.shrob.obfuscation.Renamer;
import com.android.jack.shrob.obfuscation.annotation.AnnotationRemover;
import com.android.jack.shrob.obfuscation.annotation.ParameterAnnotationRemover;
import com.android.jack.shrob.shrink.TypeAndMemberLister;
import com.android.jack.shrob.spec.Flags;
import com.android.jack.transformations.renamepackage.PackageRenamer;
import com.android.jack.util.filter.AllMethods;
import com.android.jack.util.filter.Filter;
import com.android.sched.util.RunnableHooks;
import com.android.sched.util.codec.DirectoryCodec;
import com.android.sched.util.codec.PathCodec;
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
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.location.StringLocation;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.tracer.StatsTracerFtl;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.ExplicitBooleanOptionHandler;
import org.kohsuke.args4j.spi.MapOptionHandler;
import org.kohsuke.args4j.spi.StopOptionHandler;

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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
/**
 * Jack command line options Bean
 */
@HasKeyId
public class Options {

  /**
   * Container types.
   */
  public enum Container {
    FILE,
    DIR,
    ZIP
  }

  @Nonnull
  public static final
      JavaVersionPropertyId JAVA_SOURCE_VERSION = JavaVersionPropertyId.create(
          "jack.java.source.version", "Java source version").addDefaultValue("1.6");

  @Nonnull
  public static final BooleanPropertyId GENERATE_DEX_FILE = BooleanPropertyId.create(
      "jack.dex.generate", "Generate dex file").addDefaultValue(Boolean.FALSE);

  @Nonnull
  public static final BooleanPropertyId GENERATE_JACK_FILE = BooleanPropertyId.create(
      "jack.jackfile.generate", "Generate jack files").addDefaultValue(Boolean.FALSE);

  @Nonnull
  public static final EnumPropertyId<Container> DEX_OUTPUT_CONTAINER_TYPE = EnumPropertyId.create(
      "jack.dex.output.container", "Output container type", Container.values())
      .ignoreCase().requiredIf(GENERATE_DEX_FILE.getValue().isTrue());

  @Nonnull
  public static final EnumPropertyId<Container> JACK_OUTPUT_CONTAINER_TYPE = EnumPropertyId.create(
      "jack.jackfile.output.container", "Output container type", Container.values())
      .ignoreCase().requiredIf(GENERATE_JACK_FILE.getValue().isTrue());

  @Nonnull
  public static final
      PropertyId<File> JACK_FILE_OUTPUT_ZIP = PropertyId.create("jack.jackfile.output.zip",
          "Output zip archive for jack files", new PathCodec()).requiredIf(
          GENERATE_JACK_FILE.getValue().isTrue().and(JACK_OUTPUT_CONTAINER_TYPE.is(Container.ZIP)));

  @Nonnull
  public static final PropertyId<Directory> JACK_FILE_OUTPUT_DIR = PropertyId.create(
      "jack.jackfile.output.dir", "Output folder for jack files",
      new DirectoryCodec(Existence.MAY_EXIST, Permission.READ | Permission.WRITE))
      .requiredIf(GENERATE_JACK_FILE.getValue().isTrue()
          .and(JACK_OUTPUT_CONTAINER_TYPE.is(Container.DIR)));

  @Nonnull
  public static final PropertyId<File> DEX_FILE_OUTPUT = PropertyId.create(
      "jack.dex.output", "Dex output file", new PathCodec())
      .requiredIf(GENERATE_DEX_FILE.getValue().isTrue());

  @Option(name = "-v", aliases = "--version", usage = "display options")
  protected boolean version;

  @Option(name = "-h", aliases = "--help", usage = "display help")
  protected boolean help;

  @Option(name = "--help-properties", usage = "display properties list")
  protected boolean helpProperties;

  @Option(name = "--dump-properties", usage = "dump properties with their value")
  protected boolean dumpProperties;

  @Option(name = "-D", metaVar = "<property>=<value>",
      usage = "set value for the given property (see --help-properties)",
      handler = MapOptionHandler.class)
  @Nonnull
  protected final Map<String, String> properties = new HashMap<String, String>();

  @Option(
      name = "-c", aliases = "--config-file", metaVar = "FILE", usage = "set properties from file")
  protected final File propertiesFile = null;

  enum VerbosityLevel {
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

  @Option(name = "-o", aliases = "--output",
      usage = "output to this dex file (default: ./classes.dex)", metaVar = "FILE")
  protected File out = new File("./classes.dex");

  @Option(name = "--output-zip", usage = "output to this zip file", metaVar = "FILE")
  protected File outZip = null;

  @Option(name = "-d", aliases = "--jack-output", usage = "output jack files to this folder",
      metaVar = "DIRECTORY")
  protected File jayceOutDir = null;

  @Option(name = "--jack-output-zip", usage = "output jack files to this zip", metaVar = "FILE")
  protected File jayceOutZip = null;

  @Option(name = "--jarjar-rules", usage = "use this jarjar rules file (default: none)",
      metaVar = "FILE")
  protected File jarjarRulesFile = null;

  @Option(name = "-i", aliases = "--import-jack", usage = "Import the given jack files",
      metaVar = "FILE")
  protected List<File> jayceImport = new ArrayList<File>();

  @Option(name = "--dx-legacy", usage = "keep generation close to dx (default: on)",
      handler = ExplicitBooleanOptionHandler.class, metaVar = "[on | off]")
  protected boolean dxLegacy = true;

  @Option(name = "--runtime-legacy",
      usage = "keep generation compatible with older runtime (default: on)",
      handler = ExplicitBooleanOptionHandler.class, metaVar = "[on | off]")
  protected boolean runtimeLegacy = true;

  @Option(name = "--proguard-flags", usage = "use these proguard flags files (default: none)",
      metaVar = "FILES")
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

  @Option(name = "--bootclasspath", usage = "bootclasspath", metaVar = "PATH")
  protected String bootclasspath = null;

  @Argument
  @Option(name = "--ecj", usage = "mark the beginning of ecj options (--ecj -help for help)",
      metaVar = "...", handler = StopOptionHandler.class)
  protected List<String> ecjArguments;

  @Nonnull
  private static final String ECJ_HELP_ARG = "-help";

  @Option(name = "-g", aliases = "--debug", usage = "emit debug infos")
  protected boolean emitLocalDebugInfo = false;

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

  @CheckForNull
  protected Flags flags = null;

  @Nonnull
  public static final ObjectId<Flags> FLAGS =
      new ObjectId<Flags>("jack.shrink.flags", Flags.class);

  @Nonnull
  public static final BooleanPropertyId USE_MIXED_CASE_CLASSNAME = BooleanPropertyId.create(
      "jack.obfuscation.mixedcaseclassname",
      "Use mixed case class name when obfuscating").addDefaultValue(Boolean.FALSE);

  protected File typeAndMemberListing;

  @CheckForNull
  public String nameProvider = null;

  @Nonnull
  protected Filter<JMethod> filter = new AllMethods();

  @SuppressWarnings("unchecked")
  @Nonnull
  public static final ImplementationPropertyId<Filter<JMethod>> METHOD_FILTER =
      (ImplementationPropertyId<Filter<JMethod>>) (Object) ImplementationPropertyId.create(
          "jack.internal.filter.method", "Define which filter will be used for methods",
          Filter.class).addDefaultValue("all-methods");

  //
  // Getter
  //

  public VerbosityLevel getVerbosityLevel() {
    return verbose;
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
  public File getOutputFile() {
    return out;
  }

  public void setOutputFile(File out) {
    this.out = out;
  }

  boolean hasSanityChecks() {
    return sanityChecks;
  }

  @Nonnull
  public List<File> getClasspath() {
    return getFilesFromPathString(classpath);
  }

  @Nonnull
  public List<File> getBootclasspath() {
    return getFilesFromPathString(bootclasspath);
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
  public GatherConfigBuilder getConfigBuilder()
      throws IllegalOptionsException {
    GatherConfigBuilder configBuilder;

    if (propertiesFile != null) {
      if (!propertiesFile.exists()) {
        throw new IllegalOptionsException(
            "The specified config file '" + propertiesFile.getAbsolutePath() + "' does not exist.");
      }

      if (!propertiesFile.isFile()) {
        throw new IllegalOptionsException(
            "The specified config file '" + propertiesFile.getAbsolutePath() + "' is not a file.");
      }

      if (!propertiesFile.canRead()) {
        throw new IllegalOptionsException(
            "The specified config file '" + propertiesFile.getAbsolutePath() + "' cannot be read.");
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
            "The specified config file '" + propertiesFile.getAbsolutePath() + "' cannot be read.",
            e.getCause());
      }
    } else {
      try {
        configBuilder = getDefaultConfigBuilder();
      } catch (IOException e) {
        throw new IllegalOptionsException(e.getMessage(), e);
      }
    }

    for (Entry<String, String> entry : properties.entrySet()) {
      configBuilder.setString(entry.getKey(), entry.getValue(), new StringLocation("-D option"));
    }

    configBuilder.pushDefaultLocation(new StringLocation("Options"));

    if (jarjarRulesFile != null) {
      configBuilder.set(PackageRenamer.JARJAR_FILE, jarjarRulesFile);
    }

    configBuilder.pushDefaultLocation(new StringLocation("proguard flags"));

    if (flags != null) {
      configBuilder.set(ReflectAnnotationsAdder.EMIT_ANNOTATION_SIG,
          flags.keepAttribute("Signatures"));
      configBuilder.set(ReflectAnnotationsAdder.EMIT_ANNOTATION_THROWS,
          flags.keepAttribute("Exceptions"));
      configBuilder.set(ReflectAnnotationsAdder.EMIT_ANNOTATION_MEMBER_CLASSES,
          flags.keepAttribute("InnerClasses"));
      configBuilder.set(ReflectAnnotationsAdder.EMIT_ANNOTATION_ENCLOSING_METHOD,
          flags.keepAttribute("EnclosingMethod"));
      configBuilder.set(DefaultValueAnnotationAdder.EMIT_ANNOTATION_DEFAULT,
          flags.keepAttribute("AnnotationDefault"));
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
        configBuilder.setString(Renamer.MAPPING_FILE, mapping.getAbsolutePath());
      } else {
        configBuilder.set(Renamer.USE_MAPPING, false);
      }

      File seeds = flags.getSeedsFile();
      if (seeds != null) {
        configBuilder.setString(SeedPrinter.SEEDS_OUTPUT_FILE, seeds.getAbsolutePath());
      }

      File dictionary = flags.getObfuscationDictionary();
      if (dictionary != null) {
        configBuilder.set(Renamer.USE_OBFUSCATION_DICTIONARY, true);
        configBuilder.setString(Renamer.OBFUSCATION_DICTIONARY, dictionary.getAbsolutePath());
      } else {
        configBuilder.set(Renamer.USE_OBFUSCATION_DICTIONARY, false);
      }

      File classDictionary = flags.getClassObfuscationDictionary();
      if (classDictionary != null) {
        configBuilder.set(Renamer.USE_CLASS_OBFUSCATION_DICTIONARY, true);
        configBuilder.setString(Renamer.CLASS_OBFUSCATION_DICTIONARY,
            classDictionary.getAbsolutePath());
      } else {
        configBuilder.set(Renamer.USE_CLASS_OBFUSCATION_DICTIONARY, false);
      }

      File packageDictionary = flags.getPackageObfuscationDictionary();
      if (packageDictionary != null) {
        configBuilder.set(Renamer.USE_PACKAGE_OBFUSCATION_DICTIONARY, true);
        configBuilder.setString(
            Renamer.PACKAGE_OBFUSCATION_DICTIONARY, packageDictionary.getAbsolutePath());
      } else {
        configBuilder.set(Renamer.USE_PACKAGE_OBFUSCATION_DICTIONARY, false);
      }
      File outputmapping = flags.getOutputMapping();
      if (outputmapping != null) {
        configBuilder.setString(MappingPrinter.MAPPING_OUTPUT_FILE,
            outputmapping.getAbsolutePath());
      }
      if (nameProvider != null) {
        configBuilder.setString(NameProviderFactory.NAMEPROVIDER, nameProvider);
      } else {
        if (flags.getUseMixedCaseClassName()) {
          configBuilder.setString(NameProviderFactory.NAMEPROVIDER, "mixed-case");
        }
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
    }

    configBuilder.popDefaultLocation();

    configBuilder.set(EMIT_LOCAL_DEBUG_INFO, emitLocalDebugInfo);
    configBuilder.set(
        CodeItemBuilder.EMIT_SYNTHETIC_LOCAL_DEBUG_INFO, emitSyntheticDebugInfo);

    if (typeAndMemberListing != null) {
      configBuilder.set(TypeAndMemberLister.TYPE_AND_MEMBER_LISTING, true);
      configBuilder.setString(
          TypeAndMemberLister.TYPE_AND_MEMBER_LISTING_FILE, typeAndMemberListing.getAbsolutePath());
    }

    if (jayceOutZip != null) {
      configBuilder.setString(JACK_FILE_OUTPUT_ZIP, jayceOutZip.getAbsolutePath());
      configBuilder.set(JACK_OUTPUT_CONTAINER_TYPE, Container.ZIP);
      configBuilder.set(GENERATE_JACK_FILE, true);
    } else if (jayceOutDir != null) {
      configBuilder.setString(JACK_FILE_OUTPUT_DIR, jayceOutDir.getAbsolutePath());
      configBuilder.set(JACK_OUTPUT_CONTAINER_TYPE, Container.DIR);
      configBuilder.set(GENERATE_JACK_FILE, true);
    } else if (outZip != null) {
      configBuilder.setString(DEX_FILE_OUTPUT, outZip.getAbsolutePath());
      configBuilder.set(DEX_OUTPUT_CONTAINER_TYPE, Container.ZIP);
      configBuilder.set(GENERATE_DEX_FILE, true);
    } else {
      configBuilder.setString(DEX_FILE_OUTPUT, out.getAbsolutePath());
      configBuilder.set(DEX_OUTPUT_CONTAINER_TYPE, Container.FILE);
      configBuilder.set(GENERATE_DEX_FILE, true);
    }
    configBuilder.set(FieldInitializerRemover.CLASS_AS_INITIALVALUE, !dxLegacy);
    configBuilder.set(
        FieldInitializerRemover.STRING_AS_INITIALVALUE_OF_OBJECT, !runtimeLegacy);

    if (tracerDir != null) {
      configBuilder.setString(TracerFactory.TRACER, "html");
      configBuilder.setString(StatsTracerFtl.TRACER_DIR, tracerDir.getAbsolutePath());
    }

    configBuilder.set(SANITY_CHECKS, sanityChecks);

    if (dumpProperties) {
      configBuilder.setString(ConfigPrinterFactory.CONFIG_PRINTER, "properties-file");
    }

    configBuilder.popDefaultLocation();

    configBuilder.processEnvironmentVariables("JACK_CONFIG_");

    return configBuilder;
  }

  public void checkValidity(@Nonnull RunnableHooks hooks)
      throws IllegalOptionsException, NothingToDoException, ConfigurationException {
    config = getConfigBuilder().setHooks(hooks).build();

    LoggerFactory.loadLoggerConfiguration(
        this.getClass(), "/" + getVerbosityLevel().getId() + ".jack.logging.properties");

    // Check ecj arguments
    if (ecjArguments != null) {
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
        if (!compiler.proceed) {
          throw new NothingToDoException();
        }
      } catch (IllegalArgumentException e) {
        throw new IllegalOptionsException(e.getMessage(), e);
      }
    }

    // Check Jack arguments
    if (emitSyntheticDebugInfo && !emitLocalDebugInfo) {
      throw new IllegalOptionsException(
          "Impossible to emit synthetic debug info when not emitting debug info");
    }

    if (jayceOutDir != null) {
      if (!jayceOutDir.exists()) {
        throw new IllegalOptionsException("The specified output folder '"
            + jayceOutDir.getAbsolutePath() + "' for jack files does not exist.");
      }

      if (!jayceOutDir.canWrite()) {
        throw new IllegalOptionsException("The specified output folder '"
            + jayceOutDir.getAbsolutePath() + "' for jack files cannot be written to.");
      }
    }

    if (jarjarRulesFile != null) {
      if (!jarjarRulesFile.exists()) {
        throw new IllegalOptionsException("The specified rules file '"
            + jarjarRulesFile.getAbsolutePath() + "' for package renaming does not exist.");
      }

      if (!jarjarRulesFile.canRead()) {
        throw new IllegalOptionsException("The specified rules file '"
            + jarjarRulesFile.getAbsolutePath() + "' for package renaming cannot be read.");
      }
    }
  }

  public void setJayceOutputDir(@Nonnull File outputDir) {
    jayceOutDir = outputDir;
  }

  public void setJayceOutputZip(@Nonnull File outputZip) {
    jayceOutZip = outputZip;
  }

  public void setJayceImports(@Nonnull List<File> imports) {
    jayceImport = imports;
  }

  public boolean outputToZip() {
    return outZip != null || jayceOutZip != null;
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
        jayceOutDir = outJar;
      } else {
        jayceOutZip = outJar;
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

  public void enableDxOptimizations() {
    properties.put(CodeItemBuilder.DEX_OPTIMIZE.getName(), "true");
  }

  public void disableDxOptimizations() {
    properties.put(CodeItemBuilder.DEX_OPTIMIZE.getName(), "false");
  }
}
