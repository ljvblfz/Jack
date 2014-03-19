/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.sched.build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nonnull;

/**
 * Utility tool to merge several jars into one by taking care of {@code SchedDiscover} resource by
 * merging data.
 */
public class JarMerger {
  private static final int BUFFER_SIZE = 4096;

  public static void main(@Nonnull String[] args) throws IOException {
    if (args.length < 2) {
      System.err.println(
          "Usage: in-jar-file-1 [in-jar-file-2 [...] [in-jar-file-n]] [out-jar-file]");
      System.err.println("  In case of entry collision, the first encountered is kept.");
      System.err.println("  Only META-INF directory from the first jar is kept.");
      System.exit(1);
    }

    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(args[args.length - 1]));

    Set<String> entries = new HashSet<String>();
    SchedDiscover data = new SchedDiscover();

    try {
      for (int idx = 0; idx < args.length - 1; idx++) {
        File file = new File(args[idx]);
        ZipInputStream zis = new ZipInputStream(new FileInputStream(file));

        try {
          ZipEntry entry;
          while ((entry = zis.getNextEntry()) != null) {
            if (entry.isDirectory()) {
              continue;
            } else if (entry.getName().equals(data.getResourceName())) {
              data.readResource(new BufferedReader(new InputStreamReader(zis)));
            } else {
              if (idx > 0 && entry.getName().startsWith("META-INF/")) {
                continue;
              }

              String newName = entry.getName();
              if (!entries.contains(newName)) {
                entries.add(newName);

                ZipEntry newEntry = new ZipEntry(newName);
                newEntry.setTime(entry.getTime());
                zos.putNextEntry(newEntry);

                try {
                  byte[] buffer = new byte[BUFFER_SIZE];
                  int length = zis.read(buffer);
                  while (length > 0) {
                    zos.write(buffer, 0, length);
                    length = zis.read(buffer);
                  }
                } finally {
                  zos.closeEntry();
                }
              }  else {
                System.err.println("Warning: dropping duplicate entry '" + newName
                    + "' found in '" + args[idx] + "'");
              }
            }
          }
        } finally {
          zis.close();
        }
      }

      zos.putNextEntry(new ZipEntry(data.getResourceName()));
      try {
        Writer writer = new OutputStreamWriter(zos);
        data.writeResource(writer);
        writer.flush();
      } finally {
        zos.closeEntry();
      }
    } finally {
      zos.close();
    }

    System.exit(0);
  }
}
