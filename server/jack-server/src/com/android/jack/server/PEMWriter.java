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

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Write PEM files.
 */
public class PEMWriter implements Closeable {

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

  public PEMWriter(@Nonnull File file) throws IOException {
    targetFile = file;
    tmpFile = File.createTempFile("jackserver-", file.getName(), file.getParentFile());
    if (!tmpFile.exists()) {
      if (!tmpFile.createNewFile()) {
        throw new IOException("Failed to create temp file '" + tmpFile.getPath() + "'");
      }
      if  (!(tmpFile.setExecutable(false, false)
          && tmpFile.setWritable(false, false)
          && tmpFile.setReadable(false, false)
          && tmpFile.setWritable(true, true)
          && tmpFile.setReadable(true, true))) {
        throw new IOException("Failed to set permission of '" + tmpFile.getPath() + "'");
      }
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
    if (!tmpFile.renameTo(targetFile)) {
      throw new IOException("Failed to rename \"" + tmpFile.getPath() + "\" to \""
          + targetFile.getPath() + "\"");
    }
  }
}
