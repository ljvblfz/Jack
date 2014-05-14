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

public final class URI implements java.lang.Comparable<java.net.URI>, java.io.Serializable {
  public URI(java.lang.String spec) throws java.net.URISyntaxException {
    throw new RuntimeException("Stub!");
  }

  public URI(java.lang.String scheme, java.lang.String schemeSpecificPart,
      java.lang.String fragment) throws java.net.URISyntaxException {
    throw new RuntimeException("Stub!");
  }

  public URI(java.lang.String scheme,
      java.lang.String userInfo,
      java.lang.String host,
      int port,
      java.lang.String path,
      java.lang.String query,
      java.lang.String fragment) throws java.net.URISyntaxException {
    throw new RuntimeException("Stub!");
  }

  public URI(java.lang.String scheme, java.lang.String host, java.lang.String path,
      java.lang.String fragment) throws java.net.URISyntaxException {
    throw new RuntimeException("Stub!");
  }

  public URI(java.lang.String scheme, java.lang.String authority, java.lang.String path,
      java.lang.String query, java.lang.String fragment) throws java.net.URISyntaxException {
    throw new RuntimeException("Stub!");
  }

  public int compareTo(java.net.URI uri) {
    throw new RuntimeException("Stub!");
  }

  public static java.net.URI create(java.lang.String uri) {
    throw new RuntimeException("Stub!");
  }

  public boolean equals(java.lang.Object o) {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String getScheme() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String getSchemeSpecificPart() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String getRawSchemeSpecificPart() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String getAuthority() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String getRawAuthority() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String getUserInfo() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String getRawUserInfo() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String getHost() {
    throw new RuntimeException("Stub!");
  }

  public int getPort() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String getPath() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String getRawPath() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String getQuery() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String getRawQuery() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String getFragment() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String getRawFragment() {
    throw new RuntimeException("Stub!");
  }

  public int hashCode() {
    throw new RuntimeException("Stub!");
  }

  public boolean isAbsolute() {
    throw new RuntimeException("Stub!");
  }

  public boolean isOpaque() {
    throw new RuntimeException("Stub!");
  }

  public java.net.URI normalize() {
    throw new RuntimeException("Stub!");
  }

  public java.net.URI parseServerAuthority() throws java.net.URISyntaxException {
    throw new RuntimeException("Stub!");
  }

  public java.net.URI relativize(java.net.URI relative) {
    throw new RuntimeException("Stub!");
  }

  public java.net.URI resolve(java.net.URI relative) {
    throw new RuntimeException("Stub!");
  }

  public java.net.URI resolve(java.lang.String relative) {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String toASCIIString() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String toString() {
    throw new RuntimeException("Stub!");
  }

  public java.net.URL toURL() throws java.net.MalformedURLException {
    throw new RuntimeException("Stub!");
  }
}
