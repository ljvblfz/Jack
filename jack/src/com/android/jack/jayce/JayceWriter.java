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

package com.android.jack.jayce;

import com.android.jack.JackEventType;
import com.android.jack.ir.ast.JNode;
import com.android.sched.util.log.Event;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import javax.annotation.Nonnull;

/**
 * A writer of Jayce streams.
 */
public class JayceWriter extends JayceProcessor {

  private static final int DEFAULT_MAJOR_VERSION = 2;
  @Nonnull
  private static final Charset encoding = Charset.forName("UTF-8");
  @Nonnull
  private static final Tracer tracer = TracerFactory.getTracer();

  @Nonnull
  private final OutputStream out;

  public JayceWriter(@Nonnull OutputStream out) {
    this.out = out;
  }

  /**
   * Write a {@link JNode} as a Jayce stream.
   *
   * @param jNode the node to write as a Jayce stream
   * @param emitterId the identifier of the emitter
   * @throws IOException thrown when there is an issue writing the stream
   * @throws JayceFormatException thrown if the Jayce stream has an invalid format
   * @throws JayceVersionException thrown if the version of the Jayce stream is not supported
   */
  public void write(
      @Nonnull JNode jNode, @Nonnull String emitterId)
      throws IOException, JayceFormatException, JayceVersionException {
    write(jNode, emitterId, DEFAULT_MAJOR_VERSION);
  }

  /**
   * Write a {@link JNode} as a Jayce stream.
   *
   * @param jNode the node to write as a Jayce stream
   * @param emitterId the identifier of the emitter
   * @param majorVersion major version number of the Jayce format to use
   * @throws IOException thrown when there is an issue writing the stream
   * @throws JayceFormatException thrown if the Jayce stream has an invalid format
   * @throws JayceVersionException thrown if the version of the Jayce stream is not supported
   */
  public void write(
      @Nonnull JNode jNode, @Nonnull String emitterId, int majorVersion)
      throws IOException, JayceFormatException, JayceVersionException {

    String majorVersionString = JayceHeader.getVersionString(majorVersion);

    String className =
        "com.android.jack.jayce.v" + majorVersionString + ".io.JayceInternalWriterImpl";
    JayceInternalWriter jayceInternalWriter =
        (JayceInternalWriter) instantiateConstructorWithParameters(className,
            new Class[] {OutputStream.class, Charset.class}, new Object[] {out, encoding},
            String.valueOf(majorVersion));
    int currentMinor = jayceInternalWriter.getCurrentMinor();
    JayceHeader jayceHeader = new JayceHeader(majorVersion, currentMinor, encoding, emitterId);

    Event event = tracer.start(JackEventType.NNODE_WRITING);

    try {
      jayceHeader.writeHeader(out);
      jayceInternalWriter.write(jNode);
      out.flush();
    } finally {
      event.end();
    }
  }
}
