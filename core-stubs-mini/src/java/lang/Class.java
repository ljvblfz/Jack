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

public final class Class<T>
    implements
    java.io.Serializable,
    java.lang.reflect.AnnotatedElement,
    java.lang.reflect.GenericDeclaration,
    java.lang.reflect.Type {
  Class() {
    throw new RuntimeException("Stub!");
  }

  public static java.lang.Class<?> forName(java.lang.String className)
      throws java.lang.ClassNotFoundException {
    throw new RuntimeException("Stub!");
  }

  public static java.lang.Class<?> forName(java.lang.String className, boolean shouldInitialize,
      java.lang.ClassLoader classLoader) throws java.lang.ClassNotFoundException {
    throw new RuntimeException("Stub!");
  }

  public java.lang.Class<?>[] getClasses() {
    throw new RuntimeException("Stub!");
  }

  public <A extends java.lang.annotation.Annotation> A getAnnotation(
      java.lang.Class<A> annotationType) {
    throw new RuntimeException("Stub!");
  }

  public java.lang.annotation.Annotation[] getAnnotations() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String getCanonicalName() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.ClassLoader getClassLoader() {
    throw new RuntimeException("Stub!");
  }

  public native java.lang.Class<?> getComponentType();

  @java.lang.SuppressWarnings(value = {"unchecked"})
  public java.lang.reflect.Constructor<T> getConstructor(java.lang.Class<?>... parameterTypes)
      throws java.lang.NoSuchMethodException {
    throw new RuntimeException("Stub!");
  }

  public java.lang.reflect.Constructor<?>[] getConstructors() {
    throw new RuntimeException("Stub!");
  }

  public native java.lang.annotation.Annotation[] getDeclaredAnnotations();

  public java.lang.Class<?>[] getDeclaredClasses() {
    throw new RuntimeException("Stub!");
  }

  @java.lang.SuppressWarnings(value = {"unchecked"})
  public java.lang.reflect.Constructor<T> getDeclaredConstructor(
      java.lang.Class<?>... parameterTypes) throws java.lang.NoSuchMethodException {
    throw new RuntimeException("Stub!");
  }

  public java.lang.reflect.Constructor<?>[] getDeclaredConstructors() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.reflect.Field getDeclaredField(java.lang.String name)
      throws java.lang.NoSuchFieldException {
    throw new RuntimeException("Stub!");
  }

  public java.lang.reflect.Field[] getDeclaredFields() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.reflect.Method getDeclaredMethod(java.lang.String name,
      java.lang.Class<?>... parameterTypes) throws java.lang.NoSuchMethodException {
    throw new RuntimeException("Stub!");
  }

  public java.lang.reflect.Method[] getDeclaredMethods() {
    throw new RuntimeException("Stub!");
  }

  public native java.lang.Class<?> getDeclaringClass();

  public native java.lang.Class<?> getEnclosingClass();

  public native java.lang.reflect.Constructor<?> getEnclosingConstructor();

  public native java.lang.reflect.Method getEnclosingMethod();

  @java.lang.SuppressWarnings(value = {"unchecked"})
  public T[] getEnumConstants() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.reflect.Field getField(java.lang.String name)
      throws java.lang.NoSuchFieldException {
    throw new RuntimeException("Stub!");
  }

  public java.lang.reflect.Field[] getFields() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.reflect.Type[] getGenericInterfaces() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.reflect.Type getGenericSuperclass() {
    throw new RuntimeException("Stub!");
  }

  public native java.lang.Class<?>[] getInterfaces();

  public java.lang.reflect.Method getMethod(java.lang.String name,
      java.lang.Class<?>... parameterTypes) throws java.lang.NoSuchMethodException {
    throw new RuntimeException("Stub!");
  }

  public java.lang.reflect.Method[] getMethods() {
    throw new RuntimeException("Stub!");
  }

  public int getModifiers() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String getName() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String getSimpleName() {
    throw new RuntimeException("Stub!");
  }

  public java.security.ProtectionDomain getProtectionDomain() {
    throw new RuntimeException("Stub!");
  }

  public java.net.URL getResource(java.lang.String resourceName) {
    throw new RuntimeException("Stub!");
  }

  public java.io.InputStream getResourceAsStream(java.lang.String resourceName) {
    throw new RuntimeException("Stub!");
  }

  public java.lang.Object[] getSigners() {
    throw new RuntimeException("Stub!");
  }

  public native java.lang.Class<? super T> getSuperclass();

  @java.lang.SuppressWarnings(value = {"unchecked"})
  public synchronized java.lang.reflect.TypeVariable<java.lang.Class<T>>[] getTypeParameters() {
    throw new RuntimeException("Stub!");
  }

  public boolean isAnnotation() {
    throw new RuntimeException("Stub!");
  }

  public boolean isAnnotationPresent(
      java.lang.Class<? extends java.lang.annotation.Annotation> annotationType) {
    throw new RuntimeException("Stub!");
  }

  public native boolean isAnonymousClass();

  public boolean isArray() {
    throw new RuntimeException("Stub!");
  }

  public native boolean isAssignableFrom(java.lang.Class<?> c);

  public boolean isEnum() {
    throw new RuntimeException("Stub!");
  }

  public native boolean isInstance(java.lang.Object object);

  public native boolean isInterface();

  public boolean isLocalClass() {
    throw new RuntimeException("Stub!");
  }

  public boolean isMemberClass() {
    throw new RuntimeException("Stub!");
  }

  public native boolean isPrimitive();

  public boolean isSynthetic() {
    throw new RuntimeException("Stub!");
  }

  public T newInstance() throws java.lang.InstantiationException, java.lang.IllegalAccessException {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String toString() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.Package getPackage() {
    throw new RuntimeException("Stub!");
  }

  public native boolean desiredAssertionStatus();

  @java.lang.SuppressWarnings(value = {"unchecked"})
  public <U> java.lang.Class<? extends U> asSubclass(java.lang.Class<U> c) {
    throw new RuntimeException("Stub!");
  }

  @java.lang.SuppressWarnings(value = {"unchecked"})
  public T cast(java.lang.Object obj) {
    throw new RuntimeException("Stub!");
  }
}
