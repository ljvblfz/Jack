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

package com.android.jack.jayce;

import com.android.jack.LibraryException;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.library.HasInputLibrary;
import com.android.jack.library.InputLibrary;
import com.android.jack.library.LibraryFormatException;
import com.android.jack.library.LibraryIOException;
import com.android.jack.load.AbstractParameterLoader;
import com.android.jack.load.JackLoadingException;
import com.android.sched.util.location.Location;

import java.lang.ref.SoftReference;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A loader for parameter loaded from a jayce file.
 */
public class JayceParameterLoader extends AbstractParameterLoader implements HasInputLibrary {

  @Nonnull
  private final JayceMethodLoader enclosingMethodLoader;

  @Nonnull
  private SoftReference<ParameterNode> nnode;

  @Nonnegative
  private final int parameterNodeIndex;

  private boolean isAnnotationsLoaded = false;

  public JayceParameterLoader(@Nonnull ParameterNode nnode,
      @Nonnegative int parameterNodeIndex,
      @Nonnull JayceMethodLoader enclosingMethodLoader) {
    this.enclosingMethodLoader = enclosingMethodLoader;
    this.nnode = new SoftReference<>(nnode);
    this.parameterNodeIndex = parameterNodeIndex;
  }

  @Nonnull
  private ParameterNode getNNode() throws LibraryFormatException,
      LibraryIOException {
    ParameterNode node = nnode.get();
    if (node == null) {
      MethodNode declaredMethodNode = enclosingMethodLoader.getNNode(NodeLevel.STRUCTURE);
      node = declaredMethodNode.getParameterNode(parameterNodeIndex);
      nnode = new SoftReference<ParameterNode>(node);
    }
    return node;
  }

  @Override
  @Nonnull
  public Location getLocation(@Nonnull JParameter loaded) {
    return enclosingMethodLoader.getLocation(loaded.getEnclosingMethod());
  }

  @Override
  protected void ensureAll(@Nonnull JParameter loaded) {
    // ensureMarkers and ensureAnnotations are implemented, ensureAll, should never be called.
    throw new UnsupportedOperationException();
  }

  @Override
  public void ensureMarkers(@Nonnull JParameter loaded) {
    // Nothing to do, markers are loaded at creation.
  }

  @Override
  public void ensureAnnotations(@Nonnull JParameter loaded) {
    synchronized (this) {
      if (isAnnotationsLoaded) {
        return;
      }
      ParameterNode node;
      try {
        node = getNNode();
        node.loadAnnotations(loaded);
      } catch (LibraryException e) {
        throw new JackLoadingException(getLocation(loaded), e);
      }
      isAnnotationsLoaded = true;
    }
    loaded.removeLoader();
  }

  @Override
  @Nonnull
  public InputLibrary getInputLibrary() {
    return enclosingMethodLoader.getInputLibrary();
  }
}
