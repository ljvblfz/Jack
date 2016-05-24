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

package com.android.jack.backend.dex;

import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.backend.dex.rop.RopHelper;
import com.android.jack.dx.dex.file.ClassDefItem;
import com.android.jack.dx.rop.code.AccessFlags;
import com.android.jack.dx.rop.cst.CstString;
import com.android.jack.dx.rop.cst.CstType;
import com.android.jack.dx.rop.type.TypeList;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.scheduling.marker.ClassDefItemMarker;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.Access;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.Protect;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.FileUtils;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Builds a {@code ClassDefItem} instance from a {@code JDefinedClassOrInterface} and
 * adds it to the {@code DexFile} currently being built.
 *
 * <p>The {@code ClassDefItem} instance is initialized with name, modifiers,
 * superclass, interfaces and source file information. It does not contain any
 * field or method.
 *
 * <p>Its {@code EncodedMethod}s are created by the {@code EncodedMethodBuilder}
 * class.
 *
 * @see EncodedMethodBuilder
 */
@Description("Builds ClassDefItem from JDeclaredType.")
@Name("ClassDefItemBuilder")
@Synchronized
@Transform(add = ClassDefItemMarker.class)
@Protect(add = JDefinedClassOrInterface.class, modify = JDefinedClassOrInterface.class,
    remove = JDefinedClassOrInterface.class)
@Filter(TypeWithoutPrebuiltFilter.class)
//Access isAnonymous which may depend on TypeName that is accessing enclosing type name.
@Access(JSession.class)
public class ClassDefItemBuilder implements RunnableSchedulable<JDefinedClassOrInterface> {

  private final boolean emitSourceFileInfo =
      ThreadConfig.get(Options.EMIT_SOURCE_FILE_DEBUG_INFO).booleanValue();

  /**
   * Creates the {@code ClassDefItem} for the given {@code JDeclaredType}. The
   * created {@code ClassDefItem} instance is then attached to the {@code JDeclaredType}
   * in a {@code ClassDefItemMarker} to be accessible from other schedulables.
   *
   * <p>If the given type is external (not declared in the source files being
   * compiled), it is ignored. No {@code ClassDefItem} is created for it and no
   * marker is attached to it.
   *
   * @param declaredType a non-null {@code JDeclaredType} for which a {@code ClassDefItem}
   * is created.
   * @throws Exception if any exception is thrown while building the {@code ClassDefItem}.
   */
  @Override
  public synchronized void run(@Nonnull JDefinedClassOrInterface declaredType) throws Exception {
    ClassDefItem classDefItem = createClassDefItem(declaredType);
    ClassDefItemMarker classDefItemMarker = new ClassDefItemMarker(classDefItem);
    declaredType.addMarker(classDefItemMarker);
  }

  @Nonnull
  private ClassDefItem createClassDefItem(@Nonnull JDefinedClassOrInterface type)
      throws JTypeLookupException {
    CstType thisClass = RopHelper.getCstType(type);
    CstType superclassType = createSuperClass(type);
    int accessFlags = getDxAccessFlagsForType(type);
    TypeList interfaces = createInterfacesList(type);
    CstString sourceFile = null;
    if (emitSourceFileInfo) {
      sourceFile = createSourceFile(type);
    }
    ClassDefItem classDefItem = new ClassDefItem(thisClass, accessFlags,
        superclassType, interfaces, sourceFile);
    return classDefItem;
  }

  @CheckForNull
  private static CstType createSuperClass(@Nonnull JDefinedClassOrInterface type)
      throws JTypeLookupException {
    JClass superClass = type.getSuperClass();
    if (superClass == null) {
      if (type instanceof JDefinedInterface) {
        return RopHelper.getCstType(
            Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_OBJECT));
      } else {
        assert type.isSameType(
            Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_OBJECT));
        return null;
      }
    }
    CstType superclassType = RopHelper.getCstType(superClass);
    return superclassType;
  }

  @Nonnull
  private static TypeList createInterfacesList(@Nonnull JDefinedClassOrInterface type) {
    List<JInterface> interfacesList = type.getImplements();
    return RopHelper.createTypeList(interfacesList);
  }

  @CheckForNull
  private static CstString createSourceFile(@Nonnull JDefinedClassOrInterface type) {
    CstString sourceFile = null;
    SourceInfo sourceInfo = type.getSourceInfo();

    // Only keep filename without the path
    String sourceFileName = sourceInfo.getFileName();
    String fileSeparator = FileUtils.getFileSeparator();
    int separatorPos = sourceFileName.lastIndexOf(fileSeparator);
    if (separatorPos > 0) {
      sourceFileName = sourceFileName.substring(separatorPos + 1);
    }
    sourceFile = new CstString(sourceFileName);

    return sourceFile;
  }

  private int getDxAccessFlagsForType(@Nonnull JDefinedClassOrInterface type) {
    int accessFlags = type.getModifier();

    boolean isInner = type.getEnclosingType() != null;

    // A protected inner class becomes public
    if (isInner && type.isProtected()) {
        accessFlags |= JModifier.PUBLIC;
    }

    // An anonymous class should not be flagged as final unless it is static
    if (type.isAnonymous()) {

      if (!type.isStatic()) {
        accessFlags &= ~JModifier.FINAL;
      }
    }

    // Remove GWT-specific flags and flags that are not allowed in a class_def_item
    // If it is an inner class, this will remove its static, private or protected flags.
    // Those should only be found in InnerClass annotations.
    accessFlags &= AccessFlags.CLASS_FLAGS;

    return accessFlags;
  }
}
