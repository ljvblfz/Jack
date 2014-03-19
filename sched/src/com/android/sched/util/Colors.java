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

package com.android.sched.util;

import java.awt.Color;
import java.util.Random;

import javax.annotation.Nonnull;

/**
 * Utility class to manipulate Color.
 */
public class Colors {
  /**
   * Return a valid string describing a color valid for Css.
   * @param color the color
   * @return the string describing the color
   */
  @Nonnull
  public static String getCssColor(@Nonnull Color color) {
    StringBuffer buffer = new StringBuffer(7);
    buffer.append('#');
    buffer.append(getHexColorComponent(color.getRed()));
    buffer.append(getHexColorComponent(color.getGreen()));
    buffer.append(getHexColorComponent(color.getBlue()));

    return buffer.toString();
  }

  @Nonnull
  private static String getHexColorComponent(int colorComponent) {
    String hex = Integer.toString(colorComponent, 16);

    // Make sure hex value is two digits
    if (hex.length() == 1) {
      hex = "0" + hex;
    }

    return hex;
  }

  /**
   * Create a random pastel color.
   *
   * @return a color
   */
  @Nonnull
  public static Color getRandomPastel() {
    Random random = new Random();

    final float hue = random.nextFloat();
    final float saturation = 0.9f; // 1.0 for brilliant, 0.0 for dull
    final float luminance = 1.0f;  // 1.0 for brighter, 0.0 for black

    return Color.getHSBColor(hue, saturation, luminance);
  }

  /**
   * Create a random pastel color based on a seed.
   *
   * @param seed the seed
   * @return a color
   */
  @Nonnull
  public static Color getRandomPastel(int seed) {
    final float hue;
    if (seed != 0) {
      hue = 1.0F / ((((long) seed) << 32) >>> 32); // Remove the sign
    } else {
      hue = 1.0F;
    }

    final float saturation = 0.9f; // 1.0 for brilliant, 0.0 for dull
    final float luminance = 1.0f;  // 1.0 for brighter, 0.0 for black

    return Color.getHSBColor(hue, saturation, luminance);
  }

  private Colors() {}
}
