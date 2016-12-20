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

package com.android.jack.backend.dex.annotations;

import com.android.jack.Jack;
import com.android.jack.backend.dex.DexAnnotations;
import com.android.jack.backend.dex.annotations.tag.ParameterMetadataAnnotation;
import com.android.jack.backend.dex.annotations.tag.ParameterMetadataFeature;
import com.android.jack.debug.DebugVariableInfoMarker;
import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JAnnotationType;
import com.android.jack.ir.ast.JArrayLiteral;
import com.android.jack.ir.ast.JIntLiteral;
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JNameValuePair;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JRetentionPolicy;
import com.android.jack.ir.ast.JStringLiteral;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.library.DumpInLibrary;
import com.android.jack.library.PrebuiltCompatibility;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.transformations.request.AddAnnotation;
import com.android.jack.transformations.request.PutNameValuePair;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.Protect;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.With;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.id.BooleanPropertyId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Add "system annotation" to provide parameter metadata such as parameter names and modifiers.
 */
@HasKeyId
@Description("Add annotations for parameter metadata")
@Constraint(need = DebugVariableInfoMarker.class)
@Transform(add = {ParameterMetadataAnnotation.class, JAnnotation.class, JNameValuePair.class,
                  JStringLiteral.class, JIntLiteral.class, JArrayLiteral.class})
@Protect(add = {JMethod.class, JParameter.class},
    unprotect = @With(remove = ParameterMetadataAnnotation.class))
@Filter(TypeWithoutPrebuiltFilter.class)
@Support(ParameterMetadataFeature.class)
public class ParameterMetadataAnnotationsAdder implements RunnableSchedulable<JMethod> {

  @Nonnull
  public static final BooleanPropertyId PARAMETER_ANNOTATION = BooleanPropertyId
      .create("jack.dex.parameter.annotations", "Emit parameter annotations")
      .addDefaultValue(Boolean.FALSE).addCategory(DumpInLibrary.class)
      .addCategory(PrebuiltCompatibility.class);

  @Nonnull
  JAnnotationType methodParametersAnnotationType = Jack.getSession().getPhantomLookup()
      .getAnnotationType(DexAnnotations.ANNOTATION_METHOD_PARAMETERS);

  @Nonnull
  JMethodIdWide namesMethodIdWide = methodParametersAnnotationType.getOrCreateMethodIdWide(
      "names", Collections.<JType>emptyList(), MethodKind.INSTANCE_VIRTUAL);

  @Nonnull
  JMethodIdWide accessFlagsMethodIdWide = methodParametersAnnotationType.getOrCreateMethodIdWide(
      "accessFlags", Collections.<JType>emptyList(), MethodKind.INSTANCE_VIRTUAL);

  private static class Visitor extends JVisitor {

    private final List<JLiteral> names;
    private final List<JLiteral> accessFlags;
    private final SourceInfo si = SourceInfo.UNKNOWN;

    public Visitor(@Nonnull int parameterCount) {
      names = new ArrayList<>(parameterCount);
      accessFlags = new ArrayList<>(parameterCount);
    }

    @Override
    public boolean visit(@Nonnull JMethod method) {
      for (JParameter parameter : method.getParams()) {
        addParameterName(parameter.isNamePresent() ? parameter.getName() : null);
        // Remove NAME_PRESENT modifier since it is only an internal modifier
        accessFlags.add(new JIntLiteral(si, parameter.getModifier() & ~JModifier.NAME_PRESENT));
      }

      return super.visit(method);
    }

    private void addParameterName(@CheckForNull String parameterName) {
      if (parameterName != null) {
        names.add(new JStringLiteral(si, parameterName));
      } else {
        names.add(new JNullLiteral(si));
      }
    }
  }

  @Override
  public void run(JMethod method) {
    Visitor visitor = new Visitor(method.getParams().size());
    visitor.accept(method);

    writeParameterAnnotation(method, visitor.names, visitor.accessFlags);
  }

  private void writeParameterAnnotation(@Nonnull JMethod method, @Nonnull List<JLiteral> names,
      @Nonnull List<JLiteral> accessFlags) {
    TransformationRequest tr = new TransformationRequest(method);
    SourceInfo si = SourceInfo.UNKNOWN;

    JAnnotation annotation =
        new JAnnotation(si, JRetentionPolicy.SYSTEM, methodParametersAnnotationType);
    tr.append(new AddAnnotation(annotation, method));

    JNameValuePair namesPair =
        new JNameValuePair(si, namesMethodIdWide, new JArrayLiteral(si, names));
    tr.append(new PutNameValuePair(annotation, namesPair));

    JNameValuePair accessFlagsPair =
        new JNameValuePair(si, accessFlagsMethodIdWide, new JArrayLiteral(si, accessFlags));
    tr.append(new PutNameValuePair(annotation, accessFlagsPair));

    tr.commit();
  }
}
