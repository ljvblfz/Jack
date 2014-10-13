/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.ir.ast;

import com.android.jack.ir.JNodeInternalError;
import com.android.jack.ir.StringInterner;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.load.PackageLoader;
import com.android.jack.lookup.JLookupException;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;
import com.android.sched.util.location.Location;
import com.android.sched.util.log.stats.Counter;
import com.android.sched.util.log.stats.CounterImpl;
import com.android.sched.util.log.stats.StatisticId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Represents a Java package
 */
@Description("Represents a Java package")
public class JPackage extends JNode implements HasName, CanBeRenamed, HasEnclosingPackage {

  private static enum OnPath {
    NOT_YET_AVAILABLE,
    TRUE,
    FALSE;
  }

  @Nonnull
  public static final StatisticId<Counter> PACKAGE_CREATION = new StatisticId<Counter>(
      "jack.package.create", "Created JPackage",
      CounterImpl.class, Counter.class);

  @Nonnull
  public static final StatisticId<Counter> PHANTOM_CREATION = new StatisticId<Counter>(
      "jack.phantom.create", "Created phantom class or interface",
      CounterImpl.class, Counter.class);

  @CheckForNull
  private JPackage enclosingPackage;

  @Nonnull
  private final List<JPackage> subPackages = new ArrayList<JPackage>();

  @Nonnull
  private final List<JDefinedClassOrInterface> declaredTypes =
    new ArrayList<JDefinedClassOrInterface>();

  @Nonnull
  private final List<JPhantomClassOrInterface> phantomTypes =
    new ArrayList<JPhantomClassOrInterface>();

  @Nonnull
  private final List<JPhantomClass> phantomClasses = new ArrayList<JPhantomClass>();

  @Nonnull
  private final List<JPhantomEnum> phantomEnums = new ArrayList<JPhantomEnum>();

  @Nonnull
  private final List<JPhantomInterface> phantomInterfaces = new ArrayList<JPhantomInterface>();

  @Nonnull
  private final List<JPhantomAnnotation> phantomAnnotations = new ArrayList<JPhantomAnnotation>();

  @Nonnull
  private String name;

  @Nonnull
  private final JSession session;

  @Nonnull
  private final List<PackageLoader> loaders =
      new LinkedList<PackageLoader>();

  private OnPath isOnPath = OnPath.NOT_YET_AVAILABLE;

  public JPackage(
      @Nonnull String name, @Nonnull JSession session, @CheckForNull JPackage enclosingPackage) {
    this(name, session, enclosingPackage, Collections.<PackageLoader>emptyList());
  }

  public JPackage(@Nonnull String name, @Nonnull JSession session,
      @CheckForNull JPackage enclosingPackage,
      @Nonnull List<PackageLoader> loaders) {
    super(SourceInfo.UNKNOWN);
    this.session = session;
    this.name = StringInterner.get().intern(name);
    this.loaders.addAll(loaders);
    if (enclosingPackage != null) {
      assert !name.isEmpty();
      this.enclosingPackage = enclosingPackage;
      this.enclosingPackage.addPackage(this);
    }
    session.getTracer().getStatistic(PACKAGE_CREATION).incValue();
  }

  public void addType(@Nonnull JDefinedClassOrInterface type) {
    declaredTypes.add(type);
  }

  public void addPackage(@Nonnull JPackage newPackage) {
    subPackages.add(newPackage);
  }

  public void add(@Nonnull HasEnclosingPackage node) {
    if (node instanceof JDefinedClassOrInterface) {
      addType((JDefinedClassOrInterface) node);
    } else if (node instanceof JPackage) {
      addPackage((JPackage) node);
    } else {
      throw new AssertionError("Not supported");
    }
  }

  @Nonnull
  public List<JPackage> getSubPackages() {
    loadSubPackages();
    return subPackages;
  }

  @Nonnull
  public List<JDefinedClassOrInterface> getTypes() {
    loadClassesAndInterfaces();
    return declaredTypes;
  }

  @Override
  @CheckForNull
  public JPackage getEnclosingPackage() {
    return enclosingPackage;
  }

  @Override
  public void setEnclosingPackage(@CheckForNull JPackage enclosingPackage) {
    this.enclosingPackage = enclosingPackage;
  }

  public boolean isTopLevelPackage() {
    return enclosingPackage == null;
  }

  @Nonnull
  public synchronized JPackage getSubPackage(@Nonnull String packageName)
      throws JPackageLookupException {
    for (JPackage f : subPackages) {
      if (f.name.equals(packageName)) {
        return f;
      }
    }

    return loadSubPackage(packageName);
  }

  @Nonnull
  public synchronized JPackage getOrCreateSubPackage(@Nonnull String packageName) {
    try {
      return getSubPackage(packageName);
    } catch (JPackageLookupException e) {
      assert !packageName.isEmpty();
      JPackage newPackage = new JPackage(packageName, session, this);
      newPackage.updateParents(this);
      return newPackage;
    }
  }

  @Nonnull
  public synchronized JDefinedClassOrInterface getType(@Nonnull String typeName)
      throws JTypeLookupException {
    for (JDefinedClassOrInterface type : declaredTypes) {
      if (type.getName().equals(typeName)) {
        return type;
      }
    }

    return loadClassOrInterface(typeName);
  }

  public void setOnPath() {
    this.isOnPath = OnPath.TRUE;
  }

  /**
   * Return true if this JPackage is defined in bootclasspath, classpath or a sourcepath.
   */
  public boolean isOnPath() {
    if (isOnPath == OnPath.NOT_YET_AVAILABLE) {
      isOnPath = OnPath.FALSE;
      for (PackageLoader loader : loaders) {
        if (loader.isOnPath(this)) {
          isOnPath = OnPath.TRUE;
          break;
        }
      }
    }
    return isOnPath == OnPath.TRUE;
  }

  @Nonnull
  public synchronized JClassOrInterface getPhantomClassOrInterface(@Nonnull String typeName) {
    try {
      return getType(typeName);
    } catch (JLookupException e) {
      for (JPhantomClassOrInterface f : phantomTypes) {
        if (f.name.equals(typeName)) {
          return f;
        }
      }
      JPhantomClassOrInterface phantom = new JPhantomClassOrInterface(typeName, this);
      phantomTypes.add(phantom);
      session.getTracer().getStatistic(PHANTOM_CREATION).incValue();
      return phantom;
    }
  }

  @Nonnull
  public synchronized JClass getPhantomClass(@Nonnull String typeName) {
    try {
      JDefinedClassOrInterface defined = getType(typeName);
      if (defined instanceof JClass) {
        return (JClass) defined;
      }
    } catch (JLookupException e) {
      // ignore
    }
    for (JPhantomClass f : phantomClasses) {
      if (f.name.equals(typeName)) {
        return f;
      }
    }
    JPhantomClass phantom = new JPhantomClass(typeName, this);
    phantomClasses.add(phantom);
    session.getTracer().getStatistic(PHANTOM_CREATION).incValue();
    return phantom;
  }

  @Nonnull
  public synchronized JEnum getPhantomEnum(@Nonnull String typeName) {
    try {
      JDefinedClassOrInterface defined = getType(typeName);
      if (defined instanceof JEnum) {
        return (JEnum) defined;
      }
    } catch (JLookupException e) {
      // ignore
    }
    for (JPhantomEnum f : phantomEnums) {
      if (f.name.equals(typeName)) {
        return f;
      }
    }
    JPhantomEnum phantom = new JPhantomEnum(typeName, this);
    phantomEnums.add(phantom);
    session.getTracer().getStatistic(PHANTOM_CREATION).incValue();
    return phantom;
  }

  @Nonnull
  public synchronized JInterface getPhantomInterface(@Nonnull String typeName) {
    try {
      JDefinedClassOrInterface defined = getType(typeName);
      if (defined instanceof JInterface) {
        return (JInterface) defined;
      }
    } catch (JLookupException e) {
      // ignore
    }
    for (JPhantomInterface f : phantomInterfaces) {
      if (f.name.equals(typeName)) {
        return f;
      }
    }
    JPhantomInterface phantom = new JPhantomInterface(typeName, this);
    phantomInterfaces.add(phantom);
    session.getTracer().getStatistic(PHANTOM_CREATION).incValue();
    return phantom;
  }

  @Nonnull
  public synchronized JAnnotation getPhantomAnnotation(@Nonnull String typeName) {
    try {
      JDefinedClassOrInterface defined = getType(typeName);
      if (defined instanceof JAnnotation) {
        return (JAnnotation) defined;
      }
    } catch (JLookupException e) {
      // ignore
    }
    for (JPhantomAnnotation f : phantomAnnotations) {
      if (f.name.equals(typeName)) {
        return f;
      }
    }
    JPhantomAnnotation phantom = new JPhantomAnnotation(typeName, this);
    phantomAnnotations.add(phantom);
    session.getTracer().getStatistic(PHANTOM_CREATION).incValue();
    return phantom;
  }

  @Override
  public void setName(@Nonnull String name) {
    this.name = StringInterner.get().intern(name);
  }

  public boolean isDefaultPackage() {
    return name.equals("");
  }

  @Nonnull
  public JSession getSession() {
    return session;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      if (visitor.needLoading()) {
        loadSubPackages();
        loadClassesAndInterfaces();
      }
      visitor.accept(subPackages);
      visitor.accept(declaredTypes);
    }
    visitor.endVisit(this);
  }

  @Override
  protected void transform(@Nonnull JNode existingNode, @CheckForNull JNode newNode,
      @Nonnull Transformation transformation) throws UnsupportedOperationException {
    boolean found = false;
    if (existingNode instanceof JPackage) {
      found =
          transform(this.subPackages, existingNode, (JPackage) newNode, transformation);
    } else if (existingNode instanceof JDefinedClassOrInterface) {
      found =
          transform(this.declaredTypes, existingNode, (JDefinedClassOrInterface) newNode,
              transformation);
    } else if (existingNode instanceof JPhantomClassOrInterface) {
      found =
          transform(this.phantomTypes, existingNode, (JPhantomClassOrInterface) newNode,
              transformation);
      if (!found) {
        if (existingNode instanceof JPhantomInterface) {
          found =
              transform(this.phantomInterfaces, existingNode, (JPhantomInterface) newNode,
                  transformation);
          if (!found) {
            if (newNode instanceof JPhantomAnnotation) {
              found =
                  transform(this.phantomAnnotations, existingNode, (JPhantomAnnotation) newNode,
                      transformation);
            }
          }
        } else if (existingNode instanceof JPhantomClass) {
          found =
              transform(this.phantomClasses, existingNode, (JPhantomClass) newNode, transformation);
          if ((!found) && (existingNode instanceof JPhantomEnum)) {
            found =
                transform(this.phantomEnums, existingNode, (JPhantomEnum) newNode, transformation);
           }
        }
      }
    }

    if (!found) {
      super.transform(existingNode, newNode, transformation);
    }
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    for (JPackage child : subPackages) {
      child.traverse(schedule);
    }
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }

  @Override
  public String getName() {
    return name;
  }

  public Collection<? extends JDefinedClassOrInterface> getLoadedTypes() {
    return declaredTypes;
  }

  @Nonnull
  public JPackage addLoader(@Nonnull PackageLoader loader) {
    loaders.add(loader);
    return this;
  }

  @Nonnull
  public List<Location> getLocations(@Nonnull JPackage loaded) {
    List<Location> locations = new ArrayList<Location>(loaders.size());
    for (PackageLoader loader : loaders) {
      locations.add(loader.getLocation(loaded));
    }
    return locations;
  }

  @Nonnull
  protected JPackage loadSubPackage(@Nonnull String simpleName)
      throws JPackageLookupException {
    List<PackageLoader> subLoaders = null;
    for (PackageLoader loader : loaders) {
      try {
        PackageLoader subLoader =
            loader.getLoaderForSubPackage(this, simpleName);
        if (subLoaders == null) {
          subLoaders = new LinkedList<PackageLoader>();
        }
        subLoaders.add(subLoader);
      } catch (JPackageLookupException e) {
        // ignore
      }
    }
    if (subLoaders != null) {
      JPackage subPackage = new JPackage(simpleName, getSession(), this, subLoaders);
      subPackage.updateParents(this);
      return subPackage;
    } else {
      throw new JPackageLookupException(simpleName, this);
    }
  }

  protected void loadSubPackages() {
    HashSet<String> subNames = new HashSet<String>();
    for (PackageLoader loader : loaders) {
      subNames.addAll(loader.getSubPackageNames(this));
    }

    for (String name : subNames) {
      try {
        getSubPackage(name);
      } catch (JPackageLookupException e) {
        // We know the packages exist so this should not happen
        throw new AssertionError(e);
      }
    }
  }

  @Nonnull
  protected JDefinedClassOrInterface loadClassOrInterface(
      @Nonnull String simpleName) throws JTypeLookupException {
    for (PackageLoader loader : loaders) {
      try {
        return loader.loadClassOrInterface(this, simpleName);
      } catch (JLookupException e) {
        // ignore
      }
    }
    throw new MissingJTypeLookupException(this, simpleName);
  }

  protected void loadClassesAndInterfaces() {
    HashSet<String> subNames = new HashSet<String>();
    for (PackageLoader loader : loaders) {
      subNames.addAll(loader.getSubClassNames(this));
    }

    for (String name : subNames) {
      try {
        getType(name);
      } catch (JTypeLookupException e) {
        // We know the packages exist so this should not happen
        throw new AssertionError(e);
      }
    }
  }

  @Override
  public void checkValidity() {
    if (!(parent instanceof JPackage || parent instanceof JSession)) {
      throw new JNodeInternalError(this, "Invalid parent");
    }
  }
}
