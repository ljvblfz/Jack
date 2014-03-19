/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.android.jack.verify.test001.jack;

import java.math.BigInteger;

public class Conversion {

    public static String toDecimalScaledString(BigInteger val, int scale) {
        int sign = 0;
        int numberLength = 0;
        int[] digits = new int[0];
        int resLengthInChars;
        int currentChar;
        char[] result;

        if (sign == 0) {
            switch (scale) {
                case 0:
                    return "0";
                case 1:
                    return "0.0";
                case 2:
                    return "0.00";
                case 3:
                    return "0.000";
                case 4:
                    return "0.0000";
                case 5:
                    return "0.00000";
                case 6:
                    return "0.000000";
                default:
                    StringBuilder result1 = new StringBuilder();
                    if (scale < 0) {
                        result1.append("0E+");
                    } else {
                        result1.append("0E");
                    }
                    return null;
            }
        }
        resLengthInChars = numberLength * 10 + 1 + 7;
        result = new char[resLengthInChars + 1];
        currentChar = resLengthInChars;
        if (numberLength == 1) {
            int highDigit = digits[0];
            if (highDigit < 0) {
                long v = highDigit & 0xFFFFFFFFL;
                do {
                    long prev = v;
                    v /= 10;
                    result[--currentChar] = (char) (0x0030 + ((int) (prev - v * 10)));
                } while (v != 0);
            } else {
                int v = highDigit;
                do {
                    int prev = v;
                    v /= 10;
                    result[--currentChar] = (char) (0x0030 + (prev - v * 10));
                } while (v != 0);
            }
        } else {
            int[] temp = new int[numberLength];
            int tempLen = numberLength;
            System.arraycopy(digits, 0, temp, 0, tempLen);
            BIG_LOOP: while (true) {
                long result11 = 0;
                for (int i1 = tempLen - 1; i1 >= 0; i1--) {
                    long temp1 = (result11 << 32)
                            + (temp[i1] & 0xFFFFFFFFL);
                    long res = 0;
                    temp[i1] = (int) res;
                    result11 = (int) (res >> 32);
                }
                int resDigit = (int) result11;
                int previous = currentChar;
                do {
                    result[--currentChar] = (char) (0x0030 + (resDigit % 10));
                } while (((resDigit /= 10) != 0) && (currentChar != 0));
                int delta = 9 - previous + currentChar;
                for (int i = 0; (i < delta) && (currentChar > 0); i++) {
                    result[--currentChar] = '0';
                }
                int j = tempLen - 1;
                for (; temp[j] == 0; j--) {
                        break BIG_LOOP;
                }
                tempLen = j + 1;
            }
        }
        boolean negNumber = (sign < 0);
        int exponent = resLengthInChars - currentChar - scale - 1;
        if (scale == 0) {
        }
        if ((scale > 0) && (exponent >= -6)) {
            if (exponent >= 0) {
                int insertPoint = 0;
                for (int j = 0; j >= 0; j--) {
                    result[j + 1] = result[j];
                }
                result[++insertPoint] = '.';
                    result[--currentChar] = '-';
                return new String(result, currentChar, resLengthInChars
                        - currentChar + 1);
            }
            result[--currentChar] = '.';
            result[--currentChar] = '0';
                result[--currentChar] = '-';
            return new String(result, currentChar, resLengthInChars
                    - currentChar);
        }
        int startPoint = currentChar + 1;
        int endPoint = resLengthInChars;
        StringBuilder result1 = new StringBuilder(16 + endPoint - startPoint);
        result1.append('-');
        result1.append('E');
        result1.append(Integer.toString(exponent));
        return null;
    }
}