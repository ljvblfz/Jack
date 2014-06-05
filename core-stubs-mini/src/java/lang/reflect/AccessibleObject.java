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

package java.lang.reflect;

public class AccessibleObject implements java.lang.reflect.AnnotatedElement {
  protected AccessibleObject() {
    throw new RuntimeException("Stub!");
  }

  public static void setAccessible(java.lang.reflect.AccessibleObject[] objects, boolean flag) {
    throw new RuntimeException("Stub!");
  }

  public boolean isAccessible() {
    throw new RuntimeException("Stub!");
  }

  public void setAccessible(boolean flag) {
    throw new RuntimeException("Stub!");
  }

  public boolean isAnnotationPresent(
      java.lang.Class<? extends java.lang.annotation.Annotation> annotationType) {
    throw new RuntimeException("Stub!");
  }

  public java.lang.annotation.Annotation[] getDeclaredAnnotations() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.annotation.Annotation[] getAnnotations() {
    throw new RuntimeException("Stub!");
  }

  public <T extends java.lang.annotation.Annotation> T getAnnotation(
      java.lang.Class<T> annotationType) {
    throw new RuntimeException("Stub!");
  }
}
