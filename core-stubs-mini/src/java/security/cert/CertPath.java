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

package java.security.cert;

public abstract class CertPath implements java.io.Serializable {
  protected static class CertPathRep implements java.io.Serializable {
    protected CertPathRep(java.lang.String type, byte[] data) {
      throw new RuntimeException("Stub!");
    }

    protected java.lang.Object readResolve() throws java.io.ObjectStreamException {
      throw new RuntimeException("Stub!");
    }
  }

  protected CertPath(java.lang.String type) {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String getType() {
    throw new RuntimeException("Stub!");
  }

  public boolean equals(java.lang.Object other) {
    throw new RuntimeException("Stub!");
  }

  public int hashCode() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String toString() {
    throw new RuntimeException("Stub!");
  }

  public abstract java.util.List<? extends java.security.cert.Certificate> getCertificates();

  public abstract byte[] getEncoded() throws java.security.cert.CertificateEncodingException;

  public abstract byte[] getEncoded(java.lang.String encoding)
      throws java.security.cert.CertificateEncodingException;

  public abstract java.util.Iterator<java.lang.String> getEncodings();

  protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
    throw new RuntimeException("Stub!");
  }
}
