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
import com.android.jack.ir.ast.JAnnotationType;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JSession;
import com.android.jack.library.HasInputLibrary;
import com.android.jack.library.InputLibrary;
import com.android.jack.library.LibraryFormatException;
import com.android.jack.library.LibraryIOException;
import com.android.jack.load.FieldLoader;
import com.android.jack.load.JackLoadingException;
import com.android.sched.marker.Marker;
import com.android.sched.util.location.Location;

import java.lang.ref.SoftReference;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A loader for method loaded from a jayce file.
 */
public class JayceFieldLoader implements FieldLoader, HasInputLibrary {

  @Nonnull
  private final JayceClassOrInterfaceLoader enclosingClassLoader;

  @Nonnull
  private SoftReference<FieldNode> nnode;

  @Nonnegative
  private final int fieldNodeIndex;

  private boolean isAnnotationsLoaded = false;

  public JayceFieldLoader(@Nonnull FieldNode nnode,
      @Nonnegative int fieldNodeIndex,
      @Nonnull JayceClassOrInterfaceLoader enclosingClassLoader) {
    this.enclosingClassLoader = enclosingClassLoader;
    this.nnode = new SoftReference<>(nnode);
    this.fieldNodeIndex = fieldNodeIndex;
  }

  @Nonnull
  private FieldNode getNNode() throws LibraryFormatException,
      LibraryIOException {
    FieldNode node = nnode.get();
    if (node == null) {
      DeclaredTypeNode declaredTypeNode = enclosingClassLoader.getNNode(NodeLevel.STRUCTURE);
      node = declaredTypeNode.getFieldNode(fieldNodeIndex);
      nnode = new SoftReference<FieldNode>(node);
    }
    return node;
  }

  @Override
  @Nonnull
  public Location getLocation(@Nonnull JField loaded) {
    return enclosingClassLoader.getLocation();
  }

  @Override
  public void ensureMarkers(@Nonnull JField loaded) {
    // Nothing to do, markers are loaded at creation.
  }

  @Override
  public void ensureAnnotations(@Nonnull JField loaded) {
    synchronized (this) {
      if (isAnnotationsLoaded) {
        return;
      }
      FieldNode node;
      try {
        node = getNNode();
        node.loadAnnotations(loaded, this);
      } catch (LibraryException e) {
        throw new JackLoadingException(getLocation(loaded), e);
      }
      isAnnotationsLoaded = true;
    }
  }

  @Override
  @Nonnull
  public InputLibrary getInputLibrary() {
    return enclosingClassLoader.getInputLibrary();
  }

  @Override
  public void ensureMarker(@Nonnull JField loaded, @Nonnull Class<? extends Marker> cls) {
    ensureMarkers(loaded);
  }

  @Override
  public void ensureAnnotation(@Nonnull JField loaded, @Nonnull JAnnotationType annotation) {
    ensureAnnotations(loaded);
  }

  @Nonnull
  public JSession getSession() {
    return enclosingClassLoader.getSession();
  }
}
