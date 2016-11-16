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

package com.android.jack.util;

import com.android.sched.util.codec.EnumName;
import com.android.sched.util.codec.VariableName;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Android API level description
 */
public class AndroidApiLevel {
  /**
   * Released API level
   */
  public static enum ReleasedLevel {
    N_MR1(25),
    N(24),
    M(23),
    L_MR1(22),
    L(21),
    K_WATCH(20),
    K(19),
    J_MR2(18),
    J_MR1(17),
    J(16),
    I_MR1(15),
    I(14),
    H_MR2(13),
    H_MR1(12),
    H(11),
    G_MR1(10),
    G(9),
    F(8),
    E_MR1(7),
    E_0_1(6),
    E(5),
    D(4),
    C(3),
    B_1_1(2),
    B(1);

    @Nonnegative
    private final int level;

    ReleasedLevel(@Nonnegative int level) {
      this.level = level;
    }

    public int getLevel() {
      return level;
    }
  }

  /**
   * Provisional API level
   *
   * Be aware that this enumeration evolves from jack release to jack release, even during
   * release candidate / release / maintenance release cycle. So, Jack Plugins have to use this
   * enumeration with string operations or catch {@link NoSuchFieldError} when accessing it.
   * Only {@link #NONE} is guaranteed to not disappear.
   */
  @VariableName("level")
  public static enum ProvisionalLevel {
    NONE(null),
    @EnumName(name = "o-b1",
              description = "Dex file 0x37 with invoke-poly")
    O_BETA1(ReleasedLevel.N_MR1),
    @EnumName(name = "o-b2",
              description = "Dex file 0x38 with invoke-{poly,custom}")
    O_BETA2(ReleasedLevel.N_MR1);

    @Nonnegative
    private final int level;

    ProvisionalLevel(@CheckForNull AndroidApiLevel.ReleasedLevel level) {
      if (level != null) {
        this.level = level.getLevel();
      } else {
        this.level = 0;
      }
    }

    private int getLevel() {
      return level;
    }

    static {
      // Do not remove this level
      assert NONE != null;
    }
  }

  @Nonnegative
  private final int releasedLevel;
  @Nonnull
  private final AndroidApiLevel.ProvisionalLevel provisionalLevel;

  public AndroidApiLevel(@Nonnegative int level) {
    this.releasedLevel = level;
    this.provisionalLevel = ProvisionalLevel.NONE;
  }

  public AndroidApiLevel(@Nonnull AndroidApiLevel.ReleasedLevel level) {
    this.releasedLevel = level.getLevel();
    this.provisionalLevel = ProvisionalLevel.NONE;
  }

  public AndroidApiLevel(@Nonnull AndroidApiLevel.ProvisionalLevel provisionalLevel) {
    this.releasedLevel = provisionalLevel.getLevel();
    this.provisionalLevel = provisionalLevel;
  }

  public boolean isReleasedLevel() {
    return provisionalLevel == ProvisionalLevel.NONE;
  }

  @Nonnull
  public AndroidApiLevel.ProvisionalLevel getProvisionalLevel() {
    return provisionalLevel;
  }

  @Nonnegative
  public int getReleasedLevel() {
    return releasedLevel;
  }
}