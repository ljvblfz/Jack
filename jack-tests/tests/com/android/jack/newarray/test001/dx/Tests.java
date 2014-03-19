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

package com.android.jack.newarray.test001.dx;

import com.android.jack.newarray.test001.jack.Data;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Arrays;

public class Tests {
  @Test
  public void testInt1() {
    int[] array = Data.getInt1();
    int[] ref = new int[12];
    Assert.assertTrue(Arrays.equals(array, ref));
  }

  @Test
  public void testInt1b() {
    int[] ref = new int[]{-2, -1, 0, 1, 2, 3};
    int[] array = Data.getInt1b();
    Assert.assertTrue(Arrays.equals(array, ref));
  }

  @Test
  public void testInt1c() {
    int[] ref = new int[15];
    int[] array = Data.getInt1(15);
    Assert.assertTrue(Arrays.equals(array, ref));
  }

  @Test
  public void testInt1d() {
    int[] ref = new int[]{-2, -1};
    int[] array = Data.getInt1d();
    Assert.assertTrue(Arrays.equals(array, ref));
  }
  @Test
  public void testInt1e() {
    int[] ref = new int[]{
        -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42,
        43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65,
        -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42,
        43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65,
        -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42,
        43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65,
        -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42,
        43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65,
        -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42,
        43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65,
        };
    int[] array = Data.getInt1e();
    Assert.assertTrue(Arrays.equals(array, ref));
  }
  @Test
  public void testInt1f() {
    int v = 157;
    int[] ref = new int[]{
        -5, -4, -3, -2, -1, v, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42,
        43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65,
        -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42,
        43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65,
        -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42,
        43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65,
        -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42,
        43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65,
        -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42,
        43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65,
        };
    int[] array = Data.getInt1f(v);
    Assert.assertTrue(Arrays.equals(array, ref));
  }

  @Test
  public void testLong1() {
    long[] ref = new long[]{12345678901l, -12345678901l};
    long[] array = Data.getLong1();
    Assert.assertTrue(Arrays.equals(array, ref));
  }

  @Test
  public void testInt2() {
    int[][] ref = new int[3][];
    int[][] array = Data.getInt2(3);
    for (int i = 0; i < array.length; i++) {
      Assert.assertTrue(Arrays.equals(array[i], ref[i]));
    }
  }

  @Test
  public void testInt2b() {
    int[][] ref = new int[][] {
        {1, 2, 3},
        {4, 5, 6, 7},
        {},
        {}
    };
    int[][] array = Data.getInt2b();
    for (int i = 0; i < array.length; i++) {
      Assert.assertTrue(Arrays.equals(array[i], ref[i]));
    }
  }

  @Test
  public void testInt2c() {
    int[][] ref = new int[3][5];
    int[][] array = Data.getInt2c(3, 5);
    for (int i = 0; i < array.length; i++) {
      Assert.assertTrue(Arrays.equals(array[i], ref[i]));

    }
  }

  @Test
  public void testObject1() {
    Object[] ref = new Object[12];
    Object[] array = Data.getObject1();
    Assert.assertTrue(Arrays.equals(array, ref));
  }

  @Test
  public void testObject1bis() {
    Object[] ref = new Object[]{"0", "1", "2", "3"};
    Object[] array = Data.getObject1(ref[0], ref[1], ref[2], ref[3]);
    Assert.assertTrue(Arrays.equals(array, ref));
  }

  @Test
  public void testObject1ter() {
    Object[] ref = new Object[13];
    Object[] array = Data.getObject1(13);
    Assert.assertTrue(Arrays.equals(array, ref));
  }

  @Test
  public void testObject1b() {
    String a = "a";
    String b = "b";
    String c = "c";
    String d = "d";
    Object[] ref = new Object[]{
        a, b, c, d, a
       };
    Object[] array = Data.getObject1b(a, b, c, d);
    Assert.assertTrue(Arrays.equals(array, ref));
  }

  @Test
  public void testObject1c() {
    String a = "a";
    String b = "b";
    String c = "c";
    String d = "d";
    Object[] ref = new Object[]{
        a, d, b, c
       };
    Object[] array = Data.getObject1c(a, b, c, d);
    Assert.assertTrue(Arrays.equals(array, ref));
  }

  @Test
  public void testObject2() {
    Object[][] ref = new Object[5][7];
    Object[][] array = Data.getObject2(5, 7);
    for (int i = 0; i < array.length; i++) {
      Assert.assertTrue(Arrays.equals(array[i], ref[i]));
    }
  }

  @Test
  public void testObject2b() {
    Object[][] ref = new Object[9][];
    Object[][] array = Data.getObject2(9);
    Assert.assertTrue(Arrays.equals(array, ref));
  }

  @Test
  public void testGetByteArray() {
    byte[] array = Data.getByteArray();
    Assert.assertEquals(2, array[2]);
  }

  @Test
  public void testGetIntArray() {
    int[] array = Data.getIntArray();
    Assert.assertEquals(2, array[0]);
  }
}
