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

package com.android.jack.shrob.spec;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Class representing a shrob flags file.
 */
public class Flags {

  private boolean shrink = true;

  private boolean obfuscate = true;

  private boolean keepParameterNames = false;

  private boolean useMixedCaseClassName = true;

  @CheckForNull
  private File obfuscationMapping = null;

  private boolean printMapping = false;

  private boolean useUniqueClassMemberNames = false;

  @CheckForNull
  private String packageForRenamedClasses = null;

  @CheckForNull
  private String packageForFlatHierarchy = null;

  @CheckForNull
  private String libraryJars = null;

  @Nonnull
  private final List<File> inJars = new ArrayList<File>(1);

  @Nonnull
  private final List<File> outJars = new ArrayList<File>(1);

  @CheckForNull
  private File outputMapping;

  @CheckForNull
  private File obfuscationDictionary;

  @CheckForNull
  private File classObfuscationDictionary;

  @CheckForNull
  private File packageObfuscationDictionary;

  @CheckForNull
  private FilterSpecification keepAttributes;

  @CheckForNull
  private FilterSpecification keepPackageNames;

  @CheckForNull
  private FilterSpecification adaptClassStrings;

  @Nonnull
  private final List<ClassSpecification> keepClassSpecs
  = new ArrayList<ClassSpecification>();

  @Nonnull
  private final List<ClassSpecification> keepClassesWithMembersSpecs
  = new ArrayList<ClassSpecification>();

  @Nonnull
  private final List<ClassSpecification> keepClassMembersSpecs
  = new ArrayList<ClassSpecification>();

  private boolean printSeeds = false;

  @CheckForNull
  private File seedsFile;

  @CheckForNull
  private FilterSpecification adaptResourceFileNames;

  public void setShrink(boolean shrink) {
    this.shrink = shrink;
  }

  public void setPrintMapping(boolean printMapping) {
    this.printMapping = printMapping;
  }

  public boolean printMapping() {
    return this.printMapping;
  }

  public void setOutputMapping(@CheckForNull File outputMapping) {
    this.outputMapping = outputMapping;
  }

  public boolean shrink() {
    return shrink;
  }

  public void setObfuscate(boolean obfuscate) {
    this.obfuscate = obfuscate;
  }

  public boolean obfuscate() {
    return obfuscate;
  }

  public void setKeepParameterNames(boolean keepParameterNames) {
    this.keepParameterNames = keepParameterNames;
  }

  public boolean getKeepParameterNames() {
    if (obfuscate) {
      return keepParameterNames;
    } else {
      return true;
    }
  }

  public void setObfuscationMapping(@CheckForNull File obfuscationMapping) {
    this.obfuscationMapping = obfuscationMapping;
  }

  public void setUseMixedCaseClassName(boolean useMixedCaseClassName) {
    this.useMixedCaseClassName = useMixedCaseClassName;
  }

  public void setUseUniqueClassMemberNames(boolean useUniqueClassMemberNames) {
    this.useUniqueClassMemberNames = useUniqueClassMemberNames;
  }

  public void addInJars(@Nonnull List<File> inJars) {
    this.inJars.addAll(inJars);
  }

  public void addOutJars(@Nonnull List<File> outJars) {
    this.outJars.addAll(outJars);
  }

  public void addLibraryJars(@Nonnull String libraryJars) {
    if (this.libraryJars == null) {
      this.libraryJars = libraryJars;
    } else {
      this.libraryJars += File.pathSeparatorChar + libraryJars;
    }
  }

  public boolean getUseUniqueClassMemberNames() {
    return this.useUniqueClassMemberNames;
  }

  public boolean getUseMixedCaseClassName() {
    return this.useMixedCaseClassName;
  }

  @Nonnull
  public List<File> getInJars() {
    return inJars;
  }

  @Nonnull
  public List<File> getOutJars() {
    return outJars;
  }

  @CheckForNull
  public String getLibraryJars() {
    return libraryJars;
  }

  public File getObfuscationMapping() {
    return obfuscationMapping;
  }

  public File getOutputMapping() {
    return outputMapping;
  }

  public File getObfuscationDictionary() {
    return obfuscationDictionary;
  }

  public void setObfuscationDictionary(@CheckForNull File obfuscationDictionary) {
    this.obfuscationDictionary = obfuscationDictionary;
  }

  public File getPackageObfuscationDictionary() {
    return packageObfuscationDictionary;
  }

  public void setPackageObfuscationDictionary(@CheckForNull File packageObfuscationDictionary) {
    this.packageObfuscationDictionary = packageObfuscationDictionary;
  }

  public File getClassObfuscationDictionary() {
    return classObfuscationDictionary;
  }

  public void setClassObfuscationDictionary(@CheckForNull File classObfuscationDictionary) {
    this.classObfuscationDictionary = classObfuscationDictionary;
  }

  public void setPackageForRenamedClasses(@CheckForNull String packageForRenamedClasses) {
    this.packageForRenamedClasses = packageForRenamedClasses;
    if (packageForRenamedClasses != null) {
      // packageForRenamedClasses overrides packageForFlatHierarchy
      this.packageForFlatHierarchy = null;
    }
  }

  @CheckForNull
  public String getPackageForRenamedClasses() {
    return packageForRenamedClasses;
  }

  public void setPackageForFlatHierarchy(@CheckForNull String packageForFlatHierarchy) {
    if (packageForRenamedClasses == null) {
      // packageForRenamedClasses overrides packageForFlatHierarchy
      this.packageForFlatHierarchy = packageForFlatHierarchy;
    } else {
      assert this.packageForFlatHierarchy == null;
    }
  }

  @CheckForNull
  public String getPackageForFlatHierarchy() {
    return packageForFlatHierarchy;
  }

  @CheckForNull
  public FilterSpecification getKeepAttributes() {
    return keepAttributes;
  }

  @Nonnull
  public List<ClassSpecification> getKeepClassSpecs() {
    return keepClassSpecs;
  }

  @Nonnull
  public List<ClassSpecification> getKeepClassesWithMembersSpecs() {
    return keepClassesWithMembersSpecs;
  }

  @Nonnull
  public List<ClassSpecification> getKeepClassMembersSpecs() {
    return keepClassMembersSpecs;
  }

  public void addKeepClassSpecification(
      @CheckForNull ClassSpecification classSpecification) {
    assert classSpecification != null;
    keepClassSpecs.add(classSpecification);
  }

  public void addKeepClassesWithMembers(
      @CheckForNull ClassSpecification classSpecification) {
    assert classSpecification != null;
    keepClassesWithMembersSpecs.add(classSpecification);
  }

  public void addKeepClassMembers(
      @CheckForNull ClassSpecification classSpecification) {
    assert classSpecification != null;
    keepClassMembersSpecs.add(classSpecification);
  }

  public void setKeepAttribute(@CheckForNull FilterSpecification attribute) {
    keepAttributes = attribute;
  }

  public void setKeepPackageName(@CheckForNull FilterSpecification packageSpec) {
    keepPackageNames = packageSpec;
  }

  public FilterSpecification getKeepPackageNames() {
    return keepPackageNames;
  }

  public void addKeepPackageNames(@Nonnull NameSpecification packageName, boolean negator) {
    if (keepPackageNames == null) {
      keepPackageNames = new FilterSpecification();
    }
    keepPackageNames.addElement(packageName, negator);
  }

  public boolean keepAttribute(@Nonnull String attributeName) {
    if (obfuscate) {
      if (keepAttributes != null) {
        return keepAttributes.matches(attributeName);
      }
      return false;
    } else {
      return true;
    }
  }

  @CheckForNull
  public FilterSpecification getAdaptClassStrings() {
    return adaptClassStrings;
  }

  public void setAdaptClassStrings(@CheckForNull FilterSpecification adaptClassStrings) {
    this.adaptClassStrings = adaptClassStrings;
  }

  public boolean printSeeds() {
    return printSeeds;
  }

  public void setPrintSeeds(boolean printSeeds) {
    this.printSeeds = printSeeds;
  }

  @CheckForNull
  public File getSeedsFile() {
    return seedsFile;
  }

  public void setSeedsFile(@CheckForNull File seedsFile) {
    this.seedsFile = seedsFile;
  }

  public void adaptResourceFileNames(@CheckForNull FilterSpecification filter) {
    this.adaptResourceFileNames = filter;
  }

  @CheckForNull
  public FilterSpecification getAdaptResourceFileNames() {
    return adaptResourceFileNames;
  }
}