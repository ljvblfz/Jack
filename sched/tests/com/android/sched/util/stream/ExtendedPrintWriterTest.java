/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.sched.util.stream;

import org.junit.Assert;
import org.junit.Test;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import javax.annotation.Nonnull;

public class ExtendedPrintWriterTest {
  @Test
  public void test() {
    CharArrayWriter caw;
    ExtendedPrintWriter writer;

    // Test writer with a specific line separator
    caw = new CharArrayWriter();
    writer = new ExtendedPrintWriter(caw, "<cr>");
    testPrintWriter(writer);
    writer.close();
    Assert.assertEquals(
        "true<cr>%<cr>1<cr>2<cr>3<cr>4.0<cr>5.0<cr>array<cr>string<cr>null<cr>object<cr><cr>" +
        "false&11121314.015.0ArrayStringnullObject<cr>(bufferstringrtiff<cr>)Stringtri",
        caw.toString());
    Assert.assertFalse(writer.checkError());
    try {
      writer.throwPendingException();
    } catch (IOException e) {
      System.out.println(e.getMessage());
      Assert.fail();
    }

    // IOException
    writer = new ExtendedPrintWriter(new Writer() {
      private int id = 6;

      @Override
      public void write(char[] cbuf, int off, int len) throws IOException {
        throw new IOException(String.valueOf("write-" + id++));
      }

      @Override
      public void flush() {
      }

      @Override
      public void close() {
      }
    });
    testPrintWriter(writer);
    writer.close();
    Assert.assertTrue(writer.checkError());
    try {
      writer.throwPendingException();
      Assert.fail();
    } catch (IOException e) {
      Assert.assertEquals("write-6", e.getMessage());
    }

    // throwPendingException clear the error state
    Assert.assertFalse(writer.checkError());
    try {
      writer.throwPendingException();
    } catch (IOException e) {
      Assert.fail();
    }

    // Write on closed writer
    writer.println();
    Assert.assertTrue(writer.checkError());
    try {
      writer.throwPendingException();
      Assert.fail();
    } catch (IOException e) {
      Assert.assertEquals("Writer already closed", e.getMessage());
    }
  }

  private void testPrintWriter(@Nonnull PrintWriter writer) {
    writer.println(true);
    writer.println((char) 37);
    writer.println((byte) 1);
    writer.println(2);
    writer.println((long) 3);
    writer.println((float) 4);
    writer.println((double) 5);
    writer.println(new char[]{'a', 'r', 'r', 'a', 'y'});
    writer.println("string");
    writer.println((Object) null);
    writer.println(new Object(){
      @Override
      public String toString() {
        return "object";
      }
    });

    writer.println();
    writer.print(false);
    writer.print((char) 38);
    writer.print((byte) 11);
    writer.print(12);
    writer.print((long) 13);
    writer.print((float) 14);
    writer.print((double) 15);
    writer.print(new char[]{'A', 'r', 'r', 'a', 'y'});
    writer.print("String");
    writer.print((Object) null);
    writer.print(new Object(){
      @Override
      public String toString() {
        return "Object";
      }
    });

    writer.println();
    writer.write(40);
    writer.write(new char[]{'b', 'u', 'f', 'f', 'e', 'r'});
    writer.write("string");
    writer.write("srting", 1, 3);
    writer.write(new char[]{'b', 'u', 'f', 'f', 'e', 'r'}, 2, 2);

    writer.println();
    writer.append((char)41);
    writer.append("String");
    writer.append("string", 1, 4);

    writer.close();
  }
}
