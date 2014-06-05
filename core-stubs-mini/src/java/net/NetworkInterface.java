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

public final class NetworkInterface {
  NetworkInterface() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String getName() {
    throw new RuntimeException("Stub!");
  }

  public java.util.Enumeration<java.net.InetAddress> getInetAddresses() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String getDisplayName() {
    throw new RuntimeException("Stub!");
  }

  public static java.net.NetworkInterface getByName(java.lang.String interfaceName)
      throws java.net.SocketException {
    throw new RuntimeException("Stub!");
  }

  public static java.net.NetworkInterface getByInetAddress(java.net.InetAddress address)
      throws java.net.SocketException {
    throw new RuntimeException("Stub!");
  }

  public static java.util.Enumeration<java.net.NetworkInterface> getNetworkInterfaces()
      throws java.net.SocketException {
    throw new RuntimeException("Stub!");
  }

  public boolean equals(java.lang.Object obj) {
    throw new RuntimeException("Stub!");
  }

  public int hashCode() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String toString() {
    throw new RuntimeException("Stub!");
  }

  public java.util.List<java.net.InterfaceAddress> getInterfaceAddresses() {
    throw new RuntimeException("Stub!");
  }

  public java.util.Enumeration<java.net.NetworkInterface> getSubInterfaces() {
    throw new RuntimeException("Stub!");
  }

  public java.net.NetworkInterface getParent() {
    throw new RuntimeException("Stub!");
  }

  public boolean isUp() throws java.net.SocketException {
    throw new RuntimeException("Stub!");
  }

  public boolean isLoopback() throws java.net.SocketException {
    throw new RuntimeException("Stub!");
  }

  public boolean isPointToPoint() throws java.net.SocketException {
    throw new RuntimeException("Stub!");
  }

  public boolean supportsMulticast() throws java.net.SocketException {
    throw new RuntimeException("Stub!");
  }

  public byte[] getHardwareAddress() throws java.net.SocketException {
    throw new RuntimeException("Stub!");
  }

  public int getMTU() throws java.net.SocketException {
    throw new RuntimeException("Stub!");
  }

  public boolean isVirtual() {
    throw new RuntimeException("Stub!");
  }
}
