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

public class InetAddress implements java.io.Serializable {
  InetAddress() {
    throw new RuntimeException("Stub!");
  }

  public boolean equals(java.lang.Object obj) {
    throw new RuntimeException("Stub!");
  }

  public byte[] getAddress() {
    throw new RuntimeException("Stub!");
  }

  public static java.net.InetAddress[] getAllByName(java.lang.String host)
      throws java.net.UnknownHostException {
    throw new RuntimeException("Stub!");
  }

  public static java.net.InetAddress getByName(java.lang.String host)
      throws java.net.UnknownHostException {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String getHostAddress() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String getHostName() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String getCanonicalHostName() {
    throw new RuntimeException("Stub!");
  }

  public static java.net.InetAddress getLocalHost() throws java.net.UnknownHostException {
    throw new RuntimeException("Stub!");
  }

  public int hashCode() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String toString() {
    throw new RuntimeException("Stub!");
  }

  public boolean isAnyLocalAddress() {
    throw new RuntimeException("Stub!");
  }

  public boolean isLinkLocalAddress() {
    throw new RuntimeException("Stub!");
  }

  public boolean isLoopbackAddress() {
    throw new RuntimeException("Stub!");
  }

  public boolean isMCGlobal() {
    throw new RuntimeException("Stub!");
  }

  public boolean isMCLinkLocal() {
    throw new RuntimeException("Stub!");
  }

  public boolean isMCNodeLocal() {
    throw new RuntimeException("Stub!");
  }

  public boolean isMCOrgLocal() {
    throw new RuntimeException("Stub!");
  }

  public boolean isMCSiteLocal() {
    throw new RuntimeException("Stub!");
  }

  public boolean isMulticastAddress() {
    throw new RuntimeException("Stub!");
  }

  public boolean isSiteLocalAddress() {
    throw new RuntimeException("Stub!");
  }

  public boolean isReachable(int timeout) throws java.io.IOException {
    throw new RuntimeException("Stub!");
  }

  public boolean isReachable(java.net.NetworkInterface networkInterface, int ttl, int timeout)
      throws java.io.IOException {
    throw new RuntimeException("Stub!");
  }

  public static java.net.InetAddress getByAddress(byte[] ipAddress)
      throws java.net.UnknownHostException {
    throw new RuntimeException("Stub!");
  }

  public static java.net.InetAddress getByAddress(java.lang.String hostName, byte[] ipAddress)
      throws java.net.UnknownHostException {
    throw new RuntimeException("Stub!");
  }
}
