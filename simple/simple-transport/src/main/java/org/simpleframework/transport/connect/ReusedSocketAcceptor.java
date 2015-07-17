/*
 * Acceptor.java October 2002
 *
 * Copyright (C) 2002, Niall Gallagher <niallg@users.sf.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.simpleframework.transport.connect;

import org.simpleframework.transport.SocketProcessor;
import org.simpleframework.transport.trace.TraceAnalyzer;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;

import javax.net.ssl.SSLContext;

/**
 * A {@link SocketAcceptor} capable of using already bound {@link ServerSocketChannel}.
 */
class ReusedSocketAcceptor extends SocketAcceptor {

   /**
    * Constructor for the <code>ReusedSocketAcceptor</code> object. This
    * accepts new TCP connections from the specified server socket.
    * Each of the connections that is accepted is configured for
    * performance for the applications.
    *
    * @param channel this is the channel to accept connections from
    * @param processor this is used to initiate the HTTP processing
    * @param analyzer this is the tracing analyzer to be used
    * @param context this is the SSL context used for secure HTTPS
    */
   public ReusedSocketAcceptor(ServerSocketChannel channel, SocketProcessor processor,
       TraceAnalyzer analyzer, SSLContext context) throws IOException {
      super(channel, processor, analyzer, context);
   }

   /**
    * Ensure that this <code>SocketAcceptor</code> is ready to accept connections.
    */
   @Override
   public void bind() throws IOException {
      assert (!getChannel().isBlocking())
         && ((ServerSocketChannel) getChannel()).socket().isBound();
   }

   /**
    * Close this Acceptor.
    */
   @Override
   public void close() {
      // let the channel creator closing it.
   }
}
