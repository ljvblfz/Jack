/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.transformations.renamepackage;

import com.android.jack.ir.ast.JAbstractStringLiteral;
import com.android.jack.ir.ast.JAnnotationLiteral;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JStringLiteral;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.Resource;
import com.android.jack.ir.formatter.BinaryQualifiedNameFormatter;
import com.android.jack.ir.formatter.TypeFormatter;
import com.android.jack.lookup.JLookup;
import com.android.jack.transformations.Jarjar;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.NamingTools;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.codec.PathCodec;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.vfs.VPath;
import com.tonicsystems.jarjar.PackageRemapper;
import com.tonicsystems.jarjar.PatternElement;
import com.tonicsystems.jarjar.RulesFileParser;
import com.tonicsystems.jarjar.Wildcard;

import java.io.File;
import java.util.List;
import java.util.Stack;

import javax.annotation.Nonnull;

/**
 * This {@code RunnableSchedulable} uses jarjar rules file of modules to rename packages.
 */
@HasKeyId
@Description("Uses jarjar rules file of modules to rename packages.")
@Name("PackageRenamer")
@Support(Jarjar.class)
@Transform(add = {JStringLiteral.class, JPackage.class}, modify = JDefinedClassOrInterface.class)
public class PackageRenamer implements RunnableSchedulable<JSession>{

  @Nonnull
  public static final PropertyId<File> JARJAR_FILE = PropertyId.create(
      "jack.jarjar.file", "The jarjar rules file", new PathCodec())
      .addDefaultValue("jarjar.txt");

  @Nonnull
  private final File jarjarRulesFile = ThreadConfig.get(JARJAR_FILE);

  private static class Visitor extends JVisitor {

    @Nonnull
    private final PackageRemapper remapper;
    @Nonnull
    private final Stack<JNode> transformationRequestRoot = new Stack<JNode>();

    @Nonnull
    private final JLookup lookup;
    @Nonnull
    private final TypeFormatter formatter = BinaryQualifiedNameFormatter.getFormatter();

    public Visitor(@Nonnull JLookup lookup, @Nonnull PackageRemapper remapper) {
      this.lookup = lookup;
      this.remapper = remapper;
    }

    @Override
    public void endVisit(@Nonnull JDefinedClassOrInterface type) {
      String binaryName = remapper.mapValue(formatter.getName(type));
      String simpleName = NamingTools.getSimpleClassNameFromBinaryName(binaryName);
      type.setName(simpleName);
      type.getEnclosingPackage().remove(type);
      String packageName = NamingTools.getPackageNameFromBinaryName(binaryName);
      JPackage newPackage = lookup.getOrCreatePackage(packageName);
      type.setEnclosingPackage(newPackage);
      newPackage.addType(type);
    }

    @Override
    public boolean visit(@Nonnull JAnnotationLiteral annotationLiteral) {
      transformationRequestRoot.push(annotationLiteral);
      return super.visit(annotationLiteral);
    }

    @Override
    public void endVisit(@Nonnull JAnnotationLiteral annotationLiteral) {
      assert annotationLiteral == transformationRequestRoot.peek();
      transformationRequestRoot.pop();
      super.endVisit(annotationLiteral);
    }

    @Override
    public boolean visit(@Nonnull JMethod method) {
      transformationRequestRoot.push(method);
      return super.visit(method);
    }

    @Override
    public void endVisit(@Nonnull JMethod x) {
      assert x == transformationRequestRoot.peek();
      transformationRequestRoot.pop();
      super.endVisit(x);
    }

    @Override
    public void endVisit(@Nonnull JAbstractStringLiteral x) {
      assert !transformationRequestRoot.isEmpty();

      TransformationRequest tr = new TransformationRequest(transformationRequestRoot.peek());
      String newValue = remapper.mapValue(x.getValue());
      tr.append(new Replace(x, new JStringLiteral(x.getSourceInfo(), newValue)));
      tr.commit();

      super.endVisit(x);
    }
  }

  @Override
  public void run(@Nonnull JSession session) throws Exception {
    List<PatternElement> result = RulesFileParser.parse(jarjarRulesFile);
    List<Wildcard> wildcards = PatternElement.createWildcards(result);
    PackageRemapper remapper = new PackageRemapper(wildcards);

    new Visitor(session.getLookup(), remapper).accept(session.getTypesToEmit());

    for (Resource res : session.getResources()) {
      String pathToTransform = res.getPath().getPathAsString('/');
      String transformedPath = remapper.mapValue(pathToTransform);
      res.setPath(new VPath(transformedPath, '/'));
    }

    session.getLookup().clear();
    session.getPhantomLookup().clear();
  }
}
