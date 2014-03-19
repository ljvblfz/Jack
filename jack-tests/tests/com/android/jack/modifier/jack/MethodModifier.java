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

package com.android.jack.modifier.jack;

public abstract class MethodModifier {

  public void methodPublic() {

  }

  private void methodPrivate() {

  }

  protected void methodProtected() {

  }

  static void methodStatic() {

  }

  final void methodFinal() {

  }

  public final void methodPublicFinal() {

  }

  synchronized void methodSynchronized() {

  }

  native void methodNative();

  void methodVarargs(int... values) {

  }

  private MethodModifier() {

  }

  public MethodModifier(int i) {

  }

  public abstract void methodAbstract();

  strictfp void methodStrictfp() {

  }
}
