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

package com.android.sched.reflections;

import com.android.sched.util.TextUtils;
import com.android.sched.util.log.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Set;
import java.util.logging.Level;

import javax.annotation.Nonnull;

/**
 * This {@link ReflectionManager} writes all queries in a file.
 * Each file contains the values returned by the ReflectionManager upon such
 * request.
 */
public class ReflectionManagerRecorder extends CommonReflectionManager
    implements ReflectionManager {
  @Nonnull
  private final File recordLocation;

  @Nonnull
  private final ReflectionManager manager;

  public ReflectionManagerRecorder(
      @Nonnull ReflectionManager manager, @Nonnull File recordLocation) {
    this.manager = manager;
    this.recordLocation = recordLocation;
  }

  /* (non-Javadoc)
   * @see com.android.sched.reflections.ReflectionManager#getSubTypesOf(java.lang.Class)
   */
  @Override
  @Nonnull
  public <T> Set<Class<? extends T>> getSubTypesOf(@Nonnull Class<T> cls) {
    Set<Class<? extends T>> result = manager.getSubTypesOf(cls);

    if (recordLocation.exists()) {

      StringBuilder filePath = new StringBuilder(cls.getName().replace('.', '/'));
      filePath.append(FileReflectionManager.SUBTYPES_FILE_SUFFIX);

      File outputFile = new File(recordLocation, filePath.toString());

      if (outputFile.exists()) {
        LoggerFactory.getLogger().log(Level.WARNING, "File already exists: {0}",
            outputFile.getAbsolutePath());
        if (!outputFile.delete()) {
          LoggerFactory.getLogger().log(Level.SEVERE, "Can not remove file: {0}",
              outputFile.getAbsolutePath());
        }
      }

      OutputStreamWriter fw = null;
      try {
        if (!outputFile.getParentFile().mkdirs()) {
          LoggerFactory.getLogger().log(Level.SEVERE, "Can not create parent directory: {0}",
              outputFile.getAbsolutePath());
        }

        if (!outputFile.createNewFile()) {
          LoggerFactory.getLogger().log(Level.SEVERE, "Can not create file: {0}",
              outputFile.getAbsolutePath());
        }

        fw = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");

        for (Class<? extends T> c : result) {
          fw.write(c.getName());
          fw.write(TextUtils.LINE_SEPARATOR);
        }
      } catch (IOException e) {
        LoggerFactory.getLogger().log(
            Level.WARNING, "An error occured while writing file " + outputFile.getAbsolutePath(),
            e);
      } finally {
        try {
          if (fw != null) {
            fw.close();
          }
        } catch (IOException e) {
          // Nothing more to be done.
        }
      }
    } else {
      LoggerFactory.getLogger().log(Level.WARNING, "Output directory {0} does not exist",
          recordLocation.getAbsolutePath());
    }

    return result;
  }

}
