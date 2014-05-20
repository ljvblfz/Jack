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

package com.android.jack.ecj.loader.jast;

import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JType;
import com.android.jack.lookup.JLookup;
import com.android.jack.lookup.JLookupException;
import com.android.jack.lookup.JNodeLookup;
import com.android.jack.util.NamingTools;

import org.eclipse.jdt.internal.compiler.batch.ClasspathLocation;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.ClasspathSectionProblemReporter;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;

import java.io.File;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Classpath element exposing the content of a jack lookup.
 */
public class JAstClasspath extends ClasspathLocation {

  /**
   * Jack lookup
   */
  @Nonnull
  protected final JNodeLookup lookup;
  @Nonnull
  private final String virtualFilePath;

  public JAstClasspath(@Nonnull String virtualFilePath, @Nonnull JNodeLookup lookup,
      @CheckForNull AccessRuleSet accessRuleSet) {
    super(accessRuleSet, Main.NONE);
    this.lookup = lookup;
    this.virtualFilePath = virtualFilePath;
  }

  /**
   * {@inheritDoc}
   */
  @CheckForNull
  @Override
  public char[][][] findTypeNames(@CheckForNull String qualifiedPackageName) {
    // never called + ecj implementation (3.7.1) always return null
    throw new AssertionError("No yet implemented");
  }

  /**
   * {@inheritDoc}
   */
  @CheckForNull
  @Override
  public NameEnvironmentAnswer findClass(
      @CheckForNull char[] typeName,
      @CheckForNull String qualifiedPackageName,
      @CheckForNull String qualifiedBinaryFileName) {
    return findClass(typeName, qualifiedPackageName, qualifiedBinaryFileName, false);
  }

  /**
   * {@inheritDoc}
   */
  @CheckForNull
  @Override
  public NameEnvironmentAnswer findClass(
      @CheckForNull char[] typeName,
      @CheckForNull String qualifiedPackageName,
      @CheckForNull String qualifiedBinaryFileName,
               boolean asBinaryOnly) {
    assert typeName != null;
    assert qualifiedBinaryFileName != null;
    assert qualifiedPackageName != null;

    String simpleName = new String(typeName);

    String searchedDescriptor;
    if (qualifiedPackageName.isEmpty()) {
      searchedDescriptor = "L" + simpleName + ";";
    } else {
      searchedDescriptor =
          "L" + qualifiedPackageName.replace(File.separatorChar, NamingTools.SIGNATURE_SEPARATOR)
              + NamingTools.SIGNATURE_SEPARATOR + simpleName + ";";
    }

    try {
      JType type = lookup.getType(searchedDescriptor);
      if (type instanceof JDefinedClassOrInterface) {
        assert !(type instanceof JDefinedClass
            && ((JDefinedClass) type).getEnclosingMethod() != null);
        JDefinedClassOrInterface declaredType = (JDefinedClassOrInterface) type;
        AccessRestriction restriction = fetchAccessRestriction(
            LoaderUtils.getQualifiedNameFormatter().getName(declaredType)
            + SuffixConstants.SUFFIX_STRING_CLASS);

        return new NameEnvironmentAnswer(new JAstBinaryType(declaredType, this), restriction);
      } else {
        return null;
      }
    } catch (JLookupException e) {
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isPackage(@CheckForNull String qualifiedPackageName) {
    assert (qualifiedPackageName != null);
    return lookup.isPackageOnPath(
        qualifiedPackageName.replace(File.separatorChar, JLookup.PACKAGE_SEPARATOR));
  }

  /**
   * {@inheritDoc}
   */
  @CheckForNull
  @Override
  public List<? extends Classpath> fetchLinkedJars(
      @CheckForNull ClasspathSectionProblemReporter problemReporter) {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void reset() {
    // nothing to reset for now
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  public char[] normalizedPath() {
    String path = virtualFilePath.replace(File.separatorChar, '/');
    if (path.indexOf('/') < path.indexOf('.')) {
      path = path.substring(0, path.indexOf('.'));
    }
    return path.toCharArray();
  }

  @CheckForNull
  JAstBinaryType findType(@Nonnull JType type) {
    return new JAstBinaryType((JDefinedClassOrInterface) type, this);
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  public String getPath() {
    return virtualFilePath;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void initialize() {
  }
}
