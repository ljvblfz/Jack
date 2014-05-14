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

package java.util;

public interface ListIterator<E> extends java.util.Iterator<E> {
  public abstract void add(E object);

  public abstract boolean hasNext();

  public abstract boolean hasPrevious();

  public abstract E next();

  public abstract int nextIndex();

  public abstract E previous();

  public abstract int previousIndex();

  public abstract void remove();

  public abstract void set(E object);
}
