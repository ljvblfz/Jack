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

package com.android.jack.test.toolchain;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import com.android.jack.Options;
import com.android.jack.api.v01.CompilationException;
import com.android.jack.api.v01.ConfigurationException;
import com.android.jack.api.v01.DebugInfoLevel;
import com.android.jack.api.v01.ReporterKind;
import com.android.jack.api.v01.VerbosityLevel;
import com.android.jack.api.v02.JavaSourceVersion;
import com.android.jack.api.v03.Api03Config;
import com.android.jack.test.TestConfigurationException;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.vfs.Container;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This {@link Toolchain} uses Jack through v03 API
 */
public class JackApiV03Toolchain extends JackApiToolchainBase implements JackApiV03 {

  @Nonnull
  private Api03Config apiV03Config;

  JackApiV03Toolchain(@CheckForNull File jackPrebuilt) {
    super(jackPrebuilt, Api03Config.class);
    apiV03Config = (Api03Config) config;
    addProperty(Options.USE_DEFAULT_LIBRARIES.getName(), "false");
  }

  @Override
  public void srcToExe(@Nonnull File out, boolean zipFile, @Nonnull File... sources)
      throws Exception {
    srcToCommon(sources);
    setOutputDex(out, zipFile);
    run();
  }

  @Override
  public void srcToLib(@Nonnull File out, boolean zipFiles, @Nonnull File... sources)
      throws Exception {
    srcToCommon(sources);
    setOutputJack(out, zipFiles);
    run();
  }

  @Override
  public void libToExe(@Nonnull File[] in, @Nonnull File out, boolean zipFile) throws Exception {
    libToCommon(in);
    setOutputDex(out, zipFile);
    run();
  }

  @Override
  public void libToLib(@Nonnull File[] in, @Nonnull File out, boolean zipFiles) throws Exception {
    libToCommon(in);
    setOutputJack(out, zipFiles);
    run();
  }

  @Override
  @Nonnull
  public JackApiV03Toolchain setIncrementalFolder(@Nonnull File incrementalFolder) {
    try {
      apiV03Config.setIncrementalDir(incrementalFolder);
      return this;
    } catch (ConfigurationException e) {
      throw new TestConfigurationException(e);
    }
  }

  @Override
  @Nonnull
  public JackApiV03Toolchain setVerbose(boolean isVerbose) {
    super.setVerbose(isVerbose);
    try {
      if (isVerbose) {
        apiV03Config.setVerbosityLevel(VerbosityLevel.INFO);
      } else {
        apiV03Config.setVerbosityLevel(VerbosityLevel.WARNING);
      }
      return this;
    } catch (ConfigurationException e) {
      throw new TestConfigurationException(e);
    }
  }

  @Override
  @Nonnull
  public JackApiV03Toolchain addProperty(@Nonnull String propertyName,
      @Nonnull String propertyValue) {
    try {
      apiV03Config.setProperty(propertyName, propertyValue);
      return this;
    } catch (ConfigurationException e) {
      throw new TestConfigurationException(e);
    }
  }

  @Override
  @Nonnull
  public JackApiV03Toolchain setWithDebugInfos(boolean withDebugInfos) {
    super.setWithDebugInfos(withDebugInfos);
    try {
      if (withDebugInfos) {
        apiV03Config.setDebugInfoLevel(DebugInfoLevel.FULL);
      } else {
        apiV03Config.setDebugInfoLevel(DebugInfoLevel.LINES);
      }
      return this;
    } catch (ConfigurationException e) {
      throw new TestConfigurationException(e);
    }
  }

  @Override
  @Nonnull
  public JackApiV03Toolchain setSanityChecks(boolean sanityChecks) {
    try {
      apiV03Config.setProperty(Options.SANITY_CHECKS.getName(), Boolean.toString(sanityChecks));
      return this;
    } catch (ConfigurationException e) {
      throw new TestConfigurationException(e);
    }
  }

  @Override
  @Nonnull
  public final Toolchain setErrorStream(@Nonnull OutputStream errorStream) {
    super.setErrorStream(errorStream);
    try {
      apiV03Config.setReporter(ReporterKind.DEFAULT, errorStream);
    } catch (ConfigurationException e) {
      throw new TestConfigurationException(e);
    }
    return this;
  }

  @Nonnull
  public JackApiV03Toolchain setBaseDirectory(@Nonnull File baseDir) {
    try {
      apiV03Config.setBaseDirectory(baseDir);
    } catch (ConfigurationException e) {
      throw new TestConfigurationException(e);
    }
    return this;
  }

  @Override
  @Nonnull
  public JackApiV03Toolchain setSourceLevel(@Nonnull SourceLevel sourceLevel) {
    super.setSourceLevel(sourceLevel);

    try {
    switch (sourceLevel) {
      case JAVA_6:
        apiV03Config.setJavaSourceVersion(JavaSourceVersion.JAVA_6);
        break;
      case JAVA_7:
        apiV03Config.setJavaSourceVersion(JavaSourceVersion.JAVA_7);
        break;
      case JAVA_8:
        apiV03Config.setJavaSourceVersion(JavaSourceVersion.JAVA_8);
        break;
      default:
        throw new AssertionError("Unkown level: '" + sourceLevel.toString() + "'");
    }
    } catch (ConfigurationException e) {
      throw new TestConfigurationException();
    }
    return this;

  }

  private void run() throws Exception {
    try {
      System.setOut(outRedirectStream);
      System.setErr(errRedirectStream);
      LoggerFactory.configure(LogLevel.ERROR);
      apiV03Config.getTask().run();
    } catch (ConfigurationException e) {
      Throwable t1 = e.getCause();
      if (t1 instanceof Exception) {
        throw (Exception) t1;
      } else {
        throw new AssertionError();
      }
    } catch (CompilationException e1) {
      Throwable t1 = e1.getCause();
      if (t1 instanceof Exception) {
        throw (Exception) t1;
      } else {
        throw new AssertionError();
      }
    } finally {
      System.setOut(stdOut);
      System.setErr(stdErr);
      LoggerFactory.configure(LogLevel.ERROR);
    }
  }

  private void srcToCommon(@Nonnull File... sources) throws Exception {
    apiV03Config.setClasspath(classpath);
    apiV03Config.setImportedJackLibraryFiles(staticLibs);
    apiV03Config.setSourceEntries(Lists.newArrayList(sources));
    apiV03Config.setResourceDirs(resImport);
    apiV03Config.setMetaDirs(metaImport);
    apiV03Config.setProguardConfigFiles(proguardFlags);
    if (!jarjarRules.isEmpty()) {
      apiV03Config.setJarJarConfigFiles(jarjarRules);
    }
    apiV03Config.setProcessorOptions(annotationProcessorOptions);

    if (annotationProcessorClasses != null) {
      apiV03Config.setProcessorNames(annotationProcessorClasses);
    }


    if (processorPath != null) {
      List<File> fileList = new ArrayList<File>();
      for (String entry : Splitter.on(File.pathSeparatorChar).split(processorPath)) {
        fileList.add(new File(entry));
      }
      apiV03Config.setProcessorPath(fileList);
    }
  }

  private void libToCommon(@Nonnull File... in) throws Exception {
    apiV03Config.setClasspath(classpath);
    List<File> importedLibs = new ArrayList<File>(staticLibs);
    Collections.addAll(importedLibs, in);
    apiV03Config.setImportedJackLibraryFiles(importedLibs);
    apiV03Config.setResourceDirs(resImport);
    apiV03Config.setMetaDirs(metaImport);
    apiV03Config.setProguardConfigFiles(proguardFlags);
    if (jarjarRules != null) {
      apiV03Config.setJarJarConfigFiles(jarjarRules);
    }
  }

  private void setOutputDex(@Nonnull File outDex, boolean zipFiles) throws Exception {
    if (!zipFiles) {
      apiV03Config.setOutputDexDir(outDex);
    } else {
      apiV03Config.setProperty(Options.DEX_OUTPUT_CONTAINER_TYPE.getName(), Container.ZIP.name());
      apiV03Config.setProperty(Options.GENERATE_DEX_FILE.getName(), "true");
      apiV03Config.setProperty(Options.DEX_OUTPUT_ZIP.getName(), outDex.getAbsolutePath());
    }
  }

  @Override
  public void setOutputJack(@Nonnull File outjack, boolean zipFiles) throws Exception {
    if (zipFiles) {
      apiV03Config.setOutputJackFile(outjack);
    } else {
      apiV03Config.setProperty(Options.LIBRARY_OUTPUT_CONTAINER_TYPE.getName(),
          Container.DIR.name());
      apiV03Config.setProperty(Options.LIBRARY_OUTPUT_DIR.getName(), outjack.getAbsolutePath());
      apiV03Config.setProperty(Options.GENERATE_JACK_LIBRARY.getName(), "true");
      apiV03Config.setProperty(Options.GENERATE_JAYCE_IN_LIBRARY.getName(), "true");
      apiV03Config.setProperty(Options.GENERATE_DEPENDENCIES_IN_LIBRARY.getName(), "true");
    }
  }

  @Nonnull
  public JackApiV03Toolchain setPluginNames(@Nonnull List<String> pluginNames) {
    try {
      apiV03Config.setPluginNames(pluginNames);
    } catch (ConfigurationException e) {
      throw new TestConfigurationException(e);
    }
    return this;
  }

  @Nonnull
  public JackApiV03Toolchain setPluginPath(@Nonnull List<File> pluginPath) {
    try {
      apiV03Config.setPluginPath(pluginPath);
    } catch (ConfigurationException e) {
      throw new TestConfigurationException(e);
    }
    return this;
  }

  @Override
  @Nonnull
  public AndroidToolchain setAndroidMinApiLevel(@Nonnull String minApiLevel) throws Exception {
    apiV03Config.setProperty(Options.ANDROID_MIN_API_LEVEL.getName(), minApiLevel);
    return this;
  }
}
