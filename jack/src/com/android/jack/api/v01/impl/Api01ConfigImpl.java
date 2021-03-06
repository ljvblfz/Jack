/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.api.v01.impl;

import com.google.common.base.Joiner;

import com.android.jack.IllegalOptionsException;
import com.android.jack.Jack;
import com.android.jack.JackAbortException;
import com.android.jack.JackUserException;
import com.android.jack.Options;
import com.android.jack.api.impl.ApiFeature;
import com.android.jack.api.impl.JackConfigImpl;
import com.android.jack.api.v01.Api01CompilationTask;
import com.android.jack.api.v01.Api01Config;
import com.android.jack.api.v01.CompilationException;
import com.android.jack.api.v01.ConfigurationException;
import com.android.jack.api.v01.DebugInfoLevel;
import com.android.jack.api.v01.JavaSourceVersion;
import com.android.jack.api.v01.MultiDexKind;
import com.android.jack.api.v01.ReporterKind;
import com.android.jack.api.v01.ResourceCollisionPolicy;
import com.android.jack.api.v01.TypeCollisionPolicy;
import com.android.jack.api.v01.UnrecoverableException;
import com.android.jack.api.v01.VerbosityLevel;
import com.android.jack.api.v04.impl.Api04ConfigImpl;
import com.android.jack.config.id.JavaVersionPropertyId.JavaVersion;
import com.android.jack.frontend.FrontendCompilationException;
import com.android.jack.reporting.Reporter;
import com.android.jack.resource.ResourceImporter;
import com.android.jack.shrob.obfuscation.MappingPrinter;
import com.android.sched.scheduler.ProcessException;
import com.android.sched.util.RunnableHooks;

import java.io.File;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A configuration implementation for API level 01 of the Jack compiler.
 */
public class Api01ConfigImpl implements JackConfigImpl, Api01Config {

  @Nonnull
  protected final Options options;
  @CheckForNull
  private Class<? extends ApiFeature> api;

  public Api01ConfigImpl() {
    options = new Options();
  }

  @Override
  @Nonnull
  public Api01CompilationTask getTask() throws ConfigurationException {
    assert api != null;

    RunnableHooks configHooks = new RunnableHooks();
    try {
      Jack.check(api, options, configHooks);
    } catch (com.android.sched.util.config.ConfigurationException e) {
      try {
        configHooks.runHooks();
      } catch (Error | RuntimeException hooksExn) {
        // Ignored since already logged by RunnableHooks.
      }
      throw new ConfigurationException(e.getMessage(), e);
    } catch (IllegalOptionsException e) {
      try {
        configHooks.runHooks();
      } catch (Error | RuntimeException hooksExn) {
        // Ignored since already logged by RunnableHooks.
      }
      throw new ConfigurationException(e.getMessage(), e);
    }

    return new Api01CompilationTaskImpl(api, options, configHooks);
  }

  private static class Api01CompilationTaskImpl implements Api01CompilationTask {
    @Nonnull
    private final Class<? extends ApiFeature> api;
    @Nonnull
    private final Options options;
    @Nonnull
    private final RunnableHooks runSessionHooks;

    public Api01CompilationTaskImpl(
        @Nonnull Class<? extends ApiFeature> api,
        @Nonnull Options options,
        @Nonnull RunnableHooks runSessionHooks) {
      this.api = api;
      this.options = options;
      this.runSessionHooks = runSessionHooks;
    }

    @Override
    public void run() throws CompilationException, UnrecoverableException {
      ProcessException pe = null;

      try {

        try {
          try {
            Jack.run(api, options, runSessionHooks);
          } catch (ProcessException e) {
            // Handle the cause, but keep the ProcessException in case of Internal Compiler Error
            // only
            pe = e;
            throw e.getCause();
          }
        } catch (Throwable e1) {
          // If any exception, run hooks and ignore exceptions
          try {
            runSessionHooks.runHooks();
          } catch (Error | RuntimeException e2) {
            // Ignored but already logged in runHooks.
          }
          throw e1;
        }

        // If everything goes well, run hooks and report exception
        runSessionHooks.runHooks();
      } catch (FrontendCompilationException e) {
        throw new CompilationException(e.getMessage(), e);
      } catch (JackUserException e) {
        throw new CompilationException(e.getMessage(), e);
      } catch (JackAbortException e) {
        throw new CompilationException(e.getMessage(), e);
      } catch (com.android.sched.util.UnrecoverableException e) {
        throw new UnrecoverableException(e.getMessage(), e);
      } catch (Throwable e) {
        // Internal Compiler Error here
        // If the exception comes from a ProcessException, we want
        // to report the ProcessException instead of the cause
        if (pe != null) {
          e = pe;
        }
        if (e instanceof RuntimeException) {
          throw (RuntimeException) e;
        } else {
          throw new RuntimeException(e);
        }
      }
    }

  }

  @Override
  public void setClasspath(@Nonnull List<File> classpath) {
    options.setClasspath(Joiner.on(File.pathSeparator).join(classpath));
  }

  @Override
  public void setDebugInfoLevel(@Nonnull DebugInfoLevel debugLevel) throws ConfigurationException {
    switch (debugLevel) {
      case FULL: {
        options.addProperty(Options.EMIT_LOCAL_DEBUG_INFO.getName(), "true");
        options.addProperty(Options.EMIT_LINE_NUMBER_DEBUG_INFO.getName(), "true");
        options.addProperty(Options.EMIT_SOURCE_FILE_DEBUG_INFO.getName(), "true");
        break;
      }
      case LINES: {
        options.addProperty(Options.EMIT_LOCAL_DEBUG_INFO.getName(), "false");
        options.addProperty(Options.EMIT_LINE_NUMBER_DEBUG_INFO.getName(), "true");
        options.addProperty(Options.EMIT_SOURCE_FILE_DEBUG_INFO.getName(), "true");
        break;
      }
      case NONE: {
        options.addProperty(Options.EMIT_LOCAL_DEBUG_INFO.getName(), "false");
        options.addProperty(Options.EMIT_LINE_NUMBER_DEBUG_INFO.getName(), "false");
        options.addProperty(Options.EMIT_SOURCE_FILE_DEBUG_INFO.getName(), "false");
        break;
      }
      default: {
          throw new ConfigurationException(
              "Debug info level '" + debugLevel.toString() + "' is unsupported");
        }
    }
  }

  @Override
  public void setImportedJackLibraryFiles(@Nonnull List<File> importedJackLibraryFiles) {
    options.setImportedLibraries(importedJackLibraryFiles);
  }

  @Override
  public void setIncrementalDir(@Nonnull File incrementalDir) {
    options.setIncrementalFolder(incrementalDir);
  }

  @Override
  public void setJarJarConfigFiles(@Nonnull List<File> jarJarConfigFiles) {
    options.setJarjarRulesFiles(Api04ConfigImpl.manageFilesCharsetAsList(jarJarConfigFiles));
  }

  @Override
  public void setJavaSourceVersion(@Nonnull JavaSourceVersion javaSourceVersion)
      throws ConfigurationException {
    JavaVersion javaSourceVersionWrapped = null;

    switch (javaSourceVersion) {
      case JAVA_3: {
        javaSourceVersionWrapped = JavaVersion.JAVA_3;
        break;
      }
      case JAVA_4: {
        javaSourceVersionWrapped = JavaVersion.JAVA_4;
        break;
      }
      case JAVA_5: {
        javaSourceVersionWrapped = JavaVersion.JAVA_5;
        break;
      }
      case JAVA_6: {
        javaSourceVersionWrapped = JavaVersion.JAVA_6;
        break;
      }
      case JAVA_7: {
        javaSourceVersionWrapped = JavaVersion.JAVA_7;
        break;
      }
      default: {
        throw new ConfigurationException(
            "Java source version '" + javaSourceVersion + "' is unsupported");
      }
    }
    options.addProperty(Options.JAVA_SOURCE_VERSION.getName(), javaSourceVersionWrapped.toString());
  }

  @Override
  public void setMetaDirs(@Nonnull List<File> metaDirs) {
    options.setMetaDirs(metaDirs);
  }

  @Override
  public void setMultiDexKind(@Nonnull MultiDexKind multiDexKind) throws ConfigurationException {
    switch (multiDexKind) {
      case LEGACY: {
        options.setMultiDexKind(com.android.jack.Options.MultiDexKind.LEGACY);
        break;
      }
      case NATIVE: {
        options.setMultiDexKind(com.android.jack.Options.MultiDexKind.NATIVE);
        break;
      }
      case NONE: {
        options.setMultiDexKind(com.android.jack.Options.MultiDexKind.NONE);
        break;
      }
      default: {
        throw new ConfigurationException("Multi dex kind '" + multiDexKind + "' is unsupported");
      }
    }
  }

  @Override
  public void setObfuscationMappingOutputFile(@Nonnull File obfuscationMappingOutputFile) {
    options.addProperty(MappingPrinter.MAPPING_OUTPUT_ENABLED.getName(), "true");
    options.addProperty(MappingPrinter.MAPPING_OUTPUT_FILE.getName(),
        Api04ConfigImpl.manageFileCharset(obfuscationMappingOutputFile).getPath());
  }

  @Override
  public void setOutputDexDir(@Nonnull File outputDexDir) {
    options.setOutputDir(outputDexDir);
  }

  @Override
  public void setOutputJackFile(@Nonnull File outputJackFile) {
    options.setJayceOutputZip(outputJackFile);
  }

  @Override
  public void setProcessorNames(@Nonnull List<String> processorNames) {
    options.addProperty(Options.ANNOTATION_PROCESSOR_MANUAL.getName(), "true");
    options.addProperty(Options.ANNOTATION_PROCESSOR_MANUAL_LIST.getName(),
        Options.ANNOTATION_PROCESSOR_MANUAL_LIST.getCodec().formatValue(processorNames));
  }

  @Override
  public void setProcessorOptions(@Nonnull Map<String, String> processorOptions) {
    options.addProperty(Options.ANNOTATION_PROCESSOR_OPTIONS.getName(),
        Options.ANNOTATION_PROCESSOR_OPTIONS.getCodec().formatValue(processorOptions));
  }

  @Override
  public void setProcessorPath(@Nonnull List<File> processorPath) {
    options.addProperty(Options.ANNOTATION_PROCESSOR_PATH.getName(), "true");
    options.addProperty(Options.ANNOTATION_PROCESSOR_PATH_LIST.getName(),
        Joiner.on(File.pathSeparatorChar).join(processorPath));
  }

  @Override
  public void setProguardConfigFiles(@Nonnull List<File> proguardConfigFiles) {
    options.setProguardFlagsFile(Api04ConfigImpl.manageFilesCharsetAsList(proguardConfigFiles));
  }

  @Override
  public void setProperty(@Nonnull String key, @Nonnull String value) {
    options.addProperty(key, value);
  }

  @Override
  public void setReporter(@Nonnull ReporterKind reporterKind, @Nonnull OutputStream reporterStream)
      throws ConfigurationException {
    String reporterKindAsString = null;
    switch (reporterKind) {
      case DEFAULT: {
        reporterKindAsString = "default";
        break;
      }
      case SDK: {
        reporterKindAsString = "sdk";
        break;
      }
      default: {
        throw new ConfigurationException("Reporter kind '" + reporterKind + "' is unsupported");
      }
    }
    options.addProperty(Reporter.REPORTER.getName(), reporterKindAsString);
    options.setReporterStream(reporterStream);
  }

  @Override
  public void setResourceDirs(@Nonnull List<File> resourceDirs) {
    options.setResourceDirs(resourceDirs);
  }

  @Override
  public void setResourceImportCollisionPolicy(
      @Nonnull ResourceCollisionPolicy resourceImportCollisionPolicy)
      throws ConfigurationException {
    String collisionPolicy = null;
    switch (resourceImportCollisionPolicy) {
      case FAIL: {
        collisionPolicy = "fail";
        break;
      }
      case KEEP_FIRST: {
        collisionPolicy = "keep-first";
        break;
      }
      default:
        {
          throw new ConfigurationException("Resource collision policy '"
              + resourceImportCollisionPolicy.toString() + "' is unsupported");
        }
    }
    options.addProperty(ResourceImporter.RESOURCE_COLLISION_POLICY.getName(), collisionPolicy);
  }

  @Override
  public void setSourceEntries(@Nonnull Collection<File> sourceEntries) {
    options.setInputSources(Api04ConfigImpl.manageFilesCharsetAsList(sourceEntries));
  }

  @Override
  public void setTypeImportCollisionPolicy(@Nonnull TypeCollisionPolicy typeImportCollisionPolicy)
      throws ConfigurationException {
    String collissionPolicy = null;
    switch (typeImportCollisionPolicy) {
      case FAIL: {
        collissionPolicy = "fail";
        break;
      }
      case KEEP_FIRST: {
        collissionPolicy = "keep-first";
        break;
      }
      default: {
        throw new ConfigurationException(
            "Type collision policy '" + typeImportCollisionPolicy + "' is unsupported");
      }
    }
    options.addProperty(ResourceImporter.RESOURCE_COLLISION_POLICY.getName(), collissionPolicy);
  }

  @Override
  public void setVerbosityLevel(@Nonnull VerbosityLevel verbosityLevel)
      throws ConfigurationException {
    com.android.jack.VerbosityLevel jackVerbosityLevel;
    switch (verbosityLevel) {
      case DEBUG: {
        jackVerbosityLevel = com.android.jack.VerbosityLevel.INFO;
        break;
      }
      case ERROR: {
        jackVerbosityLevel = com.android.jack.VerbosityLevel.ERROR;
        break;
      }
      case INFO: {
        jackVerbosityLevel = com.android.jack.VerbosityLevel.INFO;
        break;
      }
      case WARNING: {
        jackVerbosityLevel = com.android.jack.VerbosityLevel.WARNING;
        break;
      }
      default: {
        throw new ConfigurationException("Verbosity level '" + verbosityLevel + "' is unsupported");
      }
    }
    options.setVerbosityLevel(jackVerbosityLevel);
  }

  @Override
  public void setApi(Class<? extends ApiFeature> api) {
    this.api = api;
  }
}
