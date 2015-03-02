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

import com.android.jack.IllegalOptionsException;
import com.android.jack.Jack;
import com.android.jack.JackAbortException;
import com.android.jack.JackUserException;
import com.android.jack.Options;
import com.android.jack.api.v01.AbortException;
import com.android.jack.api.v01.Api01Compiler;
import com.android.jack.api.v01.Api01Config;
import com.android.jack.api.v01.ConfigurationException;
import com.android.jack.api.v01.JavaSourceVersion;
import com.android.jack.api.v01.MultiDexKind;
import com.android.jack.api.v01.ReporterKind;
import com.android.jack.api.v01.ResourceCollisionPolicy;
import com.android.jack.api.v01.TypeCollisionPolicy;
import com.android.jack.api.v01.UnrecoverableException;
import com.android.jack.api.v01.VerbosityLevel;
import com.android.sched.scheduler.ProcessException;
import com.android.sched.util.RunnableHooks;

import java.io.File;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * STOPSHIP
 */
public class Api01ConfigImpl implements Api01Config {

  @Nonnull
  private final Options options;

  public Api01ConfigImpl() {
    options = new Options();
  }

  @Override
  @Nonnull
  public Api01Compiler build() throws ConfigurationException {
    RunnableHooks configHooks = new RunnableHooks(); //STOPSHIP: run configHooks
    try {
      Jack.check(options, configHooks);
    } catch (com.android.sched.util.config.ConfigurationException e) {
      throw new ConfigurationException(e.getMessage(), e);
    } catch (IllegalOptionsException e) {
      throw new ConfigurationException(e.getMessage(), e);
    }
    return new Api01CompilerImpl(options);
  }

  private static class Api01CompilerImpl implements Api01Compiler {

    @Nonnull
    private final Options options;

    public Api01CompilerImpl(@Nonnull Options options) {
      this.options = options;
    }

    @Override
    public void run() throws AbortException, UnrecoverableException {
      ProcessException pe = null;

      RunnableHooks runSessionHooks = new RunnableHooks();
      try {
        try {
          Jack.run(options, runSessionHooks);
        } catch (ProcessException e) {
          // Handle the cause, but keep the ProcessException in case of Internal Compiler Error only
          pe = e;
          throw e.getCause();
        }
      } catch (JackUserException e) {
        throw new AbortException(e.getMessage(), e);
      } catch (JackAbortException e) {
        throw new AbortException(e.getMessage(), e);
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
          throw new RuntimeException(e); //STOPSHIP: we have Throwables here that we can't throw as
                                         //is
        }
      } finally {
        runSessionHooks.runHooks();
      }
    }

  }

  /* (non-Javadoc)
   * @see com.android.jack.api.v01.Api01Config#setClasspath(java.util.List)
   */
  @Override
  @Nonnull
  public void setClasspath(@Nonnull List<File> arg0) throws ConfigurationException {
    // TODO(benoitlamarche): Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see com.android.jack.api.v01.Api01Config#setEmitDebug(boolean)
   */
  @Override
  @Nonnull
  public void setEmitDebug(boolean arg0) throws ConfigurationException {
    // TODO(benoitlamarche): Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see com.android.jack.api.v01.Api01Config#setImportedJackLibraryFiles(java.util.List)
   */
  @Override
  @Nonnull
  public void setImportedJackLibraryFiles(@Nonnull List<File> arg0) throws ConfigurationException {
    // TODO(benoitlamarche): Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see com.android.jack.api.v01.Api01Config#setIncrementalDir(java.io.File)
   */
  @Override
  @Nonnull
  public void setIncrementalDir(@Nonnull File arg0) throws ConfigurationException {
    // TODO(benoitlamarche): Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see com.android.jack.api.v01.Api01Config#setJarJarConfigFile(java.io.File)
   */
  @Override
  @Nonnull
  public void setJarJarConfigFile(@Nonnull File arg0) throws ConfigurationException {
    // TODO(benoitlamarche): Auto-generated method stub

  }

  @Override
  @Nonnull
  public void setJavaSourceVersion(@Nonnull JavaSourceVersion arg0) throws ConfigurationException {
    // TODO(benoitlamarche): Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see com.android.jack.api.v01.Api01Config#setMetaDirs(java.util.List)
   */
  @Override
  @Nonnull
  public void setMetaDirs(@Nonnull List<File> arg0) throws ConfigurationException {
    // TODO(benoitlamarche): Auto-generated method stub

  }

  @Override
  @Nonnull
  public void setMultiDexKind(@Nonnull MultiDexKind arg0) throws ConfigurationException {
    // TODO(benoitlamarche): Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see com.android.jack.api.v01.Api01Config#setObfuscationMappingOutputFile(java.io.File)
   */
  @Override
  @Nonnull
  public void setObfuscationMappingOutputFile(@Nonnull File arg0) throws ConfigurationException {
    // TODO(benoitlamarche): Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see com.android.jack.api.v01.Api01Config#setOutputDexDir(java.io.File)
   */
  @Override
  @Nonnull
  public void setOutputDexDir(@Nonnull File arg0) throws ConfigurationException {
    // TODO(benoitlamarche): Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see com.android.jack.api.v01.Api01Config#setOutputJackFile(java.io.File)
   */
  @Override
  @Nonnull
  public void setOutputJackFile(@Nonnull File arg0) throws ConfigurationException {
    // TODO(benoitlamarche): Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see com.android.jack.api.v01.Api01Config#setProcessorNames(java.util.List)
   */
  @Override
  @Nonnull
  public void setProcessorNames(@Nonnull List<String> arg0) throws ConfigurationException {
    // TODO(benoitlamarche): Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see com.android.jack.api.v01.Api01Config#setProcessorOptions(java.util.Map)
   */
  @Override
  @Nonnull
  public void setProcessorOptions(@Nonnull Map<String, String> arg0) throws ConfigurationException {
    // TODO(benoitlamarche): Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see com.android.jack.api.v01.Api01Config#setProcessorPath(java.util.List)
   */
  @Override
  @Nonnull
  public void setProcessorPath(@Nonnull List<File> arg0) throws ConfigurationException {
    // TODO(benoitlamarche): Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see com.android.jack.api.v01.Api01Config#setProguardConfigFiles(java.util.List)
   */
  @Override
  @Nonnull
  public void setProguardConfigFiles(@Nonnull List<File> arg0) throws ConfigurationException {
    // TODO(benoitlamarche): Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see com.android.jack.api.v01.Api01Config#setProperty(java.lang.String, java.lang.String)
   */
  @Override
  @Nonnull
  public void setProperty(@Nonnull String arg0, @Nonnull String arg1)
      throws ConfigurationException {
    // TODO(benoitlamarche): Auto-generated method stub

  }

  @Override
  @Nonnull
  public void setReporter(@Nonnull ReporterKind arg0, @Nonnull OutputStream arg1)
      throws ConfigurationException {
    // TODO(benoitlamarche): Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see com.android.jack.api.v01.Api01Config#setResourceDirs(java.util.List)
   */
  @Override
  @Nonnull
  public void setResourceDirs(@Nonnull List<File> arg0) throws ConfigurationException {
    // TODO(benoitlamarche): Auto-generated method stub

  }

  @Override
  @Nonnull
  public void setResourceImportCollisionPolicy(@Nonnull ResourceCollisionPolicy arg0)
      throws ConfigurationException {
    // TODO(benoitlamarche): Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see com.android.jack.api.v01.Api01Config#setSourceEntries(java.util.List)
   */
  @Override
  @Nonnull
  public void setSourceEntries(@Nonnull List<File> arg0) throws ConfigurationException {
    // TODO(benoitlamarche): Auto-generated method stub

  }

  @Override
  @Nonnull
  public void setTypeImportCollisionPolicy(@Nonnull TypeCollisionPolicy arg0)
      throws ConfigurationException {
    // TODO(benoitlamarche): Auto-generated method stub

  }

  @Override
  @Nonnull
  public void setVerbosityLevel(@Nonnull VerbosityLevel arg0) throws ConfigurationException {
    // TODO(benoitlamarche): Auto-generated method stub

  }
}
