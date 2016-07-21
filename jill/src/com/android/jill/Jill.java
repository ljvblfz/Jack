/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jill;

import com.android.jill.frontend.java.JavaTransformer;
import com.android.jill.utils.FileUtils;
import com.android.sched.util.Version;
import com.android.sched.util.file.CannotListDirException;
import com.android.sched.util.log.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.logging.Level;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Main class of Jill.
 */
public class Jill {

  @CheckForNull
  private static Version version = null;

  public static void process(@Nonnull Options options) {
    File binaryFile = options.getBinaryFile();
    JavaTransformer jt = new JavaTransformer(getVersion().getVersion(), options);
    if (binaryFile.isFile()) {
      if (FileUtils.isJavaBinaryFile(binaryFile)) {
        List<File> javaBinaryFiles = new ArrayList<File>();
        javaBinaryFiles.add(binaryFile);
        jt.transform(javaBinaryFiles);
      } else if (FileUtils.isJarFile(binaryFile)) {
        JarFile jarFile = null;
        try {
          jarFile = new JarFile(binaryFile);
          jt.transform(jarFile);
        } catch (IOException e) {
          throw new JillException("Failed to create jar file " + binaryFile.getName(), e);
        } finally {
          if (jarFile != null) {
            try {
              jarFile.close();
            } catch (IOException e) {
              LoggerFactory.getLogger()
                  .log(Level.WARNING, "Failed to close jar file " + binaryFile.getName());
            }
          }
        }
      } else {
        throw new JillException("Unsupported file type: " + binaryFile.getName());
      }
    } else {
      List<File> javaBinaryFiles = new ArrayList<File>();
      try {
        FileUtils.getJavaBinaryFiles(binaryFile, javaBinaryFiles);
      } catch (CannotListDirException e) {
        throw new JillException(e);
      }
      jt.transform(javaBinaryFiles);
    }
  }

  @Nonnull
  public static Version getVersion() {
    if (version == null) {
      try {
        version = new Version("jill", Jill.class.getClassLoader());
      } catch (IOException e) {
        System.err.println("Failed to read version properties file: " + e.getMessage());
        throw new AssertionError();
      }
    }

    assert version != null;
    return version;
  }

}
