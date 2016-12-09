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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.Subscribe;

import com.android.jack.Jack;
import com.android.jack.LibraryException;
import com.android.jack.frontend.ParentSetter;
import com.android.jack.ir.ast.JAnnotationType;
import com.android.jack.ir.ast.JDefinedAnnotationType;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.formatter.TypePackageAndMethodFormatter;
import com.android.jack.library.HasInputLibrary;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.library.InputLibrary;
import com.android.jack.library.LibraryFormatException;
import com.android.jack.library.LibraryIOException;
import com.android.jack.library.TypeInInputLibraryLocation;
import com.android.jack.load.ClassOrInterfaceLoader;
import com.android.jack.load.JackLoadingException;
import com.android.jack.lookup.JLookupException;
import com.android.jack.management.CleanMemoryRequest;
import com.android.jack.management.Impact;
import com.android.sched.marker.Marker;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.findbugs.SuppressFBWarnings;
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
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * {@link ClassOrInterfaceLoader} for jayce files.
 */
@HasKeyId
public class JayceClassOrInterfaceLoader implements ClassOrInterfaceLoader, HasInputLibrary {
  @Nonnull
  public static final BooleanPropertyId NNODE_CACHE = BooleanPropertyId
      .create("jack.jayce.cache", "Use NNode cache")
      .addDefaultValue(Boolean.TRUE);

  @Nonnull
  private static final StatisticId<Counter> NNODE_TYPE_LOAD = new StatisticId<
      Counter>("jack.nnode-to-jnode.type", "NDeclaredType loaded in a JNode at type level",
          CounterImpl.class, Counter.class);

  @Nonnull
  private static final StatisticId<Percent> NNODE_REREAD = new StatisticId<
      Percent>("jack.jayce-to-nnode", "Jayce file reread versus total jayce file read",
          PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Percent> NNODE_CACHE_HIT = new StatisticId<Percent>(
      "jack.jayce.cache.hit", "NNode cache hit", PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Percent> NNODE_CACHE_CROSS =
      new StatisticId<Percent>("jack.jayce.cache.hit.cross-compilation",
          "NNode cache hit due to previous compilation", PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Percent> NNODE_CACHABLE = new StatisticId<Percent>(
      "jack.jayce.cache.cachable", "NNode cache possible", PercentImpl.class, Percent.class);

  @Nonnull
  private static final StatisticId<Counter> NNODE_STRUCTURE_LOAD = new StatisticId<Counter>(
      "jack.nnode-to-jnode.structure", "NDeclaredType loaded in a JNode at structure level",
          CounterImpl.class, Counter.class);

  private static class NNodeId {
    @CheckForNull
    private final String digest;
    @Nonnull
    private final String fqName;
    @Nonnull
    private final String sessionId;

    public NNodeId(@Nonnull String sessionId, @CheckForNull String digest, @Nonnull String fqName) {
      this.digest = digest;
      this.fqName = fqName;
      this.sessionId = sessionId;
    }

    public boolean isCachable() {
      return digest != null;
    }

    @Nonnull
    public String getSessionId() {
      return sessionId;
    }

    @Override
    public final boolean equals(Object obj) {
      if (obj instanceof NNodeId) {
        NNodeId id = (NNodeId) obj;

        if (digest == null || id.digest == null) {
          return false;
        }

        return digest.equals(id.digest) && fqName.equals(id.fqName);
      }

      return false;
    }

    @Override
    public final int hashCode() {
      return ((digest != null) ? digest.hashCode() : 0) ^ fqName.hashCode();
    }
  }

  @Nonnull
  private static Cache<NNodeId, DeclaredTypeNode> cache =
      CacheBuilder.newBuilder().softValues().<NNodeId, DeclaredTypeNode>build();

  static {
    Jack.getResourceRequestBus().register(new Object() {
      @SuppressFBWarnings({"UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS"})
      // Call by EventBus framework
      @Subscribe
      public void cleanMemory(@Nonnull CleanMemoryRequest event) {
        if (event.getImpacts().contains(Impact.PERFORMANCE)) {
          logger.log(Level.INFO, "Clean NNode cache on event request");
          cache.invalidateAll();
        }
      }
    });
  }

  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private final InputVFile source;

  @Nonnull
  private Reference<DeclaredTypeNode> nnode = new SoftReference<DeclaredTypeNode>(null);

  private boolean structureLoaded = false;

  private boolean annotationLoaded = false;

  @Nonnull
  private final JSession session;

  @Nonnull
  private final NodeLevel defaultLoadLevel;

  @Nonnegative
  private int nnodeReadCount = 0;

  @Nonnull
  private final InputJackLibrary inputJackLibrary;

  @Nonnull
  private final String simpleName;

  @Nonnull
  private final JPackage enclosingPackage;

  @Nonnull
  private final Location location;

  @Nonnull
  private final Object annotationLock = new Object();

  @Nonnull
  private final NNodeId id;

  @Nonnull
  final Tracer tracer = TracerFactory.getTracer();

  JayceClassOrInterfaceLoader(@Nonnull InputJackLibrary jackLibrary,
      @Nonnull JPackage enclosingPackage,
      @Nonnull String simpleName,
      @Nonnull InputVFile source,
      @Nonnull JSession session,
      @Nonnull NodeLevel defaultLoadLevel) {
    this.inputJackLibrary = jackLibrary;
    this.enclosingPackage = enclosingPackage;
    this.simpleName = simpleName;
    this.source = source;
    this.session = session;
    this.defaultLoadLevel = defaultLoadLevel;
    String fqName = Jack.getUserFriendlyFormatter().getName(enclosingPackage, simpleName);
    this.location = new TypeInInputLibraryLocation(inputJackLibrary, fqName);
    boolean cache = session.getConfig().get(NNODE_CACHE).booleanValue();
    this.id = new NNodeId(session.getConfig().getName(),
        cache ? inputJackLibrary.getDigest() : null, fqName);
  }

  @Nonnull
  public JSession getSession() {
    return session;
  }

  @Override
  @Nonnull
  public Location getLocation(@Nonnull JDefinedClassOrInterface loaded) {
    return getLocation();
  }

  @Override
  public void ensureRetentionPolicy(@Nonnull JDefinedAnnotationType loaded) {
    // done at creation
  }

  @Override
  public void ensureModifier(@Nonnull JDefinedClassOrInterface loaded) {
    // done at creation
  }

  @Override
  public void ensureAnnotations(@Nonnull JDefinedClassOrInterface loaded) {
    ensureStructure(loaded);
    synchronized (annotationLock) {
      if (!annotationLoaded) {
        annotationLoaded = true;
        DeclaredTypeNode type;
        try {
          type = getNNode(NodeLevel.STRUCTURE);
        } catch (LibraryException e) {
          throw new JackLoadingException(getLocation(), e);
        }
        try {
          type.loadAnnotations(loaded, this);
        } catch (JLookupException e) {
          throw new JackLoadingException(getLocation(), e);
        }
        ParentSetter parentSetter = new ParentSetter();
        parentSetter.accept(loaded);
        loaded.removeLoader();
      }
    }
  }

  @Override
  public void ensureHierarchy(@Nonnull JDefinedClassOrInterface loaded) {
    ensureStructure(loaded);
  }

  @Override
  public void ensureMarkers(@Nonnull JDefinedClassOrInterface loaded) {
    ensureStructure(loaded);
  }

  @Override
  public void ensureMarker(@Nonnull JDefinedClassOrInterface loaded,
      @Nonnull Class<? extends Marker> cls) {
    ensureMarkers(loaded);
  }

  @Override
  public void ensureEnclosing(@Nonnull JDefinedClassOrInterface loaded) {
    ensureStructure(loaded);
  }

  @Override
  public void ensureInners(@Nonnull JDefinedClassOrInterface loaded) {
    ensureStructure(loaded);
  }

  @Override
  public void ensureAnnotation(@Nonnull JDefinedClassOrInterface loaded,
      @Nonnull JAnnotationType annotation) {
    ensureAnnotations(loaded);
  }

  @Override
  public void ensureMethods(@Nonnull JDefinedClassOrInterface loaded) {
    ensureStructure(loaded);
  }

  @Override
  public void ensureMethod(@Nonnull JDefinedClassOrInterface loaded, @Nonnull String name,
      @Nonnull List<? extends JType> args, @Nonnull JType returnType) {
    ensureMethods(loaded);
  }

  @Override
  public void ensureFields(@Nonnull JDefinedClassOrInterface loaded) {
    ensureStructure(loaded);
  }

  @Override
  public void ensureFields(@Nonnull JDefinedClassOrInterface loaded, @Nonnull String fieldName) {
    ensureFields(loaded);
  }

  @Override
  public void ensureSourceInfo(@Nonnull JDefinedClassOrInterface loaded) {
    ensureStructure(loaded);
  }

  @Nonnull
  Location getLocation() {
    return location;
  }

  @Nonnull
  JDefinedClassOrInterface load() throws LibraryFormatException, LibraryIOException {
    if (defaultLoadLevel == NodeLevel.TYPES) {
      tracer.getStatistic(NNODE_TYPE_LOAD).incValue();
    }
    DeclaredTypeNode type = getNNode(NodeLevel.TYPES);
    assert checkName(type.getSignature());
    JDefinedClassOrInterface jType = type.create(enclosingPackage, this);
    return jType;
  }

  @Nonnull
  DeclaredTypeNode getNNode(@Nonnull NodeLevel minimumLevel) throws LibraryFormatException,
      LibraryIOException {
    DeclaredTypeNode candidate;

    if (id.isCachable()) {
      tracer.getStatistic(NNODE_CACHABLE).addTrue();
      candidate = cache.getIfPresent(id);

      if (tracer.isTracing()) {
        boolean hit = (candidate != null) && (candidate.getLevel().keep(minimumLevel));
        tracer.getStatistic(NNODE_CACHE_HIT).add(hit);
        if (hit) {
          for (Entry<NNodeId, DeclaredTypeNode> entry : cache.asMap().entrySet()) {
            if (entry.getValue() == candidate) {
              tracer.getStatistic(NNODE_CACHE_CROSS)
                  .add(!entry.getKey().getSessionId().equals(ThreadConfig.getConfig().getName()));
              break;
            }
          }
        }
      }
    } else {
      tracer.getStatistic(NNODE_CACHABLE).addFalse();
      candidate = nnode.get();
    }

    if (candidate == null || !candidate.getLevel().keep(minimumLevel)) {
      InputStream in = null;
      try {
        in = new BufferedInputStream(source.getInputStream());
        NodeLevel loadLevel = getLevelForLoading(minimumLevel);
        candidate = JayceReaderFactory.get(inputJackLibrary, in).readType(loadLevel);
        if (id.isCachable()) {
          cache.put(id, candidate);
        } else {
          nnode = new SoftReference<DeclaredTypeNode>(candidate);
        }
      } catch (IOException | WrongPermissionException e) {
        throw new LibraryIOException(inputJackLibrary.getLocation(), e);
      } catch (JayceFormatException e) {
        logger.log(Level.SEVERE,
            "Library " + inputJackLibrary.getLocation().getDescription() + " is invalid", e);
        throw new LibraryFormatException(inputJackLibrary.getLocation());
      } finally {
        try {
          if (in != null) {
            in.close();
          }
        } catch (IOException e) {
          logger.log(Level.WARNING,
              "Failed to close input stream on " + source.getLocation().getDescription(), e);
        }
      }
      tracer.getStatistic(NNODE_REREAD).add(nnodeReadCount > 0);
      nnodeReadCount++;
    }
    return candidate;
  }

  private void ensureStructure(@Nonnull JDefinedClassOrInterface loaded) {
    synchronized (this) {
      if (!structureLoaded) {
        structureLoaded = true;
        DeclaredTypeNode type;
        try {
          type = getNNode(NodeLevel.STRUCTURE);
        } catch (LibraryException e) {
          throw new JackLoadingException(getLocation(), e);
        }
        try {
          type.loadStructure(loaded, this);
        } catch (JLookupException e) {
          throw new JackLoadingException(getLocation(), e);
        }
        ParentSetter parentSetter = new ParentSetter();
        parentSetter.accept(loaded);
        tracer.getStatistic(NNODE_STRUCTURE_LOAD).incValue();
      }
    }
  }

  @Override
  @Nonnull
  public InputLibrary getInputLibrary() {
    return inputJackLibrary;
  }

  private boolean checkName(@Nonnull String signature) {
    TypePackageAndMethodFormatter lookupFormatter = Jack.getLookupFormatter();
    String expectedSignature = lookupFormatter.getName(enclosingPackage, simpleName);
    if (!signature.equals(expectedSignature)) {
      throw new AssertionError("Wrong type in '" + source.getLocation().getDescription()
          + "', found '" + signature + "' while expecting '" + expectedSignature + "'");
    }
    return true;
  }

  /**
   * Get the appropriate {@link NodeLevel} for loading a NNode according to the default load level
   * and the required data.
   * @param requiredData level of data required.
   */
  @Nonnull
  private NodeLevel getLevelForLoading(@Nonnull NodeLevel requiredData) {
    NodeLevel loadLevel = defaultLoadLevel;
    if (!loadLevel.keep(requiredData)) {
      loadLevel = requiredData;
    }
    return loadLevel;
  }
}
