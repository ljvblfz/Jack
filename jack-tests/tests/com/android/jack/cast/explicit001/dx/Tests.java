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

package com.android.jack.cast.explicit001.dx;

import com.android.jack.cast.explicit001.jack.Data;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Tests about casts.
 */
public class Tests {

  @Test
  public void test1() {
    Object stringValue = "stringValue";
    String string = "string";
    Object object = new Object();
    testByteValue((byte)159);
    testByteValue((byte)0);
    testByteValue((byte)-1);
    testByteValue(Byte.MAX_VALUE);
    testByteValue(Byte.MIN_VALUE);

    testLongValue(214356475546l);
    testLongValue(-1l);
    testLongValue(0l);
    testLongValue(Long.MAX_VALUE);
    testLongValue(Long.MIN_VALUE);

    testFloatValue((float) 0.0);
    testFloatValue(-1);
    testFloatValue((float) 158753.1574);
    testFloatValue(Float.MAX_VALUE);
    testFloatValue(Float.MIN_VALUE);

    Assert.assertEquals((String) stringValue, Data.objectToString(stringValue));
    Assert.assertEquals((String) string, Data.stringToString(string));
    Assert.assertEquals((Object) object, Data.objectToObject(object));
  }

  private void testLongValue(long longValue) {
    Assert.assertEquals((byte) longValue, Data.longToByte(longValue));
    Assert.assertEquals((char) longValue, Data.longToChar(longValue));
    Assert.assertEquals((short) longValue, Data.longToShort(longValue));
    Assert.assertEquals((int) longValue, Data.longToInt(longValue));
    Assert.assertEquals((long) longValue, Data.longToLong(longValue));
    Assert.assertEquals((float) longValue, Data.longToFloat(longValue), 0.0);
    Assert.assertEquals((double) longValue, Data.longToDouble(longValue), 0.0);
 }

  private void testFloatValue(float value) {
    Assert.assertEquals((byte) value, Data.floatToByte(value));
    Assert.assertEquals((char) value, Data.floatToChar(value));
    Assert.assertEquals((short) value, Data.floatToShort(value));
    Assert.assertEquals((int) value, Data.floatToInt(value));
    Assert.assertEquals((long) value, Data.floatToLong(value));
    Assert.assertEquals((float) value, Data.floatToFloat(value), 0.0);
    Assert.assertEquals((double) value, Data.floatToDouble(value), 0.0);
  }

  private void testByteValue(byte byteValue) {
    Assert.assertEquals((byte) byteValue, Data.byteToByte(byteValue));
    Assert.assertEquals((short) byteValue, Data.byteToShort(byteValue));
    Assert.assertEquals((char) byteValue, Data.byteToChar(byteValue));
    Assert.assertEquals((int) byteValue, Data.byteToInt(byteValue));
    Assert.assertEquals((long) byteValue, Data.byteToLong(byteValue));
  }

}
