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

package java.util;

public class Properties extends java.util.Hashtable<java.lang.Object, java.lang.Object> {
  public Properties() {
    throw new RuntimeException("Stub!");
  }

  public Properties(java.util.Properties properties) {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String getProperty(java.lang.String name) {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String getProperty(java.lang.String name, java.lang.String defaultValue) {
    throw new RuntimeException("Stub!");
  }

  public void list(java.io.PrintStream out) {
    throw new RuntimeException("Stub!");
  }

  public void list(java.io.PrintWriter out) {
    throw new RuntimeException("Stub!");
  }

  public synchronized void load(java.io.InputStream in) throws java.io.IOException {
    throw new RuntimeException("Stub!");
  }

  @java.lang.SuppressWarnings(value = {"fallthrough"})
  public synchronized void load(java.io.Reader in) throws java.io.IOException {
    throw new RuntimeException("Stub!");
  }

  public java.util.Enumeration<?> propertyNames() {
    throw new RuntimeException("Stub!");
  }

  public java.util.Set<java.lang.String> stringPropertyNames() {
    throw new RuntimeException("Stub!");
  }

  @java.lang.Deprecated()
  public void save(java.io.OutputStream out, java.lang.String comment) {
    throw new RuntimeException("Stub!");
  }

  public java.lang.Object setProperty(java.lang.String name, java.lang.String value) {
    throw new RuntimeException("Stub!");
  }

  public synchronized void store(java.io.OutputStream out, java.lang.String comment)
      throws java.io.IOException {
    throw new RuntimeException("Stub!");
  }

  public synchronized void store(java.io.Writer writer, java.lang.String comment)
      throws java.io.IOException {
    throw new RuntimeException("Stub!");
  }

  public synchronized void loadFromXML(java.io.InputStream in) throws java.io.IOException,
      java.util.InvalidPropertiesFormatException {
    throw new RuntimeException("Stub!");
  }

  public void storeToXML(java.io.OutputStream os, java.lang.String comment)
      throws java.io.IOException {
    throw new RuntimeException("Stub!");
  }

  public synchronized void storeToXML(java.io.OutputStream os, java.lang.String comment,
      java.lang.String encoding) throws java.io.IOException {
    throw new RuntimeException("Stub!");
  }

  protected java.util.Properties defaults;
}
