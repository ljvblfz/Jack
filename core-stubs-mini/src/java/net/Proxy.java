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

package java.net;

public class Proxy {
  public static enum Type {
    DIRECT(), HTTP(), SOCKS();
  }

  public Proxy(java.net.Proxy.Type type, java.net.SocketAddress sa) {
    throw new RuntimeException("Stub!");
  }

  public java.net.Proxy.Type type() {
    throw new RuntimeException("Stub!");
  }

  public java.net.SocketAddress address() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String toString() {
    throw new RuntimeException("Stub!");
  }

  public final boolean equals(java.lang.Object obj) {
    throw new RuntimeException("Stub!");
  }

  public final int hashCode() {
    throw new RuntimeException("Stub!");
  }

  public static final java.net.Proxy NO_PROXY;
  static {
    NO_PROXY = null;
  }
}
