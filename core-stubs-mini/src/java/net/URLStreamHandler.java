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

public abstract class URLStreamHandler {
  public URLStreamHandler() {
    throw new RuntimeException("Stub!");
  }

  protected abstract java.net.URLConnection openConnection(java.net.URL u)
      throws java.io.IOException;

  protected java.net.URLConnection openConnection(java.net.URL u, java.net.Proxy proxy)
      throws java.io.IOException {
    throw new RuntimeException("Stub!");
  }

  protected void parseURL(java.net.URL url, java.lang.String spec, int start, int end) {
    throw new RuntimeException("Stub!");
  }

  @java.lang.Deprecated()
  protected void setURL(java.net.URL u,
      java.lang.String protocol,
      java.lang.String host,
      int port,
      java.lang.String file,
      java.lang.String ref) {
    throw new RuntimeException("Stub!");
  }

  protected void setURL(java.net.URL u,
      java.lang.String protocol,
      java.lang.String host,
      int port,
      java.lang.String authority,
      java.lang.String userInfo,
      java.lang.String path,
      java.lang.String query,
      java.lang.String ref) {
    throw new RuntimeException("Stub!");
  }

  protected java.lang.String toExternalForm(java.net.URL url) {
    throw new RuntimeException("Stub!");
  }

  protected boolean equals(java.net.URL a, java.net.URL b) {
    throw new RuntimeException("Stub!");
  }

  protected int getDefaultPort() {
    throw new RuntimeException("Stub!");
  }

  protected java.net.InetAddress getHostAddress(java.net.URL url) {
    throw new RuntimeException("Stub!");
  }

  protected int hashCode(java.net.URL url) {
    throw new RuntimeException("Stub!");
  }

  protected boolean hostsEqual(java.net.URL a, java.net.URL b) {
    throw new RuntimeException("Stub!");
  }

  protected boolean sameFile(java.net.URL a, java.net.URL b) {
    throw new RuntimeException("Stub!");
  }
}
