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

package java.lang;

public class Package implements java.lang.reflect.AnnotatedElement {
  Package() {
    throw new RuntimeException("Stub!");
  }

  @java.lang.SuppressWarnings(value = {"unchecked"})
  public <A extends java.lang.annotation.Annotation> A getAnnotation(
      java.lang.Class<A> annotationType) {
    throw new RuntimeException("Stub!");
  }

  public java.lang.annotation.Annotation[] getAnnotations() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.annotation.Annotation[] getDeclaredAnnotations() {
    throw new RuntimeException("Stub!");
  }

  public boolean isAnnotationPresent(
      java.lang.Class<? extends java.lang.annotation.Annotation> annotationType) {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String getImplementationTitle() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String getImplementationVendor() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String getImplementationVersion() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String getName() {
    throw new RuntimeException("Stub!");
  }

  public static java.lang.Package getPackage(java.lang.String packageName) {
    throw new RuntimeException("Stub!");
  }

  public static java.lang.Package[] getPackages() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String getSpecificationTitle() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String getSpecificationVendor() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String getSpecificationVersion() {
    throw new RuntimeException("Stub!");
  }

  public int hashCode() {
    throw new RuntimeException("Stub!");
  }

  public boolean isCompatibleWith(java.lang.String version) throws java.lang.NumberFormatException {
    throw new RuntimeException("Stub!");
  }

  public boolean isSealed() {
    throw new RuntimeException("Stub!");
  }

  public boolean isSealed(java.net.URL url) {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String toString() {
    throw new RuntimeException("Stub!");
  }
}
