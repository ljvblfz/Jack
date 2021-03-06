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

package java.io;

public class PrintWriter extends java.io.Writer {
  public PrintWriter(java.io.OutputStream out) {
    throw new RuntimeException("Stub!");
  }

  public PrintWriter(java.io.OutputStream out, boolean autoFlush) {
    throw new RuntimeException("Stub!");
  }

  public PrintWriter(java.io.Writer wr) {
    throw new RuntimeException("Stub!");
  }

  public PrintWriter(java.io.Writer wr, boolean autoFlush) {
    throw new RuntimeException("Stub!");
  }

  public PrintWriter(java.io.File file) throws java.io.FileNotFoundException {
    throw new RuntimeException("Stub!");
  }

  public PrintWriter(java.io.File file, java.lang.String csn) throws java.io.FileNotFoundException,
      java.io.UnsupportedEncodingException {
    throw new RuntimeException("Stub!");
  }

  public PrintWriter(java.lang.String fileName) throws java.io.FileNotFoundException {
    throw new RuntimeException("Stub!");
  }

  public PrintWriter(java.lang.String fileName, java.lang.String csn)
      throws java.io.FileNotFoundException, java.io.UnsupportedEncodingException {
    throw new RuntimeException("Stub!");
  }

  public boolean checkError() {
    throw new RuntimeException("Stub!");
  }

  protected void clearError() {
    throw new RuntimeException("Stub!");
  }

  public void close() {
    throw new RuntimeException("Stub!");
  }

  public void flush() {
    throw new RuntimeException("Stub!");
  }

  public java.io.PrintWriter format(java.lang.String format, java.lang.Object... args) {
    throw new RuntimeException("Stub!");
  }

  public java.io.PrintWriter format(java.util.Locale l, java.lang.String format,
      java.lang.Object... args) {
    throw new RuntimeException("Stub!");
  }

  public java.io.PrintWriter printf(java.lang.String format, java.lang.Object... args) {
    throw new RuntimeException("Stub!");
  }

  public java.io.PrintWriter printf(java.util.Locale l, java.lang.String format,
      java.lang.Object... args) {
    throw new RuntimeException("Stub!");
  }

  public void print(char[] charArray) {
    throw new RuntimeException("Stub!");
  }

  public void print(char ch) {
    throw new RuntimeException("Stub!");
  }

  public void print(double dnum) {
    throw new RuntimeException("Stub!");
  }

  public void print(float fnum) {
    throw new RuntimeException("Stub!");
  }

  public void print(int inum) {
    throw new RuntimeException("Stub!");
  }

  public void print(long lnum) {
    throw new RuntimeException("Stub!");
  }

  public void print(java.lang.Object obj) {
    throw new RuntimeException("Stub!");
  }

  public void print(java.lang.String str) {
    throw new RuntimeException("Stub!");
  }

  public void print(boolean bool) {
    throw new RuntimeException("Stub!");
  }

  public void println() {
    throw new RuntimeException("Stub!");
  }

  public void println(char[] chars) {
    throw new RuntimeException("Stub!");
  }

  public void println(char c) {
    throw new RuntimeException("Stub!");
  }

  public void println(double d) {
    throw new RuntimeException("Stub!");
  }

  public void println(float f) {
    throw new RuntimeException("Stub!");
  }

  public void println(int i) {
    throw new RuntimeException("Stub!");
  }

  public void println(long l) {
    throw new RuntimeException("Stub!");
  }

  public void println(java.lang.Object obj) {
    throw new RuntimeException("Stub!");
  }

  public void println(java.lang.String str) {
    throw new RuntimeException("Stub!");
  }

  public void println(boolean b) {
    throw new RuntimeException("Stub!");
  }

  protected void setError() {
    throw new RuntimeException("Stub!");
  }

  public void write(char[] buf) {
    throw new RuntimeException("Stub!");
  }

  public void write(char[] buf, int offset, int count) {
    throw new RuntimeException("Stub!");
  }

  public void write(int oneChar) {
    throw new RuntimeException("Stub!");
  }

  public void write(java.lang.String str) {
    throw new RuntimeException("Stub!");
  }

  public void write(java.lang.String str, int offset, int count) {
    throw new RuntimeException("Stub!");
  }

  public java.io.PrintWriter append(char c) {
    throw new RuntimeException("Stub!");
  }

  public java.io.PrintWriter append(java.lang.CharSequence csq) {
    throw new RuntimeException("Stub!");
  }

  public java.io.PrintWriter append(java.lang.CharSequence csq, int start, int end) {
    throw new RuntimeException("Stub!");
  }

  protected java.io.Writer out;
}
