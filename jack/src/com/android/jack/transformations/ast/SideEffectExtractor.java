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

package com.android.jack.transformations.ast;

import com.android.jack.ir.ast.JArrayRef;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JMultiExpression;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JParameterRef;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.transformations.LocalVarCreator;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * An helper class to extract parts in a expression that have a side-effect.
 */
@Transform(add = {JLocalRef.class,
    JParameterRef.class,
    JAsgOperation.NonReusedAsg.class,
    JMultiExpression.class,
    JFieldRef.class,
    JArrayRef.class}, remove = ThreeAddressCodeForm.class)
@Use(LocalVarCreator.class)
public class SideEffectExtractor {
  @Nonnull
  protected final LocalVarCreator lvCreator;

  public SideEffectExtractor(@Nonnull LocalVarCreator lvCreator) {
    this.lvCreator = lvCreator;
  }

  @CheckForNull
  private JExpression extract(@Nonnull List<JExpression> extracted,
      @CheckForNull JExpression toExtract, @Nonnull TransformationRequest tr) {
    if (toExtract != null) {
      SourceInfo sourceInfo = toExtract.getSourceInfo();
      // TODO(delphinemartin): use cloner if no side-effect
      if (toExtract instanceof JLocalRef) {
        return ((JLocalRef) toExtract).getLocal().makeRef(sourceInfo);
      } else if (toExtract instanceof JParameterRef) {
        return ((JParameterRef) toExtract).getParameter().makeRef(sourceInfo);
      } else {
        JLocal tmp = lvCreator.createTempLocal(toExtract.getType(), sourceInfo, tr);
        JAsgOperation asg = new JAsgOperation(sourceInfo, tmp.makeRef(sourceInfo), toExtract);
        extracted.add(asg);
        tr.append(new Replace(toExtract, tmp.makeRef(sourceInfo)));
        return tmp.makeRef(sourceInfo);
      }
    }
    return null;
  }

  @Nonnull
  private JFieldRef extractInstance(@Nonnull JFieldRef ref, @Nonnull TransformationRequest tr) {
    // a.b => (t = a, t.b)
    SourceInfo sourceInfo = ref.getSourceInfo();
    ArrayList<JExpression> extracted = new ArrayList<JExpression>();
    JExpression newInstance = extract(extracted, ref.getInstance(), tr);
    if (!extracted.isEmpty()) {
      extracted.add((JExpression) ref.getParent());
      JMultiExpression multiExpression = new JMultiExpression(sourceInfo, extracted);
      tr.append(new Replace(ref.getParent(), multiExpression));
    }

    return new JFieldRef(sourceInfo, newInstance, ref.getFieldId(), ref.getReceiverType());
  }

  @Nonnull
  private JArrayRef extractInstanceAndIndex(@Nonnull JArrayRef ref,
      @Nonnull TransformationRequest tr) {
    // a[b] -> t1 = a, t2 = b, t1[t2]
    SourceInfo sourceInfo = ref.getSourceInfo();
    ArrayList<JExpression> extracted = new ArrayList<JExpression>();
    JExpression newInstance = extract(extracted, ref.getInstance(), tr);
    JExpression newIndex = extract(extracted, ref.getIndexExpr(), tr);

    if (!extracted.isEmpty()) {
      extracted.add((JExpression) ref.getParent());
      JMultiExpression multiExpression = new JMultiExpression(sourceInfo, extracted);
      tr.append(new Replace(ref.getParent(), multiExpression));
    }

    assert newInstance != null;
    assert newIndex != null;
    return new JArrayRef(sourceInfo, newInstance, newIndex);
  }

  @Nonnull
  public JExpression copyWithoutSideEffects(@Nonnull JExpression toCopy,
      @Nonnull TransformationRequest tr) {
    SourceInfo sourceInfo = toCopy.getSourceInfo();
    if (toCopy instanceof JParameterRef) {
      JParameter a = ((JParameterRef) toCopy).getParameter();
      return a.makeRef(sourceInfo);
    } else if (toCopy instanceof JLocalRef) {
      JLocal a = ((JLocalRef) toCopy).getLocal();
      return a.makeRef(sourceInfo);
    } else if (toCopy instanceof JFieldRef) {
      return extractInstance((JFieldRef) toCopy, tr);
    } else if (toCopy instanceof JArrayRef) {
      return extractInstanceAndIndex((JArrayRef) toCopy, tr);
    } else {
      throw new AssertionError("Not yet supported");
    }
  }
}
