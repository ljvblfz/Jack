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

import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.ir.ast.JAsgConcatOperation;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JConcatOperation;
import com.android.jack.ir.ast.JDynamicCastOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JNewInstance;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JPrimitiveType;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.NamingTools;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.collect.Lists;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Removes {@link JConcatOperation} and replaces it by
 * <code>new StringBuilder.append([lhs]).append([rhs])</code>.
 * This is a very simple implementation including no optimization.
 */
@Description("Replace JConcatOperation by new StringBuilder.append([lhs]).append([rhs]).")
@Constraint(no = JAsgConcatOperation.class, need = OriginalNames.class)
@Transform(
    remove = {JConcatOperation.class, ThreeAddressCodeForm.class, NewInstanceRemoved.class}, add = {
        JNewInstance.class, JMethodCall.class, JDynamicCastOperation.class})

public class ConcatRemover implements RunnableSchedulable<JMethod> {

  @Nonnull
  private static final String APPEND_METHOD_NAME = "append";

  @Nonnull
  private static final String STRING_BUILDER_SIGNATURE = "Ljava/lang/StringBuilder;";

  @Nonnull
  private static final String CHAR_SEQUENCE_SIGNATURE = "Ljava/lang/CharSequence;";

  @Nonnull
  private static final String STRING_BUILDER_CONSTRUCTOR_NAME = NamingTools.INIT_NAME;

  @Nonnull
  private static final String TO_STRING = "toString";

  @Nonnull
  private final JClass jlo =
      Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_OBJECT);

  @Nonnull
  private final JClass jls =
      Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_STRING);

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);
  @CheckForNull
  private final JSession session = Jack.getSession();
  @CheckForNull
  private JClassOrInterface stringBuilder;
  @CheckForNull
  private JClassOrInterface charSequence;

  private class Visitor extends JVisitor {

    @Nonnull
    private final JMethod method;

    public Visitor(@Nonnull JMethod method) {
      this.method = method;
    }

    @Override
    public boolean visit(@Nonnull JBinaryOperation binary) {

      if (binary instanceof JConcatOperation) {
        TransformationRequest tr = new TransformationRequest(method);
        SourceInfo sourceInfo = binary.getSourceInfo();
        JNode parent = binary.getParent();
        if (isReplaceableAppend(parent)) {
          JMethodCall toReplace = (JMethodCall) parent;

          JExpression instance = toReplace.getInstance();
          assert instance != null;
          JMethodCall appendLhs =
              getCallToAppend(sourceInfo, instance, binary.getLhs());

          JMethodCall appendRhs = getCallToAppend(sourceInfo, appendLhs, binary.getRhs());
          tr.append(new Replace(toReplace, appendRhs));

        } else {
          JClassOrInterface stringBuilder = getStringBuilder();
          JNewInstance instance = new JNewInstance(sourceInfo, stringBuilder,
              stringBuilder.getOrCreateMethodId(STRING_BUILDER_CONSTRUCTOR_NAME,
                  Lists.<JType>create(), MethodKind.INSTANCE_NON_VIRTUAL));

          JMethodCall appendLhs = getCallToAppend(sourceInfo, instance, binary.getLhs());

          JMethodCall appendRhs = getCallToAppend(sourceInfo, appendLhs, binary.getRhs());

          JMethodId stringBuilderToString =
              stringBuilder.getOrCreateMethodId(TO_STRING, Lists.<JType>create(),
                  MethodKind.INSTANCE_VIRTUAL);
          assert session != null;
          JMethodCall toString = new JMethodCall(sourceInfo,
              appendRhs,
              stringBuilder,
              stringBuilderToString,
              jls,
              stringBuilderToString.canBeVirtual());

          tr.append(new Replace(binary, toString));
        }
        tr.commit();
      }
      return super.visit(binary);
    }

    private boolean isReplaceableAppend(@Nonnull JNode node) {
      if (node instanceof JMethodCall) {
        JMethodCall call = (JMethodCall) node;
        return (call.getMethodName().equals(APPEND_METHOD_NAME)
            && call.getType().isSameType(getStringBuilder())
            && call.getReceiverType().isSameType(getStringBuilder()));
      } else {
        return false;
      }
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    Visitor visitor = new Visitor(method);
    visitor.accept(method);
  }

  @Nonnull
  private JClassOrInterface getStringBuilder() {
    if (stringBuilder == null) {
      assert session != null;
      stringBuilder =
          (JClassOrInterface) session.getPhantomLookup().getType(STRING_BUILDER_SIGNATURE);
    }

    assert stringBuilder != null;
    return stringBuilder;
  }

  @Nonnull
  private JClassOrInterface getCharSequence() {
    if (charSequence == null) {
      assert session != null;
      charSequence =
          (JClassOrInterface) session.getPhantomLookup().getType(CHAR_SEQUENCE_SIGNATURE);
    }

    assert charSequence != null;
    return charSequence;
  }

  @Nonnull
  private JMethodCall getCallToAppend(@Nonnull SourceInfo sourceInfo,
      @Nonnull JExpression stringBuilderInstance,
      @Nonnull JExpression toAppend) {
    JType elementType = toAppend.getType();

    JType appendArgType = elementType;

    assert session != null;
    if (elementType instanceof JPrimitiveType) {
      JPrimitiveTypeEnum primitiveType = ((JPrimitiveType) elementType).getPrimitiveTypeEnum();
      switch (primitiveType) {
        case BOOLEAN:
        case CHAR:
        case INT:
        case LONG:
        case FLOAT:
        case DOUBLE:
          // it's ok nothing to correct.
          break;
        case BYTE:
        case SHORT:
          // append(Z) or append(S) do not exist, apply correction to use append(I)
          toAppend =
              new JDynamicCastOperation(sourceInfo, toAppend, JPrimitiveTypeEnum.INT.getType());
          appendArgType = JPrimitiveTypeEnum.INT.getType();
          break;
        default:
          throw new AssertionError();
      }
    } else if (elementType.isSameType(jls)) {
      appendArgType = jls;
    } else {
      JType charSequence = getCharSequence();
      assert session != null; // FINDBUGS
      if (elementType == charSequence){
        appendArgType = charSequence;
      } else {
        appendArgType = jlo;
      }
    }

    JClassOrInterface stringBuilder = getStringBuilder();
    JMethodId stringBuilderAppend =
        stringBuilder.getOrCreateMethodId(APPEND_METHOD_NAME, Lists.create(appendArgType),
        MethodKind.INSTANCE_VIRTUAL);
    JMethodCall call = new JMethodCall(sourceInfo,
        stringBuilderInstance,
        stringBuilder,
        stringBuilderAppend,
        stringBuilder,
        true /* isVirtualDispatch */);
    call.addArg(toAppend);
    return call;
  }
}
