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

package com.android.jack.transformations.lambda;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JLambda;
import com.android.jack.ir.formatter.TypeFormatter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.Nonnull;

/**
 * Represents an interface signature which is a set of interfaces implemented
 * by a particular lambda class, i.e. essentially the transitive closure of the
 * interfaces the class explicitly specifies in its 'implements' clause.
 *
 * NOTE: interface signature is used for hashing and sorting purposes only, it is
 * not intended to actually store the exact set of the interfaces.
 */
final class LambdaInterfaceSignature {
  @Nonnull
  private static final TypeFormatter FORMATTER = Jack.getLookupFormatter();

  /**
   * The interface signature is represented as a sorted list of the interface
   * binary names from the normalized set of the interfaces in the signature,
   * see details below. This may be revised when JType supports hashing.
   */
  @Nonnull
  private final String allInterfaces;

  private LambdaInterfaceSignature(@Nonnull JLambda lambda) {
    StringBuilder sb = new StringBuilder();
    List<JInterface> interfaces = new ArrayList<>(lambda.getInterfaceBounds());
    interfaces.add(lambda.getType());
    for (String key : getNormalizedInterfacesMap(interfaces).keySet()) {
      sb.append(key);
    }
    allInterfaces = sb.toString();
  }

  /** create an interface signature for a given lambda */
  static LambdaInterfaceSignature forLambda(@Nonnull JLambda lambda) {
    return new LambdaInterfaceSignature(lambda);
  }

  /** A short id that can be used to uniquely identify and sort interface signatures */
  @Nonnull
  String getUniqueId() {
    return allInterfaces;
  }

  /**
   * Returns a normalized set of interfaces implemented by a given list of lambdas.
   *
   * We call a set of interfaces normalized if removing any of the interfaces from
   * this set affects the full set of the interfaces represented by interface signature.
   *
   * For example: given an interface hierarchy like { IA; IB; IC: IA, IB; ID: IA },
   * normalizeInterfaces({IA, IB, IC, ID}) returns {IC, ID}.
   *
   * NOTE: the resulting list of the interfaces will be sorted by their binary names
   * to ensure it is stable.
   */
  @Nonnull
  static List<JInterface> normalizeInterfaces(@Nonnull List<JLambda> lambdas) {
    ArrayList<JInterface> interfaces = new ArrayList<>();
    for (JLambda lambda : lambdas) {
      interfaces.addAll(lambda.getInterfaceBounds());
      interfaces.add(lambda.getType());
    }
    return new ArrayList<>(getNormalizedInterfacesMap(interfaces).values());
  }

  /** Returns a sorted map of normalized (see above) interfaces */
  @Nonnull
  private static TreeMap<String, JInterface> getNormalizedInterfacesMap(
      @Nonnull List<JInterface> interfaces) {
    // Interfaces hidden by some other interfaces
    Set<String> hidden = new HashSet<>();

    // Walk each interface hierarchy, collect top level interfaces
    TreeMap<String, JInterface> map = new TreeMap<>();
    for (JInterface inter : interfaces) {
      map.put(collectHiddenInterfaces(hidden, inter, true), inter);
    }

    // Filter out top level hidden interfaces
    Iterator<Map.Entry<String, JInterface>> iterator = map.entrySet().iterator();
    while (iterator.hasNext()) {
      if (hidden.contains(iterator.next().getKey())) {
        iterator.remove();
      }
    }
    return map;
  }

  /** Walk the interface hierarchy and collect hidden interfaces, return interface signature */
  @Nonnull
  private static String collectHiddenInterfaces(
      @Nonnull Set<String> hidden, @Nonnull JInterface inter, boolean isTopLevel) {
    String signature = FORMATTER.getName(inter);
    if (hidden.contains(signature)) {
      if (inter instanceof JDefinedInterface) {
        for (JInterface implemented : ((JDefinedInterface) inter).getImplements()) {
          collectHiddenInterfaces(hidden, implemented, false);
        }
      }

      if (!isTopLevel) {
        hidden.add(signature);
      }
    }
    return signature;
  }
}
