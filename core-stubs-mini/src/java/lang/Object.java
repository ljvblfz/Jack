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

public class Object {
  public Object() {
    throw new RuntimeException("Stub!");
  }

  protected java.lang.Object clone() throws java.lang.CloneNotSupportedException {
    throw new RuntimeException("Stub!");
  }

  public boolean equals(java.lang.Object o) {
    throw new RuntimeException("Stub!");
  }

  protected void finalize() throws java.lang.Throwable {
    throw new RuntimeException("Stub!");
  }

  public final native java.lang.Class<?> getClass();

  public native int hashCode();

  public final native void notify();

  public final native void notifyAll();

  public java.lang.String toString() {
    throw new RuntimeException("Stub!");
  }

  public final void wait() throws java.lang.InterruptedException {
    throw new RuntimeException("Stub!");
  }

  public final void wait(long millis) throws java.lang.InterruptedException {
    throw new RuntimeException("Stub!");
  }

  public final native void wait(long millis, int nanos) throws java.lang.InterruptedException;
}
