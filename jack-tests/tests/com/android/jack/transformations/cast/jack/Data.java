/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.transformations.cast.jack;

import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.text.CharacterIterator;

@SuppressWarnings("cast")
public class Data {

  public static Object[] castObjectToArray(Object s) {
    return (Object[]) s;
  }

  public static Object castObjectToObject(Object s) {
    return (Object) s;
  }

  public static Object castStringToObject(String s) {
    return (Object) s;
  }

  public static Serializable castStringToSerializable(String s) {
    return (Serializable) s;
  }

  public static Cloneable castArrayToCloneable(int[] a) {
    return (Cloneable) a;
  }

  public static String castNullToString() {
    return (String) null;
  }

  public static Throwable castExceptionToThrowable(Exception e) {
    return (Throwable) e;
  }

  public static Cloneable castCharacterIteratorToCloneable(CharacterIterator c) {
    return (Cloneable) c;
  }

  public static Object[] castMultiToObjectArray(int[][] a) {
    return (Object[]) a;
  }

  public static Object[] castStringArrayToObjectArray(String[] a) {
    return (Object[]) a;
  }

  public static InetAddress castExternalTypes(Inet4Address ia) {
    return (InetAddress) ia;
  }
}
