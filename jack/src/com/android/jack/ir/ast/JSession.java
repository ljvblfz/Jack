/*
 * Copyright 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.android.jack.ir.ast;


import com.google.common.collect.Iterators;

import com.android.jack.Jack;
import com.android.jack.analysis.dependency.file.FileDependencies;
import com.android.jack.analysis.dependency.library.LibraryDependencies;
import com.android.jack.analysis.dependency.type.TypeDependencies;
import com.android.jack.incremental.InputFilter;
import com.android.jack.ir.JNodeInternalError;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.ir.sourceinfo.SourceInfoFactory;
import com.android.jack.jayce.JaycePackageLoader;
import com.android.jack.library.FileType;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.library.InputLibrary;
import com.android.jack.library.OutputJackLibrary;
import com.android.jack.lookup.JNodeLookup;
import com.android.jack.lookup.JPhantomLookup;
import com.android.jack.meta.Meta;
import com.android.jack.reporting.Reporter;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;
import com.android.sched.util.RunnableHooks;
import com.android.sched.util.config.Config;
import com.android.sched.util.config.ReflectFactory;
import com.android.sched.util.config.ThreadConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Storage for information concerning one compilation.
 */
@Description("Representing a compilation")
public class JSession extends JNode {

  @Nonnull
  private final Set<JDefinedClassOrInterface> typesToEmit =
      new HashSet<JDefinedClassOrInterface>();

  @Nonnull
  private JPackage topLevelPackage;

  @Nonnull
  private JNodeLookup lookup;

  @Nonnull
  private JPhantomLookup phantomLookup;

  @Nonnull
  private JArrayType[] primitiveArrays = new JArrayType[JPrimitiveTypeEnum.values().length];

  @Nonnull
  private final SourceInfoFactory sourceInfoFactory = new SourceInfoFactory();

  @Nonnull
  private final List<Resource> resources = new ArrayList<Resource>();

  @Nonnull
  private final List<Meta> metas = new ArrayList<Meta>();

  @Nonnull
  private final Logger userLogger = Logger.getLogger("Jack");

  @CheckForNull
  private Reporter reporter;

  @Nonnull
  private final List<FileType> generatedBinaryKinds = new ArrayList<FileType>(2);

  @CheckForNull
  private OutputJackLibrary jackOutputLibrary;

  @Nonnull
  private final List<InputLibrary> importedLibraries = new ArrayList<InputLibrary>(0);

  @Nonnull
  private final List<InputLibrary> librariesOnClasspath = new ArrayList<InputLibrary>(0);

  @Nonnull
  private final LibraryDependencies libDependencies = new LibraryDependencies();

  @CheckForNull
  private TypeDependencies typeDependencies;

  @CheckForNull
  private FileDependencies fileDependencies;

  @CheckForNull
  private InputFilter inputFilter;

  @CheckForNull
  private RunnableHooks hooks;

  @Nonnull
  private final Config config;

  private boolean mustAbortEventually = false;

  public JSession() {
    super(SourceInfo.UNKNOWN);
    this.config = ThreadConfig.getConfig();
    topLevelPackage = new JPackage("", null);
    topLevelPackage.updateParents(this);
    lookup = new JNodeLookup(topLevelPackage);
    phantomLookup = new JPhantomLookup(lookup);
  }

  public synchronized void reset() {
    typesToEmit.clear();
    topLevelPackage = new JPackage("", null);
    topLevelPackage.updateParents(this);
    lookup = new JNodeLookup(topLevelPackage);
    phantomLookup = new JPhantomLookup(lookup);
    primitiveArrays = new JArrayType[JPrimitiveTypeEnum.values().length];

    ReflectFactory<JaycePackageLoader> importFactory =
        ThreadConfig.getConfig().get(Jack.IMPORT_POLICY);
    for (InputLibrary library : getImportedLibraries()) {
      if (library instanceof InputJackLibrary) {
        topLevelPackage.addLoader(importFactory.create(library, this));
      }
    }

    ReflectFactory<JaycePackageLoader> classpathFactory =
        ThreadConfig.getConfig().get(Jack.CLASSPATH_POLICY);
    for (InputLibrary library : getLibraryOnClasspath()) {
      if (library instanceof InputJackLibrary) {
        topLevelPackage.addLoader(classpathFactory.create(library, this));
      }
    }
  }

  @Nonnull
  public Config getConfig() {
    return config;
  }

  @CheckForNull
  public InputFilter getInputFilter() {
    return inputFilter;
  }

  @Nonnull
  public void setInputFilter(@Nonnull InputFilter inputFilter) {
    this.inputFilter = inputFilter;
  }

  /**
   * Returns the {@link JNodeLookup} instance for this session.
   *
   * It is recommended to use that lookup when searching a type must fail if that type does not
   * exist in Jack.
   *
   * @return a {@link JNodeLookup} instance.
   */
  @Nonnull
  public JNodeLookup getLookup() {
    return lookup;
  }

  /**
   * Returns the {@link JPhantomLookup} instance for this session.
   *
   * It is recommended to use that lookup when searching a type must never fail,
   * even if that type does not exist in Jack. In that case, a {@link JPhantomClassOrInterface}
   * will be created to represent this type.
   *
   * @return a {@link JPhantomLookup} instance.
   */
  @Nonnull
  public JPhantomLookup getPhantomLookup() {
    return phantomLookup;
  }

  @Deprecated
  @Nonnull
  public Logger getUserLogger() {
    return userLogger;
  }

  @Nonnull
  public Reporter getReporter() {
    assert reporter != null;
    return reporter;
  }

  @Nonnull
  public SourceInfoFactory getSourceInfoFactory() {
    return sourceInfoFactory;
  }

  public void abortEventually() {
    mustAbortEventually = true;
  }

  public boolean mustAbortEventually() {
    return mustAbortEventually;
  }

  public void addTypeToEmit(@Nonnull JDefinedClassOrInterface type) {
    typesToEmit.add(type);
    type.setToEmit(true);
  }

  public void removeTypeToEmit(@Nonnull JDefinedClassOrInterface type) {
    boolean removed = typesToEmit.remove(type);
    assert removed;
    type.setToEmit(false);
  }

  @Nonnull
  public Collection<JDefinedClassOrInterface> getTypesToEmit() {
    return typesToEmit;
  }

  @Nonnull
  public JPackage getTopLevelPackage() {
    return topLevelPackage;
  }

  public void addResource(@Nonnull Resource resource) {
    resources.add(resource);
  }

  @Nonnull
  public List<Resource> getResources() {
    return resources;
  }

  public void addMeta(@Nonnull Meta meta) {
    metas.add(meta);
  }

  @Nonnull
  public List<Meta> getMetas() {
    return metas;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      visitor.accept(topLevelPackage);
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    topLevelPackage.traverse(schedule);
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }

  @Nonnull
  synchronized JArrayType getArrayOf(JPrimitiveTypeEnum primitive) {
    assert !primitive.equals(JPrimitiveTypeEnum.VOID);
    if (primitiveArrays[primitive.ordinal()] == null) {
      primitiveArrays[primitive.ordinal()] = new JArrayType(primitive.getType());
    }
    return primitiveArrays[primitive.ordinal()];
  }

  @Override
  public void checkValidity() {
    if (parent != null) {
      throw new JNodeInternalError(this, "Invalid parent");
    }
  }

  @Nonnull
  public OutputJackLibrary getJackOutputLibrary() {
    assert jackOutputLibrary != null;
    return jackOutputLibrary;
  }

  public void setJackOutputLibrary(@Nonnull OutputJackLibrary jackOutputLibrary) {
    this.jackOutputLibrary = jackOutputLibrary;
  }

  @Nonnull
  public List<FileType> getGeneratedFileTypes() {
    return generatedBinaryKinds;
  }

  public void addGeneratedFileType(@Nonnull FileType fileType) {
    generatedBinaryKinds.add(fileType);
  }

  public void addImportedLibrary(@Nonnull InputLibrary source) {
    importedLibraries.add(source);
  }

  @Nonnull
  public List<InputLibrary> getImportedLibraries() {
    return Jack.getUnmodifiableCollections().getUnmodifiableList(importedLibraries);
  }

  public void addLibraryOnClasspath(@Nonnull InputLibrary source) {
    librariesOnClasspath.add(source);
  }

  @Nonnull
  public List<InputLibrary> getLibraryOnClasspath() {
    return Jack.getUnmodifiableCollections().getUnmodifiableList(librariesOnClasspath);
  }


  @Nonnull
  public Iterator<InputLibrary> getPathSources() {
    return Iterators.concat(
        importedLibraries.iterator(),
        librariesOnClasspath.iterator());
  }

  @Nonnull
  public TypeDependencies getTypeDependencies() {
    assert typeDependencies != null;
    return typeDependencies;
  }

  @Nonnull
  public FileDependencies getFileDependencies() {
    assert fileDependencies != null;
    return fileDependencies;
  }

  @Nonnull
  public LibraryDependencies getLibraryDependencies() {
    return libDependencies;
  }

  public void setTypeDependencies(@Nonnull TypeDependencies typeDependencies) {
    this.typeDependencies = typeDependencies;
  }

  public void setFileDependencies(@Nonnull FileDependencies fileDependencies) {
    this.fileDependencies = fileDependencies;
  }

  public void setHooks(@Nonnull RunnableHooks hooks) {
    this.hooks = hooks;
  }

  @Nonnull
  public RunnableHooks getHooks() {
    assert hooks != null;
    return hooks;
  }

  public void setReporter(@Nonnull Reporter reporter) {
    this.reporter = reporter;
  }
}
