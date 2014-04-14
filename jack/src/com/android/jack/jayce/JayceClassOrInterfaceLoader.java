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

import com.android.jack.Jack;
import com.android.jack.JackIOException;
import com.android.jack.frontend.ParentSetter;
import com.android.jack.ir.ast.JDefinedAnnotation;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JSession;
import com.android.jack.load.AbtractClassOrInterfaceLoader;
import com.android.jack.lookup.JPhantomLookup;
import com.android.jack.util.NamingTools;
import com.android.sched.util.location.Location;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Counter;
import com.android.sched.util.log.stats.CounterImpl;
import com.android.sched.util.log.stats.Percent;
import com.android.sched.util.log.stats.PercentImpl;
import com.android.sched.util.log.stats.StatisticId;
import com.android.sched.vfs.InputVFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * {@code ClassOrInterfaceLoader} for jayce files.
 */
public class JayceClassOrInterfaceLoader extends AbtractClassOrInterfaceLoader {
  @Nonnull
  private static final StatisticId<Percent> NNODE_RELOAD = new StatisticId<
      Percent>("jayce.reload", "Jayce file reload versus total jayce file load",
          PercentImpl.class, Percent.class);
  @Nonnull
  private static final StatisticId<Counter> STRUCTURE_LOAD = new StatisticId<Counter>(
      "jayce.structure.load", "NDeclaredType structure loaded in a JNode",
          CounterImpl.class, Counter.class);

  @Nonnull
  private final Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private final InputVFile source;

  @Nonnull
  private Reference<DeclaredTypeNode> nnode;

  private boolean structureLoaded = false;

  @Nonnull
  private final JPhantomLookup lookup;

  @Nonnull
  private final NodeLevel defaultLoadLevel;

  @Nonnegative
  private int loadCount = 0;

  @Nonnull
  final Tracer tracer = TracerFactory.getTracer();

  @Nonnull
  public static JDefinedClassOrInterface load(@Nonnull InputVFile source, @Nonnull JSession session,
      @Nonnull NodeLevel maxLevel)
      throws JayceFormatException, IOException {
    return new JayceClassOrInterfaceLoader(source, session.getPhantomLookup(),
        maxLevel).create(session);
  }

  JayceClassOrInterfaceLoader(@Nonnull InputVFile source, @Nonnull JPhantomLookup lookup,
      @Nonnull NodeLevel defaultLoadLevel) {
    this.source = source;
    this.lookup = lookup;
    nnode = new SoftReference<DeclaredTypeNode>(null);
    this.defaultLoadLevel = defaultLoadLevel;
  }

  @Nonnull
  public JPhantomLookup getLookup() {
    return lookup;
  }

  @Override
  @Nonnull
  public Location getLocation(@Nonnull JDefinedClassOrInterface loaded) {
    return getLocation();
  }

  @Override
  public void ensureRetentionPolicy(@Nonnull JDefinedAnnotation loaded) {
    // done at creation
  }

  @Override
  public void ensureModifier(@Nonnull JDefinedClassOrInterface loaded) {
    // done at creation
  }

  @Nonnull
  Location getLocation() {
    return source.getLocation();
  }

  @Nonnull
 JDefinedClassOrInterface loadClassOrInterface(@Nonnull JPackage enclosingPackage,
     @Nonnull String simpleName) throws JayceFormatException, IOException {

    DeclaredTypeNode type = getNNode(NodeLevel.TYPES);

    String expectedSignature = Jack.getLookupFormatter().getName(enclosingPackage, simpleName);
    if (!type.getSignature().equals(expectedSignature)) {
      throw new JayceFormatException("Wrong type in '" + source + "', found '"
          + type.getSignature() + "' while expecting '" + expectedSignature + "'");
    }
    JDefinedClassOrInterface jType = type.create(enclosingPackage, this);
    return jType;
  }

  @Nonnull
  private JDefinedClassOrInterface create(@Nonnull JSession session)
      throws JayceFormatException, IOException {

    DeclaredTypeNode type = getNNode(NodeLevel.TYPES);
    String packageQualifiedName = NamingTools.getPackageNameFromBinaryName(
        NamingTools.getClassBinaryNameFromDescriptor(type.getSignature()));
    JPackage pack = session.getLookup().getOrCreatePackage(packageQualifiedName);
    JDefinedClassOrInterface jType = type.create(pack, this);
    return jType;
  }

  @Nonnull
  DeclaredTypeNode getNNode(@Nonnull NodeLevel minimumLevel) throws IOException {
    DeclaredTypeNode type = nnode.get();
    if (type == null || !type.getLevel().keep(minimumLevel)) {
      InputStream in = new BufferedInputStream(source.openRead());
      try {
        JayceReader reader = new JayceReader(in);
        NodeLevel loadLevel = defaultLoadLevel;
        if (!loadLevel.keep(minimumLevel)) {
          loadLevel = minimumLevel;
        }
        type = reader.readType(loadLevel);
        nnode = new SoftReference<DeclaredTypeNode>(type);
      } finally {
        try {
          in.close();
        } catch (IOException e) {
          logger.log(Level.WARNING, "Failed to close input stream on '" + source + "'", e);
        }
      }
      tracer.getStatistic(NNODE_RELOAD).add(loadCount > 0);
      loadCount++;
    }
    return type;
  }

  @Override
  protected void ensureAll(@Nonnull JDefinedClassOrInterface loaded) {
    synchronized (this) {
      if (!structureLoaded) {
        structureLoaded = true;
        DeclaredTypeNode type;
        try {
          type = getNNode(NodeLevel.STRUCTURE);
        } catch (IOException e) {
          throw new JackIOException("Failed to load structure of '" +
              Jack.getUserFriendlyFormatter().getName(loaded) + "'", e);
        }
        type.updateToStructure(loaded, this);
        ParentSetter parentSetter = new ParentSetter();
        parentSetter.accept(loaded);
        tracer.getStatistic(STRUCTURE_LOAD).incValue();
      }
    }
  }

}
