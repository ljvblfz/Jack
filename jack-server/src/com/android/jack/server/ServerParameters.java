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

import com.android.sched.util.Version;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.nio.channels.ServerSocketChannel;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Handle server to server parameters.
 */
public class ServerParameters {

  @Nonnull
  public static final String SERVICE_CHANNEL_PARAMETER = "service.channel";
  @Nonnull
  public static final String ADMIN_CHANNEL_PARAMETER = "admin.channel";
  @Nonnull
  public static final String SERVER_RELEASE_CODE_PARAMETER = "server.version.release.code";
  @Nonnull
  public static final String SERVER_SUB_RELEASE_CODE_PARAMETER = "server.version.sub-release.code";
  @Nonnull
  public static final String SERVER_SUB_RELEASE_KIND_PARAMETER = "server.version.sub-release.kind";

  private final Map<String, Object> parameters;

  public ServerParameters(@Nonnull Map<String, Object> parameters) {
    this.parameters = parameters;
    Version serverVersion = JackHttpServer.getServerVersion();
    this.parameters.put(SERVER_RELEASE_CODE_PARAMETER,
        Integer.toString(serverVersion.getReleaseCode()));
    this.parameters.put(SERVER_SUB_RELEASE_CODE_PARAMETER,
        Integer.toString(serverVersion.getSubReleaseCode()));
    this.parameters.put(SERVER_SUB_RELEASE_KIND_PARAMETER,
        serverVersion.getSubReleaseKind().name());
  }

  @Nonnull
  public Map<String, Object> asMap() {
    return parameters;
  }

  @Nonnull
  public ServerSocketChannel getServiceSocket(@Nonnull InetSocketAddress serviceAddress)
      throws IOException, SocketException {
    return openSocket(serviceAddress, SERVICE_CHANNEL_PARAMETER);
  }

  @Nonnull
  public ServerSocketChannel getAdminSocket(@Nonnull InetSocketAddress adminAddress)
      throws IOException, SocketException {
    return openSocket(adminAddress, ADMIN_CHANNEL_PARAMETER);
  }

  @Nonnull
  private ServerSocketChannel openSocket(@Nonnull InetSocketAddress serviceAddress,
      @Nonnull String channelParameter) throws IOException,
      SocketException {
    Object existingChannel = parameters.get(channelParameter);
    if (existingChannel instanceof ServerSocketChannel) {
      return (ServerSocketChannel) existingChannel;
    } else {
      ServerSocketChannel channel = ServerSocketChannel.open();
      channel.configureBlocking(false);
      ServerSocket socket = channel.socket();
      socket.setReuseAddress(true);
      socket.bind(serviceAddress, 100);
      parameters.put(channelParameter, channel);
      return channel;
    }
  }

}
