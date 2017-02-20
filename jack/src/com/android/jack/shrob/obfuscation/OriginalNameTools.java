/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.shrob.obfuscation;

import com.google.common.base.Strings;

import com.android.jack.ir.ast.HasName;
import com.android.jack.ir.ast.JArrayType;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JType;
import com.android.sched.marker.LocalMarkerManager;

import javax.annotation.Nonnull;

/**
 * A class regrouping all the tools used to retrieve original names.
 */
public class OriginalNameTools {

  private static final char PACKAGE_SEPARATOR = '.';

  public static void appendOriginalQualifiedName(
      @Nonnull StringBuilder nameBuilder, @Nonnull JPackage pack) {
    JPackage enclosingPackage;
    OriginalPackageMarker marker = pack.getMarker(OriginalPackageMarker.class);
    if (marker != null) {
      enclosingPackage = marker.getOriginalEnclosingPackage();
    } else {
      enclosingPackage = pack.getEnclosingPackage();
    }
    if (enclosingPackage != null && !enclosingPackage.isTopLevelPackage()) {
      appendOriginalQualifiedName(nameBuilder, enclosingPackage);
      nameBuilder.append(PACKAGE_SEPARATOR);
    }
    appendOriginalName(nameBuilder, pack);
  }

  public static void appendOriginalQualifiedName(
      @Nonnull StringBuilder nameBuilder, @Nonnull JClassOrInterface type) {
    JPackage enclosingPackage;
    OriginalPackageMarker marker = ((JNode) type).getMarker(OriginalPackageMarker.class);
    if (marker != null) {
      enclosingPackage = marker.getOriginalEnclosingPackage();
    } else {
      enclosingPackage = type.getEnclosingPackage();
    }
    assert enclosingPackage != null;
    appendOriginalQualifiedName(nameBuilder, enclosingPackage);
    if (!enclosingPackage.isTopLevelPackage()) {
      nameBuilder.append(PACKAGE_SEPARATOR);
    }
    appendOriginalName(nameBuilder, type);
  }

  public static void appendOriginalName(@Nonnull StringBuilder nameBuilder, @Nonnull HasName node) {
    OriginalNameMarker marker = ((LocalMarkerManager) node).getMarker(OriginalNameMarker.class);
    if (marker != null) {
      nameBuilder.append(marker.getOriginalName());
    } else {
      nameBuilder.append(node.getName());
    }
  }

  public static void appendOriginalQualifiedName(
      @Nonnull StringBuilder nameBuilder, @Nonnull HasName node) {
    if (node instanceof JArrayType) {
      JArrayType arrayType = (JArrayType) node;
      appendOriginalQualifiedName(nameBuilder, arrayType.getLeafType());
      nameBuilder.append(Strings.repeat("[]", arrayType.getDims()));
    } else if (node instanceof JDefinedClassOrInterface) {
      appendOriginalQualifiedName(nameBuilder, (JClassOrInterface) node);
    } else if (node instanceof JType) {
      nameBuilder.append(node.getName());
    } else {
      appendOriginalName(nameBuilder, node);
    }
  }

}

