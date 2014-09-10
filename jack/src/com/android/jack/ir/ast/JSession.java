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


import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.ir.sourceinfo.SourceInfoFactory;
import com.android.jack.lookup.JNodeLookup;
import com.android.jack.lookup.JPhantomLookup;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
  private final JPackage topLevelPackage;

  @Nonnull
  private final JNodeLookup lookup;

  @Nonnull
  private final JPhantomLookup phantomLookup;

  @Nonnull
  private final JArrayType[] primitiveArrays = new JArrayType[JPrimitiveTypeEnum.values().length];

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  @Nonnull
  private final SourceInfoFactory sourceInfoFactory = new SourceInfoFactory();

  @Nonnull
  private final List<Resource> resources = new ArrayList<Resource>();

  public JSession() {
    super(SourceInfo.UNKNOWN);
    topLevelPackage = new JPackage("", this, null);
    topLevelPackage.updateParents(this);
    lookup = new JNodeLookup(topLevelPackage);
    phantomLookup = new JPhantomLookup(lookup);
  }

  /**
   * @return the lookup
   */
  @Nonnull
  public JNodeLookup getLookup() {
    return lookup;
  }

  @Nonnull
  public Tracer getTracer() {
    return tracer;
  }

  /**
   * @return the phantom lookup
   */
  @Nonnull
  public JPhantomLookup getPhantomLookup() {
    return phantomLookup;
  }

  @Nonnull
  public SourceInfoFactory getSourceInfoFactory() {
    return sourceInfoFactory;
  }

  public void addTypeToEmit(@Nonnull JDefinedClassOrInterface type) {
    typesToEmit.add(type);
    type.setExternal(false);
  }

  public void removeTypeToEmit(@Nonnull JDefinedClassOrInterface type) {
    boolean removed = typesToEmit.remove(type);
    assert removed;
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
}
