/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.ir.ast;

import javax.annotation.Nonnull;

/**
 * Number is a class allowing to abstract Byte, Character, Short, Integer, Float, Double and Long.
 */
public class Number {

  @Nonnull
  private final Object objectNumber;

  public Number(@Nonnull Object objectNumber) {
    assert objectNumber instanceof java.lang.Number || objectNumber instanceof Character;
    this.objectNumber = objectNumber;
  }

  public byte byteValue() {
    if (objectNumber instanceof Character) {
      return ((byte) ((Character) objectNumber).charValue());
    }
    return (((java.lang.Number) objectNumber).byteValue());
  }

  public short shortValue() {
    if (objectNumber instanceof Character) {
      return ((short) ((Character) objectNumber).charValue());
    }
    return (((java.lang.Number) objectNumber).shortValue());
  }

  public char charValue() {
    if (objectNumber instanceof Character) {
      return (((Character) objectNumber).charValue());
    }
    return ((char) ((java.lang.Number) objectNumber).intValue());
  }

  public int intValue() {
    if (objectNumber instanceof Character) {
      return (((Character) objectNumber).charValue());
    }
    return (((java.lang.Number) objectNumber).intValue());
  }

  public float floatValue() {
    if (objectNumber instanceof Character) {
      return (((Character) objectNumber).charValue());
    }
    return (((java.lang.Number) objectNumber).floatValue());
  }

  public long longValue() {
    if (objectNumber instanceof Character) {
      return (((Character) objectNumber).charValue());
    }
    return (((java.lang.Number) objectNumber).longValue());
  }

  public double doubleValue() {
    if (objectNumber instanceof Character) {
      return (((Character) objectNumber).charValue());
    }
    return (((java.lang.Number) objectNumber).doubleValue());
  }
}
