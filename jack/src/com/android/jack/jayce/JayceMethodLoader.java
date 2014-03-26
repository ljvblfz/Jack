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

package com.android.jack.jayce;

import com.android.jack.JackFileException;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JNode;
import com.android.jack.load.AbstractMethodLoader;
import com.android.jack.load.JackLoadingException;
import com.android.jack.lookup.JLookupException;
import com.android.sched.util.location.Location;
import com.android.sched.util.log.stats.Counter;
import com.android.sched.util.log.stats.CounterImpl;
import com.android.sched.util.log.stats.StatisticId;

import java.io.IOException;
import java.lang.ref.SoftReference;

import javax.annotation.Nonnull;

/**
 * A loader for method loaded from a jack file.
 */
public class JayceMethodLoader extends AbstractMethodLoader {
  @Nonnull
  private static final StatisticId<Counter> BODY_LOAD_COUNT = new StatisticId<Counter>(
      "jayce.body.load", "Body loaded from a NNode in a JNode",
          CounterImpl.class, Counter.class);

  @Nonnull
  private final JayceClassOrInterfaceLoader enclosingClassLoader;

  @Nonnull
  private final SoftReference<MethodNode> nnode;

  private boolean isLoaded = false;

  public JayceMethodLoader(@Nonnull MethodNode nnode,
      @Nonnull JayceClassOrInterfaceLoader enclosingClassLoader) {
    this.enclosingClassLoader = enclosingClassLoader;
    this.nnode = new SoftReference<MethodNode>(nnode);
  }

  @Override
  public void ensureBody(@Nonnull JMethod loaded) {
    synchronized (this) {
      if (isLoaded) {
        return;
      }
      MethodNode methodNode;
      try {
        methodNode = getNNode(loaded);
      } catch (JackFileException e) {
        throw new JackLoadingException(getLocation(loaded), e);
      } catch (IOException e) {
        throw new JackLoadingException(getLocation(loaded), e);
      }
      JNode body;
      try {
        body = methodNode.loadBody(loaded);
      } catch (JLookupException e) {
        throw new JackLoadingException(getLocation(loaded), e);
      }
      if (body != null) {
        body.updateParents(loaded);
      }
      isLoaded = true;
      enclosingClassLoader.tracer.getStatistic(BODY_LOAD_COUNT).incValue();
    }
    loaded.removeLoader();
    enclosingClassLoader.notifyMethodLoaded(loaded.getEnclosingType());
  }

  public void loadFully(@Nonnull JMethod loaded) {
    ensureBody(loaded);
  }

  @Nonnull
  private MethodNode getNNode(@Nonnull JMethod loaded) throws JayceFormatException,
      JayceVersionException, IOException {
    MethodNode methodNode = nnode.get();
    if (methodNode == null || methodNode.getLevel() != NodeLevel.FULL) {
      DeclaredTypeNode declaredTypeNode = enclosingClassLoader.getNNode(NodeLevel.FULL);
      methodNode = declaredTypeNode.getMethodNode(loaded);
    }
    return methodNode;
  }

  @Override
  @Nonnull
  public Location getLocation(@Nonnull JMethod loaded) {
    return enclosingClassLoader.getLocation();
  }

  @Override
  protected void ensureAll(@Nonnull JMethod loaded) {
    // nothing to do, only body is lazily loaded and ensureBody is handling that.
  }

}
