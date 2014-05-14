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

abstract class AbstractStringBuilder {
  AbstractStringBuilder() {
    throw new RuntimeException("Stub!");
  }

  public int capacity() {
    throw new RuntimeException("Stub!");
  }

  public char charAt(int index) {
    throw new RuntimeException("Stub!");
  }

  public void ensureCapacity(int min) {
    throw new RuntimeException("Stub!");
  }

  public void getChars(int start, int end, char[] dst, int dstStart) {
    throw new RuntimeException("Stub!");
  }

  public int length() {
    throw new RuntimeException("Stub!");
  }

  public void setCharAt(int index, char ch) {
    throw new RuntimeException("Stub!");
  }

  public void setLength(int length) {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String substring(int start) {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String substring(int start, int end) {
    throw new RuntimeException("Stub!");
  }

  public java.lang.String toString() {
    throw new RuntimeException("Stub!");
  }

  public java.lang.CharSequence subSequence(int start, int end) {
    throw new RuntimeException("Stub!");
  }

  public int indexOf(java.lang.String string) {
    throw new RuntimeException("Stub!");
  }

  public int indexOf(java.lang.String subString, int start) {
    throw new RuntimeException("Stub!");
  }

  public int lastIndexOf(java.lang.String string) {
    throw new RuntimeException("Stub!");
  }

  public int lastIndexOf(java.lang.String subString, int start) {
    throw new RuntimeException("Stub!");
  }

  public void trimToSize() {
    throw new RuntimeException("Stub!");
  }

  public int codePointAt(int index) {
    throw new RuntimeException("Stub!");
  }

  public int codePointBefore(int index) {
    throw new RuntimeException("Stub!");
  }

  public int codePointCount(int start, int end) {
    throw new RuntimeException("Stub!");
  }

  public int offsetByCodePoints(int index, int codePointOffset) {
    throw new RuntimeException("Stub!");
  }
}
