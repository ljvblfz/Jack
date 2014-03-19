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


import com.android.jack.Jack;
import com.android.jack.ir.SourceInfo;
import com.android.jack.ir.SourceOrigin;
import com.android.jack.load.ComposedPackageLoader;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.lookup.JNodeLookup;
import com.android.jack.lookup.JPhantomLookup;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Root for the AST representing an entire Java program.
 */
@Description("Representing an entire Java program")
public class JProgram extends JNode {

  private static final long serialVersionUID = 1L;

  public static final String STATIC_INIT_NAME = "<clinit>";

  public static final String INIT_NAME = "<init>";

  public static boolean isClinit(JMethod method) {
    return method.getName().equals(STATIC_INIT_NAME);
  }

  public final List<JDefinedClass> codeGenTypes = new ArrayList<JDefinedClass>();

  @Nonnull
  private final List<JDefinedClassOrInterface> typesToEmit =
      new ArrayList<JDefinedClassOrInterface>();

  @Nonnull
  private final JPackage topLevelPackage;

  @Nonnull
  private final transient JNodeLookup lookup;

  @Nonnull
  private final transient JPhantomLookup phantomLookup;

  public JProgram() {
    super(SourceOrigin.create(0, 0, JProgram.class.getName()));
    JPrimitiveType.reset();
    topLevelPackage = new JPackage("", this, null);
    topLevelPackage.updateParents(this);
    lookup = new JNodeLookup(topLevelPackage);
    phantomLookup = new JPhantomLookup(lookup);
  }

  @Nonnull
  public ComposedPackageLoader getTopLevelLoader() {
    return topLevelPackage.getLoader();
  }

  /**
   * @return the lookup
   */
  @Nonnull
  public JNodeLookup getLookup() {
    return lookup;
  }

  /**
   * @return the phantom lookup
   */
  @Nonnull
  public JPhantomLookup getPhantomLookup() {
    return phantomLookup;
  }

  public void addTypeToEmit(@Nonnull JDefinedClassOrInterface type) {
    typesToEmit.add(type);
    type.setExternal(false);
    type.updateParents(this);
  }

@Nonnull
  public List<JDefinedClassOrInterface> getTypesToEmit() {
    return typesToEmit;
  }

  @Nonnull
  public JPackage getTopLevelPackage() {
    return topLevelPackage;
  }

  public JStringLiteral getLiteralString(SourceInfo sourceInfo, char[] s) {
    return getLiteralString(sourceInfo, String.valueOf(s));
  }

  public JStringLiteral getLiteralString(SourceInfo sourceInfo, String s) {
    return new JStringLiteral(sourceInfo, s);
  }

  public boolean isJavaLangString(JType type) {
    JClass jls = Jack.getProgram().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_STRING);
    return type == jls;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      visitor.accept(topLevelPackage);
      visitor.accept(typesToEmit);
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    for (JDefinedClassOrInterface type : typesToEmit) {
      type.traverse(schedule);
    }
    topLevelPackage.traverse(schedule);
  }

  @Override
  protected void transform(@Nonnull JNode existingNode, @CheckForNull JNode newNode,
      @Nonnull Transformation transformation) throws UnsupportedOperationException {
    if (!transform(typesToEmit, existingNode, (JDefinedClassOrInterface) newNode,
        transformation)) {
      super.transform(existingNode, newNode, transformation);
    }
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }
}
