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

package com.android.jack.server;

import com.google.common.io.BaseEncoding;

import com.android.sched.util.file.CannotChangePermissionException;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileAlreadyExistsException;
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.log.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.Key;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Write PEM files.
 */
public class PEMWriter implements Closeable {

  @Nonnull
  private static Logger logger = LoggerFactory.getLogger();

  @Nonnegative
  private static final int MAX_LINE_SIZE = 65;

  @Nonnull
  private static final Charset CHARSET = StandardCharsets.US_ASCII;

  @Nonnull
  private static final String EOL = "\r\n";

  @Nonnull
  private static final BaseEncoding base64Encoder =
    BaseEncoding.base64().withSeparator(EOL, MAX_LINE_SIZE);

  @Nonnull
  private final Writer out;

  @Nonnull
  private final File targetFile;


  @Nonnull
  private final File tmpFile;

  public PEMWriter(@Nonnull File file) throws IOException, CannotCreateFileException,
      CannotChangePermissionException {
    targetFile = file;
    Directory parentDirectory;
    try {
      parentDirectory = new Directory(file.getParentFile().getPath(),
          null,
          Existence.MUST_EXIST,
          Permission.READ | Permission.WRITE,
          ChangePermission.NOCHANGE);
    } catch (NotDirectoryException | WrongPermissionException | NoSuchFileException
        | FileAlreadyExistsException e) {
      throw new AssertionError(e.getMessage(), e);
    }
    tmpFile = com.android.sched.util.file.Files.createTempFile("jackserver-",
          file.getName(), parentDirectory);
    if  (!(tmpFile.setExecutable(false, false)
        && tmpFile.setWritable(false, false)
        && tmpFile.setReadable(false, false)
        && tmpFile.setWritable(true, true)
        && tmpFile.setReadable(true, true))) {
      throw new IOException("Failed to set permission of '" + tmpFile.getPath() + "'");
    }

    out = new OutputStreamWriter(new FileOutputStream(tmpFile), CHARSET);
  }

  public void writeCertificate(@Nonnull Certificate certificate) throws IOException {
    out.write("-----BEGIN CERTIFICATE-----");
    out.write(EOL);
    try {
      out.write(base64Encoder.encode(certificate.getEncoded()));
    } catch (CertificateEncodingException e) {
      throw new IOException(e.getMessage());
    }
    out.write(EOL);
    out.write("-----END CERTIFICATE-----");
    out.write(EOL);
  }

  public void writeKey(@Nonnull Key key) throws IOException {
    out.write("-----BEGIN PRIVATE KEY-----");
    out.write(EOL);
    out.write(base64Encoder.encode(key.getEncoded()));
    out.write(EOL);
    out.write("-----END PRIVATE KEY-----");
    out.write(EOL);
  }

  @Override
  public void close() throws IOException {
    out.close();
    try {
      Files.move(tmpFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING,
          StandardCopyOption.ATOMIC_MOVE);
    } catch (AtomicMoveNotSupportedException e) {
      logger.log(Level.WARNING, "Atomic move not supported for renaming '" + tmpFile.getPath()
        + "' to '" + targetFile.getPath() + "'");
      Files.move(tmpFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
  }
}
